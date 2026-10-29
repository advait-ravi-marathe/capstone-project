# Conditional learning Day 62: M2.2 proposal

**Status: Not started. Conditional proposal; no learner progress is inferred.**

## Fixed 180-minute budget

- DSA: 60 minutes in the separate DSA workflow; log attempts on their actual dates.
- Role interview/AWS bank: 30 minutes using the existing bank.
- AI: 90 minutes total. Longer DSA sessions reduce AI time and carry unfinished work forward; do not compress segments.

## AI allocation

**60 minutes — M2.2-S01: threat boundary / access matrix**

Study/worked example: FastAPI JWT current-user sections; trace subject→SQL ownership→vector filtering and show why hiding a button proves nothing.

Learner seam/acceptance: `authorize_document(context, document, action)`: decide own/shared-read/foreign/revoked/unknown actions from server-authenticated context. Cover read/write/delete and missing or malformed claims.

Negative control: Substitute request-body tenant for authenticated tenant; cross-tenant fixtures must be denied.

**Accounting:** M2.2 substantive 60 min; buffer 0 min; visibility 0 min; M2.1 boundary buffer 30 min; unallocated 0 min. Role-bank focus: AWS. Visibility is inside the AI 90-minute cap; do not count it twice.

[M2.2 detailed guide](../proposals/m22-detailed-days-62-82.md)

## Entry gate and truthful status

Do not progress until M2.1/P1 prerequisites are independently cleared. Day 62 shares the remaining 60 AI minutes after M2.1's separate 30-minute readback; if that readback or entry gate is pending, carry M2.2 forward. Deterministic/reference tests validate mechanics only. Model inference, live cloud, learner ownership, independent human evidence and milestone completion remain separate. Record the actual date, minutes, commands, outputs and carryover; calendar passage does not advance this day.
