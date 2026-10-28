import { afterEach, expect, vi } from 'vitest';
import { cleanup, fireEvent, render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';

afterEach(() => {
  cleanup();
  vi.unstubAllGlobals();
});

const expectedRequest = {
  feature_names: ['schedule_count', 'exclusion_count', 'definition_count'],
  features: [1.4, 0.1, 0],
};
const response = (body: unknown, status = 200) => new Response(JSON.stringify(body), { status });
const schedule = { predicted_label: 'schedule', decision_scope: 'synthetic_document_type_only' };

export async function assertPanelPositive(Panel: React.ComponentType) {
  cleanup(); vi.unstubAllGlobals();
  const fetchMock = vi.fn().mockResolvedValue(response(schedule));
  vi.stubGlobal('fetch', fetchMock);
  render(<Panel />);
  await userEvent.click(screen.getByRole('button', { name: 'Classify sample' }));
  await waitFor(() => expect(screen.getByRole('status')).toHaveTextContent(/schedule/i));
  expect(fetchMock).toHaveBeenCalledTimes(1);
  expect(fetchMock).toHaveBeenCalledWith('/api/model/predict', expect.objectContaining({
    method: 'POST', headers: expect.objectContaining({ 'Content-Type': 'application/json' }),
    body: JSON.stringify(expectedRequest),
  }));
  expect(screen.getByRole('status')).toHaveTextContent(/not an insurance decision/i);
}

export async function assertPanelPendingState(Panel: React.ComponentType) {
  cleanup(); vi.unstubAllGlobals();
  let finish!: (response: Response) => void;
  vi.stubGlobal('fetch', vi.fn(() => new Promise<Response>(resolve => { finish = resolve; })));
  render(<Panel />);
  await userEvent.click(screen.getByRole('button', { name: 'Classify sample' }));
  expect(screen.getByRole('status')).toHaveTextContent(/waiting for model/i);
  finish(response(schedule));
  await waitFor(() => expect(screen.getByRole('status')).toHaveTextContent(/schedule/i));
}

export async function assertPanelDenial(Panel: React.ComponentType) {
  cleanup(); vi.unstubAllGlobals();
  vi.stubGlobal('fetch', vi.fn().mockResolvedValue(response({ error: 'invalid_request', detail: 'private parser detail' }, 400)));
  render(<Panel />);
  await userEvent.click(screen.getByRole('button', { name: 'Classify sample' }));
  expect(await screen.findByRole('alert')).toHaveTextContent(/prediction unavailable/i);
  expect(screen.queryByText(/private parser detail|predicted synthetic document type: schedule/i)).toBeNull();
}

export async function assertPanelIgnoresMalformedSuccess(Panel: React.ComponentType) {
  for (const body of [
    { predicted_label: 'underwriting-approved', decision_scope: 'synthetic_document_type_only' },
    { predicted_label: 'schedule' },
    { predicted_label: 'schedule', decision_scope: 'underwriting_decision' },
  ]) {
    cleanup(); vi.unstubAllGlobals();
    vi.stubGlobal('fetch', vi.fn().mockResolvedValue(response(body)));
    render(<Panel />);
    await userEvent.click(screen.getByRole('button', { name: 'Classify sample' }));
    expect(await screen.findByRole('alert')).toHaveTextContent(/prediction unavailable/i);
    expect(screen.queryByText(/underwriting-approved|predicted synthetic document type: schedule/i)).toBeNull();
  }
}

export async function assertPanelLatestResponseWins(Panel: React.ComponentType) {
  cleanup(); vi.unstubAllGlobals();
  const pending: Array<(response: Response) => void> = [];
  const fetchMock = vi.fn(() => new Promise<Response>(resolve => pending.push(resolve)));
  vi.stubGlobal('fetch', fetchMock);
  render(<Panel />);
  const user = userEvent.setup();
  const button = screen.getByRole('button', { name: 'Classify sample' });
  await user.click(button);
  const changedFeature = screen.getByLabelText('schedule_count');
  fireEvent.change(changedFeature, { target: { value: '0.1' } });
  await waitFor(() => expect(changedFeature).toHaveValue('0.1'));
  await user.click(button);
  expect(fetchMock).toHaveBeenCalledTimes(2);
  expect(fetchMock).toHaveBeenNthCalledWith(2, '/api/model/predict', expect.objectContaining({
    method: 'POST', headers: expect.objectContaining({ 'Content-Type': 'application/json' }),
    body: JSON.stringify({ feature_names: expectedRequest.feature_names, features: [0.1, 0.1, 0] }),
  }));
  pending[1](response({ predicted_label: 'exclusion', decision_scope: 'synthetic_document_type_only' }));
  await waitFor(() => expect(screen.getByRole('status')).toHaveTextContent(/exclusion/i));
  pending[0](response(schedule));
  await waitFor(() => expect(screen.getByRole('status')).toHaveTextContent(/exclusion/i));
  expect(screen.getByRole('status')).not.toHaveTextContent(/predicted synthetic document type: schedule/i);
}
