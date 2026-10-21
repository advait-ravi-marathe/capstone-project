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
    return pairs


def _event_anchor(event, anchors):
    pid = event.get("anchor_problem_id", event.get("problem_id"))
    aid = event.get("anchor_id")
    matches = [a for a in anchors if (aid and a["anchor_id"] == aid) or
               (pid is not None and int(pid) == a["problem_id"] and
                (event.get("anchor_day") is None or
                 (int(event["anchor_day"]), int(pid)) in accepted_anchor_pairs()))]
    if len(matches) != 1:
        raise ValueError(f"attempt log references unknown anchor or ambiguous canonical anchor: {event}")
    return matches[0]


def _validate_event(event, anchors):
    if not isinstance(event, dict):
        raise ValueError("each DSA event must be an object")
    if not event.get("date"):
        raise ValueError("each actual DSA event requires its observed local date")
    dt.date.fromisoformat(event["date"])
    schema = event.get("schema_version")
    if schema not in (None, 1, 2):
        raise ValueError(f"unsupported DSA event schema_version: {schema}")
    if schema == 2:
        if not event.get("event_id") or not event.get("timezone"):
            raise ValueError("version 2 events require event_id and IANA timezone")
        try:
            ZoneInfo(event["timezone"])
        except ZoneInfoNotFoundError as exc:
            raise ValueError(f"unknown IANA timezone: {event['timezone']}") from exc
    anchor = _event_anchor(event, anchors)
    kind = event.get("kind")
    if kind not in ACTUAL_KINDS | REVIEW_KINDS | {"exposure", "correction"}:
        raise ValueError(f"unknown DSA event kind: {kind}")
    if kind == "correction" and not event.get("corrects_event_id"):
        raise ValueError("correction events must reference the event they correct")
    assistance = event.get("assistance")
    if assistance is not None:
        values = assistance if isinstance(assistance, list) else [assistance]
        if any(v not in ASSISTANCE for v in values):
            raise ValueError(f"unknown assistance classification: {assistance}")
    if schema == 2:
        if event.get("activity") not in {"guided_anchor", "independent_transfer", "original_recall", "transfer_retention", "repair"}:
            raise ValueError("version 2 events require a recognized activity")
        if event.get("result") not in {"attempted", "passed", "failed", "incomplete"}:
            raise ValueError("version 2 events require an observed result")
    return anchor


def _effective_events(events, on_date, anchors):
    normalized = []
    ids = {}
    for event in events:
        anchor = _validate_event(event, anchors)
        if event["date"] > on_date.isoformat():
            continue
        eid = event.get("event_id")
        if eid:
            signature = json.dumps(event, sort_keys=True, separators=(",", ":"))
            if eid in ids and ids[eid] != signature:
                raise ValueError(f"conflicting duplicate event_id: {eid}")
            if eid in ids:
                continue
            ids[eid] = signature
        normalized.append((anchor, event))
    return normalized


def _tiers(count):
    return 60 if count <= 2 else 75 if count == 3 else 90


