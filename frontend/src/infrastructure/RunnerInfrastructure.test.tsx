import { describe, expect, it } from 'vitest';
import { render, screen } from '@testing-library/react';

function HarnessProbe() {
  return <button type="button">Harness ready</button>;
}

describe('frontend test infrastructure', () => {
  it('runs React Testing Library in the configured DOM environment', () => {
    render(<HarnessProbe />);
    expect(screen.getByRole('button', { name: 'Harness ready' })).toBeTruthy();
  });
});
