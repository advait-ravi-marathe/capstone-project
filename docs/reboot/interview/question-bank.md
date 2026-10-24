# Interview practice question bank

These 36 prompts are original, curated practice. They are not copied from a
company interview and make no claim about question frequency. Start each
prompt with the learner's own clarification and proposal before consulting an
AI response or a reference design. Existing EAI acceptance criteria take
precedence over these prompts.

## HTTP and service boundaries

1. **Q01 — Quote selection API:** What must the request identify, and what
   should happen when the same pair appears twice?
2. **Q02 — Validation boundary:** Which invalid request shapes should be
   rejected before a service call?
3. **Q03 — Expiry and time:** How would you make quote expiry deterministic
   across tests and production code?
4. **Q04 — Error contract:** How should clients distinguish malformed input,
   a stale quote, and a server failure?
5. **Q05 — Read pagination:** How would you page a growing quote history
   without unstable ordering?
6. **Q06 — Owner-scoped lookup:** How should a foreign quote compare with an
   absent quote at the HTTP boundary?
7. **Q07 — Price representation:** Which numeric type and rounding boundary
   protect a quoted premium from binary rounding surprises?
8. **Q08 — Shared error handling:** What belongs in controller advice, and
   what domain detail must not leak to a client?

## Data and consistency

9. **Q09 — Quote snapshots:** Which facts should be copied into an immutable
   quote snapshot, and which can remain catalog references?
10. **Q10 — Selection schema:** Which keys and constraints express the
    identity of one selection?
11. **Q11 — Request audit:** Which minimal audit fields support diagnosis
    without storing unnecessary sensitive input?
12. **Q12 — Ownership model:** Where should owner identity enter a read, and
    how can a client-supplied owner be prevented from widening access?
13. **Q13 — Atomic issue:** Which writes must commit or roll back together
    when issuing a policy?
14. **Q14 — Idempotency scope:** Should a request key be unique globally or
    within an owner, and what existing contract determines that choice?
15. **Q15 — Repeated intent:** How should an exact retry differ from reuse of
    the same key with changed intent?
16. **Q16 — Query plan:** What evidence would justify changing an index for a
    real read path?

## Concurrency and operations

17. **Q17 — Two writers:** How should two clients selecting the same quote
    version avoid a silent lost update?
18. **Q18 — Transaction isolation:** Which anomaly could affect this workflow,
    and what database observation would demonstrate it?
19. **Q19 — Unique conflict:** What should an application do when a database
    uniqueness constraint rejects a concurrent duplicate?
20. **Q20 — Retry boundary:** Which failures are safe to retry, and how would
    the client keep the same request identity?
21. **Q21 — Request timeout:** What does an HTTP timeout prove about a JDBC
    statement that may still be running?
22. **Q22 — Recovery:** How can an operator distinguish a failed write from
    a committed write whose response was lost?
23. **Q23 — Health checks:** What should a liveness check establish, and what
    belongs in a readiness check?
24. **Q24 — CI failure:** How would you separate a setup failure from a
    learner acceptance failure in the first failing CI step?

## Full-stack behavior

25. **Q25 — Owner switch:** How does the UI prevent one owner's data from
    remaining visible while the next owner's request is pending?
26. **Q26 — API evolution:** How can an API add an optional field without
    silently changing existing clients' interpretation?
27. **Q27 — Accessible errors:** How should a form expose validation and
    expired-session state to keyboard and screen-reader users?
28. **Q28 — CSRF and session:** Why does a valid cookie not replace a CSRF
    token on a state-changing request?
29. **Q29 — Loading and retry:** How should the UI distinguish a pending
    request, a retryable network failure, and a definitive conflict?
30. **Q30 — Browser acceptance:** Which independent observations show the
    rendered UI, HTTP response, and persisted rows agree?

## Later AI system prompts (gated)

Use Q31–Q36 only after the relevant data, authorization, and evaluation
foundations have passed their existing gates. These are design exercises;
they do not authorize production AI features or activate StreamForge.

31. **Q31 — First failed boundary:** Given a bounded service journey, where
    should an assistant stop and ask for human review rather than infer a
    consequential policy change?
32. **Q32 — Grounded explanation:** How could an explanation cite the exact
    policy source passage and expose missing source evidence?
33. **Q33 — Evaluation set:** What cases would test a policy explanation's
    factuality, refusal behavior, and source attribution?
34. **Q34 — Tool authorization:** How do you keep a model-proposed tool call
    from bypassing the authenticated owner's existing permissions?
35. **Q35 — Human approval:** Which actions require explicit human approval,
    and how can approval bind to the exact proposed action?
36. **Q36 — Value baseline:** What non-AI baseline and measurable outcome
    would justify adding a model to an existing workflow?
