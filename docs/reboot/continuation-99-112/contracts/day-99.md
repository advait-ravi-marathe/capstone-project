# Day 99 — 2027-01-30: Define the durable source and minimal envelope

## Scope

Accept a supported immutable ISSUED envelope and trace the existing source transaction. Do not reimplement Day 35.

- Learner seam: `IssuanceEnvelope.validateSupported()`.
- Java DSA queue: run `python3 scripts/dsa-review-queue.py --on-date "$(TZ=Asia/Kolkata date +%F)" --roadmap-day 99` for the actual local study date. Select 60/75/90 minutes by 1–2/3/4 substantive exercises; failed or overdue retrieval displaces new guidance.
- Engineering total: 120 minutes, with 85 implementation/testing, 25 study, and 10 evidence. The study cap includes pauses and teach-back. Unfinished work carries forward and implementation scope shrinks.

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

## DSA study (workload-tiered; actual attempts only)

**Time:** 1–2 substantive exercises = 60 minutes; 3 = 75 minutes; 4 = 90 minutes. Keep engineering at 120 minutes (25 study, 85 practice/test, 10 evidence), for 180/195/210 total minutes. Brief recall and logging are inside the tier; do not add exercises to fill time or exceed four. Failed/overdue retrieval displaces new guidance; excess due work carries forward with its original due date, including D+21.

**Guided/transfer selection:** First process actual failed or overdue D+3/D+7/D+21 retrieval, then due retrieval, then an eligible guided first encounter in the preserved prerequisite order. New introductions stop after roadmap Day 150. Candidate transfers come from the verified catalog only after checking prior exposure, access, and prerequisite readiness; unknown freshness is not fresh. Do not assign by old even/odd cadence.

Guided learning may use LeetCode Premium/editorial explanations. Before opening an editorial, attempt independently; if assistance is used, label the event guided/hinted/editorial-assisted even if accepted. A transfer is independent only after an unaided attempt, with the intended invariant explained; platform acceptance alone is insufficient. D, D+3, D+7, and D+21 are based on the earliest actual first-attempt local date, including guided attempts; never use planned dates or reset the clock. A transfer target does not start another anchor cycle.

Run `python3 scripts/dsa-review-queue.py --on-date "$(TZ=Asia/Kolkata date +%F)" --roadmap-day 99` first. The date command uses the actual Asia/Kolkata local study date; dates shown elsewhere are forecasts only. Record actual activity, assistance, outcome, and evidence in the append-only log; never infer completion from this contract. See [../../dsa/guided-transfer-policy.md](../../dsa/guided-transfer-policy.md) and [the DSA error log](../../dsa-error-log-template.md).
