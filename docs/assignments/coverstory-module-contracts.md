# Instructor scaffolding: CoverStory module contracts

These contracts establish ownership and safe integration seams. They do not complete assessed product behavior. Each module keeps its persistence private; collaborators call public ports only. Learners implement use cases, authorization decisions, quote/application rules, simulated payment/refund reconciliation, policy dispatch transitions, and user flows according to the final daily cards when those are available.

## Current port map

| Owner | Source package | Allowed public surface | Learner-owned behavior |
| --- | --- | --- | --- |
| Catalog | `coverstory.modules.catalog` | Versioned synthetic offer references and catalog port | Search rules, comparison, quote semantics |
| Shortlist | `coverstory.modules.shortlist` | Owner-scoped shortlist port | Authorization, duplicate handling, ordering |
| Application | `coverstory.modules.application` | Authoritative resume state and revision-checked submission result | Owner checks, revision rules, journey state machine |
| Payments | `coverstory.modules.payments` | Integer minor-unit money and simulated payment port | Durable idempotency flow, cancel/refund/reconcile outcomes |
| Policy | `coverstory.modules.policy` | Simulated dispatch port and explicit dispatch states | Dispatch confirmation and safe transitions |
| Support | `coverstory.modules.support` | Owner-scoped support-case port | Case rules and authorized context retrieval |
| Business audit | `coverstory.modules.audit` | Bounded event value and append port | Same-transaction recording with the business change |
| Analytics | `coverstory.modules.analytics` | Bounded behavior event and best-effort port | Allowlisted event delivery and consent-aware collection |

## Data contracts in the first instructor schema

`src/main/resources/db/schema/ownership.sql` creates one PostgreSQL schema per owner. `constraints.sql` sketches owner-scoped journey state, a durable owner/idempotency uniqueness key, integer minor units plus currency, an append-only payment journal, separate business audit and bounded analytics tables, and simulated dispatch states. It has no real provider integration, consumer health fields, secrets, arbitrary analytics map, Redis, Kafka, or second database.

The SQL is a starter constraint set, not the final saved contract package. Before expanding schema details, reconcile these files against the verified final operating record and saved cards. Consumer retention and consent remain configuration seams rather than invented legal periods.

## Check instructor infrastructure separately

From the repository root:

```sh
python3 scripts/check_scaffold.py
./scripts/test_field_cipher.sh
python3 -m unittest discover -s ai/tests -p test_infrastructure.py
```

The first checks module and data boundaries. The second exercises the infrastructure AES-GCM adapter with a transient synthetic key; it does not create or store credentials. The third checks the existing CAP-44 fixture/tokenizer. The learner acceptance suites are separate and still expected to fail until implemented.
