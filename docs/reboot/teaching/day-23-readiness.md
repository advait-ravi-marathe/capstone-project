# Day 23 — Dependency health is not endpoint exposure

Required course foundation: “Step 12 - Get Production Ready with Spring Boot - 4 - Actuator” (7:33). The 25-minute block is 7:33 course, 3:00 focused reading, 8:00 worked trace, 3:00 prediction, and 3:27 pause buffer. Read only the official Actuator sections on endpoint exposure, health detail, and readiness/liveness.

The focused primary reference is the [Spring Boot Actuator endpoints guide](https://docs.spring.io/spring-boot/reference/actuator/endpoints.html): endpoint exposure, health details, and Kubernetes liveness/readiness distinctions. It explains exposure and health behavior; the worked test supplies this repository's exact response allowlist.

## Indicator versus projection

Run the live instructor acceptance with a disposable PostgreSQL instance:

```sh
./mvnw -Dtest=Day23ActuatorInfrastructureTest test
```

It starts an actual Actuator HTTP server and a temporary PostgreSQL database. A read-only probe checks a real database connection; after the test stops that database, health changes from UP/200 to DOWN/503. An explicit test-only projection witness keeps this infrastructure check green while the learner-owned projection remains unfinished. Both health URLs are asserted to return exactly `{"status":"UP"}` or `{"status":"DOWN"}`; `/actuator/env` and `/actuator/configprops` return 404.

The narrow learner seam is `HealthResponseProjection.publicOptions()`: choose options that hide component details and group names while Boot retains the evaluated status and its HTTP status code. The learner acceptance is a separate real-HTTP test:

```sh
./mvnw -Plearner-tests -Dtest=Day23HealthProjectionContractTest test
```

That test is currently expected to be red at the named projection TODO. `Day23ContractTest` independently remains red at the existing `DatabaseReadinessIndicator` TODO. Do not turn the instructor witness into learner evidence, or describe an unimplemented projection as complete.

Indicator truth, response projection, endpoint availability, and database reachability are separate evidence. This infrastructure test uses a real embedded PostgreSQL probe; the projection witness only substitutes the response-shape decision so that the current learner TODO does not make the instructor test fail.

**Readiness prompt:** What does the readiness dependency say, and what does the public endpoint reveal? Explain why status and HTTP code must remain aligned while component/group detail stays hidden. Engineering remains 25/85/10.
