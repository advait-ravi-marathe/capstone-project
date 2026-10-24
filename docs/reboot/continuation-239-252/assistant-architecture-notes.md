# Assistant-supplied architecture notes

The assistant selects and explains one bounded local architecture; the learner implements and tests it. No capstone design proposals or post-hoc justifications are assigned. System-design practice remains in the separate interview question bank.

<a id="day-239"></a>
## Day 239 — Establish synthetic evaluation baseline v1

**Architecture:** Assistant-supplied choice: one deterministic HomeSecure explanation workflow with authorization filtering before a local fake-provider adapter; the provider receives only owner-approved synthetic excerpts and cannot mutate policy or trigger action. The response gate accepts exact in-scope citations and exact policy text, otherwise abstains. Retries are at most one for timeout/unavailable only; malformed responses fail closed. Trace records carry an opaque stable ID and allowlisted counts/status/latency, never owner, question, source text or answer. Expected labels are fixed independently from observed outputs. All examples use synthetic customer cust-synth-01 / owner synthetic-owner-a. Both prior evidence gates must be genuinely met before learner workflow acceptance; if blocked, use the named recovery queue and do not treat scaffold or CI as learner completion.

<a id="day-240"></a>
## Day 240 — Verify exact citation and principled abstention

**Architecture:** Assistant-supplied choice: one deterministic HomeSecure explanation workflow with authorization filtering before a local fake-provider adapter; the provider receives only owner-approved synthetic excerpts and cannot mutate policy or trigger action. The response gate accepts exact in-scope citations and exact policy text, otherwise abstains. Retries are at most one for timeout/unavailable only; malformed responses fail closed. Trace records carry an opaque stable ID and allowlisted counts/status/latency, never owner, question, source text or answer. Expected labels are fixed independently from observed outputs. All examples use synthetic customer cust-synth-01 / owner synthetic-owner-a. Both prior evidence gates must be genuinely met before learner workflow acceptance; if blocked, use the named recovery queue and do not treat scaffold or CI as learner completion.

<a id="day-241"></a>
## Day 241 — Prove retrieval owner isolation

**Architecture:** Assistant-supplied choice: one deterministic HomeSecure explanation workflow with authorization filtering before a local fake-provider adapter; the provider receives only owner-approved synthetic excerpts and cannot mutate policy or trigger action. The response gate accepts exact in-scope citations and exact policy text, otherwise abstains. Retries are at most one for timeout/unavailable only; malformed responses fail closed. Trace records carry an opaque stable ID and allowlisted counts/status/latency, never owner, question, source text or answer. Expected labels are fixed independently from observed outputs. All examples use synthetic customer cust-synth-01 / owner synthetic-owner-a. Both prior evidence gates must be genuinely met before learner workflow acceptance; if blocked, use the named recovery queue and do not treat scaffold or CI as learner completion.

<a id="day-242"></a>
## Day 242 — Treat retrieved instructions as untrusted data

**Architecture:** Assistant-supplied choice: one deterministic HomeSecure explanation workflow with authorization filtering before a local fake-provider adapter; the provider receives only owner-approved synthetic excerpts and cannot mutate policy or trigger action. The response gate accepts exact in-scope citations and exact policy text, otherwise abstains. Retries are at most one for timeout/unavailable only; malformed responses fail closed. Trace records carry an opaque stable ID and allowlisted counts/status/latency, never owner, question, source text or answer. Expected labels are fixed independently from observed outputs. All examples use synthetic customer cust-synth-01 / owner synthetic-owner-a. Both prior evidence gates must be genuinely met before learner workflow acceptance; if blocked, use the named recovery queue and do not treat scaffold or CI as learner completion.

<a id="day-243"></a>
## Day 243 — Compare deterministic baseline with scripted fake outputs

