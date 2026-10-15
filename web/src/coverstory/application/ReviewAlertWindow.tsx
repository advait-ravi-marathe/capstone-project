import { useEffect, useRef, useState } from 'react'

type Summary = {
  alertState: 'NORMAL' | 'PENDING' | 'FIRING' | 'RESOLVED'
  qualifyingWindows: number
  firingTransitions: number
  resolutionTransitions: number
  recordedWindows: number
  needsInformationCount: number
  applicationReviewStatus: 'NEEDS_INFORMATION' | 'REVIEW'
  requestedFieldCodes: string[]
  paymentStatus: 'NOT_STARTED' | 'PAID'
  dispatchConfirmed: boolean
  classification: 'SYNTHETIC_ONLY'
}

const instructorHeader = { 'X-CAP114-Instructor-Session': '11400000-0000-4000-8000-000000000999' }

export function ReviewAlertWindow() {
  const [summary, setSummary] = useState<Summary | null>(null)
  const [error, setError] = useState('')
  const sequence = useRef(0)
  const lastWindow = useRef<{ windowId: string; eligibleRequests: number; errorRequests: number; needsInformationCount: number } | null>(null)

  async function readSummary() {
    const response = await fetch('/api/support/__instructor/cap114/summary', { headers: instructorHeader, cache: 'no-store' })
    if (!response.ok) throw new Error(`Could not load synthetic review status (${response.status}).`)
    setSummary(await response.json() as Summary)
  }
  useEffect(() => { void readSummary().catch((e: unknown) => setError(e instanceof Error ? e.message : 'Summary unavailable.')) }, [])

  async function record(kind: 'six' | 'five' | 'low' | 'healthy' | 'duplicate') {
    let sample: { windowId: string; eligibleRequests: number; errorRequests: number; needsInformationCount: number }
    if (kind === 'duplicate') {
      if (!lastWindow.current) return
      sample = lastWindow.current
    } else {
      sequence.current += 1
      const values = kind === 'six' ? [100, 6, 1] : kind === 'five' ? [100, 5, 0] : kind === 'low' ? [2, 1, 1] : [100, 0, 0]
      sample = { windowId: `window-${sequence.current}`, eligibleRequests: values[0]!, errorRequests: values[1]!, needsInformationCount: values[2]! }
      lastWindow.current = sample
    }
    const response = await fetch('/api/support/__instructor/cap114/windows', {
      method: 'POST', headers: { ...instructorHeader, 'Content-Type': 'application/json' }, body: JSON.stringify(sample),
    })
    if (!response.ok) throw new Error(`Synthetic window was rejected (${response.status}).`)
    const result = await response.json() as Summary
    if ('duplicate' in result && result.duplicate) document.querySelector('[data-testid="duplicate-result"]')!.textContent = 'Duplicate ignored'
    await readSummary()
  }
  async function reset() {
    const response = await fetch('/api/support/__instructor/cap114/reset', { method: 'POST', headers: instructorHeader })
    if (!response.ok) throw new Error(`Could not reset synthetic fixture (${response.status}).`)
    sequence.current = 0; lastWindow.current = null
    document.querySelector('[data-testid="duplicate-result"]')!.textContent = ''
    await readSummary()
  }
  function safely(run: () => Promise<void>) { setError(''); void run().catch((e: unknown) => setError(e instanceof Error ? e.message : 'Synthetic alert request failed.')) }

  return <section aria-labelledby="cap114-title">
    <h1 id="cap114-title">Review operations · synthetic instructor harness</h1>
    <p>Thresholds are a teaching fixture. No insurer or payment provider is contacted.</p>
    <p role="status" data-testid="alert-state">{summary?.alertState ?? 'LOADING'}</p>
    <p data-testid="window-count">Windows recorded: {summary?.recordedWindows ?? 0}</p>
    <p data-testid="qualifying-count">Qualifying windows: {summary?.qualifyingWindows ?? 0}</p>
    <p data-testid="firing-count">Firing transitions: {summary?.firingTransitions ?? 0}</p>
    <p data-testid="resolution-count">Resolution transitions: {summary?.resolutionTransitions ?? 0}</p>
    <p data-testid="review-status">Application review: {summary?.applicationReviewStatus ?? 'LOADING'}</p>
    <p data-testid="requested-fields">Requested fields: {summary?.requestedFieldCodes.join(', ') ?? 'LOADING'}</p>
    <p data-testid="payment-status">Payment: {summary?.paymentStatus ?? 'LOADING'}; dispatch confirmed: {String(summary?.dispatchConfirmed ?? false)}</p>
    <p data-testid="classification">{summary?.classification ?? 'LOADING'}</p>
    <p data-testid="next-action">{summary?.applicationReviewStatus === 'NEEDS_INFORMATION'
      ? `Next action: provide requested synthetic fields (${summary.requestedFieldCodes.length}). This is not rejection or issuance.`
      : 'No information request is pending.'}</p>
    <div aria-label="Synthetic test windows">
      <button data-testid="reset" onClick={() => safely(reset)}>Reset fixture</button>
      <button data-testid="six-window" onClick={() => safely(() => record('six'))}>Add 6/100 window</button>
      <button data-testid="repeat-window" onClick={() => safely(() => record('duplicate'))}>Repeat last window</button>
      <button data-testid="five-window" onClick={() => safely(() => record('five'))}>Add exactly 5/100 window</button>
      <button data-testid="low-window" onClick={() => safely(() => record('low'))}>Add 1/2 window</button>
      <button data-testid="healthy-window" onClick={() => safely(() => record('healthy'))}>Add healthy 100/100 window</button>
    </div>
    <p data-testid="duplicate-result" />
    <p role="alert" data-testid="error">{error}</p>
  </section>
}
