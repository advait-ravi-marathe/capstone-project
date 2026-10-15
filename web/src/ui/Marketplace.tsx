import { useEffect, useRef, useState } from 'react'
import { useQuery } from '@tanstack/react-query'
import type { Session } from '../state/session'
import { fetchPrivateJson, mutatePrivate } from '../state/session'
import { PersistedHealthComparison } from '../coverstory/catalog/PersistedHealthComparison'
import { SourceFreshnessPanel } from '../coverstory/catalog/SourceFreshnessPanel'
import { QuoteReviewPanel } from '../coverstory/catalog/QuoteReviewPanel'
import { PaymentAuditWindowPanel } from '../coverstory/payments/PaymentAuditWindowPanel'
import { DemoSessionControls } from './DemoSessionControls'
import { queryClient } from '../state/queryClient'

type Category = 'health' | 'term' | 'motor' | 'ulip'
type Plan = {
  id: string
  name: string
  version: string
  category: Category
  sourceStatus: 'SYNTHETIC'
  waitingPeriod: 'UNKNOWN'
  copay: 'UNKNOWN'
  network: 'UNKNOWN'
  sourceLabel: string
}

type CatalogResponse = { schemaMajor: 1; offers: Array<{
  offerId: string; catalogVersion: string; displayName: string; category: Category
  evidenceStatus: 'SYNTHETIC'; waitingPeriodStatus: 'UNKNOWN'; copayStatus: 'UNKNOWN'
  networkStatus: 'UNKNOWN'; sourceLabel: string
}> }
type Selection = { deviceScope: string; catalogVersionId: string }
type SelectionOrderResponse = { schemaMajor: 1; selections: Selection[] }
type WordingHit = { source_id: string; version: string; evidence_status: 'SYNTHETIC'; excerpt: string; score: number }
type WordingResponse = { schemaMajor: 1; classification: 'SYNTHETIC'; hits: WordingHit[] }
type OwnedJourney = { journeyId: string; status: string; resumeAction: string }
type OwnedJourneyPage = { journeys: OwnedJourney[]; nextCursor: string | null }
type OwnedShortlistItem = { offerId: string; catalogVersion: string }
type OwnedShortlistPage = { schemaMajor: 1; items: OwnedShortlistItem[] }

function readDeviceScope(): string {
  try {
    const existing = sessionStorage.getItem('coverstory.device-scope')
    if (existing && /^[0-9a-f-]{36}$/i.test(existing)) return existing
    const created = crypto.randomUUID(); sessionStorage.setItem('coverstory.device-scope', created); return created
  } catch { return crypto.randomUUID() }
}
function readDeviceSelections(): Selection[] {
  try {
    const value: unknown = JSON.parse(sessionStorage.getItem('coverstory.public-selections') ?? '[]')
    if (Array.isArray(value) && value.length <= 3 && value.every((row) => typeof row === 'object' && row !== null &&
      'deviceScope' in row && typeof row.deviceScope === 'string' && /^[0-9a-f-]{36}$/i.test(row.deviceScope) &&
      'catalogVersionId' in row && typeof row.catalogVersionId === 'string' &&
      /^[0-9a-f-]{36}@[A-Za-z0-9._-]{1,80}$/i.test(row.catalogVersionId))) return value
  } catch { /* stale or unavailable storage is an empty public selection */ }
  return []
}

async function fetchPublicCatalog(category: Category, signal: AbortSignal): Promise<Plan[]> {
  const response = await fetch(`/api/catalog?category=${encodeURIComponent(category)}`, {
    headers: { Accept: 'application/json' }, signal,
  })
  if (!response.ok) throw new Error(`Catalog request failed (${response.status}).`)
  const raw: unknown = await response.json()
  if (!isCatalogResponse(raw)) throw new Error('Catalog response was invalid.')
  return raw.offers.map((offer) => ({
    id: offer.offerId, name: offer.displayName, version: offer.catalogVersion,
    category: offer.category, sourceStatus: offer.evidenceStatus,
    waitingPeriod: offer.waitingPeriodStatus, copay: offer.copayStatus,
    network: offer.networkStatus, sourceLabel: offer.sourceLabel,
  }))
}

