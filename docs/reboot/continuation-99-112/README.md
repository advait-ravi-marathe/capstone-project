# CoverStory Days 99–112: restart-safe local delivery

This isolated continuation teaches event-log-backed durable-work materialization for already committed synthetic ISSUED lifecycle events. Day 35 source issuance remains unchanged. Receipt commit precedes acknowledgment. No broker, scheduler, automatic retry, route, cloud resource, or real data is added.

- Schedule: 2027-01-30 through 2027-02-12, Sprint 8.
- The in28Minutes course is reference-only with access unconfirmed; catalog metadata verified, videos not audited. Assigned lessons are partial foundations. No React or AWS lesson is relevant to this local backend batch.
- Setup and process/browser plumbing are instructor scaffolding, not extra learner hours.

Daily engineering remains 120 minutes (85 implementation/testing, 25 study, 10 evidence).

[Chronological manifest](manifest.json) · [Ticket-ready export](scrum-owner-manifest.json) · [Dated contracts](contracts/) · [Verification receipt](../../../evidence/continuation-99-112/verification.json)

Recovery depends on repeated manual invocation, retained source, finite backlog, and resolved failures. This does not prove global order, database crash durability, or distributed exactly-once behavior.

Install the locked frontend test dependency before browser-backed default checks:

```sh
cd frontend && npm ci && npm run build && npm run build:local
```

If the Playwright browser CDN is unavailable and system Chromium is installed, select that binary explicitly:

```sh
CHROMIUM_BIN=/usr/bin/chromium ./mvnw test
CHROMIUM_BIN=/usr/bin/chromium ./mvnw -Dtest=Day99To112InfrastructureTest test
```

Learner acceptance remains separate and expected red at named TODOs.


Each day uses 120 engineering minutes: 25 study, 85 practice/testing, and 10 evidence.
