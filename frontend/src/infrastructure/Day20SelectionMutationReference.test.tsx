import { useRef, useState, type ReactElement } from 'react';
import { act, cleanup, fireEvent, render, screen } from '@testing-library/react';
import { afterEach, describe, expect, it, vi } from 'vitest';

type Select = (journeyId: string, quoteVersionId: string) => Promise<void>;
type SelectionFailure = { status: number };

afterEach(cleanup);

function SelectionButton({ journeyId, quoteVersionId, select }: {
  journeyId: string; quoteVersionId: string; select: Select;
}): ReactElement {
  const busy = useRef(false);
  const [pending, setPending] = useState(false);
  const [message, setMessage] = useState('');
  const [error, setError] = useState('');

  async function handleSelect(): Promise<void> {
    if (busy.current) return;
    busy.current = true;
    setPending(true);
    setMessage('');
    setError('');
    try {
      await select(journeyId, quoteVersionId);
      setMessage('Selection confirmed by server');
    } catch (failure) {
      const status = (failure as SelectionFailure).status;
      const safeMessage = status === 409 ? 'Refresh quote' :
        status === 401 ? 'Sign in' :
          status === 404 ? 'Quote unavailable' : 'Could not confirm selection. Try again';
      setError(safeMessage);
    } finally {
      busy.current = false;
      setPending(false);
    }
  }

  return (
    <div>
      <button type="button" disabled={pending} onClick={() => { void handleSelect(); }}>
        {pending ? 'Selecting' : 'Select quote'}
      </button>
      {message && <p role="status">{message}</p>}
      {error && <p role="alert">{error}</p>}
    </div>
  );
}

function deferred<T>() {
  let resolve!: (value: T | PromiseLike<T>) => void;
  let reject!: (reason?: unknown) => void;
  const promise = new Promise<T>((res, rej) => { resolve = res; reject = rej; });
  return { promise, resolve, reject };
}

describe('Day 20 truthful selection mutation example', () => {
  it('sends the exact pair once, waits for Promise<void>, and confirms only on resolve', async () => {
    const result = deferred<void>();
    const select = vi.fn<Select>(() => result.promise);
    render(<SelectionButton journeyId="journey-R" quoteVersionId="quote-v3" select={select} />);
    const button = screen.getByRole('button', { name: 'Select quote' });
    fireEvent.click(button);
    expect(select).toHaveBeenCalledExactlyOnceWith('journey-R', 'quote-v3');
    expect(screen.getByRole('button', { name: 'Selecting' })).toBeDisabled();
    expect(screen.queryByRole('status')).not.toBeInTheDocument();
    fireEvent.click(button);
    expect(select).toHaveBeenCalledTimes(1);

    await act(async () => { result.resolve(undefined); await result.promise; });
    expect(await screen.findByRole('status')).toHaveTextContent('Selection confirmed by server');
    expect(screen.getByRole('button', { name: 'Select quote' })).toBeEnabled();
    expect(screen.queryByText(/purchase|coverage active/i)).not.toBeInTheDocument();
  });

  it.each([
    [409, 'Refresh quote'],
    [401, 'Sign in'],
    [404, 'Quote unavailable'],
    [0, 'Could not confirm selection. Try again'],
    [503, 'Could not confirm selection. Try again'],
  ])('maps rejection status %s to a safe message, not a success', async (status, message) => {
    const select = vi.fn<Select>().mockRejectedValue({ status });
    render(<SelectionButton journeyId="journey-R" quoteVersionId="quote-v3" select={select} />);
    fireEvent.click(screen.getByRole('button', { name: 'Select quote' }));
    expect(await screen.findByRole('alert')).toHaveTextContent(message);
    expect(screen.queryByRole('status')).not.toBeInTheDocument();
    expect(screen.getByRole('button', { name: 'Select quote' })).toBeEnabled();
  });
});