function isCatalogResponse(value: unknown): value is CatalogResponse {
  if (typeof value !== 'object' || value === null || !('schemaMajor' in value) ||
      value.schemaMajor !== 1 || !('offers' in value) || !Array.isArray(value.offers)) return false
  return value.offers.every((offer) => typeof offer === 'object' && offer !== null &&
    'offerId' in offer && typeof offer.offerId === 'string' &&
    'catalogVersion' in offer && typeof offer.catalogVersion === 'string' &&
    'displayName' in offer && typeof offer.displayName === 'string' &&
    'category' in offer && ['health', 'term', 'motor', 'ulip'].includes(String(offer.category)) &&
    'evidenceStatus' in offer && offer.evidenceStatus === 'SYNTHETIC' &&
    'waitingPeriodStatus' in offer && offer.waitingPeriodStatus === 'UNKNOWN' &&
    'copayStatus' in offer && offer.copayStatus === 'UNKNOWN' &&
    'networkStatus' in offer && offer.networkStatus === 'UNKNOWN' &&
    'sourceLabel' in offer && typeof offer.sourceLabel === 'string')
}

function isSelectionOrderResponse(value: unknown): value is SelectionOrderResponse {
  return typeof value === 'object' && value !== null && 'schemaMajor' in value && value.schemaMajor === 1 &&
    'selections' in value && Array.isArray(value.selections) && value.selections.every((item) =>
      typeof item === 'object' && item !== null && 'deviceScope' in item && typeof item.deviceScope === 'string' &&
      'catalogVersionId' in item && typeof item.catalogVersionId === 'string')
}

function isWordingResponse(value: unknown): value is WordingResponse {
  return typeof value === 'object' && value !== null && 'schemaMajor' in value && value.schemaMajor === 1 &&
    'classification' in value && value.classification === 'SYNTHETIC' && 'hits' in value &&
    Array.isArray(value.hits) && value.hits.every((hit) => typeof hit === 'object' && hit !== null &&
      'source_id' in hit && typeof hit.source_id === 'string' && 'version' in hit && typeof hit.version === 'string' &&
      'evidence_status' in hit && hit.evidence_status === 'SYNTHETIC' && 'excerpt' in hit &&
      typeof hit.excerpt === 'string' && 'score' in hit && typeof hit.score === 'number')
}

function isOwnedJourney(value: unknown): value is OwnedJourney {
  return typeof value === 'object' && value !== null &&
    'journeyId' in value && typeof value.journeyId === 'string' &&
    'status' in value && typeof value.status === 'string' &&
    'resumeAction' in value && typeof value.resumeAction === 'string'
}

async function orderSelections(selections: Selection[], signal: AbortSignal): Promise<Selection[]> {
  const response = await fetch('/api/shortlist/selection-order', {
    method: 'POST', headers: { Accept: 'application/json', 'Content-Type': 'application/json' },
    body: JSON.stringify({ selections }), signal,
  })
  if (!response.ok) throw new Error(response.status === 501
    ? 'CAP-40 selection ordering is still a learner task. The compare preview is waiting for that implementation.'
    : `Selection order could not be loaded (${response.status}).`)
  const raw: unknown = await response.json()
  if (!isSelectionOrderResponse(raw)) throw new Error('Selection order response was invalid.')
  const allowed = new Set(selections.map((item) => `${item.deviceScope}\u0000${item.catalogVersionId}`))
  const seen = new Set<string>()
  for (const item of raw.selections) {
    const key = `${item.deviceScope}\u0000${item.catalogVersionId}`
    if (!allowed.has(key) || seen.has(key)) throw new Error('Selection order response did not match the requested public examples.')
    seen.add(key)
  }
  return raw.selections
}

