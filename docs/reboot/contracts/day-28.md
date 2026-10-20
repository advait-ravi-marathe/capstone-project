# Day 28 — 2026-11-16: Milestone 2 browser-to-database acceptance

**State:** A tagged real Chromium → built React test route → Spring MVC → actual learner service methods → disposable PostgreSQL acceptance harness is installed. It checks form submission, quote snapshot rendering, selection, summary refresh, reload readback and direct row counts independently. In the committed learner state it must fail at the explicit TODO seams; no successful lifecycle behavior is claimed. Days 14–27 remain prerequisites.

**Course lesson:** No new lecture bundle. Conditional reference to [React - The Complete Guide (incl. Next.js, Redux)](https://www.udemy.com/course/react-the-complete-guide-incl-redux/); access is not confirmed. Revisit an earlier lesson only if useful. No new study assignment: trace the browser/API/database harness and verify the actual contract. No purchase/enrollment or extra daily time. See the [Days 1–30 course map](../course-learning-map.md). Worked guide: [course-learning-supplements.md](../course-learning-supplements.md#day-28--browserapidatabase-trace).

## Learner seam

`HomeSecureFlow` test. Keep the business rule in this focused subject. Do not implement later-day behavior early.

## Acceptance invariants

UI input → quote snapshot → owner selection → refreshed summary survives reload. Cover duplicate clicks, stale/expired conflict, other-user access and backend outage. No purchase or policy-issuance claim.

## Required edge cases

Success/reload; double submit; stale/expired; other user; outage.

## Prerequisite and isolation

Days 14–27; readiness checker reports gaps; real completed components and PostgreSQL. Earlier unfinished collaborators are replaced by explicit test-only canned values in this day's isolated test; do not copy their business solution into production. A later integrated gate remains blocked until its listed prerequisites are genuinely completed.

**System-design prompt (within the 25-minute concept block):** Draw the browser-to-database path and mark each prerequisite that is still blocked. See the [system design practice guide](../system-design-practice.md) and use only the applicable blank template.

## Concept (25 minutes)

One request end to end, security and consistency boundaries.

## Evidence (10 minutes)

Automated report/screenshots and milestone rubric. Record dated attempt state (`not attempted`, `attempted`, `contract passed`, or `integrated passed`), exact command/exit status, changed paths, assertion before/after where applicable, and an invariant/trade-off explanation. DSA notes include Java attempt, edge cases, complexity and independent-solve status.

## Planned isolated command

`cd frontend && npm ci && npm run build`

`cd .. && CHROMIUM_BIN=/path/to/chromium ./mvnw -Plearner-tests -Dtest=Day28BrowserToDatabaseTest test`

The harness runs the real Playwright/Chromium browser against the built test route and reports all lifecycle assertions even when an earlier learner TODO blocks downstream behavior. The route is isolated for acceptance and does not change the production product page. Infrastructure setup and learner acceptance are separate; use the learner profile only when inspecting expected TODO failures.

## Separate Java DSA block (60 minutes)

**Consolidation day — no new mandatory problem.** Use 25 minutes to refresh due patterns, 20 minutes to cold-solve the weakest due anchor, and 15 minutes for same-day/other due active recall and the error log. Planned review items: LC 49 (D+21 from Day 7), LC 20 (D+7 from Day 21), LC 704 (D+3 from Day 25). Recompute from actual first-attempt dates and record completion only after the review. If no review is actually due, cold-recall the weakest earlier anchor; failed recall displaces the next new anchor. Use [`../dsa-error-log-template.md`](../dsa-error-log-template.md).

## Interview transfer (inside the current evidence block)

**Question:** Explain one browser-to-database request, then inject an outage or stale version. Where does truth live?

**Observable evidence:** 20-minute mock replaces implementation: 5 minutes integrated demo, 5 controlled failure, 5 security/concurrency defense, 5 next bottleneck. Evidence must use real integrated components.

**Assistance and gap:** Record unaided, hinted, or AI-assisted work honestly. For the change/debug prompt, work without AI. Score the relevant rubric dimensions 0–2 and name one bounded repair if a gap remains; do not treat tests as explanation credit.

**Related source:** [A3](../interview-evidence-overlay.md#a3) (anecdotal); [A5](../interview-evidence-overlay.md#a5) (anecdotal); [A6](../interview-evidence-overlay.md#a6) (anecdotal). These references guide practice; they do not establish question frequency or predict interview content.

**Milestone timebox:** Use 20 minutes from the existing 85-minute implementation block (65 build/test + 20 mock); do not add hours. If the integration gate is blocked, explain the actual failing boundary and next diagnostic step, without presenting a passing demo.
