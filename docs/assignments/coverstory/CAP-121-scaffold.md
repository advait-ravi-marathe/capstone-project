# CAP-121 instructor scaffold: immutable citation lineage

The saved primary is [Day 088](saved-days/day-088-cap-121.md), sourced from the final 594-day contract. This scaffold prepares one narrow read-only seam; it does not claim the product transition or Support case infrastructure exists.

## Prepared files

- Learner seam: `src/main/java/capstone/coverstory/support/internal/SourceLineageReader.java` (`TODO(CAP-121)` retained).
- Tagged learner acceptance: `src/test/java/capstone/coverstory/support/SourceLineageReaderTest.java` (3 cases).
- Independent offline oracle: `src/test/java/capstone/coverstory/support/CAP121ReferenceOracle.java`.
- Instructor fixture checks: `src/test/java/capstone/coverstory/support/CAP121InstructorInfrastructureTest.java`.
- Frozen input: `docs/coverstory/fixtures/cap-121.json`.
- Bounded proposed ER artifact: `docs/architecture/coverstory/CAP-121-delta.md`.

The fixture hash is `3637cd41563ac6b1c71303b88c87401011e886ba1f26eb20f658c84bc09c0ce2`. It locks synthetic policy content hashes and half-open UTF-8 byte offsets. Positive citation `ai1` resolves to `policy-A/v2/m2/pA`, bytes `[23,39)`, with zero application/offer/payment/policy writes. The broken-chain case includes v1 but removes exact v2. The foreign-owner case asks as owner A for a B-only citation and permits only the generic `Citation not found.` response.

## Commands

Instructor oracle:

```sh
./mvnw -Dtest=CAP121InstructorInfrastructureTest test
```

Learner acceptance, deliberately separate:

```sh
./mvnw -Plearner-tests -Dtest=SourceLineageReaderTest test
```

The learner command is expected to report three errors at the retained unsupported TODO until the learner implements the named seam. A passing instructor oracle does not imply learner completion.

## Explicit prerequisites and limits

The repository does not yet provide a public `SupportReadApi`, owning Application/Policy/Payments read adapters, a persisted Support case with atomic source/actor audit, or a production model boundary. The fixture and oracle do not simulate those as if present. Do not add whole-workflow code to the learner slot; record the runtime integration as blocked until those bounded prerequisites are prepared. No real policy, consumer record, insurer, model, network, or provider is used.

The architecture delta and explain-back are templates only. Record actual command, candidate commit/environment, expected/observed result, and assistance after execution; until then the receipt remains `NOT_RUN`. No learner score or timebox rehearsal is claimed.
