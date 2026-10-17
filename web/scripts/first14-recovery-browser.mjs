import assert from 'node:assert/strict'
import { spawn } from 'node:child_process'
import { createServer } from 'node:net'
import { mkdtemp, rm } from 'node:fs/promises'
import { tmpdir } from 'node:os'
import { join } from 'node:path'
import readline from 'node:readline'

const apiTarget=process.argv[2]
if(!apiTarget)throw new Error('Usage: node scripts/first14-recovery-browser.mjs <spring-api-url>')
const root=new URL('..',import.meta.url).pathname
const chromium=process.env.COVERSTORY_CHROMIUM?.trim()||'chromium'
const wait=(ms)=>new Promise(resolve=>setTimeout(resolve,ms))
const freePort=async()=>await new Promise((resolve,reject)=>{const s=createServer();s.once('error',reject);s.listen(0,'127.0.0.1',()=>{const p=s.address().port;s.close(()=>resolve(p))})})
const webPort=await freePort(),profile=await mkdtemp(join(tmpdir(),'cs90-30-chrome-'))
let vite,chrome,cdp,reader
const chromeErr=[]
try{
 vite=spawn('npm',['run','dev','--','--host','127.0.0.1','--port',String(webPort),'--strictPort'],{cwd:root,env:{...process.env,COVERSTORY_API_TARGET:apiTarget,npm_config_cache:'/tmp/capstone-npm-cache'},stdio:['ignore','ignore','ignore']})
 const base=`http://127.0.0.1:${webPort}`
 let up=false;for(let i=0;i<100;i++){try{if((await fetch(`${base}/first14-recovery-harness.html`)).ok){up=true;break}}catch{}await wait(100)}assert.ok(up,'Vite CS90-30 harness did not start')
 chrome=spawn(chromium,['--headless=new','--no-sandbox','--disable-gpu','--disable-dev-shm-usage','--remote-debugging-port=0',`--user-data-dir=${profile}`,'about:blank'],{stdio:['ignore','ignore','pipe']})
 let stderr='';chrome.stderr.setEncoding('utf8');chrome.stderr.on('data',x=>{stderr+=x;chromeErr.push(x);if(chromeErr.length>60)chromeErr.shift()})
 let browserWs;for(let i=0;i<100&&!browserWs;i++){browserWs=stderr.match(/DevTools listening on (ws:\/\/[^\s]+)/)?.[1];if(!browserWs)await wait(100)}assert.ok(browserWs,`Chromium DevTools did not start: ${stderr.slice(-1000)}`)
 const cdpHttp=`http://127.0.0.1:${new URL(browserWs).port}`;let target
 for(let i=0;i<50&&!target;i++){try{const r=await fetch(`${cdpHttp}/json/new?about:blank`,{method:'PUT'});if(r.ok)target=await r.json()}catch{}await wait(100)}assert.ok(target?.webSocketDebuggerUrl,'Chromium target unavailable')
 cdp=new WebSocket(target.webSocketDebuggerUrl);await new Promise((resolve,reject)=>{cdp.addEventListener('open',resolve,{once:true});cdp.addEventListener('error',reject,{once:true})})
 let id=0;const pending=new Map();cdp.addEventListener('message',e=>{const m=JSON.parse(e.data);if(m.id&&pending.has(m.id)){const p=pending.get(m.id);pending.delete(m.id);m.error?p.reject(new Error(m.error.message)):p.resolve(m.result)}})
 const send=(method,params={})=>new Promise((resolve,reject)=>{const n=++id;pending.set(n,{resolve,reject});cdp.send(JSON.stringify({id:n,method,params}));setTimeout(()=>{if(pending.has(n)){pending.delete(n);reject(new Error(`CDP timeout ${method}`))}},10000).unref()})
 await send('Page.enable');await send('Runtime.enable')
 const evaluate=async(expression)=>{const r=await send('Runtime.evaluate',{expression,returnByValue:true,awaitPromise:true});if(r.exceptionDetails)throw new Error(r.exceptionDetails.exception?.description??r.exceptionDetails.text);return r.result.value}
 await send('Page.navigate',{url:`${base}/first14-recovery-harness.html`})
 async function until(expr){for(let i=0;i<120;i++){try{if(await evaluate(expr))return}catch{}await wait(100)}throw new Error(`Browser timeout: ${expr}`)}
 await until("document.querySelectorAll('.persisted-catalog .offer-card').length === 3")
 for(const [source,version] of [['synthetic-health-A','A/v1'],['synthetic-health-B','B/v1']]){
  const found=await evaluate(`(()=>{const card=[...document.querySelectorAll('.persisted-catalog .offer-card')].find(x=>x.innerText.includes(${JSON.stringify(source)})&&x.innerText.includes(${JSON.stringify(version)}));if(!card)return false;card.querySelector('input[type=checkbox]').click();return true})()`)
  assert.equal(found,true,`${source} ${version} must be selectable`)
 }
 await until("document.querySelector('.persisted-catalog .selection-toolbar strong')?.textContent === '2'")
 await evaluate("document.querySelector('.persisted-catalog .selection-toolbar button').click()")
 await until("document.querySelector('.persisted-catalog .compare-panel') !== null")
 console.log('CS90_30_BROWSER_READY')
 reader=readline.createInterface({input:process.stdin,crlfDelay:Infinity})
 for await(const line of reader){
  if(line.trim()==='OUTAGE'){
   await evaluate("document.querySelector('.persisted-catalog .refresh-persisted-catalog').click()")
   await until("document.querySelector('.persisted-catalog').innerText.includes('Refresh failed')")
   const outage=await evaluate(`(()=>{const s=[...document.querySelectorAll('.persisted-catalog .offer-card')].filter(c=>c.querySelector('input').checked).map(c=>c.innerText);return {selectedCount:s.length,compareDisabled:document.querySelector('.persisted-catalog .selection-toolbar button').disabled,staleWarning:document.querySelector('.persisted-catalog').innerText.includes('may be stale'),compareStillVisible:document.querySelector('.persisted-catalog .compare-panel')!==null}})()`)
   assert.deepEqual(outage,{selectedCount:2,compareDisabled:true,staleWarning:true,compareStillVisible:true})
   console.log('CS90_30_OUTAGE_VALIDATED '+JSON.stringify(outage));continue
  }
  if(line.trim()==='RECOVER'){
   await evaluate("document.querySelector('.persisted-catalog .refresh-persisted-catalog').click()")
   await until("[...document.querySelectorAll('.persisted-catalog .offer-card')].some(c=>c.innerText.includes('synthetic-health-A')&&c.innerText.includes('A/v2')&&c.innerText.includes('PUBLISHED'))")
   const beforeCompare=await evaluate(`(()=>{const cards=[...document.querySelectorAll('.persisted-catalog .offer-card')];return {rows:cards.length,selected:cards.filter(c=>c.querySelector('input').checked).map(c=>c.innerText),a1Retired:cards.some(c=>c.innerText.includes('synthetic-health-A')&&c.innerText.includes('A/v1')&&c.innerText.includes('RETIRED')),a2Published:cards.some(c=>c.innerText.includes('synthetic-health-A')&&c.innerText.includes('A/v2')&&c.innerText.includes('PUBLISHED'))}})()`)
   assert.equal(beforeCompare.rows,4);assert.equal(beforeCompare.selected.length,2);assert.equal(beforeCompare.a1Retired,true);assert.equal(beforeCompare.a2Published,true)
   await evaluate("document.querySelector('.persisted-catalog .selection-toolbar button').click()")
   await until("document.querySelectorAll('.persisted-catalog .compare-panel tbody tr').length === 4")
   const ids=await evaluate(`(()=>{const row=[...document.querySelectorAll('.persisted-catalog .compare-panel tbody tr')].find(x=>x.firstElementChild.textContent.trim()==='Version ID');return [...row.querySelectorAll('td')].map(td=>td.textContent.trim())})()`)
   assert.deepEqual(ids,['a8100000-0000-4000-8000-000000000001','b8100000-0000-4000-8000-000000000001'])
   console.log('CS90_30_RECOVERY_VALIDATED '+JSON.stringify({...beforeCompare,comparisonVersionIds:ids,explicitRefresh:true}));break
  }
 }
}catch(error){console.error(error?.stack??String(error));if(chromeErr.length)console.error(chromeErr.join('').slice(-1200));process.exitCode=1}
finally{reader?.close();try{cdp?.close()}catch{}for(const p of [chrome,vite])if(p&&!p.killed){p.kill('SIGTERM');await wait(150);if(!p.killed)p.kill('SIGKILL')}await rm(profile,{recursive:true,force:true})}
