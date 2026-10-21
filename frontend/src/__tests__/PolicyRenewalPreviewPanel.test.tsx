import { afterEach, describe, expect, it, vi } from 'vitest';
import { cleanup, fireEvent, render, screen } from '@testing-library/react';
import { PolicyRenewalPreviewPanel, type PolicyRenewalPreview } from '../components/PolicyRenewalPreviewPanel';

const preview: PolicyRenewalPreview = {
  policyId: 'renewal-demo', sourcePolicyVersion: 3, productVersion: 'home-secure-v1', rateVersion: 'home-secure-rate-v1',
  currentAnnualPremiumRupees: '125.00', currency: 'INR', nextTermEffectiveDate: '2027-12-01',
  notice: 'Reference only; not a renewal offer or coverage commitment.',
};

afterEach(cleanup);

describe('Days 52–53 renewal preview panel', () => {
  it('shows the current frozen snapshot as reference-only and exposes no renewal command', () => {
    render(<PolicyRenewalPreviewPanel state="ready" preview={preview} />);
    expect(screen.getByRole('region', { name: 'Renewal preview' })).toBeTruthy();
    expect(screen.getByText('Current annual premium (reference only): 125.00 INR')).toBeTruthy();
    expect(screen.getByText('Next term begins 2027-12-01')).toBeTruthy();
    expect(screen.getByText('Version 3')).toBeTruthy();
    expect(screen.getByText(/not a renewal offer or coverage commitment/i)).toBeTruthy();
    expect(screen.queryByRole('button', { name: /accept|renew now|purchase/i })).toBeNull();
    expect(screen.queryByText(/owner-a|journey-hidden|quote-hidden/)).toBeNull();
  });

  it.each([
    ['loading', 'Checking renewal preview'],
    ['unauthenticated', 'Sign in to view a renewal preview'],
    ['unavailable', 'Renewal preview unavailable'],
  ] as const)('renders an explicit %s state', (state, message) => {
    render(<PolicyRenewalPreviewPanel state={state} />);
    const role = state === 'loading' ? 'status' : 'alert';
    expect(screen.getByRole(role).textContent).toContain(message);
    expect(screen.queryByRole('button', { name: /accept|renew now|purchase/i })).toBeNull();
  });

  it('uses the refresh callback only to request another read', () => {
    const refresh = vi.fn();
    render(<PolicyRenewalPreviewPanel state="ready" preview={preview} onRefresh={refresh} />);
    fireEvent.click(screen.getByRole('button', { name: 'Refresh preview' }));
    expect(refresh).toHaveBeenCalledTimes(1);
  });
});
