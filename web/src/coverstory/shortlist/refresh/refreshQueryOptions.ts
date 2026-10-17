import type { CheckpointRefreshPreview } from './types'

/** CAP-636/638 query factory starter; server owner authorization remains authoritative. */
export function refreshPreviewQueryOptions(shortlistId: string, expectedRevision: number) {
  return {
    queryKey: ['shortlist', 'checkpoint-refresh-preview', shortlistId, expectedRevision] as const,
    queryFn: async ({ signal }: { signal?: AbortSignal }): Promise<CheckpointRefreshPreview> => {
      // TODO(CAP-636): call the bounded preview API, decode schema and reject
      // mixed snapshots without placing private data in persistent browser cache.
      void signal
      throw new Error('TODO(CAP-636): fetch authoritative checkpoint preview')
    },
    staleTime: 0,
    gcTime: 0,
  }
}
