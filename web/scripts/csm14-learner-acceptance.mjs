import assert from 'node:assert/strict'
import { spawn } from 'node:child_process'
import { createServer } from 'node:net'
import { mkdtemp, rm } from 'node:fs/promises'
import { tmpdir } from 'node:os'
import { join } from 'node:path'
import { fileURLToPath } from 'node:url'

const cap=process.argv[2]
if(!['CAP-638','CAP-639','CAP-640','CAP-641'].includes(cap))throw new Error('Usage: node scripts/csm14-learner-acceptance.mjs CAP-638|CAP-639|CAP-640|CAP-641')
const root=fileURLToPath(new URL('..',import.meta.url)), chromium=process.env.COVERSTORY_CHROMIUM?.trim()||'chromium'
const wait=ms=>new Promise(r=>setTimeout(r,ms)); const freePort=()=>new Promise((resolve,reject)=>{const s=createServer();s.once('error',reject);s.listen(0,'127.0.0.1',()=>{const p=s.address().port;s.close(()=>resolve(p))})})
const port=await freePort(), profile=await mkdtemp(join(tmpdir(),'csm14-chrome-')); let vite,chrome,ws,stderr=''
try {
  vite=spawn('npm',['run','dev','--','--host','127.0.0.1','--port',String(port),'--strictPort'],{cwd:root,env:{...process.env,npm_config_cache:'/tmp/capstone-npm-cache'},stdio:['ignore','ignore','ignore']})
  const base=`http://127.0.0.1:${port}`; let ready=false
  for(let i=0;i<100;i++){try{if((await fetch(`${base}/csm14-refresh-harness.html`)).ok){ready=true;break}}catch{}await wait(100)}
  assert.ok(ready,'Vite synthetic harness failed to start')
  chrome=spawn(chromium,['--headless=new','--no-sandbox','--disable-gpu','--disable-dev-shm-usage','--remote-debugging-port=0',`--user-data-dir=${profile}`,'about:blank'],{stdio:['ignore','ignore','pipe']})
  chrome.stderr.setEncoding('utf8');chrome.stderr.on('data',s=>stderr+=s)
  let endpoint;for(let i=0;i<100&&!endpoint;i++){endpoint=stderr.match(/DevTools listening on (ws:\/\/[^\s]+)/)?.[1];if(!endpoint)await wait(100)}
  assert.ok(endpoint,'Chromium DevTools endpoint missing')
  let target;const cdpHttp=`http://127.0.0.1:${new URL(endpoint).port}`
  for(let i=0;i<50&&!target;i++){try{const r=await fetch(`${cdpHttp}/json/new?about:blank`,{method:'PUT'});if(r.ok)target=await r.json()}catch{}await wait(100)}
  ws=new WebSocket(target.webSocketDebuggerUrl);await new Promise((resolve,reject)=>{ws.addEventListener('open',resolve,{once:true});ws.addEventListener('error',reject,{once:true})})
  let id=0;const pending=new Map();ws.addEventListener('message',e=>{const m=JSON.parse(e.data);if(m.id&&pending.has(m.id)){const p=pending.get(m.id);pending.delete(m.id);m.error?p.reject(new Error(m.error.message)):p.resolve(m.result)}})
  const send=(method,params={})=>new Promise((resolve,reject)=>{const n=++id;pending.set(n,{resolve,reject});ws.send(JSON.stringify({id:n,method,params}));setTimeout(()=>{if(pending.has(n)){pending.delete(n);reject(new Error(`CDP timeout: ${method}`))}},10000).unref()})
  await send('Page.enable');await send('Runtime.enable');const evalInPage=async(expression)=>{const r=await send('Runtime.evaluate',{expression,returnByValue:true,awaitPromise:true});if(r.exceptionDetails)throw new Error(r.exceptionDetails.exception?.description||r.exceptionDetails.text);return r.result.value}
  await send('Page.navigate',{url:`${base}/csm14-refresh-harness.html`})
  for(let i=0;i<120;i++){if(await evalInPage("document.querySelectorAll('fieldset').length===3"))break;await wait(100)}
  if(cap==='CAP-638'){
    assert.equal(await evalInPage("document.querySelector('h2')?.textContent"),'Review comparison refresh')
    assert.equal(await evalInPage("document.querySelector('button[aria-disabled=true]') !== null"),false)
    assert.equal(await evalInPage("document.querySelectorAll('input[type=radio]').length"),2)
    assert.equal(await evalInPage("document.querySelector('button').disabled"),true,'unavailable source keeps accept blocked')
    await evalInPage("document.querySelectorAll('input[type=radio]')[1].click()")
    assert.equal(await evalInPage("document.querySelector('button').disabled"),true,'unavailable source remains blocked after resolving one conflict')
    await evalInPage("window.__setCsm14Fingerprint('SYNTHETIC_FINGERPRINT_21')");await wait(100)
    assert.equal(await evalInPage("[...document.querySelectorAll('input[type=radio]')].some(x=>x.checked)"),false,'new preview fingerprint requires review again')
    await evalInPage('window.__setCsm14Stale()');await wait(100)
    assert.equal(await evalInPage("document.querySelector('[role=alert]')?.textContent.includes('changed')"),true)
    assert.equal(await evalInPage("document.querySelector('button').disabled"),true)
    assert.equal(await evalInPage('localStorage.length'),0)
    if(process.argv.includes('--instructor-shell')) console.log('CAP-638 instructor browser shell checks PASS (accessible shell, blocked unsafe accept, no persistent private cache).')
    else throw new Error('CAP-638 learner acceptance is incomplete: unavailable/review-fingerprint scenarios remain blocked at the learner-owned decision seam')
  } else if(cap==='CAP-639') {
    const result=await evalInPage("import('/src/coverstory/shortlist/refresh/refreshRecovery.ts').then(m=>m.recoverRefresh({accept:async()=>({outcome:'ACCEPTED',revision:2,code:'OK'}),readCurrent:async()=>2},{ownerScope:'SYNTHETIC_OWNER_A',shortlistId:'SYNTHETIC_SHORTLIST_A',operationKey:'SYNTHETIC_KEY',canonicalRequestSha256:'a'.repeat(64)},{})).then(()=> 'resolved').catch(e=>String(e.message))")
    assert.match(result,/TODO\(CAP-639\)/,'expected learner TODO should be visible')
    throw new Error('CAP-639 acceptance failure: learner recovery seam still reaches its TODO')
  } else if(cap==='CAP-640') {
    const result=await evalInPage("import('/src/coverstory/shortlist/refresh/refreshOwnerBoundary.ts').then(m=>m.acceptOwnerResponse('SYNTHETIC_OWNER_B',{ownerId:'SYNTHETIC_OWNER_A',value:{shortlistId:'SYNTHETIC_SHORTLIST_A'},private:true})).then(x=>String(x)).catch(e=>String(e.message))")
    assert.match(result,/TODO\(CAP-640\)/,'expected learner owner-boundary TODO should be visible')
    throw new Error('CAP-640 acceptance failure: late private response still reaches its owner-boundary TODO')
  } else {
    const result=await evalInPage("import('/src/coverstory/shortlist/refresh/decodeCheckpointProvenance.ts').then(m=>m.decodeProvenance({schemaMajor:1})).then(x=>x).catch(e=>String(e.message))")
    assert.match(result,/TODO\(CAP-641\)/,'expected learner TODO should be visible')
    throw new Error('CAP-641 acceptance failure: provenance decoder still reaches its TODO')
  }
} catch(error){console.error(`${cap} learner acceptance: ${error.stack||error}`);process.exitCode=1}
finally{try{ws?.close()}catch{}for(const p of [chrome,vite])if(p&&!p.killed){p.kill('SIGTERM');await wait(150);if(!p.killed)p.kill('SIGKILL')}await rm(profile,{recursive:true,force:true})}
