/** Planned frontend contract only; no public Support API is present yet. */
export type SourceCitation = {
  citationId: string;
  sourceId: string;
  versionId: string;
  batchId: string;
  startByte: number;
  endByte: number;
};

export type SourceLineageState =
  | { status: "loading" }
  | { status: "found"; citation: SourceCitation }
  | { status: "not-found" }
  | { status: "lineage-broken" };

// TODO(CAP-121): connect only after an authenticated, owner-scoped Support API exists.
// Keep private response data in memory and clear it on logout/account change.
export function SourceLineageReader() {
  return <p role="status">Source lineage view awaits the authenticated Support API.</p>;
}
