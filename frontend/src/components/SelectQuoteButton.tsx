export type SelectQuoteButtonProps = {
  journeyId: string;
  quoteVersionId: string;
  select: (journeyId: string, quoteVersionId: string) => Promise<void>;
};

export function SelectQuoteButton({ journeyId, quoteVersionId, select }: SelectQuoteButtonProps) {
  function handleClick() {
    // LEARNER TODO (Day 20): prevent duplicate writes while pending and map server
    // responses truthfully: selected, refresh, sign-in, unavailable or network error.
    void journeyId;
    void quoteVersionId;
    void select;
    return;
  }

  return <button type="button" onClick={handleClick}>Select quote</button>;
}
