# Day 17 — Render typed UI states accessibly

## Study (25 minutes)

Course access is unconfirmed. The single required reference is course 1362070, “Making Components Reusable with Props [Core Concept]” (9:06). “Working with Props & TypeScript” (14:20) is replacement-only later, not stacked today. If the course is unavailable, replace its 9:06 slot with React’s [TypeScript with React Components](https://react.dev/learn/typescript) typed-props example and [input labels](https://react.dev/reference/react-dom/components/input#providing-a-label-for-an-input). Work the supplied state/render trace for 8 minutes, predict one-language and empty cases for 2:30, and leave 5:24 for pauses. Access/title verification is not video playback.

## Worked trace

`Day17ProductStatesReference.test.tsx` defines a separate `TourState` union: `loading`, `error`, `empty`, or `ready` with a tour. `TourChoices` receives state as a prop; it performs no fetch. Loading is announced with `role=status`, error with `role=alert`, empty has an explicit message, and ready renders a heading and labelled radio options. The tests query by role and accessible name: Hindi is the visible name; `HI` is its value. On rerender to error, the radio controls disappear.

Run:

```sh
npm --prefix frontend test -- --run src/infrastructure/Day17ProductStatesReference.test.tsx
```

Observed instructor reference: **2 tests pass**. This toy is separate from the learner `ProductPanel` and supplies no production URL.

## Practice and transfer

Inspect/reproduce for 15 minutes; predict the one-language and empty-state variation for 10; then work the existing component seam. Learner command remains `npm --prefix frontend test -- src/components/ProductPanel.test.tsx`.

Transfer the four existing `ProductPanelState` branches and its contract-supplied labels. Explain how the discriminated union narrows after the earlier returns and why a labelled control is different from visible text. Do not infer that TypeScript eliminates runtime input checks. Replace the broad system-design template with this concrete prop → state branch → accessible output trace; do not add a worksheet.
