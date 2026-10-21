import { chromium } from 'playwright';

const [url, phase, resultPath] = process.argv.slice(2);
const browser = await chromium.launch({
  headless: true,
  executablePath: process.env.CHROMIUM_BIN || undefined,
  args: ['--no-sandbox', '--disable-dev-shm-usage'],
  timeout: 10000,
});
try {
  const page = await browser.newPage();
  page.setDefaultTimeout(3000);
  await page.goto(url, { waitUntil: 'domcontentloaded', timeout: 8000 });
  if (phase === 'servicing') {
    const responses = [];
    page.on('response', r => { if (r.url().includes('/test/day44/lifecycle/')) responses.push({url:new URL(r.url()).pathname,status:r.status()}); });
    await page.getByRole('button', { name: 'Run issue and endorsement' }).click();
    const panel = page.getByRole('region', { name: 'Policy servicing' });
    await panel.getByText('policy-a', { exact: true }).waitFor();
    await panel.getByText('125.00 INR', { exact: true }).waitFor();
    await panel.getByText('Version 2', { exact: true }).waitFor();
    await panel.getByText('REBUILD_AMOUNT', { exact: true }).waitFor();
    const result = {phase, browserOpened:true, responses, panelText:await panel.innerText()};
    await page.screenshot({path:resultPath.replace('.json','.png')});
    await import('node:fs/promises').then(({writeFile})=>writeFile(resultPath,JSON.stringify(result,null,2)));
  } else if (phase === 'servicing-failure') {
    const panel = page.getByRole('region', { name: 'Policy servicing' });
    await page.getByRole('button', { name: 'Read servicing summary' }).click();
    await panel.getByText('policy-a', { exact: true }).waitFor();
    const path = '**/test/day44/lifecycle/servicing/policy-a';
    await page.route(path, route => route.abort('failed'));
    await page.getByRole('button', { name: 'Read servicing summary' }).click();
    await panel.getByRole('alert').filter({hasText:'Policy unavailable'}).waitFor();
    if (await panel.getByText('policy-a',{exact:true}).count() !== 0) throw new Error('stale policy remained after failed read');
    await page.unroute(path);
    await page.getByRole('button', { name: 'Read servicing summary' }).click();
    await panel.getByText('policy-a', { exact: true }).waitFor();
    if (await panel.getByRole('alert').count() !== 0) throw new Error('stale alert remained after recovery');
    const result = {phase,browserOpened:true,abortedRequestStatus:null,recovered:true,panelText:await panel.innerText()};
    await page.screenshot({path:resultPath.replace('.json','.png')});
    await import('node:fs/promises').then(({writeFile})=>writeFile(resultPath,JSON.stringify(result,null,2)));
  } else if (phase === 'servicing-cancel') {
    await page.getByRole('button', { name: 'Apply cancellation' }).click();
    const panel = page.getByRole('region', { name: 'Policy servicing' });
    await panel.getByText('CANCELLED', { exact: true }).waitFor();
    await panel.getByText('Version 4', { exact: true }).waitFor();
    await panel.getByText('2026-12-10', { exact: true }).waitFor();
    const result={phase,browserOpened:true,panelText:await panel.innerText()};
    await page.screenshot({path:resultPath.replace('.json','.png')});
    await import('node:fs/promises').then(({writeFile})=>writeFile(resultPath,JSON.stringify(result,null,2)));
  } else if (phase === 'servicing-read') {
    await page.getByRole('button', { name: 'Read servicing summary' }).click();
    const panel = page.getByRole('region', { name: 'Policy servicing' });
    await panel.getByText('CANCELLED', { exact: true }).waitFor();
    await panel.getByText('Version 4', { exact: true }).waitFor();
    const result={phase,browserOpened:true,panelText:await panel.innerText()};
    await page.screenshot({path:resultPath.replace('.json','.png')});
    await import('node:fs/promises').then(({writeFile})=>writeFile(resultPath,JSON.stringify(result,null,2)));
  } else if (phase === 'flow') {
    await page.getByRole('button', { name: 'Run lifecycle API acceptance flow' }).click();
    await page.getByTestId('lifecycle-flow').filter({ hasText: '/test/day44/lifecycle/history:' }).waitFor();
    const flow = await page.getByTestId('lifecycle-flow').textContent();
    const result = { browserOpened: true, phase, flow };
    await import('node:fs/promises').then(({ writeFile }) => writeFile(resultPath, JSON.stringify(result, null, 2)));
  } else if (phase === 'lifecycle-read') {
    await page.getByRole('button', { name: 'Read policy after restart' }).click();
    await page.getByTestId('lifecycle-flow').filter({ hasText: '/test/day44/lifecycle/history:' }).waitFor();
    const result = { browserOpened: true, phase, lifecycleReadback: await page.getByTestId('lifecycle-flow').textContent() };
    await import('node:fs/promises').then(({ writeFile }) => writeFile(resultPath, JSON.stringify(result, null, 2)));
  } else if (phase === 'write') {
    await page.getByRole('button', { name: 'Write synthetic witness' }).click();
    await page.getByRole('status').filter({ hasText: 'saved' }).waitFor();
  } else if (phase === 'read') {
    await page.getByRole('button', { name: 'Read witness' }).click();
  } else {
    throw new Error(`unknown phase ${phase}`);
  }
  if (phase === 'write' || phase === 'read') {
    await page.getByTestId('readback').filter({ hasText: 'lifecycle-smoke-v1:persisted-after-server-restart' }).waitFor();
    const result = { browserOpened: true, phase, readback: await page.getByTestId('readback').textContent() };
    await import('node:fs/promises').then(({ writeFile }) => writeFile(resultPath, JSON.stringify(result, null, 2)));
  }
} finally {
  await browser.close();
}
