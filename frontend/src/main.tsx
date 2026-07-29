import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import App from './App'
import { SessionGate } from './auth/SessionGate'
import { BrowserRouter } from 'react-router-dom'
import './index.css'

createRoot(document.getElementById('root')!).render(
  <StrictMode>
    <SessionGate><BrowserRouter><App /></BrowserRouter></SessionGate>
  </StrictMode>,
)
