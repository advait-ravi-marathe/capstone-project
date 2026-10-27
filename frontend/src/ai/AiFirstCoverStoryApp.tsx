import { useEffect, useState } from 'react';
import type { FormEvent } from 'react';

type Session = { ownerId: string };
type Citation = { documentId: string; version: string; section: string };
type AssistantResponse = {
  answer: string;
  citations: Citation[];
  abstained: boolean;
  executionMode: string;
  providerRequestMade: boolean;
  liveQualityEvaluated: boolean;
};

const safeMessage = (code: string) => {
  if (code === 'AI_PROVIDER_UNAVAILABLE') return 'The local AI service is unavailable. No answer was produced.';
  if (code === 'INVALID_QUESTION') return 'Enter a short nonblank question.';
  if (code === 'UNAUTHENTICATED') return 'Select a synthetic training owner to continue.';
  return 'The request could not be completed.';
};

export function AiFirstCoverStoryApp() {
  const [ownerId, setOwnerId] = useState<string | null>(null);
  const [csrf, setCsrf] = useState<{ headerName: string; token: string } | null>(null);
  const [question, setQuestion] = useState('');
  const [result, setResult] = useState<AssistantResponse | null>(null);
  const [message, setMessage] = useState('Loading local training session…');
  const [busy, setBusy] = useState(false);

  useEffect(() => {
    let active = true;
    fetch('/api/session', { credentials: 'same-origin' })
      .then(async (response) => {
        if (!response.ok) throw new Error('UNAUTHENTICATED');
        return response.json() as Promise<Session>;
      })
      .then((session) => {
        if (active) {
          setOwnerId(session.ownerId);
          setMessage('Local synthetic session ready.');
        }
        return fetch('/api/csrf', { credentials: 'same-origin' })
          .then(async (response) => {
            if (!response.ok) throw new Error('CSRF_UNAVAILABLE');
            return response.json() as Promise<{ headerName: string; token: string }>;
          })
          .then((value) => { if (active) setCsrf(value); });
      })
      .catch(() => {
        if (active) setMessage('Select one of the fixed synthetic owners.');
      });
    return () => { active = false; };
  }, []);

  async function selectOwner(nextOwner: string) {
    setBusy(true);
    setMessage('');
    try {
      const csrfResponse = await fetch('/api/csrf', { credentials: 'same-origin' });
      if (!csrfResponse.ok) throw new Error('UNAUTHENTICATED');
      const csrfValue = await csrfResponse.json() as { headerName: string; token: string };
      const response = await fetch('/api/session', {
        method: 'POST',
        credentials: 'same-origin',
        headers: { 'Content-Type': 'application/json', [csrfValue.headerName]: csrfValue.token },
        body: JSON.stringify({ ownerId: nextOwner }),
      });
      if (!response.ok) throw new Error('UNAUTHENTICATED');
      const session = await response.json() as Session;
      setOwnerId(session.ownerId);
      setCsrf(csrfValue);
      setMessage('Local synthetic session ready.');
    } catch {
      setMessage('The local session could not be started.');
    } finally {
      setBusy(false);
    }
  }

  async function ask(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    if (!ownerId || !csrf || !question.trim()) return;
    setBusy(true);
    setMessage('');
    setResult(null);
    try {
      const response = await fetch('/api/ai/policy-assistant', {
        method: 'POST',
        credentials: 'same-origin',
        headers: { 'Content-Type': 'application/json', [csrf.headerName]: csrf.token },
        body: JSON.stringify({ question }),
      });
      const body = await response.json() as AssistantResponse | { code?: string };
      if (!response.ok) {
        setMessage(safeMessage('code' in body ? (body.code ?? '') : ''));
        return;
      }
      setResult(body as AssistantResponse);
    } catch {
      setMessage('The request could not reach the local application.');
    } finally {
      setBusy(false);
    }
  }

  return (
    <main>
      <h1>CoverStory AI — local training</h1>
      <p role="note">Synthetic policy text only. Offline fixture responses are not model output, insurance advice, or a coverage decision.</p>
      {!ownerId ? (
        <section aria-label="Synthetic session">
          <p>{message}</p>
          <button type="button" disabled={busy} onClick={() => void selectOwner('owner-a')}>Use owner-a</button>
          <button type="button" disabled={busy} onClick={() => void selectOwner('owner-b')}>Use owner-b</button>
        </section>
      ) : (
        <>
          <p>Current synthetic owner: <strong>{ownerId}</strong></p>
          <form onSubmit={(event) => void ask(event)}>
            <label htmlFor="policy-question">Ask about the supplied policy excerpt</label>
            <textarea id="policy-question" value={question} maxLength={1000}
              onChange={(event) => setQuestion(event.target.value)} />
            <button type="submit" disabled={busy || !question.trim()}>Ask</button>
          </form>
        </>
      )}
      {busy && <p role="status">Working…</p>}
      {message && <p role="alert">{message}</p>}
      {result && (
        <section aria-label="Assistant response">
          <h2>Response</h2>
          <p>{result.answer}</p>
          <p>Execution mode: {result.executionMode}</p>
          {result.abstained && <p>The assistant abstained because the fixture has insufficient evidence.</p>}
          <ul aria-label="Citations">
            {result.citations.map((citation) => <li key={`${citation.documentId}:${citation.version}:${citation.section}`}>
              {citation.documentId} · {citation.version} · {citation.section}
            </li>)}
          </ul>
          <p>Provider request made: {result.providerRequestMade ? 'yes' : 'no'}</p>
          <p>Live answer quality evaluated: {result.liveQualityEvaluated ? 'yes' : 'no'}</p>
        </section>
      )}
    </main>
  );
}
