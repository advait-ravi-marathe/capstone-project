# CAP-124 — bounded read-only evidence tool boundary

```mermaid
sequenceDiagram
    participant M as Model arguments
    participant S as Strict schema (learner TODO)
    participant C as Trusted authenticated context
    participant A as ApplicationReadApi (owning module)
    M->>S: applicationId, limit only
    S->>S: exact keys + integer 1..50
    C->>S: principal supplied out of band
    alt invalid schema
      S-->>M: SCHEMA_REJECTED; no API invocation
    else valid request
      S->>A: evidenceForOwner(principal.ownerId, applicationId, limit)
      A-->>S: read-only timeline rows
      S-->>M: bounded evidence result
    end
```

The instructor reference oracle exercises the argument contract and a synthetic read port. It is test-only. This repository currently has no public `SupportReadApi` nor a concrete public Application module read adapter, so this artifact does not imply production tool registration or runtime readiness. The learner implementation remains TODO.

Rejected parameters: ownerId, SQL, cancel, submit, payment, wallet, quote acceptance, and support case mutation. Schema rejection is required before calling the owning API. A valid principal comes from trusted authenticated context and cannot be selected by the model.

Interview prompt: Why must `cancel=true` be rejected even if the model claims the user already approved cancellation? Because tool schema is an authority boundary; approval claims in model-controlled input cannot authorize state transitions. Mutation commands belong to explicit authenticated product flows.
