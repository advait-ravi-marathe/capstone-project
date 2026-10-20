import { useEffect, useState } from 'react';
import { QuoteForm, type QuoteFormInput } from '../components/QuoteForm';
import { QuoteSummary } from '../components/QuoteSummary';
import { SelectQuoteButton } from '../components/SelectQuoteButton';

type QuoteView = {
  quoteVersionId: string;
  annualPremiumRupees: string;
  currency: string;
  productVersion: string;
  rateVersion: string;
  expiresAt: string;
};
type SummaryView = {
  journeyId: string;
  quote: QuoteView | null;
  selections: Array<{ quoteVersionId: string; selectionOrder: number }>;
};
const journeyId = 'journey-day28';

async function jsonRequest<T>(path: string, init?: RequestInit): Promise<T> {
  const response = await fetch(path, {
    ...init,
    headers: { 'Content-Type': 'application/json', ...init?.headers },
  });
  if (!response.ok) throw new Error(`HTTP ${response.status}: ${await response.text()}`);
  return response.json() as Promise<T>;
}

/** Test-only React page composing the learner-owned UI components over real test APIs. */
export function HomeSecureBrowserHarness() {
  const [quote, setQuote] = useState<QuoteView | null>(null);
  const [summary, setSummary] = useState<SummaryView | null>(null);
  const [selectionMessage, setSelectionMessage] = useState('');
  const [error, setError] = useState('');

  async function refreshSummary() {
    const data = await jsonRequest<SummaryView>('/test/day28/summary');
    setSummary(data);
    if (data.quote) setQuote(data.quote);
    return data;
  }

  useEffect(() => {
    void refreshSummary().catch((failure: Error) => setError(failure.message));
  }, []);

  async function createQuote(input: QuoteFormInput) {
    const data = await jsonRequest<QuoteView>('/test/day28/quotes', {
      method: 'POST', body: JSON.stringify(input),
    });
    setQuote(data);
    setError('');
  }

  async function refreshQuote() {
    if (!quote) throw new Error('No quote to refresh');
    const data = await jsonRequest<QuoteView>(`/test/day28/quotes/${quote.quoteVersionId}`);
    setQuote(data);
    return data;
  }

  async function selectQuote(id: string) {
    await jsonRequest('/test/day28/selections', {
      method: 'POST',
      body: JSON.stringify({ selections: [{ journeyId, quoteVersionId: id }] }),
    });
    const data = await refreshSummary();
    setSelectionMessage(`Saved selection ${id}`);
    if (!data.selections.some((selection) => selection.quoteVersionId === id)) {
      throw new Error('Selection response did not appear in the refreshed summary');
    }
  }

  return (
    <main aria-label="HomeSecure browser-to-database test flow">
      <h1>HomeSecure quote</h1>
      <QuoteForm coverageCodes={['BUILDING']} onSubmit={createQuote} />
      {quote && <section aria-label="Latest quote">
        <QuoteSummary snapshot={quote} loading={false} onRefresh={refreshQuote} />
        <div data-testid="quote-version">{quote.quoteVersionId}</div>
        <SelectQuoteButton journeyId={journeyId} quoteVersionId={quote.quoteVersionId} select={async (_journeyId, id) => selectQuote(id)} />
      </section>}
      {summary && <div data-testid="summary-selections">
        {summary.selections.map((selection) => selection.quoteVersionId).join(',')}
      </div>}
      {selectionMessage && <p role="status">{selectionMessage}</p>}
      {error && <p role="alert">{error}</p>}
    </main>
  );
}