**Architecture:** Assistant-supplied choice: one deterministic HomeSecure explanation workflow with authorization filtering before a local fake-provider adapter; the provider receives only owner-approved synthetic excerpts and cannot mutate policy or trigger action. The response gate accepts exact in-scope citations and exact policy text, otherwise abstains. Retries are at most one for timeout/unavailable only; malformed responses fail closed. Trace records carry an opaque stable ID and allowlisted counts/status/latency, never owner, question, source text or answer. Expected labels are fixed independently from observed outputs. All examples use synthetic customer cust-synth-01 / owner synthetic-owner-a. Both prior evidence gates must be genuinely met before learner workflow acceptance; if blocked, use the named recovery queue and do not treat scaffold or CI as learner completion.

<a id="day-244"></a>
## Day 244 — Measure bounded response latency

**Architecture:** Assistant-supplied choice: one deterministic HomeSecure explanation workflow with authorization filtering before a local fake-provider adapter; the provider receives only owner-approved synthetic excerpts and cannot mutate policy or trigger action. The response gate accepts exact in-scope citations and exact policy text, otherwise abstains. Retries are at most one for timeout/unavailable only; malformed responses fail closed. Trace records carry an opaque stable ID and allowlisted counts/status/latency, never owner, question, source text or answer. Expected labels are fixed independently from observed outputs. All examples use synthetic customer cust-synth-01 / owner synthetic-owner-a. Both prior evidence gates must be genuinely met before learner workflow acceptance; if blocked, use the named recovery queue and do not treat scaffold or CI as learner completion.

<a id="day-245"></a>
## Day 245 — Define local adapter request and response contract

**Architecture:** Assistant-supplied choice: one deterministic HomeSecure explanation workflow with authorization filtering before a local fake-provider adapter; the provider receives only owner-approved synthetic excerpts and cannot mutate policy or trigger action. The response gate accepts exact in-scope citations and exact policy text, otherwise abstains. Retries are at most one for timeout/unavailable only; malformed responses fail closed. Trace records carry an opaque stable ID and allowlisted counts/status/latency, never owner, question, source text or answer. Expected labels are fixed independently from observed outputs. All examples use synthetic customer cust-synth-01 / owner synthetic-owner-a. Both prior evidence gates must be genuinely met before learner workflow acceptance; if blocked, use the named recovery queue and do not treat scaffold or CI as learner completion.

<a id="day-246"></a>
## Day 246 — Bound transient retries and stop on malformed output

**Architecture:** Assistant-supplied choice: one deterministic HomeSecure explanation workflow with authorization filtering before a local fake-provider adapter; the provider receives only owner-approved synthetic excerpts and cannot mutate policy or trigger action. The response gate accepts exact in-scope citations and exact policy text, otherwise abstains. Retries are at most one for timeout/unavailable only; malformed responses fail closed. Trace records carry an opaque stable ID and allowlisted counts/status/latency, never owner, question, source text or answer. Expected labels are fixed independently from observed outputs. All examples use synthetic customer cust-synth-01 / owner synthetic-owner-a. Both prior evidence gates must be genuinely met before learner workflow acceptance; if blocked, use the named recovery queue and do not treat scaffold or CI as learner completion.

<a id="day-247"></a>
## Day 247 — Correlate traces while redacting content

**Architecture:** Assistant-supplied choice: one deterministic HomeSecure explanation workflow with authorization filtering before a local fake-provider adapter; the provider receives only owner-approved synthetic excerpts and cannot mutate policy or trigger action. The response gate accepts exact in-scope citations and exact policy text, otherwise abstains. Retries are at most one for timeout/unavailable only; malformed responses fail closed. Trace records carry an opaque stable ID and allowlisted counts/status/latency, never owner, question, source text or answer. Expected labels are fixed independently from observed outputs. All examples use synthetic customer cust-synth-01 / owner synthetic-owner-a. Both prior evidence gates must be genuinely met before learner workflow acceptance; if blocked, use the named recovery queue and do not treat scaffold or CI as learner completion.

<a id="day-248"></a>
## Day 248 — Estimate tokens and synthetic call cost

