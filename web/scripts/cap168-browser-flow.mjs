import assert from 'node:assert/strict'
import { spawn } from 'node:child_process'
import { createServer } from 'vite'
import { mkdtemp, readFile, rm } from 'node:fs/promises'
import { tmpdir } from 'node:os'
import { join } from 'node:path'

const vite = await createServer({ configFile: 'vite.config.ts', server: { host: '127.0.0.1', port: 0, strictPort: false } })
let browser
let profile
let socket
try {
  await vite.listen()
  const port = vite.httpServer.address().port
  profile = await mkdtemp(join(tmpdir(), 'coverstory-cap168-chrome-'))
  browser = spawn('/usr/bin/chromium', ['--headless=new', '--no-sandbox', '--disable-gpu', '--disable-dev-shm-usage',
    '--remote-allow-origins=*', `--user-data-dir=${profile}`, '--remote-debugging-port=0', 'about:blank'], { stdio: 'ignore' })
  const endpointFile = join(profile, 'DevToolsActivePort')
  let endpoint
  for (let i = 0; i < 100 && !endpoint; i++) {
    try { endpoint = (await readFile(endpointFile, 'utf8')).split('\n')[0] } catch { await new Promise((resolve) => setTimeout(resolve, 100)) }
  }
  assert.ok(endpoint, 'Chromium debugging endpoint started')
  const debugPort = Number(endpoint)
  const targetResponse = await fetch(`http://127.0.0.1:${debugPort}/json/new?about:blank`, { method: 'PUT' })
  const target = await targetResponse.json()
  socket = new WebSocket(target.webSocketDebuggerUrl)
  await new Promise((resolve, reject) => { socket.onopen = resolve; socket.onerror = reject })
  let id = 0
  const pending = new Map()
  socket.onmessage = (event) => {
    const message = JSON.parse(event.data)
    if (message.id && pending.has(message.id)) {
      const { resolve, reject } = pending.get(message.id); pending.delete(message.id)
      if (message.error) reject(new Error(message.error.message)); else resolve(message.result)
    }
  }
  const cdp = (method, params = {}) => new Promise((resolve, reject) => {
    const callId = ++id; pending.set(callId, { resolve, reject }); socket.send(JSON.stringify({ id: callId, method, params }))
  })
  const source = String.raw`
    (() => {
      const json = (value, status = 200) => new Response(status === 204 ? null : JSON.stringify(value), {
        status, headers: { 'Content-Type': 'application/json', 'Cache-Control': 'no-store' }
      })
      const offers = [1,2,3].map((n) => ({ offerId: '10000000-0000-4000-8000-00000000000' + n,
        catalogVersion: 'v1', displayName: 'Example ' + String.fromCharCode(64+n), category: 'health',
        evidenceStatus: 'SYNTHETIC', waitingPeriodStatus: 'UNKNOWN', copayStatus: 'UNKNOWN',
        networkStatus: 'UNKNOWN', sourceLabel: 'Synthetic layout example' }))
      const store = () => JSON.parse(localStorage.getItem('__cap168_saved') || '{}')
      const current = () => sessionStorage.getItem('__cap168_account')
      const owner = () => current() === 'A' ? 'a1680000-0000-4000-8000-000000000001' : 'b1680000-0000-4000-8000-000000000002'
      const original = window.fetch.bind(window)
      window.fetch = async (input, init = {}) => {
        const url = new URL(typeof input === 'string' ? input : input.url, location.href)
        if (!url.pathname.startsWith('/api/')) return original(input, init)
        if (url.pathname === '/api/session') return current() ? json({ ownerId: owner() }) : json({}, 401)
        if (url.pathname === '/api/demo/session' && init.method === 'POST') {
          const account = JSON.parse(init.body).account
          sessionStorage.setItem('__cap168_account', account); return json({ ownerId: owner() })
        }
        if (url.pathname === '/api/demo/logout') { sessionStorage.removeItem('__cap168_account'); return json({}, 204) }
        if (url.pathname === '/api/catalog') return json({ schemaMajor: 1, offers })
        if (url.pathname === '/api/v1/me/journeys') return current() ? json({ journeys: [], nextCursor: null }) : json({}, 401)
        if (url.pathname === '/api/v1/me/shortlist' && init.method !== 'POST')
          return current() ? json({ schemaMajor: 1, items: store()[current()] || [] }) : json({}, 401)
        if (url.pathname === '/api/v1/me/shortlist/items' && init.method === 'POST') {
          if (!current()) return json({}, 401)
          const data = store(); const row = JSON.parse(init.body); data[current()] = data[current()] || []
          if (!data[current()].some((item) => item.offerId === row.offerId)) data[current()].push(row)
          localStorage.setItem('__cap168_saved', JSON.stringify(data)); return json({ saved: true }, 201)
        }
        if (url.pathname === '/api/v1/me/shortlist/items' && init.method === 'DELETE') {
          const data = store(); data[current()] = (data[current()] || []).filter((item) => item.offerId !== url.searchParams.get('offerId'))
          localStorage.setItem('__cap168_saved', JSON.stringify(data)); return json({}, 204)
        }
        if (url.pathname === '/api/catalog/source-freshness') return json({ code: 'FRESHNESS_NOT_IMPLEMENTED' }, 501)
        if (url.pathname === '/api/v1/me/quote-validation') return json({ code: 'QUOTE_REVIEW_UNAVAILABLE' }, 503)
        if (url.pathname === '/api/shortlist/selection-order') return json({ code: 'NOT_IMPLEMENTED' }, 501)
        return json({ code: 'NOT_FOUND' }, 404)
      }
      const waitFor = async (condition, label) => {
        for (let i=0; i<120; i++) { if (condition()) return; await new Promise((resolve) => setTimeout(resolve, 100)) }
        throw new Error('Timed out: ' + label)
      }
      const button = (name) => [...document.querySelectorAll('button')].find((item) => item.textContent.includes(name))
      const run = async () => {
        try {
          if (sessionStorage.getItem('__cap168_phase') === 'after-refresh') {
            await waitFor(() => document.querySelectorAll('.offer-card').length === 3, 'catalog after refresh')
            await waitFor(() => document.querySelector('.session-label')?.textContent?.includes('Signed in') || document.querySelector('.demo-session')?.textContent?.includes('Synthetic account A'), 'A session restored')
            await waitFor(() => document.querySelectorAll('.saved-shortlist-item').length === 1, 'owned shortlist refetched')
            if (!document.querySelector('.offer-card input[type=checkbox]')?.checked) throw new Error('device-local selection did not survive login and refresh')
            if (!document.querySelector('.saved-shortlist-item')?.textContent?.includes('10000000-0000-4000-8000-000000000001')) throw new Error('saved owner A row missing after refresh')
            button('Switch demo account')?.click()
            await waitFor(() => document.querySelector('.demo-session')?.textContent?.includes('Synthetic account B'), 'account B active')
            await waitFor(() => document.body.textContent.includes('No saved examples for this account.'), 'B shortlist loaded')
            if (document.querySelector('.saved-shortlist-item')?.textContent?.includes('10000000-0000-4000-8000-000000000001')) throw new Error('owner A shortlist leaked to B')
            document.body.dataset.cap168 = 'PASS'
            return
          }
          await waitFor(() => document.querySelectorAll('.offer-card').length === 3, 'synthetic catalog')
          document.querySelector('.offer-card input[type=checkbox]')?.click()
          await waitFor(() => JSON.parse(sessionStorage.getItem('coverstory.public-selections') || '[]').length === 1, 'device-local selection stored')
          button('Continue as demo A')?.click()
          await waitFor(() => current() === 'A' && document.querySelector('.demo-session')?.textContent?.includes('Synthetic account A'), 'A login return')
          button('Save to owned shortlist')?.click()
          await waitFor(() => document.querySelector('.saved-shortlist-item')?.textContent?.includes('10000000-0000-4000-8000-000000000001'), 'A shortlist saved')
          sessionStorage.setItem('__cap168_phase', 'after-refresh')
          location.reload()
        } catch (error) { document.body.dataset.cap168 = 'FAIL:' + String(error) }
      }
      setTimeout(() => void run(), 0)
    })()
  `
  await cdp('Page.enable')
  await cdp('Runtime.enable')
  await cdp('Page.addScriptToEvaluateOnNewDocument', { source })
  await cdp('Page.navigate', { url: `http://127.0.0.1:${port}/` })
  let outcome
  for (let i = 0; i < 240; i++) {
    const result = await cdp('Runtime.evaluate', { expression: 'document.body?.dataset.cap168 || ""', returnByValue: true })
    outcome = result.result.value
    if (outcome) break
    await new Promise((resolve) => setTimeout(resolve, 100))
  }
  assert.equal(outcome, 'PASS', `browser flow result: ${outcome}`)
  console.log('CAP-168 Chromium demo flow passed: login return, shortlist persistence, refresh restore, and owner isolation.')
} finally {
  if (socket) socket.close()
  if (browser) { browser.kill('SIGTERM'); await new Promise((resolve) => browser.once('exit', resolve)) }
  if (profile) await rm(profile, { recursive: true, force: true })
  await vite.close()
}
