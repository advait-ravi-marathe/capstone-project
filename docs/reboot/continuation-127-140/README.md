# CoverStory Days 127–140: local restore rehearsal and offline AWS gate

This batch verifies a real PostgreSQL 18.6 logical archive restored into a new, empty `template0` database, full business/recovery evidence, the actual packaged local JAR, and a quiesced local configuration cutover. AWS is studied only through a non-deployable offline plan. No account, credentials, resources, spending, external deployment or public app exposure is part of this work.

- Dates: 2027-02-23 through 2027-03-08, Sprint 10.
- Daily total 180/195/210 minutes by DSA tier (60/75/90 DSA; engineering 120) (build/test 85, one study block 25, evidence 10).
- Recovery days 133 and 140 rerun prior work only.
- PostgreSQL 18.6 client tools are supplied in the official `postgres:18.6` Docker image; local tool-version checks and an actual dump/restore selector are installed. CI checks the same image before running tests.
- The host app remains loopback-only and synthetic. The plan cannot turn cloud execution on. All cloud proof and permission gates remain pending.
- Day 134 is a same-artifact local configuration rehearsal: A stays selected on S until B on restored T passes every data, schema, JAR and readiness gate. A later write on T blocks a blind return to stale S.
- The offline cost fixture now models units and currency per line and retains compute, database storage, backups/snapshots, logs, secrets and transfer. These are conspicuously fictional arithmetic fixtures, not regional AWS prices.
- The Day 131 restore checks include a valid archive truncated after its table of contents, so restore work starts and the single-transaction option must roll back partial work. The full schema oracle includes formatted PostgreSQL type modifiers.
- Maarek lesson metadata and durations are copied from verified Udemy catalog responses; video content was not audited, and course access is not rechecked. Exact lesson titles/sections are embedded in each corresponding contract and ticket card.

[Manifest](manifest.json) · [Ticket-ready export](scrum-owner-manifest.json) · [Contracts](contracts/) · [Offline AWS design](aws-lab-design.json) · [Verification receipt](../../../evidence/continuation-127-140/verification.json)

The restore proof is local and single-database. It does not establish RDS point-in-time restore, cluster-global role recovery, production recovery time, percentile latency, AWS service availability, or a cross-version rollback. Same-artifact rollback is blocked after candidate-side writes.

Use root-preserving frontend build commands and the exact selectors in each contract. The Day 44/45 default browser transport smokes serve test-owned pages so the default instructor suite does not depend on learner UI buttons; tagged browser acceptance continues to cover the production learner UI. All credentials must remain outside logs and manifests. No learner completion is inferred from these instructor-owned tests or temporary witnesses.
