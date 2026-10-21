import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';
import { rename, rm } from 'node:fs/promises';
import { resolve } from 'node:path';

export default defineConfig({
  root: '.',
  build: {
    outDir: 'dist-local',
    emptyOutDir: true,
    rollupOptions: { input: 'local/index.html' },
  },
  plugins: [
    react(),
    {
      name: 'local-root-html',
      async writeBundle(outputOptions) {
        const outputDir = resolve(outputOptions.dir ?? 'dist-local');
        await rename(resolve(outputDir, 'local/index.html'), resolve(outputDir, 'index.html'));
        await rm(resolve(outputDir, 'local'), { recursive: true, force: true });
      },
    },
  ],
});
