# Conditional M2.3–M3.2 starter guides

**Status:** These are draft study/build plans and instructor mechanics only. They do not activate calendar days, satisfy prerequisite gates, or record learner completion. The accepted milestone outcomes remain in [`later-milestone-contracts.json`](../accepted/later-milestone-contracts.json). AWS account/service/cost approval is absent. P1 actual model/RAG evidence, M2.1 learner evidence, M2.2 learner evidence, and all later learner evidence remain unpassed. No cloud API, model API, external repository, or deployment is exercised by these local tests.

## Budget and dependency accounting

All day cards retain **60 minutes DSA + 30 minutes role/AWS bank + 90 minutes AI = 180 minutes total**. If an actual DSA review uses75/90 minutes, AI becomes75/60 and the same segments carry forward. AWS stays in the twice-per-seven-day role-bank cadence; other sessions stay interview preparation. Every seventh actual learning day uses 30 AI minutes for private visibility, counted once. These are conditional relative days, not dated completion estimates.

| Milestone | Conditional days | Substantive AI | Buffer | Visibility in AI | Explicit unallocated AI |
|---|---:|---:|---:|---:|---:|
| M2.3 | 82–102 | 1,440 min (24 h) | 300 min (5 h) | 90 min | 60 min |
| M3.1 | 103–123 | 1,500 min (25 h) | 240 min (4 h, per accepted contract) | 90 min | 60 min |
| M3.2 | 123–143 | 1,470 min (24.5 h) | 240 min (4 h) | 90 min | 90 min |

M3.2 Day123 shares the 30-minute M3.1 buffer boundary; reconcile both rows as a single 90-minute AI day. M2.3 and M3.1/M3.2 have their own nominal unallocated minutes; do not move them to another day or treat them as silent mandatory work. M3.1 buffer is 240, following the accepted four-hour contract; a previous draft proposal mistakenly said300 and is not used.

Each stage is split across its mapped days in sequence. A 150-minute stage uses30 minutes specified reading,20 worked example,80 learner work and20 evidence/error log; shorter sessions continue the same sequence. Unfinished learner work carries forward; never squeeze it into the separate DSA/role bank.

## M2.3 — Real AWS and hosted-model delivery

**Prerequisite:** M2.2 security/deployment preparation must be evidenced before learning progression. No authorization to create or use AWS resources is present. Any future live work requires a specific account, region, service/model, spend ceiling, bounded lifetime, teardown owner, and explicit acknowledgement that Budgets alerts are not a hard cap.

Study only the linked headings for each session:

