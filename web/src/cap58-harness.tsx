import { BoundedTelemetryQueue, type LowSensitivityEvent } from './coverstory/analytics/BoundedTelemetryQueue'

const queue = new BoundedTelemetryQueue()
const state = document.querySelector<HTMLParagraphElement>('#queue-state')!
const evidence = document.querySelector<HTMLPreElement>('#evidence')!
const checkout = document.querySelector<HTMLParagraphElement>('#checkout-state')!
const event = (n: number): LowSensitivityEvent => ({
  eventId: `58000000-0000-4000-8000-${String(n).padStart(12, '0')}`,
  eventType: 'PAGE_VIEW', schemaVersion: 1, pageId: 'health_discovery',
})

document.querySelector('#fill')!.addEventListener('click', () => {
  let accepted = 0
  for (let i = 1; i <= 21; i++) if (queue.enqueue(event(i))) accepted++
  state.textContent = `Queue contains ${queue.size}; accepted ${accepted}; rejected ${21 - accepted}`
})
document.querySelector('#checkout')!.addEventListener('click', () => {
  const count = Number(checkout.dataset.count ?? '0') + 1
  checkout.dataset.count = String(count)
  checkout.textContent = `Checkout actions: ${count}`
})
document.querySelector('#flush')!.addEventListener('click', () => {
  void queue.flush(async () => { await new Promise((r) => setTimeout(r, 20)); return 429 })
    .then((result) => {
      state.textContent = `Queue contains ${queue.size}; dropped ${result.dropped} after retry limit`
      evidence.textContent = JSON.stringify({ ...result, queuedAfterFlush: queue.size, checkoutActionsRemainAvailable: true })
    })
})
