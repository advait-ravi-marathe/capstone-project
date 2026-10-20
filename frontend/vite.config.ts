import { defineConfig } from 'vitest/config';
import react from '@vitejs/plugin-react';

export default defineConfig({
  plugins: [react()],
  server: {
    proxy: process.env.VITE_API_TARGET
      ? { '/test/day28': process.env.VITE_API_TARGET }
      : undefined,
  },
  test: {
    environment: 'jsdom',
    setupFiles: './src/test-setup.ts',
    clearMocks: true,
    include: process.env.RUN_LEARNER_ACCEPTANCE === 'true'
      ? ['src/**/*.test.tsx']
      : ['src/infrastructure/**/*.test.tsx'],
  },
});
