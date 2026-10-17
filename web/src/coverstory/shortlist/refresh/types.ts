export type VersionRef = { productId: string; versionId: string; sourceSha256: string }
export type SlotPreview = {
  slotId: string
  base: VersionRef | null
  local: VersionRef | null
  upstream: VersionRef | null
  state: 'RESOLVED' | 'CONFLICT' | 'UNAVAILABLE'
  allowedChoices?: VersionRef[]
}
export type CheckpointRefreshPreview = {
  schemaMajor: 1
  shortlistId: string
  checkpointRevision: number
  catalogGeneration: number
  fingerprint: string
  slots: SlotPreview[]
}
export type Resolution = { slotId: string; choice: VersionRef }
export type RefreshReply = { outcome: 'ACCEPTED' | 'CONFLICT' | 'UNAVAILABLE' | 'UNKNOWN'; revision?: number; code: string }
