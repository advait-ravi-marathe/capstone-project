export type CatalogCategory = 'health' | 'term' | 'motor' | 'ulip'
export type CatalogEvidenceStatus = 'SYNTHETIC' | 'SOURCE_EVIDENCE'
export type CatalogVersionStatus = 'PUBLISHED' | 'RETIRED'

export interface CatalogVersionV1 {
  versionId: string
  category: CatalogCategory
  scenarioId: string
  sourceId: string
  sourceVersion: string
  evidenceStatus: CatalogEvidenceStatus
  status: CatalogVersionStatus
}

export interface CatalogEnvelopeV1 {
  schemaMajor: 1
  versions: CatalogVersionV1[]
}

export type CatalogEnvelopeError =
  | { kind: 'UnsupportedVersion'; field: 'schemaMajor'; actual: unknown }
  | { kind: 'FieldError'; field: string; reason: string }

export type CatalogEnvelopeResult =
  | { ok: true; value: CatalogEnvelopeV1 }
  | { ok: false; error: CatalogEnvelopeError }

/** Learner seam. Keep fixture and expected cases instructor-owned. */
export function decodeCatalogEnvelope(raw: unknown): CatalogEnvelopeResult {
  void raw
  throw new Error('TODO(CAP-166): implement the bounded schema-major decoder')
}
