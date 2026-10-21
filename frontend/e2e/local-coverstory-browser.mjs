import fs from 'node:fs';
import path from 'node:path';
import { chromium } from 'playwright';
import { createApiCaptureMerger } from './api-capture-merge.mjs';

const [baseUrl, outputPath, scenario] = process.argv.slice(2);
if (!baseUrl || !outputPath || !scenario) throw new Error('Expected base URL, output JSON path, and scenario.');

const sessionCookieInput = process.env.COVERSTORY_SESSION_COOKIE_INPUT;
const sessionCookieOutput = process.env.COVERSTORY_SESSION_COOKIE_OUTPUT;
const milestoneDir = process.env.COVERSTORY_MILESTONE_DIR;
let savedSessionCookie = null;
if (sessionCookieInput) {
  savedSessionCookie = fs.readFileSync(sessionCookieInput, 'utf8');
  fs.rmSync(sessionCookieInput, { force: true });
}

let browser;
const apiResponses = [];
const controls = {};
const result = { apiResponses, controls, completed: false };
const checkpoint = async (name) => {
  if (!milestoneDir) return;
  fs.mkdirSync(milestoneDir, { recursive: true });
  const reached = path.join(milestoneDir, `${name}.reached`);
  const proceed = path.join(milestoneDir, `${name}.continue`);
  fs.writeFileSync(reached, 'observed');
  const deadline = Date.now() + 45000;
  while (!fs.existsSync(proceed)) {
    if (Date.now() > deadline) throw new Error(`Timed out waiting for test observer at ${name}`);
    await new Promise((resolve) => setTimeout(resolve, 25));
  }
};
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
  const waitForOwnerAPreviewCleared = async () => page.waitForFunction(() => {
    const text = document.body.innerText;
    return !text.includes('policy-renewal-a') && !text.includes('125.00')
      && !text.includes('2027-01-15') && !text.includes('home-secure-rate-v1');
  }, null, { timeout: 5000 });

  const pageResponse = await page.goto(baseUrl);
  result.browserOpened = true;
  result.pageStatus = pageResponse?.status() ?? null;
  const heading = await page.getByRole('heading', { name: 'CoverStory local training' }).textContent().catch(() => null);
  result.heading = heading;
  const click = async (name, expectedResponse, afterResponse) => {
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
        await afterResponse?.(response);
      }
    }
    return available;
  };
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
    controls.viewPreviewAfterRestart = await click('View preview', { method: 'GET', path: /\/renewal-preview$/ },
      (response) => response.status() === 200
        ? page.getByRole('region', { name: 'Renewal preview' }).waitFor({ state: 'visible', timeout: 5000 })
        : waitForOwnerAPreviewCleared());
    result.afterRestartSession = apiResponses.find((r) => r.method === 'GET' && r.path === '/api/session') ?? null;
    result.previewAfterRestart = latestPreview();
    result.visibleAfterRestart = await page.locator('body').innerText();
  } else {
    controls.selectOwnerA = await click('Use owner-a', { method: 'POST', path: /^\/api\/session$/ });
    const ownerASessionCookie = await currentSessionCookie();
    if (scenario === 'day68-preview' || scenario === 'day69') {
      controls.viewPreviewAsOwnerA = await click('View preview', { method: 'GET', path: /\/renewal-preview$/ },
        () => page.getByRole('region', { name: 'Renewal preview' }).waitFor({ state: 'visible', timeout: 5000 }));
      result.previewAsOwnerA = latestPreview();
      result.visibleAfterOwnerA = await page.locator('body').innerText();
      if (scenario === 'day69') await checkpoint('after-preview-owner-a');
    }
    if (scenario === 'day68-issue') {
      await checkpoint('before-issue');
      controls.issueSelectedQuote = await click('Issue selected quote', { method: 'POST', path: /^\/api\/policies$/ },
        () => page.getByRole('region', { name: 'Issued policy' }).waitFor({ state: 'visible', timeout: 5000 }));
      await checkpoint('after-issue');
      controls.readIssuedPolicy = await click('View issued policy', { method: 'GET', path: /^\/api\/policies\/[^/]+$/ },
        () => page.getByRole('region', { name: 'Issued policy' }).waitFor({ state: 'visible', timeout: 5000 }));
      await checkpoint('after-read');
      controls.replayIssue = await click('Replay issue request', { method: 'POST', path: /^\/api\/policies$/ },
        () => page.getByRole('region', { name: 'Issued policy' }).waitFor({ state: 'visible', timeout: 5000 }));
      await checkpoint('after-replay');
      result.issueResponses = issues();
      result.readbackResponses = readbacks();
      result.visibleAfterIssueFlow = await page.locator('body').innerText();
    }
    if (scenario === 'day69') {
      controls.selectOwnerB = await click('Use owner-b', { method: 'POST', path: /^\/api\/session$/ },
        () => waitForOwnerAPreviewCleared());
      result.ownerBSessionSelection = apiResponses.filter((r) => r.method === 'POST' && r.path === '/api/session').at(-1) ?? null;
      await checkpoint('after-owner-switch');
      const ownerBSessionCookie = await currentSessionCookie();
      result.ownerSessionRotated = Boolean(ownerASessionCookie && ownerBSessionCookie
        && ownerASessionCookie !== ownerBSessionCookie);
      controls.viewForeignPreview = await click('View preview', { method: 'GET', path: /\/renewal-preview$/ },
        () => waitForOwnerAPreviewCleared());
      result.previewAsOwnerB = latestPreview();
      result.visibleAfterOwnerB = await page.locator('body').innerText();
      await checkpoint('after-foreign-read');
      controls.restoreOwnerAForLogout = await click('Use owner-a', { method: 'POST', path: /^\/api\/session$/ });
      controls.viewPreviewBeforeLogout = await click('View preview', { method: 'GET', path: /\/renewal-preview$/ },
        () => page.getByRole('region', { name: 'Renewal preview' }).waitFor({ state: 'visible', timeout: 5000 }));
      result.visibleBeforeLogout = await page.locator('body').innerText();
      await checkpoint('before-logout');
      controls.logout = await click('Log out', { method: 'POST', path: /^\/api\/session\/logout$/ },
        () => waitForOwnerAPreviewCleared());
      if (controls.logout) {
        await mergePageCaptures();
        await checkpoint('after-logout');
        result.visibleImmediatelyAfterLogout = await page.locator('body').innerText();
        const responseStart = apiResponses.length;
        const reloadedSession = page.waitForResponse((response) => {
          const request = response.request();
          return request.method() === 'GET' && new URL(response.url()).pathname === '/api/session';
        });
        captureDocumentId += 1;
        await page.reload();
        const sessionResponse = await reloadedSession;
        await responseSettlers.get(sessionResponse);
        await waitForOwnerAPreviewCleared();
        result.sessionAfterLogout = apiResponses.slice(responseStart).find((r) => r.method === 'GET' && r.path === '/api/session') ?? null;
        result.visibleAfterLogout = await page.locator('body').innerText();
      }
    }
    if (scenario === 'day69-restart-seed') {
      controls.viewPreviewBeforeRestart = await click('View preview', { method: 'GET', path: /\/renewal-preview$/ });
      await page.getByRole('region', { name: 'Renewal preview' }).waitFor({ state: 'visible', timeout: 5000 });
      result.previewBeforeRestart = latestPreview();
      result.sessionBeforeRestart = await page.evaluate(async () => {
        const response = await fetch('/api/session', { credentials: 'same-origin' });
        return { status: response.status, body: await response.text() };
      });
      result.sessionCookieActiveBeforeRestart = result.sessionBeforeRestart.status === 200;
      result.visibleBeforeRestart = await page.locator('body').innerText();
      if (sessionCookieOutput) {
        fs.mkdirSync(path.dirname(sessionCookieOutput), { recursive: true });
        fs.writeFileSync(sessionCookieOutput, sessionCookie ?? '', { mode: 0o600 });
      }
      const exactSavedCookie = sessionCookieOutput ? fs.readFileSync(sessionCookieOutput, 'utf8') : sessionCookie;
      const exactCookieResponse = await fetch(`${baseUrl}/api/session`, { headers: { Cookie: `JSESSIONID=${exactSavedCookie}` } });
      result.savedCookieStatusBeforeStop = exactCookieResponse.status;
      result.savedCookieActiveBeforeStop = exactCookieResponse.status === 200;
      if (!result.savedCookieActiveBeforeStop) throw new Error('Exact saved restart cookie is not active before stop.');
    }
  }

  await mergePageCaptures();
  if (sessionCookieOutput && scenario === 'day69-restart-seed') {
    fs.mkdirSync(path.dirname(sessionCookieOutput), { recursive: true });
    fs.writeFileSync(sessionCookieOutput, sessionCookie ?? '', { mode: 0o600 });
  }
  if (sessionCookieOutput && scenario === 'day69') {
    fs.mkdirSync(path.dirname(sessionCookieOutput), { recursive: true });
    fs.writeFileSync(sessionCookieOutput, sessionCookie ?? '', { mode: 0o600 });
    const exactLoggedOutCookie = fs.readFileSync(sessionCookieOutput, 'utf8');
    const loggedOutCookieResponse = await fetch(`${baseUrl}/api/session`, { headers: { Cookie: `JSESSIONID=${exactLoggedOutCookie}` } });
    result.savedLogoutCookieStatusWhileServerAlive = loggedOutCookieResponse.status;
    result.savedLogoutCookieRejectedBeforeStop = loggedOutCookieResponse.status === 401;
    if (!result.savedLogoutCookieRejectedBeforeStop) throw new Error('Saved logout cookie must already be invalid while the first server is alive.');
  }
  fs.mkdirSync(path.dirname(outputPath), { recursive: true });
  result.completed = true;
  fs.writeFileSync(outputPath, JSON.stringify(result, null, 2));
  await context.close();
} catch (error) {
  result.completed = false;
  result.failure = { name: error?.name ?? 'Error', message: error?.message ?? String(error) };
  fs.mkdirSync(path.dirname(outputPath), { recursive: true });
  fs.writeFileSync(outputPath, JSON.stringify(result, null, 2));
  throw error;
} finally {
  await browser?.close();
}
