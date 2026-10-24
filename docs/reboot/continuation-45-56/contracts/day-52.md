# Day 52 — 2026-12-14: Render the renewal reference view in React

## Contract

**Learner-owned seam:** `PolicyRenewalPreviewPanel`
**Status:** Contract defined; learner implementation not attempted.

### Expected behavior

Display all approved reference fields with the existing string premium prop, exact date/version/notice, and no renew/accept/purchase/bind action. A changed fixture replaces all old values. Rendering does not mutate or call back.

### Readiness before the task

- Day51 response meaning and prior React props/conditional rendering

### Acceptance and expected TODO state

The fixed selectors remain learner-owned. Before the named TODO is completed, the semantic test is expected to stop at that exact seam. Instructor references and transport smoke are separate evidence, not learner completion. Do not implement behavior in starter code or weaken assertions. Record the actual command, result and first blocker in `evidence/continuation-45-56/day-52.json` only after a real learner attempt.

**Acceptance commands:**
- `npm --prefix frontend run test:learner -- src/__tests__/PolicyRenewalPreviewPanel.test.tsx -t "reference-only"`

**Instructor infrastructure/reference commands:**
- `./mvnw -Dtest=RenewalPreviewManifestInfrastructureTest test`

## Practice and evidence

**85-minute practice allocation:**
- 10 minutes: compare the supplied ParcelWeight panel.
- 55 minutes: implement the renewal panel.
- 20 minutes: test changed data and forbidden actions.

**10-minute evidence allocation:** record actual command/output, source SHA, one concise explanation, and the first blocker or result. Do not infer learner completion from a green instructor reference.

## Study block — exactly 25 minutes

- Required source: **Rendering Content Conditionally**, section **React Essentials - Components, JSX, Props, State & More** (8:03), access `unconfirmed_reference_only`. No viewing is inferred.
- Worked guide: [Render a supplied reference value](../worked/day-52-reference-only-panel.md). The exact 25-minute segments are in `fde45-56-v2-curriculum.json`; all study items share that cap.

**Focused official sources:**
- [React: Conditional Rendering](https://react.dev/learn/conditional-rendering#conditionally-returning-jsx) — Conditionally returning JSX.
- [React: Choosing the State Structure](https://react.dev/learn/choosing-the-state-structure#avoid-contradictions-in-state) — Avoid contradictions in state.

Optional references are deferred and replacement-only; no purchase/enrollment or extra minutes are required.

## Safety and scope

This is a synthetic, read-only reference preview. It does not renew, accept, bind, price a new offer, collect payment or promise coverage. The browser harness uses a test-injected principal; that fixture is not production authentication. Real PostgreSQL/browser checks are restricted to disposable local test infrastructure.

## Separate DSA track

The daily DSA assignment, tests, review schedule, and attempt history live only in the [DSA preparation repository](https://github.com/advait-ravi-marathe/dsa-prep/tree/b9d4706a9ead281a8881ab3767aa415ad6039f14). This engineering contract contains no DSA assignment.
