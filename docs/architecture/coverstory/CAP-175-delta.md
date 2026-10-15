# CAP-175 bounded trace correlation

```mermaid
flowchart LR
  B[Browser explicit submit] --> A[Application command]
  A --> S[(Submission record: pending)]
  S -->|persisted trace ref| D[Simulated SQL dispatch job]
  D --> M[Mock insurer adapter]
  I[Later Support investigation] -. span link .-> D
  I -->|owner authorization first| S
```

The later support query is a new trace and links to completed dispatch work. A missing reference stays unresolved. Span IDs, trace IDs and links are observability metadata; they do not authorize reading the Application or change submission status. The provided witness is a pure instructor oracle, not a production tracing or Support API.
