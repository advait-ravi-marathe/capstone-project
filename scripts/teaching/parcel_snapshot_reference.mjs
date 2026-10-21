import assert from 'node:assert/strict';

const original = {
  parcels: [{ ref: 'parcel-17', owner: 'owner-a', revision: 4, weight: '2.50' }],
  events: [{ ref: 'parcel-17', revision: 4, kind: 'MEASURED' }],
  receipts: [{ owner: 'owner-a', request: 'request-1', fingerprint: 'original' }],
};
function snapshot(database) {
  return JSON.stringify({
    parcels: [...database.parcels].sort((a, b) => a.ref.localeCompare(b.ref)),
    events: [...database.events].sort((a, b) => a.ref.localeCompare(b.ref) || a.revision - b.revision),
    receipts: [...database.receipts].sort((a, b) => a.owner.localeCompare(b.owner) || a.request.localeCompare(b.request)),
  });
}
function observe(database) {
  const parcel = database.parcels.find(p => p.ref === 'parcel-17');
  return { status: 200, body: { ref: parcel.ref, revision: parcel.revision, weight: parcel.weight } };
}
const database = structuredClone(original);
const before = snapshot(database);
const first = observe(database);
assert.equal(snapshot(database), before);
const restartedReader = { read: () => observe(database) };
const second = restartedReader.read();
assert.equal(first.status, 200); assert.equal(second.status, 200);
assert.deepEqual(second.body, first.body); assert.equal(snapshot(database), before);
for (const mutate of [
  db => { db.parcels[0].revision += 1; }, db => { db.parcels[0].weight = '9.99'; },
  db => { db.events.push({ ref: 'parcel-17', revision: 5, kind: 'READ' }); },
  db => { db.receipts[0].fingerprint = 'overwritten'; },
]) {
  const wrong = structuredClone(original); mutate(wrong);
  assert.equal(wrong.parcels.length, original.parcels.length);
  assert.notEqual(snapshot(wrong), before);
}
console.log('PASS: stable read; four count-preserving mutation counterexamples detected');
