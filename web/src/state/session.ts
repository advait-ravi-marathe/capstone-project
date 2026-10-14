import { clearPrivateQueries } from './queryClient'

export type Session = { ownerId: string; generation: number }

let activeGeneration = 0
let activeOwner: string | null = null

export async function revalidateSession(signal?: AbortSignal): Promise<Session | null> {
  const response = await fetch('/api/session', {
    credentials: 'same-origin',
    cache: 'no-store',
    headers: { Accept: 'application/json' },
    signal,
  })
  if (response.status === 401) {
    if (activeOwner !== null) {
      activeOwner = null
      activeGeneration += 1
      await clearPrivateQueries()
    }
    return null
  }
  if (!response.ok) throw new Error('Session could not be checked.')
  const value: unknown = await response.json()
  if (!isSessionResponse(value)) throw new Error('Session response was invalid.')
  if (activeOwner !== value.ownerId) {
    const hadOwner = activeOwner !== null
    activeOwner = value.ownerId
    activeGeneration += 1
    if (hadOwner) await clearPrivateQueries()
  }
  return { ownerId: value.ownerId, generation: activeGeneration }
}

function isSessionResponse(value: unknown): value is { ownerId: string } {
  return typeof value === 'object' && value !== null &&
    'ownerId' in value && typeof value.ownerId === 'string' && value.ownerId.length > 0
}

export function isCurrentSession(session: Session): boolean {
  return activeOwner === session.ownerId && activeGeneration === session.generation
}

export async function fetchPrivateJson<T>(session: Session, path: string, signal: AbortSignal): Promise<T> {
  if (!path.startsWith('/api/')) throw new Error('Private requests must use an API path.')
  const response = await fetch(path, {
    credentials: 'same-origin',
    cache: 'no-store',
    headers: { Accept: 'application/json' },
    signal,
  })
  if (!isCurrentSession(session)) throw new DOMException('Session changed', 'AbortError')
  if (!response.ok) throw new Error(`Request failed (${response.status}).`)
  const value: unknown = await response.json()
  if (!isCurrentSession(session)) throw new DOMException('Session changed', 'AbortError')
  return value as T
}
