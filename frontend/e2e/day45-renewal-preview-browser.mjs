import { chromium } from 'playwright';
import { mkdir, writeFile } from 'node:fs/promises';
import { dirname } from 'node:path';

const [url, outputPath] = process.argv.slice(2);
if (!url || !outputPath) throw new Error('Usage: node day45-renewal-preview-browser.mjs URL OUTPUT');
await mkdir(dirname(outputPath), { recursive: true });

const browser = await chromium.launch({
  headless: true,
  executablePath: process.env.CHROMIUM_BIN || undefined,
  args: ['--no-sandbox', '--disable-dev-shm-usage'],
  timeout: 10000,
});
try {
  const page = await browser.newPage();
  page.setDefaultTimeout(3000);
  page.on('console', message => console.log(`browser console ${message.type()}: ${message.text()}`));
  page.on('pageerror', error => console.log(`browser page error: ${error.message}`));
  page.on('requestfailed', request => console.log(`browser request failed ${request.url()}: ${request.failure()?.errorText}`));
  page.on('request', request => console.log(`browser request ${request.method()} ${request.url()}`));
  page.on('response', response => console.log(`browser response ${response.status()} ${response.url()}`));
  await page.goto(url, { waitUntil: 'domcontentloaded', timeout: 8000 });
  const responsePromise = new Promise((resolve, reject) => {
    const timer = setTimeout(() => reject(new Error('No renewal preview API response observed within 10 seconds')), 10000);
    page.on('response', value => {
      if (value.url().includes('/api/policies/renewal-demo/renewal-preview')) {
        clearTimeout(timer);
        resolve(value);
      }
    });
  });
  await page.getByRole('button', { name: 'Check preview' }).click();
  const response = await responsePromise;
  // A learner TODO can make Spring start an error dispatch without closing the response body promptly.
  // Keep the red-state harness finite while preserving status and rendered page evidence.
  const body = response.status() >= 500 ? '' : await response.text();
  const output = {
    browserOpened: true,
    status: response.status(),
    body,
    visibleText: await page.locator('#root').innerText(),
  };
  await writeFile(outputPath, JSON.stringify(output, null, 2));
} finally {
  await browser.close();
}
