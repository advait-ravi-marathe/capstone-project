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
  const safeSession = sessionQuery.isFetching ? null : session

  useEffect(() => {
    if (session && !isCurrentSession(session)) void clearPrivateQueries()
    if (session === null) void clearPrivateQueries()
  }, [session])

  const sessionStatus = sessionQuery.isPending || sessionQuery.isFetching
    ? 'checking'
    : sessionQuery.isError || !session
      ? 'unavailable'
      : 'ready'

  // Public synthetic discovery remains available while private session resolution
  // is pending, rejected, or absent. Private resume actions stay unavailable.
  return <Marketplace session={safeSession ?? null} sessionStatus={sessionStatus} />
}
