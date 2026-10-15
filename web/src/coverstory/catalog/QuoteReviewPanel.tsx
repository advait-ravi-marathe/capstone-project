import { useEffect, useRef, useState } from 'react'
import type { Session } from '../../state/session'
import { isCurrentSession } from '../../state/session'

type ReviewResult = {
  status: string; deltaPaise: number; explicitReviewRequired: boolean; escalationRequired: boolean
  quoteId: string; revision: string; currentTotalPaise: number; currency: string; termsHash: string
  expiresAt: string; orderCreated: false
}
const quoteId = 'demo-health-A-v1'
const displayed = { revision: 'revision-v1', totalPaise: 100000, termsHash: 'aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa' }

function isReviewResult(value: unknown): value is ReviewResult {
  return typeof value === 'object' && value !== null && 'status' in value && typeof value.status === 'string' &&
    'deltaPaise' in value && typeof value.deltaPaise === 'number' && Number.isSafeInteger(value.deltaPaise) &&
    'explicitReviewRequired' in value && typeof value.explicitReviewRequired === 'boolean' &&
    'escalationRequired' in value && typeof value.escalationRequired === 'boolean' &&
    'quoteId' in value && value.quoteId === quoteId && 'revision' in value && typeof value.revision === 'string' &&
    'currentTotalPaise' in value && typeof value.currentTotalPaise === 'number' && Number.isSafeInteger(value.currentTotalPaise) &&
    'currency' in value && value.currency === 'INR' && 'termsHash' in value && typeof value.termsHash === 'string' &&
    /^[a-f0-9]{64}$/.test(value.termsHash) && 'expiresAt' in value && typeof value.expiresAt === 'string' &&
    'orderCreated' in value && value.orderCreated === false
}

export function QuoteReviewPanel({ session, ready }: { session: Session | null; ready: boolean }) {
  const [result, setResult] = useState<ReviewResult | null>(null)
  const [error, setError] = useState<string | null>(null)
  const [busy, setBusy] = useState(false)
  const activeRequest = useRef<AbortController | null>(null)
  useEffect(() => {
    activeRequest.current?.abort()
    setResult(null); setError(null); setBusy(false)
    return () => activeRequest.current?.abort()
  }, [session?.ownerId, session?.generation, ready])

  async function validate(accepted?: ReviewResult) {
    if (!session || !ready) return
    activeRequest.current?.abort()
    const controller = new AbortController(); activeRequest.current = controller
    setBusy(true); setError(null)
    const body = {
      quoteId, displayedRevision: displayed.revision, displayedTotalPaise: displayed.totalPaise,
      displayedTermsHash: displayed.termsHash,
      ...(accepted ? { acceptedRevision: accepted.revision, acceptedTotalPaise: accepted.currentTotalPaise,
        acceptedTermsHash: accepted.termsHash } : {}),
    }
    try {
      const response = await fetch('/api/v1/me/quote-validation', {
        method: 'POST', credentials: 'same-origin', cache: 'no-store', signal: controller.signal,
        headers: { Accept: 'application/json', 'Content-Type': 'application/json' }, body: JSON.stringify(body),
      })
      const raw: unknown = await response.json()
      if (!isCurrentSession(session)) return
      if ((response.status === 200 || response.status === 409) && isReviewResult(raw)) setResult(raw)
      else if (response.status === 401 || response.status === 403) setError('Your session needs to be checked again.')
      else if (response.status === 422 || response.status === 409) setError('This synthetic quote is incomplete or no longer available.')
      else setError(`Quote review is unavailable (${response.status}).`)
    } catch (failure) {
      if (failure instanceof DOMException && failure.name === 'AbortError') return
      if (isCurrentSession(session)) setError('Quote review could not be completed. No order was created.')
    } finally { if (isCurrentSession(session)) setBusy(false) }
  }

  return <section className="quote-review" aria-labelledby="quote-review-title">
    <p className="eyebrow">OWNED QUOTE REVIEW / SYNTHETIC</p><h2 id="quote-review-title">Check today’s sandbox quote.</h2>
    <p>Fixed example only. A changed amount or terms requires review. This screen cannot place an order.</p>
    {!session && <p role="status">Choose a local synthetic demo account to open the owner-scoped quote.</p>}
    {session && <button type="button" className="secondary-button" disabled={!ready || busy} onClick={() => void validate()}>
      {busy ? 'Checking current quote…' : 'Recheck quote'}</button>}
    {error && <p role="alert">{error}</p>}
    {result && <div role="status" className="quote-review-result">
      <strong>{result.status.replaceAll('_', ' ')}</strong>
      <span>Current total: INR {(result.currentTotalPaise / 100).toFixed(2)} · change: {result.deltaPaise} paise</span>
      <span>Revision {result.revision} · terms {result.termsHash.slice(0, 12)}… · expires {result.expiresAt}</span>
      {result.explicitReviewRequired && <button type="button" className="primary-button compact-button" disabled={busy} onClick={() => void validate(result)}>Accept these exact terms for review</button>}
      {result.escalationRequired && <span className="notice">Large change needs extra review.</span>}
      <span>No order was created.</span>
    </div>}
  </section>
}
