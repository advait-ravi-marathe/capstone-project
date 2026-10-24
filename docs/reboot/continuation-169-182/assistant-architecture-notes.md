# Assistant-supplied architecture notes: Days 169–182

These are the capstone implementation decisions. The learner implements and tests the specified seams; no capstone architecture proposal or post-hoc design justification is required. System-design practice belongs only to the separate interview question bank.

## Day 169 — derive positive pairs from validated events

Keep `positive_pairs` a pure projection over the already validated event sequence. Emit a `frozenset` of `(viewer_id, media_id)` only when `watch_seconds > 0`; event IDs and repeated watches do not increase the binary preference signal. This keeps file parsing and temporal-split responsibilities in their earlier modules and avoids interpreting an unseen item as dislike. The current A/B entry gate must be met before starting StreamForge implementation; otherwise use the implementation slice for prior-gate recovery.

## Day 170 — freeze the candidate set at the cutoff

Filter the supplied catalog using the half-open availability rule `available_from <= cutoff < available_to`, then return exact IDs in deterministic order. Do not consult training or holdout outcomes to decide eligibility. Keeping eligibility as a pure function makes cutoff boundaries testable and keeps future items and expired items out of the score map without deleting their source rows.

## Day 171 — count distinct positive viewers

Initialize every eligible candidate to zero, then count each distinct positive `(viewer, media)` pair once. Consume the Day 169 pair projection, not raw event counts or watch duration. Keep the typed `PopularityState` as the single handoff to ranking. Until the A/B gate is met, use this day's implementation slice for prior-gate recovery instead of advancing the StreamForge seam.

## Day 172 — rank with one fixed global rule

Sort by descending support and then ascending exact media ID; return up to `K=3`. Keep the ranking global and independent of viewer history, holdout outcomes, randomness, and adaptive K. The explicit tie-break makes the same typed state produce the same list on every run.

## Day 173 — preserve the complete holdout target cohort

Group distinct positive holdout media IDs by viewer, while separately retaining all observed viewer IDs and zero-positive viewers. Do not intersect targets with candidates: unreachable positives must remain visible in coverage counts so recall cannot be inflated by filtering difficult targets away.

## Day 174 — score one viewer against fixed-slot metrics

Compute hits once from the supplied recommendation IDs and positive targets. Precision remains `hits / 3`, even for a shorter list; recall is `hits / positive_target_count`. The supplied boundary rejects duplicate or overlong lists. Empty-target cases remain explicitly non-evaluable rather than receiving a fabricated score.

## Day 175 — recover prior seams only

Keep this recovery day restricted to Days 169–174. Reproduce one earlier counterexample, repair the first unresolved seam, and rerun only the mapped selectors. Do not add comparison/report/CLI work early. If the A/B gate is still unmet, its recovery remains the priority.

## Day 176 — aggregate macro metrics and coverage

Use the existing per-viewer scorer for every positive-target viewer and take unweighted macro means. Report all cohort and reachability counts alongside the metrics. Define cold start against all training interactions, including zero-watch events, so an observed zero profile is not mislabeled as a new viewer.

## Day 177 — compare fixed baselines on identical inputs

Evaluate the popularity ranking and instructor-owned ascending-ID reference with the same candidates, targets, K, and cohort. Return both results and signed deltas without selecting a winner. This prevents comparison code from silently changing the protocol to favor one baseline.

## Day 178 — separate prediction lineage from evaluation evidence

Assemble the report from supplied typed results and canonical helpers. The prediction fingerprint covers the frozen protocol and actual prediction inputs/results, while holdout metrics and full-source provenance stay outside that prediction fingerprint. Keep source lineage and limitations in the report without implying authenticity, personalization, or mastery.

## Day 179 — compose the existing bounded CLI

Route a registered fixture through `prepare_evaluation_input`, which calls the existing `prepare_dataset` exactly once, then evaluate the returned typed bundle. Preserve the supplied CLI framing: one complete JSON document only on success; diagnostics on stderr and nonzero status on failure. Do not add arbitrary paths, URL access, a second parser, or a subprocess service.

## Day 180 — use independent negative assertions

Complete the supplied assertions against literal fixture expectations. The oracle must not derive its expected values by calling the learner function under test. Keep each named defect isolated so event-count, leakage, unreachable-target, denominator, and constant-output mutants fail for the intended assertion.

## Day 181 — exercise real fresh-process behavior

Use the supplied subprocess harness to invoke the real CLI in fresh interpreters with a bounded timeout. Compare independently expected semantic output as well as reproducibility, and retain the bounded Python network-API guard. Do not describe that guard as OS-wide isolation or an observed production SLO.

## Day 182 — recover, summarize evidence, and keep the boundary

Add no new production function. Repair prior seams, rerun the first failing acceptance slice, and distinguish learner results from instructor witnesses. Keep the result described as a deterministic synthetic global baseline; the evidence does not establish personalized ML, real-user quality, cloud deployment, or certification readiness.
