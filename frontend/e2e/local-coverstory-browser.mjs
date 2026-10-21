import fs from 'node:fs';
import path from 'node:path';
import { chromium } from 'playwright';
import { createApiCaptureMerger } from './api-capture-merge.mjs';

const [baseUrl, outputPath, scenario] = process.argv.slice(2);
if (!baseUrl || !outputPath || !scenario) throw new Error('Expected base URL, output JSON path, and scenario.');

const sessionCookieInput = process.env.COVERSTORY_SESSION_COOKIE_INPUT;
const sessionCookieOutput = process.env.COVERSTORY_SESSION_COOKIE_OUTPUT;
let savedSessionCookie = null;
if (sessionCookieInput) {
  savedSessionCookie = fs.readFileSync(sessionCookieInput, 'utf8');
  fs.rmSync(sessionCookieInput, { force: true });
}

let browser;
try {
  browser = await chromium.launch({
    headless: true,
    ...(process.env.CHROMIUM_BIN ? { executablePath: process.env.CHROMIUM_BIN } : {}),
    args: process.env.CHROMIUM_BIN ? ['--no-sandbox'] : [],
  });
  const context = await browser.newContext();
  if (scenario === 'day69-restart-check' && savedSessionCookie) {
    await context.addCookies([{ name: 'JSESSIONID', value: savedSessionCookie, url: baseUrl }]);
  }
  const page = await context.newPage();
  const apiResponses = [];
  const responseSettlers = new WeakMap();
  const mergeApiCaptures = createApiCaptureMerger(apiResponses);
  let captureDocumentId = 0;
  let sessionCookie = savedSessionCookie ?? null;
  page.on('response', (response) => {
    const request = response.request();
    const url = new URL(response.url());
    if (!url.pathname.startsWith('/api/')) return;
    const entry = { method: request.method(), path: url.pathname, status: response.status(), body: '' };
    apiResponses.push(entry);
    const settleSession = request.method() === 'POST' && url.pathname === '/api/session'
      ? response.allHeaders().then((headers) => {
        const match = (headers['set-cookie'] ?? '').match(/(?:^|,\s*)JSESSIONID=([^; ,]+)/);
        if (match) sessionCookie = match[1];
      })
      : Promise.resolve();
    responseSettlers.set(response, settleSession);
  });
  await page.addInitScript(() => {
    window.__coverStoryApiCaptures = [];
    window.__coverStoryApiCapturePromises = [];
    const originalFetch = window.fetch.bind(window);
    window.fetch = async (input, init) => {
      const response = await originalFetch(input, init);
      const inputUrl = input instanceof Request ? input.url : String(input);
      const url = new URL(inputUrl, window.location.href);
      if (url.pathname.startsWith('/api/')) {
        const requestMethod = init?.method ?? (input instanceof Request ? input.method : 'GET');
        const capture = {
          method: requestMethod.toUpperCase(), path: url.pathname, status: response.status, body: '',
          requestBody: init?.body == null ? null : String(init.body),
        };
        window.__coverStoryApiCaptures.push(capture);
        window.__coverStoryApiCapturePromises.push(
          response.clone().text().then((body) => { capture.body = body; }).catch(() => {})
        );
      }
      return response;
    };
  });
  const mergePageCaptures = async () => {
    const captures = await page.evaluate(async () => {
      await Promise.all(window.__coverStoryApiCapturePromises);
      return window.__coverStoryApiCaptures;
    });
    mergeApiCaptures(captures, captureDocumentId);
  };

  const pageResponse = await page.goto(baseUrl);
  const heading = await page.getByRole('heading', { name: 'CoverStory local training' }).textContent().catch(() => null);
  const click = async (name, expectedResponse) => {
    const button = page.getByRole('button', { name, exact: true });
    try { await button.waitFor({ state: 'visible', timeout: 2500 }); } catch { /* retain a finite missing-control result */ }
    const available = (await button.count()) > 0;
    if (available) {
      const responsePromise = expectedResponse
        ? page.waitForResponse((response) => {
          const request = response.request();
          const url = new URL(response.url());
          return request.method() === expectedResponse.method && expectedResponse.path.test(url.pathname);
        })
        : null;
      await button.first().click({ timeout: 3000 });
      if (responsePromise) {
        const response = await responsePromise;
        await responseSettlers.get(response);
        await mergePageCaptures();
      }
    }
    return available;
  };
  const controls = {};
  const result = { apiResponses, controls };
  result.prohibitedControls = {
    buttons: await page.getByRole('button', { name: /purchase|renew now|buy coverage/i }).count(),
    links: await page.getByRole('link', { name: /purchase|renew now|buy coverage/i }).count(),
  };
  const currentSessionCookie = async () => (await context.cookies(baseUrl)).find((cookie) => cookie.name === 'JSESSIONID')?.value ?? null;
  const previews = () => apiResponses.filter((r) => r.method === 'GET' && r.path.endsWith('/renewal-preview'));
  const latestPreview = () => previews().at(-1) ?? null;
  const issues = () => apiResponses.filter((r) => r.method === 'POST' && r.path === '/api/policies');
  const readbacks = () => apiResponses.filter((r) => r.method === 'GET' && /^\/api\/policies\/[^/]+$/.test(r.path));

  if (scenario === 'day69-restart-check') {
    controls.reselectOwnerA = await click('Use owner-a', { method: 'POST', path: /^\/api\/session$/ });
    controls.viewPreviewAfterRestart = await click('View preview', { method: 'GET', path: /\/renewal-preview$/ });
    result.afterRestartSession = apiResponses.find((r) => r.method === 'GET' && r.path === '/api/session') ?? null;
    result.previewAfterRestart = latestPreview();
    result.visibleAfterRestart = await page.locator('body').innerText();
  } else {
    controls.selectOwnerA = await click('Use owner-a', { method: 'POST', path: /^\/api\/session$/ });
    const ownerASessionCookie = await currentSessionCookie();
    if (scenario === 'day68-preview' || scenario === 'day69') {
      controls.viewPreviewAsOwnerA = await click('View preview', { method: 'GET', path: /\/renewal-preview$/ });
      result.previewAsOwnerA = latestPreview();
      result.visibleAfterOwnerA = await page.locator('body').innerText();
    }
    if (scenario === 'day68-issue') {
      controls.issueSelectedQuote = await click('Issue selected quote', { method: 'POST', path: /^\/api\/policies$/ });
      controls.readIssuedPolicy = await click('View issued policy', { method: 'GET', path: /^\/api\/policies\/[^/]+$/ });
      controls.replayIssue = await click('Replay issue request', { method: 'POST', path: /^\/api\/policies$/ });
      result.issueResponses = issues();
      result.readbackResponses = readbacks();
      result.visibleAfterIssueFlow = await page.locator('body').innerText();
    }
    if (scenario === 'day69') {
      controls.selectOwnerB = await click('Use owner-b', { method: 'POST', path: /^\/api\/session$/ });
      result.ownerBSessionSelection = apiResponses.filter((r) => r.method === 'POST' && r.path === '/api/session').at(-1) ?? null;
      const ownerBSessionCookie = await currentSessionCookie();
      result.ownerSessionRotated = Boolean(ownerASessionCookie && ownerBSessionCookie
        && ownerASessionCookie !== ownerBSessionCookie);
      controls.viewForeignPreview = await click('View preview', { method: 'GET', path: /\/renewal-preview$/ });
      result.previewAsOwnerB = latestPreview();
      result.visibleAfterOwnerB = await page.locator('body').innerText();
      controls.logout = await click('Log out', { method: 'POST', path: /^\/api\/session\/logout$/ });
      if (controls.logout) {
        await mergePageCaptures();
        const responseStart = apiResponses.length;
        const reloadedSession = page.waitForResponse((response) => {
          const request = response.request();
          return request.method() === 'GET' && new URL(response.url()).pathname === '/api/session';
        });
        captureDocumentId += 1;
        await page.reload();
        const sessionResponse = await reloadedSession;
        await responseSettlers.get(sessionResponse);
        result.sessionAfterLogout = apiResponses.slice(responseStart).find((r) => r.method === 'GET' && r.path === '/api/session') ?? null;
        result.visibleAfterLogout = await page.locator('body').innerText();
      }
    }
    if (scenario === 'day69-restart-seed') {
      controls.viewPreviewBeforeRestart = await click('View preview', { method: 'GET', path: /\/renewal-preview$/ });
      result.previewBeforeRestart = latestPreview();
      result.sessionBeforeRestart = await page.evaluate(async () => {
        const response = await fetch('/api/session', { credentials: 'same-origin' });
        return { status: response.status, body: await response.text() };
      });
      result.sessionCookieActiveBeforeRestart = result.sessionBeforeRestart.status === 200;
      result.visibleBeforeRestart = await page.locator('body').innerText();
    }
  }

  await mergePageCaptures();
  if (sessionCookieOutput && scenario === 'day69-restart-seed') {
    fs.mkdirSync(path.dirname(sessionCookieOutput), { recursive: true });
    fs.writeFileSync(sessionCookieOutput, sessionCookie ?? '', { mode: 0o600 });
  }
  fs.mkdirSync(path.dirname(outputPath), { recursive: true });
  fs.writeFileSync(outputPath, JSON.stringify({ browserOpened: true, pageStatus: pageResponse?.status(), heading, ...result }, null, 2));
  await context.close();
} finally {
  await browser?.close();
}
