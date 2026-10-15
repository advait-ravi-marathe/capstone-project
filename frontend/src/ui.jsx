import React, { useCallback, useEffect, useRef, useState } from 'react';
import * as defaultExercises from './assignments.js';
import fixtures from '../../fixtures/cases.json';
import manifest from '../../SPRINT-MANIFEST.json';

const days = manifest.assignments.filter((item) => item.role === 'frontend');
const offers = [
  { id: 'ST-14', name: 'Northstar Storage', type: 'Standard', units: 240, feeCents: 124000, days: 2 },
  { id: 'ST-28', name: 'Harbor Fulfillment', type: 'Oversize', units: 120, feeCents: 98000, days: 3 },
  { id: 'ST-36', name: 'Juniper Network', type: 'Standard', units: 300, feeCents: 156000, days: 1 },
];
const defaultApi = {
  submit: async (payload) => ({ accepted: true, name: payload.name }),
  search: async (query, { signal } = {}) => {
    if (signal?.aborted) throw new DOMException('Aborted', 'AbortError');
    return offers.filter((x) => x.name.toLowerCase().includes(query.toLowerCase()));
  },
  quote: async (item) => ({ feeCents: item.feeCents, version: 'sample-v1', status: 200 }),
  placements: async (query, { signal } = {}) => {
    if (signal?.aborted) throw new DOMException('Aborted', 'AbortError');
    return offers.filter((x) => `${x.name} ${x.type}`.toLowerCase().includes(query.toLowerCase()));
  },
  poll: async () => ({ state: 'ready' }),
};

function useLearner(day, exerciseFunctions) {
  const [result, setResult] = useState(undefined);
  const [error, setError] = useState('');
  const run = useCallback(async (payload) => {
    setError('');
    try {
      const value = await exerciseFunctions[`day${String(day).padStart(2, '0')}`](payload);
      setResult(value);
      return value;
    } catch (e) {
      setResult(undefined);
      setError(e?.message || 'Learner function failed');
      return undefined;
    }
  }, [day, exerciseFunctions]);
  const reset = useCallback(() => { setResult(undefined); setError(''); }, []);
  return { result, error, run, reset };
}
function Result({ result, error }) {
  if (error) return <p role="status">{error}</p>;
  if (result === undefined) return null;
  return <p role="status">{typeof result === 'string' ? result : JSON.stringify(result)}</p>;
}
function TaskPanel({ day, title, children, payload, exerciseFunctions, resultView }) {
  const learner = useLearner(day, exerciseFunctions);
  return <div className="task-form" data-assignment-day={day}>
    {title && <h3>{title}</h3>}
    {children({ ...learner, payload })}
    {resultView && resultView(learner.result, learner.error)}
    {!resultView && <Result result={learner.result} error={learner.error}/>}
  </div>;
}

