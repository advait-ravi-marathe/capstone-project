# Day 66 — Treat response data as untrusted until decoded

**Study: 25 minutes.** Read MDN “Including credentials”, “Handling the response”; TypeScript “Discriminated unions”; React “Fetching data with Effects”. Trace the parcel example: status is checked before JSON, malformed successful payload is unavailable, and a stale response cannot repopulate state after owner change.

## Worked example

In [`parcelState.ts`](../../../../frontend/src/teaching/parcelState.ts) and its [Vitest reference](../../../../frontend/src/teaching/parcelState.test.ts), `401` returns `expired` before parsing an HTML body; string revision is rejected despite valid JSON; a response captured for the previous owner is ignored after generation changes. `submitParcelHold` takes an already frozen body and current CSRF token, makes exactly one same-origin POST, returns the status without parsing a 204 body, and never retries. The test makes two explicit calls and confirms both use the same request body/key; this is an independent reference, not the native app implementation.

## Transfer

The actual `LocalCoverStoryApp` remains TODO. Use the existing five endpoint shapes, server-derived owner, same-origin cookie, separate CSRF header, typed status states, and clear state on owner change/logout. The required Sprint 5 command deliberately excludes later PREVIEW_BUSY coverage: `npm --prefix frontend run test:sprint5`. `npm --prefix frontend run test:local` remains the full later suite.

**Practice 85:** 15 state predictions, 25 transport, 30 session-plus-preview slice, 15 focused checks. The slice alone does not satisfy the entire task. **Evidence 10:** actual request and state observations; never store session cookies.
