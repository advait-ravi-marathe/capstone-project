import { chromium } from 'playwright';
import { mkdir, writeFile } from 'node:fs/promises';
import { dirname } from 'node:path';
import assert from 'node:assert/strict';

const [url, outputPath, mode = 'learner'] = process.argv.slice(2);
if (!url || !outputPath) throw new Error('Usage: node day45-renewal-preview-browser.mjs URL OUTPUT [learner|smoke]');
await mkdir(dirname(outputPath), { recursive: true });
let browser;
let page;
const actual = { browserOpened: false, status: null, body: null, visibleText: '', apiRequests: [], screenshot: null, failure: null };
async function readBodyBounded(response) {
  return await Promise.race([
    response.text(),
    new Promise(resolve => setTimeout(() => resolve(null), 2000)),
  ]);
}
try {
  browser = await chromium.launch({ headless: true, executablePath: process.env.CHROMIUM_BIN || undefined,
    args: ['--no-sandbox', '--disable-dev-shm-usage'], timeout: 10000 });
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
    assert.equal(response.status(), 200, `smoke response must be HTTP 200, got ${response.status()}`);
    try {
      actual.body = await readBodyBounded(response);
      if (actual.body === null) actual.bodyReadFailure = 'Timed out reading response body after 2000ms';
    } catch (error) { actual.body = null; actual.bodyReadFailure = error instanceof Error ? error.message : String(error); }
    actual.visibleText = await page.locator('#root').innerText().catch(() => '');
  } else {
    const panel = page.getByRole('region', { name: 'Renewal preview', exact: true });
    if (response.status() === 200) {
      actual.body = await response.json();
      const body = actual.body;
      assert.deepEqual(Object.keys(body).sort(), ['policyId','sourcePolicyVersion','productVersion','rateVersion',
        'currentAnnualPremiumRupees','currency','nextTermEffectiveDate','notice'].sort());
      assert.equal(typeof body.currentAnnualPremiumRupees, 'number');
      assert.equal(Number.isSafeInteger(body.sourcePolicyVersion), true, 'source version must be an exact integer');
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
      actual.failure = `Expected HTTP 200, observed ${response.status()}`;
      let statusAssertion;
      try {
        assert.equal(response.status(), 200, actual.failure);
      } catch (error) {
        statusAssertion = error;
      }
      try {
        actual.body = await readBodyBounded(response);
        if (actual.body === null) actual.bodyReadFailure = 'Timed out reading response body after 2000ms';
      } catch (error) { actual.bodyReadFailure = error instanceof Error ? error.message : String(error); }
      actual.visibleText = await panel.innerText().catch(() => '');
      throw statusAssertion ?? new Error(actual.failure);
    }
  }
} catch (error) {
  actual.failure ??= error instanceof Error ? error.message : String(error);
  if (page) {
    actual.visibleText = await page.locator('body').innerText().catch(() => actual.visibleText);
    const screenshotPath = `${outputPath}.failure.png`;
    const screenshotSaved = await Promise.race([
      page.screenshot({ path: screenshotPath, fullPage: true }).then(() => true),
      new Promise(resolve => setTimeout(() => resolve(false), 2000)),
    ]).catch(() => false);
    actual.screenshot = screenshotSaved ? screenshotPath : null;
    if (!screenshotSaved) actual.screenshotFailure = 'Screenshot did not finish within 2000ms';
  }
} finally {
  await writeFile(outputPath, JSON.stringify(actual, null, 2));
  if (browser) await Promise.race([browser.close(), new Promise(resolve => setTimeout(resolve, 2000))]);
}
if (actual.failure) process.exitCode = 1;
