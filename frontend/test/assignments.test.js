import test from 'node:test';
import assert from 'node:assert/strict';
import fs from 'node:fs';
import * as assignments from '../src/assignments.js';
const fixtures = JSON.parse(fs.readFileSync(new URL('../../fixtures/cases.json', import.meta.url), 'utf8'));
for (let day = 1; day <= 30; day++) {
  if (process.env.DAY && Number(process.env.DAY) !== day) continue;
  const fn = assignments[`day${String(day).padStart(2,'0')}`];
  for (const [index, item] of fixtures[`frontend_${String(day).padStart(2,'0')}`].cases.entries()) {
    test(`FBA day ${day} case ${index+1}`, () => assert.deepEqual(fn(item.input), item.expected));
  }
}
