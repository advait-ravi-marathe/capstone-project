# System design pattern library

Use this reference after saving your own design. A pattern is a candidate, not
a default solution. Before adopting one, state its assumptions, compare a
simpler alternative, name a likely failure, and identify the observation that
would justify its cost. Question links use stable anchors in the
[question bank](question-bank.md).

<a id="requirement-slice"></a>
## Requirement slice

**Use when:** the prompt mixes a user outcome with vague scale or reliability
claims. Write core operations, must-not-happen cases, traffic shape, and the
latency or durability target before choosing components. **Alternative:** a
small table of use cases and invariants. **Trade-off:** a short, explicit
scope takes interview time but prevents solving imagined requirements.
**Failure mode:** treating an unstated million-user scale as a requirement.
**Questions:** [Q01](question-bank.md#q01), [Q05](question-bank.md#q05),
[Q07](question-bank.md#q07).

<a id="http-boundary"></a>
## HTTP boundary

**Use when:** clients need stable routes, validation, status codes, and error
meaning. Keep transport translation at the controller or gateway and domain
decisions with the owning service. **Alternative:** a single internal API for
a local slice. **Trade-off:** gateway centralization simplifies shared policy
but can couple releases or hide the actual failing service. **Failure mode:**
the edge converts every failure to `200` or leaks exception details.
**Questions:** [Q07](question-bank.md#q07), [Q08](question-bank.md#q08).

<a id="short-key-allocation"></a>
## Short-key allocation

**Use when:** a short URL must map to one durable destination. Choose random
keys with collision detection, or allocate an encoded unique ID when
predictability is acceptable. **Trade-off:** random keys resist enumeration
but need collision retries; sequential IDs are compact but reveal volume and
are guessable. **Failure mode:** a collision overwrites another owner's link
or an attacker enumerates private links. **Questions:** [Q01](question-bank.md#q01),
[Q02](question-bank.md#q02).

<a id="expiry-and-retention"></a>
## Expiry and retention

**Use when:** records expire or links have a lifetime. Store the declared
expiry and evaluate it against an injected clock; use background cleanup only
for storage reclamation. **Alternative:** TTL support in a data store.
**Trade-off:** logical expiry is deterministic but expired bytes remain until
cleanup; eager cleanup adds work and race conditions. **Failure mode:** using
wall-clock sleeps in tests or deleting a record before its expiry contract.
**Questions:** [Q03](question-bank.md#q03), [Q11](question-bank.md#q11).

<a id="deterministic-time"></a>
## Deterministic time

**Use when:** correctness depends on expiry, windows, leases, or scheduled
work. Pass a clock or timestamp through the boundary and test just-before,
equal-to, and just-after cases. **Alternative:** a fake scheduler for
time-driven execution. **Trade-off:** explicit time improves repeatability but
requires consistent ownership of timestamps. **Failure mode:** mixing local
time zones, database time, and application time without a contract.
**Questions:** [Q03](question-bank.md#q03), [Q04](question-bank.md#q04),
[Q11](question-bank.md#q11).

<a id="rate-limiting"></a>
## Rate limiting

**Use when:** an actor can consume a bounded resource. Fixed windows are
simple; sliding windows reduce boundary bursts; token buckets permit bounded
bursts. **Trade-off:** per-process counters are cheap but inconsistent across
instances; a shared atomic store improves coordination but adds latency and a
dependency. **Failure mode:** trusting a spoofable client address or rejecting
all work when the counter store is down without a deliberate policy.
**Questions:** [Q04](question-bank.md#q04), [Q02](question-bank.md#q02),
[Q35](question-bank.md#q35).

<a id="gateway-boundary"></a>
## Gateway boundary

**Use when:** several services share routing, authentication, request limits,
or protocol translation. Keep resource ownership and domain authorization in
the service that owns the data. **Alternative:** a modular monolith with
controller-level boundaries. **Trade-off:** a gateway centralizes policy but
can become a bottleneck or single point of failure. **Failure mode:** an edge
check is mistaken for row-level owner authorization. **Questions:**
[Q07](question-bank.md#q07), [Q08](question-bank.md#q08),
[Q12](question-bank.md#q12).

<a id="ownership-boundary"></a>
## Ownership boundary

**Use when:** data is private to a user, tenant, or account. Derive identity
from authenticated context and include it in the database predicate or
constraint. **Alternative:** a separate database per tenant when isolation
requirements justify it. **Trade-off:** shared tables are efficient but demand
consistent scoped access; separate databases isolate but raise operational
cost. **Failure mode:** a client-supplied owner widens access, or a cache key
omits the owner. **Questions:** [Q05](question-bank.md#q05),
[Q12](question-bank.md#q12), [Q18](question-bank.md#q18),
[Q30](question-bank.md#q30).

<a id="domain-workflow"></a>
## Domain workflow

**Use when:** a business operation passes through named states such as quote,
selection, and issue. Make allowed transitions explicit and preserve the
version of the source decision. **Alternative:** a simple CRUD update for
non-consequential data. **Trade-off:** a state machine is auditable but adds
explicit transition code. **Failure mode:** a later update silently changes
what an earlier accepted action meant. **Questions:** [Q05](question-bank.md#q05),
[Q13](question-bank.md#q13), [Q14](question-bank.md#q14).

<a id="immutable-snapshot"></a>
## Immutable snapshot

**Use when:** later catalog changes must not rewrite an accepted price,
version, or user decision. Store the relevant facts with the accepted record.
**Alternative:** resolve mutable references on every read. **Trade-off:**
snapshots preserve history but duplicate data and need a versioning plan.
**Failure mode:** copying an ID while reading the latest mutable price.
**Questions:** [Q03](question-bank.md#q03), [Q05](question-bank.md#q05),
[Q19](question-bank.md#q19).

<a id="transaction-boundary"></a>
## Transaction boundary

**Use when:** multiple local database writes represent one business result.
Commit them together and roll back on a failed dependent write. **Alternative:**
an asynchronous workflow with explicit intermediate states for long-running
work. **Trade-off:** one transaction gives atomicity but holds locks and must
remain short. **Failure mode:** committing the aggregate while losing its
receipt or event. **Questions:** [Q13](question-bank.md#q13),
[Q14](question-bank.md#q14), [Q19](question-bank.md#q19).

<a id="data-store-choice"></a>
## Relational SQL versus document storage

**Use when:** a design prompt asks whether related records should live in
normalized tables or flexible documents. Start with relationships, constraints,
transaction boundaries, query shapes, and evolution needs. Relational SQL fits
strong references, joins, and constraints; a document model can fit naturally
aggregate-shaped data with fields that evolve together. **Alternative:** use
PostgreSQL relational tables with a bounded JSON column for a genuinely flexible
subdocument; PostgreSQL supports querying JSON alongside relational data.
**Trade-off:** document flexibility can move validation and relationship work
into application code; relational constraints require schema evolution.
**Failure mode:** choosing NoSQL because a product is “large” without naming
access patterns, or storing independently updated facts in one opaque document.
See [PostgreSQL JSON types](https://www.postgresql.org/docs/current/datatype-json.html).
**Questions:** [Q06](question-bank.md#q06), [Q13](question-bank.md#q13),
[Q23](question-bank.md#q23).

<a id="load-balancing"></a>
## Load balancing

**Use when:** multiple healthy application targets must serve the same route
and one process cannot meet the measured capacity or availability target.
Distribute traffic only after defining health checks, session/state ownership,
timeouts, and safe behavior for an unavailable target. **Alternative:** one
process behind a simple reverse proxy until measurement or availability needs
justify more targets. **Trade-off:** distribution improves utilization and can
allow target replacement, but adds health propagation, routing, and operational
failure modes. **Failure mode:** marking a target healthy while its database
dependency is unusable, or relying on sticky sessions for state that should be
durable. See [AWS Elastic Load Balancing overview](https://docs.aws.amazon.com/elasticloadbalancing/latest/application/introduction.html).
**Questions:** [Q07](question-bank.md#q07), [Q35](question-bank.md#q35).

<a id="frontend-state-machine"></a>
## Frontend state machine

**Use when:** a screen has distinct loading, ready, empty, pending, failed,
expired, or retryable states. Name valid states and events, and derive visible
controls from the current state instead of maintaining contradictory booleans.
**Alternative:** a small discriminated union and local reducer; a separate
state-machine library is unnecessary for a bounded form or panel.
**Trade-off:** explicit transitions make edge cases testable but require the
team to define pending, cancellation, and stale-response behavior.
**Failure mode:** a delayed old-owner response repopulates a cleared screen, or
two booleans render an impossible combination. See React's guidance on
[reacting to input with state](https://react.dev/learn/reacting-to-input-with-state)
and [extracting state logic into a reducer](https://react.dev/learn/extracting-state-logic-into-a-reducer).
**Questions:** [Q14](question-bank.md#q14), [Q18](question-bank.md#q18),
[Q19](question-bank.md#q19), [Q21](question-bank.md#q21).

<a id="concurrency-guards"></a>
## Optimistic and unique guards

**Use when:** concurrent requests must not overwrite a newer version or claim
the same scarce resource. Use a version condition, unique constraint, or
conditional update at the durable boundary. **Alternative:** pessimistic row
locking for short, contended critical sections. **Trade-off:** optimistic
checks avoid long locks but can reject/retry; locks serialize work but can
reduce throughput. **Failure mode:** checking in application memory and then
writing without an atomic database guard. **Questions:** [Q13](question-bank.md#q13),
[Q17](question-bank.md#q17), [Q18](question-bank.md#q18).

<a id="idempotency"></a>
## Idempotency

**Use when:** a client may retry after losing a response. Bind the key to the
actor and request intent, then return the original result for an exact replay
and a conflict for changed intent. **Alternative:** expose a status/readback
operation when no write retry is needed. **Trade-off:** receipts add storage
and retention decisions but prevent duplicate effects. **Failure mode:**
reusing a key globally across owners or changing its meaning between requests.
**Questions:** [Q10](question-bank.md#q10), [Q14](question-bank.md#q14),
[Q20](question-bank.md#q20), [Q21](question-bank.md#q21).

<a id="outbox-and-messaging"></a>
## Outbox and messaging

**Use when:** a database change must eventually cause a brokered event. Write
the event to an outbox in the same transaction, then publish and mark it
delivered with an idempotent consumer. **Alternative:** direct publish after
commit for a deliberately best-effort notification. **Trade-off:** outbox
recovery is reliable but needs polling/CDC, retention, and duplicate handling.
**Failure mode:** the process crashes between database commit and broker send.
**Questions:** [Q09](question-bank.md#q09), [Q15](question-bank.md#q15),
[Q16](question-bank.md#q16), [Q23](question-bank.md#q23).

<a id="timeouts-and-retries"></a>
## Timeouts and retries

**Use when:** a downstream call may hang or fail transiently. Bound the wait,
classify safe retryable failures, and preserve the same request identity.
**Alternative:** return a pending operation and reconcile asynchronously.
**Trade-off:** retries improve recovery but increase load and can repeat effects.
**Failure mode:** a client timeout is treated as proof the database rolled
back. **Questions:** [Q08](question-bank.md#q08), [Q10](question-bank.md#q10),
[Q20](question-bank.md#q20), [Q22](question-bank.md#q22).

<a id="backpressure"></a>
## Backpressure

**Use when:** producers can outpace consumers. Bound queues, reduce intake,
and expose lag rather than allowing memory growth without limit. **Alternative:**
scale consumers when capacity is available. **Trade-off:** admission control
can reject or delay work but protects service health. **Failure mode:** an
unbounded queue turns a short overload into a long outage. **Questions:**
[Q15](question-bank.md#q15), [Q22](question-bank.md#q22),
[Q23](question-bank.md#q23), [Q26](question-bank.md#q26).

<a id="scheduled-work"></a>
## Scheduled work

**Use when:** work must run near a specified time or be retried after worker
failure. Persist due time and lease/claim state; make handlers idempotent.
**Alternative:** a managed scheduler for simple single-owner jobs. **Trade-off:**
database-backed scheduling provides recovery at the cost of polling and lease
logic. **Failure mode:** a job is claimed and the worker dies before recording
completion. **Questions:** [Q11](question-bank.md#q11),
[Q12](question-bank.md#q12).

<a id="object-storage"></a>
## Object storage

**Use when:** files are large or durable byte storage has different scaling
needs from metadata. Store an owner-scoped object reference and verify length,
checksum, and authorization before serving it. **Alternative:** database BLOBs
for small payloads needing one transactional boundary. **Trade-off:** object
stores scale bytes independently but metadata and content can diverge.
**Failure mode:** a committed row points to a partial or deleted object.
**Questions:** [Q06](question-bank.md#q06), [Q36](question-bank.md#q36).

<a id="payment-state"></a>
## Payment state

**Use when:** an external provider can return an uncertain outcome. Persist a
stable intent, use the provider's idempotency support, and reconcile status
before retrying effects. **Alternative:** synchronous capture for a simple
bounded flow. **Trade-off:** explicit pending states improve recovery but
make UI and operations more complex. **Failure mode:** issuing a second charge
because the first response was lost. **Questions:** [Q19](question-bank.md#q19),
[Q20](question-bank.md#q20).

<a id="webhook-delivery"></a>
## Webhook delivery

**Use when:** systems notify one another asynchronously. Sign payloads, include
a stable event ID, retry with bounded backoff, and provide delivery history.
**Alternative:** polling when receiver availability or inbound firewall rules
make push unsuitable. **Trade-off:** webhooks are timely but expose delivery,
signature-key rotation, and replay concerns. **Failure mode:** a receiver
processes a duplicated or forged request. **Questions:**
[Q21](question-bank.md#q21), [Q22](question-bank.md#q22).

<a id="event-pipeline"></a>
## Event pipeline

**Use when:** high-volume events feed independent consumers or rebuildable
views. Partition by the key that needs ordering and track offsets/checkpoints.
**Alternative:** direct synchronous calls for low-volume, strongly coupled
work. **Trade-off:** streams improve decoupling and replay but introduce lag,
schema compatibility, and duplicate delivery. **Failure mode:** poison events
or unbounded consumer lag. **Questions:** [Q15](question-bank.md#q15),
[Q23](question-bank.md#q23), [Q24](question-bank.md#q24),
[Q33](question-bank.md#q33).

<a id="chat-ordering"></a>
## Chat ordering

**Use when:** messages must appear in a stable per-conversation sequence.
Allocate a durable sequence or cursor and treat presence as ephemeral.
**Alternative:** timestamp sorting when clock skew and ties are acceptable.
**Trade-off:** per-conversation ordering simplifies clients but limits
parallelism for a very hot conversation. **Failure mode:** reconnecting clients
skip or duplicate messages. **Questions:** [Q25](question-bank.md#q25),
[Q26](question-bank.md#q26).

<a id="stable-pagination"></a>
## Stable pagination

**Use when:** records arrive while clients page through results. Use a
deterministic composite cursor matching a unique sort order. **Alternative:**
offset pagination for small, static datasets. **Trade-off:** cursors are more
complex but avoid duplicate/skipped rows as the set changes. **Failure mode:**
using a non-unique timestamp alone. **Questions:** [Q25](question-bank.md#q25),
[Q27](question-bank.md#q27).

<a id="feed-fan-out"></a>
## Feed fan-out

**Use when:** a feed mixes many ordinary publishers with a few high-follower
accounts. Choose fan-out-on-write, fan-out-on-read, or a hybrid based on
measured skew and freshness. **Alternative:** one database query for a small
dataset. **Trade-off:** precomputation speeds reads but amplifies writes and
creates lag. **Failure mode:** a celebrity post overwhelms per-recipient
queues. **Questions:** [Q27](question-bank.md#q27), [Q28](question-bank.md#q28).

<a id="search-index"></a>
## Search index

**Use when:** prefix/ranked retrieval cannot meet its target with a simple
indexed table query. Keep the authoritative record separate from the
rebuildable index and filter results by authorization. **Alternative:** a
database prefix index for modest data. **Trade-off:** search engines offer
ranking but add indexing lag and another failure domain. **Failure mode:**
stale or deleted private content remains searchable. **Questions:**
[Q29](question-bank.md#q29), [Q30](question-bank.md#q30).

<a id="streaming-delivery"></a>
## Streaming delivery

**Use when:** clients consume large media over variable networks. Separate
metadata, object bytes, manifests, and range/segment delivery. **Alternative:**
download a small file as one response. **Trade-off:** adaptive segments improve
playback but require encoding, manifests, and edge distribution. **Failure
mode:** a stale manifest points at unavailable segments. **Questions:**
[Q31](question-bank.md#q31), [Q32](question-bank.md#q32).

<a id="analytics-pipeline"></a>
## Analytics pipeline

**Use when:** reports aggregate large or late-arriving event sets. Preserve
source events, define event-time rules, and make aggregates rebuildable.
**Alternative:** transactional counters for small, immediate metrics.
**Trade-off:** batch/stream processing handles volume but introduces lag and
correction rules. **Failure mode:** duplicate or late events silently change
the metric. **Questions:** [Q33](question-bank.md#q33),
[Q34](question-bank.md#q34).

<a id="read-scaling"></a>
## Read scaling

**Use when:** measurement shows reads dominate and primary capacity is a real
bottleneck. Consider indexes/projections first, then replicas or partitioning.
**Alternative:** optimize a measured query or add a bounded cache. **Trade-off:**
replicas increase read capacity but can be stale; partitioning adds routing and
rebalancing work. **Failure mode:** relying on a lagging replica for a read-after-
write guarantee. **Questions:** [Q17](question-bank.md#q17),
[Q27](question-bank.md#q27), [Q35](question-bank.md#q35).

<a id="cache-policy"></a>
## Cache policy

**Use when:** a measured read is expensive and a stated staleness window is
acceptable. Choose cache-aside, explicit invalidation, or versioned keys.
**Alternative:** keep the query simple and read the source of truth.
**Trade-off:** caching cuts latency/load but adds invalidation, memory bounds,
and possible private-data exposure. **Failure mode:** stale content or a key
that omits tenant/owner. **Questions:** [Q17](question-bank.md#q17),
[Q18](question-bank.md#q18).

<a id="multi-region-boundary"></a>
## Multi-region boundary

**Use when:** the prompt has a concrete geography, latency, or outage target.
Choose a home region for strongly coordinated writes or document conflict
resolution for multi-writer data. **Alternative:** a single region with tested
recovery. **Trade-off:** multi-region improves locality/availability but adds
replication lag, conflict handling, and operational cost. **Failure mode:**
claiming globally strict rate limits without global coordination.
**Questions:** [Q35](question-bank.md#q35), [Q32](question-bank.md#q32).

<a id="resilience"></a>
## Resilience

**Use when:** a dependency or region can fail. Bound calls with timeouts,
isolate exhausted capacity with circuit breakers, and define what degrades
safely. **Alternative:** fail fast when a correct fallback is impossible.
**Trade-off:** graceful degradation preserves part of the service but can
hide data freshness limits. **Failure mode:** retries amplify an overloaded
dependency. **Questions:** [Q08](question-bank.md#q08),
[Q22](question-bank.md#q22), [Q32](question-bank.md#q32),
[Q36](question-bank.md#q36).

<a id="measure-before-scaling"></a>
## Measure before scaling

**Use when:** someone proposes caching, a queue, replicas, or sharding. Define
the target, collect a query/latency/volume observation, and change one bottleneck
at a time. **Alternative:** keep the current simple design until it misses a
measured target. **Trade-off:** measurement takes time but avoids permanent
complexity without evidence. **Failure mode:** optimizing an imagined workload
or conflating average with tail latency. **Questions:**
[Q02](question-bank.md#q02), [Q16](question-bank.md#q16),
[Q28](question-bank.md#q28).

When a pattern suggests a new feature or abstraction, check the current daily
contract before implementation. The catalog is a reference for design
reasoning; Sprint 1 only implements the bounded experiments and EAI tasks
assigned by its story.
