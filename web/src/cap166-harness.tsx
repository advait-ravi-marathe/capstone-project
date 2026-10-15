import React from 'react'
import { createRoot } from 'react-dom/client'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { PersistedHealthComparison, type SanitizedCatalogExport } from './coverstory/catalog/PersistedHealthComparison'
import { cap166InstructorDecoderAdapter } from './test/cap166InstructorDecoderAdapter'
import './styles.css'

const client = new QueryClient({ defaultOptions: { queries: { retry: 0, staleTime: 0 } } })
const params = new URLSearchParams(window.location.search)
const instructor = params.get('decoder') !== 'learner'
const recordExport = (payload: SanitizedCatalogExport) => {
  ;(window as Window & { __cap166SanitizedExport?: SanitizedCatalogExport }).__cap166SanitizedExport = payload
}
createRoot(document.getElementById('root')!).render(
  <React.StrictMode><QueryClientProvider client={client}>
    <main><PersistedHealthComparison {...(instructor ? { decoder: cap166InstructorDecoderAdapter } : {})} onSanitizedExport={recordExport} /></main>
  </QueryClientProvider></React.StrictMode>,
)
