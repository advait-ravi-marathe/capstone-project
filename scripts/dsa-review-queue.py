#!/usr/bin/env python3
"""Project DSA retrieval from actual attempts; never mutate the event log."""
from __future__ import annotations

import argparse
import datetime as dt
import json
from pathlib import Path
from zoneinfo import ZoneInfo, ZoneInfoNotFoundError

ROOT = Path(__file__).resolve().parents[1]
INTERVALS = (3, 7, 21)
ACTUAL_KINDS = {"first_attempt", "anchor_attempt", "retrieval_attempt"}
REVIEW_KINDS = {"review", "retrieval_attempt"}
ASSISTANCE = {"none", "editorial", "hint", "walkthrough", "other_assistance", "unknown"}
POLICY_EFFECTIVE_DATE = dt.date(2026, 10, 22)


def load_registry():
    return json.loads((ROOT / "docs/reboot/dsa/canonical-anchors.json").read_text())["anchors"]


def load_catalog():
    return {e["canonical_id"]: e for e in json.loads(
        (ROOT / "docs/reboot/dsa/guided-transfer-catalog-2026-10-21.json").read_text())["entries"]}


def anchors_from_manifests():
    """Compatibility API for older tests; registry identities remain canonical."""
    anchors = load_registry()
    names = [(1, "legacy"), (31, "continuation"), (45, "continuation-45-56"),
             (57, "continuation-57-70"), (71, "continuation-71-84")]
    return [{**a, "anchor_day": a["original_anchor_day"],
             "batch": next(v for k, v in reversed(names) if a["original_anchor_day"] >= k),
             "planned_first_attempt_date": a["original_planned_date"]} for a in anchors]


def accepted_anchor_pairs():
    """Historical source day/problem pairs, including genuine planned revisits."""
    pairs = set()
    paths = [ROOT / "docs/reboot/manifest.json", *sorted((ROOT / "docs/reboot").glob("continuation-*/manifest.json"))]
    for path in paths:
        manifest = json.loads(path.read_text())
        for day in manifest.get("days", []):
            dsa = day.get("java_dsa", {})
            historical_role = day.get("historical_dsa_role")
            historical_pid = day.get("historical_dsa_problem_id")
            if historical_role == "new_pattern_anchor" and historical_pid is not None:
                pairs.add((day["day"], historical_pid))
            if day.get("dsa_role") == "new_pattern_anchor":
                problem_id = day.get("dsa_problem_id", dsa.get("problem_id"))
                if problem_id is not None:
                    pairs.add((day["day"], problem_id))
            elif dsa.get("mode") == "new_pattern_anchor" or dsa.get("problem_id") is not None and dsa.get("mode") in ("new_pattern_anchor", None):
                if dsa.get("problem_id") is not None:
                    pairs.add((day["day"], dsa["problem_id"]))
            for revisit in dsa.get("review_anchors", []):
                if revisit.get("planned_revisit_date"):
                    pairs.add((day["day"], revisit["problem_id"]))
            historical = dsa.get("historical_planned_assignment", {})
            old_pid = historical.get("problem_id")
            if historical.get("mode") == "new_pattern_anchor" and old_pid is not None:
                pairs.add((day["day"], old_pid))
            for revisit in historical.get("review_anchors", []):
                if revisit.get("planned_revisit_date"):
                    pairs.add((day["day"], revisit["problem_id"]))
    return pairs


def _event_anchor(event, anchors):
    pid = event.get("anchor_problem_id", event.get("problem_id"))
    aid = event.get("anchor_id")
    day = event.get("anchor_day")
    by_id = {a["anchor_id"]: a for a in anchors}
    by_pid = {a["problem_id"]: a for a in anchors}
    if aid is not None and aid not in by_id:
        raise ValueError(f"attempt log references unknown canonical anchor_id: {aid}")
    if pid is not None:
        try:
            pid = int(pid)
        except (TypeError, ValueError) as exc:
            raise ValueError(f"invalid anchor problem id: {pid}") from exc
        if pid not in by_pid:
            raise ValueError(f"attempt log references unknown canonical problem id: {pid}")
    if aid is not None and pid is not None and by_id[aid]["problem_id"] != pid:
        raise ValueError(f"anchor_id and problem id identify different canonical anchors: {event}")
    anchor = by_id.get(aid) if aid is not None else by_pid.get(pid)
    if anchor is None:
        raise ValueError(f"attempt log references unknown anchor or ambiguous canonical anchor: {event}")
    if day is not None:
        try:
            day = int(day)
        except (TypeError, ValueError) as exc:
            raise ValueError(f"invalid historical anchor day alias: {day}") from exc
        aliases = anchor.get("historical_day_aliases", [anchor["original_anchor_day"]])
        if day not in aliases:
            raise ValueError(f"unknown anchor day alias for canonical anchor {anchor['anchor_id']}: {day}")
        if (day, anchor["problem_id"]) not in accepted_anchor_pairs():
            raise ValueError(f"anchor day/problem pair is not a recorded source alias: {day}/{anchor['problem_id']}")
    return anchor


