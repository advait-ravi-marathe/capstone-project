import React from 'react';
import { afterEach, describe, expect, it, vi } from 'vitest';
import { cleanup } from '@testing-library/react';
import { assertPanelDenial, assertPanelIgnoresMalformedSuccess, assertPanelLatestResponseWins, assertPanelPendingState, assertPanelPositive } from './ModelPredictionPanel.contract';

function CorrectReferencePanel() {
  const [features, setFeatures] = React.useState([1.4, 0.1, 0]);
  const [result, setResult] = React.useState('Ready. This synthetic label is not an insurance decision.');
  const [error, setError] = React.useState(false);
  const [pending, setPending] = React.useState(false);
  const latest = React.useRef(0);
  async function submit() {
    const requestId = ++latest.current;
    setPending(true);
    setError(false);
    try {
      const response = await fetch('/api/model/predict', { method: 'POST', headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ feature_names: ['schedule_count', 'exclusion_count', 'definition_count'], features }) });
      if (!response.ok) throw new Error('denied');
      const body = await response.json();
      if (!['schedule', 'exclusion', 'definition'].includes(body.predicted_label) || body.decision_scope !== 'synthetic_document_type_only') throw new Error('invalid');
      if (requestId !== latest.current) return;
      setResult(`Predicted synthetic document type: ${body.predicted_label}. This is not an insurance decision.`);
      setPending(false);
      setError(false);
    } catch {
      if (requestId !== latest.current) return;
      setPending(false);
      setError(true);
      setResult('Ready. This synthetic label is not an insurance decision.');
    }
  }
  return <section>{['schedule_count', 'exclusion_count', 'definition_count'].map((name, index) => <label key={name}>{name}<input aria-label={name} value={features[index]} onChange={event => setFeatures(current => current.map((value, position) => position === index ? Number(event.target.value) : value))} /></label>)}<button onClick={submit}>Classify sample</button>{error && <p role="alert">Prediction unavailable. Check the local model service and try again.</p>}<p role="status">{pending ? 'Waiting for model response…' : result}</p></section>;
}

function NoRequestMutant() { return <section><button onClick={() => undefined}>Classify sample</button><p role="status">Ready.</p></section>; }
function LeaksFailureMutant() {
  const [message, setMessage] = React.useState('Ready.');
  return <section><button onClick={async () => { const response = await fetch('/api/model/predict'); setMessage(await response.text()); }}>Classify sample</button><p role="status">{message}</p></section>;
}
function TrustsAnyLabelMutant() {
  const [message, setMessage] = React.useState('Ready.');
  return <section><button onClick={async () => { const response = await fetch('/api/model/predict'); const body = await response.json(); setMessage(body.predicted_label); }}>Classify sample</button><p role="status">{message}</p></section>;
}

afterEach(() => { cleanup(); vi.unstubAllGlobals(); });

describe('OWN-INT instructor witnesses use the same UI acceptance assertions', () => {
  it('passes positive, denial and malformed-success contracts with a correct witness', async () => {
    await assertPanelPositive(CorrectReferencePanel);
    await assertPanelPendingState(CorrectReferencePanel);
    await assertPanelDenial(CorrectReferencePanel);
    await assertPanelIgnoresMalformedSuccess(CorrectReferencePanel);
    await assertPanelLatestResponseWins(CorrectReferencePanel);
  });

  it('rejects a no-request mutant on the positive acceptance path', async () => {
    await expect(assertPanelPositive(NoRequestMutant)).rejects.toThrow();
  });

  it('rejects a details-leaking mutant on the denial acceptance path', async () => {
    await expect(assertPanelDenial(LeaksFailureMutant)).rejects.toThrow();
  });

  it('rejects an arbitrary-label mutant on the malformed-success acceptance path', async () => {
    await expect(assertPanelIgnoresMalformedSuccess(TrustsAnyLabelMutant)).rejects.toThrow();
  });

  it('rejects a stale-response mutant on the same ordering assertion', async () => {
    function StaleResponseMutant() {
      const [features, setFeatures] = React.useState([1.4, 0.1, 0]);
      const [result, setResult] = React.useState('Ready. This synthetic label is not an insurance decision.');
      async function submit() {
        const response = await fetch('/api/model/predict', { method: 'POST', headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({ feature_names: ['schedule_count', 'exclusion_count', 'definition_count'], features }) });
        const body = await response.json();
        setResult(`Predicted synthetic document type: ${body.predicted_label}. This is not an insurance decision.`);
      }
      return <section>{['schedule_count', 'exclusion_count', 'definition_count'].map((name, index) => <label key={name}>{name}<input aria-label={name} value={features[index]} onChange={event => setFeatures(current => current.map((value, position) => position === index ? Number(event.target.value) : value))} /></label>)}<button onClick={submit}>Classify sample</button><p role="status">{result}</p></section>;
    }
    await expect(assertPanelLatestResponseWins(StaleResponseMutant)).rejects.toThrow();
  });
});
