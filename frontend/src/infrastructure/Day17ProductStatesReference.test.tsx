import { cleanup, render, screen } from '@testing-library/react';
import { afterEach, describe, expect, it } from 'vitest';
import type { ReactElement } from 'react';

type Language = { code: string; label: string };
type Tour = { name: string; languages: Language[] };
type TourState =
  | { status: 'loading' }
  | { status: 'error'; message: string }
  | { status: 'empty' }
  | { status: 'ready'; tour: Tour };

afterEach(cleanup);

function TourChoices({ state }: { state: TourState }): ReactElement {
  if (state.status === 'loading') return <p role="status">Loading tours</p>;
  if (state.status === 'error') return <p role="alert">{state.message}</p>;
  if (state.status === 'empty') return <p>No tours available</p>;

  return (
    <section aria-label="Tour choices">
      <h2>{state.tour.name}</h2>
      <fieldset>
        <legend>Tour language</legend>
        {state.tour.languages.map((language) => (
          <label key={language.code}>
            <input type="radio" name="language" value={language.code} />
            {language.label}
          </label>
        ))}
      </fieldset>
    </section>
  );
}

describe('Day 17 independent typed-state rendering example', () => {
  it('renders loading, error, empty and ready states with accessible choices', () => {
    const view = render(<TourChoices state={{ status: 'loading' }} />);
    expect(screen.getByRole('status')).toHaveTextContent('Loading tours');
    expect(screen.queryByRole('radio')).not.toBeInTheDocument();

    view.rerender(<TourChoices state={{ status: 'error', message: 'Tours unavailable' }} />);
    expect(screen.getByRole('alert')).toHaveTextContent('Tours unavailable');
    expect(screen.queryByRole('radio')).not.toBeInTheDocument();

    view.rerender(<TourChoices state={{ status: 'empty' }} />);
    expect(screen.getByText('No tours available')).toBeInTheDocument();

    view.rerender(<TourChoices state={{
      status: 'ready',
      tour: { name: 'Night Gallery', languages: [{ code: 'EN', label: 'English' }, { code: 'HI', label: 'Hindi' }] },
    }} />);
    expect(screen.getByRole('heading', { name: 'Night Gallery' })).toBeInTheDocument();
    expect(screen.getByRole('group', { name: 'Tour language' })).toBeInTheDocument();
    expect(screen.getByRole('radio', { name: 'English' })).toHaveAttribute('value', 'EN');
    expect(screen.getByRole('radio', { name: 'Hindi' })).toHaveAttribute('value', 'HI');
    expect(screen.queryByRole('alert')).not.toBeInTheDocument();
  });

  it('renders a one-language variation and an empty state without hidden controls', () => {
    const view = render(<TourChoices state={{
      status: 'ready', tour: { name: 'Local Tour', languages: [{ code: 'MR', label: 'Marathi' }] },
    }} />);
    expect(screen.getByRole('radio', { name: 'Marathi' })).toHaveAttribute('value', 'MR');
    view.rerender(<TourChoices state={{ status: 'empty' }} />);
    expect(screen.queryByRole('radio')).not.toBeInTheDocument();
    expect(screen.getByText('No tours available')).toBeInTheDocument();
  });
});
