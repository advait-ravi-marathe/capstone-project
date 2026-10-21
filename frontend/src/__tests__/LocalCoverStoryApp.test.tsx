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
    vi.stubGlobal('fetch', vi.fn().mockResolvedValue(new Response('<html>session expired</html>', { status: 401 })));
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

  it('returns explicit error states for malformed and rejected responses', async () => {
    const replies: Array<() => Response | Promise<Response>> = [
      () => new Response('not-json', { status: 200 }),
      () => new Response('<html>forbidden diagnostic</html>', { status: 403 }),
      () => new Response('<html>server diagnostic</html>', { status: 500 }),
      () => Promise.reject(new Error('offline transport detail')),
    ];
    for (const createReply of replies) {
      const fetchMock = vi.fn(async (input: RequestInfo | URL, init?: RequestInit) => {
        const url = String(input);
        if (url === '/api/session' && (!init || init.method === 'GET')) {
          return new Response('<html>expired</html>', { status: 401 });
        }
        if (url === '/api/csrf') return response({ headerName: 'X-XSRF-TOKEN', token: 'synthetic-csrf' });
        if (url === '/api/session') return response({ ownerId: 'owner-a' });
        if (url.endsWith('/renewal-preview')) return createReply();
        return response({ code: 'NOT_FOUND' }, 404);
      });
      vi.stubGlobal('fetch', fetchMock);
      render(<LocalCoverStoryApp />);
      await userEvent.click(await screen.findByRole('button', { name: 'Use owner-a' }));
      await userEvent.click(await screen.findByRole('button', { name: 'View preview' }));
      expect(await screen.findByRole('alert')).toBeTruthy();
      expect(screen.queryByRole('region', { name: 'Renewal preview' })).toBeNull();
      expect(screen.queryByText(/forbidden diagnostic|server diagnostic|offline transport detail/)).toBeNull();
      cleanup();
    }
  });

  it('locks a pending issue, does not retry a failed POST, and handles bodyless logout', async () => {
    let finishIssue!: (value: Response) => void;
    let issueCalls = 0;
    const logoutJson = vi.fn();
    let logoutRequest: RequestInit | undefined;
    const fetchMock = vi.fn(async (input: RequestInfo | URL, init?: RequestInit): Promise<Response> => {
      const url = String(input);
      if (url === '/api/session' && (!init || init.method === 'GET')) return response({ code: 'UNAUTHENTICATED' }, 401);
      if (url === '/api/csrf') return response({ headerName: 'X-XSRF-TOKEN', token: 'synthetic-csrf' });
      if (url === '/api/session') return response({ ownerId: 'owner-a' });
      if (url === '/api/policies' && init?.method === 'POST') {
        issueCalls += 1;
        if (issueCalls === 1) return await new Promise<Response>(resolve => { finishIssue = resolve; });
        return new Response('<html>failed</html>', { status: 500 });
      }
      if (url === '/api/session/logout') {
        logoutRequest = init;
        const empty = { status: 204, ok: true, json: logoutJson } as unknown as Response;
        return empty;
      }
      return response({ code: 'NOT_FOUND' }, 404);
    });
    vi.stubGlobal('fetch', fetchMock);
    render(<LocalCoverStoryApp />);
    await userEvent.click(await screen.findByRole('button', { name: 'Use owner-a' }));
    const issue = screen.getByRole('button', { name: 'Issue selected quote' });
    const pending = userEvent.click(issue);
    expect(await screen.findByRole('button', { name: 'Issue selected quote' })).toBeDisabled();
    await pending;
    finishIssue(new Response('<html>server error</html>', { status: 500 }));
    expect(await screen.findByRole('alert')).toBeTruthy();
    await new Promise(resolve => setTimeout(resolve, 100));
    expect(issueCalls).toBe(1);
    await userEvent.click(await screen.findByRole('button', { name: 'Log out' }));
    expect(logoutJson).not.toHaveBeenCalled();
    expect(logoutRequest).toEqual(expect.objectContaining({
      method: 'POST', credentials: 'same-origin',
      headers: expect.objectContaining({ 'X-XSRF-TOKEN': 'synthetic-csrf' }),
    }));
    expect(screen.getByRole('button', { name: 'Use owner-a' })).toBeTruthy();
    expect(screen.queryByRole('region', { name: 'Issued policy' })).toBeNull();
  });

  it('clears preview on owner change and ignores the late prior-owner response', async () => {
    let finishOwnerAPreview!: (value: Response) => void;
    const fetchMock = vi.fn(async (input: RequestInfo | URL, init?: RequestInit): Promise<Response> => {
      const url = String(input);
      if (url === '/api/session' && (!init || init.method === 'GET')) return response({ code: 'UNAUTHENTICATED' }, 401);
      if (url === '/api/csrf') return response({ headerName: 'X-XSRF-TOKEN', token: 'synthetic-csrf' });
      if (url === '/api/session') return response({ ownerId: JSON.parse(String(init?.body)).ownerId });
      if (url.endsWith('/renewal-preview')) {
        return await new Promise<Response>(resolve => { finishOwnerAPreview = resolve; });
      }
      return response({ code: 'NOT_FOUND' }, 404);
    });
    vi.stubGlobal('fetch', fetchMock);
    render(<LocalCoverStoryApp />);
    await userEvent.click(await screen.findByRole('button', { name: 'Use owner-a' }));
    await userEvent.click(screen.getByRole('button', { name: 'View preview' }));
    await userEvent.click(await screen.findByRole('button', { name: 'Use owner-b' }));
    expect(screen.queryByText(/policy-renewal-a|125\.00|2027-01-15/)).toBeNull();
    finishOwnerAPreview(response({
      policyId: 'policy-renewal-a', sourcePolicyVersion: 3, productVersion: 'home-secure-v1',
      rateVersion: 'home-secure-rate-v1', currentAnnualPremiumRupees: 125.00, currency: 'INR',
      nextTermEffectiveDate: '2027-01-15', notice: 'Reference only; not a renewal offer or coverage commitment.',
    }));
    await new Promise(resolve => setTimeout(resolve, 25));
    expect(screen.queryByText(/policy-renewal-a|125\.00|2027-01-15/)).toBeNull();
    expect(screen.queryByRole('region', { name: 'Renewal preview' })).toBeNull();
  });
});