**Architecture:** Assistant-supplied choice: one deterministic HomeSecure explanation workflow with authorization filtering before a local fake-provider adapter; the provider receives only owner-approved synthetic excerpts and cannot mutate policy or trigger action. The response gate accepts exact in-scope citations and exact policy text, otherwise abstains. Retries are at most one for timeout/unavailable only; malformed responses fail closed. Trace records carry an opaque stable ID and allowlisted counts/status/latency, never owner, question, source text or answer. Expected labels are fixed independently from observed outputs. All examples use synthetic customer cust-synth-01 / owner synthetic-owner-a. Both prior evidence gates must be genuinely met before learner workflow acceptance; if blocked, use the named recovery queue and do not treat scaffold or CI as learner completion.

<a id="day-249"></a>
## Day 249 — Aggregate versioned metrics by risk slice

**Architecture:** Assistant-supplied choice: one deterministic HomeSecure explanation workflow with authorization filtering before a local fake-provider adapter; the provider receives only owner-approved synthetic excerpts and cannot mutate policy or trigger action. The response gate accepts exact in-scope citations and exact policy text, otherwise abstains. Retries are at most one for timeout/unavailable only; malformed responses fail closed. Trace records carry an opaque stable ID and allowlisted counts/status/latency, never owner, question, source text or answer. Expected labels are fixed independently from observed outputs. All examples use synthetic customer cust-synth-01 / owner synthetic-owner-a. Both prior evidence gates must be genuinely met before learner workflow acceptance; if blocked, use the named recovery queue and do not treat scaffold or CI as learner completion.

<a id="day-250"></a>
## Day 250 — Challenge evaluation with counterfactuals and mutations

**Architecture:** Assistant-supplied choice: one deterministic HomeSecure explanation workflow with authorization filtering before a local fake-provider adapter; the provider receives only owner-approved synthetic excerpts and cannot mutate policy or trigger action. The response gate accepts exact in-scope citations and exact policy text, otherwise abstains. Retries are at most one for timeout/unavailable only; malformed responses fail closed. Trace records carry an opaque stable ID and allowlisted counts/status/latency, never owner, question, source text or answer. Expected labels are fixed independently from observed outputs. All examples use synthetic customer cust-synth-01 / owner synthetic-owner-a. Both prior evidence gates must be genuinely met before learner workflow acceptance; if blocked, use the named recovery queue and do not treat scaffold or CI as learner completion.

<a id="day-251"></a>
## Day 251 — Reproduce paired result in a fresh process

**Architecture:** Assistant-supplied choice: one deterministic HomeSecure explanation workflow with authorization filtering before a local fake-provider adapter; the provider receives only owner-approved synthetic excerpts and cannot mutate policy or trigger action. The response gate accepts exact in-scope citations and exact policy text, otherwise abstains. Retries are at most one for timeout/unavailable only; malformed responses fail closed. Trace records carry an opaque stable ID and allowlisted counts/status/latency, never owner, question, source text or answer. Expected labels are fixed independently from observed outputs. All examples use synthetic customer cust-synth-01 / owner synthetic-owner-a. Both prior evidence gates must be genuinely met before learner workflow acceptance; if blocked, use the named recovery queue and do not treat scaffold or CI as learner completion.

<a id="day-252"></a>
## Day 252 — Close local evaluation with blind spots and handoff

**Architecture:** Assistant-supplied choice: one deterministic HomeSecure explanation workflow with authorization filtering before a local fake-provider adapter; the provider receives only owner-approved synthetic excerpts and cannot mutate policy or trigger action. The response gate accepts exact in-scope citations and exact policy text, otherwise abstains. Retries are at most one for timeout/unavailable only; malformed responses fail closed. Trace records carry an opaque stable ID and allowlisted counts/status/latency, never owner, question, source text or answer. Expected labels are fixed independently from observed outputs. All examples use synthetic customer cust-synth-01 / owner synthetic-owner-a. Both prior evidence gates must be genuinely met before learner workflow acceptance; if blocked, use the named recovery queue and do not treat scaffold or CI as learner completion.
