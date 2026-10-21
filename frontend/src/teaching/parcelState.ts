export type Parcel = { id: string; revision: number };
export type State =
  | { kind: 'idle' | 'loading' | 'expired' }
  | { kind: 'unavailable'; reason: 'http' | 'payload' | 'network' }
  | { kind: 'ready'; value: Parcel };

export function isParcel(x: unknown): x is Parcel {
  if (typeof x !== 'object' || x === null) return false;
  const value = x as Record<string, unknown>;
  return typeof value.id === 'string' && value.id.length > 0
    && typeof value.revision === 'number'
    && Number.isInteger(value.revision) && value.revision >= 1;
}

export async function readParcel(send: typeof fetch): Promise<State> {
  let response: Response;
  try {
    response = await send('/api/parcels/p-7', { method: 'GET', credentials: 'same-origin' });
  } catch {
    return { kind: 'unavailable', reason: 'network' };
  }
  if (response.status === 401) return { kind: 'expired' };
  if (!response.ok) return { kind: 'unavailable', reason: 'http' };
  try {
    const value: unknown = await response.json();
    return isParcel(value) ? { kind: 'ready', value } : { kind: 'unavailable', reason: 'payload' };
  } catch {
    return { kind: 'unavailable', reason: 'payload' };
  }
}

export class ParcelView {
  private generation = 0;
  state: State = { kind: 'idle' };
  clearForOwnerChangeOrLogout(): void { ++this.generation; this.state = { kind: 'idle' }; }
  async load(send: typeof fetch): Promise<void> {
    const mine = ++this.generation;
    this.state = { kind: 'loading' };
    const result = await readParcel(send);
    if (mine === this.generation) this.state = result;
  }
}
