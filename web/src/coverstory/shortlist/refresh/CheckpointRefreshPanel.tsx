import { useEffect, useState } from 'react'
import type { CheckpointRefreshPreview, Resolution, VersionRef } from './types'

export type CheckpointRefreshPanelProps = {
  preview: CheckpointRefreshPreview
  onAccept: (fingerprint: string, resolutions: Resolution[]) => Promise<void>
  onRetry: () => void
  error?: string
  stale?: boolean
}

/** CAP-638 accessible starter composition. Conflict semantics remain learner-owned. */
export function CheckpointRefreshPanel({ preview, onAccept, onRetry, error, stale = false }: CheckpointRefreshPanelProps) {
  const [choices, setChoices] = useState<Record<string, VersionRef>>({})
  useEffect(() => {
    // TODO(CAP-638): invalidate only decisions incompatible with a new preview fingerprint.
    setChoices({})
  }, [preview.fingerprint])
  const blocked = stale || preview.slots.some(slot => slot.state === 'UNAVAILABLE') ||
    preview.slots.some(slot => slot.state === 'CONFLICT' && !choices[slot.slotId])
  const resolutions = Object.entries(choices).map(([slotId, choice]) => ({ slotId, choice }))
  return <section aria-labelledby="checkpoint-refresh-title" data-preview-fingerprint={preview.fingerprint}>
    <h2 id="checkpoint-refresh-title">Review comparison refresh</h2>
    <p>Preview only · checkpoint r{preview.checkpointRevision} · catalog generation {preview.catalogGeneration}</p>
    {preview.slots.map(slot => <fieldset key={slot.slotId} aria-describedby={`${slot.slotId}-state`}>
      <legend>Slot {slot.slotId}</legend>
      <p>Base: {versionLabel(slot.base)}</p><p>Your branch: {versionLabel(slot.local)}</p>
      <p>Catalog proposal: {versionLabel(slot.upstream)}</p>
      <p id={`${slot.slotId}-state`}>{slot.state}</p>
      {slot.state === 'CONFLICT' && (slot.allowedChoices ?? []).map(choice => <label key={`${slot.slotId}-${choice.versionId}`}>
        <input type="radio" name={`choice-${slot.slotId}`} value={choice.versionId}
          checked={choices[slot.slotId]?.versionId === choice.versionId}
          onChange={() => {
            // TODO(CAP-638): bind the decision to preview fingerprint and exact source identity.
            setChoices(current => ({ ...current, [slot.slotId]: choice }))
          }} /> Use {choice.versionId}
      </label>)}
    </fieldset>)}
    {stale && <p role="alert">This preview changed. Review the latest versions before accepting.</p>}
    {error && <p role="alert">{error}</p>}
    <button type="button" disabled={blocked} onClick={() => void onAccept(preview.fingerprint, resolutions)}>Accept reviewed refresh</button>
    <button type="button" onClick={onRetry}>Retry preview</button>
  </section>
}

function versionLabel(value: VersionRef | null): string { return value ? `${value.productId} / ${value.versionId}` : 'Unavailable' }
