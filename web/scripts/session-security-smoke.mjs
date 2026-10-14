import assert from 'node:assert/strict'
import React from 'react'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { renderToString } from 'react-dom/server'
import { createServer } from 'vite'

const server = await createServer({
  configFile: 'vite.config.ts',
  server: { middlewareMode: true },
  appType: 'custom',
})
const originalFetch = globalThis.fetch

try {
  const sessionModule = await server.ssrLoadModule('/src/state/session.ts')
  const queryModule = await server.ssrLoadModule('/src/state/queryClient.ts')
  const boundaryModule = await server.ssrLoadModule('/src/state/SessionBoundary.tsx')
  const { fetchPrivateJson, isCurrentSession, revalidateSession } = sessionModule
  const { queryClient } = queryModule

  const boundaryClient = new QueryClient()
  boundaryClient.setQueryData(['session', 'revalidate'], { ownerId: 'stale-owner', generation: 99 })
  globalThis.fetch = () => new Promise(() => {})
  const boundaryMarkup = renderToString(React.createElement(
    QueryClientProvider,
    { client: boundaryClient },
    React.createElement(boundaryModule.SessionBoundary),
  ))
  assert.match(boundaryMarkup, /Checking session/)
  assert.match(boundaryMarkup, /Explore example records/,
    'public synthetic discovery should remain available during session revalidation')
  assert.doesNotMatch(boundaryMarkup, /Signed in|synthetic-owner|private data/,
    'previous private identity was exposed during session revalidation')
  boundaryClient.clear()

  const jsonResponse = (value) => ({
    ok: true,
    status: 200,
    json: async () => value,
  })

  const sessionResponses = []
  globalThis.fetch = () => new Promise((resolve) => sessionResponses.push(resolve))
  const olderCheck = revalidateSession()
  const newerCheck = revalidateSession()
  sessionResponses[1](jsonResponse({ ownerId: 'synthetic-owner-b' }))
  const sessionB = await newerCheck
  sessionResponses[0](jsonResponse({ ownerId: 'synthetic-owner-a' }))
  assert.deepEqual(await olderCheck, sessionB)
  assert.equal(isCurrentSession(sessionB), true, 'stale session response replaced the newer owner')

  queryClient.setQueryData(['private', sessionB.ownerId, sessionB.generation, 'sample'], 'private data')
  let resolveLateResponse
  globalThis.fetch = (path) => path === '/api/session'
    ? Promise.resolve(jsonResponse({ ownerId: 'synthetic-owner-c' }))
    : new Promise((resolve) => { resolveLateResponse = resolve })
  const lateResponse = fetchPrivateJson(sessionB, '/api/sample', new AbortController().signal)
  const sessionC = await revalidateSession()
  assert.equal(sessionC.ownerId, 'synthetic-owner-c')
  assert.equal(queryClient.getQueryCache().findAll({ queryKey: ['private'] }).length, 0,
    'private cache was retained after an account switch')
  resolveLateResponse(jsonResponse({ secret: 'synthetic owner B value' }))
  await assert.rejects(lateResponse, { name: 'AbortError' })

  let bodyStarted
  const bodyIsPending = new Promise((resolve) => { bodyStarted = resolve })
  let resolveLateBody
  globalThis.fetch = (path) => path === '/api/session'
    ? Promise.resolve(jsonResponse({ ownerId: 'synthetic-owner-d' }))
    : Promise.resolve({
      ok: true,
      status: 200,
      json: () => {
        bodyStarted()
        return new Promise((resolve) => { resolveLateBody = resolve })
      },
    })
  const lateBody = fetchPrivateJson(sessionC, '/api/sample', new AbortController().signal)
  await bodyIsPending
  const sessionD = await revalidateSession()
  resolveLateBody({ secret: 'synthetic owner C value' })
  await assert.rejects(lateBody, { name: 'AbortError' })
  assert.equal(isCurrentSession(sessionD), true)

  queryClient.setQueryData(['private', sessionD.ownerId, sessionD.generation, 'sample'], 'private data')
  globalThis.fetch = async () => ({ status: 401, ok: false })
  assert.equal(await revalidateSession(), null)
  assert.equal(queryClient.getQueryCache().findAll({ queryKey: ['private'] }).length, 0,
    'private cache was retained after session invalidation')

  let unexpectedRequest = false
  globalThis.fetch = async () => { unexpectedRequest = true; throw new Error('unexpected fetch') }
  await assert.rejects(fetchPrivateJson(sessionD, 'https://attacker.invalid/api/sample', new AbortController().signal))
  assert.equal(unexpectedRequest, false, 'external URL passed the API-path guard')

  console.log('Session/cache security smoke checks passed.')
} finally {
  globalThis.fetch = originalFetch
  await server.close()
}
