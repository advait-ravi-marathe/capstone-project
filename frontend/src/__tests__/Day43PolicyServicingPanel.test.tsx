import { afterEach, describe, expect, it } from 'vitest';
import { cleanup, render, screen, within } from '@testing-library/react';
import { PolicyServicingPanel } from '../components/PolicyServicingPanel';

const summary = {
  policyId: 'policy-demo-1', productVersion: 'home-secure-v1', annualPremiumRupees: '125.00',
  currency: 'INR', status: 'CANCEL_SCHEDULED' as const, version: 3,
  history: [{ version: 2, effectiveDate: '2026-12-03', changeCode: 'REBUILD_AMOUNT' }],
  cancellationEffectiveDate: '2026-12-15',
};

afterEach(cleanup);

describe('Day 43 policy servicing panel', () => {
  it('renders snapshot, status, versioned history, and cancellation date without private identifiers', () => {
    render(<PolicyServicingPanel state="ready" summary={summary} />);
    expect(screen.getByText('policy-demo-1')).toBeTruthy();
    expect(screen.getByText('CANCEL_SCHEDULED')).toBeTruthy();
    expect(screen.getByText('125.00 INR')).toBeTruthy();
    expect(screen.getByText('Version 3')).toBeTruthy();
    expect(within(screen.getByRole('list', { name: 'Endorsement history' })).getByRole('listitem').textContent).toContain('REBUILD_AMOUNT');
    expect(screen.getByText('2026-12-15')).toBeTruthy();
    expect(screen.queryByText(/owner-a|journey-a|quote-a/)).toBeNull();
  });

  it.each([
    ['loading', 'Loading policy'], ['unauthenticated', 'Sign in'], ['unavailable', 'Policy unavailable'],
  ] as const)('shows explicit %s state', (state, message) => {
    render(<PolicyServicingPanel state={state} />);
    expect(screen.getByRole(state === 'loading' ? 'status' : 'alert').textContent).toContain(message);
  });
  it('shows an explicit empty state when ready has no selected policy', () => {
    render(<PolicyServicingPanel state="ready" />);
    const panel = screen.getByRole('region', { name: 'Policy servicing' });
    expect(within(panel).getByRole('status').textContent).toContain('No policy selected');
    expect(within(panel).queryByText('125.00 INR')).toBeNull();
  });

  it('clears every prior value when ready changes to ready with no selected policy', () => {
    const { rerender } = render(<PolicyServicingPanel state="ready" summary={summary} />);
    const panel = screen.getByRole('region', { name: 'Policy servicing' });
    const priorHistory = within(panel).getAllByRole('listitem').map(item => item.textContent ?? '');
    expect(priorHistory).toHaveLength(1);
    expect(priorHistory[0]).toContain('REBUILD_AMOUNT');
    expect(priorHistory[0]).toContain('2026-12-03');
    rerender(<PolicyServicingPanel state="ready" />);
    expect(within(panel).getByRole('status').textContent).toContain('No policy selected');
    for (const oldValue of ['policy-demo-1', 'home-secure-v1', '125.00 INR', 'CANCEL_SCHEDULED', 'Version 3', '2026-12-15', 'REBUILD_AMOUNT']) {
      expect(within(panel).queryByText(oldValue, { exact: true })).toBeNull();
    }
    expect(within(panel).queryByRole('list', { name: 'Endorsement history' })).toBeNull();
    expect(within(panel).queryAllByRole('listitem')).toHaveLength(0);
  });

  it.each(['loading', 'unauthenticated', 'unavailable'] as const)('clears stale summary on rerender to %s', (state) => {
    const { rerender } = render(<PolicyServicingPanel state="ready" summary={summary} />);
    const panel = screen.getByRole('region', { name: 'Policy servicing' });
    expect(within(panel).getByText('policy-demo-1')).toBeTruthy();
    expect(within(panel).getByText('home-secure-v1')).toBeTruthy();
    expect(within(panel).getByText('125.00 INR')).toBeTruthy();
    expect(within(panel).getByText('CANCEL_SCHEDULED')).toBeTruthy();
    expect(within(panel).getByText('Version 3')).toBeTruthy();
    expect(within(panel).getByText('2026-12-15')).toBeTruthy();
    const oldHistory = within(panel).getAllByRole('listitem').map(item => item.textContent ?? '');
    expect(oldHistory).toHaveLength(1);
    expect(oldHistory[0]).toContain('REBUILD_AMOUNT');
    expect(oldHistory[0]).toContain('2026-12-03');
    rerender(<PolicyServicingPanel state={state} summary={summary} />);
    expect(within(panel).getByRole(state === 'loading' ? 'status' : 'alert')).toBeTruthy();
    expect(within(panel).queryByText('policy-demo-1')).toBeNull();
    expect(within(panel).queryByText('125.00 INR')).toBeNull();
    expect(within(panel).queryByText('CANCEL_SCHEDULED')).toBeNull();
    expect(within(panel).queryByText('Version 3')).toBeNull();
    expect(within(panel).queryByRole('list', { name: 'Endorsement history' })).toBeNull();
    expect(within(panel).queryByText('2026-12-15')).toBeNull();
    expect(within(panel).queryByText('2026-12-03')).toBeNull();
    expect(within(panel).queryAllByRole('listitem')).toHaveLength(0);
  });

  it('renders fresh server values after unavailable without retaining old values', () => {
    const { rerender } = render(<PolicyServicingPanel state="ready" summary={summary} />);
    const panel = screen.getByRole('region', { name: 'Policy servicing' });
    rerender(<PolicyServicingPanel state="unavailable" summary={summary} />);
    const fresh = { ...summary, policyId: 'policy-demo-2', productVersion: 'home-secure-v9', annualPremiumRupees: '987.65', status: 'ISSUED' as const, version: 8, history: [], cancellationEffectiveDate: null };
    rerender(<PolicyServicingPanel state="ready" summary={fresh} />);
    expect(within(panel).getByText('policy-demo-2')).toBeTruthy();
    expect(within(panel).getByText('home-secure-v9')).toBeTruthy();
    expect(within(panel).getByText('987.65 INR')).toBeTruthy();
    expect(within(panel).getByText('Version 8')).toBeTruthy();
    expect(within(panel).getByText('ISSUED')).toBeTruthy();
    expect(within(panel).queryByText('CANCEL_SCHEDULED')).toBeNull();
    expect(within(panel).queryByText('Version 3')).toBeNull();
    expect(within(panel).queryByText('policy-demo-1')).toBeNull();
    expect(within(panel).queryByText('home-secure-v1')).toBeNull();
    expect(within(panel).queryByText('125.00 INR')).toBeNull();
    expect(within(panel).queryByText('REBUILD_AMOUNT')).toBeNull();
    expect(within(panel).queryAllByRole('listitem')).toHaveLength(0);
    expect(within(panel).queryByText('2026-12-03')).toBeNull();
    expect(within(panel).queryByText('2026-12-15')).toBeNull();
    expect(within(panel).queryByRole('alert')).toBeNull();
  });

});
