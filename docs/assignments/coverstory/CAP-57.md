# CAP-57 — Atomic publication of prepared catalog versions

## Purpose

Publish one prepared synthetic health-offer version as the active catalog choice. The chosen version must remain stable for subsequent exports; replaying the same publication is idempotent, while changed terms under the same version identity conflict. Publication failures must leave the old selection intact.

## Start here

- Learner seam: `src/main/java/coverstory/modules/catalog/api/CAP57Seam.java`
- Synthetic fixture: `fixtures/coverstory/CAP-57/synthetic-v1.json`
- PostgreSQL schema: `src/main/resources/db/schema/catalog_publication.sql`
- Fixture rows: `src/test/resources/coverstory/catalog/cap_57_fixtures.sql`
- Learner contracts: `src/test/java/coverstory/modules/catalog/CAP57AcceptanceTest.java`
- Instructor database witness: `src/test/java/coverstory/modules/catalog/CAP57InstructorInfrastructureTest.java`
- Instructor HTTP/PostgreSQL witness: `src/test/java/coverstory/modules/catalog/CAP57PublicationRuntimeIntegrationTest.java`
- Independent test-only reference publisher: `src/test/java/coverstory/modules/catalog/CAP57ReferencePublicationAdapter.java`

Run schema and fixture checks with `mvn -Dtest=CAP57InstructorInfrastructureTest test`. Run the real Spring HTTP/PostgreSQL reference flow with `mvn -Dtest=CAP57PublicationRuntimeIntegrationTest test`. Run the learner contract suite with `mvn -Plearner-tests -Dtest=CAP57AcceptanceTest test`; the current starter intentionally stops at explicit TODO exceptions.

## Safety boundaries

All rows and values are synthetic. Amounts use integer minor units and INR. The immutable version table stores prepared data; the active table stores only the selected version. The public export excludes restrictions. This seam does not call insurers, payment providers, Kafka, or production services.

Each learner acceptance method starts and closes its own fresh embedded PostgreSQL instance. The instructor test also starts two independent instances, publishes in one, and positively verifies the second contains the three prepared versions but no active publication. It verifies schema constraints, immutable rows, export projection, duplicate behavior, conflict behavior, and rollback after an injected pre-commit failure. These checks validate setup; they do not complete the learner implementation or demonstrate independent mastery.

The additional instructor runtime test starts the actual Spring application against disposable PostgreSQL and posts through a test-profile-only publication controller. The independent reference publisher atomically writes `active_health_offer` and `publication_audit_event`; a synthetic editor filter is supplied only by that test profile. It verifies anonymous rejection, publish, idempotent retry without a second audit row, changed-hash conflict, rollback after both writes, publication of a second version, exact deterministic export fields, public `/api/catalog/versions` status, and persistence across application restart. The ordinary application does not register this instructor route, and `CAP57Seam` remains TODO.
