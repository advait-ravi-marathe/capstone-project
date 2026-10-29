# Conditional learning Day 78: M2.2 proposal

**Status: Not started. Conditional proposal; no learner progress is inferred.**

## Fixed 180-minute budget

- DSA: 60 minutes in the separate DSA workflow; log attempts on their actual dates.
- Role interview/AWS bank: 30 minutes using the existing bank.
- AI: 90 minutes total. Longer DSA sessions reduce AI time and carry unfinished work forward; do not compress segments.

## AI allocation

**30 minutes — M2.2-S09: AWS gaps, no provisioning**

Study/worked example: AWS Well-Architected introduction/chosen pillar and AWS Budgets; map local identity/network/storage/compute to alternatives.

Learner seam/acceptance: Use the existing C0 result if it exists; record one selected gap, least-privilege service choice, region/data boundary, cost assumptions and teardown steps. If C0 is absent, mark missing.

Negative control: Fabricate C0 evidence or label a diagram “deployed/live”; evidence validator rejects both. No AWS account/configuration/resource action.

**60 minutes — M2.2-S10: release evidence / explanation**

Study/worked example: FastAPI and Qdrant sections; walk one synthetic denial end-to-end with server and retrieval checks separately.

Learner seam/acceptance: `evaluate_evidence(receipts)` can report only BLOCKED or ELIGIBLE_FOR_INDEPENDENT_REVIEW. Learner explains one actual failure/fix; independent review is still required.

Negative control: A missing actual receipt, synthetic auth/model, clean build only, or local-only plan cannot become M2.2 PASS.

**Accounting:** M2.2 substantive 90 min; buffer 0 min; visibility 0 min; M2.1 boundary buffer 0 min; unallocated 0 min. Role-bank focus: interview. Visibility is inside the AI 90-minute cap; do not count it twice.

[M2.2 detailed guide](../proposals/m22-detailed-days-62-82.md)

## Entry gate and truthful status

Do not progress until M2.1/P1 prerequisites are independently cleared. Day 62 shares the remaining 60 AI minutes after M2.1's separate 30-minute readback; if that readback or entry gate is pending, carry M2.2 forward. Deterministic/reference tests validate mechanics only. Model inference, live cloud, learner ownership, independent human evidence and milestone completion remain separate. Record the actual date, minutes, commands, outputs and carryover; calendar passage does not advance this day.
