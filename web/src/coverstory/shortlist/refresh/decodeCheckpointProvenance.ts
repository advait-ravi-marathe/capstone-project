/** CAP-641 compatibility seam. Historical provenance is unknown, never inferred. */
export type Provenance = 'LOCAL_UNCHANGED' | 'UPSTREAM_REFRESH' | 'EXPLICIT_CONFLICT_CHOICE' | 'UNKNOWN'
export type CheckpointEnvelope = { schemaMajor: number; provenance?: string | null }
export function decodeProvenance(envelope: CheckpointEnvelope): Provenance {
  // TODO(CAP-641): decode absent historical values as UNKNOWN and reject an
  // unsupported required schema major without changing immutable source refs.
  void envelope
  throw new Error('TODO(CAP-641): decode checkpoint provenance')
}
