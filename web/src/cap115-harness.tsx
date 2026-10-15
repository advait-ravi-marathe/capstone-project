import React from 'react'
import { createRoot } from 'react-dom/client'
import { ReviewedOfferAnswerValidatorPanel } from './coverstory/application/ReviewedOfferAnswerValidatorPanel'
import './styles.css'

createRoot(document.getElementById('root')!).render(<React.StrictMode><ReviewedOfferAnswerValidatorPanel /></React.StrictMode>)
