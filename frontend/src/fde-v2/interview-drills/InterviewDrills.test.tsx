import { describe, expect, it } from 'vitest';
import * as starter from './starter';
import * as reference from './reference';

const candidate = import.meta.env.VITE_DRILL_IMPLEMENTATION === 'starter' ? starter : reference;

describe('bounded frontend interview drills', () => {
  it('ignores a stale response from an older request', () => {
    expect(candidate.acceptsResponse(12, 11)).toBe(false);
    expect(candidate.acceptsResponse(12, 12)).toBe(true);
  });
  it('retries transient failures but not permanent client errors', () => {
    expect(candidate.shouldRetry(400)).toBe(false);
    expect(candidate.shouldRetry(429)).toBe(true);
    expect(candidate.shouldRetry(503)).toBe(true);
  });
  it('provides an accessible name for an icon-only action', () => {
    expect(candidate.iconButtonProps('Cancel')['aria-label']).toBe('Cancel');
  });
});
