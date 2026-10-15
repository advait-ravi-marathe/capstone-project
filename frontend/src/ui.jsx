import React, { useEffect, useMemo, useRef, useState } from 'react';
import * as exercises from './assignments.js';
import fixtures from '../../fixtures/cases.json';
import manifest from '../../SPRINT-MANIFEST.json';

const days = manifest.assignments.filter((item) => item.role === 'frontend');
const demoInventory = [
  { id: 'ST-14', name: 'Northstar Storage', type: 'Standard', units: 240, feeCents: 124000, days: 2 },
  { id: 'ST-28', name: 'Harbor Fulfillment', type: 'Oversize', units: 120, feeCents: 98000, days: 3 },
  { id: 'ST-36', name: 'Juniper Network', type: 'Standard', units: 300, feeCents: 156000, days: 1 },
];
const defaultApi = {
  submit: async (payload) => ({ accepted: true, name: payload.name }),
  search: async (query, { signal } = {}) => {
    if (signal?.aborted) throw new DOMException('Aborted', 'AbortError');
    return demoInventory.filter((x) => x.name.toLowerCase().includes(query.toLowerCase()));
  },
  quote: async (item) => ({ feeCents: item.feeCents, version: 'sample-v1' }),
  placements: async (query, { signal } = {}) => {
    if (signal?.aborted) throw new DOMException('Aborted', 'AbortError');
    return demoInventory.filter((x) => `${x.name} ${x.type}`.toLowerCase().includes(query.toLowerCase()));
  },
  poll: async () => ({ state: 'ready' }),
};

function LearnerRunner({ day, payload }) {
  const [result, setResult] = useState('');
  const [busy, setBusy] = useState(false);
  const run = async () => {
    setBusy(true); setResult('Running learner function…');
    try {
      const value = await Promise.resolve(exercises[`day${String(day).padStart(2, '0')}`](payload));
      setResult(JSON.stringify(value));
    } catch (error) {
      setResult(error?.message || 'Learner function failed');
    } finally { setBusy(false); }
  };
  return <section className="learner-runner" aria-label="Learner implementation">
    <button type="button" onClick={run} disabled={busy}>{busy ? 'Running…' : 'Run my implementation'}</button>
    {result && <p role="status">{result}</p>}
  </section>;
}

