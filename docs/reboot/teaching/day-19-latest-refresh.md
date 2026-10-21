# Day 19 — Make refresh latest-request-wins

## Study (25 minutes)

Course access is unconfirmed. Required course 1362070 references: “Using async / await” (2:25) and “Handling Loading States” (5:27), total 7:52. TanStack query-key and cancellation lessons are optional comparisons only; they are not this component’s implementation. If unavailable, replace that 7:52 viewing slot with React’s focused [out-of-order response note](https://react.dev/reference/react/useEffect#fetching-data-with-effects) and the Promise-state reference linked in Day 18. Spend 10 minutes tracing the deferred requests, 3 minutes predicting error/settlement order, and 2:08 pause buffer.

## Worked trace

`Day19LatestRefreshReference.test.tsx` supplies a separate train board and `refresh(): Promise<Board>`. Each click increments a component-local request token. Only the newest token may change displayed data, error, or loading. A starts, then B; B resolves with platform 7/rev-B; A later resolves with platform 2/rev-old. The display remains 7. Another test rejects obsolete A while B is pending: A’s error is ignored and its `finally` cannot clear B’s loading state. The second click is allowed; no cancellation signal is claimed.

Run:

```sh
npm --prefix frontend test -- --run src/infrastructure/Day19LatestRefreshReference.test.tsx
```

Observed instructor reference: **3 tests pass** (latest success, obsolete error/finally, and latest-error ordering). It is an event callback example, not a `useEffect` or TanStack Query rewrite.

## Practice and transfer

Use 15 minutes for the deferred-promise trace and 10 for the A-error/B-success and A-success/B-error predictions; then implement the existing `QuoteSummary.onRefresh` seam. Learner command: `npm --prefix frontend test -- --run src/__tests__/Day19Contract.test.tsx`.

Keep exact server-provided annual premium, currency, version, and expiry strings. Explain why response arrival order does not establish which request is newer, and why a stale `finally` can hide a newer request’s pending state. The callbacks have no abort signal, so do not claim cancellation. Remove TanStack Query as a required design question; no new dependency is assigned.
