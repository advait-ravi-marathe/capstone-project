# Day 69 — 2027-01-02: Prove restart, logout, foreign-owner and no-write behavior

## Contract

**Learner-owned seam:** `Local runtime security and preview acceptance`
**Status:** Contract defined; learner implementation not attempted.

### Expected behavior

The packaged JAR is first started so Flyway applies migrations before the first database snapshot; it is then stopped and restarted against the same disposable PostgreSQL instance. The old session cookie is rejected, a newly selected synthetic owner sees byte-for-byte identical preview JSON, and preview, owner switching, and logout leave the full ordered contents of `training_policy_lifecycle`, `training_policy_lifecycle_event` (including versions), and `training_policy_idempotency` unchanged. The other owner cannot read the fixed owner-A policy (HTTP 404).

## Acceptance and expected TODO state

**Acceptance harness:** prerequisite-gated packaged learner proof

Separate logout and restart experiments. Logout starts with populated preview state and checks immediate rendered clearing plus session 401 while the process is live. Restart saves and verifies the exact cookie bytes as active before stop, then checks 401 on a distinct PID against the same DB/fixed clock, followed by a complete expected owner preview. DB snapshots bracket each operation; cookie secrets are never evidence.

**Acceptance selector:** `day69PreviewIsStableAcrossPackagedRestartAndLogout`
**Acceptance source:** `src/test/java/assignments/local/LocalPackagedBrowserAcceptanceTest.java`
**Command:**

```sh
scripts/package-sprint5-demo.sh
CHROMIUM_BIN=/usr/bin/chromium ./mvnw -Plocal-app,sprint5-local,learner-tests -Dtest=LocalPackagedBrowserAcceptanceTest#day69PreviewIsStableAcrossPackagedRestartAndLogout test
```

Stop at the first actual prerequisite TODO and record the reached boundary. A valid positive path is not green merely because its negative controls passed.

The package command must succeed immediately before the packaged selector. Artifact identity is checked by the stage receipt.

## Interview design mock (45 minutes inside the 85-minute practice block)

**Question:** [Q18 — Cache invalidation: owner privacy after a write](../../interview/question-bank.md#q18)

Design how a private read cache is invalidated after an owner switch, logout, or a write. Compare cache keying and invalidation strategies, including a late in-flight response. Transfer the state reasoning to the supplied UI/restart experiment; do not add a cache feature.

For this interview question only, save the initial proposal before opening hints, worked references, or AI. Use Requirements 5 minutes; Capacity and constraints 5; Initial architecture and data model 15; Bottlenecks, reliability, scaling 10; Trade-offs, failures, follow-ups 10. Use the [100-point rubric](../../interview/mock-rubric.md).

**Remaining task slice (40 minutes):**

- 5 minutes: preconditions and prediction.
- 25 minutes: Run the packaged journey and inspect whether the exact saved cookie was active before shutdown, whether logout is independent from restart, and whether populated private UI state clears immediately. The selector enforces these conditions with an exact-cookie live precondition and action-level database barriers; learner evidence must still record actual observed results.
- 10 minutes: run the named selector and record its result or first blocker. The full day acceptance above remains unchanged; unmet work stays open as **S5-D69-RESTART**.

## Selector limits and open evidence

Separate logout and restart experiments. Logout starts with populated preview state and checks immediate rendered clearing plus session 401 while the process is live. Restart saves and verifies the exact cookie bytes as active before stop, then checks 401 on a distinct PID against the same DB/fixed clock, followed by a complete expected owner preview. DB snapshots bracket each operation; cookie secrets are never evidence. Any learner-owned behavior not implemented yet remains explicitly incomplete; these assertions only make its first failure observable.

## Implementation/testing allocation (85 minutes)

Work only this day's seam. Reuse existing issuance, policy readback, eligibility, snapshot, idempotency and preview rules. Do not duplicate a domain rule or modify prior contracts. Use the fixed synthetic fixture, configured `Clock`, loopback local app and disposable PostgreSQL only where specified. No real identities, PII, passwords, OAuth, secrets, external services or persistent access grants. On a recovery day, replace planned work inside the existing 85 minutes; do not add overtime.

## Concept/course allocation (25 minutes)

**Assistant-provided capstone architecture:** [../../assistant-architecture-notes-15-70.md#day-69](<../../assistant-architecture-notes-15-70.md#day-69>). This note supplies the implementation decision and concise rationale; read it within this day's existing 25-minute concept/course allocation, not as extra work. Use it as context for the day's existing task; on implementation days, implement and test the stated design. It is supplied awareness, not a design exercise. No capstone architecture proposal or post-hoc justification is assigned; learner design proposals occur only for the assigned system-design interview question on scheduled mock days.

**Learning guidance (inside the single 25-minute engineering study allocation):**
- Course: [Master Spring Boot 3 & Spring Framework 6 with Java](https://www.udemy.com/course/spring-boot-and-spring-framework-tutorial-for-beginners/) — in28Minutes Official; access: confirmed owned. Do not purchase/enroll based on this map.
- Exact lesson title: **Step 21 - Understanding Spring Security Authentication**; exact section title: **Securing Spring Boot Applications with Spring Security** (4:17).
- Exact lesson title: **Step 22 - Exploring Spring Security Authorization**; exact section title: **Securing Spring Boot Applications with Spring Security** (9:47).
- Fit: partial foundation. The lessons cover auth concepts, not session invalidation or restart persistence. Trace the supplied cookie-session logout and foreign-owner scenarios in the packaged browser harness; compare complete PostgreSQL snapshots and never treat JWT examples as equivalent.
- Focused worked references: [Spring Security logout handling — logout architecture and URI customization](https://docs.spring.io/spring-security/reference/servlet/authentication/logout.html); [PostgreSQL transaction isolation — Read Committed statement snapshots](https://www.postgresql.org/docs/18/transaction-iso.html).
- Video viewing: 14:04 at 1×; the remaining 10:56 in the 25-minute cap is for pauses, the worked example and focused reference. Stop at 25 minutes; carry unfinished material forward and shrink implementation scope. Viewing is not learner completion.

## Evidence allocation (10 minutes)

Record command, exact result, architecture-note reference and learner-owned changed paths in `evidence/continuation-57-70/day-69.json`. Leave completion and actual dates blank until the work is actually done.

## Separate DSA track

The daily DSA assignment, tests, review schedule, and attempt history live only in the [DSA preparation repository](https://github.com/advait-ravi-marathe/dsa-prep/tree/859e161f6553e9aca8dc501fda152780cec66ef0). This engineering contract contains no DSA assignment.
