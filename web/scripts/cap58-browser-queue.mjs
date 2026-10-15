import assert from 'node:assert/strict'
import { spawn } from 'node:child_process'
import { createServer } from 'node:net'
import { mkdtemp, rm } from 'node:fs/promises'
import { tmpdir } from 'node:os'
import { join } from 'node:path'

const root = new URL('..', import.meta.url).pathname
const apiTarget = process.argv[2] ?? null
const freePort = async () => await new Promise((resolve, reject) => { const s = createServer(); s.once('error', reject); s.listen(0, '127.0.0.1', () => { const p = s.address().port; s.close(() => resolve(p)) }) })
const wait = (ms) => new Promise((r) => setTimeout(r, ms))
const stop = async (child, signal) => {
  if (!child || child.exitCode !== null) return
  const exited = new Promise((resolve) => child.once('exit', resolve))
  child.kill(signal)
  await Promise.race([exited, wait(3000)])
  if (child.exitCode === null) child.kill('SIGKILL')
}
const port = await freePort(), profile = await mkdtemp(join(tmpdir(), 'cap58-queue-chrome-'))
let vite, chrome, cdp
try {
  vite = spawn('npm', ['run', 'dev', '--', '--host', '127.0.0.1', '--port', String(port), '--strictPort'], { cwd: root, stdio: 'ignore', env: { ...process.env, npm_config_cache: '/tmp/capstone-npm-cache', ...(apiTarget ? { COVERSTORY_API_TARGET: apiTarget } : {}) } })
  const base = `http://127.0.0.1:${port}`
  for (let i=0;i<100;i++) { try { if ((await fetch(`${base}/cap58-harness.html`)).ok) break } catch {} await wait(100) }
  chrome = spawn('/usr/bin/chromium', ['--headless=new','--no-sandbox','--disable-gpu','--disable-dev-shm-usage','--remote-debugging-port=0',`--user-data-dir=${profile}`,'about:blank'], { stdio: ['ignore','ignore','pipe'] })
  chrome.stderr.setEncoding('utf8'); let stderr=''; chrome.stderr.on('data', x=>stderr+=x)
  let ws; for(let i=0;i<100&&!ws;i++){ws=stderr.match(/DevTools listening on (ws:\/\/[^\s]+)/)?.[1];if(!ws)await wait(100)}
  assert.ok(ws, 'Chromium did not start')
  const endpoint=`http://127.0.0.1:${new URL(ws).port}`; let target
  for(let i=0;i<50&&!target;i++){try{const r=await fetch(`${endpoint}/json/new?about:blank`,{method:'PUT'});if(r.ok)target=await r.json()}catch{}await wait(100)}
  cdp = new WebSocket(target.webSocketDebuggerUrl); await new Promise(r=>cdp.addEventListener('open',r,{once:true}))
  let id=0;const pending=new Map();cdp.addEventListener('message',e=>{const m=JSON.parse(e.data);if(m.id&&pending.has(m.id)){pending.get(m.id)(m.result);pending.delete(m.id)}})
  const send=(method,params={})=>new Promise(resolve=>{const n=++id;pending.set(n,resolve);cdp.send(JSON.stringify({id:n,method,params}))})
  await send('Page.enable');await send('Runtime.enable')
  const evaljs=async expression=>(await send('Runtime.evaluate',{expression,returnByValue:true,awaitPromise:true})).result.value
  await send('Page.navigate',{url:`${base}/cap58-harness.html${apiTarget ? '?live=1' : ''}`});await wait(300)
  await evaljs("document.querySelector('#fill').click()")
  assert.match(await evaljs("document.querySelector('#queue-state').textContent"),/accepted 20; rejected 1/)
  await evaljs("document.querySelector('#flush').click()")
  await evaljs("document.querySelector('#checkout').click()")
  for(let i=0;i<100&&!await evaljs("document.querySelector('#evidence').textContent.length>0");i++)await wait(50)
  const evidence=JSON.parse(await evaljs("document.querySelector('#evidence').textContent"))
  assert.equal(evidence.attempts,6);assert.equal(evidence.dropped,apiTarget ? 10 : 20);assert.equal(evidence.sent,apiTarget ? 10 : 0);assert.equal(evidence.queuedAfterFlush,0)
  assert.match(await evaljs("document.querySelector('#checkout-state').textContent"),/1/)
  console.log('CAP58_BROWSER_QUEUE ' + JSON.stringify({...evidence, mode: apiTarget ? 'real-spring-api' : 'offline-retry-oracle', maxQueue:20, rejectedOverflow:1, checkoutRemainedAvailable:true}))
} finally {
  cdp?.close()
  await stop(chrome, 'SIGTERM')
  await stop(vite, 'SIGTERM')
  // Chromium may leave crash-report/lock files for a moment after exit.
  for (let i=0;i<10;i++) {
    try { await rm(profile,{recursive:true,force:true}); break }
    catch (error) { if (i===9) throw error; await wait(100) }
  }
}
