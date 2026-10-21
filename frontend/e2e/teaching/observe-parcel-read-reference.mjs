import assert from 'node:assert/strict';

// Complete trace-only teaching helper. It needs an independently supplied real page
// and parcel teaching route; this repository does not provide either one.
export async function observeParcelRead(page, pageUrl) {
  const api = '/teaching/parcels/parcel-17';
  const methods = [];
  const capture = request => {
    if (new URL(request.url()).pathname.startsWith('/teaching/parcels/')) methods.push(request.method());
  };
  page.on('request', capture);
  try {
    await page.goto(pageUrl, { waitUntil: 'domcontentloaded' });
    const waiting = page.waitForResponse(response =>
      new URL(response.url()).pathname === api && response.request().method() === 'GET');
    await page.getByRole('button', { name: 'Check parcel measurement', exact: true }).click();
    const response = await waiting;
    assert.equal(response.status(), 200, 'record and fail actual non-200, not a success-only wait');
    const body = await response.json();
    assert.deepEqual(Object.keys(body).sort(), ['ref', 'revision', 'weightKg', 'measuredOn', 'notice'].sort());
    assert.equal(typeof body.weightKg, 'number');
    const panel = page.getByRole('region', { name: 'Parcel measurement', exact: true });
    await panel.getByText(`Recorded weight: ${body.weightKg.toFixed(2)} kg`, { exact: true }).waitFor();
    await panel.getByText(`Measured on ${body.measuredOn}`, { exact: true }).waitFor();
    await panel.getByText(`Revision ${body.revision}`, { exact: true }).waitFor();
    await panel.getByText(body.notice, { exact: true }).waitFor();
    assert.equal(await panel.getByRole('button', { name: /dispatch|purchase|guarantee/i }).count(), 0);
    assert.equal(await panel.getByRole('link', { name: /dispatch|purchase|guarantee/i }).count(), 0);
    assert.deepEqual(methods, ['GET']);
    return { status: response.status(), body, visibleText: await panel.innerText(), methods };
  } finally {
    page.off('request', capture);
  }
}
