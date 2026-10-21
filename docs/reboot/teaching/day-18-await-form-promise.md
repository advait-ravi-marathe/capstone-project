# Day 18 — Keep form state until the Promise settles

## Study (25 minutes)

Course access is unconfirmed. Required: course 1362070, “Managing & Getting User Input via State & Generic Handlers” (11:12). “User Input & Two-Way-Binding” (7:42) is optional replacement material, not added viewing. If unavailable, replace its 11:12 slot with React’s focused [controlled input](https://react.dev/reference/react-dom/components/input#controlling-an-input-with-a-state-variable) example and MDN’s [Promise states](https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Promise). Work the event trace for 7 minutes, predict the variation for 2:30, pause buffer 4:18.

## Worked trace

`Day18PromiseFormReference.test.tsx` implements a separate reading-club form whose injected `saveAlias(alias)` returns `Promise<void>`. Blank input shows field feedback and makes zero calls. A valid submit sets a ref-based admission guard immediately and React pending state for the disabled button, clears old feedback, then awaits the save operation. Resolve shows “Registered”; reject shows a fixed safe error and preserves the typed value. `finally` releases both guard and pending state. A second submit event while the first promise is unresolved still produces one call.

Run:

```sh
npm --prefix frontend test -- --run src/infrastructure/Day18PromiseFormReference.test.tsx
```

Observed instructor reference: **2 tests pass**, including controlled-promise success and rejection. It uses no network. The learner acceptance currently has no explicit server-rejection case; this supporting example teaches the Promise boundary without changing that original test.

## Practice and transfer

Use 15 minutes for the supplied form and tests, 10 minutes for the toy max-length variation, then work the actual `QuoteForm` contract. Existing command: `npm --prefix frontend test -- --run src/__tests__/Day18Contract.test.tsx`.

Transfer only the taught state ownership and `Promise<void>` lifecycle. Keep the contract’s exact domain bounds and feedback. Local checks help the user; server validation remains authoritative. Explain why pending cannot clear immediately after starting the promise and why both resolve and reject need cleanup. Do not add another API or change the method signature.