async function fetchWording(query: string, signal: AbortSignal): Promise<WordingHit[]> {
  const response = await fetch('/retrieval/health', {
    method: 'POST', headers: { Accept: 'application/json', 'Content-Type': 'application/json' },
    body: JSON.stringify({ query, k: 3 }), signal,
  })
  if (!response.ok) throw new Error(response.status === 501
    ? 'CAP-41 citation retrieval is still a learner task. No wording has been returned.'
    : `Synthetic wording search failed (${response.status}).`)
  const raw: unknown = await response.json()
  if (!isWordingResponse(raw)) throw new Error('Synthetic wording response was invalid.')
  return raw.hits
}

const categories: { id: Category; number: string; title: string; text: string }[] = [
  { id: 'health', number: '01', title: 'Health', text: 'Understand waiting periods, exclusions, and what is still unknown.' },
  { id: 'term', number: '02', title: 'Term life', text: 'Explore cover duration, disclosures, and the offer process.' },
  { id: 'motor', number: '03', title: 'Motor', text: 'Compare cover scope, deductibles, and repair-network evidence.' },
  { id: 'ulip', number: '04', title: 'ULIP', text: 'Review investment risk, charges, and exit terms separately.' },
]

const categoryLabel: Record<Category, string> = { health: 'Health', term: 'Term life', motor: 'Motor', ulip: 'ULIP' }
function categoryFromUrl(): Category {
  if (typeof window === 'undefined') return 'health'
  const candidate = new URLSearchParams(window.location.search).get('category')
  return candidate === 'term' || candidate === 'motor' || candidate === 'ulip' ? candidate : 'health'
}

