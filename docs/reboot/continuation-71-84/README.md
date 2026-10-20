# CoverStory continuation — Days 71–84

This is a planning contract and instructor starter batch based on the verified
Day 70 head `db2caf3adabf11e6d619ab9a7f7e1f5aa40f2a24`. It defines a single
bounded synchronous read path for the existing synthetic HomeSecure renewal
preview. It does not edit the Scrum board or change the Day 48 learner-owned SQL.

- [Chronological manifest](manifest.json)
- [Documentation-only Scrum Story export](scrum-owner-manifest.json)
- [DSA study map](dsa/tree-study-map.md)
- [Dependency review](dependency-review.md)
- [Day 71–84 contracts](contracts/)
- Evidence files under `evidence/continuation-71-84/`

Scope is intentionally narrow: two nonblocking process-local permits around
the existing synchronous MVC/JDBC read, the existing Hikari pool maximum of
four with a finite one-second acquisition timeout, two sanitized capacity
errors, fixed-cardinality metrics, accessible manual retry, and recovery
verification using real pool/lock contention. No work queue, asynchronous
execution, automatic retry, fault route or extra infrastructure is added.

All learner implementations remain unattempted. Day 83 packaged JAR/browser
lock recovery is not installed until its real prerequisites and test harness
are available. The actual-attempt log is empty; DSA review forecasts are not
completion evidence.
