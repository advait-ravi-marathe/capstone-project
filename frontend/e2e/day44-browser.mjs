import { chromium } from 'playwright';

const [url, phase, resultPath] = process.argv.slice(2);
const browser = await chromium.launch({
  headless: true,
  executablePath: process.env.CHROMIUM_BIN || undefined,
  args: ['--no-sandbox', '--disable-dev-shm-usage'],
  timeout: 10000,
});
const api = path => new URL(path, url).toString();
const canonical = value => Array.isArray(value) ? value.map(canonical) : value && typeof value === 'object'
  ? Object.fromEntries(Object.keys(value).sort().map(key => [key, canonical(value[key])])) : value;
const stable = value => JSON.stringify(canonical(value));
const assert = (condition, message) => { if (!condition) throw new Error(message); };

async function readDiagnostics(page) {
  const response = await page.request.get(api('/test/day44/lifecycle/diagnostics'));
  assert(response.ok(), `diagnostics returned ${response.status()}`);
  return response.json();
}

function assertCounts(actual, expected, label) {
  assert(actual.policies === expected.policies && actual.receipts === expected.receipts && actual.events === expected.events,
    `${label} expected ${JSON.stringify(expected)} got ${JSON.stringify({policies:actual.policies,receipts:actual.receipts,events:actual.events})}`);
}

function assertSummary(summary, {status, version, cancellationEffectiveDate, history}) {
  assert(summary.policyId === 'policy-a', 'wrong policy id');
  assert(summary.productVersion === 'home-secure-v1', 'wrong product version');
  assert(summary.annualPremiumRupees === '125.00', 'annual premium must remain 125.00');
  assert(summary.currency === 'INR', 'wrong currency');
  assert(summary.status === status, `wrong status: ${summary.status}`);
  assert(summary.version === version, `wrong version: ${summary.version}`);
  assert(summary.cancellationEffectiveDate === cancellationEffectiveDate, 'wrong cancellation date');
  assert(stable(summary.history) === stable(history), `wrong persisted history: ${JSON.stringify(summary.history)}`);
  const keys = Object.keys(summary).sort().join(',');
  assert(keys === 'annualPremiumRupees,cancellationEffectiveDate,currency,history,policyId,productVersion,status,version', `projection keys are not allowlisted: ${keys}`);
  return summary;
}

function watchLifecycleResponses(page) {
  const tasks = [];
  page.on('response', response => {
    const parsed = new URL(response.url());
    if (parsed.pathname.startsWith('/test/day44/lifecycle/')) {
      tasks.push((async () => {
        let body = null;
        try { body = await response.json(); } catch { body = null; }
        return {path:parsed.pathname,status:response.status(),body};
      })());
    }
  });
  return async () => Promise.all(tasks);
}

function compactSnapshot(snapshot) {
  if (!snapshot) return null;
  return {
    policyId:snapshot.policyId, ownerId:snapshot.ownerId, journeyId:snapshot.journeyId,
    quoteVersionId:snapshot.quoteVersionId, productVersion:snapshot.productVersion,
    rateVersion:snapshot.rateVersion, annualPremiumRupees:String(snapshot.annualPremiumRupees),
    currency:snapshot.currency, effectiveDate:snapshot.effectiveDate, status:snapshot.status,
    version:snapshot.version, endorsements:snapshot.endorsements,
    cancellationEffectiveDate:snapshot.cancellationEffectiveDate,
  };
}

