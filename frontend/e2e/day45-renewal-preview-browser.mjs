import { chromium } from 'playwright';
import { mkdir, writeFile } from 'node:fs/promises';
import { dirname } from 'node:path';
import assert from 'node:assert/strict';

const [url, outputPath, mode = 'learner'] = process.argv.slice(2);
if (!url || !outputPath) throw new Error('Usage: node day45-renewal-preview-browser.mjs URL OUTPUT [learner|smoke]');
await mkdir(dirname(outputPath), { recursive: true });
const browser = await chromium.launch({ headless: true, executablePath: process.env.CHROMIUM_BIN || undefined,
  args: ['--no-sandbox', '--disable-dev-shm-usage'], timeout: 10000 });
let page;
const actual = { browserOpened: false, status: null, body: null, visibleText: '', apiRequests: [], screenshot: null, failure: null };
try {
  page = await browser.newPage(); page.setDefaultTimeout(5000);
  page.on('request', request => {
    const parsed = new URL(request.url());
    if (parsed.pathname.startsWith('/api/')) actual.apiRequests.push({ method: request.method(), path: parsed.pathname });
  });
  await page.goto(url, { waitUntil: 'domcontentloaded', timeout: 10000 }); actual.browserOpened = true;
  const expectedPath = '/api/policies/' + (new URL(url).searchParams.get('policyId') ?? 'renewal-demo') + '/renewal-preview';
  const responsePromise = page.waitForResponse(response => new URL(response.url()).pathname === expectedPath && response.request().method() === 'GET', { timeout: 10000 });
  await page.getByRole('button', { name: mode === 'smoke' ? 'Check preview' : 'Check preview', exact: true }).click();
  const response = await responsePromise; actual.status = response.status();
  if (mode === 'smoke') {
    actual.body = await response.text(); actual.visibleText = await page.locator('#root').innerText();
  } else {
    if (response.status() >= 500) actual.body = await response.text();
    else actual.body = await response.json();
    const panel = page.getByRole('region', { name: 'Renewal preview', exact: true });
    if (response.status() === 200) {
      const body = actual.body;
      assert.deepEqual(Object.keys(body).sort(), ['policyId','sourcePolicyVersion','productVersion','rateVersion',
        'currentAnnualPremiumRupees','currency','nextTermEffectiveDate','notice'].sort());
      assert.equal(typeof body.currentAnnualPremiumRupees, 'number');
      assert.equal(typeof body.sourcePolicyVersion, 'number');
      assert.equal(typeof body.policyId, 'string'); assert.equal(typeof body.productVersion, 'string');
      assert.equal(typeof body.rateVersion, 'string'); assert.equal(typeof body.currency, 'string');
      assert.equal(typeof body.nextTermEffectiveDate, 'string'); assert.equal(typeof body.notice, 'string');
      await panel.getByText(body.policyId, { exact: true }).waitFor();
      await panel.getByText(body.productVersion, { exact: true }).waitFor();
      await panel.getByText(body.rateVersion, { exact: true }).waitFor();
      await panel.getByText(`Current annual premium (reference only): ${body.currentAnnualPremiumRupees.toFixed(2)} ${body.currency}`, { exact: true }).waitFor();
      await panel.getByText(`Next term begins ${body.nextTermEffectiveDate}`, { exact: true }).waitFor();
      await panel.getByText(`Version ${body.sourcePolicyVersion}`, { exact: true }).waitFor();
      await panel.getByText(body.notice, { exact: true }).waitFor();
      assert.equal(await panel.getByRole('button', { name: /accept|renew|purchase|bind/i }).count(), 0);
      assert.equal(await panel.getByRole('link', { name: /accept|renew|purchase|bind/i }).count(), 0);
      assert.deepEqual(actual.apiRequests, [{ method: 'GET', path: expectedPath }]);
      actual.visibleText = await panel.innerText();
    } else {
      actual.visibleText = await panel.innerText().catch(() => '');
      actual.failure = `Expected HTTP 200, observed ${response.status()}`;
      throw new Error(actual.failure);
    }
  }
} catch (error) {
  actual.failure ??= error instanceof Error ? error.message : String(error);
  if (page) {
    actual.visibleText = await page.locator('body').innerText().catch(() => actual.visibleText);
    const screenshotPath = `${outputPath}.failure.png`;
    await page.screenshot({ path: screenshotPath, fullPage: true }).catch(() => undefined);
    actual.screenshot = screenshotPath;
  }
} finally {
  await writeFile(outputPath, JSON.stringify(actual, null, 2));
  await browser.close();
}
if (actual.failure) process.exitCode = 1;
