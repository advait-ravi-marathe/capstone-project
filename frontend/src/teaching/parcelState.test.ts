import { describe, expect, it, vi } from 'vitest';
import { ParcelView, readParcel, submitParcelHold } from './parcelState';

describe('parcel transport/state teaching reference', () => {
  it('checks status before parsing, validates unknown JSON, and clears late owner results', async () => {
    const html401 = vi.fn<typeof fetch>().mockResolvedValue(new Response('<html>expired</html>', { status: 401 }));
    expect(await readParcel(html401)).toEqual({ kind: 'expired' });
    const wrongShape = vi.fn<typeof fetch>().mockResolvedValue(new Response('{"id":"p-7","revision":"2"}'));
    expect(await readParcel(wrongShape)).toEqual({ kind: 'unavailable', reason: 'payload' });
    const view = new ParcelView();
    let resolve!: (response: Response) => void;
    const pending = view.load(() => new Promise<Response>(done => { resolve = done; }));
    view.clearForOwnerChangeOrLogout();
    resolve(new Response('{"id":"p-7","revision":2}'));
    await pending;
    expect(view.state).toEqual({ kind: 'idle' });
  });
  it('uses same-origin credentials and distinguishes HTTP/network failure', async () => {
    const fetcher = vi.fn<typeof fetch>().mockResolvedValue(new Response('{"id":"p-7","revision":2}'));
    expect(await readParcel(fetcher)).toEqual({ kind: 'ready', value: { id: 'p-7', revision: 2 } });
    expect(fetcher).toHaveBeenCalledWith('/api/parcels/p-7', { method: 'GET', credentials: 'same-origin' });
    expect(await readParcel(vi.fn<typeof fetch>().mockResolvedValue(new Response('bad', { status: 500 }))))
      .toEqual({ kind: 'unavailable', reason: 'http' });
    expect(await readParcel(vi.fn<typeof fetch>().mockRejectedValue(new Error('offline'))))
      .toEqual({ kind: 'unavailable', reason: 'network' });
  });

  it('uses an explicit frozen POST twice only when requested and handles bodyless 204', async () => {
    const request = Object.freeze({ parcelId: 'p-7', clientRequestId: 'hold-17' });
    const body = JSON.stringify(request);
    const send = vi.fn<typeof fetch>().mockResolvedValue(new Response(null, { status: 204 }));
    expect(send).toHaveBeenCalledTimes(0);
    expect(await submitParcelHold(send, body, 'toy-token')).toBe(204);
    expect(await submitParcelHold(send, body, 'toy-token')).toBe(204);
    expect(send).toHaveBeenCalledTimes(2);
    expect(send.mock.calls.map(call => call[1]?.body)).toEqual([body, body]);
    expect(send).toHaveBeenNthCalledWith(1, '/api/parcel-holds', expect.objectContaining({
      method: 'POST', credentials: 'same-origin',
      headers: { 'Content-Type': 'application/json', 'X-XSRF-TOKEN': 'toy-token' }, body,
    }));
  });
});
