import React from 'react'
import { createRoot } from 'react-dom/client'
import { QueryClientProvider } from '@tanstack/react-query'
import '@fontsource-variable/inter'
import './styles.css'
import { queryClient } from './state/queryClient'
import { SessionBoundary } from './state/SessionBoundary'

createRoot(document.getElementById('root')!).render(
  <React.StrictMode>
    <QueryClientProvider client={queryClient}>
      <SessionBoundary />
    </QueryClientProvider>
  </React.StrictMode>,
)
