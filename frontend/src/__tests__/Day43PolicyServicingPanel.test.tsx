import { describe, expect, it } from 'vitest';
import { render, screen } from '@testing-library/react';
import { PolicyServicingPanel } from '../components/PolicyServicingPanel';

const summary = {
  policyId: 'policy-demo-1', productVersion: 'home-secure-v1', annualPremiumRupees: '125.00',
  currency: 'INR', status: 'CANCEL_SCHEDULED' as const, version: 3,
  history: [{ version: 2, effectiveDate: '2026-12-03', changeCode: 'REBUILD_AMOUNT' }],
  cancellationEffectiveDate: '2026-12-15',
};

describe('Day 43 policy servicing panel', () => {
  it('renders snapshot, status, versioned history, and cancellation date without private identifiers', () => {
    render(<PolicyServicingPanel state="ready" summary={summary} />);
    expect(screen.getByText('policy-demo-1')).toBeTruthy();
    expect(screen.getByText('CANCEL_SCHEDULED')).toBeTruthy();
    expect(screen.getByText('125.00 INR')).toBeTruthy();
    expect(screen.getByText('Version 3')).toBeTruthy();
    expect(screen.getByText('REBUILD_AMOUNT')).toBeTruthy();
    expect(screen.getByText('2026-12-15')).toBeTruthy();
    expect(screen.queryByText(/owner-a|journey-a|quote-a/)).toBeNull();
  });

  it.each([
    ['loading', 'Loading policy'], ['unauthenticated', 'Sign in'], ['unavailable', 'Policy unavailable'],
  ] as const)('shows explicit %s state', (state, message) => {
    render(<PolicyServicingPanel state={state} />);
    expect(screen.getByRole(state === 'loading' ? 'status' : 'alert').textContent).toContain(message);
  });
});
