import { defineConfig } from 'vitest/config';
import react from '@vitejs/plugin-react';
import { dirname, resolve } from 'node:path';
import { fileURLToPath } from 'node:url';

const projectDir = dirname(fileURLToPath(import.meta.url));

export default defineConfig({
  plugins: [react()],
  server: {
    proxy: {
      ...(process.env.VITE_FDE_V2_API_TARGET ? {
        '/health': process.env.VITE_FDE_V2_API_TARGET,
        '/documents': process.env.VITE_FDE_V2_API_TARGET,
        '/generate': process.env.VITE_FDE_V2_API_TARGET,
        '/retrieve': process.env.VITE_FDE_V2_API_TARGET,
        '/ask': process.env.VITE_FDE_V2_API_TARGET,
      } : {}),
    },
  },
  build: {
    rollupOptions: { input: { main: resolve(projectDir, 'index.html'), fdeV2: resolve(projectDir, 'fde-v2.html') } },
  },
  test: {
    environment: 'jsdom',
    setupFiles: './src/test-setup.ts',
    clearMocks: true,
    include: ['src/fde-v2/**/*.test.tsx'],
  },
});
