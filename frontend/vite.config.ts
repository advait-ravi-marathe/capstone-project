import { defineConfig } from 'vitest/config';
import react from '@vitejs/plugin-react';

export default defineConfig({
  plugins: [react()],
  server: {
    proxy: {
      ...(process.env.VITE_API_TARGET ? { '/test/day28': process.env.VITE_API_TARGET } : {}),
      ...(process.env.VITE_MODEL_API_TARGET ? { '/api/model': process.env.VITE_MODEL_API_TARGET } : {}),
    },
  },
  test: {
    environment: 'jsdom',
    setupFiles: './src/test-setup.ts',
    clearMocks: true,
    include: process.env.RUN_MODEL_INTEGRATION_WITNESS === 'true'
      ? ['src/local/ModelPredictionPanel.witness.test.tsx']
      : process.env.RUN_MODEL_INTEGRATION_ACCEPTANCE === 'true'
        ? ['src/local/ModelPredictionPanel.test.tsx']
        : process.env.RUN_LEARNER_ACCEPTANCE === 'true'
      ? ['src/**/*.test.tsx']
      : ['src/infrastructure/**/*.test.tsx'],
  },
});
