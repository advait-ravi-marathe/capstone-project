import { ModelPredictionPanel } from './ModelPredictionPanel';

/** Day 66 learner-owned UI shell for the dedicated packaged local application. */
export function LocalCoverStoryApp() {
  // LEARNER TODO Day 66: add the typed API client and normal policy/preview states.
  return (
    <main>
      <h1>CoverStory local training</h1>
      <p>Policy issue and renewal preview will appear here after the local API is wired.</p>
      <ModelPredictionPanel />
    </main>
  );
}