def _validate_event(event, anchors):
    if not isinstance(event, dict):
        raise ValueError("each DSA event must be an object")
    if not event.get("date"):
        raise ValueError("each actual DSA event requires its observed local date")
    dt.date.fromisoformat(event["date"])
    schema = event.get("schema_version")
    if schema not in (None, 1, 2):
        raise ValueError(f"unsupported DSA event schema_version: {schema}")
    kind = event.get("kind")
    if kind not in ACTUAL_KINDS | REVIEW_KINDS | {"exposure", "correction"}:
        raise ValueError(f"unknown DSA event kind: {kind}")
    if kind == "correction" and schema != 2:
        raise ValueError("correction events require version 2 corrected_fields")
    if schema == 2:
        if not event.get("event_id") or not event.get("timezone"):
            raise ValueError("version 2 events require event_id and IANA timezone")
        try:
            ZoneInfo(event["timezone"])
        except ZoneInfoNotFoundError as exc:
            raise ValueError(f"unknown IANA timezone: {event['timezone']}") from exc
    anchor = _event_anchor(event, anchors)
    assistance = event.get("assistance")
    if assistance is not None:
        values = assistance if isinstance(assistance, list) else [assistance]
        if any(v not in ASSISTANCE for v in values):
            raise ValueError(f"unknown assistance classification: {assistance}")
    if schema == 2 and kind == "correction":
        fields = event.get("corrected_fields")
        allowed = {"result", "assistance", "invariant_explanation"}
        if not event.get("corrects_event_id") or not isinstance(fields, dict) or not fields:
            raise ValueError("version 2 correction requires a referenced event and corrected_fields")
        if not set(fields).issubset(allowed):
            raise ValueError("correction contains unsupported fields")
        if "result" in fields and fields["result"] not in {"attempted", "passed", "failed", "incomplete"}:
            raise ValueError("correction contains an invalid result")
        if "assistance" in fields:
            values = fields["assistance"] if isinstance(fields["assistance"], list) else [fields["assistance"]]
            if any(v not in ASSISTANCE for v in values):
                raise ValueError("correction contains invalid assistance")
    elif schema == 2 and kind in ACTUAL_KINDS | REVIEW_KINDS:
        if event.get("activity") not in {"guided_anchor", "independent_transfer", "qualified_extension",
                                          "original_recall", "transfer_retention", "repair"}:
            raise ValueError("version 2 attempt events require a recognized activity")
        if event.get("activity") == "repair" and kind != "retrieval_attempt":
            raise ValueError("repair activity must be a retrieval_attempt with target and evidence")
        if event.get("result") not in {"attempted", "passed", "failed", "incomplete"}:
            raise ValueError("version 2 attempt events require an observed result")
        if "assistance" not in event or assistance is None:
            raise ValueError("version 2 attempt events require an explicit assistance classification")
    elif schema == 2 and kind == "exposure":
        if event.get("target_problem_id") is None:
            raise ValueError("version 2 exposure events require target_problem_id")
    if kind == "retrieval_attempt" and schema == 2:
        interval = event.get("review_interval_days")
        try:
            interval = int(interval)
        except (TypeError, ValueError) as exc:
            raise ValueError("version 2 retrieval_attempt requires review_interval_days") from exc
        if interval not in INTERVALS:
            raise ValueError("retrieval_attempt has an unsupported review interval")
        if event.get("activity") not in {"independent_transfer", "qualified_extension", "transfer_retention",
                                          "original_recall", "repair"}:
            raise ValueError("retrieval_attempt has an invalid review activity")
        if event.get("target_problem_id") is None:
            raise ValueError("retrieval_attempt requires a target_problem_id")
        if not isinstance(event.get("attempt_id"), str) or not event["attempt_id"].strip() or \
                not isinstance(event.get("evidence_reference"), str) or not event["evidence_reference"].strip():
            raise ValueError("retrieval_attempt requires attempt_id and evidence_reference")
        expected_slot = f"{anchor['anchor_id']}+{interval}"
        if event.get("review_slot_id") != expected_slot:
            raise ValueError(f"review_slot_id must be {expected_slot}")
        if event.get("activity") == "repair" and (not isinstance(event.get("repairs_event_id"), str) or
                                                       not event["repairs_event_id"].strip()):
            raise ValueError("repair attempt requires repairs_event_id")
    if kind in {"first_attempt", "anchor_attempt"} and event.get("activity") in {
            "independent_transfer", "qualified_extension", "transfer_retention", "repair"}:
        raise ValueError("a transfer activity, qualified extension activity, or repair activity cannot be recorded as a canonical anchor attempt")
    if kind == "anchor_attempt" and event.get("activity") not in (None, "guided_anchor"):
        raise ValueError("anchor_attempt must describe the canonical guided first attempt")
    return anchor


