/** Instructor reference fixes for isolated role-bank exercises. */
export function acceptsResponse(currentRequestId: number, responseRequestId: number): boolean {
  return currentRequestId === responseRequestId;
}

export function shouldRetry(status: number): boolean {
  return status === 429 || status >= 500;
}

export function iconButtonProps(label: string): { type: 'button'; 'aria-label': string } {
  if (!label.trim()) throw new Error('accessible label required');
  return { type: 'button', 'aria-label': label };
}
