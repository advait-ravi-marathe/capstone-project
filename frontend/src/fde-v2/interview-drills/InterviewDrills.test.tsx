import { fireEvent, render, screen } from '@testing-library/react';
import { describe, expect, it } from 'vitest';
import * as starter from './starter';
import * as reference from './reference';

const candidate = import.meta.env.VITE_DRILL_IMPLEMENTATION === 'starter' ? starter : reference;

describe('bounded frontend interview drills', () => {
  it('ignores a stale response from an older request', () => {
    expect(candidate.acceptsResponse(12, 11)).toBe(false);
    expect(candidate.acceptsResponse(12, 12)).toBe(true);
  });
  it('retries transient failures but not permanent client errors', () => {
    expect(candidate.shouldRetry(400)).toBe(false);
    expect(candidate.shouldRetry(429)).toBe(true);
    expect(candidate.shouldRetry(503)).toBe(true);
  });
  it('provides an accessible name for an icon-only action', () => {
    expect(candidate.iconButtonProps('Cancel')['aria-label']).toBe('Cancel');
  });

  it('renders a labelled search action and ignores an out-of-order stale response', async () => {
    const pending: Array<(value: { requestId: number; result: string }) => void> = [];
    const search = () => new Promise<{ requestId: number; result: string }>(resolve => pending.push(resolve));
    render(<candidate.SearchPanel search={search} />);
    expect(screen.getByRole('button', { name: 'Clear search' })).toBeVisible();
    fireEvent.change(screen.getByLabelText('Search'), { target: { value: 'tenant data' } });
    fireEvent.click(screen.getByRole('button', { name: 'Search' }));
    fireEvent.click(screen.getByRole('button', { name: 'Search' }));
    pending[1]({ requestId: 2, result: 'new result' });
    await screen.findByText('new result');
    pending[0]({ requestId: 1, result: 'stale result' });
    await new Promise(resolve => setTimeout(resolve, 0));
    expect(screen.getByText('new result')).toBeInTheDocument();
    expect(screen.queryByText('stale result')).not.toBeInTheDocument();
  });

  it('measures row work for a selection change and skips unaffected rows', () => {
    const items = Array.from({ length: 40 }, (_, index) => `item-${index}`);
    const rendered: string[] = [];
    const onRowRender = (item: string) => rendered.push(item);
    const view = render(<candidate.RenderList items={items} selected="item-0" onRowRender={onRowRender} />);
    const afterMount = rendered.length;
    view.rerender(<candidate.RenderList items={items} selected="item-1" onRowRender={onRowRender} />);
    const updateRenders = rendered.length - afterMount;
    // 40 initial rows are expected; a single selection move should update only
    // the old and new selected rows. This is a render-count exercise, not a
    // wall-clock performance claim.
    expect(afterMount).toBe(40);
    expect(updateRenders).toBeLessThanOrEqual(2);
  });
});
