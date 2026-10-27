#!/usr/bin/env python3
"""Create a read-only mapping from the original 305 issue records."""

import hashlib
import json
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
SOURCE = ROOT / "docs/ai-first-roadmap/legacy-issue-migration.json"
DEST = ROOT / "docs/model-first-roadmap/legacy-issue-migration.json"
MANIFEST = ROOT / "docs/model-first-roadmap/manifest.json"


def generate() -> dict:
    original = json.loads(SOURCE.read_text())
    active = json.loads(MANIFEST.read_text())
    # Reuse the already-proposed first-30 issue IDs. Board definitions still need
    # a separate reviewed migration; this file does not perform one.
    old_active = {row["existing_issue_id"]: row for row in original["active_mappings"]}
    source_bytes = (ROOT / original["legacy_source"]).read_bytes()
    source_hash = hashlib.sha256(source_bytes).hexdigest()
    mappings = []
    for day in active["detailed_days"]:
        issue = f"EAI-{210 + day['day']}"
        prior = old_active[issue]
        mappings.append({
            "active_unit_id": day["unit_id"], "active_day": day["day"], "active_date": day["date"],
            "existing_issue_id": issue,
            "mapping_status": "candidate_restatement_requires_parent_review_and_board_migration",
            "legacy_unit_id": prior["legacy_unit_id"],
            "legacy_title": prior["legacy_title"],
            "legacy_study_day": prior["legacy_study_day"],
            "legacy_definition_sha256": prior["legacy_definition_sha256"],
            "legacy_contract_path": prior["legacy_contract_path"],
            "legacy_status_preserved": prior["legacy_status_preserved"],
            "legacy_definition_immutable": True,
            "learner_completion_credit": False,
            "dsa_pointer": prior["dsa_pointer"],
        })
    superseded = original["superseded_records"]
    return {
        "schema_version": 1,
        "status": "local_review_proposal_only_no_board_writes",
        "source_issue_export": original["legacy_source"],
        "source_issue_export_sha256": source_hash,
        "source_prior_candidate_mapping": "docs/ai-first-roadmap/legacy-issue-migration.json",
        "active_manifest": "docs/model-first-roadmap/manifest.json",
        "active_days": 30, "active_issue_count": len(mappings),
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
