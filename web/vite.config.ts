import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'
import tailwindcss from '@tailwindcss/vite'

const apiTarget = (globalThis as typeof globalThis & { process?: { env?: Record<string, string | undefined> } })
  .process?.env?.COVERSTORY_API_TARGET ?? 'http://127.0.0.1:8080'

export default defineConfig({
  plugins: [react(), tailwindcss()],
  server: { proxy: { '/api': { target: apiTarget, changeOrigin: true } } },
})
