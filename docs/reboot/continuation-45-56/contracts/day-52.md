# Day 52 — 2026-12-10: Render the renewal reference view in React

## Contract

**Learner-owned seam:** `PolicyRenewalPreviewPanel`
**Status:** Contract defined; learner implementation not attempted.
**Time:** 180/195/210 minutes by DSA tier; engineering remains120 minutes:25 study,85 practice/testing,10 evidence.

### Expected behavior

Display all approved reference fields with the existing string premium prop, exact date/version/notice, and no renew/accept/purchase/bind action. A changed fixture replaces all old values; ready without a preview is a safe unavailable state. Rendering does not mutate or call back.

### Readiness before the task

- Day51 response meaning and prior React props/conditional rendering

### Acceptance and expected TODO state

The fixed selectors remain learner-owned. Before the named TODO is completed, the semantic test is expected to stop at that exact seam. Instructor references and transport smoke are separate evidence, not learner completion. Do not implement behavior in starter code or weaken assertions. Record the actual command, result and first blocker in `evidence/continuation-45-56/day-52.json` only after a real learner attempt.

**Acceptance commands:**
- `npm --prefix frontend run test:learner -- src/__tests__/PolicyRenewalPreviewPanel.test.tsx -t reference-only`

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

## DSA study (workload-tiered; actual attempts only)

**Time:** 1–2 substantive exercises = 60 minutes; 3 = 75 minutes; 4 = 90 minutes. Keep engineering at 120 minutes (25 study, 85 practice/test, 10 evidence), for 180/195/210 total minutes. Brief recall and logging are inside the tier; do not add exercises to fill time or exceed four. Failed/overdue retrieval displaces new guidance; excess due work carries forward with its original due date, including D+21.

**Guided/transfer selection:** First process actual failed or overdue D+3/D+7/D+21 retrieval, then due retrieval, then an eligible guided first encounter in the preserved prerequisite order. New introductions stop after roadmap Day 150. Candidate transfers come from the verified catalog only after checking prior exposure, access, and prerequisite readiness; unknown freshness is not fresh. Do not assign by old even/odd cadence.

Guided learning may use LeetCode Premium/editorial explanations. Before opening an editorial, attempt independently; if assistance is used, label the event guided/hinted/editorial-assisted even if accepted. A transfer is independent only after an unaided attempt, with the intended invariant explained; platform acceptance alone is insufficient. D, D+3, D+7, and D+21 are based on the earliest actual first-attempt local date, including guided attempts; never use planned dates or reset the clock. A transfer target does not start another anchor cycle.

Run `python3 scripts/dsa-review-queue.py --on-date "$(TZ=Asia/Kolkata date +%F)" --roadmap-day 52` first. The date command uses the actual Asia/Kolkata local study date; dates shown elsewhere are forecasts only. Record actual activity, assistance, outcome, and evidence in the append-only log; never infer completion from this contract. See [../../dsa/guided-transfer-policy.md](../../dsa/guided-transfer-policy.md) and [the DSA error log](../../dsa-error-log-template.md).
