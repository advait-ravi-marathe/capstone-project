import type { RefreshReply } from './types'

export type RecoveryState = 'IDLE' | 'SUBMITTING' | 'UNKNOWN' | 'REFETCHING' | 'SETTLED' | 'FAILED'
export type OperationIdentity = { ownerScope: string; shortlistId: string; operationKey: string; canonicalRequestSha256: string }
export type RecoveryPorts = {
  accept: (operation: OperationIdentity, request: unknown, signal?: AbortSignal) => Promise<RefreshReply>
  readCurrent: (shortlistId: string, signal?: AbortSignal) => Promise<number>
}

/** CAP-639 learner seam: reconcile a lost response without minting a new operation. */
export async function recoverRefresh(
  ports: RecoveryPorts, operation: OperationIdentity, request: unknown, signal?: AbortSignal,
): Promise<{ state: RecoveryState; currentRevision?: number }> {
  // TODO(CAP-639): distinguish definite rejection from unknown delivery outcome,
  // replay the same scoped identity and refetch authoritative state after reload.
  void ports; void operation; void request; void signal
  throw new Error('TODO(CAP-639): reconcile lost checkpoint refresh response')
}
