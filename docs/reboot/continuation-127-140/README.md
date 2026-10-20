# CoverStory Days 127–140: local restore rehearsal and offline AWS gate

This batch verifies a real PostgreSQL 18.6 logical archive restored into a new, empty `template0` database, full business/recovery evidence, the actual packaged local JAR, and a quiesced local configuration cutover. AWS is studied only through a non-deployable offline plan. No account, credentials, resources, spending, external deployment or public app exposure is part of this work.

- Dates: 2027-02-23 through 2027-03-08, Sprint 10.
- Daily 180 minutes: Java DSA 60; engineering 120 (build/test 85, one study block 25, evidence 10).
- Recovery days 133 and 140 rerun prior work only.
- PostgreSQL 18.6 client tools are supplied in the official `postgres:18.6` Docker image; local tool-version checks and an actual dump/restore selector are installed. CI checks the same image before running tests.
- The host app remains loopback-only and synthetic. The plan cannot turn cloud execution on. All cloud proof and permission gates remain pending.
- Maarek lesson metadata and durations are copied from verified Udemy catalog responses; video content was not audited, and course access is not rechecked. Exact lesson titles/sections are embedded in each corresponding contract and ticket card.

[Manifest](manifest.json) · [Ticket-ready export](scrum-owner-manifest.json) · [Contracts](contracts/) · [Offline AWS design](aws-lab-design.json) · [Verification receipt](../../../evidence/continuation-127-140/verification.json)

The restore proof is local and single-database. It does not establish RDS point-in-time restore, cluster-global role recovery, production recovery time, percentile latency, AWS service availability, or a cross-version rollback. Same-artifact rollback is blocked after candidate-side writes.

Use root-preserving frontend build commands and the exact selectors in each contract. All credentials must remain outside logs and manifests. No learner completion is inferred from these instructor-owned tests or temporary witnesses.
