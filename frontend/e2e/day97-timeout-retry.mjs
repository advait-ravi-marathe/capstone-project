import { chromium } from 'playwright';
import { mkdir, readFile, writeFile } from 'node:fs/promises';
import { join } from 'node:path';

const [baseUrl, controlDirectory, outputPath] = process.argv.slice(2);
if (!baseUrl || !controlDirectory || !outputPath) {
  throw new Error('Usage: node e2e/day97-timeout-retry.mjs BASE_URL CONTROL_DIR OUTPUT_JSON');
}
await mkdir(controlDirectory, { recursive: true });
const browser = await chromium.launch({
  headless: true,
  executablePath: process.env.CHROMIUM_BIN || undefined,
  args: ['--no-sandbox', '--disable-dev-shm-usage'],
  timeout: 10000,
});

async function signal(name) { await writeFile(join(controlDirectory, name), 'ok'); }
async function waitForSignal(name) {
  const path = join(controlDirectory, name);
  const deadline = Date.now() + 30000; // watchdog only; not an application response-time assertion
  while (Date.now() < deadline) {
    try { return await readFile(path, 'utf8'); } catch { /* bounded file barrier */ }
    await new Promise(resolve => setTimeout(resolve, 25));
  }
  throw new Error(`Timed out waiting for browser harness barrier ${name}`);
}
async function responseData(response) {
  const text = await response.text();
  let body;
  try { body = JSON.parse(text); } catch { body = text; }
  return { status: response.status(), body };
}

try {
  const context = await browser.newContext({ serviceWorkers: 'block' });
  const page = await context.newPage();
  page.setDefaultTimeout(10000);
  let previewResponses = 0;
  page.on('response', response => {
    if (new URL(response.url()).pathname === '/api/policies/policy-renewal-a/renewal-preview')
      previewResponses += 1;
  });
  await page.goto(baseUrl, { waitUntil: 'domcontentloaded' });
  await page.getByRole('button', { name: 'Use owner-a', exact: true }).click();
  const previewPath = '/api/policies/policy-renewal-a/renewal-preview';
  const timeoutResponsePromise = page.waitForResponse(response =>
    new URL(response.url()).pathname === previewPath && response.request().method() === 'GET');
  await page.getByRole('button', { name: 'View preview', exact: true }).click();
  await signal('first-started');
  const timeoutResult = await responseData(await timeoutResponsePromise);
  await page.getByRole('status').filter({ hasText: /busy|unavailable|timeout/i }).waitFor({ state: 'visible' });
  await page.getByText('policy-renewal-a').waitFor({ state: 'visible' });
  await page.getByRole('button', { name: /retry/i }).waitFor({ state: 'visible' });
  const beforeRelease = {
    status: timeoutResult.status,
    body: timeoutResult.body,
    previewResponses,
    visibleStatus: await page.getByRole('status').innerText(),
    policyContextRetained: await page.getByText('policy-renewal-a').isVisible(),
    manualRetryVisible: await page.getByRole('button', { name: /retry/i }).isVisible(),
  };
  await writeFile(join(controlDirectory, 'timeout-observed.json'), JSON.stringify(beforeRelease));
  await waitForSignal('release-lock');

  const retryResponsePromise = page.waitForResponse(response =>
    new URL(response.url()).pathname === previewPath && response.request().method() === 'GET');
  await page.getByRole('button', { name: /retry/i }).click();
  const retry = await responseData(await retryResponsePromise);
  await page.getByText(/not a renewal offer or coverage commitment/i).waitFor({ state: 'visible' });
  await writeFile(outputPath, JSON.stringify({ beforeRelease, retry, previewResponses, retryWasExplicitUserAction: true }, null, 2));
  await signal('finished');
  await context.close();
} finally {
  await browser.close();
}
