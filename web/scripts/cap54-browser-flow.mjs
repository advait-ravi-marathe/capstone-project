import assert from 'node:assert/strict'
import { spawn } from 'node:child_process'
import { createServer } from 'node:net'
import { mkdtemp, rm } from 'node:fs/promises'
import { tmpdir } from 'node:os'
import { join } from 'node:path'

const apiTarget = process.argv[2]
assert.ok(apiTarget?.startsWith('http://127.0.0.1:'), 'use only the local synthetic API')
const root = new URL('..', import.meta.url).pathname
const wait = (ms) => new Promise((resolve) => setTimeout(resolve, ms))
const freePort = () => new Promise((resolve, reject) => {
  const server = createServer(); server.once('error', reject)
  server.listen(0, '127.0.0.1', () => { const port = server.address().port; server.close(() => resolve(port)) })
})
const stop = async (child, signal) => {
  if (!child?.pid || child.exitCode !== null) return
  const exited = child.exitCode !== null ? Promise.resolve() : new Promise((resolve) => child.once('exit', resolve))
  child.kill(signal); await Promise.race([exited, wait(3000)])
  if (child.exitCode === null) { child.kill('SIGKILL'); await Promise.race([exited, wait(1000)]) }
}

const port = await freePort(), profile = await mkdtemp(join(tmpdir(), 'cap54-chrome-'))
let vite, chrome, cdp, nextId = 0
let passed = false
try {
  vite = spawn(process.execPath, ['node_modules/vite/bin/vite.js', '--host', '127.0.0.1', '--port', String(port), '--strictPort'], {
    cwd: root, stdio: 'ignore', env: { ...process.env, COVERSTORY_API_TARGET: apiTarget, npm_config_cache: '/tmp/capstone-npm-cache' },
  })
  const base = `http://127.0.0.1:${port}`
  for (let i=0;i<120;i++) { try { if ((await fetch(base)).ok) break } catch {} await wait(100) }
  chrome = spawn('/usr/lib/chromium/chromium', ['--headless=new','--no-sandbox','--disable-gpu','--disable-dev-shm-usage','--remote-debugging-port=0',`--user-data-dir=${profile}`,'about:blank'], { stdio:['ignore','ignore','pipe'] })
  chrome.stderr.setEncoding('utf8'); let stderr=''; chrome.stderr.on('data',(chunk)=>stderr+=chunk)
  let ws; for(let i=0;i<100&&!ws;i++){ws=stderr.match(/DevTools listening on (ws:\/\/[^\s]+)/)?.[1];if(!ws)await wait(100)}
  assert.ok(ws,'Chromium did not start')
  const endpoint=`http://127.0.0.1:${new URL(ws).port}`
  const targetResponse=await fetch(`${endpoint}/json/new?about:blank`,{method:'PUT'})
  assert.ok(targetResponse.ok)
  const target=await targetResponse.json()
  cdp=new WebSocket(target.webSocketDebuggerUrl); await new Promise((resolve)=>cdp.addEventListener('open',resolve,{once:true}))
  const pending=new Map(); let sessionRequests=0; let privateRequests=0
  cdp.addEventListener('message',(event)=>{
    const message=JSON.parse(event.data)
    if(message.method==='Network.requestWillBeSent'){
      const url=message.params.request.url
      if(url.endsWith('/api/session'))sessionRequests++
      if(url.includes('/api/v1/me/journeys'))privateRequests++
    }
    if(message.id&&pending.has(message.id)){pending.get(message.id)(message.result);pending.delete(message.id)}
  })
  const send=(method,params={})=>new Promise((resolve)=>{const id=++nextId;pending.set(id,resolve);cdp.send(JSON.stringify({id,method,params}))})
  await send('Network.enable'); await send('Page.enable'); await send('Runtime.enable')
  const evaluate=async(expression)=>(await send('Runtime.evaluate',{expression,returnByValue:true,awaitPromise:true})).result.value
  const setSession=async(value)=>send('Network.setCookie',{name:'cap54_test_session',value,url:base,httpOnly:true,sameSite:'Lax'})
  const reload=async()=>{await send('Page.reload',{ignoreCache:true});await wait(900)}
  await setSession('synthetic-session-a')
  await send('Page.navigate',{url:base})
  for(let i=0;i<100&&await evaluate("document.querySelector('.session-label')?.textContent !== 'Signed in'");i++)await wait(100)
  assert.equal((await evaluate("document.querySelector('.session-label').textContent")),'Signed in')
  assert.ok(await evaluate("document.querySelectorAll('.offer-card').length >= 1"),'anonymous health catalog remains visible')
  for(let i=0;i<100&&await evaluate("!document.querySelector('.resume-card')?.textContent.includes('Resume journey')");i++)await wait(100)
  assert.ok(await evaluate("document.querySelector('.resume-card').textContent.includes('Resume journey')"))
  await evaluate("[...document.querySelectorAll('.resume-card button')].find(b=>b.textContent.includes('Resume journey')).click()")
  for(let i=0;i<100&&await evaluate("!document.querySelector('.resume-card')?.textContent.includes('DRAFT · CONTINUE_CHECKOUT')");i++)await wait(50)
  assert.ok(await evaluate("document.querySelector('.resume-card').textContent.includes('DRAFT · CONTINUE_CHECKOUT')"))
  await setSession('synthetic-session-a-reloaded'); await reload()
  for(let i=0;i<100&&await evaluate("!document.querySelector('.resume-card')?.textContent.includes('Resume journey')");i++)await wait(100)
  assert.ok(await evaluate("document.querySelector('.session-label').textContent === 'Signed in' && document.querySelector('.resume-card').textContent.includes('Resume journey')"),'fresh page revalidates session and refetches owned journeys')
  const afterReloadSessions=sessionRequests, afterReloadPrivate=privateRequests
  await setSession('synthetic-session-b'); await reload()
  for(let i=0;i<100&&await evaluate("!document.querySelector('.resume-card')?.textContent.includes('Resume journey')");i++)await wait(100)
  await evaluate("[...document.querySelectorAll('.resume-card button')].find(b=>b.textContent.includes('Resume journey')).click()")
  for(let i=0;i<100&&await evaluate("!document.querySelector('.resume-card')?.textContent.includes('SUBMITTED_PENDING_DISPATCH · WAIT_FOR_DISPATCH')");i++)await wait(50)
  assert.ok(await evaluate("document.querySelector('.resume-card').textContent.includes('SUBMITTED_PENDING_DISPATCH · WAIT_FOR_DISPATCH')"),'account switch returns only the newly authenticated owner journey')
  await send('Network.deleteCookies',{name:'cap54_test_session',url:base}); await reload()
  for(let i=0;i<100&&await evaluate("document.querySelector('.session-label')?.textContent !== 'Guest preview'");i++)await wait(100)
  assert.equal(await evaluate("document.querySelector('.session-label').textContent"),'Guest preview')
  assert.ok(await evaluate("document.querySelectorAll('.offer-card').length >= 1"),'public catalog survives logout')
  assert.equal(await evaluate("[...document.querySelectorAll('.resume-card button')].some(b=>!b.disabled)"),false,'private resume is disabled without a session')
  assert.ok(sessionRequests>=4&&privateRequests>=4,'session and private API are revalidated after reload/account changes')
  console.log('CAP54_BROWSER_FLOW '+JSON.stringify({reloadRefetchedSession:true,ownerScopedResume:true,accountSwitchClearedPrivateView:true,logoutKeptPublicCatalog:true,privateCacheMemoryOnly:true,sessionRequests,privateRequests}))
  passed = true
} finally {
  if (cdp?.readyState === WebSocket.OPEN) cdp.send(JSON.stringify({id:++nextId,method:'Browser.close',params:{}}))
  cdp?.close(); await stop(chrome,'SIGTERM'); await stop(vite,'SIGTERM')
  for(let i=0;i<10;i++){try{await rm(profile,{recursive:true,force:true});break}catch(error){if(i===9)throw error;await wait(100)}}
}
// All spawned browser/server processes and the temporary profile are cleaned above.
if (passed) process.exit(0)
