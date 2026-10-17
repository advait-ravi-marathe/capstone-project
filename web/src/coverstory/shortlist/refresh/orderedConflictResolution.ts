import type { Resolution, SlotPreview } from './types'

/** CAP-644 learner seam. Stable slot identity and display order are separate dimensions. */
export function resolveInLocalOrder(slots: SlotPreview[], localOrder: string[], resolutions: Resolution[]): SlotPreview[] {
  // TODO(CAP-644): require an exact slot permutation, resolve by slot ID and
  // preserve local order independently of upstream map iteration.
  void slots; void localOrder; void resolutions
  throw new Error('TODO(CAP-644): apply ordered conflict resolutions')
}
