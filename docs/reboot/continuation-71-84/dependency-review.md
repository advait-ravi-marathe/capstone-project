# Dependency review — Days 71–84

Reviewed 2026-10-20 from the effective Maven dependency tree and npm registry audit.
This is a targeted review of resolved dependencies, not a complete software
composition analysis of every transitive artifact.

## Resolved versions

| Component | Resolved version | Decision |
|---|---:|---|
| Spring Boot BOM | 4.1.1 | Existing repository pin retained. |
| Spring JDBC | 7.0.9 | Managed by the Boot BOM; required by the new pool-acquisition exception boundary. |
| HikariCP | 7.0.2 | Boot BOM managed version retained for compatibility. No HikariCP security advisory was found in the targeted official-source search; that is not an all-clear. Upstream's current change log also has 7.1.0, whose noted change concerns virtual-thread contention; this application remains synchronous. |
| PostgreSQL JDBC | 42.7.14 | Explicit override from the Boot BOM's 42.7.13 after upstream advisories identified 42.7.13 as affected. |
| Embedded PostgreSQL | 18.6.0 | Existing disposable test runtime retained. |
| React / React DOM | 19.3.1 | Existing lockfile pins retained. |

The pgJDBC 42.7.13 range was affected by [GHSA-rhp9-mr79-r74h](https://github.com/pgjdbc/pgjdbc/security/advisories/GHSA-rhp9-mr79-r74h)
(`requireAuth` silently accepted authentication methods when configured with
an empty allow set) and [GHSA-f64h-wr5q-3qf3](https://github.com/pgjdbc/pgjdbc/security/advisories/GHSA-f64h-wr5q-3qf3)
(short declared values could retain earlier protocol bytes). The [42.7.14
release notes](https://github.com/pgjdbc/pgjdbc/releases/tag/REL42.7.14) list
both fixes. Maven now resolves 42.7.14 for runtime and tests.

Spring's August 2026 advisories list 7.0.0–7.0.8 as affected for the reviewed
issues and 7.0.9 as the fixed release; the current BOM-resolved Spring JDBC is
7.0.9. See [CVE-2026-47884](https://spring.io/security/cve-2026-47884) for one
example and its fixed-version table.

`npm audit --json` reported 0 advisories for the current lockfile at review
time. No OWASP Dependency-Check, GitHub dependency review, or full transitive
Maven SCA report was produced. Do not treat this note or the npm result as
proof that every dependency is advisory-free; repeat registry and SCA checks
when the branch is integrated.

## Verification commands

- `./mvnw -Plocal-app -Dincludes=org.postgresql:postgresql,org.springframework:spring-jdbc,com.zaxxer:HikariCP dependency:tree`
- `npm audit --json` from `frontend/`
