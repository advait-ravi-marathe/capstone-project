# Day 63 — Keep recovery within the practice budget

**Study: 25 minutes.** No new lecture. Trace the supplied gallery guard example: a required `guard-check=false`, unrelated `bench-clean=true`, and 60 remaining practice minutes means recovery is required, the 60 is preserved, and no new work is added.

## Worked example

Run `java scripts/teaching/WorkshopRecoveryReference.java`. It applies the required `guard-check` to the remaining practice allocation, rejects values outside `0..85`, and always returns `newWorkAdded=false`. Expected output: `PASS: required evidence, bounded practice, no new work`. The complete cases are `{guard-check:false},60 → (true,60,false)`; missing key with 60 gives the same recovery; `guard-check=true,60 → (false,60,false)` for retrieval only; true with 0 remains 0. Values -1 or 86 are rejected, not capped or interpreted as a new budget. The native selector strengthens this only after the contract states its `0..85` practice-time boundary. It does not infer prior completion from flags.

**Transfer:** use the existing `day-62-security` key, preserve valid minutes, fail closed on missing/false evidence, and always keep `newWorkAdded=false`. Record the actual first blocker. **Practice 85:** 15 evidence triage, 45 recovery or consolidation, 15 timed trace, 10 retest. **Selector:** `./mvnw -Plearner-tests -Dtest=LocalReadinessGateAcceptanceTest#day63SecurityCheckpointKeepsRecoveryInsideDailyBudget test`.
