import { chromium } from 'playwright';

const [url, phase, resultPath] = process.argv.slice(2);
const browser = await chromium.launch({
  headless: true,
  executablePath: process.env.CHROMIUM_BIN || undefined,
  args: ['--no-sandbox', '--disable-dev-shm-usage'],
  timeout: 10000,
});
try {
  const page = await browser.newPage();
  page.setDefaultTimeout(3000);
  await page.goto(url, { waitUntil: 'domcontentloaded', timeout: 8000 });
  if (phase === 'flow') {
    await page.getByRole('button', { name: 'Run lifecycle API acceptance flow' }).click();
    await page.getByTestId('lifecycle-flow').filter({ hasText: '/test/day44/lifecycle/history:' }).waitFor();
    const flow = await page.getByTestId('lifecycle-flow').textContent();
    const result = { browserOpened: true, phase, flow };
    await import('node:fs/promises').then(({ writeFile }) => writeFile(resultPath, JSON.stringify(result, null, 2)));
  } else if (phase === 'lifecycle-read') {
    await page.getByRole('button', { name: 'Read policy after restart' }).click();
    await page.getByTestId('lifecycle-flow').filter({ hasText: '/test/day44/lifecycle/history:' }).waitFor();
    const result = { browserOpened: true, phase, lifecycleReadback: await page.getByTestId('lifecycle-flow').textContent() };
    await import('node:fs/promises').then(({ writeFile }) => writeFile(resultPath, JSON.stringify(result, null, 2)));
  } else if (phase === 'write') {
    await page.getByRole('button', { name: 'Write synthetic witness' }).click();
    await page.getByRole('status').filter({ hasText: 'saved' }).waitFor();
  } else if (phase === 'read') {
    await page.getByRole('button', { name: 'Read witness' }).click();
  } else {
    throw new Error(`unknown phase ${phase}`);
  }
  if (phase === 'write' || phase === 'read') {
    await page.getByTestId('readback').filter({ hasText: 'lifecycle-smoke-v1:persisted-after-server-restart' }).waitFor();
    const result = { browserOpened: true, phase, readback: await page.getByTestId('readback').textContent() };
    await import('node:fs/promises').then(({ writeFile }) => writeFile(resultPath, JSON.stringify(result, null, 2)));
  }
} finally {
  await browser.close();
}
