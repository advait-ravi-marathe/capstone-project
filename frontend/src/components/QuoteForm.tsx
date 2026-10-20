import type { FormEvent } from 'react';

export type QuoteFormInput = {
  occupancy: string;
  rebuildAmountRupees: string;
  coverageCode: string;
};

export type QuoteFormProps = {
  coverageCodes: readonly string[];
  onSubmit: (input: QuoteFormInput) => Promise<void>;
};

export function QuoteForm({ coverageCodes, onSubmit }: QuoteFormProps) {
  function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    // LEARNER TODO (Day 18): control inputs, validate for feedback, track pending,
    // block duplicate submits, and call onSubmit with the entered values.
    void onSubmit;
    return;
  }

  return (
    <form onSubmit={handleSubmit}>
      <label>Occupancy <input name="occupancy" /></label>
      <label>Rebuild amount (INR) <input name="rebuildAmountRupees" inputMode="decimal" /></label>
      <label>Coverage <select name="coverageCode">
        <option value="">Choose coverage</option>
        {coverageCodes.map((code) => <option key={code} value={code}>{code}</option>)}
      </select></label>
      <button type="submit">Get quote</button>
    </form>
  );
}
