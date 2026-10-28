import { describe, it } from 'vitest';
import { ModelPredictionPanel } from './ModelPredictionPanel';
import { assertPanelDenial, assertPanelIgnoresMalformedSuccess, assertPanelLatestResponseWins, assertPanelPendingState, assertPanelPositive } from './ModelPredictionPanel.contract';

describe('OWN-INT-333 learner acceptance: local model UI/API integration', () => {
  it('sends the declared feature order and renders only the bounded model label', () => assertPanelPositive(ModelPredictionPanel));
  it('shows a pending state until the API request resolves', () => assertPanelPendingState(ModelPredictionPanel));
  it('shows a safe error for a denied request without exposing backend details', () => assertPanelDenial(ModelPredictionPanel));
  it('rejects malformed success payloads and does not promote them to decisions', () => assertPanelIgnoresMalformedSuccess(ModelPredictionPanel));
  it('does not let an older, slower response overwrite the latest UI result', () => assertPanelLatestResponseWins(ModelPredictionPanel));
});
