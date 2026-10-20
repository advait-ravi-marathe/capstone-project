import fs from 'node:fs';
import path from 'node:path';
import { chromium } from 'playwright';

const [baseUrl, outputPath, scenario, savedSessionCookie] = process.argv.slice(2);
if (!baseUrl || !outputPath || !scenario) throw new Error('Expected base URL, output JSON path, and scenario.');

const browser = await chromium.launch({
  headless: true,
  ...(process.env.CHROMIUM_BIN ? { executablePath: process.env.CHROMIUM_BIN } : {}),
  args: process.env.CHROMIUM_BIN ? ['--no-sandbox'] : [],
});

try {
  const page = await browser.newPage();
  let sessionCookie = null;
  page.on('request', async (request) => {
    if (!new URL(request.url()).pathname.startsWith('/api/')) return;
    const cookieHeader = (await request.allHeaders()).cookie ?? '';
    const match = cookieHeader.match(/(?:^|;\s*)JSESSIONID=([^; ,]+)/);
    if (match) sessionCookie = match[1];
  });
  page.on('response', async (response) => {
    if (response.request().method() !== 'POST' || new URL(response.url()).pathname !== '/api/session') return;
    const setCookie = (await response.headers())['set-cookie'] ?? '';
    const match = setCookie.match(/(?:^|,\s*)JSESSIONID=([^; ,]+)/);
    if (match) sessionCookie = match[1];
  });
  if (scenario === 'day69-restart-check' && savedSessionCookie) {
    await page.context().addCookies([{ name: 'JSESSIONID', value: savedSessionCookie, url: baseUrl }]);
  }
  const pageResponse = await page.goto(`${baseUrl}/local.html`);
  const heading = await page.getByRole('heading', { name: 'CoverStory local training' }).textContent().catch(() => null);
  const result = await page.evaluate(async (flow) => {
    async function session() {
      const response = await fetch('/api/session', { credentials: 'same-origin' });
      return { status: response.status, body: await response.text() };
    }
    const csrfResponse = await fetch('/api/csrf', { credentials: 'same-origin' });
    const csrf = await csrfResponse.json();
    async function select(ownerId) {
      const response = await fetch('/api/session', {
        method: 'POST', credentials: 'same-origin',
        headers: { 'content-type': 'application/json', [csrf.headerName]: csrf.token },
        body: JSON.stringify({ ownerId }),
      });
      return { status: response.status, body: await response.text() };
    }
    async function preview() {
      const response = await fetch('/api/policies/policy-renewal-a/renewal-preview', { credentials: 'same-origin' });
      return { status: response.status, body: await response.text() };
    }
    if (flow === 'day69-restart-check') return { afterRestart: await session() };
    const health = await fetch('/actuator/health');
    const anonymousBeforeSession = await session();
    const selectedOwnerA = await select('owner-a');
    const authenticatedOwnerA = await session();
    const previewAsOwnerA = await preview();
    if (flow === 'day68') {
      return { healthStatus: health.status, anonymousBeforeSession, selectedOwnerA, authenticatedOwnerA, previewAsOwnerA };
    }
    const selectedOwnerB = await select('owner-b');
    const previewAsOwnerB = await preview();
    const logoutResponse = await fetch('/api/session/logout', {
      method: 'POST', credentials: 'same-origin',
      headers: { 'content-type': 'application/json', [csrf.headerName]: csrf.token }, body: '{}',
    });
    const afterLogout = await session();
    return {
      healthStatus: health.status, anonymousBeforeSession, selectedOwnerA, authenticatedOwnerA,
      previewAsOwnerA, selectedOwnerB, previewAsOwnerB,
      logoutStatus: logoutResponse.status, afterLogout,
    };
  }, scenario);
  await page.waitForTimeout(25);
  if (scenario === 'day69') result.sessionCookie = sessionCookie;
  fs.mkdirSync(path.dirname(outputPath), { recursive: true });
  fs.writeFileSync(outputPath, JSON.stringify({ browserOpened: true, pageStatus: pageResponse?.status(), heading, ...result }, null, 2));
  await page.close();
} finally {
  await browser.close();
}
