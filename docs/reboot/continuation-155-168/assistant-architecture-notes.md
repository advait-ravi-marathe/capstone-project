# Assistant-supplied architecture notes — Days 155–168

These notes describe the supplied StreamForge slice and why its boundaries are
there. They are implementation guidance, not a capstone design exercise. The
learner implements and tests the named seam. System-design interview practice
uses only the separate question bank.

## Day 155 — Admit the fixture manifest

Keep manifest admission as a pure validation step over the already decoded
mapping. Check its exact version, registered fixture identity, synthetic source
declarations, file names, and fixed interval; return a typed admission value.
Do not read files or parse timestamps here. That keeps declaration validation
separate from bounded I/O and makes the boundary easy to test without side
effects.

## Day 156 — Read bounded CSV rows

Use Python's CSV reader over the supplied text, then validate exact header
names, row widths, row count, and UTF-8 field/input limits. Keep values as
strings. CSV quoting is part of the format; splitting lines or commas manually
would corrupt quoted fields and hide malformed records.

## Day 157 — Parse canonical UTC instants

Accept only the declared `YYYY-MM-DDTHH:MM:SSZ` representation and return an
aware UTC `datetime`. Reject offsets, missing zones, fractions, and invalid
calendar dates instead of normalizing alternate forms. A single time format
makes cutoff comparisons deterministic.

## Day 158 — Validate catalog rows

Convert validated string mappings into immutable typed catalog records. Enforce
the genre allowlist, integer duration bounds, exact IDs, unique media IDs, and
paired finite availability. The exact identity is retained so a later event
join cannot silently match a trimmed or merged ID.

## Day 159 — Validate and join event rows

Validate each event's exact IDs, timestamp, and watch duration, then resolve its
media ID against the catalog and apply the half-open availability interval.
The catalog supplies reference metadata; it is not training interaction data.
One validation boundary prevents malformed events from entering later splits.

## Day 160 — Deduplicate before temporal partitioning

Index the fully typed events by event ID before splitting. Collapse an exact
normalized replay once and reject the same ID with changed payload. Doing this
before partition prevents a conflicting replay from being hidden on the other
side of the cutoff.

## Day 161 — Recover the prior-only parser slice

Do not add a new function. Re-run only Days 155–160 and repair the first broken
boundary. Keep the fixture all-or-nothing so one invalid row cannot leave a
partial dataset that looks usable.

## Day 162 — Partition on the fixed UTC window

Validate `start < cutoff < end`, then assign every unique event exactly once
to `[start, cutoff)`, `[cutoff, end)`, or outside the declared observation
window. Use stable `(timestamp, event_id)` ordering. The cutoff is fixed by the
fixture contract, never selected after seeing outcomes.

## Day 163 — Report holdout coverage

Compare holdout viewers and media IDs with training interactions, producing
four mutually exclusive coverage categories. Catalog presence alone does not
mean an item was observed in training. Explicit empty-holdout status avoids
inventing a coverage ratio when its denominator is zero.

## Day 164 — Aggregate training only

Compute counts and watched seconds only from the train partition, using the
validated catalog for the declared media rows. Keep holdout diagnostics out of
the summary. This prevents future-period observations from leaking into the
training description and makes holdout mutation a useful negative control.

## Day 165 — Assemble canonical output and hashes

Assemble the typed records into the fixed versioned bundle and use the supplied
canonical JSON/hash helper. Keep train content, holdout diagnostics, reference
catalog, and source-file hashes in separate fields. The content digest describes
the defined training content; source digests describe exact input bytes.

## Day 166 — Compose the local CLI

Call the existing bounded reader and fixture registry, then compose the
validated functions in chronological order. Emit one complete JSON document
only after the full operation succeeds; send errors to stderr and return
nonzero without a success document. Do not add networking, persistence, or a
model dependency.

## Day 167 — Verify the composed CLI with an independent oracle

The instructor-owned subprocess oracle already supplies hand-computed rows,
cutoff membership, summary, and hash expectations. Work only on prior seams;
do not create a new semantic function. Independent constants prevent a
consistently wrong implementation from validating itself.

## Day 168 — Recover and report limits

Use the day for prior-only repair and evidence. State exactly which fixture and
selectors ran, preserve blocked or not-run cases, and keep the result limited
to offline synthetic data preparation. This does not prove recommendation
quality, privacy/legal clearance, cloud operation, or production readiness.

## Prerequisite gate: earlier A/B learner evidence

<!-- SPRINT12_PRIOR_GATE_START -->
The authoritative records are `evidence/continuation-141-154/day-153.json` and `evidence/continuation-141-154/day-154.json`. Current gate status is **not met**: `evidence/continuation-141-154/day-153.json` records `not_attempted` / `not_run`, tested-source SHA null, 0 artifact hashes, and learner completion `false`; `evidence/continuation-141-154/day-154.json` records `not_attempted` / `not_run`, tested-source SHA null, 0 artifact hashes, and learner completion `false`. A starter or assistant/instructor witness cannot satisfy this gate. Re-evaluate these records when genuine learner evidence is added; do not freeze the gate as permanently unmet.
<!-- SPRINT12_PRIOR_GATE_END -->
