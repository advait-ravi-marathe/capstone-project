export type RenewalPreviewBusyStateProps = {
  policyId: string;
  state: 'busy' | 'unavailable';
  onRetry: () => void;
};

/** Manual recovery control for the read-only renewal preview. */
export function RenewalPreviewBusyState(_props: RenewalPreviewBusyStateProps) {
  // LEARNER TODO Day 81: accessible status, retain policy context, and one manual retry.
  // Do not automatically retry, reset the session, or accept/bind a renewal.
  return <section aria-label="Renewal preview recovery"><p>Day 81 learner TODO</p></section>;
}
