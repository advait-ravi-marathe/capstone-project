# Day 5 — Read a decision table before writing policy code

**Required course bridge:** in28Minutes Official, *Master Spring Boot 3 & Spring Framework 6 with Java*, section “APPENDIX - Functional Programming - A Quick Review using Q&A Format”: “Core concepts of Functional Programming” (6:43; item 49578631). Catalog title verified; playback not verified. “Step 05 - Do Functional Programming Exercises with Streams, Filters and Lambdas” (5:30; item 35017372) is optional later. Streams are not needed for this rule.

Use 6:43 course viewing, this worked guide (14:17), and four minutes for readiness: total 25 minutes. The lesson gives functional-programming context; the guide teaches a concrete decision table before the eligibility implementation.

## Worked rule and table

The toy rule accepts a parcel only if its destination is exactly `LOCAL` and its weight is between 1 and 20 kg inclusive. Missing input has first precedence, then destination, then range:

| Destination | kg | First applicable rule | Outcome |
|---|---:|---|---|
| null | 30 | missing input | MISSING |
| REMOTE | null | missing input | MISSING |
| REMOTE | 30 | destination rule | LOCAL_ONLY |
| LOCAL | 0 | lower bound | WEIGHT_RANGE |
| LOCAL | 1 | none rejects | ACCEPT |
| LOCAL | 20 | none rejects | ACCEPT |
| LOCAL | 21 | upper bound | WEIGHT_RANGE |

```java
static String parcelDecision(String destination, Integer kg) {
    if (destination == null || kg == null) return "MISSING";
    if (!destination.equals("LOCAL")) return "LOCAL_ONLY";
    if (kg < 1 || kg > 20) return "WEIGHT_RANGE";
    return "ACCEPT";
}
```

For `REMOTE` with 30 kg, destination is the first rejecting rule, so the result is `LOCAL_ONLY`, even though the weight is also outside the toy range. Repeating the same call gives the same answer; this pure function reads no database, clock, or mutable external state.

Run the complete table and variations:

```sh
./mvnw -Dtest=Day05DecisionTableReferenceTest test
```

## Practice and transfer

Keep the existing 85-minute practice block: up to 15 minutes reproduce/observe the reference, 10 minutes predict and run the variation, 45 minutes on the original insurance policy seam, and 15 minutes for the existing selector and diagnosis. The existing 10-minute evidence block records outcomes, explanation, and assistance. If the bridge takes longer, reduce implementation scope and carry it forward.

Predict `LOCAL` + 19 → `ACCEPT` and `REMOTE` + 0 → `LOCAL_ONLY`; name the first applicable rule. Then use the **different, frozen insurance table** in `HomeSecureEligibilityPolicy`: required occupancy and amount boundaries, including its stated missing-field precedence. Do not copy toy labels or bounds into production.

**Readiness question:** Explain one row with simultaneous invalidities and why the pure policy reference needs neither Spring nor a database. Keep business decisions separate from HTTP error mapping.

Existing learner check: `./mvnw -Plearner-tests -Dtest=Day05ContractTest test`. The reference proves only the toy table and precedence, not insurance eligibility.

**Course scope:** the lesson is supporting material; the parcel table and its explicit precedence are an instructor worked supplement, not a claim that the video teaches insurance rules.