def _effective_events(events, on_date, anchors):
    normalized = []
    ids = {}
    for index, event in enumerate(events):
        anchor = _validate_event(event, anchors)
        eid = event.get("event_id")
        if eid:
            signature = json.dumps(event, sort_keys=True, separators=(",", ":"))
            if eid in ids and ids[eid] != signature:
                raise ValueError(f"conflicting duplicate event_id: {eid}")
            if eid in ids:
                continue
            ids[eid] = signature
        normalized.append((anchor, dict(event), index))

    corrections = {}
    for anchor, event, index in normalized:
        if event.get("kind") != "correction":
            continue
        target_id = event["corrects_event_id"]
        target = next(((a, e, i) for a, e, i in normalized if e.get("event_id") == target_id), None)
        if target is None:
            raise ValueError(f"correction references unknown event_id: {target_id}")
        target_anchor, target_event, target_index = target
        if target_index >= index or target_event.get("kind") == "correction":
            raise ValueError("correction must reference an earlier non-correction event")
        if target_event["date"] > event["date"]:
            raise ValueError("correction date cannot precede the referenced event")
        if target_anchor["anchor_id"] != anchor["anchor_id"]:
            raise ValueError("correction and referenced event must use the same canonical anchor")
        if target_id in corrections:
            raise ValueError(f"event has multiple corrections: {target_id}")
        corrections[target_id] = event

    by_event_id = {event.get("event_id"): (anchor, event, index)
                   for anchor, event, index in normalized if event.get("event_id")}
    for anchor, event, index in normalized:
        if event.get("kind") != "retrieval_attempt" or event.get("activity") != "repair":
            continue
        failure = by_event_id.get(event.get("repairs_event_id"))
        if failure is None:
            raise ValueError(f"repair references unknown failed event_id: {event.get('repairs_event_id')}")
        failed_anchor, failed_event, failed_index = failure
        if failed_index >= index or failed_event.get("kind") != "retrieval_attempt" or \
                failed_event.get("result") != "failed":
            raise ValueError("repair must reference an earlier failed retrieval_attempt")
        same_slot = (failed_event.get("review_slot_id") == event.get("review_slot_id") and
                     failed_event.get("review_interval_days") == event.get("review_interval_days"))
        if failed_anchor["anchor_id"] != anchor["anchor_id"] or not same_slot:
            raise ValueError("repair must reference a failure for the same anchor and review slot")
        if failed_event.get("target_problem_id") != event.get("target_problem_id"):
            raise ValueError("repair target must match the failed attempt target")
        if failed_event.get("date", "") > event.get("date", ""):
            raise ValueError("repair date cannot precede the failed attempt")

    attempt_slots = {}
    evidence_slots = {}
    for _, event, _ in normalized:
        if event.get("kind") != "retrieval_attempt" or event.get("schema_version") != 2:
            continue
        attempt_id, evidence, slot = event["attempt_id"], event["evidence_reference"], event["review_slot_id"]
        if attempt_id in attempt_slots:
            raise ValueError(f"attempt_id can belong to only one retrieval event and review slot: {attempt_id}")
        if evidence in evidence_slots and evidence_slots[evidence] != slot:
            raise ValueError(f"one evidence reference cannot close multiple review slots: {evidence}")
        if evidence in evidence_slots:
            raise ValueError(f"evidence reference is reused for multiple physical attempts: {evidence}")
        attempt_slots[attempt_id] = slot
        evidence_slots[evidence] = slot

    projected = []
    for anchor, event, index in normalized:
        eid = event.get("event_id")
        correction = corrections.get(eid)
        if correction is not None and correction["date"] <= on_date.isoformat():
            event.update(correction["corrected_fields"])
        if event.get("date", "9999-99-99") <= on_date.isoformat() and event.get("kind") != "correction":
            event["_ledger_index"] = index
            projected.append((anchor, event, index))
    return projected


