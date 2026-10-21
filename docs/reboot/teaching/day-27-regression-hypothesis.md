# Day 27 — Test a regression hypothesis

This is an instructor-owned toy trace, separate from the learner's saved quote-selection reproducer. It teaches how to test a hypothesis; it does not claim the capstone has a production regression.

## Worked boundary example

Toy parcel rule: charge 5 credits for weight up to and including 1,000 g, otherwise 8. A saved test reports `fee(1000) expected 5, actual 8`. Two hypotheses are plausible: the code used `< 1000` instead of `<= 1000`, or an upstream conversion changed 1,000 g into 1,000,000 mg.

Start with three assertions:

```java
assertEquals(5, fee(999));
assertEquals(5, fee(1000));
assertEquals(8, fee(1001));
```

If the function receives integer `1000` and only the boundary assertion fails, the strict comparison hypothesis fits. Change only the toy comparison to inclusive and rerun all three checks. If the function actually receives `1000000`, the unit-conversion hypothesis needs a discriminating observation at the input boundary; changing the comparison would conceal the real cause.

Run the independent instructor example with `./mvnw -Dtest=Day27RegressionHypothesisReferenceTest test`. Its positive reference checks 999/1000/1001; its explicit strict-comparison mutant is shown to disagree only at 1000. The test demonstrates the toy rule, not a repair to `QuoteSelectionPolicy`. A mock call count alone would not prove the fee.

## Transfer to the existing contract

Run `./mvnw -Plearner-tests -Dtest=Day27ContractTest test`. This is expected to fail at the named Day 1 TODO in the current learner state; that is unfinished assignment work, not an observed production regression. The saved JSON expects the same quote version under two distinct journeys to remain two keys. First inspect the assertion and establish what is actually failing. If there is no independent defect, report the TODO honestly and use the smallest real missing boundary test instead of inventing an incident.

The practice sequence is: reproducer → owning boundary → competing explanations → assertion that separates them → smallest justified change → affected checks. The conditional course videos (Good Unit Tests 5:34 and Response Matchers 5:07; Mockito Verify 6:40 optional) are only partial support and access is unconfirmed. Fit the guide and any viewing into the existing 25-minute study block. Practice remains 85 minutes; evidence remains 10.
