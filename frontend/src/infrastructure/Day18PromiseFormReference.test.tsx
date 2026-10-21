import { useRef, useState, type FormEvent, type ReactElement } from 'react';
import { act, cleanup, fireEvent, render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { afterEach, describe, expect, it, vi } from 'vitest';

type SaveAlias = (alias: string) => Promise<void>;

afterEach(cleanup);

function ReadingClubForm({ saveAlias }: { saveAlias: SaveAlias }): ReactElement {
  const [alias, setAlias] = useState('');
  const [pending, setPending] = useState(false);
  const [message, setMessage] = useState('');
  const [error, setError] = useState('');
  const busy = useRef(false);

  async function submit(): Promise<void> {
    if (busy.current) return;
    if (!alias.trim()) {
      setError('Enter an alias');
      return;
    }
    busy.current = true;
    setPending(true);
    setError('');
    setMessage('');
    try {
      await saveAlias(alias);
      setMessage('Registered');
    } catch {
      setError('Could not register. Try again');
    } finally {
      busy.current = false;
      setPending(false);
    }
  }

  function handleSubmit(event: FormEvent<HTMLFormElement>): void {
    event.preventDefault();
    void submit();
  }

  return (
    <form aria-label="Reading club registration" onSubmit={handleSubmit}>
      <label htmlFor="club-alias">Alias</label>
      <input id="club-alias" value={alias} onChange={(event) => setAlias(event.currentTarget.value)} />
      <button type="submit" disabled={pending}>{pending ? 'Saving' : 'Register'}</button>
      {message && <p role="status">{message}</p>}
      {error && <p role="alert">{error}</p>}
    </form>
  );
}

function deferred<T>() {
  let resolve!: (value: T | PromiseLike<T>) => void;
  let reject!: (reason?: unknown) => void;
  const promise = new Promise<T>((res, rej) => { resolve = res; reject = rej; });
  return { promise, resolve, reject };
}

describe('Day 18 controlled input and Promise<void> example', () => {
  it('blocks blank input, owns pending through settlement, and ignores duplicate submits', async () => {
    const user = userEvent.setup();
    const result = deferred<void>();
    const saveAlias = vi.fn<SaveAlias>(() => result.promise);
    render(<ReadingClubForm saveAlias={saveAlias} />);
    const input = screen.getByRole('textbox', { name: 'Alias' });
    const button = screen.getByRole('button', { name: 'Register' });

    await user.click(button);
    expect(screen.getByRole('alert')).toHaveTextContent('Enter an alias');
    expect(saveAlias).not.toHaveBeenCalled();

    await user.type(input, 'Mira');
    await user.click(button);
    expect(saveAlias).toHaveBeenCalledTimes(1);
    expect(saveAlias).toHaveBeenCalledWith('Mira');
    expect(screen.getByRole('button', { name: 'Saving' })).toBeDisabled();
    fireEvent.submit(screen.getByRole('form', { name: 'Reading club registration' }));
    expect(saveAlias).toHaveBeenCalledTimes(1);

    result.resolve(undefined);
    expect(await screen.findByRole('status')).toHaveTextContent('Registered');
    expect(screen.getByRole('textbox', { name: 'Alias' })).toHaveValue('Mira');
    expect(screen.getByRole('button', { name: 'Register' })).toBeEnabled();
  });

  it('preserves the entered value, shows a safe rejection message, and releases pending', async () => {
    const user = userEvent.setup();
    const result = deferred<void>();
    const saveAlias = vi.fn<SaveAlias>(() => result.promise);
    render(<ReadingClubForm saveAlias={saveAlias} />);
    await user.type(screen.getByRole('textbox', { name: 'Alias' }), 'Mira');
    await user.click(screen.getByRole('button', { name: 'Register' }));
    await act(async () => { result.reject(new Error('private service detail')); await result.promise.catch(() => undefined); });

    expect(await screen.findByRole('alert')).toHaveTextContent('Could not register. Try again');
    expect(screen.getByRole('alert')).not.toHaveTextContent('private service detail');
    expect(screen.getByRole('textbox', { name: 'Alias' })).toHaveValue('Mira');
    expect(screen.getByRole('button', { name: 'Register' })).toBeEnabled();
  });
});
