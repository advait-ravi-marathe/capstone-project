import { chromium } from 'playwright';
import { mkdir, readFile, writeFile } from 'node:fs/promises';
import { join } from 'node:path';

const [baseUrl, controlDirectory, outputPath] = process.argv.slice(2);
if (!baseUrl || !controlDirectory || !outputPath) {
  throw new Error('Usage: node e2e/day83-lock-recovery.mjs BASE_URL CONTROL_DIR OUTPUT_JSON');
}
await mkdir(controlDirectory, { recursive: true });
const browser = await chromium.launch({
  headless: true,
  executablePath: process.env.CHROMIUM_BIN || undefined,
  args: ['--no-sandbox', '--disable-dev-shm-usage'],
  timeout: 10000,
});

async function signal(name, value = 'ok') {
  await writeFile(join(controlDirectory, name), value);
}

async function waitForSignal(name) {
  const path = join(controlDirectory, name);
  const deadline = Date.now() + 20000;
  while (Date.now() < deadline) {
    try { return await readFile(path, 'utf8'); } catch { /* deterministic file barrier, guarded by deadline */ }
    await new Promise(resolve => setTimeout(resolve, 20));
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
  await context.setExtraHTTPHeaders({ 'Cache-Control': 'no-cache' });
  const page = await context.newPage();
  page.setDefaultTimeout(10000);
  page.on('request', request => console.log(`request ${request.method()} ${request.url()}`));
  page.on('response', response => console.log(`response ${response.status()} ${response.url()}`));
  page.on('pageerror', error => console.log(`pageerror ${error.message}`));
  await page.goto(baseUrl, { waitUntil: 'domcontentloaded' });
  await page.getByRole('button', { name: 'Use owner-a', exact: true }).click();

  const previewPath = '/api/policies/policy-renewal-a/renewal-preview';
  const firstResponsePromise = page.waitForResponse(response =>
    new URL(response.url()).pathname === previewPath && response.request().method() === 'GET', { timeout: 60000 });
  await page.getByRole('button', { name: 'View preview', exact: true }).click();
  await signal('first-started');

  await waitForSignal('start-second');
  const secondResponsePromise = page.evaluate(path => {
    const responseData = async response => {
      const text = await response.text();
      let body;
      try { body = JSON.parse(text); } catch { body = text; }
      return { status: response.status, body };
    };
    return fetch(path, { credentials: 'same-origin', cache: 'no-store' }).then(responseData);
  }, previewPath);
  await signal('second-started');

  await waitForSignal('start-third');
  const busyPage = await context.newPage();
  busyPage.setDefaultTimeout(10000);
  await busyPage.goto(baseUrl, { waitUntil: 'domcontentloaded' });
  await busyPage.getByRole('button', { name: 'View preview', exact: true }).waitFor({ state: 'visible' });
  const thirdResponsePromise = busyPage.waitForResponse(response =>
    new URL(response.url()).pathname === previewPath && response.request().method() === 'GET');
  await busyPage.getByRole('button', { name: 'View preview', exact: true }).click();
  const thirdResponse = await thirdResponsePromise;
  const third = await responseData(thirdResponse);
  await busyPage.getByRole('status').filter({ hasText: /busy/i }).waitFor({ state: 'visible' });
  await busyPage.getByText('policy-renewal-a').waitFor({ state: 'visible' });
  await busyPage.getByRole('button', { name: /retry/i }).waitFor({ state: 'visible' });
  const activeConnections = await busyPage.evaluate(async () => {
    const response = await fetch('/actuator/metrics/hikaricp.connections.active');
    if (!response.ok) throw new Error(`Hikari active-connection meter returned ${response.status}`);
    const meter = await response.json();
    const value = meter.measurements.find(measurement => measurement.statistic === 'VALUE')?.value;
    if (typeof value !== 'number') throw new Error('Hikari active-connection gauge is missing');
    return value;
  });
  third.activeConnections = activeConnections;
  await writeFile(join(controlDirectory, 'third.json'), JSON.stringify(third));

  await waitForSignal('release-lock');
  const firstResponse = await firstResponsePromise;
  const first = await responseData(firstResponse);
  const second = await secondResponsePromise;
  const manualRetryResponsePromise = busyPage.waitForResponse(response =>
    new URL(response.url()).pathname === previewPath && response.request().method() === 'GET');
  await busyPage.getByRole('button', { name: /retry/i }).click();
  const manualRetry = await responseData(await manualRetryResponsePromise);
  await busyPage.getByText(/not a renewal offer or coverage commitment/i).waitFor({ state: 'visible' });
  const metrics = await busyPage.evaluate(async () => {
    const readCount = async outcome => {
      const response = await fetch(`/actuator/metrics/coverstory.preview.requests?tag=outcome:${outcome}`);
      if (!response.ok) throw new Error(`Metric endpoint returned ${response.status}`);
      const meter = await response.json();
      return meter.measurements.find(value => value.statistic === 'COUNT')?.value;
    };
    return { busy: await readCount('BUSY'), success: await readCount('SUCCESS') };
  });

  await writeFile(outputPath, JSON.stringify({
    browserOpened: true,
    ownerSelectedThroughUi: true,
    firstStatus: first.status,
    secondStatus: second.status,
    thirdStatus: third.status,
    thirdCode: third.body?.code,
    manualRetryStatus: manualRetry.status,
    manualRetryWasUserButtonClick: true,
    readOnlyPreviewVisible: true,
    boundedRouteMetrics: metrics,
  }, null, 2));
  await signal('finished');
  await context.close();
} finally {
  await browser.close();
}
