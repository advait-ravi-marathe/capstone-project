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
  const pressSpace=async()=>{await send('Input.dispatchKeyEvent',{type:'keyDown',key:' ',code:'Space',windowsVirtualKeyCode:32});await send('Input.dispatchKeyEvent',{type:'keyUp',key:' ',code:'Space',windowsVirtualKeyCode:32})}
  await send('Page.navigate',{url:`${base}/csm14-refresh-harness.html`})
  for(let i=0;i<120;i++){if(await evalInPage("document.querySelectorAll('fieldset').length===3"))break;await wait(100)}
  if(cap==='CAP-638'){
    assert.equal(await evalInPage("document.querySelector('h2')?.textContent"),'Review comparison refresh')
    assert.equal(await evalInPage("document.querySelectorAll('input[type=radio]').length"),2)
    assert.equal(await evalInPage("document.querySelector('button').textContent.trim()"),'Accept reviewed refresh')
    assert.equal(await evalInPage("document.querySelector('button').disabled"),true,'unavailable source keeps accept blocked')
    await evalInPage("document.querySelectorAll('input[type=radio]')[1].focus()");await pressSpace()
    assert.equal(await evalInPage("document.querySelector('button').disabled"),true,'unavailable source remains blocked after resolving one conflict')
    await evalInPage("window.__setCsm14Fingerprint('SYNTHETIC_FINGERPRINT_21')");await wait(100)
    assert.equal(await evalInPage("[...document.querySelectorAll('input[type=radio]')].some(x=>x.checked)"),false,'new preview fingerprint requires review again')
    await evalInPage('window.__setCsm14Stale()');await wait(100)
    assert.equal(await evalInPage("document.querySelector('[role=alert]')?.textContent.includes('changed')"),true)
    assert.equal(await evalInPage("document.querySelector('button').disabled"),true)
    assert.equal(await evalInPage('localStorage.length'),0)
    await evalInPage('window.__setCsm14ReadyPreview()');await wait(100)
    assert.equal(await evalInPage("document.querySelector('button').disabled"),true,'new preview begins unresolved')
    await evalInPage("document.querySelectorAll('input[type=radio]')[1].focus()");await pressSpace();await wait(50)
    assert.equal(await evalInPage("document.querySelector('button').disabled"),false,'a complete conflict decision enables acceptance')
    await evalInPage("document.querySelector('button').click()");await wait(50)
    const accepted=await evalInPage('window.__acceptedCsm14()')
    assert.equal(accepted.fingerprint,'SYNTHETIC_FINGERPRINT_READY')
    assert.deepEqual(accepted.decisions.map(x=>x.slotId),['s2'])
    assert.equal(accepted.decisions[0].choice.versionId,'v3')
    assert.equal(accepted.decisions[0].choice.sourceSha256.length,64,'resolution retains the exact source version reference')
    console.log('CAP-638 browser state subset PASS (blocked unavailable/stale states; explicit decision enables the shell callback).')
  } else if(cap==='CAP-639') {
    const result=await evalInPage(`import('/src/coverstory/shortlist/refresh/refreshRecovery.ts').then(async m=>{
      const op={ownerScope:'SYNTHETIC_OWNER_A',shortlistId:'SYNTHETIC_SHORTLIST_A',operationKey:'SYNTHETIC_KEY_639',canonicalRequestSha256:'a'.repeat(64)};let calls=[],reads=0,head=1,effects=0
      const request={fingerprint:'FP-1',decisions:[{slotId:'s2',versionId:'v3'}]};const store={key:op.operationKey,hash:op.canonicalRequestSha256,request:structuredClone(request),reply:{outcome:'ACCEPTED',revision:2,code:'OK'}}
      head=2;effects=1 // The prior request committed, but its response was lost before the user's explicit retry.
      const recovered=await m.recoverRefresh({accept:async(operation,payload)=>{calls.push({operation:{...operation},payload:structuredClone(payload)});return operation.operationKey===store.key&&operation.canonicalRequestSha256===store.hash&&JSON.stringify(payload)===JSON.stringify(store.request)?store.reply:{outcome:'CONFLICT',code:'IDEMPOTENCY_KEY_REUSED'}},readCurrent:async()=>{reads++;return head}},op,request)
      let conflicts=0,conflictReads=0;const rejected=await m.recoverRefresh({accept:async()=>{conflicts++;return {outcome:'CONFLICT',code:'STALE_PREVIEW'}},readCurrent:async()=>{conflictReads++;return 3}},op,{fingerprint:'OLD'})
      return {recovered,calls,reads,rejected,conflicts,conflictReads,head,effects}
    }).catch(e=>({error:String(e.message)}))`)
    assert.equal(result.error,undefined,'unknown delivery should be reconciled through the same operation')
    assert.deepEqual(result.recovered,{state:'SETTLED',currentRevision:2})
    assert.equal(result.calls.length,1,"the user's retry replays once; recovery does not automatically mint/retry extra commands")
    assert.equal(result.calls[0].operation.operationKey,'SYNTHETIC_KEY_639');assert.equal(result.calls[0].operation.canonicalRequestSha256,'a'.repeat(64));assert.deepEqual(result.calls[0].payload,{fingerprint:'FP-1',decisions:[{slotId:'s2',versionId:'v3'}]})
    assert.equal(result.reads,1,'after confirmed replay, refetch authoritative persisted head');assert.equal(result.head,2);assert.equal(result.effects,1,'replay observes prior durable effect, it does not append another')
    assert.deepEqual(result.rejected,{state:'FAILED'});assert.equal(result.conflicts,1);assert.equal(result.conflictReads,0,'definite stale rejection must not be treated as a lost response')
    console.log('CAP-639 browser acceptance PASS (same-key lost-response replay/refetch; definite stale rejection is not retried).')
  } else if(cap==='CAP-640') {
    const result=await evalInPage("import('/src/coverstory/shortlist/refresh/refreshOwnerBoundary.ts').then(m=>({late:m.acceptOwnerResponse('SYNTHETIC_OWNER_B',{ownerId:'SYNTHETIC_OWNER_A',value:{shortlistId:'SYNTHETIC_SHORTLIST_A',private:true},private:true}),current:m.acceptOwnerResponse('SYNTHETIC_OWNER_A',{ownerId:'SYNTHETIC_OWNER_A',value:{shortlistId:'SYNTHETIC_SHORTLIST_A',private:true},private:true}),signedOut:m.acceptOwnerResponse(null,{ownerId:'SYNTHETIC_OWNER_A',value:{secret:'synthetic'},private:true})})).catch(e=>({error:String(e.message)}))")
    assert.equal(result.error,undefined,'owner response boundary should not throw for rejected stale responses')
    assert.equal(result.late,null,'late response from previous account must be discarded')
    assert.equal(result.signedOut,null,'private response after sign-out must be discarded')
    assert.deepEqual(result.current,{shortlistId:'SYNTHETIC_SHORTLIST_A',private:true},'current owner may consume its response')
    assert.equal(await evalInPage('localStorage.length'),0,'private responses are not persisted in local storage')
    console.log('CAP-640 browser acceptance PASS (current owner accepted; switched owner and signed-out responses discarded).')
  } else {
    const result=await evalInPage("import('/src/coverstory/shortlist/refresh/decodeCheckpointProvenance.ts').then(m=>{const unknown=m.decodeProvenance({schemaMajor:1}),upstream=m.decodeProvenance({schemaMajor:1,provenance:'UPSTREAM_REFRESH'}),conflict=m.decodeProvenance({schemaMajor:1,provenance:'EXPLICIT_CONFLICT_CHOICE'});let unsupported=false;try{m.decodeProvenance({schemaMajor:99,provenance:'UPSTREAM_REFRESH'})}catch{unsupported=true}return {unknown,upstream,conflict,unsupported}}).catch(e=>({error:String(e.message)}))")
    assert.equal(result.error,undefined,'known schema should decode without an error')
    assert.equal(result.unknown,'UNKNOWN','historical missing provenance remains unknown')
    assert.equal(result.upstream,'UPSTREAM_REFRESH');assert.equal(result.conflict,'EXPLICIT_CONFLICT_CHOICE')
    assert.equal(result.unsupported,true,'unsupported required major must fail closed')
    console.log('CAP-641 browser acceptance PASS (known provenance decoded, absent value unknown, unsupported major rejected).')
  }
} catch(error){console.error(`${cap} learner acceptance: ${error.stack||error}`);process.exitCode=1}
finally{try{ws?.close()}catch{}for(const p of [chrome,vite])if(p&&!p.killed){p.kill('SIGTERM');await wait(150);if(!p.killed)p.kill('SIGKILL')}await rm(profile,{recursive:true,force:true})}
