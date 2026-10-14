# CAP-84 — Validate mock provider event envelopes

**Saved day:** 045 · 27 November 2026 · **Primary ID:** CAP-84 · **Maximum allocation:** 150 minutes

## Purpose

Reject malformed or owner-mismatched synthetic callback records before payment reconciliation writes. The validator is a pure schema/domain seam. An instructor-owned adapter may map a `VALID` normalized event to the existing Payments command; validation itself never writes, calls a provider, or unlocks onboarding.

## Files

- Learner Python seam: `ai/coverstory/cap_84.py`
- Learner acceptance cases: `ai/tests/test_coverstory_cap84_acceptance.py`
- Instructor reference witnesses: `ai/tests/test_coverstory_cap84_infrastructure.py`
- Synthetic envelopes: `fixtures/coverstory/CAP-84/synthetic-v1.json`

Implement only `validate_provider_event(raw: dict, trusted: BatchContext) -> ValidationOutcome`. The trusted context supplies owner, expected amount/currency, and provider authorization. The event body does not establish its own authority. The accepted field set is exactly `ownerId`, `eventId`, `providerRef`, `schemaVersion`, `occurredAt`, `amountMinor`, `currency`, and `state`.

## Required checks

1. Reject missing and unknown fields. Diagnostics can identify a field name but must not include the field’s value or echo the input.
2. Require `schemaVersion` and `amountMinor` to be exact Python integers; `bool` is invalid even though it subclasses `int`. Schema version is exactly 1, and amount is a nonnegative integer.
3. Require an aware UTC timestamp and normalize it to UTC.
4. Match `ownerId` to the trusted batch owner, and match amount and `INR` currency to the expected payment before classifying the provider state.
5. Reject an unsupported provider source from the trusted context. A synthetic source is never treated as a live provider.
6. A valid result is one eligible normalized command input. It performs zero writes and never unlocks onboarding.

## Checks

Run independent fixture and defect witnesses:

```sh
python3 -m unittest ai.tests.test_coverstory_cap84_infrastructure -v
```

Run the learner contract separately. It is expected to reach the TODO until implemented:

```sh
python3 -m unittest ai.tests.test_coverstory_cap84_acceptance -v
```

The instructor witnesses use only synthetic inputs. A passing reference check does not establish provider integration, payment command behavior, onboarding state, or learner mastery.

## Saved acceptance mapping

| Case | Input | Expected |
|---|---|---|
| CAP-84-CS-01 | Owner A event, schema 1, expected INR amount, aware UTC time | `VALID`; one eligible normalized input, zero writes |
| CAP-84-CS-02 | Boolean or string schema version, or boolean amount | `INVALID_TYPE`; zero writes |
| CAP-84-CS-03 | Owner B envelope under trusted batch A | `OWNER_MISMATCH`; zero writes |
| CAP-84-CS-04 | Extra `apiKey` field | `UNKNOWN_FIELD`; diagnostics omit its value |
| CAP-84-CS-05 | Naive time or exact amount/currency mismatch | `INVALID_TIMEZONE` or `PAYMENT_MISMATCH`; no onboarding unlock |
| CAP-84-CS-06 | Unsupported source in trusted simulator context | Reject; never accept as a live callback |

## Boundary

Simulator authorization, schema validity, and payment matching are separate checks. Validating a callback creates no transaction, audit event, application, or onboarding state; the owning Payments adapter handles any authorized state transition after validation.
