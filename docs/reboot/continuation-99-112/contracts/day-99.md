# Day 99 — 2027-02-01: Define the durable source and minimal envelope

## Scope

Accept a supported immutable ISSUED envelope and trace the existing source transaction. Do not reimplement Day 35.

- Learner seam: `IssuanceEnvelope.validateSupported()`.
- Engineering total: 120 minutes, with 85 implementation/testing, 25 study, and 10 evidence. The study cap includes pauses and teach-back. Unfinished work carries forward and implementation scope shrinks.

**Assistant-provided architecture (what and why):** [Day 99 note](../assistant-architecture-notes.md#day-99). The assistant supplies these design decisions; implement and test the named seam. No capstone design proposal or post-hoc justification is assigned.

## Course and worked supplement

- Course (access unconfirmed; reference only): Master Hibernate and JPA with Spring Boot in 100 Steps — in28Minutes Official (https://www.udemy.com/course/hibernate-jpa-tutorial-for-beginners-in-100-steps/)
- Step 59 - Transaction Management - ACID Properties — exact section: Transaction Management (5:49)

### Which committed row makes recovery possible?

The committed lifecycle ISSUED event is durable intent. Keep only stable source identity, event type/date, and envelope version; policy, idempotency receipt, and event remain the existing atomic source boundary.

Focused source: [PostgreSQL 18 transactions](https://www.postgresql.org/docs/18/tutorial-transactions.html).

## Acceptance

- Positive envelope
- missing identity
- unsupported type/schema
- invalid source version/date

Learner selector (expected to reach its named TODO until implemented):

```sh
./mvnw -Plearner-tests -Dtest=DurableIssuanceAcceptanceTest#day99AcceptsOnlySupportedImmutableIssuedEnvelope test
```

Instructor infrastructure selector:

```sh
./mvnw -Dtest=Day99To112InfrastructureTest test
```

## Evidence

Record actual command, exit code, persistent source/work/receipt/ack state or process IDs as appropriate, cleanup, and remaining TODO in `evidence/continuation-99-112/day-99.json`. Keep learner, instructor, and temporary-witness outcomes distinct. The scheduled date is not completion evidence.

## Separate DSA track

The daily DSA assignment, tests, review schedule, and attempt history live only in the [DSA preparation repository](https://github.com/advait-ravi-marathe/dsa-prep/tree/859e161f6553e9aca8dc501fda152780cec66ef0). This engineering contract contains no DSA assignment.
