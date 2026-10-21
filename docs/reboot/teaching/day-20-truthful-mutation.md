# Day 20 — Show only what the mutation confirms

## Study (25 minutes)

Required course reference 1362070: “Handling HTTP Errors” (9:16), access unconfirmed. “Step 09 - Implementing POST Method to create User Resource” and its 201 status lecture are not appropriate prerequisites for this selection interaction. If the React lesson is unavailable, replace that viewing slot with MDN’s [Promise fulfillment and rejection](https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Promise) and the worked status table below. Spend 8 minutes tracing the supplied promise/status cases, 3 minutes on the variation, and 4:44 on pauses. Total stays within 25.

## Worked trace

`Day20SelectionMutationReference.test.tsx` injects `select(journeyId, quoteVersionId): Promise<void>`. It sends the exact pair once. Pending disables the button and does not show success. Only promise fulfillment shows “Selection confirmed by server”; the resolved value is `void`, so there is no `result.status`. Rejections map 409 to refresh guidance, 401 to sign-in, 404 to unavailable, and 0/other failures to “Could not confirm…”. All outcomes release pending; a second click while pending is ignored. A transport rejection means outcome may be unknown; it does not prove the server made no change.

The toy adapter contract is explicit: a successful HTTP 200 resolves `void`; unsuccessful HTTP responses reject with an object containing a numeric `status`; transport failure rejects with `status: 0`. The component does not receive a Fetch `Response` and must not read `result.status` after a resolved `Promise<void>`.

Run:

```sh
npm --prefix frontend test -- --run src/infrastructure/Day20SelectionMutationReference.test.tsx
```

Observed instructor reference: **6 tests pass**, including success, duplicate click, all four mapped failures, and generic 503. No retry is automatic.

## Practice and transfer

Use 15 minutes to run/inspect, 10 to predict the 503 case, then work the actual component. Learner command: `npm --prefix frontend test -- --run src/__tests__/Day20Contract.test.tsx`.

Transfer the exact pair, contract statuses, safe messages, and distinction between selection and insurance purchase/active coverage. Retry only if the endpoint’s explicit idempotency contract permits it; do not infer that any failed POST is safe to repeat. Replace the broad system-design prompt with click → pending guard → injected promise → confirmed/error trace, within the existing block.
