import { useQuery } from '@tanstack/react-query'
import { fetchPrivateJson, type Session } from '../state/session'

type CatalogItem = { offerId: string; displayName: string; coverLabel: string }
type CatalogResponse = { items: CatalogItem[] }

export function Marketplace({ session }: { session: Session }) {
  const catalog = useQuery({
    queryKey: ['private', session.ownerId, session.generation, 'catalog'],
    queryFn: ({ signal }) => fetchPrivateJson<CatalogResponse>(session, '/api/catalog', signal),
  })

  return (
    <div className="page-shell">
      <header className="topbar">
        <a className="wordmark" href="/" aria-label="CoverStory home">coverstory<span>.</span></a>
        <nav aria-label="Main navigation"><a href="#catalog">Explore plans</a><a href="#how-it-works">How it works</a></nav>
        <button className="avatar" type="button" aria-label="Account menu">CS</button>
      </header>
      <main>
        <section className="hero" id="how-it-works">
          <div className="hero-copy">
            <p className="eyebrow"><span className="eyebrow-dot" /> COVER, MADE CLEARER</p>
            <h1>Good cover starts with a <em>clear story.</em></h1>
            <p className="hero-text">Explore a learning simulation for comparing insurance options, understanding trade-offs, and keeping your application journey in one place.</p>
            <a className="primary-button" href="#catalog">Explore the catalog <span aria-hidden="true">↗</span></a>
            <p className="microcopy">A product simulation. No insurer or payment provider is connected.</p>
          </div>
          <div className="hero-art" aria-hidden="true">
            <div className="orbit orbit-one" /><div className="orbit orbit-two" />
            <div className="cover-card"><div className="cover-card-top"><span className="tiny-mark">c.</span><span className="card-label">YOUR COVER STORY</span></div><div className="sun-shape" /><div className="cover-lines"><i /><i /><i /></div><div className="card-footer"><span>Compare with confidence</span><b>✳</b></div></div>
            <span className="spark spark-a">✳</span><span className="spark spark-b">✦</span>
          </div>
        </section>
        <section className="trust-strip" aria-label="Product principles">
          <span><b>01</b> Clear options</span><span><b>02</b> Your journey, saved</span><span><b>03</b> No confusing jargon</span>
        </section>
        <section className="catalog-section" id="catalog">
          <div className="section-heading"><div><p className="eyebrow">A BETTER WAY TO EXPLORE</p><h2>Start with what matters to you.</h2></div><span className="demo-badge">SIMULATION</span></div>
          {catalog.isPending && <p className="notice">Loading synthetic catalog…</p>}
          {catalog.isError && <p className="notice">The catalog API scaffold is not connected yet. This screen is ready for synthetic offers.</p>}
          {catalog.data?.items.length === 0 && <p className="notice">No synthetic offers are available yet.</p>}
          {catalog.data && catalog.data.items.length > 0 && <div className="offer-grid">{catalog.data.items.map((item) => <article className="offer-card" key={item.offerId}><span className="offer-icon">✳</span><h3>{item.displayName}</h3><p>{item.coverLabel}</p><button type="button" className="text-button">View details <span aria-hidden="true">→</span></button></article>)}</div>}
        </section>
      </main>
      <footer><span className="wordmark">coverstory<span>.</span></span><p>Learning simulation · Synthetic data only</p></footer>
    </div>
  )
}
