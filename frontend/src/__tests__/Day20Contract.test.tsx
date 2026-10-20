import { afterEach, describe, expect, it, vi } from 'vitest';
import { cleanup, render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { SelectQuoteButton } from '../components/SelectQuoteButton';

afterEach(cleanup);

describe('Day 20 quote selection action', () => {
  it('sends the exact journey and quote version once and confirms server selection', async () => {
    const user = userEvent.setup();
    const select = vi.fn().mockResolvedValue(undefined);
    render(<SelectQuoteButton journeyId="journey-A" quoteVersionId="v2" select={select} />);
    await user.click(screen.getByRole('button', { name: 'Select quote' }));
    expect(select).toHaveBeenCalledTimes(1);
    expect(select).toHaveBeenCalledWith('journey-A', 'v2');
    expect((await screen.findByRole('status')).textContent).toContain('Selected');
  });

  it.each([
    [409, 'Refresh quote'], [401, 'Sign in'], [404, 'Quote unavailable'], [0, 'Try again'],
  ])('maps API/network result %i to truthful guidance', async (_status, guidance) => {
    const user = userEvent.setup();
    const select = vi.fn().mockRejectedValue({ status: _status });
    render(<SelectQuoteButton journeyId="journey-A" quoteVersionId="v2" select={select} />);
    await user.click(screen.getByRole('button', { name: 'Select quote' }));
    expect((await screen.findByRole('alert')).textContent).toContain(guidance);
  });

  it('blocks duplicate writes while the first selection is pending', async () => {
    const user = userEvent.setup();
    const select = vi.fn(() => new Promise<void>(() => {}));
    render(<SelectQuoteButton journeyId="journey-A" quoteVersionId="v2" select={select} />);
    await user.click(screen.getByRole('button', { name: 'Select quote' }));
    expect(screen.getByRole('button', { name: 'Select quote' }).hasAttribute('disabled')).toBe(true);
  });
});
