# Conditional learning Day 65: M2.2 proposal

**Status: Not started. Conditional proposal; no learner progress is inferred.**

## Fixed 180-minute budget

- DSA: 60 minutes in the separate DSA workflow; log attempts on their actual dates.
- Role interview/AWS bank: 30 minutes using the existing bank.
- AI: 90 minutes total. Longer DSA sessions reduce AI time and carry unfinished work forward; do not compress segments.

## AI allocation

**90 minutes — M2.2-S02: established local identity adapter**

Study/worked example: FastAPI token verification and hashing sections; distinguish signature-verified issuer/audience/expiry from base64 decoding.

Learner seam/acceptance: `verify_identity(token, verified_claims_provider, subject_lookup, ...)` is intentionally unsolved; the provider must call a maintained verifier that rejects signature/algorithm errors. The wrapper checks issuer/audience/expiry and server-derived subject/tenant. Never implement crypto.

Negative control: Accept unsigned, wrong-key, wrong issuer/audience, expired or disabled identity; identity acceptance must fail. The current callback mechanics tests do not validate cryptographic signatures or constitute authentication.

**Accounting:** M2.2 substantive 90 min; buffer 0 min; visibility 0 min; M2.1 boundary buffer 0 min; unallocated 0 min. Role-bank focus: interview. Visibility is inside the AI 90-minute cap; do not count it twice.

[M2.2 detailed guide](../proposals/m22-detailed-days-62-82.md)

## Entry gate and truthful status

Do not progress until M2.1/P1 prerequisites are independently cleared. Day 62 shares the remaining 60 AI minutes after M2.1's separate 30-minute readback; if that readback or entry gate is pending, carry M2.2 forward. Deterministic/reference tests validate mechanics only. Model inference, live cloud, learner ownership, independent human evidence and milestone completion remain separate. Record the actual date, minutes, commands, outputs and carryover; calendar passage does not advance this day.
