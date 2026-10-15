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
const stop = async child => { if (!child || child.exitCode !== null) return; const exited = new Promise(resolve => child.once('exit', resolve)); child.kill('SIGTERM'); await Promise.race([exited, wait(3000)]); if (child.exitCode === null) child.kill('SIGKILL') }
const port = await freePort(), profile = await mkdtemp(join(tmpdir(), 'cap115-chrome-'))
let vite, chrome, socket
try {
  vite = spawn('npm', ['run', 'dev', '--', '--host', '127.0.0.1', '--port', String(port), '--strictPort'], {
    cwd: root, stdio: 'ignore', env: { ...process.env, COVERSTORY_API_TARGET: apiTarget, npm_config_cache: '/tmp/npm-cache' },
  })
  const base = `http://127.0.0.1:${port}`
  for (let i = 0; i < 100; i++) { try { if ((await fetch(`${base}/cap115-harness.html`)).ok) break } catch {} await wait(100) }
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
  await send('Page.navigate', { url: `${base}/cap115-harness.html` })
  const text = selector => evaluate(`document.querySelector('[data-testid="${selector}"]').textContent`)
  const click = async selector => { await evaluate(`document.querySelector('[data-testid="${selector}"]').click()`); await wait(100) }
  const until = async (selector, predicate, message) => { for (let i = 0; i < 100; i++) { const value = await text(selector); if (typeof value === 'string' && predicate(value)) return value; await wait(50) } assert.fail(`Timed out: ${message}; got ${await text(selector)}`) }
  await until('original-offer', value => value.includes('₹1,000.00'), 'original amount')
  assert.match(await text('revised-offer'), /₹1,100.00/); assert.match(await text('amount-delta'), /₹100.00/)
  assert.match(await text('terms'), /copay_0_percent, copay_10_percent, restore_benefit/)
  await click('supported'); await until('validation', value => value.includes('SUPPORTED'), 'authorized answer')
  assert.match(await text('citations'), /offer-source-a@v2/)
  const outcomes = {}
  for (const [button, code] of [['foreign','UNAUTHORIZED_CITATION'],['invented','UNKNOWN_FIELD'],['missing-status','MISSING_STATUS'],['stale','STALE_OR_FOREIGN_REVISION']]) {
    await click(button); await until('validation', value => value.includes(code), code); outcomes[button] = code
    assert.match(await text('amount-delta'), /₹100.00/, 'deterministic difference remains visible after rejection')
  }
  await click('abstain'); await until('validation', value => value.includes('INSUFFICIENT_EVIDENCE'), 'valid abstention')
  assert.match(await text('citations'), /^$/); assert.match(await text('decision-count'), /0$/)
  const before = await text('payment-state')
  await click('accept'); await until('choice-result', value => value.includes('ACCEPT_OFFER'), 'explicit user quote choice')
  assert.match(await text('decision-count'), /1$/); assert.equal(await text('payment-state'), before)
  const choice = await text('choice-result')
  console.log('CAP115_BROWSER_FLOW ' + JSON.stringify({ original: '100000 INR paise', revised: '110000 INR paise', delta: '10000 INR paise', outcomes,
    abstention: 'INSUFFICIENT_EVIDENCE', decisionBeforeExplicitClick: 0, choice, paymentStateUnchanged: true }))
} finally {
  socket?.close(); await stop(chrome); await stop(vite)
  for (let i = 0; i < 10; i++) { try { await rm(profile, { recursive: true, force: true }); break } catch (error) { if (i === 9) throw error; await wait(100) } }
}
