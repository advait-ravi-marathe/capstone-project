import { useState } from 'react';
import { PolicyRenewalPreviewPanel, type PolicyRenewalPreview } from '../components/PolicyRenewalPreviewPanel';

type RenewalPreviewWire = Omit<PolicyRenewalPreview, 'currentAnnualPremiumRupees'> & {
  currentAnnualPremiumRupees: number;
};

/** Narrow numeric JSON to existing string display prop; this formats the recorded value only. */
export function renewalPreviewForDisplay(value: unknown): PolicyRenewalPreview {
  if (typeof value !== 'object' || value === null) {
    throw new Error('Unexpected preview premium representation');
  }
  const wire = value as RenewalPreviewWire;
  if (typeof wire.policyId !== 'string' || typeof wire.sourcePolicyVersion !== 'number' ||
      typeof wire.productVersion !== 'string' || typeof wire.rateVersion !== 'string' ||
      typeof wire.currentAnnualPremiumRupees !== 'number' || !Number.isFinite(wire.currentAnnualPremiumRupees) ||
      typeof wire.currency !== 'string' || typeof wire.nextTermEffectiveDate !== 'string' || typeof wire.notice !== 'string') {
    throw new Error('Unexpected preview response representation');
  }
  return {
    policyId: wire.policyId, sourcePolicyVersion: wire.sourcePolicyVersion,
    productVersion: wire.productVersion, rateVersion: wire.rateVersion,
    currentAnnualPremiumRupees: wire.currentAnnualPremiumRupees.toFixed(2),
    currency: wire.currency, nextTermEffectiveDate: wire.nextTermEffectiveDate, notice: wire.notice,
  };
}

/** Browser transport adapter only; it contains no renewal eligibility or pricing rule. */
export function PolicyRenewalPreviewBrowserHarness() {
  const [state, setState] = useState<'loading' | 'ready' | 'unauthenticated' | 'unavailable'>('loading');
  const [preview, setPreview] = useState<PolicyRenewalPreview | undefined>();
  const [message, setMessage] = useState('');

  async function loadPreview() {
    const policyId = new URLSearchParams(window.location.search).get('policyId') ?? 'renewal-demo';
    setState('loading');
    setMessage('');
    try {
      const response = await fetch(`/api/policies/${encodeURIComponent(policyId)}/renewal-preview`);
      if (response.status === 401) {
        setState('unauthenticated');
        return;
      }
      if (!response.ok) {
        setState('unavailable');
        setMessage('Policy renewal preview unavailable');
        return;
      }
      setPreview(renewalPreviewForDisplay(await response.json()));
      setState('ready');
    } catch {
      setState('unavailable');
      setMessage('Policy renewal preview unavailable');
    }
  }

  return (
    <main>
      <h1>Policy renewal preview</h1>
      <button type="button" onClick={() => void loadPreview()}>Check preview</button>
      <PolicyRenewalPreviewPanel state={state} preview={preview} onRefresh={() => void loadPreview()} />
      {message && <p role="alert">{message}</p>}
    </main>
  );
}
