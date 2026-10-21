import { afterEach, describe, expect, it, vi } from 'vitest';
import { cleanup, render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { LocalCoverStoryApp } from '../local/LocalCoverStoryApp';

afterEach(() => {
  cleanup();
  vi.unstubAllGlobals();
});

const response = (body: unknown, status = 200) => new Response(JSON.stringify(body), { status });
const issued = {
  policyId: 'policy-a', ownerId: 'owner-a', journeyId: 'journey-a', quoteVersionId: 'quote-a',
  productVersion: 'home-secure-v1', rateVersion: 'training-rate-v1', annualPremiumRupees: 125.00, currency: 'INR',
  effectiveDate: '2026-12-20', status: 'ISSUED', version: 1, endorsements: [], cancellationEffectiveDate: null,
};

describe('Day 66 local CoverStory route', () => {
  it('offers only fixed synthetic owners when the session expires', async () => {
    vi.stubGlobal('fetch', vi.fn().mockResolvedValue(response({ code: 'UNAUTHENTICATED' }, 401)));
    render(<LocalCoverStoryApp />);

    expect(await screen.findByRole('alert')).toHaveTextContent(/session expired|select a synthetic owner/i);
    expect(screen.getByRole('button', { name: 'Use owner-a' })).toBeTruthy();
    expect(screen.getByRole('button', { name: 'Use owner-b' })).toBeTruthy();
    expect(screen.queryByRole('textbox', { name: /email|password/i })).toBeNull();
  });

  it('uses the same-origin session and shows read-only preview as reference only', async () => {
    const fetchMock = vi.fn(async (input: RequestInfo | URL, init?: RequestInit) => {
      const url = String(input);
      if (url === '/api/session' && (!init || init.method === 'GET')) return response({ code: 'UNAUTHENTICATED' }, 401);
      if (url === '/api/csrf') return response({ headerName: 'X-XSRF-TOKEN', token: 'synthetic-csrf' });
      if (url === '/api/session') return response({ ownerId: 'owner-a' });
      if (url.endsWith('/renewal-preview')) return response({
        policyId: 'policy-renewal-a', sourcePolicyVersion: 3, productVersion: 'home-secure-v1',
        rateVersion: 'home-secure-rate-v1', currentAnnualPremiumRupees: 125.00, currency: 'INR',
        nextTermEffectiveDate: '2027-01-15', notice: 'Reference only; not a renewal offer or coverage commitment.',
      });
      return response({ code: 'NOT_FOUND' }, 404);
    });
    vi.stubGlobal('fetch', fetchMock);
    render(<LocalCoverStoryApp />);

    await userEvent.click(await screen.findByRole('button', { name: 'Use owner-a' }));
    await userEvent.click(await screen.findByRole('button', { name: 'View preview' }));
    expect(await screen.findByRole('region', { name: 'Renewal preview' })).toBeTruthy();
    expect(screen.getByText('Current annual premium (reference only): 125.00 INR')).toBeTruthy();
    expect(screen.getByText(/not a renewal offer/i)).toBeTruthy();
    expect(fetchMock).toHaveBeenCalledWith('/api/policies/policy-renewal-a/renewal-preview',
      expect.objectContaining({ method: 'GET', credentials: 'same-origin' }));
    expect(screen.queryByRole('button', { name: /purchase|accept|renew now/i })).toBeNull();
  });

  it('shows the real renewal route busy state and retries only after the user activates Retry', async () => {
    let previewCalls = 0;
    const fetchMock = vi.fn(async (input: RequestInfo | URL, init?: RequestInit) => {
      const url = String(input);
      if (url === '/api/session' && (!init || init.method === 'GET')) return response({ code: 'UNAUTHENTICATED' }, 401);
      if (url === '/api/csrf') return response({ headerName: 'X-XSRF-TOKEN', token: 'synthetic-csrf' });
      if (url === '/api/session') return response({ ownerId: 'owner-a' });
      if (url.endsWith('/renewal-preview')) {
        previewCalls += 1;
        if (previewCalls === 1) return response({ code: 'PREVIEW_BUSY', message: 'Renewal preview is busy' }, 503);
        return response({
          policyId: 'policy-renewal-a', sourcePolicyVersion: 3, productVersion: 'home-secure-v1',
          rateVersion: 'home-secure-rate-v1', currentAnnualPremiumRupees: 125.00, currency: 'INR',
          nextTermEffectiveDate: '2027-01-15', notice: 'Reference only; not a renewal offer or coverage commitment.',
        });
      }
      return response({ code: 'NOT_FOUND' }, 404);
    });
    vi.stubGlobal('fetch', fetchMock);
    render(<LocalCoverStoryApp />);

    await userEvent.click(await screen.findByRole('button', { name: 'Use owner-a' }));
    await userEvent.click(await screen.findByRole('button', { name: 'View preview' }));
    expect(await screen.findByRole('status')).toHaveTextContent(/busy/i);
    expect(screen.getByText(/policy-renewal-a/)).toBeTruthy();
    expect(previewCalls).toBe(1);
    await userEvent.click(screen.getByRole('button', { name: /retry/i }));
    expect(await screen.findByRole('region', { name: 'Renewal preview' })).toBeTruthy();
    expect(screen.getByText('Current annual premium (reference only): 125.00 INR')).toBeTruthy();
    expect(previewCalls).toBe(2);
  });

  it('issues, reads back and replays with the same client request ID', async () => {
    const requestBodies: string[] = [];
    let issueCount = 0;
    const fetchMock = vi.fn(async (input: RequestInfo | URL, init?: RequestInit) => {
      const url = String(input);
      if (url === '/api/session' && (!init || init.method === 'GET')) return response({ code: 'UNAUTHENTICATED' }, 401);
      if (url === '/api/csrf') return response({ headerName: 'X-XSRF-TOKEN', token: 'synthetic-csrf' });
      if (url === '/api/session') return response({ ownerId: 'owner-a' });
      if (url === '/api/policies' && init?.method === 'POST') {
        requestBodies.push(String(init.body));
        issueCount += 1;
        return response(issued, issueCount === 1 ? 201 : 200);
      }
      if (url === '/api/policies/policy-a') return response(issued);
      return response({ code: 'NOT_FOUND' }, 404);
    });
    vi.stubGlobal('fetch', fetchMock);
    render(<LocalCoverStoryApp />);

    await userEvent.click(await screen.findByRole('button', { name: 'Use owner-a' }));
    await userEvent.click(await screen.findByRole('button', { name: 'Issue selected quote' }));
    await userEvent.click(await screen.findByRole('button', { name: 'View issued policy' }));
    expect(await screen.findByRole('region', { name: 'Issued policy' })).toBeTruthy();
    expect(screen.getByText(/policy-a/)).toBeTruthy();
    await userEvent.click(screen.getByRole('button', { name: 'Replay issue request' }));

    expect(requestBodies).toHaveLength(2);
    expect(JSON.parse(requestBodies[0])).toEqual(JSON.parse(requestBodies[1]));
    expect(fetchMock).toHaveBeenCalledWith('/api/policies', expect.objectContaining({
      method: 'POST', credentials: 'same-origin', headers: expect.objectContaining({ 'X-XSRF-TOKEN': 'synthetic-csrf' }),
    }));
    expect(fetchMock).toHaveBeenCalledWith('/api/policies/policy-a',
      expect.objectContaining({ method: 'GET', credentials: 'same-origin' }));
  });
});
