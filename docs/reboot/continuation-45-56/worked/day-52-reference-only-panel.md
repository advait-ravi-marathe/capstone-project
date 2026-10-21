# Day 52 — Render a supplied reference value

Before the renewal task, run the independent `ParcelWeightReference.test.tsx` ready example. Its first wire value formats numeric2.5 as display string `2.50`; rerender with revision9, weight17.4 and a new date, and only the second values remain. The example has no dispatch or purchase action. This is a toy ParcelWeightPanel, not a solved renewal panel.

The complete component and tests are in [`frontend/src/infrastructure/ParcelWeightReference.test.tsx`](../../../../frontend/src/infrastructure/ParcelWeightReference.test.tsx); the exact command is `npm --prefix frontend test -- src/infrastructure/ParcelWeightReference.test.tsx`.

Transfer to the existing `PolicyRenewalPreviewPanel`: use distinct source values (`987.40`, version9, date `2028-04-17`, product/rate variations), preserve notice and displayed current premium, and expose no bind/renew/accept/purchase action. Rendering does not calculate a new price or mutate the input. Check both button and link roles.

Study25: 8:03 verified conditional-rendering lesson or official replacement,3:57 focused state source/prerequisite recap,9:00 supplied reference panel/trace/test,4:00 prediction. Practice85: 10 inspect current test,55 panel,20 variations. Evidence10. Optional unconfirmed course access is reference-only, no purchase. Official: [React conditional rendering](https://react.dev/learn/conditional-rendering#conditionally-returning-jsx), [state structure](https://react.dev/learn/choosing-the-state-structure#avoid-contradictions-in-state).
