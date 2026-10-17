import { defineConfig } from '@playwright/test';
const executablePath = process.env.CHROMIUM_PATH;
export default defineConfig({ testDir: './e2e', reporter: 'list', use: { baseURL: 'http://127.0.0.1:4179', browserName: 'chromium', viewport: { width: 390, height: 844 }, launchOptions: { ...(executablePath ? { executablePath } : {}), args: ['--no-sandbox'] } }, webServer: { command: 'npm run dev -- --host 127.0.0.1 --port 4179 --strictPort', url: 'http://127.0.0.1:4179', reuseExistingServer: false, timeout: 30_000 } });
