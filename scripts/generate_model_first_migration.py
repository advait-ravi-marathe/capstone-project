#!/usr/bin/env python3
"""Create the 30-active / 275-superseded mapping from the immutable board export."""

import hashlib
import json
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
SOURCE = ROOT / "docs/reboot/calendar/story-export-1-305.json"
DEST = ROOT / "docs/model-first-roadmap/legacy-issue-migration.json"
MANIFEST = ROOT / "docs/model-first-roadmap/manifest.json"


def generate() -> dict:
    source_export = json.loads(SOURCE.read_text())
    active = json.loads(MANIFEST.read_text())
    stories = source_export["stories"]
    by_issue = {story.get("eai_issue_id") or story.get("new_eai_issue_id"): story for story in stories}
    by_old_day = {story["calendar_study_day"]: story for story in stories
                  if story.get("calendar_study_day") is not None}
    if len(stories) != 305 or len(by_issue) != 305 or None in by_issue:
        raise ValueError("expected exactly 305 unique immutable legacy issue records")
    if len(active["detailed_days"]) != 30:
        raise ValueError("this release mapping supports exactly 30 detailed active days")
    source_hash = hashlib.sha256(SOURCE.read_bytes()).hexdigest()
    mappings = []
    selected = {f"EAI-{n}" for n in range(211, 241)}
    for day in active["detailed_days"]:
        issue = f"EAI-{210 + day['day']}"
        prior = by_issue[issue]
        dsa_source = by_old_day.get(day["day"])
        dsa = (dsa_source or {}).get("dsa_section") or {
            "mode": "external_daily_problem", "dsa_calendar_day": day["day"],
            "copied_content": False,
        }
        mappings.append({
            "active_unit_id": day["unit_id"], "active_day": day["day"], "active_date": day["date"],
            "active_sprint_number": (day["day"] - 1) // 14 + 1,
            "existing_issue_id": issue,
            "mapping_status": "candidate_restatement_requires_parent_review_and_board_migration",
            "legacy_unit_id": prior["unit_id"], "legacy_title": prior["title"],
            "legacy_study_day": prior.get("calendar_study_day"),
            "legacy_definition_sha256": hashlib.sha256(json.dumps(
                prior, ensure_ascii=False, sort_keys=True, separators=(",", ":")).encode()).hexdigest(),
            "legacy_contract_path": prior.get("contract_path"),
            "legacy_status_preserved": prior.get("board_status"),
            "legacy_definition_immutable": True,
            "learner_completion_credit": False,
            "dsa_pointer": {
                "mode": dsa.get("mode", "external_daily_problem"),
                "dsa_calendar_day": dsa.get("dsa_calendar_day", day["day"]),
                "repository": "https://github.com/advait-ravi-marathe/dsa-prep",
                "source_commit": "c2caee396002bb48b2a34d407545ecd96ecdb1ab",
                "pointer_source_issue_id": (dsa_source or {}).get("eai_issue_id"),
                "copied_content": False, "solutions_copied": False,
            },
        })
    superseded = []
    for story in stories:
        issue = story.get("eai_issue_id") or story.get("new_eai_issue_id")
        if issue in selected:
            continue
        superseded.append({
            "existing_issue_id": issue, "legacy_unit_id": story["unit_id"],
            "legacy_title": story["title"], "legacy_study_day": story.get("calendar_study_day"),
            "legacy_definition_sha256": hashlib.sha256(json.dumps(
                story, ensure_ascii=False, sort_keys=True, separators=(",", ":")).encode()).hexdigest(),
            "legacy_contract_path": story.get("contract_path"),
            "legacy_status_preserved": story.get("board_status"),
            "disposition": "superseded_not_deleted",
            "reason": "Prior sequence retained as historical evidence; no learner completion is credited.",
            "learner_status": "not_started", "completion_credit": False,
        })
    return {
        "schema_version": 1,
        "status": "local_review_proposal_only_no_board_writes",
        "source_issue_export": str(SOURCE.relative_to(ROOT)),
        "source_issue_export_sha256": source_hash,
        "mapping_source_note": "The immutable published export is the sole legacy mapping source; no API-first candidate files are required.",
        "active_manifest": "docs/model-first-roadmap/manifest.json",
        "active_days": 30, "active_issue_count": len(mappings),
        "detailed_sprint_windows": active["detailed_sprint_windows"],
        "superseded_issue_count": len(superseded), "total_legacy_issue_count": len(mappings) + len(superseded),
        "existing_ids_reused_without_new_issue_creation": True,
        "old_definitions_and_statuses_preserved": True,
        "completion_count": 0, "board_writes": False, "dsa_repository_writes": False,
        "dsa_source_commit": active["dsa_source"]["commit"],
        "active_mappings": mappings,
        "superseded_records": superseded,
    }


def main() -> None:
    import argparse
    parser = argparse.ArgumentParser()
    parser.add_argument("--check", action="store_true")
    args = parser.parse_args()
    expected = json.dumps(generate(), indent=2, ensure_ascii=False) + "\n"
    if args.check:
        if not DEST.exists() or DEST.read_text() != expected:
            raise SystemExit("model-first migration export is stale")
    else:
        DEST.parent.mkdir(parents=True, exist_ok=True)
        DEST.write_text(expected)
    print("verified" if args.check else "generated", "30 active + 275 superseded records")


if __name__ == "__main__":
    main()
