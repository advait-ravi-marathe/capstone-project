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
const observedResponses = [];
const observedNetworkFailures = [];
const responseTasks = [];
let activePage = null;
let failureScreenshot = null;
let unavailableScreenshot = null;
let failedPanelText = null;
const intermediateObservations = {};

function beginActualCapture(page) {
  activePage = page;
  page.on('response', response => {
    const parsed = new URL(response.url());
    if (!parsed.pathname.startsWith('/test/day44/lifecycle/')) return;
    responseTasks.push((async () => {
      let body = null;
      try { body = await response.json(); } catch { body = null; }
      observedResponses.push({path:parsed.pathname,status:response.status(),body});
    })());
  });
  page.on('requestfailed', request => {
    const parsed = new URL(request.url());
    if (parsed.pathname.startsWith('/test/day44/lifecycle/'))
      observedNetworkFailures.push({path:parsed.pathname,failure:request.failure()?.errorText ?? 'unknown'});
  });
}

async function writeResult(path, result) {
  await import('node:fs/promises').then(({writeFile}) => writeFile(path, JSON.stringify({
    ...result, actualArgv:process.argv, actualCwd:process.cwd(),
  }, null, 2)));
}

async function readDiagnostics(page) {
  const response = await page.request.get(api('/test/day44/lifecycle/diagnostics'));
  assert(response.ok(), `diagnostics returned ${response.status()}`);
  return response.json();
}

function assertCounts(actual, expected, label) {
  assert(actual.policies === expected.policies && actual.receipts === expected.receipts && actual.events === expected.events,
    `${label} expected ${JSON.stringify(expected)} got ${JSON.stringify({policies:actual.policies,receipts:actual.receipts,events:actual.events})}`);
}

