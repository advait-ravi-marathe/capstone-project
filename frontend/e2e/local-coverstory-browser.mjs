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
  const context = await browser.newContext();
  if (scenario === 'day69-restart-check' && savedSessionCookie) {
    await context.addCookies([{ name: 'JSESSIONID', value: savedSessionCookie, url: baseUrl }]);
  }
  const page = await context.newPage();
  const apiResponses = [];
  let sessionCookie = savedSessionCookie ?? null;
  page.on('response', async (response) => {
    const request = response.request();
    const url = new URL(response.url());
    if (!url.pathname.startsWith('/api/')) return;
    const body = await response.text().catch(() => '');
    apiResponses.push({ method: request.method(), path: url.pathname, status: response.status(), body });
    if (request.method() === 'POST' && url.pathname === '/api/session') {
      const cookie = (await response.headers())['set-cookie'] ?? '';
      const match = cookie.match(/(?:^|,\s*)JSESSIONID=([^; ,]+)/);
      if (match) sessionCookie = match[1];
    }
  });

  const pageResponse = await page.goto(baseUrl);
  const heading = await page.getByRole('heading', { name: 'CoverStory local training' }).textContent().catch(() => null);
  const click = async (name) => {
    const button = page.getByRole('button', { name, exact: true });
    const available = (await button.count()) > 0;
    if (available) {
      await button.first().click({ timeout: 3000 });
      await page.waitForTimeout(75);
    }
    return available;
  };
  const controls = {};
  const result = { apiResponses, controls };
  const currentSessionCookie = async () => (await context.cookies(baseUrl)).find((cookie) => cookie.name === 'JSESSIONID')?.value ?? null;
  const previews = () => apiResponses.filter((r) => r.method === 'GET' && r.path.endsWith('/renewal-preview'));
  const latestPreview = () => previews().at(-1) ?? null;
  const issues = () => apiResponses.filter((r) => r.method === 'POST' && r.path === '/api/policies');
  const readbacks = () => apiResponses.filter((r) => r.method === 'GET' && /^\/api\/policies\/[^/]+$/.test(r.path));

  if (scenario === 'day69-restart-check') {
    controls.reselectOwnerA = await click('Use owner-a');
    controls.viewPreviewAfterRestart = await click('View preview');
    result.afterRestartSession = apiResponses.find((r) => r.method === 'GET' && r.path === '/api/session') ?? null;
    result.previewAfterRestart = latestPreview();
  } else {
    controls.selectOwnerA = await click('Use owner-a');
    result.ownerASessionCookie = await currentSessionCookie();
    if (scenario === 'day68-preview' || scenario === 'day69') {
      controls.viewPreviewAsOwnerA = await click('View preview');
      result.previewAsOwnerA = latestPreview();
    }
    if (scenario === 'day68-issue') {
      controls.issueSelectedQuote = await click('Issue selected quote');
      controls.readIssuedPolicy = await click('View issued policy');
      controls.replayIssue = await click('Replay issue request');
      result.issueResponses = issues();
      result.readbackResponses = readbacks();
    }
    if (scenario === 'day69') {
      controls.selectOwnerB = await click('Use owner-b');
      result.ownerBSessionSelection = apiResponses.filter((r) => r.method === 'POST' && r.path === '/api/session').at(-1) ?? null;
      result.ownerBSessionCookie = await currentSessionCookie();
      result.sessionCookie = result.ownerBSessionCookie;
      controls.viewForeignPreview = await click('View preview');
      result.previewAsOwnerB = latestPreview();
      controls.logout = await click('Log out');
      const responseStart = apiResponses.length;
      await page.reload();
      result.sessionAfterLogout = apiResponses.slice(responseStart).find((r) => r.method === 'GET' && r.path === '/api/session') ?? null;
    }
  }

  fs.mkdirSync(path.dirname(outputPath), { recursive: true });
  fs.writeFileSync(outputPath, JSON.stringify({ browserOpened: true, pageStatus: pageResponse?.status(), heading, ...result }, null, 2));
  await context.close();
} finally {
  await browser.close();
}
