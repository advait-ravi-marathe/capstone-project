import { cleanup, fireEvent, render, screen, waitFor } from '@testing-library/react';
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import { QuestionAnswerApp } from './QuestionAnswerApp';

const response = {
  answer: 'A generated sample response.',
  elapsed_ms: 12.5,
  provider: 'pinned-local-model',
  request_id: 'test-request-1',
};

describe('assistant-supplied local question UI', () => {
  beforeEach(() => vi.restoreAllMocks());
  afterEach(() => cleanup());

  it('sends trimmed input and shows the generation response', async () => {
    const fetchMock = vi.spyOn(globalThis, 'fetch').mockResolvedValue({ ok: true, json: async () => response } as Response);
    render(<QuestionAnswerApp />);
    fireEvent.change(screen.getByLabelText('Question'), { target: { value: '  deductible?  ' } });
    fireEvent.click(screen.getByRole('button', { name: 'Ask locally' }));
    await screen.findByText(response.answer);
    expect(fetchMock).toHaveBeenCalledTimes(1);
    expect(fetchMock.mock.calls[0][0]).toBe('/generate');
    expect(JSON.parse(String(fetchMock.mock.calls[0][1]?.body))).toEqual({ question: 'deductible?' });
    expect(screen.getByText(/pinned-local-model · 12.5 ms/)).toBeInTheDocument();
  });

  it('shows a readable empty-input error without sending a request', () => {
    const fetchMock = vi.spyOn(globalThis, 'fetch');
    render(<QuestionAnswerApp />);
    fireEvent.click(screen.getByRole('button', { name: 'Ask locally' }));
    expect(screen.getByRole('alert')).toHaveTextContent('Enter a question first.');
    expect(fetchMock).not.toHaveBeenCalled();
  });

  it('does not display a failed service response as an answer', async () => {
    vi.spyOn(globalThis, 'fetch').mockResolvedValue({ ok: false, status: 503 } as Response);
    render(<QuestionAnswerApp />);
    fireEvent.change(screen.getByLabelText('Question'), { target: { value: 'deductible?' } });
    fireEvent.click(screen.getByRole('button', { name: 'Ask locally' }));
    await waitFor(() => expect(screen.getByRole('alert')).toHaveTextContent('Request failed (503).'));
    expect(screen.queryByTestId('answer')).not.toBeInTheDocument();
  });
});
