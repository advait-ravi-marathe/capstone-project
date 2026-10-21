import { cleanup, fireEvent, render, screen } from '@testing-library/react';
import { afterEach, describe, expect, it, vi } from 'vitest';

type ParcelWire = { ref: string; revision: number; weightKg: number; measuredOn: string; notice: string };
type ParcelDisplay = Omit<ParcelWire, 'weightKg'> & { weightKg: string };
type ParcelState = { kind: 'loading' } | { kind: 'unauthenticated' } | { kind: 'unavailable' } | { kind: 'ready'; parcel: ParcelDisplay };

function toDisplay(wire: ParcelWire): ParcelDisplay {
  if (typeof wire.weightKg !== 'number' || !Number.isFinite(wire.weightKg)) throw new Error('The wire weight must be finite');
  return { ...wire, weightKg: wire.weightKg.toFixed(2) };
}
async function readParcel(fetcher: typeof fetch = fetch): Promise<ParcelState> {
  try {
    const response = await fetcher('/teaching/parcels/parcel-17', { method: 'GET', credentials: 'same-origin' });
    if (response.status === 401) return { kind: 'unauthenticated' };
    if (!response.ok) return { kind: 'unavailable' };
    return { kind: 'ready', parcel: toDisplay(await response.json() as ParcelWire) };
  } catch { return { kind: 'unavailable' }; }
}
function ParcelWeightPanel({ state, onRefresh }: { state: ParcelState; onRefresh?: () => void }) {
  let content;
  if (state.kind === 'loading') content = <p role="status">Checking parcel measurement</p>;
  else if (state.kind === 'unauthenticated') content = <p role="alert">Sign in to view this parcel</p>;
  else if (state.kind === 'unavailable') content = <p role="alert">Parcel measurement unavailable</p>;
  else content = <>
    <p>Recorded weight: {state.parcel.weightKg} kg</p><p>Measured on {state.parcel.measuredOn}</p>
    <p>Revision {state.parcel.revision}</p><p>{state.parcel.notice}</p>
    {onRefresh && <button type="button" onClick={onRefresh}>Refresh measurement</button>}
  </>;
  return <section aria-label="Parcel measurement">{content}</section>;
}
const wire: ParcelWire = { ref: 'parcel-17', revision: 4, weightKg: 2.5, measuredOn: '2026-10-19', notice: 'Recorded measurement only; not a delivery promise.' };
function reply(status: number, body: unknown): Response { return { status, ok: status >= 200 && status < 300, json: async () => body } as Response; }
afterEach(cleanup);

describe('parcel teaching reference', () => {
  it('formats actual numeric response values and rerenders changed values', () => {
    const first = toDisplay(wire); const second = toDisplay({ ...wire, revision: 9, weightKg: 17.4, measuredOn: '2026-10-21' });
    expect(first.weightKg).toBe('2.50'); expect(second.weightKg).toBe('17.40');
    const { rerender } = render(<ParcelWeightPanel state={{ kind: 'ready', parcel: first }} />);
    expect(screen.getByRole('region', { name: 'Parcel measurement' })).toBeInTheDocument();
    expect(screen.getByText('Recorded weight: 2.50 kg')).toBeInTheDocument();
    rerender(<ParcelWeightPanel state={{ kind: 'ready', parcel: second }} />);
    expect(screen.getByText('Recorded weight: 17.40 kg')).toBeInTheDocument();
    expect(screen.getByText('Revision 9')).toBeInTheDocument(); expect(screen.getByText('Measured on 2026-10-21')).toBeInTheDocument();
    expect(screen.getByText(wire.notice)).toBeInTheDocument(); expect(screen.queryByText('Recorded weight: 2.50 kg')).not.toBeInTheDocument();
    expect(screen.queryByRole('button', { name: /dispatch|purchase|guarantee/i })).not.toBeInTheDocument();
  });
  it.each([['loading', 'status', 'Checking parcel measurement'], ['unauthenticated', 'alert', 'Sign in to view this parcel'], ['unavailable', 'alert', 'Parcel measurement unavailable']] as const)(
    'removes stale success in %s', (kind, role, message) => {
      const { rerender } = render(<ParcelWeightPanel state={{ kind: 'ready', parcel: toDisplay(wire) }} />);
      rerender(<ParcelWeightPanel state={{ kind }} />);
      expect(screen.getByRole(role)).toHaveTextContent(message); expect(screen.queryByText(/Recorded weight:|Measured on|Revision 4/)).not.toBeInTheDocument();
      expect(screen.queryByText(wire.notice)).not.toBeInTheDocument();
      rerender(<ParcelWeightPanel state={{ kind: 'ready', parcel: toDisplay({ ...wire, revision: 8 }) }} />);
      expect(screen.getByText('Revision 8')).toBeInTheDocument(); expect(screen.queryByRole('alert')).not.toBeInTheDocument();
      expect(screen.queryByRole('status')).not.toBeInTheDocument();
    });
  it('calls the read callback only on one explicit click', () => {
    const refresh = vi.fn(); const state = { kind: 'ready', parcel: toDisplay(wire) } as const;
    const { rerender } = render(<ParcelWeightPanel state={state} onRefresh={refresh} />);
    expect(refresh).toHaveBeenCalledTimes(0); rerender(<ParcelWeightPanel state={state} onRefresh={refresh} />);
    expect(refresh).toHaveBeenCalledTimes(0); fireEvent.click(screen.getByRole('button', { name: 'Refresh measurement' }));
    expect(refresh).toHaveBeenCalledTimes(1);
  });
  it('issues one GET and handles actual HTTP status and numeric JSON', async () => {
    const fetcher = vi.fn<typeof fetch>().mockResolvedValue(reply(200, wire));
    expect(await readParcel(fetcher)).toEqual({ kind: 'ready', parcel: toDisplay(wire) });
    expect(fetcher).toHaveBeenCalledTimes(1); expect(fetcher).toHaveBeenCalledWith('/teaching/parcels/parcel-17', { method: 'GET', credentials: 'same-origin' });
    for (const status of [401, 404, 409, 500]) {
      const parse = vi.fn(async () => wire); const failed = vi.fn<typeof fetch>().mockResolvedValue({ status, ok: false, json: parse } as unknown as Response);
      expect(await readParcel(failed)).toEqual({ kind: status === 401 ? 'unauthenticated' : 'unavailable' });
      expect(parse).not.toHaveBeenCalled(); expect(failed).toHaveBeenCalledTimes(1);
    }
  });
  it('maps rejected transport and wrong wire type to unavailable', async () => {
    const rejected = vi.fn<typeof fetch>().mockRejectedValue(new Error('offline'));
    const wrongType = vi.fn<typeof fetch>().mockResolvedValue(reply(200, { ...wire, weightKg: '2.50' }));
    expect(await readParcel(rejected)).toEqual({ kind: 'unavailable' }); expect(await readParcel(wrongType)).toEqual({ kind: 'unavailable' });
  });
});
