import { createRoot } from 'react-dom/client'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { PersistedHealthComparison } from './PersistedHealthComparison'
import type { CatalogEnvelopeResult, CatalogEnvelopeV1, CatalogVersionV1 } from './decodeCatalogEnvelope'

/** Instructor-only test decoder; the production decodeCatalogEnvelope TODO stays untouched. */
export function decodeInstructorCatalogEnvelope(raw: unknown): CatalogEnvelopeResult {
  if (!raw || typeof raw !== 'object' || Array.isArray(raw)) return { ok: false, error: { kind: 'FieldError', field: 'root', reason: 'object_required' } }
  const envelope = raw as Record<string, unknown>
  if (envelope.schemaMajor !== 1) return { ok: false, error: { kind: 'UnsupportedVersion', field: 'schemaMajor', actual: envelope.schemaMajor } }
  if (!Array.isArray(envelope.versions)) return { ok: false, error: { kind: 'FieldError', field: 'versions', reason: 'array_required' } }
  const versions: CatalogVersionV1[] = []
  for (const [index, item] of envelope.versions.entries()) {
    if (!item || typeof item !== 'object' || Array.isArray(item)) return { ok: false, error: { kind: 'FieldError', field: `versions.${index}`, reason: 'object_required' } }
    const row = item as Record<string, unknown>
    const strings = ['versionId', 'category', 'scenarioId', 'sourceId', 'sourceVersion', 'evidenceStatus', 'status']
    if (strings.some((field) => typeof row[field] !== 'string')) return { ok: false, error: { kind: 'FieldError', field: `versions.${index}`, reason: 'required_string' } }
    if (!['health', 'term', 'motor', 'ulip'].includes(row.category as string)
      || !['SYNTHETIC', 'SOURCE_EVIDENCE'].includes(row.evidenceStatus as string)
      || !['PUBLISHED', 'RETIRED'].includes(row.status as string)) {
      return { ok: false, error: { kind: 'FieldError', field: `versions.${index}`, reason: 'unsupported_enum' } }
    }
    versions.push({
      versionId: row.versionId as string,
      category: row.category as CatalogVersionV1['category'],
      scenarioId: row.scenarioId as string,
      sourceId: row.sourceId as string,
      sourceVersion: row.sourceVersion as string,
      evidenceStatus: row.evidenceStatus as CatalogVersionV1['evidenceStatus'],
      status: row.status as CatalogVersionV1['status'],
    })
  }
  return { ok: true, value: { schemaMajor: 1, versions } satisfies CatalogEnvelopeV1 }
}

declare global {
  interface Window { __CS90_SANITIZED_EXPORT__?: unknown }
}

export function mountFirst14Comparison(root: HTMLElement) {
  const queryClient = new QueryClient({ defaultOptions: { queries: { retry: false } } })
  createRoot(root).render(
    <QueryClientProvider client={queryClient}>
      <PersistedHealthComparison decoder={decodeInstructorCatalogEnvelope}
        onSanitizedExport={(payload) => { window.__CS90_SANITIZED_EXPORT__ = payload }} />
    </QueryClientProvider>,
  )
  return queryClient
}
