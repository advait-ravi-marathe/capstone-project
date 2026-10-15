import { useEffect, useState } from 'react'

type Offer = { offerId: string; revision: string; contentSha256: string; amountMinor: number; currency: 'INR'; termCodes: string[]; expiresAt: string }
type Summary = { original: Offer; revised: Offer; deltaMinor: number; authorizedCitations: Array<{ ownerId: string; sourceId: string; version: string }>; paymentStatus: 'NOT_STARTED'; paidAmountMinor: number; dispatchConfirmed: false; explicitChoices: number; classification: 'SYNTHETIC_ONLY' }
type Validation = { valid: boolean; code: string; authorizedCitations: Array<{ ownerId: string; sourceId: string; version: string }> }
const instructorHeader = { 'X-CAP115-Instructor-Session': '11500000-0000-4000-8000-000000000001' }
const money = (minor: number) => `₹${(minor / 100).toLocaleString('en-IN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`

export function ReviewedOfferAnswerValidatorPanel() {
  const [summary, setSummary] = useState<Summary | null>(null)
  const [validation, setValidation] = useState<Validation | null>(null)
  const [choice, setChoice] = useState('')
  const [error, setError] = useState('')
  useEffect(() => { void load().catch(problem) }, [])
  async function load() {
    const response = await fetch('/api/support/__instructor/cap115/summary', { headers: instructorHeader, cache: 'no-store' })
    if (!response.ok) throw new Error(`Synthetic quote facts unavailable (${response.status}).`)
    setSummary(await response.json() as Summary)
  }
  function problem(value: unknown) { setError(value instanceof Error ? value.message : 'Instructor harness request failed.') }
  async function validate(variant: string) {
    setError(''); setChoice('')
    const response = await fetch('/api/support/__instructor/cap115/validate', {
      method: 'POST', headers: { ...instructorHeader, 'Content-Type': 'application/json' }, body: JSON.stringify({ variant }),
    })
    if (!response.ok) throw new Error(`Synthetic explanation check failed (${response.status}).`)
    setValidation(await response.json() as Validation); await load()
  }
  async function choose(kind: 'ACCEPT_OFFER' | 'DECLINE_OFFER') {
    if (!summary) return
    setError('')
    const response = await fetch('/api/support/__instructor/cap115/choice', {
      method: 'POST', headers: { ...instructorHeader, 'Content-Type': 'application/json' },
      body: JSON.stringify({ offerId: summary.revised.offerId, revision: summary.revised.revision,
        contentSha256: summary.revised.contentSha256, choice: kind }),
    })
    if (!response.ok) throw new Error(`Current offer choice was rejected (${response.status}).`)
    const result = await response.json() as { choice: string; choiceCount: number; paymentStatus: string }
    setChoice(`${result.choice}; recorded choices ${result.choiceCount}; payment ${result.paymentStatus}`); await load()
  }
  function run(action: () => Promise<void>) { setError(''); void action().catch(problem) }
  const changed = summary ? [...new Set([...summary.original.termCodes.filter(code => !summary.revised.termCodes.includes(code)),
    ...summary.revised.termCodes.filter(code => !summary.original.termCodes.includes(code))])].sort() : []
  return <section aria-labelledby="cap115-title">
    <h1 id="cap115-title">Offer review · synthetic instructor harness</h1>
    <p>This read-only explanation cannot choose an offer or invoke payment.</p>
    {summary ? <>
      <p data-testid="original-offer">Original {summary.original.revision}: {money(summary.original.amountMinor)} {summary.original.currency}; expires {summary.original.expiresAt}</p>
      <p data-testid="revised-offer">Current {summary.revised.revision}: {money(summary.revised.amountMinor)} {summary.revised.currency}; expires {summary.revised.expiresAt}</p>
      <p data-testid="amount-delta">Difference: +{money(summary.deltaMinor)}</p>
      <p data-testid="terms">Changed terms: {changed.join(', ')}</p>
      <p data-testid="payment-state">{summary.paymentStatus}; paid amount {money(summary.paidAmountMinor)}; dispatch confirmed {String(summary.dispatchConfirmed)}</p>
      <p data-testid="decision-count">Explicit human choices: {summary.explicitChoices}</p>
      <p data-testid="classification">{summary.classification}</p>
    </> : <p>Loading immutable quote snapshots…</p>}
    <p data-testid="validation">{validation ? `${validation.code}; valid=${validation.valid}` : 'No explanation validated yet.'}</p>
    <p data-testid="citations">{validation?.authorizedCitations.map(c => `${c.sourceId}@${c.version}`).join(', ') ?? ''}</p>
    <p data-testid="choice-result">{choice}</p>
    <nav aria-label="Fixed synthetic explanation responses">
      <button data-testid="supported" onClick={() => run(() => validate('supported'))}>Check supported response</button>
      <button data-testid="foreign" onClick={() => run(() => validate('foreign-source'))}>Check foreign citation</button>
      <button data-testid="invented" onClick={() => run(() => validate('invented-field'))}>Check unknown claim</button>
      <button data-testid="missing-status" onClick={() => run(() => validate('missing-status'))}>Check missing status</button>
      <button data-testid="stale" onClick={() => run(() => validate('stale-hash'))}>Check stale revision</button>
      <button data-testid="abstain" onClick={() => run(() => validate('insufficient'))}>Check no-source abstention</button>
    </nav>
    <nav aria-label="Explicit human quote choice">
      <button data-testid="accept" onClick={() => run(() => choose('ACCEPT_OFFER'))}>Record my choice to accept this quote</button>
      <button data-testid="decline" onClick={() => run(() => choose('DECLINE_OFFER'))}>Record my choice to decline this quote</button>
    </nav>
    <p role="alert" data-testid="error">{error}</p>
  </section>
}
