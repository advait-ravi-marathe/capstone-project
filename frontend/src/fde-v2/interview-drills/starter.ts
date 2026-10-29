/** Isolated role-bank exercises. Edit only as a timed drill; never as capstone code. */
export function acceptsResponse(_currentRequestId: number, _responseRequestId: number): boolean {
  return true; // seeded defect: a late response can overwrite newer UI state
}

export function shouldRetry(status: number): boolean {
  return status >= 400; // seeded defect: retries permanent client errors
}

export function iconButtonProps(_label: string): { type: 'button'; 'aria-label'?: string } {
  return { type: 'button' }; // seeded defect: icon-only action has no accessible name
}
