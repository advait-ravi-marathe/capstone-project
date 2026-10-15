import React from 'react'
import { createRoot } from 'react-dom/client'
import { ReviewAlertWindow } from './coverstory/application/ReviewAlertWindow'
import './styles.css'

createRoot(document.getElementById('root')!).render(<React.StrictMode><ReviewAlertWindow /></React.StrictMode>)