function RegistrationTask({ day }) {
  const [name, setName] = useState(''); const [email, setEmail] = useState('');
  const [errors, setErrors] = useState({}); const [status, setStatus] = useState('');
  const nameRef = useRef(null); const emailRef = useRef(null);
  const submit = (event) => {
    event.preventDefault(); setStatus('');
    try {
      const next = exercises.day01({ name, email }); setErrors(next);
      const first = next.name ? nameRef.current : next.email ? emailRef.current : null;
      if (first) first.focus(); else setStatus('Registration details accepted for this practice form.');
    } catch (e) { setStatus(e.message); }
  };
  return <form className="task-form" onSubmit={submit} noValidate>
    <label htmlFor="seller-name">Seller name</label><input id="seller-name" ref={nameRef} value={name} onChange={(e) => setName(e.target.value)} aria-invalid={Boolean(errors.name)} aria-describedby={errors.name ? 'name-error' : undefined}/>
    {errors.name && <small id="name-error">{errors.name}</small>}
    <label htmlFor="seller-email">Email address</label><input id="seller-email" ref={emailRef} type="email" value={email} onChange={(e) => setEmail(e.target.value)} aria-invalid={Boolean(errors.email)} aria-describedby={errors.email ? 'email-error' : undefined}/>
    {errors.email && <small id="email-error">{errors.email}</small>}
    <button type="submit">Validate registration</button>{status && <p role="status">{status}</p>}
    <LearnerRunner day={day} payload={{ name, email }}/>
  </form>;
}
function InventoryFilterTask({ day }) {
  const [query, setQuery] = useState(''); const [minimum, setMinimum] = useState('');
  const [maximum, setMaximum] = useState('');
  const items = useMemo(() => demoInventory.filter((x) => x.name.toLowerCase().includes(query.trim().toLowerCase()) && x.units >= (minimum === '' ? -Infinity : Number(minimum)) && x.units <= (maximum === '' ? Infinity : Number(maximum))), [query, minimum, maximum]);
  const payload = { items: demoInventory.map(({name, units: qty}) => ({name, qty})), query, min: minimum === '' ? 0 : Number(minimum), max: maximum === '' ? 1000 : Number(maximum) };
  return <div className="task-form"><label htmlFor="inventory-query">Search inventory</label><input id="inventory-query" value={query} onChange={(e) => setQuery(e.target.value)} />
    <label htmlFor="minimum">Minimum units</label><input id="minimum" inputMode="numeric" value={minimum} onChange={(e) => setMinimum(e.target.value)} />
    <label htmlFor="maximum">Maximum units</label><input id="maximum" inputMode="numeric" value={maximum} onChange={(e) => setMaximum(e.target.value)} />
    <ul aria-label="Filtered placements">{items.map((x) => <li key={x.id}>{x.name} · {x.units} units</li>)}</ul>{items.length === 0 && <p role="status">No inventory matches.</p>}
    <LearnerRunner day={day} payload={payload}/></div>;
}
function AsyncSubmissionTask({ day, api }) {
  const [name, setName] = useState(''); const [pending, setPending] = useState(false); const [status, setStatus] = useState('');
  const submit = async (event) => { event.preventDefault(); if (pending) return; if (!name.trim()) { setStatus('Enter your name'); return; }
    setPending(true); setStatus('Submitting…'); try { await api.submit({ name: name.trim() }); setStatus('Submitted'); } catch { setStatus('Could not submit. Try again.'); } finally { setPending(false); }
  };
  return <form className="task-form" onSubmit={submit}><label htmlFor="async-name">Seller name</label><input id="async-name" value={name} onChange={(e) => setName(e.target.value)}/><button disabled={pending}>{pending ? 'Submitting…' : 'Submit'}</button><p role="status">{status}</p><LearnerRunner day={day} payload={{events:['SUBMIT']}}/></form>;
}
function AutocompleteTask({ day, api }) {
  const [query,setQuery]=useState(''); const [items,setItems]=useState([]); const [loading,setLoading]=useState(false); const [active,setActive]=useState(-1); const controller=useRef(null);
  useEffect(() => { if (!query.trim()) { setItems([]); return; } let alive=true; const timer=setTimeout(async()=>{ controller.current?.abort(); const c=new AbortController(); controller.current=c; setLoading(true); try { const found=await api.search(query,{signal:c.signal}); if(alive && !c.signal.aborted) setItems(found); } catch(e) { if(e.name!=='AbortError' && alive) setItems([]); } finally { if(alive) setLoading(false); } },180); return ()=>{alive=false;clearTimeout(timer);controller.current?.abort();}; },[query,api]);
  const onKey=(e)=>{ if(e.key==='ArrowDown'){e.preventDefault();setActive((v)=>Math.min(v+1,items.length-1));} if(e.key==='ArrowUp'){e.preventDefault();setActive((v)=>Math.max(v-1,0));} if(e.key==='Escape'){setQuery('');setItems([]);} if(e.key==='Enter'&&active>=0){e.preventDefault();setQuery(items[active]?.name||query);} };
  return <div className="task-form"><label htmlFor="autocomplete">Find a placement</label><input id="autocomplete" role="combobox" aria-expanded={items.length>0} aria-controls="placement-options" aria-autocomplete="list" value={query} onChange={(e)=>{setQuery(e.target.value);setActive(-1);}} onKeyDown={onKey}/>{loading&&<p role="status">Searching…</p>}<ul id="placement-options" role="listbox">{items.map((x,i)=><li role="option" aria-selected={active===i} key={x.id}>{x.name}</li>)}</ul><LearnerRunner day={day} payload={{query,options:demoInventory.map((x)=>x.name)}}/></div>;
}
function QuoteTask({ day, api }) {
  const [quote,setQuote]=useState(null); const [state,setState]=useState('idle'); const [error,setError]=useState('');
  const load=async()=>{setState('loading');setError('');try{const x=await api.quote(demoInventory[0]);setQuote(x);setState('ready');}catch{setState('error');setError('Quote unavailable. Retry.');}};
  useEffect(()=>{let active=true;api.quote(demoInventory[0]).then((x)=>{if(active){setQuote(x);setState('ready');}}).catch(()=>{if(active)setState('error');});return()=>{active=false;};},[api]);
  return <div className="task-form"><button onClick={load} disabled={state==='loading'}>Refresh quote</button><p role="status">{state==='loading'?'Loading quote…':error|| (quote?`Fee: ₹${(quote.feeCents/100).toFixed(2)} · ${quote.version}`:'Waiting for quote')}</p><LearnerRunner day={day} payload={{items:[{amountCents:124000}],feeCents:quote?.feeCents||0}}/></div>;
}
function StaleResponseTask({ day, api }) {
 const [query,setQuery]=useState('');const [results,setResults]=useState([]);const [state,setState]=useState('idle');const seq=useRef(0);
 useEffect(()=>{const current=++seq.current;const c=new AbortController();setState(query?'loading':'idle');api.placements(query,{signal:c.signal}).then((data)=>{if(current===seq.current){setResults(data);setState('ready');}}).catch((e)=>{if(e.name!=='AbortError'&&current===seq.current)setState('error');});return()=>c.abort();},[query,api]);
 return <div className="task-form"><label htmlFor="placement-query">Filter placements</label><input id="placement-query" value={query} onChange={(e)=>setQuery(e.target.value)}/><p role="status">{state==='loading'?'Loading placements…':state==='error'?'Could not load placements.':'Current results'}</p><ul>{results.map((x)=><li key={x.id}>{x.name}</li>)}</ul><LearnerRunner day={day} payload={{requestSequence:seq.current,results}}/></div>;
}
function ResilientQuoteTask({ day, api }) {
 const [attempt,setAttempt]=useState(0);const [state,setState]=useState('loading');const [data,setData]=useState(null);
 useEffect(()=>{let active=true;setState('loading');api.quote(demoInventory[0]).then((x)=>{if(active){setData(x);setState('ready');}}).catch(()=>{if(active)setState('retryable');});return()=>{active=false;};},[api,attempt]);
 return <div className="task-form"><p role="status">{state==='loading'?'Loading quote…':state==='ready'?`Ready · ${data?.version}`:'Quote temporarily unavailable'}</p>{state==='retryable'&&<button onClick={()=>setAttempt((n)=>n+1)}>Retry</button>}{data&&<p>Last successful fee: ₹{(data.feeCents/100).toFixed(2)}</p>}<LearnerRunner day={day} payload={{state,attempt}}/></div>;
}
function PollingTask({ day, api }) {
 const [count,setCount]=useState(0);const [state,setState]=useState('idle');const [enabled,setEnabled]=useState(false);const [attempt,setAttempt]=useState(0);
 useEffect(()=>{if(!enabled)return;let live=true;let timer;const poll=async()=>{if(attempt>=4){setState('max attempts reached');setEnabled(false);return;}try{const result=await api.poll();if(!live)return;setCount((n)=>n+1);setState(result.state);if(result.state==='ready'){setEnabled(false);return;}}catch{if(live)setState('retrying');} if(live){setAttempt((n)=>n+1);timer=setTimeout(poll,Math.min(800,100*2**attempt));}};poll();return()=>{live=false;clearTimeout(timer);};},[enabled,attempt,api]);
 return <div className="task-form"><button onClick={()=>{setAttempt(0);setEnabled(true);setState('polling');}} disabled={enabled}>Start bounded polling</button><button onClick={()=>setEnabled(false)} disabled={!enabled}>Cancel polling</button><p role="status">{state} · attempts {count}</p><LearnerRunner day={day} payload={{events:['POLL','SUCCESS']}}/></div>;
}
function TabsTask({ day }) {
 const [selected,setSelected]=useState(0);const refs=useRef([]);const labels=['Options','Fees','Delivery'];const move=(index)=>{setSelected(index);refs.current[index]?.focus();};
 return <div className="task-form"><div role="tablist" aria-label="Placement details" onKeyDown={(e)=>{let i=selected;if(e.key==='ArrowRight')i=(i+1)%3;else if(e.key==='ArrowLeft')i=(i+2)%3;else if(e.key==='Home')i=0;else if(e.key==='End')i=2;else return;e.preventDefault();move(i);}}>{labels.map((label,i)=><button key={label} ref={(el)=>refs.current[i]=el} role="tab" id={`tab-${i}`} aria-controls={`panel-${i}`} aria-selected={selected===i} tabIndex={selected===i?0:-1} onClick={()=>setSelected(i)}>{label}</button>)}</div>{labels.map((label,i)=><section key={label} role="tabpanel" id={`panel-${i}`} aria-labelledby={`tab-${i}`} hidden={selected!==i}>{label} details for synthetic placement options.</section>)}<LearnerRunner day={day} payload={{selected,labels}}/></div>;
}
function GenericTask({ day, item }) {
 const fixture=fixtures[`frontend_${String(day).padStart(2,'0')}`].cases[0];const [payload,setPayload]=useState(JSON.stringify(fixture.input,null,2));const [parseError,setParseError]=useState('');
 let parsed;try{parsed=JSON.parse(payload);}catch{parsed=fixture.input;}
 return <div className="task-form"><label htmlFor="task-input">Synthetic case input (edit before running)</label><textarea id="task-input" rows="8" value={payload} onChange={(e)=>{setPayload(e.target.value);setParseError('');}}/><button onClick={()=>{try{JSON.parse(payload);setParseError('Input is valid JSON.');}catch{setParseError('Fix the JSON syntax before running.');}}}>Check case input</button>{parseError&&<p role="status">{parseError}</p>}<LearnerRunner day={day} payload={parsed}/></div>;
}
function TaskBody({ day, item, api }) {
 if(day===1)return <RegistrationTask day={day}/>;
 if(day===2)return <InventoryFilterTask day={day}/>;
 if(day===3)return <AsyncSubmissionTask day={day} api={api}/>;
 if(day===11)return <AutocompleteTask day={day} api={api}/>;
 if(day===12)return <QuoteTask day={day} api={api}/>;
 if(day===17)return <StaleResponseTask day={day} api={api}/>;
 if(day===18)return <ResilientQuoteTask day={day} api={api}/>;
 if(day===23)return <PollingTask day={day} api={api}/>;
 if(day===28)return <TabsTask day={day}/>;
 return <GenericTask day={day} item={item}/>;
}
export function AssignmentApp({ api = defaultApi, initialDay = 1 }) {
 const [day,setDay]=useState(initialDay); const item=days[day-1];
 return <main className="shell">
   <header className="top"><a className="brand" href="#top">Amazon interview sprint</a><span className="demo-pill">Practice sandbox · synthetic data</span></header>
   <section className="intro"><p className="eyebrow">FRONT-END ENGINEER II · JOB 10575321</p><h1>Daily component practice</h1><p className="lede">One 60-minute practice contract per day. Implement the learner function, then exercise it here.</p></section>
   <nav className="day-nav" aria-label="Choose assignment day">{days.map((x)=><button key={x.day} type="button" aria-current={day===x.day?'page':undefined} onClick={()=>setDay(x.day)}>Day {String(x.day).padStart(2,'0')}</button>)}</nav>
   <section className="workspace" aria-labelledby="assignment-title"><div className="section-head"><div><p className="eyebrow">DAY {String(day).padStart(2,'0')} · {item.topic}</p><h2 id="assignment-title">{item.title}</h2></div><label className="day-picker">Go to day<select aria-label="Go to day" value={day} onChange={(e)=>setDay(Number(e.target.value))}>{days.map((x)=><option key={x.day} value={x.day}>Day {String(x.day).padStart(2,'0')}</option>)}</select></label></div>
   <p className="task-prompt">{item.coding_prompt}</p><TaskBody key={day} day={day} item={item} api={api}/>
   <aside className="review"><h3>15 min · design / review</h3><p>{item.design_prompt}</p><h3>10 min · {item.leadership_principle}</h3><p>Use one truthful STAR example; record your action, evidence, and learning. Do not invent metrics.</p></aside></section>
   <footer>Study plan only · no real seller or placement data</footer>
 </main>;
}
export default AssignmentApp;
