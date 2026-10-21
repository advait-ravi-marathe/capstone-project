# Day 53 — Keep states exclusive and refresh explicit

The same ParcelWeight reference first renders ready data, then rerenders loading, unauthenticated, or unavailable. Each transition removes the old weight, date, revision and notice. Recovery renders only the new revision. A callback count stays0 across render/rerender and becomes1 after one explicit button click. Its response client issues a single GET and maps401 distinctly from other HTTP errors; thrown network failures and wrong numeric wire types become unavailable.

Run `npm --prefix frontend test -- src/infrastructure/ParcelWeightReference.test.tsx` and inspect the complete source in that test file. It uses unit fetch collaborators; it does not prove a real server or browser.

Transfer: preserve renewal prop/state contract. Non-ready state overrides stale preview. Ready without a selected preview renders a safe unavailable alert, not invented data. A supplied Refresh preview callback is invoked once only on click; without the callback, no refresh action appears. Do not add an effect-driven fetch or a new query framework.

Study25: 5:27 loading lesson or fallback,3:00 focused ARIA status/alert and event callback,12:33 worked transitions,4:00 prediction. The extra food-order lesson is optional reference only. Practice85: 10 predict transitions,50 panel behavior,25 tests. Evidence10. Official: [WAI-ARIA status](https://www.w3.org/TR/wai-aria-1.2/#status), [alert](https://www.w3.org/TR/wai-aria-1.2/#alert), [React event handlers](https://react.dev/learn/responding-to-events#passing-event-handlers-as-props), [Testing Library ByRole](https://testing-library.com/docs/queries/byrole/).
