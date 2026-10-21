# Day 43: Exclusive response-state UI

> Project-authored teaching toy; not official course content and not learner completion evidence. Read this before the day's implementation task. First-30 prerequisite links refer to immutable main `80cb3cd`; the new teaching reference below and its command are in this continuation checkout.

## Worked example

Toy ParcelResult renders delivered parcel-7 as the only success state. Rerender to network error: an alert appears and parcel-7 details disappear. Rerender to parcel-9 delivered: new values appear, old alert/details disappear. A 404 cannot fall through to success. The component is a pure rendering example, not a solved policy panel.

Run the complete toy before transferring: [ParcelResult.tsx](../../../../frontend/src/infrastructure/ParcelResult.tsx) and [its rerender test](../../../../frontend/src/infrastructure/ParcelResult.test.tsx). From repository root, run `npm --prefix frontend exec -- vitest run --root frontend src/infrastructure/ParcelResult.test.tsx`. The test asserts success → error with stale value removed → a different success with the old alert/value removed. This is the instructor teaching reference; it does not implement policy fields.

## Transfer task

Transfer: implement the existing `PolicyServicingPanel` state union and the backend projection. Test ready/no summary, each nonready state with stale summary, then recovery with distinct product/premium/history values. Use scoped semantic roles; UI hiding is not server authorization.

## Readiness check

Before editing, predict one changed input and its observable result. If the prediction is wrong, repeat this toy trace inside the study block and carry forward the learner seam; do not add time or infer completion from this guide.
