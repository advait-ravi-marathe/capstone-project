import { useRef, useState, type ReactElement } from 'react';
import { act, cleanup, fireEvent, render, screen } from '@testing-library/react';
import { afterEach, describe, expect, it, vi } from 'vitest';

type Board = { platform: string; revision: string };
type Refresh = () => Promise<Board>;

afterEach(cleanup);

function TrainBoard({ initial, refresh }: { initial: Board; refresh: Refresh }): ReactElement {
  const latest = useRef(0);
  const [displayed, setDisplayed] = useState(initial);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  async function handleRefresh(): Promise<void> {
    const requestId = ++latest.current;
    setLoading(true);
    setError('');
    try {
      const next = await refresh();
      if (requestId === latest.current) setDisplayed(next);
    } catch {
      if (requestId === latest.current) setError('Refresh failed');
    } finally {
      if (requestId === latest.current) setLoading(false);
    }
  }

  return (
    <section aria-label="Train platform board">
      <p>Platform {displayed.platform}</p>
      <p>Revision {displayed.revision}</p>
      {loading && <p role="status">Refreshing</p>}
      {error && <p role="alert">{error}</p>}
      <button type="button" onClick={() => { void handleRefresh(); }}>Refresh</button>
    </section>
  );
}

function deferred<T>() {
  let resolve!: (value: T | PromiseLike<T>) => void;
  let reject!: (reason?: unknown) => void;
  const promise = new Promise<T>((res, rej) => { resolve = res; reject = rej; });
  return { promise, resolve, reject };
}

describe('Day 19 event-triggered latest-request-wins example', () => {
  it('keeps the newest fulfilled result when the older result arrives later', async () => {
    const a = deferred<Board>();
    const b = deferred<Board>();
    const refresh = vi.fn<Refresh>().mockReturnValueOnce(a.promise).mockReturnValueOnce(b.promise);
    render(<TrainBoard initial={{ platform: '4', revision: 'rev-A' }} refresh={refresh} />);
    const button = screen.getByRole('button', { name: 'Refresh' });

    fireEvent.click(button);
    fireEvent.click(button);
    expect(refresh).toHaveBeenCalledTimes(2);
    expect(screen.getByRole('status')).toHaveTextContent('Refreshing');

    await act(async () => { b.resolve({ platform: '7', revision: 'rev-B' }); await b.promise; });
    expect(screen.getByText('Platform 7')).toBeInTheDocument();
    expect(screen.getByText('Revision rev-B')).toBeInTheDocument();
    expect(screen.queryByRole('status')).not.toBeInTheDocument();

    await act(async () => { a.resolve({ platform: '2', revision: 'rev-old' }); await a.promise; });
    expect(screen.getByText('Platform 7')).toBeInTheDocument();
    expect(screen.getByText('Revision rev-B')).toBeInTheDocument();
    expect(screen.queryByText('Platform 2')).not.toBeInTheDocument();
  });

  it('ignores an obsolete rejection and does not let its finally clear newer loading', async () => {
    const a = deferred<Board>();
    const b = deferred<Board>();
    const refresh = vi.fn<Refresh>().mockReturnValueOnce(a.promise).mockReturnValueOnce(b.promise);
    render(<TrainBoard initial={{ platform: '4', revision: 'rev-A' }} refresh={refresh} />);
    fireEvent.click(screen.getByRole('button', { name: 'Refresh' }));
    fireEvent.click(screen.getByRole('button', { name: 'Refresh' }));

    await act(async () => { a.reject(new Error('obsolete')); await a.promise.catch(() => undefined); });
    expect(screen.queryByRole('alert')).not.toBeInTheDocument();
    expect(screen.getByRole('status')).toHaveTextContent('Refreshing');
    expect(screen.getByText('Platform 4')).toBeInTheDocument();

    await act(async () => { b.resolve({ platform: '7', revision: 'rev-B' }); await b.promise; });
    expect(screen.getByText('Platform 7')).toBeInTheDocument();
    expect(screen.queryByRole('status')).not.toBeInTheDocument();
  });

  it('ignores an older success while the newer request later fails', async () => {
    const a = deferred<Board>();
    const b = deferred<Board>();
    const refresh = vi.fn<Refresh>().mockReturnValueOnce(a.promise).mockReturnValueOnce(b.promise);
    render(<TrainBoard initial={{ platform: '4', revision: 'rev-A' }} refresh={refresh} />);
    fireEvent.click(screen.getByRole('button', { name: 'Refresh' }));
    fireEvent.click(screen.getByRole('button', { name: 'Refresh' }));

    await act(async () => { a.resolve({ platform: '2', revision: 'rev-old' }); await a.promise; });
    expect(screen.getByText('Platform 4')).toBeInTheDocument();
    expect(screen.getByRole('status')).toHaveTextContent('Refreshing');

    await act(async () => { b.reject(new Error('new request failed')); await b.promise.catch(() => undefined); });
    expect(screen.getByRole('alert')).toHaveTextContent('Refresh failed');
    expect(screen.getByText('Platform 4')).toBeInTheDocument();
    expect(screen.queryByRole('status')).not.toBeInTheDocument();
  });
});
