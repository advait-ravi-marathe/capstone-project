import { afterEach, describe, expect, it, vi } from 'vitest';
import { cleanup, render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { AiFirstCoverStoryApp } from '../ai/AiFirstCoverStoryApp';

afterEach(() => {
  cleanup();
  vi.unstubAllGlobals();
});

const response = (body: unknown, status = 200) => new Response(JSON.stringify(body), { status });

describe('assistant-supplied AI-first CoverStory UI', () => {
  it('uses the session, CSRF, and supplied API without sending a caller tenant', async () => {
    const fetchMock = vi.fn(async (input: RequestInfo | URL, init?: RequestInit) => {
      const url = String(input);
      if (url === '/api/session' && (!init || init.method === 'GET')) return response({ code: 'UNAUTHENTICATED' }, 401);
      if (url === '/api/csrf') return response({ headerName: 'X-XSRF-TOKEN', token: 'local-csrf' });
      if (url === '/api/session') return response({ ownerId: 'owner-a' });
      if (url === '/api/ai/policy-assistant') return response({
        answer: 'Hail damage is excluded.', citations: [{ documentId: 'SYN-POL-A', version: 'v1', section: 'Exclusions' }],
        abstained: false, executionMode: 'offline_synthetic_fixture', providerRequestMade: false, liveQualityEvaluated: false,
      });
      return response({ code: 'NOT_FOUND' }, 404);
    });
    vi.stubGlobal('fetch', fetchMock);
    render(<AiFirstCoverStoryApp />);
    await userEvent.click(await screen.findByRole('button', { name: 'Use owner-a' }));
    await userEvent.type(screen.getByRole('textbox', { name: /supplied policy excerpt/i }), 'Is hail covered?');
    await userEvent.click(screen.getByRole('button', { name: 'Ask' }));
    expect(await screen.findByRole('region', { name: 'Assistant response' })).toHaveTextContent('Hail damage is excluded.');
    const call = fetchMock.mock.calls.find(([url]) => String(url) === '/api/ai/policy-assistant');
    expect(call?.[1]).toEqual(expect.objectContaining({
      method: 'POST', credentials: 'same-origin',
      headers: expect.objectContaining({ 'X-XSRF-TOKEN': 'local-csrf' }),
      body: JSON.stringify({ question: 'Is hail covered?' }),
    }));
    expect(String(call?.[1]?.body)).not.toContain('ownerId');
    expect(await screen.findByText(/Execution mode: offline_synthetic_fixture/)).toBeTruthy();
    expect(screen.getByText(/not model output/i)).toBeTruthy();
  });

  it('shows a safe outage message without reflecting response details', async () => {
    const fetchMock = vi.fn(async (input: RequestInfo | URL, init?: RequestInit) => {
      const url = String(input);
      if (url === '/api/session' && (!init || init.method === 'GET')) return response({ ownerId: 'owner-a' });
      if (url === '/api/csrf') return response({ headerName: 'X-XSRF-TOKEN', token: 'local-csrf' });
      if (url === '/api/ai/policy-assistant') return response({ code: 'AI_PROVIDER_UNAVAILABLE', detail: 'private prompt leaked' }, 503);
      return response({ code: 'NOT_FOUND' }, 404);
    });
    vi.stubGlobal('fetch', fetchMock);
    render(<AiFirstCoverStoryApp />);
    await userEvent.type(await screen.findByRole('textbox', { name: /supplied policy excerpt/i }), 'hello');
    await userEvent.click(screen.getByRole('button', { name: 'Ask' }));
    expect(await screen.findByRole('alert')).toHaveTextContent('The local AI service is unavailable. No answer was produced.');
    expect(screen.queryByText(/private prompt leaked/i)).toBeNull();
  });
});
