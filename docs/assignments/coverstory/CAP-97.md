# CAP-97 — Compare grounded assistance with guessing

## Learner seam and paths

Implement `scoreModes(questions, sources, modelOnly, sourceGrounded)` in [CompareGroundedAssistanceWithGuessing.java](../../../src/main/java/coverstory/application/CompareGroundedAssistanceWithGuessing.java). The contract types preserve each supplied response and citation. The explicit synthetic cases are in [synthetic-v1.json](../../../fixtures/coverstory/CAP-97/synthetic-v1.json); learner acceptance tests are [CAP97AcceptanceTest.java](../../../src/test/java/coverstory/application/CAP97AcceptanceTest.java); the test-only independent oracle is [CAP97InstructorInfrastructureTest.java](../../../src/test/java/coverstory/application/CAP97InstructorInfrastructureTest.java).

Run setup and learner checks separately:

```sh
mvn -q -Dtest=CAP97InstructorInfrastructureTest test
mvn -q -Plearner-tests -Dtest=CAP97AcceptanceTest test
```

The first command checks fixed fixture values and the owner leakage witness without invoking the learner seam. The second is expected to fail at the explicit TODO until scoring is implemented. No live model/provider, network, payment, or consumer data is used.

## Contract and fixed results

There are exactly four cases: two answerable synthetic wording questions, one unsupported eligibility question, and one unavailable payable-price question. A decorative premium illustration is not an authoritative price source.

The model-only output has one exact supported answer out of four and retains two unsafe guesses on the unanswerable questions. The separate q2 answer is wrong and unsupported. The source-grounded output has two supported answers, safe abstentions on q3 and q4, and valid citations for both answerable cases: correct-or-safe-abstention is 4/4 and citation coverage is 2/2. These values describe this fixture only; they do not support a claim that grounding is generally superior.

The leakage canary replaces q1's public citation with a private B-only source while answering for owner A. It must increment leakage to one and fail the mode's safety result regardless of its accuracy metrics. Keep the canary visible in the report; do not filter the leaking answer or citations.

The model-only q4 response asserts a payable premium from an illustration. It is unsupported and cannot count as correct. Never infer a payable premium, eligibility, or underwriting decision from generated text.

## Learner ownership and limits

Learners implement the scoring, citation/source checks, safe-abstention count, unsupported-case list, and safety disposition. They may not change the fixture labels, expected outputs, owner rules, or source identity. No model call is part of this assignment. Passing these tests establishes behavior only for the four synthetic cases and says nothing about broad model quality or independent mastery.

The focused setup result is recorded in [result.json](../../evidence/coverstory/CAP-97/result.json) and [harness-self-check.json](../../evidence/coverstory/CAP-97/harness-self-check.json). The saved card names `CompareGroundedAssistanceWithGuessing.java`, `fixtures.json`, and the two evidence files as planned paths; this starter uses the canonical package path above and the versioned fixture under `fixtures/coverstory/CAP-97/`.
