import { render, screen } from '@testing-library/react';
import { describe, expect, it } from 'vitest';
import { ProductPanel, type Product } from './ProductPanel';

const homeSecure: Product = {
  code: 'HOME_SECURE',
  name: 'HomeSecure',
  coverageOptions: [
    { code: 'STANDARD', label: 'Standard cover' },
    { code: 'PLUS', label: 'Plus cover' },
  ],
};

describe('ProductPanel', () => {
  it('shows an accessible loading state', () => {
    render(<ProductPanel state={{ status: 'loading' }} />);
    expect(screen.getByRole('status')).toHaveTextContent(/loading/i);
  });

  it('shows a useful error state', () => {
    render(<ProductPanel state={{ status: 'error', message: 'Catalog unavailable' }} />);
    expect(screen.getByRole('alert')).toHaveTextContent('Catalog unavailable');
  });

  it('explains when there are no products', () => {
    render(<ProductPanel state={{ status: 'empty' }} />);
    expect(screen.getByText(/no insurance products/i)).toBeVisible();
  });

  it('renders product options with accessible names from its typed fixture', () => {
    render(<ProductPanel state={{ status: 'ready', product: homeSecure }} />);
    expect(screen.getByRole('heading', { name: 'HomeSecure' })).toBeVisible();
    expect(screen.getByRole('radio', { name: 'Standard cover' })).toBeVisible();
    expect(screen.getByRole('radio', { name: 'Plus cover' })).toBeVisible();
    expect(screen.queryByRole('textbox')).not.toBeInTheDocument();
  });
});
