import { useState, type FormEvent } from 'react'
import { useQuery } from '@tanstack/react-query'
import { fetchPrivateJson, type Session } from '../../state/session'

type AuditResponse = { ids: string[]; count: number; classification: 'SYNTHETIC_READ_ONLY' }
const validInstant = (value: string) => /^\d{4}-\d\d-\d\dT\d\d:\d\d:\d\d(?:\.\d+)?(?:Z|[+-]\d\d:\d\d)$/.test(value) &&
  Number.isFinite(Date.parse(value))

export function PaymentAuditWindowPanel({ session, ready }: { session: Session | null; ready: boolean }) {
  const demoIdentity = session?.ownerId === 'a1680000-0000-4000-8000-000000000001' ||
    session?.ownerId === 'b1680000-0000-4000-8000-000000000002'
  const [startInclusive, setStartInclusive] = useState('2026-12-15T00:00:00Z')
  const [endExclusive, setEndExclusive] = useState('2026-12-16T00:00:00Z')
  const [acceptedRange, setAcceptedRange] = useState<{ start: string; end: string } | null>(null)
  const [inputError, setInputError] = useState<string | null>(null)
  const query = useQuery({
    queryKey: ['private', session?.ownerId, session?.generation, 'payment-audit-window', acceptedRange?.start, acceptedRange?.end],
    enabled: ready && session !== null && !demoIdentity && acceptedRange !== null,
    retry: false,
    queryFn: async ({ signal }): Promise<AuditResponse> => {
      if (!session) throw new Error('Choose an authenticated account to inspect private audit IDs.')
      if (!acceptedRange) throw new Error('Select a window before reading audit records.')
      const params = new URLSearchParams({ startInclusive: acceptedRange.start, endExclusive: acceptedRange.end })
      const raw = await fetchPrivateJson(session, `/api/v1/me/payments/audit?${params}`, signal)
      if (typeof raw !== 'object' || raw === null || !('ids' in raw) || !Array.isArray(raw.ids) ||
          !raw.ids.every((id) => typeof id === 'string') || !('count' in raw) || typeof raw.count !== 'number' ||
          raw.count !== raw.ids.length || !('classification' in raw) || raw.classification !== 'SYNTHETIC_READ_ONLY')
        throw new Error('The audit response did not match its bounded contract.')
      return raw as AuditResponse
    },
  })

  function submit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    if (!session || !ready || demoIdentity) { setInputError('A trusted authenticated session is required; demo selectors are not authentication.'); return }
    if (!validInstant(startInclusive) || !validInstant(endExclusive) || Date.parse(startInclusive) >= Date.parse(endExclusive)) {
      setInputError('Use explicit UTC offsets and a nonempty [start, end) interval.')
      return
    }
    setInputError(null); setAcceptedRange({ start: startInclusive, end: endExclusive })
  }

  return <section className="source-notes payment-audit" aria-labelledby="payment-audit-title">
    <div><p className="eyebrow">CAP-173 / PRIVATE READ-ONLY</p><h2 id="payment-audit-title">Count audit records in a UTC window.</h2>
      <p>Uses an inclusive start and exclusive end. Synthetic journal IDs only; this screen cannot create money or dispatch work.</p></div>
    <form onSubmit={submit}>
      <label>Start inclusive (ISO 8601 with offset)<input value={startInclusive} onChange={(e) => setStartInclusive(e.target.value)} spellCheck={false} /></label>
      <label>End exclusive (ISO 8601 with offset)<input value={endExclusive} onChange={(e) => setEndExclusive(e.target.value)} spellCheck={false} /></label>
      <button className="secondary-button" type="submit" disabled={!ready || !session || demoIdentity || query.isFetching}>Read synthetic audit window</button>
    </form>
    {(!session || demoIdentity) && <p role="status">A trusted authenticated session is required; demo selectors are not authentication.</p>}
    {inputError && <p role="alert">{inputError}</p>}
    {query.isFetching && <p role="status">Reading the selected audit window…</p>}
    {query.isError && <p className="notice" role="alert">{query.error.message}</p>}
    {query.data && <div role="status"><p>{query.data.count} matching journal records.</p><ul>{query.data.ids.map((id) => <li key={id}>{id}</li>)}</ul></div>}
  </section>
}
