import { afterEach, describe, expect, it, vi } from 'vitest';
import { cleanup, fireEvent, render, screen } from '@testing-library/react';
import { PolicyRenewalPreviewPanel, type PolicyRenewalPreview } from '../components/PolicyRenewalPreviewPanel';

const preview: PolicyRenewalPreview = {
  policyId: 'renewal-demo', sourcePolicyVersion: 3, productVersion: 'home-secure-v1', rateVersion: 'home-secure-rate-v1',
  currentAnnualPremiumRupees: '125.00', currency: 'INR', nextTermEffectiveDate: '2027-12-01',
  notice: 'Reference only; not a renewal offer or coverage commitment.',
};
const changedPreview: PolicyRenewalPreview = {
  policyId: 'policy-demo-9', sourcePolicyVersion: 9, productVersion: 'home-secure-v9', rateVersion: 'home-secure-rate-v4',
  currentAnnualPremiumRupees: '987.40', currency: 'INR', nextTermEffectiveDate: '2028-04-17',
  notice: 'Recorded current policy values only; this is not an offer.',
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
    const panel = screen.getByRole('region', { name: 'Renewal preview' });
    expect(screen.queryByRole('button', { name: /accept|renew now|purchase|bind/i })).toBeNull();
    expect(screen.queryByRole('link', { name: /accept|renew now|purchase|bind/i })).toBeNull();
    expect(panel.querySelector('input,textarea,select,[role="spinbutton"]')).toBeNull();
    expect(screen.queryByText(/owner-a|journey-hidden|quote-hidden/)).toBeNull();
  });

  it('renders a different frozen source value as reference-only and clears stale values on ready rerender', () => {
    const { rerender } = render(<PolicyRenewalPreviewPanel state="ready" preview={preview} />);
    rerender(<PolicyRenewalPreviewPanel state="ready" preview={changedPreview} />);
    expect(screen.getByText('Current annual premium (reference only): 987.40 INR')).toBeTruthy();
    expect(screen.getByText('Next term begins 2028-04-17')).toBeTruthy();
    expect(screen.getByText('Version 9')).toBeTruthy();
    expect(screen.getByText('home-secure-v9')).toBeTruthy();
    expect(screen.getByText('home-secure-rate-v4')).toBeTruthy();
    expect(screen.getByText(changedPreview.notice)).toBeTruthy();
    expect(screen.queryByText('Current annual premium (reference only): 125.00 INR')).toBeNull();
    expect(screen.queryByText('Next term begins 2027-12-01')).toBeNull();
    expect(screen.queryByText('Version 3')).toBeNull();
    expect(screen.queryByText(preview.notice)).toBeNull();
  });

  it('handles an explicit ready state without a selected preview', () => {
    const { rerender } = render(<PolicyRenewalPreviewPanel state="ready" preview={changedPreview} />);
    rerender(<PolicyRenewalPreviewPanel state="ready" />);
    expect(screen.queryByText(/Current annual premium/)).toBeNull();
    expect(screen.queryByText(/Next term begins/)).toBeNull();
    expect(screen.queryByText(/Version 9|home-secure-v9|home-secure-rate-v4/)).toBeNull();
    expect(screen.queryByText(changedPreview.notice)).toBeNull();
    expect(screen.getByRole('alert').textContent).toContain('Renewal preview unavailable');
  });

  it.each([
    ['loading', 'Checking renewal preview'],
    ['unauthenticated', 'Sign in to view a renewal preview'],
    ['unavailable', 'Renewal preview unavailable'],
  ] as const)('renders an explicit %s state', (state, message) => {
    const { rerender } = render(<PolicyRenewalPreviewPanel state="ready" preview={preview} />);
    rerender(<PolicyRenewalPreviewPanel state={state} preview={preview} />);
    const role = state === 'loading' ? 'status' : 'alert';
    expect(screen.getByRole(role).textContent).toContain(message);
    expect(screen.queryByRole('button', { name: /accept|renew now|purchase|bind/i })).toBeNull();
    expect(screen.queryByText(/Current annual premium|Next term begins|Version 3|not a renewal offer/)).toBeNull();
    expect(screen.queryByRole(role === 'status' ? 'alert' : 'status')).toBeNull();
    rerender(<PolicyRenewalPreviewPanel state="ready" preview={changedPreview} />);
    expect(screen.getByText('Current annual premium (reference only): 987.40 INR')).toBeTruthy();
    expect(screen.getByText('Next term begins 2028-04-17')).toBeTruthy();
    expect(screen.getByText('Version 9')).toBeTruthy();
    expect(screen.getByText(changedPreview.notice)).toBeTruthy();
    expect(screen.queryByText('Current annual premium (reference only): 125.00 INR')).toBeNull();
    expect(screen.queryByText('Next term begins 2027-12-01')).toBeNull();
    expect(screen.queryByText('Version 3')).toBeNull();
    expect(screen.queryByText(preview.notice)).toBeNull();
    expect(screen.queryByRole('alert')).toBeNull();
    expect(screen.queryByRole('status')).toBeNull();
  });

  it('uses the refresh callback only to request another read', () => {
    const refresh = vi.fn();
    const { rerender } = render(<PolicyRenewalPreviewPanel state="ready" preview={preview} onRefresh={refresh} />);
    expect(refresh).toHaveBeenCalledTimes(0);
    rerender(<PolicyRenewalPreviewPanel state="ready" preview={changedPreview} onRefresh={refresh} />);
    expect(refresh).toHaveBeenCalledTimes(0);
    fireEvent.click(screen.getByRole('button', { name: 'Refresh preview' }));
    expect(refresh).toHaveBeenCalledTimes(1);
    rerender(<PolicyRenewalPreviewPanel state="ready" preview={preview} />);
    expect(screen.queryByRole('button', { name: 'Refresh preview' })).toBeNull();
  });
});
