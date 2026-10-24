# Assistant-supplied architecture notes: StreamForge Days 183–196

These are the implementation decisions for the bounded synthetic experiment. The learner implements and tests these choices; system-design proposals are practiced only in the separate interview question bank. The arithmetic profile remains a transparent heuristic and does not activate a production AI feature.

The experiment itself remains blocked until the live Day 153/154 learner-owned A/B gate qualifies. Use `python3 scripts/check_streamforge_169_182_entry_gate.py`; exit 2 means keep StreamForge implementation paused and continue the prior recovery work.

<a id="day-183"></a>
## Day 183: Encode one fixed genre

Freeze the five allowed genre labels and encode each as one fixed five-coordinate one-hot tuple. This keeps category identity explicit and prevents ordinal arithmetic from implying that one genre is “larger” than another.

<a id="day-184"></a>
## Day 184: Bind item features without fitting the holdout

Build a deterministic media-ID-to-feature map from the frozen vocabulary and catalog snapshot. Keep feature extraction separate from time-window candidate eligibility so expired historical items can still inform training without becoming recommendable candidates.

<a id="day-185"></a>
## Day 185: Count distinct positive training interests

Count distinct positive (viewer, media) training pairs by the item’s fixed genre, then aggregate for the declared training-viewer cohort. Deduplication prevents repeated events from inflating preference counts; holdout rows never enter this function.

<a id="day-186"></a>
## Day 186: Normalize profiles with exact fractions

Normalize each five-count vector by its exact L1 total using Fraction, and retain an all-zero vector for no-signal viewers. Exact arithmetic keeps ties stable and makes the zero case explicit.

<a id="day-187"></a>
## Day 187: Score candidate features transparently

Score each candidate by the dot product of its one-hot genre vector and the normalized viewer profile. This is transparent arithmetic for the declared feature set, not a learned or calibrated probability.

<a id="day-188"></a>
## Day 188: Rank profiles and explicit cold-start fallbacks

Rank candidates by descending exact score then media ID, while preserving distinct unseen, seen-zero, positive-no-overlap, and empty-candidate modes. Keep the existing global list as the explicit cold/zero-signal fallback.

<a id="day-189"></a>
## Day 189: Recover feature and ranking contracts

Recovery composes only the six prior feature/ranking selectors; it adds no production seam. That keeps recovery bounded to the first half of the experiment and avoids advancing into evaluation before its inputs are ready.

<a id="day-190"></a>
## Day 190: Freeze an immutable predictor state

Freeze protocol, candidate features, training-derived counts/profiles, and global baseline into deeply immutable predictor state. Exclude holdout/query data and source-byte hashes from model state so evaluation changes cannot leak backward into the predictor.

<a id="day-191"></a>
## Day 191: Evaluate per-viewer rankings on the same cohort

Evaluate every positive-target viewer over the same frozen candidate set and preserve all distinct positive targets, including unreachable items. Apply the fixed 2,500 scored-pair guard before ranking so resource limits fail explicitly instead of truncating.

<a id="day-192"></a>
## Day 192: Compare with the unchanged global algorithm

Compare profile and existing global results only after validating that their observable cohort, candidates, counts, statuses, and denominators agree. Signed profile-minus-global deltas make regressions visible instead of treating the profile as presumed better.

<a id="day-193"></a>
## Day 193: Assemble the complete evidence report

Assemble one complete report from validated predictor, query, evaluation, and preparation lineage values. Deep-copy and hash canonical projections at the existing boundary; never emit partial success or imply hashes prove authenticity.

<a id="day-194"></a>
## Day 194: Run the real local composition

Use the existing CLI composition to prepare the registered synthetic fixture once, call the baseline and profile seams, and emit the full report only after all stages validate. Keep temporary fixture copies byte-preserved and do not mutate production or source data.

<a id="day-195"></a>
## Day 195: Prove sensitivity and reject plausible wrong code

Keep adversarial controls outside learner production code: change only the declared training or holdout input and check the independent report oracle, process boundary, and work limit. Restore temporary witnesses byte-for-byte; a green control is scaffold evidence, not learner completion.

<a id="day-196"></a>
## Day 196: Recover and defend the experiment

Run only prior selectors and present the experiment with its observed limits. Recovery adds no new model, deployment, data source, or feature; unresolved A/B readiness and learner seams remain explicit rather than being inferred complete.

