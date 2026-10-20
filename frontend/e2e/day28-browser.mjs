import { chromium } from 'playwright';
import { writeFile } from 'node:fs/promises';

const [url, resultPath] = process.argv.slice(2);
const results = {
  browserOpened: false,
  formInputsSubmitted: false,
  quoteCreated: false,
  quoteSnapshotRendered: false,
  selectionSubmitted: false,
  summaryUpdated: false,
  reloadReadback: false,
  errors: [],
};
let browser;
try {
  console.log('launching chromium');
  browser = await chromium.launch({
    headless: true,
    executablePath: process.env.CHROMIUM_BIN || undefined,
    args: ['--no-sandbox', '--disable-dev-shm-usage'],
    timeout: 10000,
  });
  console.log('chromium launched');
  const page = await browser.newPage();
  page.setDefaultTimeout(1500);
  await page.goto(url, { waitUntil: 'domcontentloaded', timeout: 8000 });
  console.log('React test page loaded');
  results.browserOpened = true;

  try {
    await page.getByLabel('Occupancy').fill('OWNER_OCCUPIED');
    await page.getByLabel('Rebuild amount (INR)').fill('100000.00');
    await page.getByLabel('Coverage').selectOption('BUILDING');
    results.formInputsSubmitted = true;
    await page.getByRole('button', { name: 'Get quote' }).click();
    await page.getByTestId('quote-version').filter({ hasText: 'e2e-quote-v1' }).waitFor({ timeout: 4000 });
    results.quoteCreated = true;
    await page.getByText('125.00').waitFor({ timeout: 1000 });
    results.quoteSnapshotRendered = true;
  } catch (error) {
    results.errors.push(`quote form/snapshot: ${error.message}`);
  }

  try {
    // If quote creation is still a learner TODO, exercise the downstream selection
    // contract against the fixed persisted quote loaded by the page on startup.
    const version = await page.getByTestId('quote-version').textContent();
    await page.getByRole('button', { name: 'Select quote' }).click();
    results.selectionSubmitted = true;
    await page.locator('p[role="status"]').waitFor({ timeout: 4000 });
    results.summaryUpdated = (await page.getByTestId('summary-selections').textContent())?.includes(version ?? '') ?? false;
  } catch (error) {
    results.errors.push(`selection/summary: ${error.message}`);
  }

  try {
    const expectedVersion = await page.getByTestId('quote-version').textContent();
    await page.reload({ waitUntil: 'domcontentloaded' });
    await page.getByTestId('summary-selections').waitFor({ timeout: 4000 });
    results.reloadReadback = (await page.getByTestId('summary-selections').textContent())?.includes(expectedVersion ?? '') ?? false;
  } catch (error) {
    results.errors.push(`reload/readback: ${error.message}`);
  }
} catch (error) {
  results.errors.push(`browser setup: ${error.message}`);
} finally {
  if (browser) await browser.close();
  await writeFile(resultPath, JSON.stringify(results, null, 2));
}
if (Object.values(results).some((value) => value === false)) process.exitCode = 1;
