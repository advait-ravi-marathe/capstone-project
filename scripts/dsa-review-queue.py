#!/usr/bin/env python3
"""Compute DSA recall reviews from actual attempts without mutating curriculum files."""
from __future__ import annotations
import argparse
import datetime as dt
import json
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
INTERVALS = (3, 7, 21)


def anchors_from_manifests():
    sources = [
        (ROOT / "docs/reboot/manifest.json", "legacy"),
        (ROOT / "docs/reboot/continuation-31-44/manifest.json", "continuation"),
        (ROOT / "docs/reboot/continuation-45-56/manifest.json", "continuation-45-56"),
        (ROOT / "docs/reboot/continuation-57-70/manifest.json", "continuation-57-70"),
    ]
    result = []
    for path, batch in sources:
        manifest = json.loads(path.read_text())
        for day in manifest["days"]:
            dsa = day.get("java_dsa", {})
            if batch == "legacy":
                if day.get("dsa_role") != "new_pattern_anchor":
                    continue
                problem_id = day["dsa_problem_id"]
                pattern = day.get("dsa_pattern")
                planned = day.get("dsa_review_schedule", {}).get("planned_first_attempt_date")
            else:
                if dsa.get("mode") != "new_pattern_anchor":
                    continue
                problem_id = dsa["problem_id"]
                pattern = dsa.get("concept")
                planned = dsa.get("planned_first_attempt_date")
            result.append({"anchor_day": day["day"], "problem_id": problem_id,
                           "pattern": pattern, "planned_first_attempt_date": planned,
                           "batch": batch})
    # A problem has one canonical anchor. Revisit entries never start a new
    # clock; retain the earliest declared anchor in the combined history.
    canonical = {}
    for anchor in sorted(result, key=lambda a: a["anchor_day"]):
        canonical.setdefault(anchor["problem_id"], anchor)
    return list(canonical.values())


def accepted_anchor_pairs():
    """Return historical and planned revisit identifiers accepted for logging."""
    pairs = set()
    sources = [
        (ROOT / "docs/reboot/manifest.json", True),
        (ROOT / "docs/reboot/continuation-31-44/manifest.json", False),
        (ROOT / "docs/reboot/continuation-45-56/manifest.json", False),
        (ROOT / "docs/reboot/continuation-57-70/manifest.json", False),
    ]
    for path, legacy in sources:
        manifest = json.loads(path.read_text())
        for day in manifest["days"]:
            dsa = day.get("java_dsa", {})
            if legacy:
                if day.get("dsa_role") == "new_pattern_anchor":
                    pairs.add((day["day"], day["dsa_problem_id"]))
            elif dsa.get("mode") == "new_pattern_anchor":
                pairs.add((day["day"], dsa["problem_id"]))
            else:
                for revisit in dsa.get("review_anchors", []):
                    # Revisit-day entry uses the same canonical problem ID.
                    if revisit.get("planned_revisit_date"):
                        pairs.add((day["day"], revisit["problem_id"]))
    return pairs


def build_queue(events, on_date):
    on_date = dt.date.fromisoformat(on_date)
    anchors = anchors_from_manifests()
    by_problem = {a["problem_id"]: a for a in anchors}
    valid_anchor_pairs = accepted_anchor_pairs()
    grouped = {problem_id: [] for problem_id in by_problem}
    for event in events:
        key = (event.get("anchor_day"), event.get("problem_id"))
        if key not in valid_anchor_pairs:
            raise ValueError(f"attempt log references unknown anchor: {key}")
        dt.date.fromisoformat(event["date"])
        grouped[event["problem_id"]].append(event)
    queue = []
    for problem_id, anchor in by_problem.items():
        problem_events = grouped[problem_id]
        firsts = [e for e in problem_events if e.get("kind") == "first_attempt"]
        if not firsts:
            queue.append({**anchor, "actual_first_attempt_date": None,
                          "status": "awaiting_actual_attempt", "reviews": []})
            continue
        actual = min(e["date"] for e in firsts)
        first_date = dt.date.fromisoformat(actual)
        reviews = []
        for interval in INTERVALS:
            due = first_date + dt.timedelta(days=interval)
            events_for_interval = [e for e in problem_events
                                   if e.get("kind") == "review" and e.get("interval_days") == interval]
            passes = [e for e in events_for_interval if e.get("result") == "passed"]
            failures = [e for e in events_for_interval if e.get("result") == "failed"]
            if passes:
                state = "passed_with_recorded_evidence"
            elif failures:
                state = "failed_repair_pending"
            elif on_date >= due:
                state = "due" if on_date == due else "overdue"
            else:
                state = "scheduled"
            reviews.append({"interval_days": interval, "due_date_from_actual_attempt": due.isoformat(),
                            "status": state, "retrieval_method": "cold recall and re-solve; do not reread",
                            "failed_review_action": "replace new work within the same 60-minute DSA block; do not add minutes"
                                if failures else None})
        queue.append({**anchor, "actual_first_attempt_date": actual,
                      "status": "review_queue_active", "reviews": reviews})
    return {"as_of_date": on_date.isoformat(), "daily_dsa_budget_minutes": 60,
            "intervals_days": list(INTERVALS), "date_anchor": "actual first-attempt date only",
            "failed_review_rule": "A failed review displaces new work within the same 60-minute block; no extra minutes.",
            "anchors": queue}


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--on-date", required=True, help="YYYY-MM-DD local study date")
    parser.add_argument("--log", default="docs/reboot/continuation-31-44/dsa/actual-attempt-log.json")
    args = parser.parse_args()
    log = json.loads(Path(args.log).read_text())
    print(json.dumps(build_queue(log.get("events", []), args.on_date), indent=2))


if __name__ == "__main__":
    main()