def build_queue(events, on_date, roadmap_day=None, confirmed_transfers=None):
    """Return a read-only projection. Plan dates never become actual events."""
    as_of = dt.date.fromisoformat(on_date)
    anchors = load_registry()
    catalog = load_catalog()
    effective = _effective_events(events, as_of, anchors)
    grouped = {a["anchor_id"]: [] for a in anchors}
    exposures = set()
    for anchor, event in effective:
        grouped[anchor["anchor_id"]].append(event)
        target = event.get("target_problem_id")
        if target is not None:
            exposures.add(int(target))

    rows = []
    due_tasks = []
    for anchor in anchors:
        ae = grouped[anchor["anchor_id"]]
        firsts = [e for e in ae if e.get("kind") in ACTUAL_KINDS and
                  e.get("activity") != "independent_transfer"]
        # Legacy first_attempt records are retained as actual attempts. For v2,
        # retrieval/transfer events never start a canonical anchor clock.
        firsts = [e for e in firsts if not (e.get("kind") == "retrieval_attempt" and
                  e.get("activity") in {"independent_transfer", "transfer_retention"})]
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
                independent_passes = [e for e in observed if e.get("result") == "passed" and
                                      e.get("activity") == "independent_transfer" and
                                      e.get("assistance") in (None, "none", []) and
                                      isinstance(e.get("invariant_explanation"), str) and
                                      bool(e.get("invariant_explanation").strip())]
                historical_pass = any(e.get("result") == "passed" for e in recorded_as_of if e.get("schema_version") != 2)
                failed = any(e.get("result") == "failed" for e in observed)
                assisted_pass = any(e.get("result") == "passed" and e not in independent_passes for e in observed)
                state = "failed_repair_pending" if failed else (
                    "passed_independent_transfer" if independent_passes else
                    "historical_passed_unclassified" if historical_pass else
                    "scheduled" if as_of < due else "due" if as_of == due else "overdue")
                if state in {"due", "overdue", "failed_repair_pending"}:
                    due_tasks.append({"anchor_id": anchor["anchor_id"], "problem_id": anchor["problem_id"],
                                      "slot_id": slot, "interval_days": interval,
                                      "original_due_date": due.isoformat(), "status": state})
                reviews.append({"slot_id": slot, "interval_days": interval,
                                "due_date_from_earliest_actual_first_attempt": due.isoformat(),
                                "due_date_from_actual_attempt": due.isoformat(),
                                "status": state,
                                "independent_transfer_evidence": bool(independent_passes),
                                "historical_result_retained_unclassified": historical_pass,
                                "assisted_observation_does_not_close_slot": assisted_pass,
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

    # Existing reviews and repairs always take priority. The prescribed first
    # three guided anchors lead the revised sequence; later suggestions follow
    # the source registry order only when no review is due. Never infer that a
    # planned or suggested encounter was attempted or completed.
    priority = {"failed_repair_pending": 0, "overdue": 1, "due": 2}
    due_tasks.sort(key=lambda x: (priority[x["status"]], x["original_due_date"], x["anchor_id"], x["interval_days"]))
    selected = []
    deferred = []

    planned = []
    if roadmap_day in (1, 2, 3):
        chosen = {1: 121, 2: 1, 3: 383}[roadmap_day]
        planned = [next(a for a in anchors if a["problem_id"] == chosen)]
    elif roadmap_day is not None and roadmap_day <= 150 and roadmap_day not in {133, 140, 147, 154} and not due_tasks:
        # The source inventory's original prerequisite order remains the limit;
        # do not fabricate a new Day-to-anchor assignment.
        seen = {a["anchor_id"] for a in anchors if grouped[a["anchor_id"]] and
                any(e.get("kind") in ACTUAL_KINDS for e in grouped[a["anchor_id"]])}
        # The first three are already the fixed planned sequence on earlier
        # roadmap days. This is planning state only, never an attempt claim.
        if roadmap_day >= 4:
            seen.update({"lc:121", "lc:1", "lc:383"})
        planned = [a for a in anchors if a["anchor_id"] not in seen][:1]
    new_guidance_allowed = roadmap_day is not None and roadmap_day <= 150 and roadmap_day not in {133, 140, 147, 154}
    if new_guidance_allowed and not due_tasks:
        for a in planned:
            if len(selected) < 4:
                selected.append({"anchor_id": a["anchor_id"], "problem_id": a["problem_id"],
                                 "slot_id": f"guided:{a['anchor_id']}", "status": "planned_guided_encounter",
                                 "activity": "guided_anchor", "assistance": "editorial_allowed_not_independent_credit"})
    elif planned:
        deferred.append({"problem_id": planned[0]["problem_id"], "status": "guided_introduction_deferred",
                         "deferred_reason": "due retrieval is processed first; recovery/cutoff days do not introduce guidance"})

    # A candidate is counted as a scheduled exercise only when the learner has
    # checked prior exposure/access/prerequisites and explicitly supplies the
    # target for that specific due slot. The queue itself cannot inspect a
    # Premium account or establish global freshness.
    used_targets = set()
    used_anchors = set()
    by_anchor_id = {a["anchor_id"]: a for a in anchors}
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
        if target in exposures or target in used_targets:
            raise ValueError(f"candidate {target} is already exposed or duplicated in this session")
        if aid in used_anchors:
            raise ValueError(f"only one repair/transfer exercise per anchor may be scheduled in a session: {aid}")
        if len(selected) >= 4:
            raise ValueError("at most four substantive exercises may be scheduled per day")
        if len(selected) < len(due_tasks) and task is not due_tasks[len(selected)]:
            raise ValueError("schedule the oldest due task first; do not skip an earlier review obligation")
        used_anchors.add(aid)
        used_targets.add(target)
        task["target_problem_id"] = target
        task["target_title"] = candidate["title"]
        task["candidate_relation"] = candidate["relation"]
        task["freshness_and_prerequisites"] = "learner_asserted_checked; not independently verified by this tool"
        task["invariant_coach_note"] = candidate["invariant"]
        task["new_prerequisites"] = candidate["new_prerequisites"]
        task["status"] = task["status"]
        selected.append({**task, "activity": "independent_transfer_attempt", "assistance_required": "none until attempt concludes"})
    if len(selected) > 4:
        raise ValueError("at most four substantive exercises may be scheduled per day")
    selected_slots = {x.get("slot_id") for x in selected}
    deferred += [{**t, "deferred_reason": "candidate freshness/access/prerequisites require learner check; slot remains due"}
                 for t in due_tasks if t["slot_id"] not in selected_slots]

    # Candidate IDs are suggestions only after external history/access and
    # prerequisite verification. Unknown freshness can never be called fresh.
    for row in rows:
        entry = catalog.get(row["problem_id"], {})
        candidates = []
        for c in entry.get("candidates", []):
            candidates.append({"problem_id": c["id"], "title": c["title"], "url": c["url"],
                               "relation": c["relation"], "freshness": "previously_exposed" if c["id"] in exposures else "unknown_requires_history_check",
                               "assignment_status": "blocked_until_history_access_and_prerequisites_verified",
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
    parser.add_argument("--log", default="docs/reboot/continuation-31-44/dsa/actual-attempt-log.json")
    args = parser.parse_args()
    log = json.loads(Path(args.log).read_text())
    print(json.dumps(build_queue(log.get("events", []), args.on_date, args.roadmap_day,
                                 args.confirmed_transfer), indent=2))


if __name__ == "__main__":
    main()
