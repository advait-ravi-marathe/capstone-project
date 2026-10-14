import { useEffect } from 'react'
import { useQuery } from '@tanstack/react-query'
import { clearPrivateQueries } from './queryClient'
import { isCurrentSession, revalidateSession } from './session'
import { Marketplace } from '../ui/Marketplace'

export function SessionBoundary() {
  const sessionQuery = useQuery({
    queryKey: ['session', 'revalidate'],
    queryFn: ({ signal }) => revalidateSession(signal),
    staleTime: 0,
    refetchOnMount: 'always',
    refetchOnWindowFocus: 'always',
  })
  const session = sessionQuery.data

  useEffect(() => {
    if (session && !isCurrentSession(session)) void clearPrivateQueries()
    if (session === null) void clearPrivateQueries()
  }, [session])

  if (sessionQuery.isPending || sessionQuery.isFetching) return <main className="center-state">Checking your session…</main>
  if (sessionQuery.isError) return <main className="center-state">We couldn’t check your session. Refresh to try again.</main>
  if (!session) return <main className="center-state">Sign in to continue. This starter does not include authentication.</main>
  return <Marketplace session={session} />
}
