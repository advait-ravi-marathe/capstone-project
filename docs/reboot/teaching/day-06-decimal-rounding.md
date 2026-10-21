# Day 6 — Keep decimal arithmetic exact until the declared rounding boundary

**Conditional course reference:** in28Minutes Official, *Java Programming for Complete Beginners* (course 1535678; access unconfirmed; no purchase or enrollment). “Primitive Data Types And Alternatives in Java Programming”: “Step 05 - Introduction to BigDecimal Java Class” (6:58; item 9565516). The integer-addition and simple-interest videos are optional references and are not stacked into today.

If the course is accessible, use 6:58 viewing, 14:02 for this guide, and four minutes for readiness. If it is not, use the Java 21 `BigDecimal` documentation for seven minutes and this guide for 14 minutes, plus four minutes readiness. Either path totals 25 minutes. Course metadata does not establish access or playback.

## Worked arithmetic

```java
var quantity = new BigDecimal("2");
var unitFee = new BigDecimal("1.005");
var adjustment = new BigDecimal("0.005");
var exact = quantity.multiply(unitFee).add(adjustment);
var finalFee = exact.setScale(2, RoundingMode.HALF_UP);
assertEquals(new BigDecimal("2.015"), exact);
assertEquals(new BigDecimal("2.02"), finalFee);
var roundedTooEarly = quantity.multiply(unitFee.setScale(2, RoundingMode.HALF_UP))
        .add(adjustment).setScale(2, RoundingMode.HALF_UP);
assertEquals(new BigDecimal("2.03"), roundedTooEarly);
```

Exact multiplication gives `2.010`; adding the adjustment gives `2.015`, then final rounding gives `2.02`. Rounding the unit first changes `1.005` to `1.01`; multiplication and adjustment then produce `2.025`, which rounds to `2.03`. The rounding location is part of the declared rule.

Construct from decimal strings when that is the intended value. `new BigDecimal(0.1)` starts from the binary floating-point value. `compareTo` checks numeric value; `equals` also considers scale, so `2.0` and `2.00` compare numerically equal but are not equal representations.

```sh
./mvnw -Dtest=Day06DecimalReferenceTest test
```

## Practice and transfer

Keep the existing 85-minute practice block: up to 15 minutes reproduce/observe the reference, 10 minutes predict and run the variation, 45 minutes on the original premium seam, and 15 minutes for its selector and diagnosis. The existing 10-minute evidence block records the exact outputs, explanation, and assistance. If the bridge takes longer, reduce implementation scope and carry it forward.

Predict before running: quantity 3, unit fee `1.005`, adjustment `0.005` gives exact `3.020`, then `3.02` at scale 2. Transfer only exact decimal operations and the frozen rounding boundary to `PremiumCalculator`.

The existing contract permits zero at the pure calculator seam; Day 5 eligibility rejects it earlier. Do not add eligibility bounds to this calculator, or invent tax or insurance rules.

**Readiness question:** What breaks if rounding occurs before addition? Why can zero be valid here but rejected by eligibility?

Existing learner check: `./mvnw -Plearner-tests -Dtest=Day06ContractTest test`. The reference proves a separate fee example, not the capstone premium formula.

**Primary reference:** Java 21 [`BigDecimal`](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/math/BigDecimal.html), string versus double constructors, `setScale(int, RoundingMode)`, and `compareTo`/`equals`.
