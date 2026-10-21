# Day 57 — Evidence gate before demo work

**Study: 25 minutes.** Read Java `Map.get(Object)`, `containsKey(Object)` and GOV.UK “Acceptance criteria”; then run the complete library-kiosk example below and predict which case a map-size check would misclassify. CI vocabulary is optional reference only; it does not replace the evidence exercise.

## Worked example

A fictional library kiosk needs two named prerequisites: `catalog-read` and `owner-filter`. A green package smoke is useful but cannot replace either prerequisite. Missing, `false`, or `null` means recover. A true flag records a claim about evidence; it does not authenticate the person or prove the command ran.

Runnable reference: `scripts/teaching/WorkshopReadinessReference.java`. Run `java scripts/teaching/WorkshopReadinessReference.java`. Its cases cover null/empty input, each missing key, false and null values, unrelated true keys, both true values, and input preservation. An implementation that accepts any two keys fails the unrelated-keys case.

Worked expected result: `catalog-read=true, owner-filter=false, package-smoke=true` means recover only `owner-filter` within today's practice time. The synthetic demo goal is: “Show an operator the existing policy preview only after the prior browser evidence is real.” The open question is whether the prior evidence was actually produced; this gate cannot answer it.

## Transfer

Use only the existing `day-55` and `day-56` evidence names. Compare both missing-key directions, empty/null map, false/null required values, unrelated true keys, both true, and unchanged input. Do not add another dynamic prerequisite system or infer completion from the instructor's smoke run.

**Practice 85:** 10 predict cases, 50 implement `LocalReadinessGate.assess`, 25 test/diagnose. **Evidence 10:** command, actual result, one prediction and first blocker. **Selector:** `./mvnw -Plearner-tests -Dtest=LocalReadinessGateAcceptanceTest#day57MissingOrFailedPriorEvidenceReplacesNewWork test`.