export function Marketplace({ session, sessionStatus }: { session: Session | null; sessionStatus: 'checking' | 'unavailable' | 'ready' }) {
  const [category, setCategory] = useState<Category>(categoryFromUrl)
  const [deviceScope] = useState(readDeviceScope)
  const [selected, setSelected] = useState<Selection[]>(readDeviceSelections)
  const [orderedSelection, setOrderedSelection] = useState<Selection[] | null>(null)
  const [compareError, setCompareError] = useState<string | null>(null)
  const [comparing, setComparing] = useState(false)
  const [resumeResult, setResumeResult] = useState<OwnedJourney | null>(null)
  const [resumeError, setResumeError] = useState<string | null>(null)
  const [wordingInput, setWordingInput] = useState('')
  const [wordingSearch, setWordingSearch] = useState<string | null>(null)
  const [shortlistError, setShortlistError] = useState<string | null>(null)
  const [shortlistBusy, setShortlistBusy] = useState<string | null>(null)
  const compareGeneration = useRef(0)
  const catalogQuery = useQuery({
    queryKey: ['public', 'catalog', category],
    queryFn: ({ signal }) => fetchPublicCatalog(category, signal),
    staleTime: 60_000,
    retry: 0,
  })
  const visiblePlans = catalogQuery.data ?? []
  const wordingQuery = useQuery({
    queryKey: ['public', 'synthetic-wording', wordingSearch],
    queryFn: ({ signal }) => fetchWording(wordingSearch ?? '', signal),
    enabled: wordingSearch !== null,
    staleTime: 0,
    retry: 0,
  })
  const ownedJourneys = useQuery({
    queryKey: ['private', 'journeys', session?.ownerId ?? 'none', session?.generation ?? -1],
    queryFn: async ({ signal }) => {
      if (!session) throw new Error('A verified session is required.')
      const page = await fetchPrivateJson<OwnedJourneyPage>(session, '/api/v1/me/journeys?limit=20', signal)
      if (!Array.isArray(page.journeys) || page.journeys.some((row) => !isOwnedJourney(row)))
        throw new Error('Journey response was invalid.')
      return page
    },
    enabled: sessionStatus === 'ready' && session !== null,
    staleTime: 0,
    refetchOnMount: 'always',
    retry: 0,
  })
  const ownedShortlist = useQuery({
    queryKey: ['private', 'shortlist', session?.ownerId ?? 'none', session?.generation ?? -1],
    queryFn: async ({ signal }) => {
      if (!session) throw new Error('A verified session is required.')
      const page = await fetchPrivateJson<OwnedShortlistPage>(session, '/api/v1/me/shortlist', signal)
      if (page.schemaMajor !== 1 || !Array.isArray(page.items) || page.items.some((item) => !item.offerId || !item.catalogVersion)) throw new Error('Shortlist response was invalid.')
      return page
    },
    enabled: sessionStatus === 'ready' && session !== null,
    staleTime: 0, refetchOnMount: 'always', retry: 0,
  })

  useEffect(() => {
    try { sessionStorage.setItem('coverstory.public-selections', JSON.stringify(selected.slice(0,3))) } catch { /* selection remains in memory */ }
  }, [selected])

  useEffect(() => {
    setResumeResult(null)
    setResumeError(null)
  }, [session?.ownerId, session?.generation, sessionStatus])

  useEffect(() => {
    const syncCategory = () => {
      setCategory(categoryFromUrl())
      setSelected([])
      setOrderedSelection(null)
      setCompareError(null)
      setComparing(false)
      compareGeneration.current += 1
    }
    window.addEventListener('popstate', syncCategory)
    return () => {
      window.removeEventListener('popstate', syncCategory)
      compareGeneration.current += 1
    }
  }, [])

  function chooseCategory(next: Category) {
    const url = new URL(window.location.href)
    url.searchParams.set('category', next)
    window.history.pushState({}, '', url)
    setCategory(next)
    setSelected([])
    setOrderedSelection(null)
    setCompareError(null)
    setComparing(false)
    compareGeneration.current += 1
  }

  function selectionFor(plan: Plan): Selection {
    return { deviceScope, catalogVersionId: `${plan.id}@${plan.version}` }
  }

  function togglePlan(plan: Plan) {
    setComparing(false)
    setOrderedSelection(null)
    setCompareError(null)
    compareGeneration.current += 1
    const next = selectionFor(plan)
    setSelected((current) => current.some((item) => item.catalogVersionId === next.catalogVersionId)
      ? current.filter((item) => item.catalogVersionId !== next.catalogVersionId)
      : current.length < 3 ? [...current, next] : current)
  }

  async function compareSelected() {
    setCompareError(null)
    setComparing(false)
    const requestGeneration = ++compareGeneration.current
    try {
      const ordered = await orderSelections(selected, new AbortController().signal)
      if (requestGeneration !== compareGeneration.current) return
      if (ordered.length < 2) throw new Error('Choose at least two distinct public examples to compare.')
      setOrderedSelection(ordered)
      setComparing(true)
    } catch (error) {
      if (requestGeneration !== compareGeneration.current) return
      setCompareError(error instanceof Error ? error.message : 'Selection order could not be loaded.')
    }
  }

  async function resumeJourney(journeyId: string) {
    if (!session) return
    setResumeError(null)
    setResumeResult(null)
    try {
      const result = await fetchPrivateJson<OwnedJourney>(session,
        `/api/v1/me/journeys/${encodeURIComponent(journeyId)}/resume`, new AbortController().signal)
      if (!isOwnedJourney(result)) throw new Error('Resume response was invalid.')
      setResumeResult(result)
    } catch {
      setResumeError('This journey could not be resumed. Refresh your session and try again.')
    }
  }

  async function saveOffer(plan: Plan) {
    if (!session) { setShortlistError('Choose a synthetic demo account before saving an owned shortlist.'); return }
    const key = `${plan.id}@${plan.version}`; setShortlistBusy(key); setShortlistError(null)
    try {
      await mutatePrivate(session, '/api/v1/me/shortlist/items', 'POST', { offerId: plan.id, catalogVersion: plan.version })
      await queryClient.invalidateQueries({ queryKey: ['private', 'shortlist', session.ownerId, session.generation] })
    } catch (error) {
      if (!(error instanceof DOMException && error.name === 'AbortError'))
        setShortlistError(error instanceof Error ? error.message : 'Shortlist could not be saved.')
    }
    finally { setShortlistBusy(null) }
  }

  async function removeOffer(offerId: string) {
    if (!session) return
    setShortlistError(null)
    try {
      await mutatePrivate(session, `/api/v1/me/shortlist/items?offerId=${encodeURIComponent(offerId)}`, 'DELETE')
      await queryClient.invalidateQueries({ queryKey: ['private', 'shortlist', session.ownerId, session.generation] })
    } catch (error) {
      if (!(error instanceof DOMException && error.name === 'AbortError'))
        setShortlistError(error instanceof Error ? error.message : 'Shortlist could not be changed.')
    }
  }

  const selectedVersionIds = new Set(selected.map((item) => item.catalogVersionId))
  const selectedPlans = orderedSelection === null ? [] : orderedSelection
    .map((item) => visiblePlans.find((plan) => selectionFor(plan).catalogVersionId === item.catalogVersionId))
    .filter((plan): plan is Plan => plan !== undefined)

  return (
    <div className="page-shell">
      <div className="simulation-bar"><span>WIREFRAME · SYNTHETIC DATA · NO REAL OFFERS</span><a href="#source-notes">Source and status notes</a></div>
      <header className="topbar">
        <a className="wordmark" href="#top" aria-label="CoverStory home">coverstory<span>.</span><small>/ learning preview</small></a>
        <nav aria-label="Main navigation"><a href="#catalog">Explore cover</a><a href="#shortlist">Your shortlist</a><a href="#source-notes">Policy help</a></nav>
        <DemoSessionControls session={session} />
      </header>

      <nav className="journey-steps" aria-label="Learning journey">
        {['Explore', 'Compare', 'Apply', 'Onboard', 'Track'].map((label, index) => <span className={index === (comparing ? 1 : 0) ? 'journey-step is-current' : 'journey-step'} key={label}><b>{index + 1}</b>{label}</span>)}
      </nav>

      <main id="top">
        <section className="hero" aria-labelledby="hero-title">
          <div className="hero-copy">
            <p className="eyebrow">DISCOVER / {categoryLabel[category].toUpperCase()}</p>
            <h1 id="hero-title">For the unexpected hospital chapter.</h1>
            <p className="hero-text">Review what is supplied, what is unknown, and which source version you are looking at. No plan is ranked or recommended.</p>
            <div className="hero-actions"><a className="primary-button" href="#catalog">Explore example records <span aria-hidden="true">↓</span></a><a className="text-link" href="#source-notes">How to read the labels</a></div>
          </div>
          <aside className="resume-card" aria-label="Private journey resume">
            <span className="small-label">YOUR SHORTLIST</span>
            <h2>Keep the next step clear.</h2>
            <p>{sessionStatus === 'checking' ? 'Rechecking your session before showing private journeys…' : session ? 'Only records owned by your current synthetic account are shown.' : 'Public examples stay on this device; owned records need a synthetic demo account.'}</p>
            {session && sessionStatus === 'ready' && <>
              {ownedShortlist.isPending && <p role="status">Loading your saved shortlist…</p>}
              {ownedShortlist.data?.items.length === 0 && <p>No saved examples for this account.</p>}
              {ownedShortlist.data?.items.map((item) => <div key={`${item.offerId}@${item.catalogVersion}`} className="saved-shortlist-item"><span>{item.offerId} · {item.catalogVersion}</span><button type="button" className="text-link" onClick={() => void removeOffer(item.offerId)}>Remove</button></div>)}
              {ownedShortlist.isError && <p className="notice" role="alert">Saved shortlist is unavailable. Public browsing remains available.</p>}
              {ownedJourneys.isPending && <p role="status">Loading your saved journeys…</p>}
              {ownedJourneys.isError && <p className="notice" role="alert">Your private journeys are unavailable. Public browsing remains available.</p>}
              {ownedJourneys.data?.journeys.length === 0 && <p>No saved journeys are linked to this account.</p>}
              {ownedJourneys.data?.journeys.map((journey) => <button key={journey.journeyId} type="button" className="secondary-button" onClick={() => void resumeJourney(journey.journeyId)}>Resume journey</button>)}
              {resumeError && <p className="notice" role="alert">{resumeError}</p>}
              {resumeResult && <p role="status">{resumeResult.status} · {resumeResult.resumeAction}</p>}
            </>}
            {!session && <p className="fine-print">Use the synthetic account controls above for the instructor demo. They are not production authentication.</p>}
            {shortlistError && <p className="notice" role="alert">{shortlistError}</p>}
            <p className="fine-print">A journey ID alone never grants access.</p>
          </aside>
        </section>

        <section className="category-section" aria-labelledby="category-title">
          <div className="section-heading"><div><p className="eyebrow">FOUR WAYS TO EXPLORE</p><h2 id="category-title">Choose a kind of cover.</h2></div><span className="demo-badge">PUBLIC PREVIEW</span></div>
          <div className="category-grid" role="group" aria-label="Cover category">
            {categories.map((item) => <button key={item.id} type="button" className={`category-card category-${item.id}${category === item.id ? ' is-selected' : ''}`} aria-pressed={category === item.id} onClick={() => chooseCategory(item.id)}>
              <span className="category-index">{item.number} / COVER</span><span className="category-arrow" aria-hidden="true" /><strong>{item.title}</strong><span>{item.text}</span>
            </button>)}
          </div>
        </section>

        <section className="catalog-section" id="catalog" aria-labelledby="catalog-title">
          <div className="section-heading"><div><p className="eyebrow">{categoryLabel[category].toUpperCase()} / SYNTHETIC FIXTURE</p><h2 id="catalog-title">{category === 'health' ? 'Make a shortlist.' : `${categoryLabel[category]} examples are not loaded yet.`}</h2></div><span className="source-pill">No payable quotes</span></div>
            {category === 'health' ? <>
            <p className="section-intro">These named synthetic examples exercise comparison behavior. They do not contain insurer-authored terms or prices.</p>
            <div className="selection-toolbar" id="shortlist"><span><strong>{selected.length}</strong> of 3 selected</span><button type="button" className="primary-button compact-button" disabled={selected.length < 2} onClick={() => void compareSelected()}>Compare selected</button></div>
            {compareError && <p className="notice" role="alert">{compareError}</p>}
            {catalogQuery.isPending && <p role="status">Loading synthetic catalog examples…</p>}
            {catalogQuery.isError && <p className="notice" role="alert">Catalog examples could not be loaded. Retry after the local API is available.</p>}
            {selected.length === 3 && <p className="inline-note" role="status">The maximum is three same-category examples.</p>}
            <div className="offer-grid">
              {visiblePlans.map((plan, index) => <article className="offer-card" key={plan.id}>
                <div className="offer-card-top"><span className="source-pill">SYNTHETIC</span><span className="record-number">0{index + 1}</span></div>
                <h3>{plan.name}</h3><p className="version-line">Health · illustrative record · {plan.version}</p>
                <dl className="plan-facts"><div><dt>Waiting periods</dt><dd>Unknown</dd></div><div><dt>Co-pay / deductible</dt><dd>Unknown</dd></div><div><dt>Network</dt><dd>Not verified</dd></div></dl>
                <p className="source-caption">{plan.sourceLabel} · {plan.id}</p>
                <label className="selection-control"><input type="checkbox" checked={selectedVersionIds.has(selectionFor(plan).catalogVersionId)} disabled={!selectedVersionIds.has(selectionFor(plan).catalogVersionId) && selected.length >= 3} onChange={() => togglePlan(plan)} /><span>Add to compare preview</span></label>
                <button type="button" className="secondary-button" disabled={!session || shortlistBusy === `${plan.id}@${plan.version}`} onClick={() => void saveOffer(plan)}>{shortlistBusy === `${plan.id}@${plan.version}` ? 'Saving…' : 'Save to owned shortlist'}</button>
              </article>)}
            </div>
            {comparing && selectedPlans.length >= 2 && <section className="compare-panel" aria-labelledby="compare-title" aria-live="polite">
              <div className="section-heading"><div><p className="eyebrow">COMPARE / HEALTH</p><h2 id="compare-title">The differences, in daylight.</h2></div><button type="button" className="text-link" onClick={() => setComparing(false)}>Edit shortlist</button></div>
              <p className="unknown-callout"><strong>Unknown is not included.</strong><span>Missing information is shown explicitly. Neither example is ranked or recommended.</span></p>
              <div className="table-wrap"><table><thead><tr><th scope="col">What to check</th>{selectedPlans.map((plan) => <th scope="col" key={plan.id}><span className="source-pill">SYNTHETIC</span><strong>{plan.name}</strong><small>Version {plan.version}</small></th>)}</tr></thead><tbody>
                {(['Waiting period', 'Co-pay / deductible', 'Hospital network', 'Exclusions', 'Premium / quote status', 'Source and version'] as const).map((fact) => <tr key={fact}><th scope="row">{fact}</th>{selectedPlans.map((plan) => <td key={plan.id}>{fact === 'Source and version' ? `${plan.id} · ${plan.version}` : fact === 'Premium / quote status' ? 'No price supplied · no quote available' : fact === 'Hospital network' ? 'Not verified' : 'Not supplied'}</td>)}</tr>)}
              </tbody></table></div>
              <p className="payable-note">This fixture cannot continue to payable review. A later API must provide a supported scenario, current source version, explicit terms, and a complete quote.</p>
            </section>}
            <section className="source-notes" aria-labelledby="wording-search-title">
              <div><p className="eyebrow">CAP-41 / LOCAL RETRIEVAL PRACTICE</p><h2 id="wording-search-title">Find a synthetic wording excerpt.</h2></div>
              <p>These fixture phrases are not policy language, coverage terms, or insurer evidence. Search runs against the fixed local fixture and returns source IDs and versions.</p>
              <form onSubmit={(event) => { event.preventDefault(); setWordingSearch(wordingInput) }}>
                <label htmlFor="wording-query">Search fixture phrases</label>
                <input id="wording-query" value={wordingInput} maxLength={200} onChange={(event) => setWordingInput(event.target.value)} />
                <button type="submit" className="secondary-button">Search synthetic excerpts</button>
              </form>
              {wordingQuery.isFetching && <p role="status">Searching fixed synthetic wording…</p>}
              {wordingQuery.isError && <p className="notice" role="alert">{wordingQuery.error.message}</p>}
              {wordingQuery.data && (wordingQuery.data.length === 0
                ? <p role="status">No matching synthetic excerpt. No policy conclusion is inferred.</p>
                : <ol>{wordingQuery.data.map((hit) => <li key={`${hit.source_id}:${hit.version}`}>
                  <q>{hit.excerpt}</q> <span>{hit.source_id} · {hit.version} · {hit.evidence_status} · score {hit.score}</span>
                </li>)}</ol>)}
            </section>
            </> : <div className="notice">This category entry point is present in the instructor shell. Its category-specific synthetic fixture and acceptance seam have not been loaded.</div>}
        </section>

        <SourceFreshnessPanel />
        <QuoteReviewPanel session={session} ready={sessionStatus === 'ready'} />
        <PaymentAuditWindowPanel session={session} ready={sessionStatus === 'ready'} />
        <PersistedHealthComparison />

        <section className="source-notes" id="source-notes" aria-labelledby="source-title"><div><p className="eyebrow">READ THE EVIDENCE</p><h2 id="source-title">A label is part of the answer.</h2></div><ul><li><strong>Unknown</strong> means the source did not provide this field. It is never treated as included.</li><li><strong>Not verified</strong> is not a network or coverage guarantee.</li><li><strong>Synthetic</strong> describes this local layout fixture. It is not an insurer offer.</li></ul></section>
      </main>
      <footer><span className="wordmark">coverstory<span>.</span></span><p>Learning simulation · Synthetic data only · No transactions</p></footer>
    </div>
  )
}
