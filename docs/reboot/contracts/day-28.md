# Day 28 — 2026-11-22: Milestone 2 browser-to-database acceptance

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

## Concept (25 minutes)

**Assistant-provided capstone architecture:** [../assistant-architecture-notes-15-70.md#day-28](<../assistant-architecture-notes-15-70.md#day-28>). This note supplies the implementation decision and concise rationale; read it within this day's existing 25-minute concept/course allocation, not as extra work. Use it as context for the day's existing task; on implementation days, implement and test the stated design. It is supplied awareness, not a design exercise. No capstone architecture proposal or post-hoc justification is assigned; learner design proposals occur only for the assigned system-design interview question on scheduled mock days.

One request end to end, security and consistency boundaries.

## Interview-first system-design mock (45 minutes inside practice)

**Prompt:** [Q14](../interview/question-bank.md#q14) — Booking retry after a lost response. For this interview question only, write and save your initial proposal before hints, reference answers, or AI. Use the exact 5/5/15/10/10 phases and 100-point rubric in [the mock rubric](../interview/mock-rubric.md).

**Timebox:** 45 minutes of the existing 85-minute practice block for the mock, then 40 minutes for this EAI slice: Run the existing real browser-to-database selector to its first unmet boundary and record actual observations. No overtime. The unchanged full acceptance selector remains the completion gate.

**Deferred acceptance:** `S2-D28-1` is in the [Sprint 2 session scope](../interview/sprint2-session-scope.md). Preserve the actual result and keep this day pending until the named completion check and all original acceptance criteria pass.

## Evidence (10 minutes)

Automated report/screenshots and milestone rubric. Record dated attempt state (`not attempted`, `attempted`, `contract passed`, or `integrated passed`), exact command/exit status, changed paths, assertion before/after where applicable, assistance, and any blocker. Record observed results only. Record the assistant-provided architecture note used; no capstone design proposal or retrospective justification is assigned.

## Planned isolated command

`cd frontend && npm ci && npm run build`

`cd .. && CHROMIUM_BIN=/path/to/chromium ./mvnw -Plearner-tests -Dtest=Day28BrowserToDatabaseTest test`

The harness runs the real Playwright/Chromium browser against the built test route and reports all lifecycle assertions even when an earlier learner TODO blocks downstream behavior. The route is isolated for acceptance and does not change the production product page. Infrastructure setup and learner acceptance are separate; use the learner profile only when inspecting expected TODO failures.

## Separate DSA track

The daily DSA assignment, tests, review schedule, and attempt history live only in the [DSA preparation repository](https://github.com/advait-ravi-marathe/dsa-prep/tree/859e161f6553e9aca8dc501fda152780cec66ef0). This engineering contract contains no DSA assignment.
