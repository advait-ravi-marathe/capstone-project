import { describe, expect, it } from 'vitest';
import { render, within } from '@testing-library/react';
import { ParcelResult } from './ParcelResult';

describe('independent parcel UI teaching reference', () => {
  it('renders success, clears stale values on error, then recovers with only new values', () => {
    const { rerender } = render(<ParcelResult response={{ kind: 'http', status: 200, parcel: { ref: 'parcel-demo-7', stage: 'IN_TRANSIT' } }} />);
    const panel = within(document.body).getByRole('region', { name: 'Parcel tracking' });
    expect(within(panel).getByText('parcel-demo-7')).toBeTruthy();
    rerender(<ParcelResult response={{ kind: 'network-error' }} />);
    expect(within(panel).getByRole('alert').textContent).toContain('unavailable');
    expect(within(panel).queryByText('parcel-demo-7')).toBeNull();
    rerender(<ParcelResult response={{ kind: 'http', status: 200, parcel: { ref: 'parcel-demo-9', stage: 'DELIVERED' } }} />);
    expect(within(panel).getByText('parcel-demo-9')).toBeTruthy();
    expect(within(panel).getByText('DELIVERED')).toBeTruthy();
    expect(within(panel).queryByRole('alert')).toBeNull();
    expect(within(panel).queryByText('parcel-demo-7')).toBeNull();
  });
});
