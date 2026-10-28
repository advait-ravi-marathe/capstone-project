import { useEffect, useRef, useState } from 'react';
import type { FormEvent } from 'react';

type AskResponse = {
  answer: string;
  elapsed_ms: number;
  provider: string;
  request_id: string;
};

type ViewState =
  | { kind: 'idle' }
  | { kind: 'loading' }
  | { kind: 'cancelled' }
  | { kind: 'error'; message: string }
  | { kind: 'result'; data: AskResponse };

export function QuestionAnswerApp() {
  const [question, setQuestion] = useState('');
  const [view, setView] = useState<ViewState>({ kind: 'idle' });
  const generation = useRef(0);
  const activeController = useRef<AbortController | null>(null);

  useEffect(() => () => {
    generation.current += 1;
    activeController.current?.abort();
    activeController.current = null;
  }, []);

  function cancel() {
    generation.current += 1;
    activeController.current?.abort();
    activeController.current = null;
    setView({ kind: 'cancelled' });
  }

  async function submit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const normalized = question.trim();
    if (!normalized) {
      setView({ kind: 'error', message: 'Enter a question first.' });
      return;
    }
    activeController.current?.abort();
    const requestGeneration = ++generation.current;
    const controller = new AbortController();
    activeController.current = controller;
    setView({ kind: 'loading' });
    try {
      const response = await fetch('/generate', {
        method: 'POST',
        headers: { 'content-type': 'application/json' },
        body: JSON.stringify({ question: normalized }),
        signal: controller.signal,
      });
      if (!response.ok) throw new Error(`Request failed (${response.status}).`);
      const data = (await response.json()) as AskResponse;
      if (typeof data.answer !== 'string' || typeof data.elapsed_ms !== 'number'
          || typeof data.provider !== 'string' || typeof data.request_id !== 'string') {
        throw new Error('The local service returned an invalid generation response.');
      }
      if (requestGeneration === generation.current) {
        activeController.current = null;
        setView({ kind: 'result', data });
      }
    } catch (error) {
      if (controller.signal.aborted || requestGeneration !== generation.current) return;
      activeController.current = null;
      setView({ kind: 'error', message: error instanceof Error ? error.message : 'The local request failed.' });
    }
  }

  return (
    <main className="fde-v2-shell">
      <h1>Northstar policy assistant</h1>
      <p>This Sprint 1 screen demonstrates local generation only. It is not grounded in policy documents and must not be used for decisions.</p>
      <form onSubmit={submit} aria-label="Ask a policy question">
        <label htmlFor="policy-question">Question</label>
        <textarea id="policy-question" value={question} maxLength={2000}
          onChange={(event) => setQuestion(event.target.value)} />
        <button type="submit" disabled={view.kind === 'loading'}>Ask locally</button>
        {view.kind === 'loading' && <button type="button" onClick={cancel}>Cancel request</button>}
      </form>
      <section aria-live="polite" aria-busy={view.kind === 'loading'}>
        {view.kind === 'idle' && <p>Ask about the fictional sample policy.</p>}
        {view.kind === 'loading' && <p role="status">Generating a response with the local model…</p>}
        {view.kind === 'cancelled' && <p role="status">Request cancelled. No answer is displayed.</p>}
        {view.kind === 'error' && <p role="alert">{view.message}</p>}
        {view.kind === 'result' && (
          <article>
            <p data-testid="answer">{view.data.answer}</p>
            <small>{view.data.provider} · {view.data.elapsed_ms} ms · request {view.data.request_id}</small>
          </article>
        )}
      </section>
      <p className="fde-v2-notice">Single-user, loopback learning app. Not a real benefits plan or production service.</p>
    </main>
  );
}
