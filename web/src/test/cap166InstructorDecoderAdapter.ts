import type { CatalogEnvelopeResult, CatalogEnvelopeV1 } from '../coverstory/catalog/decodeCatalogEnvelope'

/** Narrow test injection for the supplied UI wiring; not product decoder behavior. */
export function cap166InstructorDecoderAdapter(raw: unknown): CatalogEnvelopeResult {
  if (typeof raw !== 'object' || raw === null || !('schemaMajor' in raw) || raw.schemaMajor !== 1 ||
      !('versions' in raw) || !Array.isArray(raw.versions)) {
    return { ok: false, error: { kind: 'UnsupportedVersion', field: 'schemaMajor', actual: 'invalid test adapter input' } }
  }
  const value = raw as CatalogEnvelopeV1
  if (new URLSearchParams(window.location.search).has('boundary')) {
    const first = value.versions[0]
    return { ok: true, value: { ...value, versions: [...value.versions,
      { ...first, versionId: '16600000-0000-4000-8000-000000000004', sourceId: 'boundary-health', sourceVersion: 'v1' },
      { ...first, versionId: '16600000-0000-4000-8000-000000000005', category: 'motor', scenarioId: 'private-car' },
    ] } }
  }
  return { ok: true, value }
}
