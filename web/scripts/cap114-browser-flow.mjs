import assert from 'node:assert/strict'
import { spawn } from 'node:child_process'
import { createServer } from 'node:net'
import { mkdtemp, rm } from 'node:fs/promises'
import { tmpdir } from 'node:os'
import { join } from 'node:path'

const root = new URL('..', import.meta.url).pathname
const apiTarget = process.argv[2]
assert.ok(apiTarget, 'pass the live Spring instructor-test URL')
const wait = ms => new Promise(resolve => setTimeout(resolve, ms))
const freePort = async () => await new Promise((resolve, reject) => { const server = createServer(); server.once('error', reject); server.listen(0, '127.0.0.1', () => { const port = server.address().port; server.close(() => resolve(port)) }) })
const stop = async (child, signal) => {
  if (!child || child.exitCode !== null) return
  const exited = new Promise(resolve => child.once('exit', resolve))
  child.kill(signal); await Promise.race([exited, wait(3000)])
  if (child.exitCode === null) child.kill('SIGKILL')
}
const port = await freePort(), profile = await mkdtemp(join(tmpdir(), 'cap114-chrome-'))
let vite, chrome, socket
try {
  vite = spawn('npm', ['run', 'dev', '--', '--host', '127.0.0.1', '--port', String(port), '--strictPort'], {
    cwd: root, stdio: 'ignore', env: { ...process.env, COVERSTORY_API_TARGET: apiTarget, npm_config_cache: '/tmp/npm-cache' },
  })
  const base = `http://127.0.0.1:${port}`
  for (let i = 0; i < 100; i++) { try { if ((await fetch(`${base}/cap114-harness.html`)).ok) break } catch {} await wait(100) }
  chrome = spawn(process.env.COVERSTORY_CHROMIUM?.trim() || 'chromium', ['--headless=new', '--no-sandbox', '--disable-gpu', '--disable-dev-shm-usage', '--remote-debugging-port=0', `--user-data-dir=${profile}`, 'about:blank'], { stdio: ['ignore', 'ignore', 'pipe'] })
  chrome.stderr.setEncoding('utf8'); let stderr = ''; chrome.stderr.on('data', value => stderr += value)
  let endpoint
  for (let i = 0; i < 100 && !endpoint; i++) { const match = stderr.match(/DevTools listening on (ws:\/\/[^\s]+)/); if (match) endpoint = `http://127.0.0.1:${new URL(match[1]).port}`; else await wait(100) }
  assert.ok(endpoint, 'Chromium did not start')
  let target
  for (let i = 0; i < 50 && !target; i++) { try { const response = await fetch(`${endpoint}/json/new?about:blank`, { method: 'PUT' }); if (response.ok) target = await response.json() } catch {} await wait(100) }
  socket = new WebSocket(target.webSocketDebuggerUrl); await new Promise(resolve => socket.addEventListener('open', resolve, { once: true }))
  let sequence = 0; const pending = new Map()
  socket.addEventListener('message', event => { const message = JSON.parse(event.data); if (message.id && pending.has(message.id)) { pending.get(message.id)(message.result); pending.delete(message.id) } })
  const send = (method, params = {}) => new Promise(resolve => { const id = ++sequence; pending.set(id, resolve); socket.send(JSON.stringify({ id, method, params })) })
  await send('Page.enable'); await send('Runtime.enable')
  const evaluate = async expression => (await send('Runtime.evaluate', { expression, returnByValue: true, awaitPromise: true })).result.value
  await send('Page.navigate', { url: `${base}/cap114-harness.html` })
  const text = selector => evaluate(`document.querySelector('[data-testid="${selector}"]').textContent`)
  const click = async selector => { await evaluate(`document.querySelector('[data-testid="${selector}"]').click()`); await wait(100) }
  const until = async (selector, predicate, message) => { for (let i = 0; i < 100; i++) { const value = await text(selector); if (predicate(value)) return value; await wait(50) } assert.fail(`Timed out: ${message}; got ${await text(selector)}`) }
  await until('alert-state', value => value === 'NORMAL', 'initial alert state')
  await click('six-window'); await until('window-count', value => value.includes('1'), 'first qualifying window')
  assert.equal(await text('alert-state'), 'PENDING')
  await click('repeat-window'); await until('duplicate-result', value => value === 'Duplicate ignored', 'idempotent repeated window')
  assert.match(await text('window-count'), /1$/)
  await click('six-window'); await until('window-count', value => value.includes('2'), 'second qualifying window')
  assert.equal(await text('alert-state'), 'PENDING')
  await click('six-window'); await until('alert-state', value => value === 'FIRING', 'third qualifying window')
  const qualifying = ['PENDING', 'PENDING', 'FIRING']
  assert.match(await text('firing-count'), /1$/)
  assert.match(await text('next-action'), /not rejection or issuance/)
  assert.match(await text('requested-fields'), /policy_start_date, income_band/)
  assert.equal(await text('payment-status'), 'Payment: NOT_STARTED; dispatch confirmed: false')
  await click('healthy-window'); await until('alert-state', value => value === 'RESOLVED', 'healthy window resolution')
  const resolved = await text('alert-state')
  assert.match(await text('resolution-count'), /1$/)
  await click('reset'); await until('window-count', value => value.endsWith('0'), 'exact-five scenario reset')
  for (let i = 0; i < 3; i++) await click('five-window')
  await until('window-count', value => value.includes('3'), 'three exact-five windows')
  const exactFive = await text('alert-state'); assert.equal(exactFive, 'NORMAL')
  assert.match(await text('firing-count'), /0$/)
  await click('reset'); await until('window-count', value => value.endsWith('0'), 'low-volume scenario reset')
  await click('low-window'); await until('window-count', value => value.includes('1'), 'low-volume window')
  assert.equal(await text('alert-state'), 'NORMAL')
  await click('healthy-window'); await until('window-count', value => value.includes('2'), 'healthy window after low volume')
  const lowThenHealthy = await text('alert-state'); assert.equal(lowThenHealthy, 'NORMAL')
  assert.match(await text('resolution-count'), /0$/)
  console.log('CAP114_BROWSER_FLOW ' + JSON.stringify({ qualifying, resolved, exactFive, lowThenHealthy, applicationReviewStatus: 'NEEDS_INFORMATION', paymentStatus: 'NOT_STARTED', dispatchConfirmed: false, duplicateIgnored: true }))
} finally {
  socket?.close(); await stop(chrome, 'SIGTERM'); await stop(vite, 'SIGTERM')
  for (let i = 0; i < 10; i++) { try { await rm(profile, { recursive: true, force: true }); break } catch (error) { if (i === 9) throw error; await wait(100) } }
}
