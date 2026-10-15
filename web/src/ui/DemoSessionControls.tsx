import { useState } from 'react'
import { queryClient } from '../state/queryClient'
import { resetActiveSession, type Session } from '../state/session'

export function DemoSessionControls({ session }: { session: Session | null }) {
  const [busy, setBusy] = useState(false)
  const [message, setMessage] = useState<string | null>(null)

  async function change(account: 'A' | 'B' | null) {
    setBusy(true); setMessage(null)
    await resetActiveSession()
    try {
      const response = await fetch(account === null ? '/api/demo/logout' : '/api/demo/session', {
        method: 'POST', credentials: 'same-origin', cache: 'no-store',
        headers: { Accept: 'application/json', 'Content-Type': 'application/json' },
        ...(account === null ? {} : { body: JSON.stringify({ account }) }),
      })
      if (!response.ok) throw new Error('Synthetic account controls require the isolated synthetic-demo profile.')
      await queryClient.invalidateQueries({ queryKey: ['session', 'revalidate'] })
    } catch (error) {
      setMessage(error instanceof Error ? error.message : 'Demo session could not be changed.')
      await queryClient.invalidateQueries({ queryKey: ['session', 'revalidate'] })
    } finally { setBusy(false) }
  }

  return <section className="demo-session" aria-label="Synthetic demo account">
    <span className="session-label" aria-live="polite">{session ? 'Signed in' : 'Guest preview'}</span>
    <span aria-live="polite">{session ? `Synthetic account ${session.ownerId === 'a1680000-0000-4000-8000-000000000001' ? 'A' : 'B'}` : 'Guest'}</span>
    {session ? <>
      <button type="button" disabled={busy} onClick={() => void change(session.ownerId === 'a1680000-0000-4000-8000-000000000001' ? 'B' : 'A')}>Switch demo account</button>
      <button type="button" disabled={busy} onClick={() => void change(null)}>End demo session</button>
    </> : <>
      <button type="button" disabled={busy} onClick={() => void change('A')}>Continue as demo A</button>
      <button type="button" disabled={busy} onClick={() => void change('B')}>Continue as demo B</button>
    </>}
    {message && <span role="alert">{message}</span>}
  </section>
}
