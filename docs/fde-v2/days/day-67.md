# Conditional learning Day 67: M2.2 proposal

**Status: Not started. Conditional proposal; no learner progress is inferred.**

## Fixed 180-minute budget

- DSA: 60 minutes in the separate DSA workflow; log attempts on their actual dates.
- Role interview/AWS bank: 30 minutes using the existing bank.
- AI: 90 minutes total. Longer DSA sessions reduce AI time and carry unfinished work forward; do not compress segments.

## AI allocation

**60 minutes — M2.2-S03: server/SQL boundary**

Study/worked example: FastAPI current-user dependency; parameterized ownership predicates across every protected route.

Learner seam/acceptance: Bind validated identity to read/search/ask/export/update/delete service calls. Existing route/service scaffold remains instructor-owned; learner owns a small policy integration and tests. Legacy records default-deny until an explicit ownership mapping exists.

Negative control: Remove a route guard or interpolate a client tenant into SQL; contract and exposure checks must fail.

**30 minutes — M2.2-S04: retrieval/cache isolation**

Study/worked example: Qdrant payload filtering/limitations; pre-filter versus top-k/post-filter example.

Learner seam/acceptance: `filter_retrieved_hits(context, hits, authoritative_documents)` is a second authoritative check. Any cache must be keyed by effective authorization/version or revalidated against current ACL/source dependencies.

Negative control: Cross-tenant same-text hit, guessed ID, missing payload, stale/deleted version, warmed cache after revocation; none may enter prompt/output.

**Accounting:** M2.2 substantive 90 min; buffer 0 min; visibility 0 min; M2.1 boundary buffer 0 min; unallocated 0 min. Role-bank focus: interview. Visibility is inside the AI 90-minute cap; do not count it twice.

[M2.2 detailed guide](../proposals/m22-detailed-days-62-82.md)

## Entry gate and truthful status

Do not progress until M2.1/P1 prerequisites are independently cleared. Day 62 shares the remaining 60 AI minutes after M2.1's separate 30-minute readback; if that readback or entry gate is pending, carry M2.2 forward. Deterministic/reference tests validate mechanics only. Model inference, live cloud, learner ownership, independent human evidence and milestone completion remain separate. Record the actual date, minutes, commands, outputs and carryover; calendar passage does not advance this day.
