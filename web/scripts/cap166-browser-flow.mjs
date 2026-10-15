import assert from 'node:assert/strict'
import { spawn } from 'node:child_process'
import { createServer } from 'node:net'
import { mkdtemp, rm } from 'node:fs/promises'
import { tmpdir } from 'node:os'
import { join } from 'node:path'
import readline from 'node:readline'

const apiTarget = process.argv[2]
if (!apiTarget) throw new Error('Usage: node scripts/cap166-browser-flow.mjs <spring-api-url>')
const decoderMode = process.argv[3] ?? 'instructor'
if (!['instructor', 'learner'].includes(decoderMode)) throw new Error('decoder mode must be instructor or learner')
const runMode = process.argv[4] ?? 'full'
if (!['full', 'todo-smoke'].includes(runMode)) throw new Error('run mode must be full or todo-smoke')
const browserExecutable = process.env.COVERSTORY_CHROMIUM?.trim() || 'chromium'
const root = new URL('..', import.meta.url).pathname
const freePort = async () => await new Promise((resolve, reject) => {
  const s = createServer(); s.once('error', reject); s.listen(0, '127.0.0.1', () => { const p = s.address().port; s.close(() => resolve(p)) })
})
const wait = (ms) => new Promise((resolve) => setTimeout(resolve, ms))
const webPort = await freePort()
const profile = await mkdtemp(join(tmpdir(), 'cap166-chrome-'))
let vite, chrome, cdp, commandReader
const chromeLogs = []
try {
  vite = spawn('npm', ['run', 'dev', '--', '--host', '127.0.0.1', '--port', String(webPort), '--strictPort'], {
    cwd: root, env: { ...process.env, COVERSTORY_API_TARGET: apiTarget, npm_config_cache: '/tmp/capstone-npm-cache' },
    stdio: ['ignore', 'ignore', 'ignore'],
  })
  const baseUrl = `http://127.0.0.1:${webPort}`
  let up = false
  for (let i = 0; i < 100; i++) { try { const r = await fetch(`${baseUrl}/cap166-harness.html`); if (r.ok) { up = true; break } } catch {} await wait(100) }
  assert.ok(up, 'Vite instructor harness did not start')

  chrome = spawn(browserExecutable, ['--headless=new', '--no-sandbox', '--disable-gpu', '--disable-dev-shm-usage',
    '--remote-debugging-port=0', `--user-data-dir=${profile}`, 'about:blank'], { stdio: ['ignore', 'ignore', 'pipe'] })
  chrome.stderr.setEncoding('utf8')
  let stderr = ''
  chrome.stderr.on('data', (x) => { stderr += x; chromeLogs.push(x); if (chromeLogs.length > 80) chromeLogs.shift() })
  let browserWs
  for (let i = 0; i < 100 && !browserWs; i++) {
    browserWs = stderr.match(/DevTools listening on (ws:\/\/[^\s]+)/)?.[1]
    if (!browserWs) await wait(100)
  }
  assert.ok(browserWs, `Chromium DevTools did not start: ${stderr.slice(-1200)}`)
  const wsUrl = new URL(browserWs)
  const cdpHttp = `http://127.0.0.1:${wsUrl.port}`
  let target
  for (let i = 0; i < 50; i++) { try { const r = await fetch(`${cdpHttp}/json/new?about:blank`, { method: 'PUT' }); if (r.ok) { target = await r.json(); break } } catch {} await wait(100) }
  assert.ok(target?.webSocketDebuggerUrl, 'Chromium target did not open')
  cdp = new WebSocket(target.webSocketDebuggerUrl)
  await new Promise((resolve, reject) => { cdp.addEventListener('open', resolve, { once: true }); cdp.addEventListener('error', reject, { once: true }) })
  let nextId = 0
  const pending = new Map()
  cdp.addEventListener('message', (event) => {
    const m = JSON.parse(event.data)
    if (m.id && pending.has(m.id)) { const p = pending.get(m.id); pending.delete(m.id); m.error ? p.reject(new Error(m.error.message)) : p.resolve(m.result) }
  })
  const send = (method, params = {}) => new Promise((resolve, reject) => {
    const id = ++nextId; pending.set(id, { resolve, reject }); cdp.send(JSON.stringify({ id, method, params }))
    setTimeout(() => { if (pending.has(id)) { pending.delete(id); reject(new Error(`CDP timeout ${method}`)) } }, 8000).unref()
  })
  await send('Page.enable'); await send('Runtime.enable')
  await send('Page.addScriptToEvaluateOnNewDocument', { source: "Object.defineProperty(window,'localStorage',{configurable:true,get(){throw new Error('synthetic storage denied')}})" })
  const evaluate = async (expression) => {
    const r = await send('Runtime.evaluate', { expression, returnByValue: true, awaitPromise: true })
    if (r.exceptionDetails) throw new Error(r.exceptionDetails.exception?.description ?? r.exceptionDetails.text)
    return r.result.value
  }
  const harnessPath = (path = '') => `/cap166-harness.html${path}${path.includes('?') ? '&' : '?'}decoder=${decoderMode}`
  const navigate = async (path = '') => { await send('Page.navigate', { url: `${baseUrl}${harnessPath(path)}` }); await waitFor("document.readyState === 'complete'") }
  const waitFor = async (expression) => {
    for (let i = 0; i < 100; i++) { try { if (await evaluate(expression)) return } catch {} await wait(100) }
    throw new Error(`Browser condition timed out: ${expression}`)
  }
  if (decoderMode === 'learner' && runMode === 'todo-smoke') {
    await navigate()
    await waitFor("document.querySelector('.persisted-catalog [role=alert]')?.textContent.includes('could not be decoded') && document.querySelectorAll('.persisted-catalog input[type=checkbox]').length === 0")
    console.log('CAP166_LEARNER_TODO_SMOKE ' + JSON.stringify({ actualDecoder: 'decodeCatalogEnvelope', noPartialCatalog: true, retryableErrorVisible: true }))
  } else {
  await navigate()
  if (decoderMode === 'learner') {
    await waitFor("document.querySelectorAll('.persisted-catalog input[type=checkbox]').length === 3 || document.querySelector('.persisted-catalog [role=alert]')?.textContent.includes('could not be decoded')")
    if (await evaluate("document.querySelector('.persisted-catalog [role=alert]')?.textContent.includes('could not be decoded')")) {
      throw new Error('CAP166_LEARNER_ACCEPTANCE_TODO: actual decoder rejected the fixed v1 envelope; full success and recovery acceptance remains unmet')
    }
  } else {
    await waitFor("document.querySelectorAll('.persisted-catalog input[type=checkbox]').length === 3")
  }
  await evaluate("[...document.querySelectorAll('.persisted-catalog input[type=checkbox]')].slice(0,2).forEach(x=>x.click())")
  await waitFor("document.querySelector('.persisted-catalog .selection-toolbar strong')?.textContent === '2'")
  await evaluate("document.querySelector('.persisted-catalog .selection-toolbar button').click()")
  await waitFor("document.querySelectorAll('.persisted-catalog .compare-panel tbody tr').length === 4")
  await evaluate("[...document.querySelectorAll('.persisted-catalog button')].find(b=>b.textContent.includes('Export sanitized selection')).click()")
  await waitFor("window.__cap166SanitizedExport !== undefined")
  const storageDeniedExport = await evaluate('window.__cap166SanitizedExport')
  assert.deepEqual(storageDeniedExport, { schemaVersion: 1, selection: [
    { versionId: '16600000-0000-4000-8000-000000000001' },
    { versionId: '16600000-0000-4000-8000-000000000002' },
  ] })
  let page = await evaluate("document.querySelector('.persisted-catalog').innerText")
  assert.match(page, /RETIRED/); assert.match(page, /PUBLISHED/); assert.match(page, /Payable review is unavailable/)
  console.log('CAP166_BROWSER_POSITIVE ' + JSON.stringify({ decoderMode, liveApi: true, persistedRows: 3, retiredAndPublishedVisible: true, comparisonRows: 4, priceOrPayableAction: false, browserStorageDenied: true, sanitizedSelectionExport: true }))

  if (decoderMode === 'instructor') await navigate('?boundary')
  if (decoderMode === 'instructor') {
  await waitFor("document.querySelectorAll('.persisted-catalog input[type=checkbox]').length === 4")
  await waitFor("document.querySelector('.persisted-catalog').innerText.includes('Other categories or scenarios')")
  await evaluate("[...document.querySelectorAll('.persisted-catalog input[type=checkbox]')].slice(0,3).forEach(x=>x.click())")
  await waitFor("document.querySelector('.persisted-catalog .selection-toolbar strong')?.textContent === '3'")
  const fourthDisabled = await evaluate("document.querySelectorAll('.persisted-catalog input[type=checkbox]')[3].disabled")
  assert.equal(fourthDisabled, true, 'a fourth same-scenario selection must be disabled accessibly')
  console.log('CAP166_BROWSER_BOUNDARY ' + JSON.stringify({ threeSelections: true, fourthDisabled, nonHealthRejected: true }))
  }

  await navigate()
  await waitFor("document.querySelectorAll('.persisted-catalog input[type=checkbox]').length === 3")
  await evaluate("[...document.querySelectorAll('.persisted-catalog input[type=checkbox]')].slice(0,2).forEach(x=>x.click())")
  await evaluate("document.querySelector('.persisted-catalog .selection-toolbar button').click()")
  await waitFor("document.querySelector('.persisted-catalog .compare-panel') !== null")
  console.log('BROWSER_READY')

  commandReader = readline.createInterface({ input: process.stdin, crlfDelay: Infinity })
  for await (const line of commandReader) {
    if (line.trim() === 'OUTAGE') {
      await evaluate("document.querySelector('.persisted-catalog .refresh-persisted-catalog').click()")
      await waitFor("document.querySelector('.persisted-catalog').innerText.includes('Refresh failed')")
      const staleDisabled = await evaluate("document.querySelector('.persisted-catalog .selection-toolbar button').disabled")
      assert.equal(staleDisabled, true, 'cached comparison cannot continue after the Catalog service stops')
      await evaluate("[...document.querySelectorAll('.persisted-catalog button')].find(b=>b.textContent.includes('Export sanitized selection')).click()")
      await waitFor("window.__cap166SanitizedExport !== undefined")
      const exported = await evaluate('window.__cap166SanitizedExport')
      assert.deepEqual(exported, { schemaVersion: 1, selection: [
        { versionId: '16600000-0000-4000-8000-000000000001' },
        { versionId: '16600000-0000-4000-8000-000000000002' },
      ] })
      assert.equal(JSON.stringify(exported).includes('http'), false)
      await send('Page.reload', { ignoreCache: true })
      await waitFor("document.querySelector('.persisted-catalog').innerText.includes('Catalog history is unavailable')")
      assert.equal(await evaluate("document.querySelector('.persisted-catalog .compare-panel') === null"), true)
      console.log('CAP166_BROWSER_OUTAGE ' + JSON.stringify({ decoderMode, cachedComparisonDisabled: staleDisabled, sanitizedExportOffered: true, reloadHasNoStaleComparison: true }))
      console.log('OUTAGE_RECOVERY_VALIDATED')
      continue
    }
    if (line.trim() === 'RECOVER') {
      await navigate()
      await waitFor("document.querySelectorAll('.persisted-catalog input[type=checkbox]').length === 3")
      const rows = await evaluate("document.querySelector('.persisted-catalog').innerText")
      assert.match(rows, /RETIRED/); assert.match(rows, /PUBLISHED/)
      await evaluate("[...document.querySelectorAll('.persisted-catalog input[type=checkbox]')].slice(0,2).forEach(x=>x.click())")
      await waitFor("document.querySelector('.persisted-catalog .selection-toolbar strong')?.textContent === '2'")
      await evaluate("document.querySelector('.persisted-catalog .selection-toolbar button').click()")
      await waitFor("document.querySelectorAll('.persisted-catalog .compare-panel tbody tr').length === 4")
      console.log('CAP166_BROWSER_RECOVERED ' + JSON.stringify({ decoderMode, serviceRestarted: true, persistedRowsRestored: 3, retiredHistoryRestored: true, comparisonReenabled: true }))
      break
    }
  }
  }
} catch (error) {
  console.error(error?.stack ?? String(error));
  if (chromeLogs.length) console.error(chromeLogs.join('').slice(-1800));
  process.exitCode = 1
} finally {
  commandReader?.close()
  try { cdp?.close() } catch {}
  for (const proc of [chrome, vite]) if (proc && !proc.killed) { proc.kill('SIGTERM'); await wait(200); if (!proc.killed) proc.kill('SIGKILL') }
  await rm(profile, { recursive: true, force: true })
}
