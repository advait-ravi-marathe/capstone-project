# CoverStory Days 99–112: restart-safe local delivery

This isolated continuation teaches event-log-backed durable-work materialization for already committed synthetic ISSUED lifecycle events. Day 35 source issuance remains unchanged. Receipt commit precedes acknowledgment. No broker, scheduler, automatic retry, route, cloud resource, or real data is added.

- Schedule: 2027-01-26 through 2027-02-08, Sprint 8.
- Daily 180 minutes: separate Java DSA 60; engineering 120 (85 implementation/testing, 25 study, 10 evidence).
- DSA uses recorded actual attempts and D+3/D+7/D+21 review queue first; no new problem, attempt, or completion is inferred.
- The in28Minutes course is reference-only with access unconfirmed; catalog metadata verified, videos not audited. Assigned lessons are partial foundations. No React or AWS lesson is relevant to this local backend batch.
- Setup and process/browser plumbing are instructor scaffolding, not extra learner hours.

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
FDE alignment: See [the North Star amendment](../fde-north-star-amendment.md) and [reference-only alignment overlay](../fde-alignment.json). Existing curriculum, prerequisites, budgets, DSA history and learner status remain unchanged; this adds purpose and evidence boundaries, not completion credit.
