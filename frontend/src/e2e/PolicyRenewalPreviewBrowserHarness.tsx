import { useState } from 'react';
import { PolicyRenewalPreviewPanel, type PolicyRenewalPreview } from '../components/PolicyRenewalPreviewPanel';

/** Browser transport adapter only; it contains no renewal eligibility or pricing rule. */
export function PolicyRenewalPreviewBrowserHarness() {
  const [state, setState] = useState<'loading' | 'ready' | 'unauthenticated' | 'unavailable'>('loading');
  const [preview, setPreview] = useState<PolicyRenewalPreview | undefined>();
  const [message, setMessage] = useState('');

  async function loadPreview() {
    setState('loading');
    setMessage('');
    try {
      const response = await fetch('/api/policies/renewal-demo/renewal-preview');
      if (response.status === 401) {
        setState('unauthenticated');
        return;
      }
      if (!response.ok) {
        setState('unavailable');
        setMessage('Policy renewal preview unavailable');
        return;
      }
      setPreview(await response.json() as PolicyRenewalPreview);
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
