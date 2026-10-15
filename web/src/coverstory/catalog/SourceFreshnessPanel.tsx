import { useQuery } from '@tanstack/react-query'

export type FreshnessRow = {
  sourceId: string
  version: string
  status: 'FRESH' | 'STALE' | 'INVALID_TIME'
  evidenceType: 'POLICY_WORDING' | 'ADVERTISEMENT_ILLUSTRATION'
  contentSha256: string
}
export type FreshnessResult = { schemaMajor: 1; status: 'FRESH' | 'ABSTAIN'; rows: FreshnessRow[]; quoteOrderEligible: false }

function decode(value: unknown): FreshnessResult {
  if (typeof value !== 'object' || value === null || !('schemaMajor' in value) || value.schemaMajor !== 1 ||
      !('status' in value) || (value.status !== 'FRESH' && value.status !== 'ABSTAIN') ||
      !('rows' in value) || !Array.isArray(value.rows) || !('quoteOrderEligible' in value) || value.quoteOrderEligible !== false)
    throw new Error('Freshness response is invalid.')
  const rows = value.rows.map((row): FreshnessRow => {
    if (typeof row !== 'object' || row === null || !('sourceId' in row) || typeof row.sourceId !== 'string' ||
        !('version' in row) || typeof row.version !== 'string' || !('status' in row) ||
        !['FRESH', 'STALE', 'INVALID_TIME'].includes(String(row.status)) || !('evidenceType' in row) ||
        !['POLICY_WORDING', 'ADVERTISEMENT_ILLUSTRATION'].includes(String(row.evidenceType)) ||
        !('contentSha256' in row) || typeof row.contentSha256 !== 'string' || !/^[a-f0-9]{64}$/.test(row.contentSha256))
      throw new Error('Freshness row is invalid.')
    return row as FreshnessRow
  })
  return { schemaMajor: 1, status: value.status, rows, quoteOrderEligible: false }
}

async function loadFreshness(signal: AbortSignal): Promise<FreshnessResult> {
  const response = await fetch('/api/catalog/source-freshness', { headers: { Accept: 'application/json' }, signal })
  if (response.status === 501) throw new Error('CAP-79 freshness filtering is still a learner task. Source status is unavailable.')
  if (!response.ok) throw new Error(`Source status could not be loaded (${response.status}).`)
  return decode(await response.json())
}

export function SourceFreshnessPanel() {
  const query = useQuery({ queryKey: ['public', 'source-freshness'], queryFn: ({ signal }) => loadFreshness(signal), retry: 0, staleTime: 0 })
  return <section className="source-freshness" aria-labelledby="freshness-title">
    <div><p className="eyebrow">SOURCE FRESHNESS</p><h2 id="freshness-title">Check the exact version.</h2>
      <p>Freshness never turns an illustration into a payable quote.</p></div>
    {query.isPending && <p role="status">Checking source status…</p>}
    {query.error && <p role="status" className="freshness-error">{query.error.message}</p>}
    {query.data && <><p role="status">{query.data.status === 'FRESH' ? 'Fresh evidence available' : 'No usable evidence; abstain'}</p>
      <ul>{query.data.rows.map((row) => <li key={`${row.sourceId}@${row.version}`}>
        <strong>{row.sourceId} · {row.version}</strong><span>{row.status} · {row.evidenceType.replaceAll('_', ' ')}</span>
      </li>)}</ul></>}
  </section>
}
