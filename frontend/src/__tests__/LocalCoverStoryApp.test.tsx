import { afterEach, describe, expect, it, vi } from 'vitest';
import { cleanup, render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { LocalCoverStoryApp } from '../local/LocalCoverStoryApp';

afterEach(() => {
  cleanup();
  vi.unstubAllGlobals();
});

describe('Day 66 local CoverStory route', () => {
  it('offers only the fixed synthetic owners after the session expires', async () => {
    vi.stubGlobal('fetch', vi.fn().mockResolvedValue(new Response('{"code":"UNAUTHENTICATED"}', { status: 401 })));
    render(<LocalCoverStoryApp />);

    expect(await screen.findByRole('alert')).toHaveTextContent(/session expired|select a synthetic owner/i);
    expect(screen.getByRole('button', { name: 'Use owner-a' })).toBeTruthy();
    expect(screen.getByRole('button', { name: 'Use owner-b' })).toBeTruthy();
    expect(screen.queryByRole('textbox', { name: /email|password/i })).toBeNull();
  });

  it('sends owner-scoped API requests and shows renewal preview as reference-only', async () => {
    const fetchMock = vi.fn()
      .mockResolvedValueOnce(new Response('{"ownerId":"owner-a"}', { status: 200 }))
      .mockResolvedValueOnce(new Response('{"policyId":"policy-renewal-a","sourcePolicyVersion":3,"productVersion":"home-secure-v1","rateVersion":"home-secure-rate-v1","currentAnnualPremiumRupees":"125.00","currency":"INR","nextTermEffectiveDate":"2027-01-15","notice":"Reference only; not a renewal offer or coverage commitment."}', { status: 200 }));
    vi.stubGlobal('fetch', fetchMock);
    render(<LocalCoverStoryApp />);

    await userEvent.click(await screen.findByRole('button', { name: 'View preview' }));
    expect(await screen.findByRole('region', { name: 'Renewal preview' })).toBeTruthy();
    expect(screen.getByText('Current annual premium (reference only): 125.00 INR')).toBeTruthy();
    expect(screen.getByText(/not a renewal offer/i)).toBeTruthy();
    expect(fetchMock).toHaveBeenCalledWith('/api/policies/policy-renewal-a/renewal-preview',
      expect.objectContaining({ method: 'GET', credentials: 'same-origin' }));
    expect(screen.queryByRole('button', { name: /purchase|accept|renew now/i })).toBeNull();
  });
});