def _tiers(count):
    return 60 if count <= 2 else 75 if count == 3 else 90


def _candidate_index(catalog):
    result = {}
    for entry in catalog.values():
        for candidate in entry.get("candidates", []):
            family = candidate.get("duplicate_family")
            family_ids = {candidate["id"]}
            if family:
                try:
                    family_ids.update(int(part) for part in family.split("="))
                except ValueError:
                    pass
            result[candidate["id"]] = {**candidate, "family_ids": family_ids}
    return result


def _unaided(assistance):
    return assistance == "none" or (isinstance(assistance, list) and
                                     bool(assistance) and all(value == "none" for value in assistance))


def _full_transfer_relation(relation):
    return relation in {"direct", "near", "near_with_new_signature", "near_with_objective_reversal"}


def _eligible_transfer(event, anchor, candidates, prior_events):
    target = event.get("target_problem_id")
    if target is None:
        return False, "missing target problem id"
    try:
        target = int(target)
    except (TypeError, ValueError):
        return False, "invalid target problem id"
    candidate = candidates.get(target)
    if candidate is None or target not in {c["id"] for c in load_catalog()[anchor["problem_id"]].get("candidates", [])}:
        return False, "target is not mapped to this canonical anchor"
    family = candidate["family_ids"]
    for earlier_anchor, earlier_event, earlier_index in prior_events:
        if earlier_event.get("target_problem_id") is None:
            continue
        try:
            earlier_target = int(earlier_event["target_problem_id"])
        except (TypeError, ValueError):
            continue
        previous = candidates.get(earlier_target)
        earlier_date = earlier_event.get("date", "9999-99-99")
        if earlier_date < event["date"] or (earlier_date == event["date"] and earlier_index < event.get("_ledger_index", -1)):
            if earlier_target in family or (previous and previous["family_ids"] & family):
                return False, "target or duplicate-equivalent family was previously exposed"
    if not _full_transfer_relation(candidate.get("relation")):
        return False, "catalog relation is not eligible for full same-invariant transfer credit"
    if not _unaided(event.get("assistance")):
        return False, "attempt was assisted or assistance is unknown"
    if not isinstance(event.get("invariant_explanation"), str) or not event["invariant_explanation"].strip():
        return False, "invariant explanation is missing"
    return True, "eligible"


