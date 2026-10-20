import { afterEach, describe, expect, it, vi } from 'vitest';
import { cleanup, render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { QuoteSummary } from '../components/QuoteSummary';
import type { QuoteSnapshotView } from '../components/QuoteSummary';

afterEach(cleanup);

const snapshot: QuoteSnapshotView = {
  annualPremiumRupees: '12345.60', currency: 'INR', productVersion: 'homesecure-2026-01',
  rateVersion: 'rate-7', expiresAt: '2026-11-07T12:15:00Z',
};

describe('Day 19 quote snapshot presentation', () => {
  it('renders exact server price, currency, versions and expiry without recomputation', () => {
    render(<QuoteSummary snapshot={snapshot} loading={false} onRefresh={vi.fn()} />);
    expect(screen.getByText('12345.60')).toBeTruthy();
    expect(screen.getByText('INR')).toBeTruthy();
    expect(screen.getByText('homesecure-2026-01')).toBeTruthy();
    expect(screen.getByText('rate-7')).toBeTruthy();
    expect(screen.getByText('2026-11-07T12:15:00Z')).toBeTruthy();
  });

  it('shows explicit loading and error states', () => {
    const { rerender } = render(<QuoteSummary loading onRefresh={vi.fn()} />);
    expect(screen.getByRole('status').textContent).toContain('Loading');
    rerender(<QuoteSummary loading={false} error="Quote service unavailable" onRefresh={vi.fn()} />);
    expect(screen.getByRole('alert').textContent).toBe('Quote service unavailable');
  });

  it('keeps the newest request result when responses resolve out of order', async () => {
    const user = userEvent.setup();
    let resolveFirst!: (value: QuoteSnapshotView) => void;
    let resolveSecond!: (value: QuoteSnapshotView) => void;
    const onRefresh = vi.fn()
      .mockImplementationOnce(() => new Promise<QuoteSnapshotView>((resolve) => { resolveFirst = resolve; }))
      .mockImplementationOnce(() => new Promise<QuoteSnapshotView>((resolve) => { resolveSecond = resolve; }));
    render(<QuoteSummary snapshot={snapshot} loading={false} onRefresh={onRefresh} />);
    await user.click(screen.getByRole('button', { name: 'Refresh quote' }));
    await user.click(screen.getByRole('button', { name: 'Refresh quote' }));
    resolveSecond({ ...snapshot, annualPremiumRupees: '20000.00' });
    resolveFirst({ ...snapshot, annualPremiumRupees: '10000.00' });
    expect(await screen.findByText('20000.00')).toBeTruthy();
    expect(screen.queryByText('10000.00')).toBeNull();
  });
});
