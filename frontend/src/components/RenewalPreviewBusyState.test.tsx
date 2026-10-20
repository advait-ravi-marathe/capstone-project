import { cleanup, fireEvent, render, screen } from '@testing-library/react';
import { afterEach, describe, expect, it, vi } from 'vitest';
import { RenewalPreviewBusyState } from './RenewalPreviewBusyState';

describe('Day 81 manual preview recovery', () => {
  afterEach(cleanup);
  it('announces busy state, keeps policy context, and retries only on explicit action', () => {
    const onRetry = vi.fn();
    render(<RenewalPreviewBusyState policyId="policy-demo" state="busy" onRetry={onRetry} />);
    expect(screen.getByRole('status')).toHaveTextContent(/busy/i);
    expect(screen.getByText(/policy-demo/)).toBeVisible();
    expect(onRetry).not.toHaveBeenCalled();
    fireEvent.click(screen.getByRole('button', { name: /retry/i }));
    expect(onRetry).toHaveBeenCalledTimes(1);
  });

  it('uses an alert for unavailable and never creates an automatic retry control', () => {
    render(<RenewalPreviewBusyState policyId="policy-demo" state="unavailable" onRetry={() => {}} />);
    expect(screen.getByRole('alert')).toHaveTextContent(/unavailable/i);
    expect(screen.getAllByRole('button', { name: /retry/i })).toHaveLength(1);
  });
});
