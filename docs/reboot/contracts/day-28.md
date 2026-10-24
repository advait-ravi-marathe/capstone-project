# Day 28 — 2026-11-20: Milestone 2 browser-to-database acceptance

**State:** A tagged real Chromium → built React test route → Spring MVC → actual learner service methods → disposable PostgreSQL acceptance harness is installed. It checks form submission, quote snapshot rendering, selection, summary refresh, reload readback and direct row counts independently. In the committed learner state it must fail at the explicit TODO seams; no successful lifecycle behavior is claimed. Days 14–27 remain prerequisites.

**Course lesson:** No new lecture bundle. Conditional reference to [React - The Complete Guide (incl. Next.js, Redux)](https://www.udemy.com/course/react-the-complete-guide-incl-redux/); access is not confirmed. Revisit an earlier lesson only if useful. No new study assignment: trace the browser/API/database harness and verify the actual contract. No purchase/enrollment or extra daily time. See the [Days 1–30 course map](../course-learning-map.md). Worked guide: [day-28-integrated-review.md](../teaching/day-28-integrated-review.md).

## Learner seam

`HomeSecureFlow` test. Keep the business rule in this focused subject. Do not implement later-day behavior early.

## Acceptance invariants

UI input → quote snapshot → owner selection → refreshed summary survives reload. Cover duplicate clicks, stale/expired conflict, other-user access and backend outage. No purchase or policy-issuance claim.

## Required edge cases

Success/reload; double submit; stale/expired; other user; outage.

## Prerequisite and isolation

Days 14–27; readiness checker reports gaps; real completed components and PostgreSQL. Earlier unfinished collaborators are replaced by explicit test-only canned values in this day's isolated test; do not copy their business solution into production. A later integrated gate remains blocked until its listed prerequisites are genuinely completed.

**System-design prompt (within the 25-minute concept block):** Draw the browser-to-database path and mark each prerequisite that is still blocked. Use the [review-only integrated trace](../teaching/day-28-integrated-review.md#worked-trace); annotate actual observed boundaries and blockers.

## Concept (25 minutes)

One request end to end, security and consistency boundaries.

## Evidence (10 minutes)

Automated report/screenshots and milestone rubric. Record dated attempt state (`not attempted`, `attempted`, `contract passed`, or `integrated passed`), exact command/exit status, changed paths, assertion before/after where applicable, assistance, and any blocker. Record observed results only. Keep design reasoning in the learner-led proposal or review when assigned; no retrospective architecture or trade-off justification is required.

## Planned isolated command

`cd frontend && npm ci && npm run build`

`cd .. && CHROMIUM_BIN=/path/to/chromium ./mvnw -Plearner-tests -Dtest=Day28BrowserToDatabaseTest test`

The harness runs the real Playwright/Chromium browser against the built test route and reports all lifecycle assertions even when an earlier learner TODO blocks downstream behavior. The route is isolated for acceptance and does not change the production product page. Infrastructure setup and learner acceptance are separate; use the learner profile only when inspecting expected TODO failures.

## Interview transfer (inside the current evidence block)

**Question:** Explain one browser-to-database request, then inject an outage or stale version. Where does truth live?

**Observable evidence:** 20-minute mock replaces implementation time. Explain one observed browser/API/database case and one controlled failure; if blocked, explain the first failed boundary and next diagnostic without simulating success.

**Assistance and gap:** Record unaided, hinted, or AI-assisted work honestly. For the change/debug prompt, work without AI. Score the relevant rubric dimensions 0–2 and name one bounded repair if a gap remains; do not treat tests as explanation credit.

**Related source:** [A3](../interview-evidence-overlay.md#a3) (anecdotal); [A5](../interview-evidence-overlay.md#a5) (anecdotal); [A6](../interview-evidence-overlay.md#a6) (anecdotal). These references guide practice; they do not establish question frequency or predict interview content.

**Milestone timebox:** Use 20 minutes from the existing 85-minute implementation block (65 build/test + 20 mock); do not add hours. If the integration gate is blocked, explain the actual failing boundary and next diagnostic step, without presenting a passing demo.

## Separate DSA track

The daily DSA assignment, tests, review schedule, and attempt history live only in the [DSA preparation repository](https://github.com/advait-ravi-marathe/dsa-prep/tree/b9d4706a9ead281a8881ab3767aa415ad6039f14). This engineering contract contains no DSA assignment.