| Stage | Teaching focus and learner task | Official resource section |
|---|---|---|
| S01 Authorization | Work a bounded request/token/runtime cost estimate; implement `validate_authorization`. No SDK call before scope passes. | [AWS Budgets](https://docs.aws.amazon.com/cost-management/latest/userguide/budgets-managing-costs.html): cost and usage tracking, actual/forecast alerts. [IAM best practices](https://docs.aws.amazon.com/IAM/latest/UserGuide/best-practices.html): temporary credentials, least privilege, policy validation. |
| S02 Deployment spec | Walk one container task/role/network diagram; validate bounded task count, TTL and secret-free config. | [ECS on Fargate](https://docs.aws.amazon.com/AmazonECS/latest/developerguide/AWS_Fargate.html): task definitions, networking, CPU/memory, platform version. [IAM best practices](https://docs.aws.amazon.com/IAM/latest/UserGuide/best-practices.html): temporary credentials and least privilege. |
| S03 Plan/read-only diff | Compare desired and fixture-observed inventories; produce a redacted plan without mutation. | [Docker build best practices](https://docs.docker.com/build/building/best-practices/): multi-stage builds, `.dockerignore`, pinned base images, non-root user. [Fargate](https://docs.aws.amazon.com/AmazonECS/latest/developerguide/AWS_Fargate.html): task definitions/networking. |
| S04 Live readback | Read a sample deployment receipt and distinguish desired configuration from actual resource readback. Live implementation is gated. | [Fargate](https://docs.aws.amazon.com/AmazonECS/latest/developerguide/AWS_Fargate.html) and [IAM best practices](https://docs.aws.amazon.com/IAM/latest/UserGuide/best-practices.html): selected headings above. |
| S05 Model adapter | Inspect provider request/response shapes; learner implements a bounded request/parser only after a model is explicitly chosen. The instructor scaffold now supplies an injectable Bedrock Converse adapter and offline request-builder fixtures; these do not make a request. | [Boto3 Bedrock Runtime `converse`](https://docs.aws.amazon.com/boto3/latest/reference/services/bedrock-runtime/client/converse.html): Request/Response Syntax, inference configuration and usage. [Bedrock inference prerequisites](https://docs.aws.amazon.com/bedrock/latest/userguide/inference-prereq.html): invocation permissions and action/resource restrictions. |
| S06 Telemetry | Trace request→retrieval→provider boundaries; record usage only when observed; redact content. | [OpenTelemetry Python](https://opentelemetry.io/docs/languages/python/instrumentation/): tracing, spans, attributes, exceptions. [CloudWatch alarms](https://docs.aws.amazon.com/AmazonCloudWatch/latest/monitoring/CloudWatch_Alarms.html): metric alarms, missing data and alarm state/actions. |
| S07 Bounded failures | Work a transient/permanent error timeline; implement a bounded policy and safe injected failure. No quota exhaustion. | [Boto3 Bedrock `invoke_model`](https://docs.aws.amazon.com/boto3/latest/reference/services/bedrock-runtime/client/invoke_model.html): Exceptions. [CloudWatch alarms](https://docs.aws.amazon.com/AmazonCloudWatch/latest/monitoring/CloudWatch_Alarms.html): alarm states and missing data. |
| S08 Rollback | Trace version A→B→A and check persistent schema compatibility before rollback. | [Fargate](https://docs.aws.amazon.com/AmazonECS/latest/developerguide/AWS_Fargate.html): task definitions, networking and task sizing. [Docker build best practices](https://docs.docker.com/build/building/best-practices/): pinned images/non-root. |
| S09 Usage/teardown | Reconcile estimated versus observed usage; keep unavailable billing fields null; inventory intended ephemeral resources. | [AWS Budgets](https://docs.aws.amazon.com/cost-management/latest/userguide/budgets-managing-costs.html): actual/forecast alerts. [Fargate](https://docs.aws.amazon.com/AmazonECS/latest/developerguide/AWS_Fargate.html): task lifecycle and resources. |
| S10 Evidence gate | Review cloud/model/operations separately. A plan, local container or test fixture cannot pass a live gate. | [Well-Architected Framework](https://docs.aws.amazon.com/wellarchitected/latest/framework/welcome.html): introduction and only the selected pillar; [AWS Budgets](https://docs.aws.amazon.com/cost-management/latest/userguide/budgets-managing-costs.html): alerts. |

**Code and commands:** ai/fde_v2/m23/learner.py contains unsolved learner seams; reference.py is an instructor-only fixture oracle. providers.py contains pure Bedrock Converse and ECS request builders plus injected-client adapters for bounded deployment, readback, rollback and staged service teardown. Service creation and update use distinct SDK request builders; teardown first requests desired count zero, then requires a separate exact approval and readback showing no running tasks before service deletion. Adapters construct no SDK clients and discover no credentials. execution_enabled defaults false. A provider call requires a one-use server-side token whose vault-owned expiry is bound to the exact SDK method/kwargs, operation, provider/service, account, region, model/spend scope and request; the vault receives a trusted approval event from its integrating application. The injected provider client and STS reader must expose the same underlying SDK credential object; unverifiable bindings are rejected. No approval UI or human approval is supplied here. Offline tests use strict-signature recording SDK-shaped clients and a clearly test-only approval callback; no AWS/model call is made, and they do not establish actual SDK-version compatibility or live evidence. The existing local runtime does not include boto3/botocore, so run mechanics with python -m ai.fde_v2.m23.acceptance --milestone AIFDE-V2-M2.3 --mode mechanics and use the local AWS-shaped request tests. Actual SDK-version check, account/region/model selection, spend authorization, live readback, rollback and teardown evidence remain outstanding. The Bedrock Converse request follows its documented modelId, messages, and inferenceConfig.maxTokens shape; ECS request builders follow the documented registration/service APIs. No provider client is created here.

## M3.1 — Bounded developer workflow

**Prerequisite:** P1 actual RAG/ownership, M2.1 evaluation and M2.2 local security evidence. M2.3 live deployment is not a prerequisite to begin M3.1, but the earlier learner gates remain. The local demo intentionally exposes real scratch-file/test-log reads and review-only patch proposals. It does not execute arbitrary shell commands, apply a repository patch, or invoke a model by default. `run_workflow` can accept an explicitly supplied planner callback: it receives actual hash-linked scoped file reads, then its untrusted plan is validated and executed through the allowlist. Reads are bounded by a strict file-size limit; cancellation is checked before each read and between workflow steps. A synchronous read is not interrupted mid-operation, so no per-read timeout is claimed.

| Stage | Short teaching/example then learner TODO | Official resource sections |
|---|---|---|
| S01 Scope | Draw the allowed-root boundary; trace traversal and symlink examples; implement `resolve_allowed_path`. | [Python `pathlib`](https://docs.python.org/3.12/library/pathlib.html): `Path.resolve`, `Path.is_relative_to`, `Path.open`. |
| S02 Typed tools | Compare a typed read request with shell/unknown-tool requests; implement strict plan validation. | [MCP client](https://py.sdk.modelcontextprotocol.io/client/): first client, listing tools and calling a tool (reference only; M3.2 adds SDK transport). |
| S03 Actual reads | Read an actual changed source file and test log, capture content hashes and trace IDs. | [Python `pathlib`](https://docs.python.org/3.12/library/pathlib.html): `Path.open` and resolved paths. |
| S04 Resource limits | Work size limits, deadlines and cancellation; no general command execution. | [Python subprocess](https://docs.python.org/3.12/library/subprocess.html): `run`, `timeout`, `returncode` (study/comprehension only; not an exposed terminal tool). |
| S05 Planning | Validate a bounded model-shaped plan without trusting model output; compare to deterministic triage. Real planner quality remains model-gated. | [MCP tools](https://modelcontextprotocol.io/docs/2026-07-28/develop/build-server): tool schemas and bounded handlers. |
| S06 Patch proposal | Inspect a unified diff, source digest, size bound and `applied=false` receipt. | [Python `pathlib`](https://docs.python.org/3.12/library/pathlib.html): resolved paths/files. |
| S07 Cancellation/failures | Trace canceled, denied, tool error and test failure distinctly; late results cannot resume a canceled run. | [Python asyncio subprocesses](https://docs.python.org/3.12/library/asyncio-subprocess.html): process/wait/communicate/cancellation. |
| S08 Actual tool trace | Inspect the local read trace and source hash; absent planning-capable model is `BLOCKED`, not a canned-agent pass. | [OpenTelemetry Python](https://opentelemetry.io/docs/languages/python/instrumentation/): traces, spans, attributes and exceptions. |
| S09 Baseline comparison | Compare deterministic outcome and agent plan outcomes without dropping failures; use simpler path when adequate. | [scikit-learn confusion matrix](https://scikit-learn.org/stable/modules/generated/sklearn.metrics.confusion_matrix.html): explicit labels and parameters (concept only; no new dependency). |
| S10 Evidence | Assemble actual read/tool trace, scope denial, cancellation result and learner explanation; never claim external write. | [Python `pathlib`](https://docs.python.org/3.12/library/pathlib.html): path resolution; [OpenTelemetry Python](https://opentelemetry.io/docs/languages/python/instrumentation/): trace boundaries. |

Run mechanics with `python -m ai.fde_v2.m31.acceptance --milestone AIFDE-V2-M3.1 --mode mechanics`; `--mode integration` runs actual scratch-file observations and a generated review-only patch through the assistant-supplied local workflow adapter and an explicit local planner fixture. This does not call a model or satisfy planning-quality/learner prerequisites. The default application workflow is runnable without learner FE/BE code; `M31_CANDIDATE=ai.fde_v2.m31.learner ... --mode learner` selects the learner reasoning TODOs separately and should remain red until attempted. Evidence artifacts can be supplied as described in the shared receipt procedure; independent review and actual learner runs remain required.

## M3.2 — Genuine MCP and exact-action approval

The current official MCP Python tutorial requires SDK2.0.0 or later; the executable was verified against official SDK2.3.0. `requirements-fde-v2-mcp.txt` pins that SDK. The version check used [official MCP server docs](https://modelcontextprotocol.io/docs/2026-07-28/develop/build-server), [official client docs](https://modelcontextprotocol.io/docs/2026-07-28/develop/build-client), and the [official Python SDK client API](https://py.sdk.modelcontextprotocol.io/client/). Current tutorial examples use `MCPServer` and `Client`; the older 1.29 runtime API was not used for this release. SDK2's direct `ctx.elicit` works only for legacy protocol versions; the starter uses the modern resolver/input-required path described in [official elicitation docs](https://py.sdk.modelcontextprotocol.io/handlers/elicitation/).

| Stage | Teaching and learner task | Official resource sections |
|---|---|---|
| S01 Compatibility | Read installed package/version and negotiated protocol; distinguish source docs from runtime negotiation. | [MCP server](https://modelcontextprotocol.io/docs/2026-07-28/develop/build-server): requirements/core concepts. [MCP client](https://modelcontextprotocol.io/docs/2026-07-28/develop/build-client): requirements and connection lifecycle. |
| S02 Server/schemas | Inspect the stdio server, schemas and stdout/stderr discipline; no arbitrary path/network tool. | [Build an MCP server](https://modelcontextprotocol.io/docs/2026-07-28/develop/build-server): core concepts, Python server and testing. |
| S03 Client/session | Use actual SDK client over stdio; read changed scratch content through a registered tool. | [Build an MCP client](https://modelcontextprotocol.io/docs/2026-07-28/develop/build-client): connection initialization and tool invocation. |
| S04 Exact digest | Calculate action digest from operation, output target, content hash, current source hash and scratch-scope hash. | [Python `pathlib`](https://docs.python.org/3.12/library/pathlib.html): resolve and constrain paths. |
| S05 Approval boundary | Understand that MCP client elicitation can be mediated by an agent; protocol response alone does not prove human approval. Production writes remain blocked without a trusted human UI. | [MCP elicitation](https://py.sdk.modelcontextprotocol.io/handlers/elicitation/): resolver mode, current client response, decline/cancel. |
| S06 One-use ledger | Work expiry, stale-source invalidation, changed-arguments and replay cases against process-local state. | [MCP elicitation](https://py.sdk.modelcontextprotocol.io/handlers/elicitation/): outcomes and request state; [Python threading](https://docs.python.org/3.12/library/threading.html): lock (for the small in-memory witness). |
| S07 Controlled write | A test-only client writes one new patch artifact under an owned temporary scratch root; no overwrite/apply/external target. | [MCP server](https://modelcontextprotocol.io/docs/2026-07-28/develop/build-server): tool handlers; [Python `pathlib`](https://docs.python.org/3.12/library/pathlib.html): path resolution. |
| S08 Error/audit | Record redacted result IDs; do not retry an uncertain mutation; distinguish denied/canceled/error. | [MCP client](https://modelcontextprotocol.io/docs/2026-07-28/develop/build-client): failures/timeouts; [OpenTelemetry Python](https://opentelemetry.io/docs/languages/python/instrumentation/): trace/exception handling. |
| S09 Adversarial boundary | Try traversal, symlinks, forged/expired/replayed tokens, changed source and fake tool approval. | [MCP server](https://modelcontextprotocol.io/docs/2026-07-28/develop/build-server): tool security and testing. |
| S10 Proof | Record actual initialize/list/read/elicitation/write/replay-denial exchange and state stdio/local limits. | [MCP client](https://modelcontextprotocol.io/docs/2026-07-28/develop/build-client), [server](https://modelcontextprotocol.io/docs/2026-07-28/develop/build-server), and [elicitation](https://py.sdk.modelcontextprotocol.io/handlers/elicitation/). |

Install the pinned official package in a project virtual environment before running M3.2 commands:

```sh
python -m pip install -r requirements-fde-v2-mcp.txt
python -m ai.fde_v2.m32.acceptance --milestone AIFDE-V2-M3.2 --mode mechanics
python -m ai.fde_v2.m32.acceptance --milestone AIFDE-V2-M3.2 --mode integration
```

The integration command launches a genuine SDK stdio client/server exchange in a temporary directory and uses an explicit **test fixture** elicitation callback. The resulting receipt states `human_approval_evidenced=false`; this exercises protocol wiring, not a real user's approval. The ordinary server defaults to the assistant-supplied bounded application adapter, has test approval disabled, offers no network tools, and will not write without the controlled test-only approval path. The actual human approval flow, M3.1 prerequisite, and learner evidence are therefore still blocked. The separate learner adapter remains an exercise seam, not required application plumbing. Local stdio does not imply remote OAuth or external-write authority.

## Local commands and status

Instructor mechanics (safe synthetic/local tests):

```sh
python -m ai.fde_v2.m23.acceptance --milestone AIFDE-V2-M2.3 --mode mechanics
python -m ai.fde_v2.m31.acceptance --milestone AIFDE-V2-M3.1 --mode mechanics
python -m ai.fde_v2.m32.acceptance --milestone AIFDE-V2-M3.2 --mode mechanics
python -m ai.fde_v2.m22.acceptance --milestone AIFDE-V2-M2.2 --mode mechanics
```

`mechanics` passing means the local reference contract and its regressions passed. It does not mean a learner seam, model, cloud resource, approval UI, deployment, certificate, or milestone passed. Default learner suites are expected to fail while the TODOs remain. M2.2 `integration` runs real local SQLite and embedded-Qdrant/FastAPI routes using signed local fixtures and the supplied application adapter; it is not a live identity provider or learner evidence. M2.3 live cloud/model evidence remains blocked pending explicit permission. M3.1 evidence requires actual learner-run receipts and its prerequisites. M3.2 protocol mechanics pass separately from trusted-human approval and learner gates.

Evidence modes accept `--receipt PATH` for the shared [hash-verified artifact manifest](../evidence-receipts.md). A verified manifest is only ready for independent review; it does not self-assert learner, provider, human or cloud completion.
