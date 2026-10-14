import { useEffect, useState } from 'react'
import { useQuery } from '@tanstack/react-query'
import type { Session } from '../state/session'

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
  const [selected, setSelected] = useState<string[]>([])
  const [comparing, setComparing] = useState(false)
  const catalogQuery = useQuery({
    queryKey: ['public', 'catalog', category],
    queryFn: ({ signal }) => fetchPublicCatalog(category, signal),
    staleTime: 60_000,
    retry: 0,
  })
  const visiblePlans = catalogQuery.data ?? []

  useEffect(() => {
    const syncCategory = () => setCategory(categoryFromUrl())
    window.addEventListener('popstate', syncCategory)
    return () => window.removeEventListener('popstate', syncCategory)
  }, [])

  function chooseCategory(next: Category) {
    const url = new URL(window.location.href)
    url.searchParams.set('category', next)
    window.history.pushState({}, '', url)
    setCategory(next)
    setSelected([])
    setComparing(false)
  }

  function togglePlan(id: string) {
    setComparing(false)
    setSelected((current) => current.includes(id)
      ? current.filter((item) => item !== id)
      : current.length < 3 ? [...current, id] : current)
  }

  const selectedPlans = visiblePlans.filter((plan) => selected.includes(plan.id))

  return (
    <div className="page-shell">
      <div className="simulation-bar"><span>WIREFRAME · SYNTHETIC DATA · NO REAL OFFERS</span><a href="#source-notes">Source and status notes</a></div>
      <header className="topbar">
        <a className="wordmark" href="#top" aria-label="CoverStory home">coverstory<span>.</span><small>/ learning preview</small></a>
        <nav aria-label="Main navigation"><a href="#catalog">Explore cover</a><a href="#shortlist">Your shortlist</a><a href="#source-notes">Policy help</a></nav>
        <span className="session-label" aria-live="polite">{session ? 'Signed in' : sessionStatus === 'checking' ? 'Checking session' : 'Guest preview'}</span>
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
            <p>{session ? 'Owned journey resume is not connected in this starter.' : 'Sign-in and owned journey resume are not connected in this starter.'}</p>
            <button type="button" className="secondary-button" disabled>Resume an owned journey</button>
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
            <div className="selection-toolbar" id="shortlist"><span><strong>{selected.length}</strong> of 3 selected</span><button type="button" className="primary-button compact-button" disabled={selected.length < 2} onClick={() => setComparing(true)}>Compare selected</button></div>
            {catalogQuery.isPending && <p role="status">Loading synthetic catalog examples…</p>}
            {catalogQuery.isError && <p className="notice" role="alert">Catalog examples could not be loaded. Retry after the local API is available.</p>}
            {selected.length === 3 && <p className="inline-note" role="status">The maximum is three same-category examples.</p>}
            <div className="offer-grid">
              {visiblePlans.map((plan, index) => <article className="offer-card" key={plan.id}>
                <div className="offer-card-top"><span className="source-pill">SYNTHETIC</span><span className="record-number">0{index + 1}</span></div>
                <h3>{plan.name}</h3><p className="version-line">Health · illustrative record · {plan.version}</p>
                <dl className="plan-facts"><div><dt>Waiting periods</dt><dd>Unknown</dd></div><div><dt>Co-pay / deductible</dt><dd>Unknown</dd></div><div><dt>Network</dt><dd>Not verified</dd></div></dl>
                <p className="source-caption">{plan.sourceLabel} · {plan.id}</p>
                <label className="selection-control"><input type="checkbox" checked={selected.includes(plan.id)} disabled={!selected.includes(plan.id) && selected.length >= 3} onChange={() => togglePlan(plan.id)} /><span>Add to shortlist</span></label>
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
            </> : <div className="notice">This category entry point is present in the instructor shell. Its category-specific synthetic fixture and acceptance seam have not been loaded.</div>}
        </section>

        <section className="source-notes" id="source-notes" aria-labelledby="source-title"><div><p className="eyebrow">READ THE EVIDENCE</p><h2 id="source-title">A label is part of the answer.</h2></div><ul><li><strong>Unknown</strong> means the source did not provide this field. It is never treated as included.</li><li><strong>Not verified</strong> is not a network or coverage guarantee.</li><li><strong>Synthetic</strong> describes this local layout fixture. It is not an insurer offer.</li></ul></section>
      </main>
      <footer><span className="wordmark">coverstory<span>.</span></span><p>Learning simulation · Synthetic data only · No transactions</p></footer>
    </div>
  )
}
