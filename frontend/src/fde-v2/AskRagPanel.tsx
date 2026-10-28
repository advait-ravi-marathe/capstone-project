import { useEffect, useRef, useState } from 'react';
import type { FormEvent } from 'react';

type Source = { chunk_id: string; document_id: string; title: string; text: string };
type RagResponse = {
  answer: string;
  citations: string[];
  abstained: boolean;
  model_id: string;
  model_revision: string;
  request_id: string;
  stage_timings_ms: Record<string, number>;
  sources: Source[];
};
type State = { kind: 'idle' | 'loading' | 'cancelled' } | { kind: 'error'; message: string }
  | { kind: 'result'; value: RagResponse };

function validateResponse(value: unknown): RagResponse {
  if (!value || typeof value !== 'object') throw new Error('The local RAG service returned an invalid response.');
  const response = value as Partial<RagResponse>;
  if (typeof response.answer !== 'string' || !Array.isArray(response.citations)
      || response.citations.some(id => typeof id !== 'string') || typeof response.abstained !== 'boolean'
      || typeof response.model_id !== 'string' || typeof response.model_revision !== 'string'
      || typeof response.request_id !== 'string' || !response.sources || !Array.isArray(response.sources)
      || !response.stage_timings_ms || typeof response.stage_timings_ms !== 'object') {
    throw new Error('The local RAG service returned an invalid response.');
  }
  const sourceIds = response.sources.map(source => source.chunk_id);
  if (response.sources.some(source => !source || typeof source.chunk_id !== 'string'
      || typeof source.document_id !== 'string' || typeof source.title !== 'string' || typeof source.text !== 'string')
      || response.citations.some(id => !sourceIds.includes(id))
      || new Set(response.citations).size !== response.citations.length) {
    throw new Error('The response citations do not match its retrieved source passages.');
  }
  return response as RagResponse;
}

export function AskRagPanel() {
  const [question, setQuestion] = useState('');
  const [state, setState] = useState<State>({ kind: 'idle' });
  const generation = useRef(0);
  const active = useRef<AbortController | null>(null);

  useEffect(() => () => {
    generation.current += 1;
    active.current?.abort();
    active.current = null;
  }, []);

  function cancel() {
    generation.current += 1;
    active.current?.abort();
    active.current = null;
    setState({ kind: 'cancelled' });
  }

  async function submit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const normalized = question.trim();
    if (!normalized) {
      setState({ kind: 'error', message: 'Enter a question first.' });
      return;
    }
    active.current?.abort();
    const current = ++generation.current;
    const controller = new AbortController();
    active.current = controller;
    setState({ kind: 'loading' });
    try {
      const response = await fetch('/ask', { method: 'POST', headers: { 'content-type': 'application/json' },
        body: JSON.stringify({ question: normalized }), signal: controller.signal });
      if (!response.ok) throw new Error(`Request failed (${response.status}).`);
      const value = validateResponse(await response.json());
      if (current === generation.current) {
        active.current = null;
        setState({ kind: 'result', value });
      }
    } catch (error) {
      if (controller.signal.aborted || current !== generation.current) return;
      active.current = null;
      setState({ kind: 'error', message: error instanceof Error ? error.message : 'The local RAG request failed.' });
    }
  }

  return (
    <section aria-labelledby="rag-heading">
      <h2 id="rag-heading">Grounded answer (model gate blocked until verified local artifacts are supplied)</h2>
      <form onSubmit={submit} aria-label="Ask the grounded policy assistant">
        <label htmlFor="rag-question">Question for grounded RAG</label>
        <textarea id="rag-question" value={question} maxLength={2000}
          onChange={event => setQuestion(event.target.value)} />
        <button type="submit" disabled={state.kind === 'loading'}>Ask with sources</button>
        {state.kind === 'loading' && <button type="button" onClick={cancel}>Cancel grounded request</button>}
      </form>
      <div aria-live="polite" aria-busy={state.kind === 'loading'}>
        {state.kind === 'loading' && <p role="status">Retrieving evidence and generating an answer…</p>}
        {state.kind === 'cancelled' && <p role="status">Request cancelled. No answer is displayed.</p>}
        {state.kind === 'error' && <p role="alert">{state.message}</p>}
        {state.kind === 'result' && (
          <article>
            <p data-testid="rag-answer">{state.value.answer}</p>
            {state.value.abstained && <p>Insufficient supporting evidence; review the cited material before acting.</p>}
            <ul aria-label="Retrieved source passages">
              {state.value.sources.map(source => <li key={source.chunk_id}>
                <strong>{source.title}</strong> <code>{source.chunk_id}</code><p>{source.text}</p>
              </li>)}
            </ul>
            <small>{state.value.model_id}@{state.value.model_revision} · request {state.value.request_id}</small>
          </article>
        )}
      </div>
    </section>
  );
}
