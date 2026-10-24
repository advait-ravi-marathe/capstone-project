# System design question bank

These 36 original questions cover canonical interview systems and the
distributed concepts needed to reason about them. They are organized by
difficulty, concept, and patterns. They are practice prompts, not copied
company questions or claims about interview frequency. For every prompt, write
your own requirements, boundary, invariant, and first test before hints,
reference answers, or AI. A question never expands the current product scope.

## Foundations

<a id="q01"></a>
1. **Q01 — URL shortener: smallest useful design.** What must a short link
  guarantee from creation through redirect, and what should a redirect do for
  an unknown key? **Concepts:** API, key identity, availability. **Patterns:**
  [short-key allocation](pattern-library.md#short-key-allocation),
  [HTTP boundary](pattern-library.md#http-boundary).

<a id="q02"></a>
2. **Q02 — Short-link abuse and operations.** Which abuse limits, diagnostics,
  and recovery controls are needed without slowing every redirect?
  **Concepts:** safety, operational signals, resource bounds. **Patterns:**
  [rate limiting](pattern-library.md#rate-limiting),
  [measurement](pattern-library.md#measure-before-scaling).

<a id="q03"></a>
3. **Q03 — URL expiry and deterministic time.** How would expiry, custom aliases,
  and clock boundaries behave without sleeping in tests? **Concepts:** time,
  retention, immutable creation facts. **Patterns:**
  [expiry and retention](pattern-library.md#expiry-and-retention),
  [injected clocks](pattern-library.md#deterministic-time).

<a id="q04"></a>
4. **Q04 — Rate limiter: one client and one window.** Which requests are
  admitted, and when does the budget reset? **Concepts:** identity, window,
  fairness. **Patterns:** [rate limiting](pattern-library.md#rate-limiting),
  [clock boundary](pattern-library.md#deterministic-time).

<a id="q05"></a>
5. **Q05 — CoverStory domain workflow.** How should a quote become an issued
  policy while preserving owner, quote version, and the accepted result?
  **Concepts:** workflow, state transitions, trust boundary. **Patterns:**
  [domain workflow](pattern-library.md#domain-workflow),
  [immutable snapshot](pattern-library.md#immutable-snapshot).

<a id="q06"></a>
6. **Q06 — File storage: upload and download.** Which metadata belongs in a
  relational database, and which bytes can live in object storage?
  **Concepts:** ownership, metadata, large payloads. **Patterns:**
  [object storage](pattern-library.md#object-storage),
  [ownership boundary](pattern-library.md#ownership-boundary).

<a id="q07"></a>
7. **Q07 — API gateway: route and protect.** Which checks belong at the edge,
  and which must remain in the owning service? **Concepts:** routing,
  authentication, authorization. **Patterns:**
  [gateway boundary](pattern-library.md#gateway-boundary),
  [ownership boundary](pattern-library.md#ownership-boundary).

<a id="q08"></a>
8. **Q08 — API gateway: safe error contract.** How can the gateway preserve
  stable client errors while avoiding internal detail leaks? **Concepts:**
  HTTP semantics, failure ownership. **Patterns:**
  [HTTP boundary](pattern-library.md#http-boundary),
  [timeouts and retries](pattern-library.md#timeouts-and-retries).

<a id="q09"></a>
9. **Q09 — Notification service: accept and deliver.** What does “accepted”
  mean when a provider can be unavailable? **Concepts:** durable intent,
  delivery state. **Patterns:** [outbox and messaging](pattern-library.md#outbox-and-messaging),
  [retry boundary](pattern-library.md#timeouts-and-retries).

<a id="q10"></a>
10. **Q10 — Notification service: retries.** How would you prevent duplicate
  messages after a worker times out? **Concepts:** idempotency, retry,
  observable outcome. **Patterns:** [idempotency](pattern-library.md#idempotency),
  [outbox and messaging](pattern-library.md#outbox-and-messaging).

<a id="q11"></a>
11. **Q11 — Scheduler: run one job.** How do you represent a scheduled job,
  its due time, and a successful completion? **Concepts:** leases, durable
  state, time. **Patterns:** [scheduled work](pattern-library.md#scheduled-work),
  [deterministic time](pattern-library.md#deterministic-time).

<a id="q12"></a>
12. **Q12 — Scheduler: missed or duplicate execution.** What happens when a
  worker stops after claiming a job but before recording completion?
  **Concepts:** recovery, leases, duplicate execution. **Patterns:**
  [scheduled work](pattern-library.md#scheduled-work),
  [idempotency](pattern-library.md#idempotency).

<a id="q13"></a>
13. **Q13 — Booking: reserve one seat atomically.** Which rows or constraints
  prevent two requests from reserving the final seat? **Concepts:** atomicity,
  concurrency, durable uniqueness. **Patterns:**
  [transaction boundary](pattern-library.md#transaction-boundary),
  [optimistic and unique guards](pattern-library.md#concurrency-guards).

## Intermediate

<a id="q14"></a>
14. **Q14 — Booking: retry after a lost response.** How can a client repeat a
  booking safely when it cannot tell whether the first write committed?
  **Concepts:** intent identity, readback, conflict. **Patterns:**
  [idempotency](pattern-library.md#idempotency),
  [transaction boundary](pattern-library.md#transaction-boundary).

<a id="q15"></a>
15. **Q15 — Messaging: publish an order event.** How do you keep a committed
  order from losing its event when the broker is briefly unavailable?
  **Concepts:** database/broker boundary, eventual delivery. **Patterns:**
  [outbox and messaging](pattern-library.md#outbox-and-messaging),
  [backpressure](pattern-library.md#backpressure).

<a id="q16"></a>
16. **Q16 — Messaging: ordering and replay.** Which ordering guarantee is
  actually required, and how can a consumer handle a duplicate event?
  **Concepts:** partition key, sequence, idempotent consumer. **Patterns:**
  [outbox and messaging](pattern-library.md#outbox-and-messaging),
  [idempotency](pattern-library.md#idempotency).

<a id="q17"></a>
17. **Q17 — Cache: accelerate a read.** Which measured read path would justify
  caching, and what freshness does the caller require? **Concepts:** latency,
  hit rate, staleness. **Patterns:** [cache policy](pattern-library.md#cache-policy),
  [measurement](pattern-library.md#measure-before-scaling).

<a id="q18"></a>
18. **Q18 — Cache: invalidation after a write.** How can the application
  prevent stale private data from surviving a change of owner or version?
  **Concepts:** invalidation, versioning, security. **Patterns:**
  [cache policy](pattern-library.md#cache-policy),
  [ownership boundary](pattern-library.md#ownership-boundary).

<a id="q19"></a>
19. **Q19 — Payments: accept one payment intent.** Which states distinguish
  pending, authorized, captured, declined, and unknown outcome? **Concepts:**
  state machine, external boundary, audit. **Patterns:**
  [payment state](pattern-library.md#payment-state),
  [idempotency](pattern-library.md#idempotency).

<a id="q20"></a>
20. **Q20 — Payments: reconcile timeout.** A provider timed out after the
  request left your process. How do you avoid charging twice and learn the
  final result? **Concepts:** uncertain outcome, reconciliation. **Patterns:**
  [payment state](pattern-library.md#payment-state),
  [timeouts and retries](pattern-library.md#timeouts-and-retries).

<a id="q21"></a>
21. **Q21 — Webhook delivery: register and send.** How should a receiver
  verify origin and process a repeated delivery? **Concepts:** signatures,
  replay, delivery state. **Patterns:** [webhook delivery](pattern-library.md#webhook-delivery),
  [idempotency](pattern-library.md#idempotency).

<a id="q22"></a>
22. **Q22 — Webhook delivery: backoff and dead letter.** Which failures are
  retried, when do attempts stop, and how is replay made safe?
  **Concepts:** retry schedule, dead-letter handling. **Patterns:**
  [webhook delivery](pattern-library.md#webhook-delivery),
  [backpressure](pattern-library.md#backpressure).

<a id="q23"></a>
23. **Q23 — Event pipeline: ingest and process.** How do you preserve a
  durable source record while processing volume grows? **Concepts:** ingestion,
  partitioning, consumer lag. **Patterns:**
  [event pipeline](pattern-library.md#event-pipeline),
  [backpressure](pattern-library.md#backpressure).

<a id="q24"></a>
24. **Q24 — Event pipeline: correction and replay.** How can you rebuild a
  derived result after a bug without duplicating side effects?
  **Concepts:** replay, checkpoints, versioned projections. **Patterns:**
  [event pipeline](pattern-library.md#event-pipeline),
  [idempotency](pattern-library.md#idempotency).

<a id="q25"></a>
25. **Q25 — Chat: send and read messages.** Which storage and ordering rules
  make one conversation understandable across devices? **Concepts:** durable
  writes, sequence, pagination. **Patterns:**
  [chat ordering](pattern-library.md#chat-ordering),
  [stable pagination](pattern-library.md#stable-pagination).

<a id="q26"></a>
26. **Q26 — Chat: reconnect and presence.** Which state can be ephemeral, and
  what happens when a client reconnects after missing updates? **Concepts:**
  presence, resync, fan-out. **Patterns:** [chat ordering](pattern-library.md#chat-ordering),
  [backpressure](pattern-library.md#backpressure).

<a id="q27"></a>
27. **Q27 — Feed: build a page.** How can a feed provide stable pagination
  while new items arrive? **Concepts:** cursor, rank, read model. **Patterns:**
  [stable pagination](pattern-library.md#stable-pagination),
  [read scaling](pattern-library.md#read-scaling).

<a id="q28"></a>
28. **Q28 — Feed: fan-out choice.** When would you precompute recipient feeds,
  compute on read, or combine both? **Concepts:** skew, write amplification,
  freshness. **Patterns:** [feed fan-out](pattern-library.md#feed-fan-out),
  [measure before scaling](pattern-library.md#measure-before-scaling).

## Advanced

<a id="q29"></a>
29. **Q29 — Autocomplete: prefix lookup.** Which index and ranking model fit
  the expected prefix and latency requirements? **Concepts:** trie/search
  index, top-k, freshness. **Patterns:** [search index](pattern-library.md#search-index),
  [read scaling](pattern-library.md#read-scaling).

<a id="q30"></a>
30. **Q30 — Autocomplete: update and privacy.** How do you handle deletions,
  stale suggestions, and tenant-specific visibility? **Concepts:** indexing
  lag, deletion, authorization. **Patterns:** [search index](pattern-library.md#search-index),
  [ownership boundary](pattern-library.md#ownership-boundary).

<a id="q31"></a>
31. **Q31 — Streaming: ingest and play.** How do you serve a large media object
  without loading it into an application heap? **Concepts:** range reads,
  object storage, edge delivery. **Patterns:**
  [streaming delivery](pattern-library.md#streaming-delivery),
  [object storage](pattern-library.md#object-storage).

<a id="q32"></a>
32. **Q32 — Streaming: adaptive delivery.** Which signals should change
  bitrate, and how do buffering and regional failures affect playback?
  **Concepts:** throughput, manifests, edge cache. **Patterns:**
  [streaming delivery](pattern-library.md#streaming-delivery),
  [resilience](pattern-library.md#resilience).

<a id="q33"></a>
33. **Q33 — Analytics: event to metric.** How do you define a metric that can
  be recomputed from its source events? **Concepts:** event time, schema,
  aggregation. **Patterns:** [analytics pipeline](pattern-library.md#analytics-pipeline),
  [event pipeline](pattern-library.md#event-pipeline).

<a id="q34"></a>
34. **Q34 — Analytics: late and duplicate data.** How should a report change
  when events arrive late or are replayed? **Concepts:** idempotent aggregate,
  watermark, correction. **Patterns:** [analytics pipeline](pattern-library.md#analytics-pipeline),
  [idempotency](pattern-library.md#idempotency).

<a id="q35"></a>
35. **Q35 — Distributed rate limiting.** What are the consistency and
  availability consequences of enforcing a limit across several regions?
  **Concepts:** partition, approximate counters, fairness. **Patterns:**
  [rate limiting](pattern-library.md#rate-limiting),
  [multi-region boundary](pattern-library.md#multi-region-boundary).

<a id="q36"></a>
36. **Q36 — Object-store recovery.** How would you detect an incomplete upload
  or an accidental deletion and restore a verified object? **Concepts:**
  checksums, versioning, retention. **Patterns:**
  [object storage](pattern-library.md#object-storage),
  [resilience](pattern-library.md#resilience).

## Later AI design prompts

AI work stays outside this 36-question interview bank until the relevant data,
authorization, and evaluation foundations pass their existing gates. The
future prompt set remains in the roadmap; these systems questions do not
authorize an AI feature or activate StreamForge.