try {
  const page = await browser.newPage();
  page.setDefaultTimeout(5000);
  await page.goto(url, { waitUntil: 'domcontentloaded', timeout: 8000 });
  if (phase === 'servicing') {
    const responses = watchLifecycleResponses(page);
    const sequenceButton=page.getByRole('button', { name: 'Run issue and endorsement' });
    await sequenceButton.click();
    await page.waitForFunction(() => [...document.querySelectorAll('button')].some(button => button.textContent?.trim()==='Run issue and endorsement' && button.disabled));
    assert(await page.getByRole('button',{name:'Apply cancellation'}).isDisabled(),'write controls must stay disabled while the sequence is in flight');
    await sequenceButton.click({force:true});
    await page.waitForFunction(() => [...document.querySelectorAll('button')].some(button => button.textContent?.trim()==='Run issue and endorsement' && !button.disabled));
    const captured = await responses();
    const panel = page.getByRole('region', { name: 'Policy servicing' });
    assert(await panel.getByText('policy-a', { exact: true }).count() === 1,
      `servicing panel did not show policy-a; panel=${await panel.innerText()} HTTP=${JSON.stringify(captured.map(x=>({path:x.path,status:x.status,body:x.body})))}`);
    const replayCheckpoint = JSON.parse(await page.getByTestId('servicing-checkpoint').textContent());
    const finalDatabase = await readDiagnostics(page);
    const issue = captured.find(item => item.path.endsWith('/issue'));
    const replay = captured.find(item => item.path.endsWith('/replay'));
    const endorsement = captured.find(item => item.path.endsWith('/endorsement'));
    const summaryResponse = captured.find(item => item.path.endsWith('/servicing/policy-a'));
    assert(issue?.status === 200 && replay?.status === 200 && endorsement?.status === 200, `unexpected issue/replay/endorsement responses: ${JSON.stringify(captured.map(x=>[x.path,x.status]))}`);
    assert(captured.filter(item=>item.path.endsWith('/issue')).length===1 && captured.filter(item=>item.path.endsWith('/replay')).length===1 && captured.filter(item=>item.path.endsWith('/endorsement')).length===1,'duplicate in-flight write sequence was not suppressed');
    assert(stable(issue.body) === stable(replay.body), 'replay must return the exact original issue snapshot');
    const expectedIssued = {policyId:'policy-a',ownerId:'owner-a',journeyId:'journey-a',quoteVersionId:'quote-a',productVersion:'home-secure-v1',rateVersion:'training-rate-v1',annualPremiumRupees:'125',currency:'INR',effectiveDate:'2026-12-01',status:'ISSUED',version:1,endorsements:[],cancellationEffectiveDate:null};
    assert(stable(compactSnapshot(issue.body)) === stable(expectedIssued), 'issue response differs from the complete expected synthetic snapshot');
    assertCounts(replayCheckpoint, {policies:1,receipts:1,events:1}, 'after replay and before endorsement');
    assertCounts(finalDatabase, {policies:1,receipts:1,events:2}, 'after endorsement');
    assert(finalDatabase.annualPremiumRupees === '125.00', 'endorsement amount must not overwrite annual premium');
    const summary = assertSummary(summaryResponse?.body, {status:'ISSUED',version:2,cancellationEffectiveDate:null,history:[{version:2,effectiveDate:'2026-12-02',changeCode:'REBUILD_AMOUNT'}]});
    await panel.getByText('125.00 INR', { exact: true }).waitFor();
    await panel.getByText('ISSUED', { exact: true }).waitFor();
    await panel.getByText('Version 2', { exact: true }).waitFor();
    assert(await panel.getByRole('listitem').filter({hasText:'REBUILD_AMOUNT'}).filter({hasText:'2026-12-02'}).count() === 1,
      `history item absent; panel=${await panel.innerText()} summary=${JSON.stringify(summary)}`);
    const panelText = await panel.innerText();
    assert(!/owner-a|journey-a|quote-a|training-rate-v1/.test(panelText), 'private source identifiers appeared in the panel');
    const result = {phase,browserOpened:true,http:captured.map(x=>({path:x.path,status:x.status,body:x.path.endsWith('/servicing/policy-a')?x.body:compactSnapshot(x.body)})),replayCheckpoint,afterEndorsement:finalDatabase,summary,panelText};
    await page.screenshot({path:resultPath.replace('.json','.png')});
    await import('node:fs/promises').then(({writeFile})=>writeFile(resultPath,JSON.stringify(result,null,2)));
  } else if (phase === 'servicing-failure') {
    const responses = watchLifecycleResponses(page);
    const failures = [];
    page.on('requestfailed', request => {
      if (new URL(request.url()).pathname === '/test/day44/lifecycle/servicing/policy-a') failures.push({path:new URL(request.url()).pathname,failure:request.failure()?.errorText ?? 'unknown'});
    });
    const panel = page.getByRole('region', { name: 'Policy servicing' });
    await page.getByRole('button', { name: 'Read servicing summary' }).click();
    await panel.getByText('policy-a', { exact: true }).waitFor();
    const before = await readDiagnostics(page);
    const path = '**/test/day44/lifecycle/servicing/policy-a';
    await page.route(path, route => route.abort('failed'));
    await page.getByRole('button', { name: 'Read servicing summary' }).click();
    await panel.getByRole('alert').filter({hasText:'Policy unavailable'}).waitFor();
    for (const old of ['policy-a','home-secure-v1','125.00 INR','ISSUED','Version 2','REBUILD_AMOUNT','2026-12-02'])
      assert(await panel.getByText(old,{exact:true}).count() === 0, `stale value remained after failed read: ${old}`);
    const failureScreenshot = resultPath.replace('.json','-failure.png');
    await page.screenshot({path:failureScreenshot});
    const failedPanelText = await panel.innerText();
    await page.unroute(path);
    await page.getByRole('button', { name: 'Read servicing summary' }).click();
    await panel.getByText('policy-a', { exact: true }).waitFor();
    assert(await panel.getByRole('alert').count() === 0, 'stale alert remained after recovery');
    const after = await readDiagnostics(page);
    assert(stable(before) === stable(after), 'database state changed during failed read/recovery');
    assert(failures.length === 1, `expected one aborted GET, got ${failures.length}`);
    const captured = await responses();
    const summaries = captured.filter(item=>item.path.endsWith('/servicing/policy-a'));
    assert(summaries.length === 2 && summaries.every(item=>item.status===200), `successful reads should bracket one abort: ${JSON.stringify(summaries.map(x=>x.status))}`);
    const recovered = assertSummary(summaries[1].body, {status:'ISSUED',version:2,cancellationEffectiveDate:null,history:[{version:2,effectiveDate:'2026-12-02',changeCode:'REBUILD_AMOUNT'}]});
    for (const value of ['125.00 INR','ISSUED','Version 2']) await panel.getByText(value,{exact:true}).waitFor();
    assert(await panel.getByRole('listitem').filter({hasText:'REBUILD_AMOUNT'}).filter({hasText:'2026-12-02'}).count()===1,'recovered endorsement history is incomplete');
    const result = {phase,browserOpened:true,abortedRequestStatus:null,observedNetworkFailure:failures[0],successfulReads:summaries.map(x=>({status:x.status,body:x.body})),before,after,recovered,failedPanelText,panelText:await panel.innerText(),screenshots:[failureScreenshot,resultPath.replace('.json','.png')]};
    await page.screenshot({path:resultPath.replace('.json','.png')});
    await import('node:fs/promises').then(({writeFile})=>writeFile(resultPath,JSON.stringify(result,null,2)));
  } else if (phase === 'servicing-cancel' || phase === 'servicing-read') {
    const responses = watchLifecycleResponses(page);
    const panel = page.getByRole('region', { name: 'Policy servicing' });
    if (phase === 'servicing-cancel') {
      await page.getByRole('button', { name: 'Apply cancellation' }).click();
      await panel.getByText('CANCELLED', { exact: true }).waitFor();
    } else {
      await page.getByRole('button', { name: 'Read servicing summary' }).click();
      await panel.getByText('CANCELLED', { exact: true }).waitFor();
    }
    const capturedBeforeHistory = await responses();
    const summaryResponse = capturedBeforeHistory.find(item=>item.path.endsWith('/servicing/policy-a'));
    const summary = assertSummary(summaryResponse?.body,{status:'CANCELLED',version:4,cancellationEffectiveDate:'2026-12-10',history:[{version:2,effectiveDate:'2026-12-02',changeCode:'REBUILD_AMOUNT'}]});
    for (const value of ['policy-a','home-secure-v1','125.00 INR','CANCELLED','Version 4','2026-12-10']) await panel.getByText(value,{exact:true}).waitFor();
    assert(await panel.getByRole('listitem').filter({hasText:'REBUILD_AMOUNT'}).filter({hasText:'2026-12-02'}).count()===1,'cancelled endorsement history is incomplete');
    const historyResponse = await page.request.get(api('/test/day44/lifecycle/history'));
    assert(historyResponse.ok(), `history returned ${historyResponse.status()}`);
    const history = await historyResponse.json();
    assert(history.status==='CANCELLED' && history.version===4 && history.eventCount===4, `wrong persisted history result: ${JSON.stringify(history)}`);
    const database = await readDiagnostics(page);
    assertCounts(database,{policies:1,receipts:1,events:4},'cancelled lifecycle');
    assert(database.annualPremiumRupees==='125.00' && database.cancellationEffectiveDate==='2026-12-10','cancelled database summary differs from frozen expectation');
    assert(!/owner-a|journey-a|quote-a|training-rate-v1/.test(await panel.innerText()),'private source identifiers appeared after cancellation');
    const result = {phase,browserOpened:true,http:capturedBeforeHistory.map(x=>({path:x.path,status:x.status,body:x.path.endsWith('/servicing/policy-a')?x.body:compactSnapshot(x.body)})),history,database,summary,panelText:await panel.innerText(),freshBrowser:phase==='servicing-read'};
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
