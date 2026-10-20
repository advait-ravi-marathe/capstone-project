export type QuoteSnapshotView = {
  annualPremiumRupees: string;
  currency: string;
  productVersion: string;
  rateVersion: string;
  expiresAt: string;
};

export type QuoteSummaryProps = {
  snapshot?: QuoteSnapshotView;
  loading: boolean;
  error?: string;
  onRefresh: () => Promise<QuoteSnapshotView>;
};

export function QuoteSummary({ snapshot, loading, error, onRefresh }: QuoteSummaryProps) {
  // LEARNER TODO (Day 19): show exact server values and make refresh race-safe.
  void snapshot;
  void loading;
  void error;
  void onRefresh;
  return <section aria-label="Quote summary">Day 19 learner TODO: render quote snapshot</section>;
}
