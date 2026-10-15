import { useState } from 'react'
import { useQuery } from '@tanstack/react-query'
import { decodeCatalogEnvelope, type CatalogEnvelopeResult, type CatalogEnvelopeV1 } from './decodeCatalogEnvelope'

type CatalogDecoder = (raw: unknown) => CatalogEnvelopeResult

export interface SanitizedCatalogExport {
  schemaVersion: 1
  selection: Array<{ versionId: string }>
}

type SanitizedExportAdapter = (payload: SanitizedCatalogExport) => void

async function readPersistedCatalog(signal: AbortSignal, decode: CatalogDecoder): Promise<CatalogEnvelopeV1> {
  const response = await fetch('/api/catalog/versions?category=health', { headers: { Accept: 'application/json' }, signal })
  if (!response.ok) throw new Error(`catalog unavailable (${response.status})`)
  const raw: unknown = await response.json()
  const decoded = decode(raw)
  if (!decoded.ok) throw new Error('unsupported catalog schema')
  return decoded.value
}

/** CAP-166 customer-facing shell. The decoder remains the learner-owned seam. */
export function PersistedHealthComparison({
  decoder = decodeCatalogEnvelope,
  onSanitizedExport,
}: { decoder?: CatalogDecoder; onSanitizedExport?: SanitizedExportAdapter }) {
  const [selectedIds, setSelectedIds] = useState<string[]>([])
  const [comparing, setComparing] = useState(false)
  const catalog = useQuery({
    queryKey: ['public', 'catalog', 'persisted-health', 1],
    queryFn: ({ signal }) => readPersistedCatalog(signal, decoder),
    staleTime: 0,
    retry: 0,
  })
  const versions = catalog.data?.versions ?? []
  const eligible = versions.filter((version) => version.category === 'health' && version.scenarioId === 'individual')
  const selected = eligible.filter((version) => selectedIds.includes(version.versionId))
  const stale = catalog.isError && catalog.data !== undefined

  function toggle(versionId: string) {
    setSelectedIds((current) => current.includes(versionId)
      ? current.filter((id) => id !== versionId)
      : current.length < 3 ? [...current, versionId] : current)
    setComparing(false)
  }

  function exportSanitizedSelection() {
    const payload: SanitizedCatalogExport = {
      schemaVersion: 1,
      selection: selectedIds.map((versionId) => ({ versionId })),
    }
    if (onSanitizedExport) {
      onSanitizedExport(payload)
      return
    }
    const blob = new Blob([JSON.stringify(payload, null, 2)], { type: 'application/json' })
    const href = URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = href
    link.download = 'coverstory-catalog-selection.json'
    link.click()
    URL.revokeObjectURL(href)
  }

  return <section className="catalog-section persisted-catalog" aria-labelledby="persisted-catalog-title">
    <div className="section-heading"><div><p className="eyebrow">DAY 014 / PERSISTED CATALOG</p><h2 id="persisted-catalog-title">Compare saved source versions.</h2></div></div>
    <p className="section-intro">Read-only synthetic catalog history from PostgreSQL. Source versions and publication status are shown without price, ranking, coverage, or payable claims.</p>
    {catalog.isPending && <p role="status">Loading persisted synthetic catalog…</p>}
    {catalog.isError && !catalog.data && <p className="notice" role="alert">Catalog history is unavailable or could not be decoded. No partial comparison is shown. Retry after the catalog service is available.</p>}
    {selectedIds.length > 0 && <div className="export-sanitized-selection">
      <p className="inline-note">Save your selection as a sanitized export. It contains version identifiers only.</p>
      <button type="button" className="secondary-button" onClick={exportSanitizedSelection}>Export sanitized selection</button>
    </div>}
    {stale && <p className="notice" role="alert">Refresh failed. The versions below are cached and may be stale. They cannot continue to payable review.</p>}
    <button type="button" className="secondary-button refresh-persisted-catalog" onClick={() => void catalog.refetch()}>Refresh catalog history</button>
    {catalog.data && <>
      <div className="selection-toolbar"><span><strong>{selectedIds.length}</strong> of 3 selected</span>
        <button type="button" className="primary-button compact-button" disabled={selectedIds.length < 1 || stale} onClick={() => setComparing(true)}>Compare saved versions</button></div>
      <p className="inline-note" role="status">A retired version remains visible as history and is never silently replaced by a newer publication.</p>
      <div className="offer-grid">{eligible.map((version, index) => <article className="offer-card" key={version.versionId}>
        <div className="offer-card-top"><span className="source-pill">{version.evidenceStatus}</span><span className="record-number">0{index + 1}</span></div>
        <h3>{version.sourceId}</h3><p className="version-line">{version.category} · {version.scenarioId} · {version.sourceVersion}</p>
        <dl className="plan-facts"><div><dt>Publication</dt><dd>{version.status}</dd></div><div><dt>Waiting period</dt><dd>Unknown</dd></div><div><dt>Co-pay</dt><dd>Unknown</dd></div></dl>
        <label className="selection-control"><input type="checkbox" checked={selectedIds.includes(version.versionId)} disabled={!selectedIds.includes(version.versionId) && selectedIds.length >= 3}
          onChange={() => toggle(version.versionId)} /><span>Select version for comparison</span></label>
      </article>)}</div>
      {eligible.length !== versions.length && <p className="notice" role="alert">Other categories or scenarios cannot be mixed into this health comparison; eligible health selections remain available.</p>}
      {selectedIds.length >= 3 && <p role="status">Three versions are selected. Remove one before choosing another.</p>}
      {comparing && selected.length > 0 && <section className="compare-panel" aria-labelledby="persisted-compare-title" aria-live="polite">
        <h3 id="persisted-compare-title">Saved version comparison</h3>
        <p className="unknown-callout"><strong>Unknown is not included.</strong><span>This instructor slice displays version metadata only; it is not a quote or recommendation.</span></p>
        <div className="table-wrap"><table><thead><tr><th scope="col">Source field</th>{selected.map((v) => <th scope="col" key={v.versionId}>{v.sourceId}<small>{v.sourceVersion}</small></th>)}</tr></thead>
          <tbody>{([['Version ID', 'versionId'], ['Scenario', 'scenarioId'], ['Evidence', 'evidenceStatus'], ['Publication status', 'status']] as const).map(([label, field]) => <tr key={field}><th scope="row">{label}</th>{selected.map((v) => <td key={v.versionId}>{v[field]}</td>)}</tr>)}</tbody></table></div>
        <p className="payable-note">Payable review is unavailable in this slice. Retired versions are history, not a current offer.</p>
      </section>}
    </>}
  </section>
}
