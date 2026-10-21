export type PolicyServicingSummary = {
  policyId: string;
  productVersion: string;
  annualPremiumRupees: string;
  currency: string;
  status: 'ISSUED' | 'CANCEL_SCHEDULED' | 'CANCELLED';
  version: number;
  history: Array<{ version: number; effectiveDate: string; changeCode: string }>;
  cancellationEffectiveDate?: string | null;
};

export type PolicyServicingPanelProps = {
  state: 'loading' | 'ready' | 'unauthenticated' | 'unavailable';
  summary?: PolicyServicingSummary;
};

/** Learner-owned HomeSecure servicing UI. Keep identity and customer data out of the view. */
export function PolicyServicingPanel(_props: PolicyServicingPanelProps) {
  // LEARNER TODO Day 43: implement accessible loading, auth, unavailable and summary states.
  return <section aria-label="Policy servicing"><p>Day 43 learner TODO</p></section>;
}
