# Day 1 dependency review — 20 October 2026

The Java build now uses Spring Boot `4.1.1` and JUnit Jupiter `6.1.3`. Spring
Boot's dependency BOM controls the Spring Framework and web-runtime transitive
versions so they stay aligned. Maven Central metadata was queried for the Boot
web starter and JUnit Jupiter; both pinned versions are published. Spring's
official release post confirms Boot 4.1.1 is available from Maven Central and
includes dependency upgrades:
<https://spring.io/blog/2026/08/20/spring-boot-4-1-1-available-now>.

The official Spring advisory index was reviewed on this date:
<https://spring.io/security>. The starter does not include Spring Security,
Spring Data, Kafka, or AMQP, so advisories for those products are not direct
evidence of an issue in this small dependency set. This review is not a full
transitive SCA scan and does not claim that all advisories are resolved.

The execution environment had a Java 21 runtime and the pinned Maven 3.10.0
wrapper distribution, but no `javac`. Maven's first direct DNS lookup failed;
a temporary proxy configuration resolved the dependency graph, after which
compilation stopped with `release version 21 not supported`. An attempted
temporary JDK download returned HTTP 403. Therefore no compilation or test
result and no full dependency tree or SCA result is claimed. In the sandbox,
Maven also requires `-Dmaven.repo.local=/tmp/d1-m2` because
`/home/agent/.m2` is not writable. A fresh learner with a full JDK 21 (including
`javac`), a writable Maven cache, and Maven Central access can use the
documented `./mvnw` commands.
