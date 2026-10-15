import React, { useMemo, useState } from 'react';

const offers = [
  { id: 'ST-14', name: 'Northstar Storage', type: 'Standard', units: 240, fee: 1240, days: 2, tag: 'Balanced' },
  { id: 'ST-28', name: 'Harbor Fulfillment', type: 'Oversize', units: 120, fee: 980, days: 3, tag: 'Lower fee' },
  { id: 'ST-36', name: 'Juniper Network', type: 'Standard', units: 300, fee: 1560, days: 1, tag: 'Fastest' },
];

export default function App() {
  const [query, setQuery] = useState('');
  const [selected, setSelected] = useState('ST-14');
  const [status, setStatus] = useState('ready');
  const [notice, setNotice] = useState('');
  const filtered = useMemo(() => offers.filter((o) => `${o.name} ${o.type}`.toLowerCase().includes(query.toLowerCase())), [query]);
  return <main className="shell">
    <header className="top"><a className="brand" href="#home" aria-label="Placement Studio home"><span className="brand-icon">p</span> placement<span className="brand-light">studio</span></a><div className="demo-pill"><span className="dot"/>Practice preview · synthetic data</div><button className="avatar" aria-label="Profile">A</button></header>
    <section className="intro"><p className="eyebrow">FBA · INVENTORY PLACEMENT</p><h1>Find the right home<br/>for your inventory.</h1><p className="lede">Compare sample placement options and choose what fits your plan.</p><div className="steps"><span className="step active"><i>1</i> Review options</span><b>—</b><span className="step"><i>2</i> Confirm shipment</span></div></section>
    <section className="workspace"><div className="section-head"><div><p className="eyebrow">YOUR OPTIONS <span className="count">03</span></p><h2>Choose a placement</h2></div><button className="refresh" onClick={() => {setStatus('loading'); setTimeout(() => setStatus('ready'), 450);}}>↻ <span>Refresh</span></button></div>
      <label className="search"><span aria-hidden="true">⌕</span><input value={query} onChange={(e) => setQuery(e.target.value)} placeholder="Search placement or type" aria-label="Search placement options"/></label>
      {status === 'loading' && <p className="status" role="status">Refreshing sample options…</p>}
      {filtered.length === 0 ? <div className="empty"><span>⌕</span><b>No placements found</b><p>Try a different search.</p></div> : <div className="cards">{filtered.map((offer) => <button className={`offer ${selected === offer.id ? 'chosen' : ''}`} key={offer.id} onClick={() => setSelected(offer.id)} aria-pressed={selected === offer.id}>
        <div className="offer-head"><div className="warehouse">▦</div><span className="tag">{offer.tag}</span><span className="radio"/></div><p className="kind">{offer.type} placement</p><h3>{offer.name}</h3><div className="metrics"><div><span>EST. PLACEMENT FEE</span><b>₹{offer.fee.toLocaleString('en-IN')}</b></div><div><span>INBOUND UNITS</span><b>{offer.units} <small>units</small></b></div></div><div className="eta"><span>◷ &nbsp;Estimated delivery</span><b>{offer.days} days</b></div>
      </button>)}</div>}
      <div className="bottom"><p><span>ⓘ</span> Estimates are for practice only. Final costs may change.</p><button className="continue" disabled={!filtered.some((o) => o.id === selected)} onClick={() => setNotice('Sample selection saved in this page only.')}>Continue <span>→</span></button></div>{notice && <p className="status" role="status">{notice}</p>}
    </section><footer>Placement Studio <span>·</span> Interview practice prototype <span>·</span> No real offer or shipment</footer>
  </main>;
}
