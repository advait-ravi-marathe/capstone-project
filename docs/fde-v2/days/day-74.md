# Conditional learning Day 74: M2.2 proposal

**Status: Not started. Conditional proposal; no learner progress is inferred.**

## Fixed 180-minute budget

- DSA: 60 minutes in the separate DSA workflow; log attempts on their actual dates.
- Role interview/AWS bank: 30 minutes using the existing bank.
- AI: 90 minutes total. Longer DSA sessions reduce AI time and carry unfinished work forward; do not compress segments.

## AI allocation

**60 minutes — M2.2-S07: container/reproducible build**

Study/worked example: Docker best-practice headings; review supplied minimal Dockerfile and config.

Learner seam/acceptance: Learner verifies clean build/test/config path, health/readiness distinction, non-root identity and image/context exclusion. General Dockerfile/CI plumbing is supplied; learner repairs only the scoped AI/security seam.

Negative control: Run as root or include fixture secrets/private corpus from build context; policy check must fail. Missing local Docker is BLOCKED, not pass.

**30 minutes — M2.2-S08: deadlines/retries/traces**

Study/worked example: OTel tracing and Python subprocess deadlines; worked shared-clock failure timeline.

Learner seam/acceptance: `remaining_budget(deadline, now)` and `retry_decision(error_kind, attempt, remaining)`; propagate trace ID; bound payload, output and concurrency using declared local limits. Measure a small development fixture before selecting thresholds.

Negative control: Restart deadline per retry, retry permanent auth/input failures, exceed attempt limit or leak content into spans.

**Accounting:** M2.2 substantive 90 min; buffer 0 min; visibility 0 min; M2.1 boundary buffer 0 min; unallocated 0 min. Role-bank focus: interview. Visibility is inside the AI 90-minute cap; do not count it twice.

[M2.2 detailed guide](../proposals/m22-detailed-days-62-82.md)

## Entry gate and truthful status

Do not progress until M2.1/P1 prerequisites are independently cleared. Day 62 shares the remaining 60 AI minutes after M2.1's separate 30-minute readback; if that readback or entry gate is pending, carry M2.2 forward. Deterministic/reference tests validate mechanics only. Model inference, live cloud, learner ownership, independent human evidence and milestone completion remain separate. Record the actual date, minutes, commands, outputs and carryover; calendar passage does not advance this day.
