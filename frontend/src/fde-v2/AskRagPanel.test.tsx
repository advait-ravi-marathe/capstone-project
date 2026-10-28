import { cleanup, fireEvent, render, screen, waitFor } from '@testing-library/react';
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import { AskRagPanel } from './AskRagPanel';

const answer = {
  answer: 'Fixture-only answer', citations: ['doc-a:v1:0'], abstained: false,
  model_id: 'test-double-only', model_revision: 'fixture', request_id: 'req-1',
  stage_timings_ms: { retrieval: 1, generation: 2 },
  sources: [{ chunk_id: 'doc-a:v1:0', document_id: 'doc-a', title: 'Fixture', text: 'Synthetic source text.' }],
};

describe('supplied grounded RAG panel contract', () => {
  beforeEach(() => vi.restoreAllMocks());
  afterEach(() => cleanup());

  it('submits a trimmed question and renders only a citation-matched response', async () => {
    const call = vi.spyOn(globalThis, 'fetch').mockResolvedValue({ ok: true, json: async () => answer } as Response);
    render(<AskRagPanel />);
    fireEvent.change(screen.getByLabelText('Question for grounded RAG'), { target: { value: '  deductible?  ' } });
    fireEvent.click(screen.getByRole('button', { name: 'Ask with sources' }));
    expect(await screen.findByTestId('rag-answer')).toHaveTextContent(answer.answer);
    expect(call.mock.calls[0][0]).toBe('/ask');
    expect(JSON.parse(String(call.mock.calls[0][1]?.body))).toEqual({ question: 'deductible?' });
    expect(screen.getByText('doc-a:v1:0')).toBeInTheDocument();
  });

  it('rejects a foreign citation instead of presenting an unsupported answer', async () => {
    vi.spyOn(globalThis, 'fetch').mockResolvedValue({ ok: true, json: async () => ({
      ...answer, citations: ['foreign:v1:0'],
    }) } as Response);
    render(<AskRagPanel />);
    fireEvent.change(screen.getByLabelText('Question for grounded RAG'), { target: { value: 'deductible?' } });
    fireEvent.click(screen.getByRole('button', { name: 'Ask with sources' }));
    expect(await screen.findByRole('alert')).toHaveTextContent(/citations do not match/i);
    expect(screen.queryByTestId('rag-answer')).not.toBeInTheDocument();
  });

  it('renders model text as text rather than interpreting it as HTML', async () => {
    const untrusted = '<img src=x onerror="alert(1)"> synthetic text';
    vi.spyOn(globalThis, 'fetch').mockResolvedValue({ ok: true, json: async () => ({
      ...answer, answer: untrusted,
    }) } as Response);
    const { container } = render(<AskRagPanel />);
    fireEvent.change(screen.getByLabelText('Question for grounded RAG'), { target: { value: 'fixture?' } });
    fireEvent.click(screen.getByRole('button', { name: 'Ask with sources' }));
    const rendered = await screen.findByTestId('rag-answer');
    expect(rendered.textContent).toBe(untrusted);
    expect(container.querySelector('img')).not.toBeInTheDocument();
  });

  it('shows service errors and rejects whitespace before sending', async () => {
    const call = vi.spyOn(globalThis, 'fetch').mockResolvedValue({ ok: false, status: 503 } as Response);
    render(<AskRagPanel />);
    fireEvent.change(screen.getByLabelText('Question for grounded RAG'), { target: { value: '   ' } });
    fireEvent.click(screen.getByRole('button', { name: 'Ask with sources' }));
    expect(screen.getByRole('alert')).toHaveTextContent('Enter a question first.');
    expect(call).not.toHaveBeenCalled();
    fireEvent.change(screen.getByLabelText('Question for grounded RAG'), { target: { value: 'real request' } });
    fireEvent.click(screen.getByRole('button', { name: 'Ask with sources' }));
    expect(await screen.findByRole('alert')).toHaveTextContent('Request failed (503).');
    expect(screen.queryByTestId('rag-answer')).not.toBeInTheDocument();
  });

  it('cancels client waiting and ignores a late response without claiming server cancellation', async () => {
    let finish!: (response: Response) => void;
    let signal!: AbortSignal;
    vi.spyOn(globalThis, 'fetch').mockImplementation((_input, init) => {
      signal = init?.signal as AbortSignal;
      return new Promise<Response>(resolve => { finish = resolve; });
    });
    render(<AskRagPanel />);
    fireEvent.change(screen.getByLabelText('Question for grounded RAG'), { target: { value: 'deductible?' } });
    fireEvent.click(screen.getByRole('button', { name: 'Ask with sources' }));
    expect(screen.getByRole('status')).toHaveTextContent(/generating/i);
    fireEvent.click(screen.getByRole('button', { name: 'Cancel grounded request' }));
    expect(signal.aborted).toBe(true);
    expect(screen.getByRole('status')).toHaveTextContent(/no answer is displayed/i);
    finish({ ok: true, json: async () => answer } as Response);
    await waitFor(() => expect(screen.getByRole('status')).toHaveTextContent(/cancelled/i));
    expect(screen.queryByTestId('rag-answer')).not.toBeInTheDocument();
  });

  it('aborts browser waiting when the component unmounts', () => {
    let signal!: AbortSignal;
    vi.spyOn(globalThis, 'fetch').mockImplementation((_input, init) => {
      signal = init?.signal as AbortSignal;
      return new Promise<Response>(() => undefined);
    });
    const { unmount } = render(<AskRagPanel />);
    fireEvent.change(screen.getByLabelText('Question for grounded RAG'), { target: { value: 'deductible?' } });
    fireEvent.click(screen.getByRole('button', { name: 'Ask with sources' }));
    unmount();
    expect(signal.aborted).toBe(true);
  });
});