function assertFullDatabase(db, {status, version, cancellationEffectiveDate, endorsement}) {
  assert(db.policyRows.length === 1 && db.receiptRows.length === 1 && db.eventRows.length === db.events,
    `full row sets do not match counts: ${JSON.stringify(db)}`);
  const policy = db.policyRows[0];
  assert(policy.policyId==='policy-a' && policy.ownerId==='owner-a' && policy.journeyId==='journey-a' && policy.quoteVersionId==='quote-a', 'policy identity columns differ');
  assert(policy.productVersion==='home-secure-v1' && policy.rateVersion==='training-rate-v1' && policy.annualPremiumRupees==='125.00' && policy.currency==='INR', 'captured policy facts differ');
  assert(policy.effectiveDate==='2026-12-01' && policy.status===status && policy.version===version && policy.cancellationEffectiveDate===cancellationEffectiveDate, 'policy lifecycle row differs');
  const receipt = db.receiptRows[0];
  assert(receipt.ownerId==='owner-a' && receipt.clientRequestId==='issue-e2e' && receipt.policyId==='policy-a'
    && typeof receipt.requestFingerprint==='string' && receipt.requestFingerprint.trim().length>0, 'complete idempotency receipt is missing intent data');
  assert(db.eventRows[0].version===1 && db.eventRows[0].eventType==='ISSUED' && db.eventRows[0].effectiveDate==='2026-12-01' && db.eventRows[0].changeCode===null && db.eventRows[0].trainingRebuildAmountRupees===null, 'issued event row differs');
  if (endorsement) {
    assert(db.eventRows[1].version===2 && db.eventRows[1].eventType==='ENDORSED' && db.eventRows[1].effectiveDate==='2026-12-02'
      && db.eventRows[1].changeCode==='REBUILD_AMOUNT' && db.eventRows[1].trainingRebuildAmountRupees==='200000.00', 'complete endorsement event payload differs');
  }
  if (version>=3) assert(db.eventRows[2].version===3 && db.eventRows[2].eventType==='CANCEL_SCHEDULED' && db.eventRows[2].effectiveDate==='2026-12-10' && db.eventRows[2].changeCode==='CUSTOMER_REQUEST' && db.eventRows[2].trainingRebuildAmountRupees===null, 'scheduled event row differs');
  if (version>=4) assert(db.eventRows[3].version===4 && db.eventRows[3].eventType==='CANCELLED' && db.eventRows[3].effectiveDate==='2026-12-10' && db.eventRows[3].changeCode===null && db.eventRows[3].trainingRebuildAmountRupees===null, 'cancelled event row differs');
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
  beginActualCapture(page);
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
    const checkpoints = JSON.parse(await page.getByTestId('servicing-checkpoint').textContent());
    const replayCheckpoint = checkpoints.afterReplay;
    assert(stable(checkpoints.issueBody) === stable(checkpoints.replayBody), 'issue and replay response bodies differ');
    assert(stable(checkpoints.afterIssue) === stable(checkpoints.afterReplay), 'complete persisted rows changed during replay');
    assert(stable(checkpoints.afterIssue.receiptRows) === stable(checkpoints.afterReplay.receiptRows), 'receipt rows changed during replay');
    assertFullDatabase(checkpoints.afterIssue,{status:'ISSUED',version:1,cancellationEffectiveDate:null,endorsement:false});
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
    assertFullDatabase(replayCheckpoint,{status:'ISSUED',version:1,cancellationEffectiveDate:null,endorsement:false});
    assertCounts(finalDatabase, {policies:1,receipts:1,events:2}, 'after endorsement');
    assert(stable(checkpoints.afterIssue.receiptRows) === stable(finalDatabase.receiptRows), 'issue receipt changed during endorsement');
    assertFullDatabase(finalDatabase,{status:'ISSUED',version:2,cancellationEffectiveDate:null,endorsement:true});
    assert(finalDatabase.annualPremiumRupees === '125.00', 'endorsement amount must not overwrite annual premium');
    const summary = assertSummary(summaryResponse?.body, {status:'ISSUED',version:2,cancellationEffectiveDate:null,history:[{version:2,effectiveDate:'2026-12-02',changeCode:'REBUILD_AMOUNT'}]});
    await panel.getByText('125.00 INR', { exact: true }).waitFor();
    await panel.getByText('ISSUED', { exact: true }).waitFor();
    await panel.getByText('Version 2', { exact: true }).waitFor();
    assert(await panel.getByRole('listitem').filter({hasText:'REBUILD_AMOUNT'}).filter({hasText:'2026-12-02'}).count() === 1,
      `history item absent; panel=${await panel.innerText()} summary=${JSON.stringify(summary)}`);
    const panelText = await panel.innerText();
    assert(!/owner-a|journey-a|quote-a|training-rate-v1/.test(panelText), 'private source identifiers appeared in the panel');
    const result = {phase,browserOpened:true,http:captured.map(x=>({path:x.path,status:x.status,body:x.path.endsWith('/servicing/policy-a')?x.body:compactSnapshot(x.body)})),issueBody:checkpoints.issueBody,replayBody:checkpoints.replayBody,afterIssue:checkpoints.afterIssue,afterReplay:checkpoints.afterReplay,replayCheckpoint,afterEndorsement:finalDatabase,summary,panelText};
    await page.screenshot({path:resultPath.replace('.json','.png')});
    await writeResult(resultPath,result);
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
    intermediateObservations.beforeReadFailure = before;
    const path = '**/test/day44/lifecycle/servicing/policy-a';
    await page.route(path, route => route.abort('failed'));
    await page.getByRole('button', { name: 'Read servicing summary' }).click();
    await panel.getByRole('alert').filter({hasText:'Policy unavailable'}).waitFor();
    for (const old of ['policy-a','home-secure-v1','125.00 INR','ISSUED','Version 2','REBUILD_AMOUNT','2026-12-02'])
      assert(await panel.getByText(old,{exact:true}).count() === 0, `stale value remained after failed read: ${old}`);
    unavailableScreenshot = resultPath.replace('.json','-unavailable.png');
    await page.screenshot({path:unavailableScreenshot});
    failedPanelText = await panel.innerText();
    intermediateObservations.failedPanelText = failedPanelText;
    intermediateObservations.abortedRequestStatus = null;
    await page.unroute(path);
    await page.getByRole('button', { name: 'Read servicing summary' }).click();
    await panel.getByText('policy-a', { exact: true }).waitFor();
    assert(await panel.getByRole('alert').count() === 0, 'stale alert remained after recovery');
    const after = await readDiagnostics(page);
    intermediateObservations.afterReadRecovery = after;
    assert(stable(before) === stable(after), 'database state changed during failed read/recovery');
    assert(stable(before.receiptRows) === stable(after.receiptRows), 'receipt rows changed during failed read/recovery');
    assert(failures.length === 1, `expected one aborted GET, got ${failures.length}`);
    const captured = await responses();
    const summaries = captured.filter(item=>item.path.endsWith('/servicing/policy-a'));
    assert(summaries.length === 2 && summaries.every(item=>item.status===200), `successful reads should bracket one abort: ${JSON.stringify(summaries.map(x=>x.status))}`);
    const recovered = assertSummary(summaries[1].body, {status:'ISSUED',version:2,cancellationEffectiveDate:null,history:[{version:2,effectiveDate:'2026-12-02',changeCode:'REBUILD_AMOUNT'}]});
    assertFullDatabase(after,{status:'ISSUED',version:2,cancellationEffectiveDate:null,endorsement:true});
    for (const value of ['125.00 INR','ISSUED','Version 2']) await panel.getByText(value,{exact:true}).waitFor();
    assert(await panel.getByRole('listitem').filter({hasText:'REBUILD_AMOUNT'}).filter({hasText:'2026-12-02'}).count()===1,'recovered endorsement history is incomplete');
    const recoveredText=await panel.innerText();
    for (const value of ['home-secure-v1','125.00 INR','ISSUED','Version 2','REBUILD_AMOUNT','2026-12-02']) assert(recoveredText.includes(value),`recovered panel missing ${value}`);
    const result = {phase,browserOpened:true,abortedRequestStatus:null,observedNetworkFailure:failures[0],successfulReads:summaries.map(x=>({status:x.status,body:x.body})),before,after,recovered,failedPanelText,panelText:recoveredText,screenshots:[unavailableScreenshot,resultPath.replace('.json','.png')]};
    await page.screenshot({path:resultPath.replace('.json','.png')});
    await writeResult(resultPath,result);
  } else if (phase === 'servicing-cancel' || phase === 'servicing-read') {
    const responses = watchLifecycleResponses(page);
    const panel = page.getByRole('region', { name: 'Policy servicing' });
    const beforeLifecycleStep = await readDiagnostics(page);
    intermediateObservations.beforeLifecycleStep = beforeLifecycleStep;
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
    intermediateObservations.afterLifecycleStep = database;
    assertFullDatabase(database,{status:'CANCELLED',version:4,cancellationEffectiveDate:'2026-12-10',endorsement:true});
    assertCounts(database,{policies:1,receipts:1,events:4},'cancelled lifecycle');
    assert(stable(beforeLifecycleStep.receiptRows) === stable(database.receiptRows), 'receipt rows changed during cancellation/read');
    assert(database.annualPremiumRupees==='125.00' && database.cancellationEffectiveDate==='2026-12-10','cancelled database summary differs from frozen expectation');
    assert(!/owner-a|journey-a|quote-a|training-rate-v1/.test(await panel.innerText()),'private source identifiers appeared after cancellation');
    const result = {phase,browserOpened:true,http:capturedBeforeHistory.map(x=>({path:x.path,status:x.status,body:x.path.endsWith('/servicing/policy-a')?x.body:compactSnapshot(x.body)})),history,beforeLifecycleStep,database,summary,panelText:await panel.innerText(),freshBrowser:phase==='servicing-read'};
    await page.screenshot({path:resultPath.replace('.json','.png')});
    await writeResult(resultPath,result);
  } else if (phase === 'flow') {
    await page.getByRole('button', { name: 'Run lifecycle API acceptance flow' }).click();
    await page.getByTestId('lifecycle-flow').filter({ hasText: '/test/day44/lifecycle/history:' }).waitFor();
    const flow = await page.getByTestId('lifecycle-flow').textContent();
    const result = { browserOpened: true, phase, flow };
    await writeResult(resultPath,result);
  } else if (phase === 'lifecycle-read') {
    await page.getByRole('button', { name: 'Read policy after restart' }).click();
    await page.getByTestId('lifecycle-flow').filter({ hasText: '/test/day44/lifecycle/history:' }).waitFor();
    const result = { browserOpened: true, phase, lifecycleReadback: await page.getByTestId('lifecycle-flow').textContent() };
    await writeResult(resultPath,result);
  } else if (phase === 'timeout-witness') {
    await new Promise(resolve => setTimeout(resolve, 15000));
    await writeResult(resultPath,{browserOpened:true,phase,unexpectedlyCompleted:true});
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
    await writeResult(resultPath,result);
  }
} catch (error) {
  const screenshot = resultPath.replace('.json', '-failure.png');
  try { await Promise.allSettled(responseTasks); } catch {}
  let panelText = null;
  if (activePage) {
    try {
      const panel = activePage.getByRole('region', {name:'Policy servicing'});
      if (await panel.count()) panelText = await panel.innerText();
    } catch {}
    try { await activePage.screenshot({path:screenshot,fullPage:true}); failureScreenshot=screenshot; } catch {}
  }
  try { await Promise.allSettled(responseTasks); } catch {}
  const partial = {
    phase, partial: true, actualArgv: process.argv, actualCwd: process.cwd(),
    http: observedResponses, observedNetworkFailures, panelText,
    failedPanelText: failedPanelText ?? panelText,
    intermediateObservations,
    error: String(error?.stack || error), screenshots: [unavailableScreenshot, failureScreenshot].filter(Boolean),
  };
  try { await import('node:fs/promises').then(({writeFile})=>writeFile(resultPath,JSON.stringify(partial,null,2))); } catch {}
  console.error(partial.error);
  process.exitCode=1;
} finally {
  await browser.close();
}
