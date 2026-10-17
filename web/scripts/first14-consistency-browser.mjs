import assert from 'node:assert/strict'
import { spawn } from 'node:child_process'
import { createServer } from 'node:net'
import { mkdtemp, rm } from 'node:fs/promises'
import { tmpdir } from 'node:os'
import { join } from 'node:path'

const apiTarget = process.argv[2]
if (!apiTarget) throw new Error('Usage: node scripts/first14-consistency-browser.mjs <spring-api-url>')
const root = new URL('..', import.meta.url).pathname
const chromium = process.env.COVERSTORY_CHROMIUM?.trim() || 'chromium'
const wait = (ms) => new Promise((resolve) => setTimeout(resolve, ms))
const freePort = async () => await new Promise((resolve, reject) => {
  const s = createServer(); s.once('error', reject); s.listen(0, '127.0.0.1', () => { const p = s.address().port; s.close(() => resolve(p)) })
})
const webPort = await freePort()
const profile = await mkdtemp(join(tmpdir(), 'cs90-29-chrome-'))
let vite, chrome, cdp
const chromeErr = []
try {
  vite = spawn('npm', ['run', 'dev', '--', '--host', '127.0.0.1', '--port', String(webPort), '--strictPort'], {
    cwd: root, env: { ...process.env, COVERSTORY_API_TARGET: apiTarget, npm_config_cache: '/tmp/capstone-npm-cache' }, stdio: ['ignore', 'ignore', 'ignore'],
  })
  const base = `http://127.0.0.1:${webPort}`
  let up = false
  for (let i=0; i<100; i++) { try { if ((await fetch(`${base}/first14-consistency-harness.html`)).ok) { up=true; break } } catch {} await wait(100) }
  assert.ok(up, 'Vite CS90-29 harness did not start')
  chrome = spawn(chromium, ['--headless=new','--no-sandbox','--disable-gpu','--disable-dev-shm-usage','--remote-debugging-port=0',`--user-data-dir=${profile}`,'about:blank'], { stdio: ['ignore','ignore','pipe'] })
  let stderr=''; chrome.stderr.setEncoding('utf8'); chrome.stderr.on('data',(part)=>{stderr+=part; chromeErr.push(part); if(chromeErr.length>60) chromeErr.shift()})
  let browserWs
  for(let i=0;i<100&&!browserWs;i++){browserWs=stderr.match(/DevTools listening on (ws:\/\/[^\s]+)/)?.[1]; if(!browserWs)await wait(100)}
  assert.ok(browserWs,`Chromium DevTools did not start: ${stderr.slice(-1000)}`)
  const wsPort=new URL(browserWs).port, cdpHttp=`http://127.0.0.1:${wsPort}`
  let target
  for(let i=0;i<50&&!target;i++){try{const r=await fetch(`${cdpHttp}/json/new?about:blank`,{method:'PUT'}); if(r.ok)target=await r.json()}catch{} await wait(100)}
  assert.ok(target?.webSocketDebuggerUrl,'Chromium target unavailable')
  cdp=new WebSocket(target.webSocketDebuggerUrl)
  await new Promise((resolve,reject)=>{cdp.addEventListener('open',resolve,{once:true});cdp.addEventListener('error',reject,{once:true})})
  let id=0; const pending=new Map()
  cdp.addEventListener('message',(event)=>{const msg=JSON.parse(event.data); if(msg.id&&pending.has(msg.id)){const p=pending.get(msg.id);pending.delete(msg.id);msg.error?p.reject(new Error(msg.error.message)):p.resolve(msg.result)}})
  const send=(method,params={})=>new Promise((resolve,reject)=>{const n=++id;pending.set(n,{resolve,reject});cdp.send(JSON.stringify({id:n,method,params}));setTimeout(()=>{if(pending.has(n)){pending.delete(n);reject(new Error(`CDP timeout ${method}`))}},10000).unref()})
  await send('Page.enable'); await send('Runtime.enable')
  const evaluate=async(expression)=>{const r=await send('Runtime.evaluate',{expression,returnByValue:true,awaitPromise:true});if(r.exceptionDetails)throw new Error(r.exceptionDetails.exception?.description??r.exceptionDetails.text);return r.result.value}
  await send('Page.navigate',{url:`${base}/first14-consistency-harness.html`})
  async function until(expr){for(let i=0;i<120;i++){try{if(await evaluate(expr))return}catch{}await wait(100)}throw new Error(`Browser timeout: ${expr}`)}
  await until("document.querySelectorAll('.persisted-catalog .offer-card').length === 3")
  const select=[
    ['synthetic-health-A','A/v1'],['synthetic-health-B','B/v1'],['synthetic-health-C','C/v1'],
  ]
  for(const [source,version] of select){
    const ok=await evaluate(`(()=>{const card=[...document.querySelectorAll('.persisted-catalog .offer-card')].find(x=>x.innerText.includes(${JSON.stringify(source)})&&x.innerText.includes(${JSON.stringify(version)}));if(!card)return false;card.querySelector('input[type=checkbox]').click();return true})()`)
    assert.equal(ok,true,`version ${source} ${version} must be visible and selectable`)
  }
  await until("document.querySelector('.persisted-catalog .selection-toolbar strong')?.textContent === '3'")
  await evaluate("document.querySelector('.persisted-catalog .selection-toolbar button').click()")
  await until("document.querySelectorAll('.persisted-catalog .compare-panel tbody tr').length === 4")
  const summary=await evaluate(`(()=>{const panel=document.querySelector('.persisted-catalog .compare-panel');const header=[...panel.querySelectorAll('thead th')].slice(1).map(th=>({sourceId:th.childNodes[0]?.textContent.trim(),sourceVersion:th.querySelector('small')?.textContent.trim()}));const row=[...panel.querySelectorAll('tbody tr')].find(tr=>tr.firstElementChild.textContent.trim()==='Version ID');return {header,versionIds:[...row.querySelectorAll('td')].map(td=>td.textContent.trim()),unknownVisible:panel.innerText.includes('Unknown'),payableClaim:panel.innerText.includes('Payable review is unavailable')}})()`)
  assert.deepEqual(summary.header,[{sourceId:'synthetic-health-A',sourceVersion:'A/v1'},{sourceId:'synthetic-health-B',sourceVersion:'B/v1'},{sourceId:'synthetic-health-C',sourceVersion:'C/v1'}])
  assert.deepEqual(summary.versionIds,['a8100000-0000-4000-8000-000000000001','b8100000-0000-4000-8000-000000000001','c8100000-0000-4000-8000-000000000001'])
  assert.equal(summary.unknownVisible,true); assert.equal(summary.payableClaim,true)
  await evaluate("[...document.querySelectorAll('.persisted-catalog button')].find(b=>b.textContent.includes('Export sanitized selection')).click()")
  const exported=await evaluate('window.__CS90_SANITIZED_EXPORT__')
  assert.deepEqual(exported,{schemaVersion:1,selection:summary.versionIds.map(versionId=>({versionId}))})
  console.log('CS90_29_RESULT '+JSON.stringify({...summary,liveSpringApi:true,instructorDecoderInjected:true,exportOnlyIds:true}))
} catch(error){console.error(error?.stack??String(error));if(chromeErr.length)console.error(chromeErr.join('').slice(-1200));process.exitCode=1}
finally{try{cdp?.close()}catch{}for(const p of [chrome,vite])if(p&&!p.killed){p.kill('SIGTERM');await wait(150);if(!p.killed)p.kill('SIGKILL')}await rm(profile,{recursive:true,force:true})}
