import { useState } from 'react';

const FEATURE_NAMES = ['schedule_count', 'exclusion_count', 'definition_count'];
const DEFAULT_FEATURES = [1.4, 0.1, 0.0];

/** Assistant-supplied UI scaffold; the learner owns the API/state behavior. */
export function ModelPredictionPanel() {
  const [features, setFeatures] = useState(DEFAULT_FEATURES);
  // LEARNER TODO OWN-INT-333: submit through same-origin /api/model/predict,
  // validate the response, show latest-request-only result, and handle errors.
  return (
    <section aria-label="Synthetic model prediction">
      <h2>Try the synthetic document-type model</h2>
      {FEATURE_NAMES.map((name, index) => (
        <label key={name}>
          {name.replace('_', ' ')}
          <input
            aria-label={name}
            type="number"
            step="any"
            value={features[index]}
            onChange={event => setFeatures(current => current.map((value, position) =>
              position === index ? Number(event.target.value) : value))}
          />
        </label>
      ))}
      <button type="button" onClick={() => undefined}>Classify sample</button>
      <p role="status">Ready. This synthetic label is not an insurance decision.</p>
    </section>
  );
}
