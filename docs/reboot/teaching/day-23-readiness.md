# Day 23 — Dependency health is not endpoint exposure

Required course foundation: “Step 12 - Get Production Ready with Spring Boot - 4 - Actuator” (7:33). The 25-minute block is 7:33 course, 3:00 focused reading, 8:00 worked trace, 3:00 prediction, and 3:27 pause buffer. Read only the official Actuator sections on endpoint exposure, health detail, and readiness/liveness.

## Indicator versus projection

Run the independent projection example:

```sh
./mvnw -Dtest=Day23ReadinessProjectionReferenceTest test
```

The instructor example converts an UP/DOWN indicator into a strict `{ "status": ... }` body, without database details. It demonstrates projection as a separate concern; it does not exercise a listening Actuator endpoint or real connectivity. The existing `Day23ActuatorInfrastructureTest` does exercise a real loopback endpoint and a disposable PostgreSQL probe. Its overall `/actuator/health` response has both `status` and `groups`, while the contract requires status only. Thus the overall endpoint contract currently fails that exact-key requirement. Readiness emits only status in the observed harness. Do not describe the all-endpoint exposure contract as passing or edit framework/security configuration in this lesson.

Indicator truth, response projection, endpoint availability, and database reachability are separate evidence. A canned probe can check indicator logic but cannot prove database connectivity. A safe exposure setting does not automatically satisfy a strict response shape.

**Readiness prompt:** What does the readiness dependency say, and what does the public endpoint reveal? Keep the known `groups` mismatch visible. Engineering remains 25/85/10.
