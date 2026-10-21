import type { ReactNode } from 'react';

export type ParcelResponse =
  | { kind: 'loading' }
  | { kind: 'network-error' }
  | { kind: 'http'; status: 401 | 404 }
  | { kind: 'http'; status: 200; parcel: null | { ref: string; stage: string } };

/** Independent teaching toy for exclusive UI states; not a policy implementation. */
export function ParcelResult({ response }: { response: ParcelResponse }) {
  let content: ReactNode;
  if (response.kind === 'loading') content = <p role="status">Loading parcel</p>;
  else if (response.kind === 'network-error') content = <p role="alert">Parcel unavailable. Try again.</p>;
  else if (response.status === 401) content = <p role="alert">Sign in to view this parcel</p>;
  else if (response.status === 404) content = <p role="alert">Parcel unavailable</p>;
  else if (response.status === 200 && response.parcel === null) content = <p role="status">No parcel selected</p>;
  else if (response.status === 200) content = <dl><dt>Reference</dt><dd>{response.parcel?.ref}</dd><dt>Stage</dt><dd>{response.parcel?.stage}</dd></dl>;
  else content = <p role="alert">Parcel unavailable</p>;
  return <section aria-label="Parcel tracking">{content}</section>;
}
