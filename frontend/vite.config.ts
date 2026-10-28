import { defineConfig } from 'vitest/config';
import react from '@vitejs/plugin-react';
import { dirname, resolve } from 'node:path';
import { fileURLToPath } from 'node:url';

const projectDir = dirname(fileURLToPath(import.meta.url));

export default defineConfig({
  plugins: [react()],
  server: {
    proxy: {
      ...(process.env.VITE_API_TARGET ? { '/test/day28': process.env.VITE_API_TARGET } : {}),
      ...(process.env.VITE_MODEL_API_TARGET ? { '/api/model': process.env.VITE_MODEL_API_TARGET } : {}),
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
    include: process.env.RUN_FDE_V2 === 'true'
      ? ['src/fde-v2/QuestionAnswerApp.test.tsx', 'src/fde-v2/AskRagPanel.test.tsx']
      : process.env.RUN_MODEL_INTEGRATION_WITNESS === 'true'
      ? ['src/local/ModelPredictionPanel.witness.test.tsx']
      : process.env.RUN_MODEL_INTEGRATION_ACCEPTANCE === 'true'
        ? ['src/local/ModelPredictionPanel.test.tsx']
        : process.env.RUN_LEARNER_ACCEPTANCE === 'true'
      ? ['src/**/*.test.tsx']
      : ['src/infrastructure/**/*.test.tsx'],
  },
});
