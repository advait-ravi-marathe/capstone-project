export type PolicyRenewalPreview = {
  policyId: string;
  sourcePolicyVersion: number;
  productVersion: string;
  rateVersion: string;
  currentAnnualPremiumRupees: string;
  currency: string;
  nextTermEffectiveDate: string;
  notice: string;
};

export type PolicyRenewalPreviewPanelProps = {
  state: 'loading' | 'ready' | 'unauthenticated' | 'unavailable';
  preview?: PolicyRenewalPreview;
  onRefresh?: () => void;
};

/** Read-only training preview; rendering this view never accepts or binds renewal. */
export function PolicyRenewalPreviewPanel(_props: PolicyRenewalPreviewPanelProps) {
  // LEARNER TODO Day 52: implement the safe read-only renewal preview presentation.
  return <section aria-label="Renewal preview"><p>Day 52 learner TODO</p></section>;
}
