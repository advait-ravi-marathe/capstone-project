# Day 4 — Collection ownership in a Java value

**Required course bridge:** in28Minutes Official, *Master Spring Boot 3 & Spring Framework 6 with Java*, section “Getting Started with Java Spring Framework - 5 - Key Questions about Spring”: “Step 13 - Exploring Java Bean vs POJO vs Spring Bean” (5:15; item 35020094). Catalog title verified; playback not verified. “Step 09 - Creating More Java Spring Beans in Spring Java Configuration File” (6:41; item 35020124) is optional setup reference, not additional required viewing.

Use the required 5:15 lesson, then this worked guide (15:45), then the four-minute readiness check: total 25 minutes. The lesson introduces bean terms; it does not teach collection ownership. This guide supplies that bridge before the product task.

## Worked example

A Java record has final component references, but a reference can still point to a mutable object. Copy the collection when the value is created:

```java
record Menu(List<String> items) {
    Menu { items = List.copyOf(items); }
}
var original = new ArrayList<>(List.of("tea", "water"));
var menu = new Menu(original);
original.add("juice");
assertEquals(List.of("tea", "water", "juice"), original);
assertEquals(List.of("tea", "water"), menu.items());
assertThrows(UnsupportedOperationException.class, () -> menu.items().add("coffee"));
```

The constructor copies the two list entries. Adding `juice` changes only the caller's list. The record exposes an unmodifiable list. If the constructor only assigned `items`, the caller could change the menu through its alias. `List.copyOf` copies collection membership, not arbitrary mutable element objects; it rejects null elements. Here the elements are immutable strings, so no deep-copy issue is hidden.

The independent reference test runs with:

```sh
./mvnw -Dtest=Day04ImmutableValuesReferenceTest test
```

## Practice and transfer

Keep the existing 85-minute practice block: up to 15 minutes reproduce/observe the reference, 10 minutes predict and run the variation, 45 minutes on the original catalog seam, and 15 minutes for the existing selector and diagnosis. The existing 10-minute evidence block records actual output, explanation, and assistance. If the bridge takes longer, reduce implementation scope and carry it forward.

Predict this variation before running the second reference assertion: create the source list `water, tea`, construct the menu, then remove the source's first item. The menu retains both entries in the original order.

Then work the existing `ProductCatalogService` assignment. Keep the frozen HomeSecure fixture, exact output order, unknown-resource behavior, and returned-collection invariant. The reference teaches alias/copy behavior; it does not solve fixture validation, duplicate detection, or the capstone service.

**Readiness question:** Who owns the source list? Why does a record alone not stop caller mutation? Name the copying cost and the limitation for mutable elements. Use the existing evidence slot; no architecture essay is added.

Existing learner check: `./mvnw -Plearner-tests -Dtest=Day04ContractTest test`. A reference test passing is instructor teaching evidence, not learner completion.

**Primary reference:** Java 21 [`List.copyOf` and unmodifiable lists](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/List.html), sections “Unmodifiable Lists” and `copyOf(Collection)`. The code and worked trace above are original teaching material.
