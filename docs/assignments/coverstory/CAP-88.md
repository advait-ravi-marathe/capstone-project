# CAP-88 — Test CoverStory module access roles

This is a bounded instructor harness for the saved CAP-88 access matrix. It implements a pure role/resource policy seam with synthetic vectors. It does not supply authenticated principals, a Spring Security filter chain, role-bound HTTP clients, or production authorization. The repository currently has no such adapter, and CAP-54 owner-scoped journey authorization remains a separate learner TODO. Consequently, saved Spring-client/HTTP assertions are still blocked pending that integration.

## Learner seam and commands

Implement only `CAP88AccessPolicySeam.decide(AccessRequest)`. The seam receives a typed actor, action and resource scope and returns allow/deny plus the permitted output fields. Unknown roles/actions deny. The learner must not add persistence, routes, IAM roles, credentials, or authorization side effects. The fixtures contain synthetic-only actor IDs and sentinel values.

```sh
PATH=/tmp/javac-launcher:/tmp/apache-maven-3.10.0/bin:$PATH mvn -q -s /tmp/maven-settings.xml -Dmaven.repo.local=/tmp/maven-repo -Dmaven.compiler.fork=true -Dmaven.compiler.executable=/tmp/javac-launcher/javac -Dmaven.compiler.release= -Dmaven.compiler.source=21 -Dmaven.compiler.target=21 -Dtest=CAP88InstructorInfrastructureTest test
PATH=/tmp/javac-launcher:/tmp/apache-maven-3.10.0/bin:$PATH mvn -q -s /tmp/maven-settings.xml -Dmaven.repo.local=/tmp/maven-repo -Dmaven.compiler.fork=true -Dmaven.compiler.executable=/tmp/javac-launcher/javac -Dmaven.compiler.release= -Dmaven.compiler.source=21 -Dmaven.compiler.target=21 -Plearner-tests -Dtest=CAP88AcceptanceTest test
```

The instructor oracle reads the fixture independently of the learner method, checks all 16 matrix rows, and proves the fixture rejects broad applicant ownership and role-only support/assistant mutants. The learner suite is intentionally separate and should fail on the unimplemented TODO. These runs do not claim HTTP access control.

## Matrix represented

- Applicant A can read/write only their own draft; private output fields are allowlisted.
- Catalog editor can edit catalog and cannot read applicant disclosures.
- Support sees only the minimum fields for an assigned case.
- Simulator can submit a synthetic provider event.
- Read-only assistant can inspect allowlisted evidence but cannot submit, charge, refund, underwrite, or mutate payment state.
- Unknown role and action deny.

## Readiness boundary

Pure policy harness: independently runnable. Spring role clients and actual request authorization: **BLOCKED** until an authentication/authorization adapter and module-bound route exist. Do not treat a passing policy suite as proof that requests are authorized before serialization or that owning-module persistence is protected. CAP-54 and CAP-83 cover different seams and do not supply that adapter.
