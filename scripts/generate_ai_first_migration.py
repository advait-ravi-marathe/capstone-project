#!/usr/bin/env python3
"""Generate a read-only proposal mapping 305 legacy issue IDs to the AI-first plan."""
from __future__ import annotations

import argparse
import hashlib
import json
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
LEGACY = ROOT / "docs/reboot/calendar/story-export-1-305.json"
ACTIVE = ROOT / "docs/ai-first-roadmap/manifest.json"
OUT = ROOT / "docs/ai-first-roadmap/legacy-issue-migration.json"
ACTIVE_ISSUE_IDS = [f"EAI-{n}" for n in range(211, 241)]
DSA_SOURCE_COMMIT = "c2caee396002bb48b2a34d407545ecd96ecdb1ab"


def canonical_sha(value: object) -> str:
    raw = json.dumps(value, ensure_ascii=False, sort_keys=True, separators=(",", ":")).encode()
    return hashlib.sha256(raw).hexdigest()


def build() -> dict:
    legacy_obj = json.loads(LEGACY.read_text(encoding="utf-8"))
    active_obj = json.loads(ACTIVE.read_text(encoding="utf-8"))
    stories = legacy_obj["stories"]
    by_issue = {story.get("eai_issue_id") or story.get("new_eai_issue_id"): story for story in stories}
    by_old_day = {story["calendar_study_day"]: story for story in stories if story.get("calendar_study_day") is not None}
    if len(stories) != 305 or len(by_issue) != 305 or None in by_issue:
        raise ValueError("expected 305 unique legacy EAI issue IDs")
    if len(active_obj["detailed_days"]) != 30:
        raise ValueError("expected 30 active detailed days")

    active = []
    selected = set(ACTIVE_ISSUE_IDS)
    for day, issue_id in zip(active_obj["detailed_days"], ACTIVE_ISSUE_IDS, strict=True):
        source = by_issue[issue_id]
        dsa_source = by_old_day.get(day["day"])
        dsa = (dsa_source or {}).get("dsa_section") or {
            "mode": "external_daily_problem",
            "dsa_calendar_day": day["day"],
            "source": "separate dsa-prep repository",
            "copied_content": False,
        }
        active.append({
            "active_unit_id": day["unit_id"],
            "active_day": day["day"],
            "active_date": day["date"],
            "existing_issue_id": issue_id,
            "mapping_status": "proposed_reuse_after_parent_review",
            "legacy_unit_id": source["unit_id"],
            "legacy_title": source["title"],
            "legacy_study_day": source.get("calendar_study_day"),
            "legacy_definition_sha256": canonical_sha(source),
            "legacy_contract_path": source.get("contract_path"),
            "legacy_status_preserved": source.get("board_status"),
            "legacy_definition_immutable": True,
            "future_card_restatement_requires_separate_board_migration": True,
            "dsa_pointer": {
                "mode": dsa.get("mode", "external_daily_problem"),
                "dsa_calendar_day": dsa.get("dsa_calendar_day", day["day"]),
                "repository": "https://github.com/advait-ravi-marathe/dsa-prep",
                "source_commit": DSA_SOURCE_COMMIT,
                "pointer_source_issue_id": (dsa_source or {}).get("eai_issue_id"),
                "copied_content": False,
                "solutions_copied": False,
            },
            "learner_status": "not_started",
            "completion_credit": False,
        })

    superseded = []
    for story in stories:
        issue_id = story.get("eai_issue_id") or story.get("new_eai_issue_id")
        if issue_id in selected:
            continue
        superseded.append({
            "existing_issue_id": issue_id,
            "legacy_unit_id": story["unit_id"],
            "legacy_title": story["title"],
            "legacy_study_day": story.get("calendar_study_day"),
            "legacy_definition_sha256": canonical_sha(story),
            "legacy_contract_path": story.get("contract_path"),
            "legacy_status_preserved": story.get("board_status"),
            "disposition": "superseded_not_deleted",
            "reason": "Prior sequence retained as historical evidence; no learner completion is credited.",
            "learner_status": "not_started",
            "completion_credit": False,
        })

    return {
        "schema_version": 1,
        "status": "review_proposal_only_no_board_writes",
        "legacy_source": "docs/reboot/calendar/story-export-1-305.json",
        "legacy_source_sha256": hashlib.sha256(LEGACY.read_bytes()).hexdigest(),
        "active_source": "docs/ai-first-roadmap/manifest.json",
        "active_days": 30,
        "active_issue_count": len(active),
        "superseded_issue_count": len(superseded),
        "total_legacy_issue_count": len(active) + len(superseded),
        "legacy_definitions_changed": False,
        "legacy_mapping_changed": False,
        "board_writes": False,
        "dsa_repository_writes": False,
        "dsa_source_commit": DSA_SOURCE_COMMIT,
        "all_old_statuses_preserved_without_completion_credit": True,
        "new_issue_ids_assigned": False,
        "native_issue_ids_are_not_calendar_day_numbers": True,
        "active_mappings": active,
        "superseded_records": superseded,
    }


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--check", action="store_true")
    args = parser.parse_args()
    expected = json.dumps(build(), indent=2, ensure_ascii=False) + "\n"
    if args.check:
        if not OUT.exists() or OUT.read_text(encoding="utf-8") != expected:
            print("legacy issue migration proposal is stale")
            return 1
    else:
        OUT.parent.mkdir(parents=True, exist_ok=True)
        OUT.write_text(expected, encoding="utf-8")
    print("checked/wrote active 30 + superseded legacy issue mapping")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