def build_queue(events, on_date, roadmap_day=None, confirmed_transfers=None, confirmed_repairs=None,
                confirmed_extensions=None):
    """Return a read-only projection. Plan dates never become actual events."""
    as_of = dt.date.fromisoformat(on_date)
    anchors = load_registry()
    catalog = load_catalog()
    candidate_by_id = _candidate_index(catalog)
    effective = _effective_events(events, as_of, anchors)
    grouped = {a["anchor_id"]: [] for a in anchors}
    exposures = set()
    for anchor, event, _ in effective:
        grouped[anchor["anchor_id"]].append(event)
        target = event.get("target_problem_id")
        if target is not None:
            exposures.add(int(target))

    rows = []
    due_tasks = []
    for anchor in anchors:
        ae = grouped[anchor["anchor_id"]]
        # Only an observed first/anchor attempt starts the canonical clock.
        # Retrievals never create or reset a clock, and legacy records without
        # an observed outcome remain visible but cannot start one.
        firsts = [e for e in ae if e.get("kind") in {"first_attempt", "anchor_attempt"}
                  and e.get("result") in {"attempted", "passed", "failed", "incomplete"}
                  and e.get("activity") not in {"independent_transfer", "qualified_extension",
                                                  "transfer_retention", "repair"}]
        first_date = min((dt.date.fromisoformat(e["date"]) for e in firsts), default=None)
        reviews = []
        if first_date:
            for interval in INTERVALS:
                due = first_date + dt.timedelta(days=interval)
                slot = f"{anchor['anchor_id']}+{interval}"
                attempts = [e for e in ae if e.get("review_interval_days", e.get("interval_days")) == interval
                            and e.get("kind") in REVIEW_KINDS]
                # Early work is retained in the source log but does not close
                # a later scheduled checkpoint.
                recorded_as_of = [e for e in attempts if e["date"] <= as_of.isoformat()]
                observed = [e for e in recorded_as_of if dt.date.fromisoformat(e["date"]) >= due]
                for event in observed:
                    event["_ledger_index"] = event.get("_ledger_index", -1)
                attempt_rows = [(e, idx) for e in observed for idx in [e.get("_ledger_index", -1)]]
                independent_passes = []
                invalid_pass_reasons = []
                prior_rows = [(a, e, idx) for a, e, idx in effective]
                for event, event_index in attempt_rows:
                    if (event.get("result") != "passed" or event.get("activity") != "independent_transfer"
                            or event.get("kind") != "retrieval_attempt" or event.get("schema_version") != 2):
                        continue
                    ok, reason = _eligible_transfer(event, anchor, candidate_by_id,
                                                    [row for row in prior_rows if row[2] != event_index])
                    if ok:
                        independent_passes.append(event)
                    else:
                        invalid_pass_reasons.append(reason)
                historical_pass_any = any(e.get("result") == "passed"
                                          and dt.date.fromisoformat(e["date"]) < POLICY_EFFECTIVE_DATE
                                          for e in recorded_as_of if e.get("schema_version") != 2)
                historical_pass_due = any(e.get("result") == "passed" and
                                          dt.date.fromisoformat(e["date"]) >= due
                                          and dt.date.fromisoformat(e["date"]) < POLICY_EFFECTIVE_DATE
                                          for e in recorded_as_of if e.get("schema_version") != 2)
                event_order = lambda e: (e.get("date", ""), e.get("_ledger_index", -1))
                failed_events = [e for e in observed if e.get("result") == "failed"]
                last_failure_event = max(failed_events, key=event_order, default=None)
                repair_passes = [e for e in observed if e.get("result") == "passed" and
                                 e.get("kind") == "retrieval_attempt" and e.get("schema_version") == 2 and
                                 e.get("activity") == "repair" and _unaided(e.get("assistance")) and
                                 last_failure_event is not None and
                                 e.get("repairs_event_id") == last_failure_event.get("event_id") and
                                 e.get("target_problem_id") == last_failure_event.get("target_problem_id")]
                last_failure = max((event_order(e) for e in failed_events), default=("", -1))
                last_independent_pass = max((event_order(e) for e in independent_passes), default=("", -1))
                last_repair_pass = max((event_order(e) for e in repair_passes), default=("", -1))
                failed = last_failure[1] >= 0 and last_failure > max(last_independent_pass, last_repair_pass)
                assisted_pass = any(e.get("result") == "passed" and e not in independent_passes and
                                     (not _unaided(e.get("assistance")) or
                                      e.get("activity") == "guided_anchor") for e in observed)
                resolved_failure = last_failure[1] >= 0 and max(last_independent_pass, last_repair_pass) > last_failure
                current_independent_pass = bool(independent_passes) and last_independent_pass > last_failure
                state = "failed_repair_pending" if failed else (
                    "passed_independent_transfer" if current_independent_pass else
                    "historical_passed_unclassified" if historical_pass_due else
                    "scheduled" if as_of < due else "due" if as_of == due else "overdue")
                if state in {"due", "overdue", "failed_repair_pending"}:
                    task = {"anchor_id": anchor["anchor_id"], "problem_id": anchor["problem_id"],
                            "slot_id": slot, "interval_days": interval,
                            "original_due_date": due.isoformat(), "status": state}
                    if state == "failed_repair_pending" and last_failure_event is not None:
                        task["failed_event_id"] = last_failure_event.get("event_id")
                        task["required_repair_target_problem_id"] = last_failure_event.get("target_problem_id")
                    due_tasks.append(task)
                reviews.append({"slot_id": slot, "interval_days": interval,
                                "due_date_from_earliest_actual_first_attempt": due.isoformat(),
                                "due_date_from_actual_attempt": due.isoformat(),
                                "status": state,
                                "independent_transfer_evidence": current_independent_pass,
                                "historical_result_retained_unclassified": historical_pass_any,
                                "assisted_observation_does_not_close_slot": assisted_pass,
                                "invalid_transfer_observation_reasons": sorted(set(invalid_pass_reasons)),
                                "failed_attempt_resolved_by_later_repair_or_transfer": resolved_failure,
                                "retrieval_method": "independent transfer attempt before editorial; brief original recall does not close this slot",
                                "failed_review_action": "replace new work within the selected DSA tier; preserve original due date" if state == "failed_repair_pending" else None,
                                "activity": "independent_transfer_required",
                                "assistance_rule": "editorial, hints, or walkthrough make this assisted; do not credit as independent transfer"})
        rows.append({"anchor_id": anchor["anchor_id"], "problem_id": anchor["problem_id"],
                     "title": anchor["title"], "original_anchor_day": anchor["original_anchor_day"],
                     "anchor_day": anchor["original_anchor_day"],
                     "batch": next(name for first, name in reversed([(1, "legacy"), (31, "continuation"), (45, "continuation-45-56"), (57, "continuation-57-70"), (71, "continuation-71-84")]) if anchor["original_anchor_day"] >= first),
                     "actual_first_attempt_date": first_date.isoformat() if first_date else None,
                     "status": "review_queue_active" if first_date else "awaiting_actual_attempt",
                     "reviews": reviews})

    # Preserve actual prerequisite order. The first three are fixed planned
    # encounters, but a later day never treats a missed plan as an attempt.
    priority = {"failed_repair_pending": 0, "overdue": 1, "due": 2}
    due_tasks.sort(key=lambda x: (priority[x["status"]], x["original_due_date"], x["anchor_id"], x["interval_days"]))
    selected = []
    deferred = []
    hard_blocker = any(task["status"] in {"failed_repair_pending", "overdue"} for task in due_tasks)

    planned = []
    if roadmap_day in (1, 2, 3):
        chosen = {1: 121, 2: 1, 3: 383}[roadmap_day]
        planned = [next(a for a in anchors if a["problem_id"] == chosen)]
    elif roadmap_day is not None and roadmap_day <= 150 and roadmap_day not in {133, 140, 147, 154}:
        seen = {a["anchor_id"] for a in anchors if grouped[a["anchor_id"]] and
                any(e.get("kind") in {"first_attempt", "anchor_attempt"} and
                    e.get("result") in {"attempted", "passed", "failed", "incomplete"} and
                    e.get("activity") not in {"independent_transfer", "qualified_extension",
                                               "transfer_retention", "repair"}
                    for e in grouped[a["anchor_id"]])}
        planned = [a for a in anchors if a["anchor_id"] not in seen][:1]
    new_guidance_allowed = roadmap_day is not None and roadmap_day <= 150 and roadmap_day not in {133, 140, 147, 154}

    # A candidate is scheduled only after learner confirmation of history,
    # access, and prerequisites. Fill available capacity in due-task order;
    # blocked gaps do not deadlock other eligible anchors.
    by_anchor_id = {a["anchor_id"]: a for a in anchors}
    confirmed = {}
    used_targets = set()
    seen_families = set()
    for exposure in exposures:
        indexed = candidate_by_id.get(exposure)
        seen_families.add(exposure)
        if indexed:
            seen_families.update(indexed["family_ids"])
    confirmed_repair_map = {}
    for raw in confirmed_repairs or []:
        try:
            slot_id, target_text = raw.split("=", 1)
            aid, interval_text = slot_id.rsplit("+", 1)
            interval, target = int(interval_text), int(target_text)
        except (ValueError, TypeError):
            raise ValueError(f"confirmed repair must look like lc:121+3=2016: {raw}")
        task = next((t for t in due_tasks if t["slot_id"] == slot_id), None)
        if interval not in INTERVALS or aid not in by_anchor_id or task is None or \
                task["status"] != "failed_repair_pending":
            raise ValueError(f"repair slot is not awaiting a failed-attempt repair: {slot_id}")
        if target != task.get("required_repair_target_problem_id"):
            raise ValueError(f"repair target must match failed target {task.get('required_repair_target_problem_id')}")
        if slot_id in confirmed_repair_map:
            raise ValueError(f"repair slot was confirmed more than once: {slot_id}")
        confirmed_repair_map[slot_id] = target
    confirmed_extension_map = {}
    for raw in confirmed_extensions or []:
        try:
            slot_id, target_text = raw.split("=", 1)
            aid, interval_text = slot_id.rsplit("+", 1)
            interval, target = int(interval_text), int(target_text)
        except (ValueError, TypeError):
            raise ValueError(f"confirmed extension must look like lc:121+7=1014: {raw}")
        if interval not in INTERVALS or aid not in by_anchor_id:
            raise ValueError(f"unknown extension review slot: {slot_id}")
        task = next((t for t in due_tasks if t["slot_id"] == slot_id), None)
        if task is None:
            raise ValueError(f"extension slot is not due as of the selected date: {slot_id}")
        candidates = catalog.get(by_anchor_id[aid]["problem_id"], {}).get("candidates", [])
        candidate = next((c for c in candidates if c["id"] == target), None)
        if candidate is None or _full_transfer_relation(candidate.get("relation")):
            raise ValueError(f"candidate {target} is not a qualified non-full extension for {aid}")
        family = candidate_by_id[target]["family_ids"]
        if family & (seen_families | used_targets):
            raise ValueError(f"extension target {target} or an equivalent problem was already exposed or selected")
        if slot_id in confirmed_extension_map:
            raise ValueError(f"extension slot was confirmed more than once: {slot_id}")
        confirmed_extension_map[slot_id] = candidate
        used_targets.update(family)
    for raw in confirmed_transfers or []:
        try:
            slot_id, target_text = raw.split("=", 1)
            aid, interval_text = slot_id.rsplit("+", 1)
            interval, target = int(interval_text), int(target_text)
        except (ValueError, TypeError):
            raise ValueError(f"confirmed transfer must look like lc:121+3=2016: {raw}")
        if interval not in INTERVALS or aid not in by_anchor_id:
            raise ValueError(f"unknown transfer review slot: {slot_id}")
        task = next((t for t in due_tasks if t["slot_id"] == slot_id), None)
        if task is None:
            raise ValueError(f"transfer slot is not due as of the selected date: {slot_id}")
        candidates = catalog.get(by_anchor_id[aid]["problem_id"], {}).get("candidates", [])
        candidate = next((c for c in candidates if c["id"] == target), None)
        if candidate is None:
            raise ValueError(f"candidate {target} is not in the verified catalog for {aid}")
        if not _full_transfer_relation(candidate.get("relation")):
            raise ValueError(f"candidate {target} is not a full same-invariant transfer for {aid}")
        family = candidate_by_id[target]["family_ids"]
        if family & (seen_families | used_targets):
            raise ValueError(f"candidate {target} or an equivalent problem was already exposed or selected")
        if slot_id in confirmed:
            raise ValueError(f"transfer slot was confirmed more than once: {slot_id}")
        confirmed[slot_id] = candidate
        used_targets.update(family)

    overlap = (set(confirmed_repair_map) & set(confirmed)) | \
              (set(confirmed_repair_map) & set(confirmed_extension_map)) | \
              (set(confirmed) & set(confirmed_extension_map))
    if overlap:
        raise ValueError(f"a slot cannot schedule both a repair and a fresh transfer: {sorted(overlap)}")

    selected_anchors = set()
    for task in due_tasks:
        target = confirmed_repair_map.get(task["slot_id"])
        if target is None:
            continue
        aid = task["anchor_id"]
        if aid in selected_anchors:
            deferred.append({**task, "deferred_reason": "one retrieval exercise per anchor per session; preserve original due date"})
            continue
        if len(selected) >= 4:
            deferred.append({**task, "deferred_reason": "four-exercise daily cap; preserve original due date"})
            continue
        selected.append({**task, "target_problem_id": target, "activity": "repair_attempt",
                         "repairs_event_id": task["failed_event_id"],
                         "independent_transfer_credit": False,
                         "assistance_rule": "record assistance accurately; only an evidenced unaided repair resolves the failure"})
        selected_anchors.add(aid)
    for task in due_tasks:
        candidate = confirmed.get(task["slot_id"])
        extension = confirmed_extension_map.get(task["slot_id"])
        if candidate is None and extension is None:
            continue
        aid = task["anchor_id"]
        if aid in selected_anchors:
            deferred.append({**task, "deferred_reason": "one retrieval exercise per anchor per session; preserve original due date"})
            continue
        if len(selected) >= 4:
            deferred.append({**task, "deferred_reason": "four-exercise daily cap; preserve original due date"})
            continue
        candidate = candidate if candidate is not None else extension
        task["target_problem_id"] = candidate["id"]
        task["target_title"] = candidate["title"]
        task["candidate_relation"] = candidate["relation"]
        task["freshness_and_prerequisites"] = "learner_asserted_checked; not independently verified by this tool"
        task["invariant_coach_note"] = candidate["invariant"]
        task["new_prerequisites"] = candidate["new_prerequisites"]
        if extension is None:
            selected.append({**task, "activity": "independent_transfer_attempt",
                             "assistance_required": "none until attempt concludes",
                             "independent_transfer_credit": "eligible only after actual unaided attempt and invariant evidence"})
        else:
            selected.append({**task, "activity": "qualified_extension_attempt",
                             "credit_scope": "extension_or_component_only; does not close independent-transfer checkpoint",
                             "independent_transfer_credit": False,
                             "assistance_required": "record actual assistance; prerequisites must already be demonstrated"})
        selected_anchors.add(aid)

    selected_slots = {x.get("slot_id") for x in selected}
    for task in due_tasks:
        if task["slot_id"] in selected_slots or any(d.get("slot_id") == task["slot_id"] for d in deferred):
            continue
        entry = catalog.get(task["problem_id"], {})
        if task["status"] == "failed_repair_pending":
            reason = (f"repair the prior failed target {task.get('required_repair_target_problem_id')} with actual attempt and evidence; "
                      "confirm using --confirmed-repair; keep the original slot date")
        else:
            reason = "validated_transfer_needed: " + "; ".join(entry.get("gaps", [])) if not entry.get("candidates") else \
                 "candidate freshness/access/prerequisites require learner check; slot remains due"
        deferred.append({**task, "deferred_reason": reason})

    # Due-today retrieval may share the session with guided work. A failed or
    # overdue review displaces new guidance; future due work remains visible.
    if new_guidance_allowed and planned and not hard_blocker:
        for anchor in planned:
            if anchor["anchor_id"] in selected_anchors:
                continue
            if len(selected) < 4:
                selected.append({"anchor_id": anchor["anchor_id"], "problem_id": anchor["problem_id"],
                                 "slot_id": f"guided:{anchor['anchor_id']}", "status": "planned_guided_encounter",
                                 "activity": "guided_anchor", "assistance": "editorial_allowed_not_independent_credit"})
            else:
                deferred.append({"anchor_id": anchor["anchor_id"], "problem_id": anchor["problem_id"],
                                 "slot_id": f"guided:{anchor['anchor_id']}", "status": "guided_introduction_deferred",
                                 "activity": "guided_anchor",
                                 "deferred_reason": "four-exercise daily cap; due retrievals retain priority"})
            break
    elif planned:
        deferred.append({"problem_id": planned[0]["problem_id"], "status": "guided_introduction_deferred",
                         "deferred_reason": "failed/overdue retrieval takes priority, or this is a recovery/cutoff day"})

    # Candidate IDs are suggestions only after external history/access and
    # prerequisite verification. Unknown freshness can never be called fresh.
    for row in rows:
        entry = catalog.get(row["problem_id"], {})
        candidates = []
        for c in entry.get("candidates", []):
            candidates.append({"problem_id": c["id"], "title": c["title"], "url": c["url"],
                               "relation": c["relation"], "freshness": "previously_exposed" if candidate_by_id[c["id"]]["family_ids"] & seen_families else "unknown_requires_history_check",
                               "assignment_status": "blocked_relation_not_full_transfer" if not _full_transfer_relation(c["relation"]) else "blocked_until_history_access_and_prerequisites_verified",
                               "invariant_coach_note": c["invariant"], "new_prerequisites": c["new_prerequisites"]})
        row["transfer_candidates"] = candidates
        row["catalog_gap"] = entry.get("gaps", [])

    count = len(selected)
    dsa_minutes = _tiers(count)
    return {"as_of_date": as_of.isoformat(), "roadmap_day": roadmap_day,
            "policy_effective_date": "2026-10-22", "timezone": "Asia/Kolkata",
            "daily_dsa_budget_minutes": dsa_minutes, "engineering_minutes": 120,
            "total_daily_minutes": dsa_minutes + 120, "substantive_exercises_selected": count,
            "tier_rule": {"1_to_2": 60, "3": 75, "4": 90},
            "intervals_days": list(INTERVALS), "date_anchor": "earliest actual first-attempt date only",
            "failed_review_rule": "Failed or overdue retrieval displaces new guidance; no extra minutes, and no due slot is dropped.",
            "selection_priority": ["failed repair", "oldest overdue", "due", "eligible guided encounter"],
            "pending_due_tasks": due_tasks, "selected_exercises": selected,
            "deferred_exercises": deferred,
            "brief_recall_minutes_are_inside_tier": True,
            "transfers_start_new_anchor_clocks": False,
            "new_guided_introductions_allowed_through_roadmap_day": 150,
            "anchors": rows}


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--on-date", required=True, help="YYYY-MM-DD local study date")
    parser.add_argument("--roadmap-day", type=int)
    parser.add_argument("--confirmed-transfer", action="append", default=[],
                        help="learner-checked due target, e.g. lc:121+3=2016; repeat per distinct due slot")
    parser.add_argument("--confirmed-repair", action="append", default=[],
                        help="repair the exact previously failed target, e.g. lc:121+3=2016; does not earn transfer credit")
    parser.add_argument("--confirmed-extension", action="append", default=[],
                        help="schedule a learner-checked qualified extension, e.g. lc:121+7=1014; does not close the transfer slot")
    parser.add_argument("--log", default="docs/reboot/continuation-31-44/dsa/actual-attempt-log.json")
    args = parser.parse_args()
    log = json.loads(Path(args.log).read_text())
    print(json.dumps(build_queue(log.get("events", []), args.on_date, args.roadmap_day,
                                 args.confirmed_transfer, args.confirmed_repair,
                                 args.confirmed_extension), indent=2))


if __name__ == "__main__":
    main()
