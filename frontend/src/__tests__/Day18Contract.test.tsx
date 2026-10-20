import { afterEach, describe, expect, it, vi } from 'vitest';
import { cleanup, render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { QuoteForm } from '../components/QuoteForm';

afterEach(cleanup);

describe('Day 18 quote input', () => {
  const submit = vi.fn().mockResolvedValue(undefined);
  const view = () => render(<QuoteForm coverageCodes={['BASIC', 'PLUS']} onSubmit={submit} />);

  it('exposes labelled required domain inputs and a submit action', () => {
    view();
    expect(screen.getByLabelText('Occupancy')).toBeTruthy();
    expect(screen.getByLabelText('Rebuild amount (INR)')).toBeTruthy();
    expect(screen.getByLabelText('Coverage')).toBeTruthy();
    expect(screen.getByRole('button', { name: 'Get quote' })).toBeTruthy();
  });

  it('submits valid entered values once by keyboard', async () => {
    const user = userEvent.setup();
    view();
    await user.type(screen.getByLabelText('Occupancy'), 'owner-occupied');
    await user.type(screen.getByLabelText('Rebuild amount (INR)'), '2500000');
    await user.selectOptions(screen.getByLabelText('Coverage'), 'PLUS');
    await user.keyboard('{Enter}');
    expect(submit).toHaveBeenCalledTimes(1);
    expect(submit).toHaveBeenCalledWith({ occupancy: 'owner-occupied', rebuildAmountRupees: '2500000', coverageCode: 'PLUS' });
  });

  it('shows field feedback and makes no request for invalid values', async () => {
    const user = userEvent.setup();
    view();
    await user.click(screen.getByRole('button', { name: 'Get quote' }));
    expect(screen.getByRole('alert').textContent).toContain('required');
    expect(submit).not.toHaveBeenCalled();
  });

  it('disables repeat submit while the request is pending', async () => {
    const user = userEvent.setup();
    view();
    await user.click(screen.getByRole('button', { name: 'Get quote' }));
    expect(screen.getByRole('button', { name: 'Get quote' }).hasAttribute('disabled')).toBe(true);
  });
});
