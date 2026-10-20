import assert from 'node:assert/strict';
import test from 'node:test';
import { createApiCaptureMerger } from './api-capture-merge.mjs';

test('consumes each response capture once and resets capture indices for a new document', () => {
  const response = () => ({ method: 'POST', path: '/api/session', status: 200, body: '' });
  const responses = [response(), response(), response()];
  const mergeCaptures = createApiCaptureMerger(responses);

  mergeCaptures([responseWithBody('owner-a')], 'document-1');
  assert.deepEqual(responses.map(({ body }) => body), ['owner-a', '', '']);

  mergeCaptures([responseWithBody('owner-a'), responseWithBody('owner-b')], 'document-1');
  assert.deepEqual(responses.map(({ body }) => body), ['owner-a', 'owner-b', '']);

  mergeCaptures([responseWithBody('owner-c')], 'document-2');
  assert.deepEqual(responses.map(({ body }) => body), ['owner-a', 'owner-b', 'owner-c']);
});

function responseWithBody(body) {
  return { method: 'POST', path: '/api/session', status: 200, body };
}
