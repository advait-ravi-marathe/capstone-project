import { clearPrivateQueries } from './queryClient'

export type Session = { ownerId: string; generation: number }

let activeGeneration = 0
let activeOwner: string | null = null
let latestSessionCheck = 0
const privateMutations = new Set<AbortController>()

export async function revalidateSession(signal?: AbortSignal): Promise<Session | null> {
  const check = ++latestSessionCheck
  let response: Response
  try {
    response = await fetch('/api/session', {
      credentials: 'same-origin',
      cache: 'no-store',
      headers: { Accept: 'application/json' },
      signal,
    })
  } catch (error) {
    if (check !== latestSessionCheck) return currentSession()
    throw error
  }
  if (check !== latestSessionCheck) return currentSession()
  if (response.status === 401) {
    if (activeOwner !== null) activeGeneration += 1
    activeOwner = null
    await clearPrivateQueries()
    if (check !== latestSessionCheck) return currentSession()
    return null
  }
  if (!response.ok) throw new Error('Session could not be checked.')
  let value: unknown
  try {
    value = await response.json()
  } catch (error) {
    if (check !== latestSessionCheck) return currentSession()
    throw error
  }
  if (check !== latestSessionCheck) return currentSession()
  if (!isSessionResponse(value)) throw new Error('Session response was invalid.')
  if (activeOwner !== value.ownerId) {
    const hadOwner = activeOwner !== null
    activeOwner = value.ownerId
    activeGeneration += 1
    if (hadOwner) {
      await clearPrivateQueries()
      if (check !== latestSessionCheck) return currentSession()
    }
  }
  return { ownerId: value.ownerId, generation: activeGeneration }
}

function currentSession(): Session | null {
  return activeOwner === null ? null : { ownerId: activeOwner, generation: activeGeneration }
}

function isSessionResponse(value: unknown): value is { ownerId: string } {
  return typeof value === 'object' && value !== null &&
    'ownerId' in value && typeof value.ownerId === 'string' && value.ownerId.length > 0
}

export function isCurrentSession(session: Session): boolean {
  return activeOwner === session.ownerId && activeGeneration === session.generation
}

/** Invalidate the current owner generation before an explicit account switch or logout. */
export async function resetActiveSession(): Promise<void> {
  latestSessionCheck += 1
  activeGeneration += 1
  activeOwner = null
  for (const controller of privateMutations) controller.abort()
  privateMutations.clear()
  await clearPrivateQueries()
}

export async function mutatePrivate(session: Session, path: string, method: 'POST' | 'DELETE', body?: unknown): Promise<void> {
  if (!path.startsWith('/api/') || !isCurrentSession(session)) throw new DOMException('Session changed', 'AbortError')
  const controller = new AbortController(); privateMutations.add(controller)
  try {
    const response = await fetch(path, {
      method, credentials: 'same-origin', cache: 'no-store', signal: controller.signal,
      headers: { Accept: 'application/json', ...(body === undefined ? {} : { 'Content-Type': 'application/json' }) },
      ...(body === undefined ? {} : { body: JSON.stringify(body) }),
    })
    if (!isCurrentSession(session)) throw new DOMException('Session changed', 'AbortError')
    if (!response.ok) throw new Error(`Request failed (${response.status}).`)
  } finally { privateMutations.delete(controller) }
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
