/** CAP-640 learner seam. Never persist private branch/refresh context to browser storage. */
export type OwnerResponse<T> = { ownerId: string; value: T; private: true }
export function acceptOwnerResponse<T>(currentOwnerId: string | null, response: OwnerResponse<T>): T | null {
  // TODO(CAP-640): reject late private responses after owner/session change and
  // ensure cancellation cannot be mistaken for server-side authorization.
  void currentOwnerId; void response
  throw new Error('TODO(CAP-640): enforce current private-response owner boundary')
}
