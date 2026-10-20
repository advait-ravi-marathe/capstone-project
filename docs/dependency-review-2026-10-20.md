# Day 1 dependency review — 20 October 2026

The Java build uses Spring Boot `4.1.1` and JUnit Jupiter `6.1.3`. Spring
Boot's dependency BOM controls Spring Framework and web-runtime transitive
versions. Maven Central metadata was queried for these artifacts. Boot's
metadata lists `4.2.0-M2` as latest, which is a milestone; `4.1.1` is the
stable release selected here. JUnit's release metadata lists `6.1.3`. Spring's
official release post confirms Boot 4.1.1 is available from Maven Central and
includes dependency upgrades:
<https://spring.io/blog/2026/08/20/spring-boot-4-1-1-available-now>.

The legacy embedded-PostgreSQL harness remains at `io.zonky.test:embedded-postgres:2.2.2`,
which Maven Central metadata lists as the latest wrapper release. Its test
binaries are pinned to PostgreSQL `14.22.0` for the preserved CAP-42/45
contracts. Maven Central metadata lists `18.6.0` for the binary artifacts as
the current latest; the older test binary was retained to avoid changing those
legacy database contracts as part of Day 1. Linux amd64 and Darwin ARM64
artifacts are both declared. This version review is not an SCA assessment.

The official Spring advisory index was reviewed on this date:
<https://spring.io/security>. The starter does not include Spring Security,
Spring Data, Kafka, or AMQP, so advisories for those products are not direct
evidence of an issue in this small dependency set. This review is not a full
transitive SCA scan and does not claim that all advisories are resolved.

Verification used a temporary Oracle JDK `21.0.12.1` under `/tmp` after the
container's preinstalled Java runtime was found to lack `javac`. Maven needed a
temporary proxy settings file and OS CA truststore to resolve Central artifacts,
and `-Dmaven.repo.local=/tmp/d1-m2` because `/home/agent/.m2` is not writable.
With those execution-only overrides, the latest clean default Maven suite passed
20 tests, including live Spring Security/CSRF checks and the embedded PostgreSQL fixture reset. The focused learner suites ran and failed at their
explicit TODOs, as designed. The Spring Boot app started; a valid quote request
returned 500 at the unimplemented TODO, and a missing request body returned
400. These override runs are recorded in
`evidence/day-1/verification.json`; they do not establish that the unmodified
command works in this sandbox. A fresh learner with a full JDK 21 (including
`javac`), a writable Maven cache, and Maven Central access can use the
documented `./mvnw` commands without those sandbox-specific overrides.
