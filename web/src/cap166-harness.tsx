import React from 'react'
import { createRoot } from 'react-dom/client'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { PersistedHealthComparison } from './coverstory/catalog/PersistedHealthComparison'
import { cap166InstructorDecoderAdapter } from './test/cap166InstructorDecoderAdapter'
import './styles.css'

const client = new QueryClient({ defaultOptions: { queries: { retry: 0, staleTime: 0 } } })
createRoot(document.getElementById('root')!).render(
  <React.StrictMode><QueryClientProvider client={client}>
    <main><PersistedHealthComparison decoder={cap166InstructorDecoderAdapter} /></main>
  </QueryClientProvider></React.StrictMode>,
)
