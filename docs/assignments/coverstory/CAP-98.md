# CAP-98 · Rehearse compatible application schema expansion

## Learner task

In a disposable PostgreSQL fixture, draft and verify an additive nullable `source_version` column for application revisions. Preserve every existing revision and payment reference. Keep a v1 projection working. Prove a pre-commit migration failure restores the v1 schema and rows. Refuse automatic down migration when it would erase a v2-only value.

The learner seam is [RehearseCompatibleApplicationSchemaExpansion.java](../../../src/main/java/coverstory/application/RehearseCompatibleApplicationSchemaExpansion.java). It is intentionally unimplemented. Do not connect it to a production datasource or add a production migration. The v1 schema and synthetic rows live in [cap_98_v1.sql](../../../src/test/resources/coverstory/application/cap_98_v1.sql); the input contract is [synthetic-v1.json](../../../fixtures/coverstory/CAP-98/synthetic-v1.json).

## Acceptance evidence

The instructor oracle runs against embedded PostgreSQL 14.22 and checks:

1. Three synthetic revisions and three owner-scoped payment foreign keys survive the nullable expansion. The v1 query still reads the original projection and all `source_version` values remain null.
2. An injected pre-commit failure rolls back transactional DDL and rows. The v1 schema and all three references remain.
3. A v2-only value blocks down migration before `DROP COLUMN`. A deliberate unsafe down mutant demonstrates why row counts alone cannot establish preservation.
4. A restored isolated schema is compared against a captured snapshot for revision/reference counts, FK validity, v1 projection values, quote hashes, revision hashes, and terms hashes. Restarting the database is not used as restore evidence.

All values are synthetic. Each embedded database is created and discarded by its test. The instructor oracle does not invoke the learner seam; its six checks are expected to pass while the TODO remains.

## Run

Use the repository's Maven test profile and the current Java 21 toolchain. The instructor-only check is:

```sh
PATH=/tmp/javac-launcher:/tmp/apache-maven-3.10.0/bin:$PATH mvn -q -s /tmp/maven-settings.xml -Dmaven.repo.local=/tmp/maven-repo -Dmaven.compiler.fork=true -Dmaven.compiler.executable=/tmp/javac-launcher/javac -Dmaven.compiler.release= -Dmaven.compiler.source=21 -Dmaven.compiler.target=21 -Dtest=CAP98InstructorInfrastructureTest test
```

The learner acceptance suite is tagged `learnerAcceptance`; run it after implementing the seam with the `learner-tests` profile. Before implementation, failures at the explicit `TODO(CAP-98)` are expected and must not be reported as successful learner work.

## Design discussion

Use an expand, verify, switch sequence: add a nullable field while old reads remain valid, verify counts/references/hashes and restore evidence, then adopt new writes only after compatibility checks. Preserve immutable revision and quote evidence. If a later value cannot fit the old schema, restore or make a reviewed forward repair; do not erase it to make rollback appear clean.

Explain why restarting a process does not test restoration, why an additive nullable column supports an old reader, and why matching table counts can hide deleted v2-only data. The oracle tests a small synthetic fixture; it does not establish production rollout readiness, backup policy, deployment TLS, or key recovery.