function RegistrationTask({ day, exerciseFunctions }) {
  const [name,setName]=useState(''); const [email,setEmail]=useState('');
  const [errors,setErrors]=useState({}); const [message,setMessage]=useState('');
  const nameRef=useRef(null);const emailRef=useRef(null);
  return <TaskPanel day={day} title="Seller registration" payload={{name,email}} exerciseFunctions={exerciseFunctions} resultView={(_,error)=><>{error&&<p role="status">{error}</p>}{message&&<p role="status">{message}</p>}</>}>
    {({run,payload})=><form onSubmit={async(e)=>{e.preventDefault();setMessage('');const next=await run(payload);if(!next)return;setErrors(next);const first=next.name?nameRef.current:next.email?emailRef.current:null;if(first)first.focus();else setMessage('Registration details accepted for this practice form.');}} noValidate>
      <label htmlFor="seller-name">Seller name</label><input id="seller-name" ref={nameRef} value={name} onChange={(e)=>setName(e.target.value)} aria-invalid={Boolean(errors.name)} aria-describedby={errors.name?'name-error':undefined}/>{errors.name&&<small id="name-error">{errors.name}</small>}
      <label htmlFor="seller-email">Email address</label><input id="seller-email" ref={emailRef} type="email" value={email} onChange={(e)=>setEmail(e.target.value)} aria-invalid={Boolean(errors.email)} aria-describedby={errors.email?'email-error':undefined}/>{errors.email&&<small id="email-error">{errors.email}</small>}
      <button type="submit">Validate registration</button></form>}
  </TaskPanel>;
}
function InventoryFilterTask({ day, exerciseFunctions }) {
  const [query,setQuery]=useState('');const [minimum,setMinimum]=useState('0');const [maximum,setMaximum]=useState('1000');
  const [filtered,setFiltered]=useState(null);
  const payload={items:offers.map(({name,units:qty})=>({name,qty})),query,min:Number(minimum),max:Number(maximum)};
  return <TaskPanel day={day} title="Inventory filters" payload={payload} exerciseFunctions={exerciseFunctions} resultView={(value,error)=><>{error&&<p role="status">{error}</p>}<ul aria-label="Filtered inventory">{(filtered??offers.map(({name,units})=>({name,qty:units}))).map((x,i)=><li key={`${x.name}-${i}`}>{x.name} · {x.qty} units</li>)}</ul>{filtered?.length===0&&<p role="status">No inventory matches.</p>}</>}>
    {({run})=><><label htmlFor="inventory-query">Search inventory</label><input id="inventory-query" value={query} onChange={(e)=>setQuery(e.target.value)}/><label htmlFor="minimum">Minimum units</label><input id="minimum" inputMode="numeric" value={minimum} onChange={(e)=>setMinimum(e.target.value)}/><label htmlFor="maximum">Maximum units</label><input id="maximum" inputMode="numeric" value={maximum} onChange={(e)=>setMaximum(e.target.value)}/><button onClick={async()=>setFiltered(await run(payload))}>Apply learner filter</button></>}
  </TaskPanel>;
}
function AsyncSubmissionTask({ day, api, exerciseFunctions }) {
  const [name,setName]=useState('');const [pending,setPending]=useState(false);const [state,setState]=useState('idle');
  const learner=useLearner(day,exerciseFunctions);const events=useRef([]);
  const advance=async(event)=>{events.current=[...events.current,event];return learner.run({events:events.current});};
  const submit=async(e)=>{e.preventDefault();if(pending)return;if(!name.trim()){const value=await advance('VALIDATION_ERROR');if(value!==undefined)setState(value);return;}setPending(true);const submitting=await advance('SUBMIT');if(submitting!==undefined)setState(submitting);try{await api.submit({name:name.trim()});const next=await advance('RESOLVE');if(next!==undefined)setState(next);}catch{const next=await advance('REJECT');if(next!==undefined)setState(next);}finally{setPending(false);}};
  return <form className="task-form" data-assignment-day={day} onSubmit={submit}><label htmlFor="async-name">Seller name</label><input id="async-name" value={name} onChange={(e)=>setName(e.target.value)}/><button disabled={pending}>{pending?'Submitting…':state==='retryable'?'Retry submission':'Submit'}</button><p role="status">{pending?'Submitting…':state}</p><Result result={learner.result} error={learner.error}/></form>;
}
function PriceBandTask({ day, exerciseFunctions }) {
 const fixture=fixtures.frontend_04.cases[0].input;const [price,setPrice]=useState(fixture.price);const [selected,setSelected]=useState(null);const bands=fixture.bands;
 return <TaskPanel day={day} title="Price-band selector" payload={{bands,price}} exerciseFunctions={exerciseFunctions} resultView={(value,error)=><>{error&&<p role="status">{error}</p>}{value!==undefined&&<p role="status">{value<bands.length?`Selected band: ${bands[value]}`:'No band covers this price.'}</p>}</>}>
 {({run})=><><label htmlFor="price">Placement price</label><input id="price" type="number" value={price} onChange={(e)=>{setPrice(Number(e.target.value));setSelected(null);}}/><div role="group" aria-label="Price bands">{bands.map((b,i)=><button key={b} type="button" aria-pressed={selected===i} onClick={()=>setSelected(i)}>{b}</button>)}</div><button onClick={()=>run({bands,price})}>Find price band</button></>}
 </TaskPanel>;
}
function IdentityRowsTask({ day, exerciseFunctions }) {
 const items=fixtures.frontend_05.cases[0].input.items;const [selected,setSelected]=useState('');const [keys,setKeys]=useState(null);const [reversed,setReversed]=useState(false);const learner=useLearner(day,exerciseFunctions);
 const loadKeys=async()=>{const result=await learner.run({items});if(result)setKeys(result);};const rows=reversed?[...items].reverse():items;
 return <div className="task-form" data-assignment-day={day}><h3>Inventory row identity</h3><p>Choose a row, reorder the sample, and check that your stable key keeps the selection attached.</p><button onClick={loadKeys}>Resolve learner row keys</button><button onClick={()=>setReversed(v=>!v)}>Reorder rows</button><Result result={learner.result} error={learner.error}/><ul>{rows.map((item,i)=>{const originalIndex=items.indexOf(item);const rowKey=keys?.[originalIndex]??`unresolved-${originalIndex}`;return <li key={rowKey}><button aria-pressed={selected===String(rowKey)} onClick={()=>setSelected(String(rowKey))}>{item.id||item.sku||'Missing identity'}</button></li>;})}</ul></div>;
}
function TreeTask({ day, exerciseFunctions }) {
 const tree=fixtures.frontend_06.cases[0].input.tree;const learner=useLearner(day,exerciseFunctions);const [expanded,setExpanded]=useState({A:true});
 const node=(item)=> <li key={item.label}><button disabled={item.disabled} aria-expanded={item.children?.length?Boolean(expanded[item.label]):undefined} onClick={()=>setExpanded(v=>({...v,[item.label]:!v[item.label]}))}>{item.label}</button>{item.children?.length&&expanded[item.label]?<ul>{item.children.map(node)}</ul>:null}</li>;
 return <div className="task-form" data-assignment-day={day}><h3>Fulfillment tree</h3><ul aria-label="Fulfillment choices">{tree.map(node)}</ul><button onClick={()=>learner.run({tree})}>Check learner traversal</button><Result result={learner.result} error={learner.error}/></div>;
}
function SortableTask({ day, exerciseFunctions }) {
 const [direction,setDirection]=useState('asc');const input=fixtures.frontend_07.cases[0].input;const learner=useLearner(day,exerciseFunctions);
 const byId=new Map(input.items.map(x=>[x.id,x]));const ordered=Array.isArray(learner.result)?learner.result.map(id=>byId.get(id)).filter(Boolean):input.items;
 return <div className="task-form" data-assignment-day={day}><h3>Inventory table</h3><button onClick={()=>setDirection(x=>x==='asc'?'desc':'asc')}>Sort quantity {direction==='asc'?'ascending':'descending'}</button><table><thead><tr><th>ID</th><th>Quantity</th></tr></thead><tbody>{ordered.map(x=><tr key={x.id}><td>{x.id}</td><td>{x.qty}</td></tr>)}</tbody></table><button onClick={()=>learner.run({...input,direction})}>Apply learner ordering</button><Result result={learner.result} error={learner.error}/></div>;
}
function TimelineTask({ day, exerciseFunctions }) {
 const input=fixtures.frontend_08.cases[0].input;const learner=useLearner(day,exerciseFunctions);const intervals=Array.isArray(learner.result)?learner.result:input.windows;
 return <div className="task-form" data-assignment-day={day}><h3>Availability timeline</h3><ol aria-label="Availability intervals">{intervals.map(([start,end],i)=><li key={i}>From {start} to {end}</li>)}</ol><button onClick={()=>learner.run(input)}>Merge intervals</button><Result result={learner.result} error={learner.error}/></div>;
}
function WizardTask({ day, exerciseFunctions }) {
 const input=fixtures.frontend_09.cases[0].input;const learner=useLearner(day,exerciseFunctions);const [complete,setComplete]=useState([]);const order=Array.isArray(learner.result)?learner.result:input.steps;
 return <div className="task-form" data-assignment-day={day}><h3>Onboarding steps</h3><button onClick={()=>learner.run(input)}>Resolve dependency order</button><Result result={learner.result} error={learner.error}/><ol>{order.map(step=><li key={step}><label><input type="checkbox" checked={complete.includes(step)} onChange={()=>setComplete(v=>v.includes(step)?v.filter(x=>x!==step):[...v,step])}/>{step}</label></li>)}</ol><p>Completed: {complete.join(', ')||'none'}</p></div>;
}
function FacetTask({ day, exerciseFunctions }) {
 const input=fixtures.frontend_10.cases[0].input;const learner=useLearner(day,exerciseFunctions);const [target,setTarget]=useState(input.target);const [selected,setSelected]=useState([]);
 const payload={values:selected.length?selected.map(i=>input.values[i]):input.values,target};
 return <div className="task-form" data-assignment-day={day}><h3>Inventory facets</h3><label htmlFor="facet-target">Target total</label><input id="facet-target" type="number" value={target} onChange={e=>setTarget(Number(e.target.value))}/><div role="group" aria-label="Facet values">{input.values.map((v,i)=><label key={i}><input type="checkbox" checked={selected.includes(i)} onChange={()=>setSelected(s=>s.includes(i)?s.filter(x=>x!==i):[...s,i])}/>{v}</label>)}</div><button onClick={()=>learner.run(payload)}>Check selected combinations</button><button onClick={()=>setSelected([])}>Clear all</button><Result result={learner.result} error={learner.error}/></div>;
}
function AutocompleteTask({ day, api, exerciseFunctions }) {
 const [query,setQuery]=useState('');const [items,setItems]=useState([]);const [loading,setLoading]=useState(false);const [active,setActive]=useState(-1);const [status,setStatus]=useState('');const controller=useRef(null);const learner=useLearner(day,exerciseFunctions);
 useEffect(()=>{if(!query.trim()){setItems([]);return undefined;}let alive=true;const timer=setTimeout(async()=>{controller.current?.abort();const c=new AbortController();controller.current=c;setLoading(true);try{const found=await api.search(query,{signal:c.signal});if(alive&&!c.signal.aborted){const names=found.map(x=>typeof x==='string'?x:x.name);const matches=await learner.run({query,options:names});if(matches)setItems(matches);else setStatus(learner.error||'');}}catch(e){if(e.name!=='AbortError'&&alive)setStatus('Search unavailable.');}finally{if(alive)setLoading(false);}},160);return()=>{alive=false;clearTimeout(timer);controller.current?.abort();};},[query,api,exerciseFunctions]);
 const keydown=e=>{if(e.key==='ArrowDown'){e.preventDefault();setActive(v=>Math.min(v+1,items.length-1));}if(e.key==='ArrowUp'){e.preventDefault();setActive(v=>Math.max(v-1,0));}if(e.key==='Escape'){setQuery('');setItems([]);}if(e.key==='Enter'&&active>=0){e.preventDefault();setQuery(items[active]||query);}};
 return <div className="task-form" data-assignment-day={day}><label htmlFor="autocomplete">Find a placement</label><input id="autocomplete" role="combobox" aria-expanded={items.length>0} aria-controls="placement-options" aria-autocomplete="list" value={query} onChange={e=>{setQuery(e.target.value);setActive(-1);}} onKeyDown={keydown}/>{loading&&<p role="status">Searching…</p>}{status&&<p role="status">{status}</p>}<ul id="placement-options" role="listbox">{items.map((x,i)=><li role="option" aria-selected={active===i} key={x}>{x}</li>)}</ul><Result result={learner.result} error={learner.error}/></div>;
}
function QuoteTask({ day, api, exerciseFunctions }) {
 const [quote,setQuote]=useState(null);const [state,setState]=useState('idle');const learner=useLearner(day,exerciseFunctions);
 const load=async()=>{setState('loading');try{const q=await api.quote(offers[0]);setQuote(q);const value=await learner.run({rows:[{cents:offers[0].feeCents}],feeCents:q.feeCents});if(value)setState('ready');else setState('error');}catch{setState('error');}};
 useEffect(()=>{load();},[api.quote]);
 return <div className="task-form" data-assignment-day={day}><h3>Placement cost</h3><button onClick={load} disabled={state==='loading'}>Refresh quote</button><p role="status">{state==='loading'?'Loading quote…':state==='error'?'Quote or learner calculation unavailable.':state==='ready'?`Subtotal ₹${(learner.result.subtotalCents/100).toFixed(2)} · fee ₹${(quote.feeCents/100).toFixed(2)} · total ₹${(learner.result.totalCents/100).toFixed(2)}`:'Waiting for quote'}</p><Result result={learner.result} error={learner.error}/></div>;
}
function LazyListTask({ day, exerciseFunctions }) {
 const cards=fixtures.frontend_13.cases[0].input.cards;const learner=useLearner(day,exerciseFunctions);const [shown,setShown]=useState(1);
 const byId=new Map(cards.map(c=>[c.id,c]));const ordered=Array.isArray(learner.result)?learner.result.map(id=>byId.get(id)).filter(Boolean):cards;
 return <div className="task-form" data-assignment-day={day}><h3>Placement options</h3><ul>{ordered.slice(0,shown).map(c=><li key={c.id}>{c.id} · priority {c.priority}</li>)}</ul><button onClick={()=>setShown(n=>Math.min(n+1,cards.length))}>Load next page</button><button onClick={()=>learner.run({cards})}>Order with learner priority</button><Result result={learner.result} error={learner.error}/></div>;
}
function MarketplaceTask({ day, exerciseFunctions }) {
 const input=fixtures.frontend_14.cases[0].input;const learner=useLearner(day,exerciseFunctions);const [selected,setSelected]=useState([]);
 return <div className="task-form" data-assignment-day={day}><h3>Marketplace groups</h3><div role="group" aria-label="Marketplaces">{input.ids.map(id=><button key={id} aria-pressed={selected.includes(id)} onClick={()=>setSelected(v=>v.includes(id)?v.filter(x=>x!==id):[...v,id])}>{id}</button>)}</div><button onClick={()=>learner.run(input)}>Group connected marketplaces</button><ul>{(Array.isArray(learner.result)?learner.result:[]).map((group,i)=><li key={i}>{group.join(' · ')}</li>)}</ul><Result error={learner.error}/></div>;
}
function UrlFilterTask({ day, exerciseFunctions }) {
 const input=fixtures.frontend_15.cases[0].input;const [market,setMarket]=useState(input.market);const [filters,setFilters]=useState(input.filters);const learner=useLearner(day,exerciseFunctions);
 const apply=async()=>{const key=await learner.run({market,filters});if(key){const params=new URLSearchParams(window.location.search);params.set('market',key[1]);params.set('filters',key[2].join(','));window.history.replaceState({},'',`${window.location.pathname}?${params.toString()}`);}};
 return <div className="task-form" data-assignment-day={day}><h3>Placement filters</h3><label htmlFor="market">Market</label><input id="market" value={market} onChange={e=>setMarket(e.target.value)}/><label htmlFor="filters">Filters (comma separated)</label><input id="filters" value={filters.join(',')} onChange={e=>setFilters(e.target.value.split(','))}/><button onClick={apply}>Sync filters to URL</button><Result result={learner.result} error={learner.error}/></div>;
}
function CacheTask({ day, exerciseFunctions }) {
 const input=fixtures.frontend_16.cases[0].input;const [version,setVersion]=useState(input.version);const learner=useLearner(day,exerciseFunctions);
 return <div className="task-form" data-assignment-day={day}><h3>Policy version cache</h3><label htmlFor="policy-version">Current policy version</label><input id="policy-version" type="number" value={version} onChange={e=>setVersion(Number(e.target.value))}/><button onClick={()=>learner.run({...input,version})}>Check cached result</button><p role="status">{learner.result===true?'Cached policy is current.':learner.result===false?'Cached policy is stale.':''}</p><Result error={learner.error}/></div>;
}
function StaleResponseTask({ day, api, exerciseFunctions }) {
 const [query,setQuery]=useState('');const [results,setResults]=useState([]);const [loading,setLoading]=useState(false);const seq=useRef(0);const responses=useRef([]);const controller=useRef(null);const learner=useLearner(day,exerciseFunctions);
 useEffect(()=>{const request=++seq.current;const c=new AbortController();controller.current?.abort();controller.current=c;setLoading(true);api.placements(query,{signal:c.signal}).then(async value=>{responses.current=[...responses.current,{seq:request,value}];const selected=await learner.run({responses:responses.current});if(selected!==undefined)setResults(Array.isArray(selected)?selected:[{id:String(selected),name:String(selected)}]);}).catch(e=>{if(e.name!=='AbortError')learner.run({responses:responses.current});}).finally(()=>setLoading(false));return()=>c.abort();},[query,api,exerciseFunctions]);
 return <div className="task-form" data-assignment-day={day}><label htmlFor="placement-query">Filter placements</label><input id="placement-query" value={query} onChange={e=>setQuery(e.target.value)}/><p role="status">{loading?'Loading placements…':'Placement results'}</p><ul>{results.map((x,i)=><li key={x.id||i}>{x.name||x}</li>)}</ul><Result result={learner.result} error={learner.error}/></div>;
}
function ResilientQuoteTask({ day, api, exerciseFunctions }) {
 const [attempt,setAttempt]=useState(0);const [quote,setQuote]=useState(null);const [state,setState]=useState('loading');const learner=useLearner(day,exerciseFunctions);
 const load=async()=>{setState('loading');try{const q=await api.quote(offers[0]);setQuote(q);const statuses=await learner.run({statuses:['loading',q.status||200]});if(statuses)setState(statuses.at(-1));else setState('error');}catch(e){const statuses=await learner.run({statuses:['loading',e.status||503]});if(statuses)setState(statuses.at(-1));else setState('error');}};
 useEffect(()=>{load();},[attempt]);
 return <div className="task-form" data-assignment-day={day}><p role="status">{state==='loading'?'Loading quote…':state==='ready'?`Ready · ${quote?.version}`:state==='retryable'?'Quote temporarily unavailable':state==='error'?'Quote could not be loaded.':'Quote unavailable'}</p>{quote&&<p>Last successful fee: ₹{(quote.feeCents/100).toFixed(2)}</p>}{state==='retryable'&&<button onClick={()=>setAttempt(x=>x+1)}>Retry</button>}<Result result={learner.result} error={learner.error}/></div>;
}
function PollingTask({ day, api, exerciseFunctions }) {
 const [state,setState]=useState('idle');const [count,setCount]=useState(0);const [active,setActive]=useState(false);const timer=useRef(null);const cycle=useRef(0);const learner=useLearner(day,exerciseFunctions);
 const cancel=()=>{cycle.current++;if(timer.current)clearTimeout(timer.current);timer.current=null;setActive(false);};
 useEffect(()=>()=>{if(timer.current)clearTimeout(timer.current);cycle.current++;},[]);
 const start=async()=>{cancel();setCount(0);const delays=await learner.run({attempts:4,base:100,cap:800});if(!Array.isArray(delays))return;const id=++cycle.current;setActive(true);setState('polling');let index=0;
   const schedule=()=>{if(cycle.current!==id)return;if(index>=delays.length){setState('max attempts reached');setActive(false);return;}const delay=delays[index];timer.current=setTimeout(async()=>{if(cycle.current!==id)return;setCount(index+1);try{const result=await api.poll();if(cycle.current!==id)return;setState(result.state);if(result.state==='ready'){setActive(false);return;}}catch{if(cycle.current!==id)return;setState('retrying');}index++;schedule();},delay);};schedule();};
 return <div className="task-form" data-assignment-day={day}><button onClick={start} disabled={active}>Start bounded polling</button><button onClick={cancel} disabled={!active}>Cancel polling</button><p role="status">{state} · attempts {count}</p><Result result={learner.result} error={learner.error}/></div>;
}
function PayloadTask({ day, exerciseFunctions }) {
 const input=fixtures.frontend_19.cases[0].input;const learner=useLearner(day,exerciseFunctions);
 return <div className="task-form" data-assignment-day={day}><h3>Placement payload</h3><button onClick={()=>learner.run(input)}>Decode synthetic payload</button><Result result={learner.result} error={learner.error}/></div>;
}
function SafeTextTask({ day, exerciseFunctions }) {
 const input=fixtures.frontend_20.cases[0].input;const learner=useLearner(day,exerciseFunctions);
 return <div className="task-form" data-assignment-day={day}><h3>Seller-provided content</h3><p>Untrusted text sample: <code>{input.text}</code></p><button onClick={()=>learner.run(input)}>Render as safe text</button><Result result={learner.result} error={learner.error}/></div>;
}
function CompareTask({ day, exerciseFunctions }) {
 const input=fixtures.frontend_21.cases[0].input;const learner=useLearner(day,exerciseFunctions);const [selected,setSelected]=useState('');
 return <div className="task-form" data-assignment-day={day}><h3>Compare placement options</h3><div className="cards">{input.options.map(o=><button key={o.id} aria-pressed={selected===o.id} onClick={()=>setSelected(o.id)} disabled={!o.available}>{o.id} · {o.fresh?'fresh':'stale'} · {o.totalCents}¢</button>)}</div><button onClick={()=>learner.run(input)}>Rank comparable options</button><Result result={learner.result} error={learner.error}/></div>;
}
function FieldTask({ day, exerciseFunctions }) {
 const input=fixtures.frontend_22.cases[0].input;const [value,setValue]=useState('');const learner=useLearner(day,exerciseFunctions);
 return <div className="task-form" data-assignment-day={day}><label htmlFor={input.id}>Email <span>Required</span></label><p id={input.hintId}>We use this for the practice account.</p><input id={input.id} value={value} onChange={e=>setValue(e.target.value)} aria-describedby={learner.result||input.hintId}/><p id={input.errorId} role="status">{learner.error||''}</p><button onClick={()=>learner.run(input)}>Check field associations</button><Result result={learner.result} error={learner.error}/></div>;
}
function VitalsTask({ day, exerciseFunctions }) {
 const input=fixtures.frontend_24.cases[0].input;const learner=useLearner(day,exerciseFunctions);
 return <div className="task-form" data-assignment-day={day}><h3>Web vitals</h3><ul>{input.samples.map((x,i)=><li key={i}>LCP sample {i+1}: {x} ms</li>)}</ul><button onClick={()=>learner.run(input)}>Classify performance samples</button><Result result={learner.result} error={learner.error}/></div>;
}
function ExplanationTask({ day, exerciseFunctions }) {
 const input=fixtures.frontend_25.cases[0].input;const learner=useLearner(day,exerciseFunctions);
 return <div className="task-form" data-assignment-day={day}><h3>Why this option was suggested</h3><button onClick={()=>learner.run(input)}>Filter allowlisted reasons</button><ul>{(learner.result||[]).map((x,i)=><li key={i}>{x}</li>)}</ul><Result error={learner.error}/></div>;
}
function QuoteContractTask({ day, exerciseFunctions }) {
 const input=fixtures.frontend_26.cases[0].input;const learner=useLearner(day,exerciseFunctions);
 return <div className="task-form" data-assignment-day={day}><h3>Quote response contract</h3><p>{input.quotes.length} synthetic quote response(s)</p><button onClick={()=>learner.run(input)}>Validate quote contract</button><Result result={learner.result} error={learner.error}/></div>;
}
function GalleryTask({ day, exerciseFunctions }) {
 const input=fixtures.frontend_27.cases[0].input;const learner=useLearner(day,exerciseFunctions);
 return <div className="task-form" data-assignment-day={day}><h3>Placement images</h3><ul>{input.images.map((image,i)=><li key={i}><span role="img" aria-label={image.alt||'Missing alternative text'}>▧</span> {image.alt||'Missing alt text'} · {image.width}px</li>)}</ul><button onClick={()=>learner.run(input)}>Check gallery descriptors</button><Result result={learner.result} error={learner.error}/></div>;
}
function TabsTask({ day, exerciseFunctions }) {
 const labels=fixtures.frontend_28.cases[0].input.tabs;const [selected,setSelected]=useState(0);const refs=useRef([]);const learner=useLearner(day,exerciseFunctions);
 const select=async(name)=>{const index=await learner.run({tabs:labels,active:name});if(Number.isInteger(index))setSelected(index);};
 const move=delta=>{let i=selected;if(delta==='home')i=0;else if(delta==='end')i=labels.length-1;else i=(selected+delta+labels.length)%labels.length;select(labels[i]).then(()=>refs.current[i]?.focus());};
 return <div className="task-form" data-assignment-day={day}><div role="tablist" aria-label="Placement details" onKeyDown={e=>{if(e.key==='ArrowRight'){e.preventDefault();move(1);}else if(e.key==='ArrowLeft'){e.preventDefault();move(-1);}else if(e.key==='Home'){e.preventDefault();move('home');}else if(e.key==='End'){e.preventDefault();move('end');}}}>{labels.map((label,i)=><button key={label} ref={el=>refs.current[i]=el} role="tab" id={`tab-${i}`} aria-controls={`panel-${i}`} aria-selected={selected===i} tabIndex={selected===i?0:-1} onClick={()=>select(label)}>{label}</button>)}</div>{labels.map((label,i)=><section key={label} role="tabpanel" id={`panel-${i}`} aria-labelledby={`tab-${i}`} hidden={selected!==i}>{label} details for synthetic placement options.</section>)}<Result result={learner.result} error={learner.error}/></div>;
}
function QuoteComparisonTask({ day, api, exerciseFunctions }) {
 const [options,setOptions]=useState([]);const [state,setState]=useState('idle');const [selected,setSelected]=useState('');const [refresh,setRefresh]=useState(0);const learner=useLearner(day,exerciseFunctions);
 const load=async()=>{setState('loading');try{const list=await api.placements('',{signal:new AbortController().signal});setOptions(list);const data=list.map(x=>({id:x.id,visible:true,feeCents:x.feeCents}));const value=await learner.run({options:data});if(value)setState('ready');else setState('error');}catch{setState(options.length?'partial':'error');}};
 useEffect(()=>{load();},[refresh]);
 return <div className="task-form" data-assignment-day={day}><h3>Quote comparison</h3><p role="status">{state==='loading'?'Loading options…':state}</p><div className="cards">{options.map(o=><button key={o.id} aria-pressed={selected===o.id} onClick={()=>setSelected(o.id)}>{o.name} · ₹{(o.feeCents/100).toFixed(2)}</button>)}</div><button onClick={()=>setRefresh(x=>x+1)}>Refresh options</button><Result result={learner.result} error={learner.error}/></div>;
}
function RetrospectiveTask({ day, exerciseFunctions }) {
 const input=fixtures.frontend_30.cases[0].input;const [note,setNote]=useState('');const learner=useLearner(day,exerciseFunctions);
 const weakest=new Set(Array.isArray(learner.result)?learner.result:[]);
 return <div className="task-form" data-assignment-day={day}><h3>Practice reflection</h3><ul>{input.attempts.map(a=><li key={a.topic} aria-label={weakest.has(a.topic)?'Weakest practice area':undefined}>{a.topic}: {a.passed}/{a.attempted} passed {weakest.has(a.topic)&&<strong> · Focus next</strong>}</li>)}</ul><button onClick={()=>learner.run(input)}>Find weakest topics</button><Result result={learner.result} error={learner.error}/><label htmlFor="next-step-note">One next-step note</label><textarea id="next-step-note" value={note} onChange={e=>setNote(e.target.value)}/><p>Practice record only; this does not establish mastery.</p></div>;
}
function TaskBody({ day, item, api, exerciseFunctions }) {
 const props={day,item,api,exerciseFunctions};
 if(day===1)return <RegistrationTask {...props}/>;
 if(day===2)return <InventoryFilterTask {...props}/>;
 if(day===3)return <AsyncSubmissionTask {...props}/>;
 if(day===4)return <PriceBandTask {...props}/>;
 if(day===5)return <IdentityRowsTask {...props}/>;
 if(day===6)return <TreeTask {...props}/>;
 if(day===7)return <SortableTask {...props}/>;
 if(day===8)return <TimelineTask {...props}/>;
 if(day===9)return <WizardTask {...props}/>;
 if(day===10)return <FacetTask {...props}/>;
 if(day===11)return <AutocompleteTask {...props}/>;
 if(day===12)return <QuoteTask {...props}/>;
 if(day===13)return <LazyListTask {...props}/>;
 if(day===19)return <PayloadTask {...props}/>;
 if(day===14)return <MarketplaceTask {...props}/>;
 if(day===15)return <UrlFilterTask {...props}/>;
 if(day===16)return <CacheTask {...props}/>;
 if(day===17)return <StaleResponseTask {...props}/>;
 if(day===18)return <ResilientQuoteTask {...props}/>;
 if(day===20)return <SafeTextTask {...props}/>;
 if(day===21)return <CompareTask {...props}/>;
 if(day===22)return <FieldTask {...props}/>;
 if(day===23)return <PollingTask {...props}/>;
 if(day===24)return <VitalsTask {...props}/>;
 if(day===25)return <ExplanationTask {...props}/>;
 if(day===26)return <QuoteContractTask {...props}/>;
 if(day===27)return <GalleryTask {...props}/>;
 if(day===28)return <TabsTask {...props}/>;
 if(day===29)return <QuoteComparisonTask {...props}/>;
 if(day===30)return <RetrospectiveTask {...props}/>;
 return null;
}
export function AssignmentApp({ api=defaultApi, initialDay=1, exerciseFunctions=defaultExercises }) {
 const [day,setDay]=useState(initialDay);const item=days[day-1];
 return <main className="shell">
  <header className="top"><a className="brand" href="#top">Amazon interview sprint</a><span className="demo-pill">Practice sandbox · synthetic data</span></header>
  <section className="intro"><p className="eyebrow">FRONT-END ENGINEER II · JOB 10575321</p><h1>Daily component practice</h1><p className="lede">One 60-minute practice contract per day. Implement the learner function, then exercise its React component.</p></section>
  <nav className="day-nav" aria-label="Choose assignment day">{days.map(x=><button key={x.day} type="button" aria-current={day===x.day?'page':undefined} onClick={()=>setDay(x.day)}>Day {String(x.day).padStart(2,'0')}</button>)}</nav>
  <section className="workspace" aria-labelledby="assignment-title"><div className="section-head"><div><p className="eyebrow">DAY {String(day).padStart(2,'0')} · {item.topic}</p><h2 id="assignment-title">{item.title}</h2></div><label className="day-picker">Go to day<select aria-label="Go to day" value={day} onChange={e=>setDay(Number(e.target.value))}>{days.map(x=><option key={x.day} value={x.day}>Day {String(x.day).padStart(2,'0')}</option>)}</select></label></div>
  <p className="task-prompt">{item.coding_prompt}</p><TaskBody key={day} day={day} item={item} api={api} exerciseFunctions={exerciseFunctions}/>
  <aside className="review"><h3>15 min · design / review</h3><p>{item.design_prompt}</p><h3>10 min · {item.leadership_principle}</h3><p>Use one truthful STAR example; record your action, evidence, and learning. Do not invent metrics.</p></aside></section>
  <footer>Study plan only · no real seller or placement data</footer>
 </main>;
}
export default AssignmentApp;
