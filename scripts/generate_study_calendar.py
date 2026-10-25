#!/usr/bin/env python3
"""Render the canonical EAI unit-to-calendar crosswalk and 305-story export."""
from __future__ import annotations

import argparse
import json
import re
from datetime import date, timedelta
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
OUT = ROOT / "docs/reboot/calendar"
START = date(2026, 10, 26)
LEGACY_HORIZON = 732
HORIZON = 774
DETAIL_THROUGH = 284
MILESTONES = [
    (175, "Backend / Full-Stack SDE II", "Demonstrate learner-owned backend/API/data implementation, tests, debugging and safe local operation."),
    (459, "Senior Backend", "Diagnose unfamiliar distributed consistency, failure and recovery cases; show repeated integrated evidence and a truthful handoff."),
    (550, "Applied AI", "Show Python/ML grounding, independent evaluation slices, uncertainty, security and measured limitations."),
    (648, "Senior AI", "Show AI work alongside security, deployment readiness, observability and repeated operational evidence; local rehearsal is not production ownership."),
    (718, "Senior FDE", "Requires externally validated discovery, enterprise integration, stakeholder delivery and actual adoption/outcome evidence."),
    (774, "Staff / FDE / Architect portfolio checkpoint", "Portfolio checkpoint only; these titles are employer-specific and Architect is not universally above Staff."),
]

GAPS = [
    ("GAP-OPENAPI", "OpenAPI and consumer compatibility", 5, "after", 70, True),
    ("GAP-OIDC", "Local OIDC test identity provider", 9, "after", 70, True),
    ("GAP-SCHEMA", "Expand/contract schema and resumable backfill", 7, "after", 154, True),
    ("GAP-AIGOV", "AI data governance and evaluation slices", 7, "before", 439, False),
    ("GAP-IAC", "Offline infrastructure as code and drift", 7, "before", 523, False),
    ("GAP-ENTERPRISE", "Enterprise security, procurement and rollout gates", 7, "before", 523, False),
]

THRESHOLD_TASKS = {
    155: "Use the exact manifest-admission selector with one unregistered-source mutation; verify the actual admission path rejects without reading files.",
    156: "Use the CSV parser selector with a short-row and extra-column fixture; verify neither is silently padded or truncated.",
    157: "Use the UTC parser selector with a non-Z offset and fractional precision mutation; verify the declared timestamp grammar rejects both.",
    158: "Use the catalog validator selector with an unknown reference and duplicate catalog ID; verify both fail at the validation boundary.",
    159: "Use the event validator selector with a dangling catalog reference and cross-viewer row; verify both fail without mutating inputs.",
    160: "Use the deduplication selector with same-key/different-payload events; verify conflicts remain visible instead of being dropped as replays.",
    161: "Run the exact prior-only parser and data-contract selectors; record every failure and repair only those existing prerequisites.",
    162: "Use the partition selector with events exactly at and just after cutoff; verify half-open membership and unchanged source rows.",
    163: "Use the coverage selector with an empty holdout viewer and an unexpected event; verify independently counted cold-start denominator and rejection.",
    164: "Use the training summary selector while varying holdout rows; verify training metrics stay byte-for-byte unchanged.",
    165: "Use the bundle selector after reordering source rows; verify canonical output bytes and independent expected provenance digest.",
    166: "Use the CLI selector with one malformed input and one valid fixture; verify nonzero fail-closed output then deterministic successful output.",
    167: "Run the prior-only integration selector with a held-out mutation; verify the CLI result does not change and preserve exact evidence.",
    168: "Run the complete existing data-foundation selectors and gate checker; distinguish missing learner evidence from harness failure.",
    169: "Use the positive-pair selector with a repeated pair and a held-out positive; verify distinct training support and no leakage.",
    170: "Use the cutoff selector with start/end equality counterexamples; verify only available-at-cutoff items enter candidates.",
    171: "Use the popularity selector with repeated viewers and a held-out event; verify independently counted distinct training viewers.",
}


def study_day_for_legacy(unit: int) -> int:
    if unit <= 70:
        return unit
    if unit <= 154:
        return unit + 14
    if unit <= 438:
        return unit + 21
    if unit <= 522:
        return unit + 28
    return unit + 42


def legacy_date(unit: int) -> str:
    return (START + timedelta(days=unit - 1)).isoformat()


def current_date(study_day: int) -> str:
    return (START + timedelta(days=study_day - 1)).isoformat()


def sprint_number(study_day: int) -> int:
    return (study_day - 1) // 14 + 1


def unit_calendar_fields(unit: int) -> dict:
    study = study_day_for_legacy(unit)
    return {
        "unit_id": f"EAI-UNIT-{unit:03}",
        "calendar_study_day": study,
        "calendar_sprint": (study - 1) // 14 + 1,
        "date": current_date(study),
    }


def gap_units() -> list[dict]:
    rows = []
    for index, (prefix, title, count, position, anchor, detailed) in enumerate(GAPS):
        sibling_total = sum(g[2] for g in GAPS if g[3] == position and g[4] == anchor)
        preceding_siblings = sum(g[2] for g in GAPS[:index] if g[3] == position and g[4] == anchor)
        if position == "before":
            first = study_day_for_legacy(anchor) - sibling_total + preceding_siblings
        else:
            first = study_day_for_legacy(anchor + 1) - sibling_total + preceding_siblings
        for offset in range(count):
            rows.append({
                "unit_id": f"{prefix}-{offset + 1:02}",
                "title": title,
                "unit_offset": offset + 1,
                "calendar_study_day": first + offset,
                "detailed_now": detailed,
            })
    return rows


def load_detailed_units() -> dict[int, dict]:
    found: dict[int, dict] = {}
    paths = [ROOT / "docs/reboot/manifest.json"]
    paths.extend(sorted((ROOT / "docs/reboot").glob("continuation-*/manifest.json")))
    for path in paths:
        try:
            payload = json.loads(path.read_text(encoding="utf-8"))
        except (OSError, json.JSONDecodeError):
            continue
        days = payload.get("days") if isinstance(payload, dict) else None
        if not isinstance(days, list):
            continue
        for row in days:
            if not isinstance(row, dict):
                continue
            day = row.get("day", row.get("day_number"))
            if isinstance(day, int) and 1 <= day <= DETAIL_THROUGH:
                found[day] = {**found.get(day, {}), **row, "manifest_path": path.relative_to(ROOT).as_posix()}
    if sorted(found) != list(range(1, DETAIL_THROUGH + 1)):
        missing = sorted(set(range(1, DETAIL_THROUGH + 1)) - set(found))
        raise ValueError(f"detailed unit manifests missing days: {missing}")
    return found


def load_owner_cards() -> dict[int, tuple[dict, str]]:
    found: dict[int, tuple[dict, str]] = {}
    paths = [ROOT / "docs/reboot/scrum-owner-manifest.json"]
    paths.extend(sorted((ROOT / "docs/reboot").glob("continuation-*/scrum-owner-manifest.json")))
    for path in paths:
        try:
            payload = json.loads(path.read_text(encoding="utf-8"))
        except (OSError, json.JSONDecodeError):
            continue
        cards = payload.get("cards") if isinstance(payload, dict) else None
        if not isinstance(cards, list):
            continue
        for card in cards:
            if not isinstance(card, dict):
                continue
            day = card.get("day_number", card.get("day", card.get("sequence")))
            if isinstance(day, int) and 1 <= day <= DETAIL_THROUGH:
                found[day] = (card, path.relative_to(ROOT).as_posix())
    if sorted(found) != list(range(1, DETAIL_THROUGH + 1)):
        missing = sorted(set(range(1, DETAIL_THROUGH + 1)) - set(found))
        raise ValueError(f"detailed owner cards missing days: {missing}")
    return found


def known_eai_id(unit: int) -> tuple[str | None, str]:
    # The protected EAI-1–30 / EAI-31–196 ranges are recorded in fde-alignment.json.
    if unit <= 196:
        return f"EAI-{unit}", "documented_eai_range"
    return None, "no_issue_id_recorded_in_repository"


def build_calendar() -> dict:
    gaps = gap_units()
    detailed_units = load_detailed_units()
    by_study = {row["calendar_study_day"]: row for row in gaps}
    rows = []
    for study in range(1, HORIZON + 1):
        gap = by_study.get(study)
        if gap:
            unit_id = gap["unit_id"]
            legacy = None
            title = gap["title"]
            detailed = gap["detailed_now"]
            eai_id = None
            issue_map = "new_story_id_not_assigned_board_write_disabled"
            unit_kind = "inserted_gap"
        else:
            legacy = next((n for n in range(1, LEGACY_HORIZON + 1) if study_day_for_legacy(n) == study), None)
            if legacy is None:
                raise ValueError(f"calendar study day {study} has no mapped unit")
            unit_id = f"EAI-UNIT-{legacy:03}"
            manifest_unit = detailed_units.get(legacy) if legacy <= DETAIL_THROUGH else None
            title = (manifest_unit or {}).get("title", f"Outcome unit {legacy}")
            detailed = legacy <= DETAIL_THROUGH
            eai_id, issue_map = known_eai_id(legacy)
            unit_kind = "existing_engineering_unit"
        dsa = None
        dsa_kind = "none"
        if study <= 150:
            dsa, dsa_kind = study, "daily_problem_day"
        elif study <= 171:
            dsa, dsa_kind = study, "attempt_relative_review_window_day"
        rows.append({
            "calendar_study_day": study,
            "date": current_date(study),
            "calendar_sprint": (study - 1) // 14 + 1,
            "unit_kind": unit_kind,
            "unit_id": unit_id,
            "legacy_engineering_unit_number": legacy,
            "title": title,
            "detail_status": "detailed_contract" if detailed else "outcome_only_not_ticketed",
            "existing_eai_issue_id": eai_id,
            "issue_id_mapping_status": issue_map,
            "dsa_calendar_reference": dsa,
            "dsa_reference_kind": dsa_kind,
        })
    return {
        "schema_version": 1,
        "title": "Study calendar and stable engineering-unit crosswalk",
        "status": "planned_only_not_learner_completion",
        "base_commit": "4a07a57e707d2d1be68f4263d1cbac1e391886c4",
        "start_date": START.isoformat(),
        "legacy_horizon": LEGACY_HORIZON,
        "inserted_study_days": 42,
        "calendar_horizon": HORIZON,
        "end_date": current_date(HORIZON),
        "budget": {
            "before_calendar_day_172": {"engineering_minutes": 120, "dsa_separate": True},
            "from_calendar_day_172": {"engineering_minutes": 165, "shared_minutes": 45},
            "calendar_day_172_date": current_date(172),
            "no_added_overtime": True,
        },
        "insertions": [
            {"prefix": p, "title": t, "days": n, "placement": f"{pos} existing unit {anchor}",
             "first_calendar_study_day": min(x["calendar_study_day"] for x in gaps if x["unit_id"].startswith(p)),
             "last_calendar_study_day": max(x["calendar_study_day"] for x in gaps if x["unit_id"].startswith(p)),
             "detailed_now": detailed}
            for p, t, n, pos, anchor, detailed in GAPS
        ],
        "dsa_boundary": {
            "dsa_repository": "external dsa-prep repository; unchanged",
            "daily_problem_calendar_days": [1, 150],
            "attempt_relative_reviews": ["D+3", "D+7", "D+21"],
            "nominal_final_day": 171,
            "actual_attempt_dates_and_evidence_mutated": False,
        },
        "role_checkpoints": [
            {"calendar_study_day": day, "date": current_date(day), "target": label,
             "status": "planned_evidence_gated_not_completed", "evidence_gate": evidence,
             "application_may_begin_earlier": True, "calendar_completion_grants_role_or_readiness": False}
            for day, label, evidence in MILESTONES
        ] + [{
            "calendar_study_day": None, "date": "2029-10-26", "target": "Professional Staff responsibility review",
            "status": "external_evidence_required_not_completed",
            "evidence_gate": "Sustained external ownership, technical influence, adoption, cross-team results and review; verified professional evidence only.",
            "application_may_begin_earlier": True, "calendar_completion_grants_role_or_readiness": False,
        }],
        "source_id_provenance": "Existing EAI issue IDs are populated only for the documented EAI-1–196 ranges in docs/reboot/fde-alignment.json. Unrecorded existing and all new IDs remain null; no ticket IDs are guessed.",
        "sprint_migration": {
            "detailed_calendar_story_count": 305,
            "full_calendar_sprints": 21,
            "final_partial_sprint_days": 11,
            "calendar_sprint_count": 22,
            "existing_sprint_ids_1_through_21": "reuse chronologically; exact board IDs are resolved by the board writer",
            "calendar_sprint_22_id": None,
            "historical_membership_receipts_mutated": False,
        },
        "calendar": rows,
    }


def build_story_export() -> dict:
    units = load_detailed_units()
    owners = load_owner_cards()
    rows = []
    gaps = {row["calendar_study_day"]: row for row in gap_units() if row["detailed_now"]}
    for study in range(1, 306):
        gap = gaps.get(study)
        if gap:
            rows.append({
                "calendar_study_day": study,
                "date": current_date(study),
                "calendar_sprint": (study - 1) // 14 + 1,
                "unit_id": gap["unit_id"],
                "legacy_engineering_unit_number": None,
                "previous_calendar_study_day": None,
                "previous_calendar_sprint_number": None,
                "title": f"Calendar study day {study} · {gap['title']} · work unit {gap['unit_id']}",
                "eai_issue_id": None,
                "new_eai_issue_id": None,
                "issue_id_mapping_status": "new_story_id_not_assigned_board_write_disabled",
                "source_manifest": "docs/reboot/calendar/insertion-plan.json",
                "contract_path": None,
                "daily_contract_status": "added_in_following_reviewable_stage",
                "owner_card": None,
                "dsa_section": dsa_section(study),
                "previous_dsa_section": {"mode": "none", "copied_content": False},
            })
            continue
        legacy = next((n for n in range(1, DETAIL_THROUGH + 1) if study_day_for_legacy(n) == study), None)
        if legacy is None:
            raise ValueError(f"no detailed unit at study day {study}")
        row = units[legacy]
        card, owner_path = owners[legacy]
        eai_id, issue_map = known_eai_id(legacy)
        title = row.get("title", card.get("title", f"Engineering unit {legacy}"))
        card_copy = dict(card)
        card_copy["engineering_unit_number"] = legacy
        card_copy["legacy_engineering_unit_number"] = legacy
        card_copy["calendar_study_day"] = study
        card_copy["day_number"] = study
        card_copy["date"] = current_date(study)
        if isinstance(card_copy.get("sprint"), str):
            card_copy.setdefault("legacy_sprint_label", card_copy["sprint"])
            card_copy["sprint"] = f"Calendar Sprint {(study - 1) // 14 + 1}"
        card_copy["title"] = f"Calendar study day {study} · engineering unit {legacy} · {title}"
        contract = next((p for p in (ROOT / "docs/reboot").glob("continuation-*/contracts/day-*.md")
                         if p.name == f"day-{legacy}.md"), None)
        if legacy <= 30:
            contract = ROOT / "docs/reboot/contracts" / f"day-{legacy:02}.md"
        rows.append({
            "calendar_study_day": study,
            "date": current_date(study),
            "calendar_sprint": (study - 1) // 14 + 1,
            "unit_id": f"EAI-UNIT-{legacy:03}",
            "legacy_engineering_unit_number": legacy,
            "previous_calendar_study_day": legacy,
            "previous_calendar_sprint_number": (legacy - 1) // 14 + 1,
            "title": f"Calendar study day {study} · engineering unit {legacy} · {title}",
            "eai_issue_id": eai_id,
            "new_eai_issue_id": None,
            "issue_id_mapping_status": issue_map,
            "source_manifest": row["manifest_path"],
            "source_owner_export": owner_path,
            "contract_path": contract.relative_to(ROOT).as_posix() if contract and contract.exists() else None,
            "daily_contract_status": "existing_assignment_preserved",
            "owner_card": card_copy,
            "dsa_section": dsa_section(study),
            "previous_dsa_section": dsa_section(legacy),
            "sprint_membership_migration": {
                "previous_sprint_number": (legacy - 1) // 14 + 1,
                "calendar_sprint_number": (study - 1) // 14 + 1,
                "historical_membership_preserved": True,
                "board_membership_update_required": (legacy - 1) // 14 != (study - 1) // 14,
            },
        })
    if len(rows) != 305 or [row["calendar_study_day"] for row in rows] != list(range(1, 306)):
        raise ValueError("the detailed story export must contain exactly study days 1–305")
    return {
        "schema_version": 1,
        "title": "305 detailed chronological EAI Story export",
        "status": "planning_export_only_no_board_writes",
        "board_writes": False,
        "story_count": len(rows),
        "dsa_content_copied": False,
        "dsa_note": "The story’s DSA section refers to the corresponding calendar study day; all actual assignments, attempts and review events remain only in the separate DSA repository.",
        "new_issue_ids_assigned": False,
        "stories": rows,
    }


def dsa_section(study: int) -> dict:
    if study <= 150:
        return {"mode": "external_daily_problem", "dsa_calendar_day": study,
                "source": "separate dsa-prep repository", "copied_content": False}
    if study <= 171:
        return {"mode": "attempt_relative_review_window", "dsa_calendar_day": study,
                "reviews": ["D+3", "D+7", "D+21"], "source": "separate dsa-prep repository", "copied_content": False}
    return {"mode": "none", "copied_content": False}


def render() -> dict[str, str]:
    return {
        "study-calendar.json": json.dumps(build_calendar(), indent=2, ensure_ascii=False) + "\n",
        "story-export-1-305.json": json.dumps(build_story_export(), indent=2, ensure_ascii=False) + "\n",
    }


def migrate_assignment_json(path: Path, payload):
    changed = False

    def walk(node):
        nonlocal changed
        if isinstance(node, list):
            return [walk(item) for item in node]
        if not isinstance(node, dict):
            return node
        row = {key: (value if key.startswith("historical_") else walk(value))
               for key, value in node.items()}
        unit = row.get("day", row.get("day_number"))
        if isinstance(unit, int) and 71 <= unit <= LEGACY_HORIZON and isinstance(row.get("date"), str):
            fields = unit_calendar_fields(unit)
            for key, value in fields.items():
                if row.get(key) != value:
                    row[key] = value
                    changed = True
        if isinstance(unit, int) and 155 <= unit <= 171:
            budget_ext = row.get("calendar_budget_transition")
            if budget_ext is None:
                budget_ext = {
                    "old_calendar_engineering_minutes": 120,
                    "new_calendar_engineering_minutes": 165,
                    "new_shared_minutes": 45,
                    "engineering_allocation_minutes": 45,
                    "shared_block_reclassified_from_existing_interview_practice": True,
                    "extra_practice_purpose": "bounded prerequisite recovery, adjacent integration, independent negative controls, or observation only; no new feature scope",
                }
                row["calendar_budget_transition"] = budget_ext
                changed = True
            engineering = row.get("engineering")
            if isinstance(engineering, dict):
                allocations = engineering.get("allocation_minutes")
                if isinstance(allocations, dict):
                    prior = dict(allocations)
                    study_key = "concept_or_course" if "concept_or_course" in allocations else "study"
                    practice_key = "implementation_and_testing" if "implementation_and_testing" in allocations else "practice_and_test"
                    revised = {**allocations, study_key: 25, practice_key: 130, "evidence": 10}
                    if engineering.get("budget_minutes") != 165 or revised != allocations:
                        engineering.setdefault("historical_120_minute_allocation", prior)
                        engineering["budget_minutes"] = 165
                        engineering["allocation_minutes"] = revised
                        scope = engineering.get("scope", "")
                        if "Calendar budget transition:" not in scope:
                            engineering["scope"] = (scope.rstrip() + " Calendar budget transition: use the added 45 engineering minutes plus the 45 minutes released by moving the daily question-bank session to the shared block for bounded remediation, adjacent integration, negative controls, or observation; do not add feature scope.").strip()
                        changed = True
                command = engineering.get("acceptance_commands") or [engineering.get("proposed_acceptance_command")]
                command = " && ".join(value for value in command if isinstance(value, str) and value)
                transition = {
                    "category": "bounded_recovery_integration_negative_control_or_observation",
                    "task": THRESHOLD_TASKS[unit],
                    "command": command,
                    "evidence": "Record exact command and result, the independent expected counterexample, whether the learner TODO remains, and the named blocker if any.",
                    "additional_minutes": 45,
                    "new_feature_scope": False,
                }
                if row.get("calendar_transition_task") != transition:
                    row["calendar_transition_task"] = transition
                    changed = True
            if isinstance(row.get("budget_minutes"), int) and row["budget_minutes"] == 120:
                row.setdefault("historical_budget_minutes", 120)
                row["budget_minutes"] = 165
                changed = True
            if row.get("shared_interview_block") is None:
                qid = None
                for key in ("interview_assignment", "interview_session"):
                    value = engineering.get(key) if isinstance(engineering, dict) else row.get(key)
                    if isinstance(value, dict):
                        qid = value.get("question_id")
                        if qid:
                            break
                if not qid and isinstance(engineering, dict):
                    for value in engineering.values():
                        if isinstance(value, dict) and value.get("question_id"):
                            qid = value["question_id"]
                            break
                if not qid:
                    qid = f"Q{(unit - 155) % 36 + 1:02}"
                question_text = (ROOT / "docs/reboot/interview/question-bank.md").read_text(encoding="utf-8")
                match = re.search(rf"\*\*{re.escape(qid)} — ([^*]+)\*\*", question_text)
                question_title = match.group(1).strip().rstrip(".") if match else "Assigned question-bank prompt"
                moved = None
                if isinstance(engineering, dict):
                    moved = engineering.pop("interview_assignment", None) or engineering.pop("interview_session", None)
                    if isinstance(moved, dict):
                        moved = dict(moved)
                        moved["relocated_to_shared_block"] = True
                    engineering.pop("interview_question_bank", None)
                    engineering.pop("interview_practice", None)
                top_level_practice = row.get("interview_practice")
                if isinstance(top_level_practice, dict):
                    top_level_practice["relocated_to_shared_block"] = True
                    top_level_practice["outside_engineering_budget"] = True
                    moved = moved or top_level_practice
                row["shared_interview_block"] = {
                    "minutes": 45,
                    "mode": "separate_system_design_question_bank_session",
                    "question_id": qid,
                    "question_title": question_title,
                    "question_bank": "docs/reboot/interview/question-bank.md",
                    "task": "Practice this question only in the interview question bank; record an independent answer before hints. No capstone design decision or proposal.",
                    "migrated_existing_assignment": moved,
                    "inside_engineering_budget": False,
                }
                changed = True
        card_unit = row.get("day_number")
        if (isinstance(card_unit, int) and 71 <= card_unit <= DETAIL_THROUGH
                and isinstance(row.get("engineering"), dict)
                and isinstance(row.get("title"), str)):
            fields = unit_calendar_fields(card_unit)
            for key, value in {
                "engineering_unit_number": card_unit,
                "legacy_engineering_unit_number": card_unit,
                "calendar_study_day": fields["calendar_study_day"],
                "calendar_sprint": fields["calendar_sprint"],
            }.items():
                if row.get(key) != value:
                    row[key] = value
                    changed = True
            old_sprint = row.get("sprint")
            if isinstance(old_sprint, str):
                if row.get("legacy_sprint_label") != old_sprint:
                    row["legacy_sprint_label"] = old_sprint
                    changed = True
                new_sprint = f"Sprint {fields['calendar_sprint']}"
                if old_sprint != new_sprint:
                    row["sprint"] = new_sprint
                    changed = True
            original_title = row["title"]
            if not original_title.startswith("Calendar study day "):
                row["title"] = (f"Calendar study day {fields['calendar_study_day']} · engineering unit "
                                f"{card_unit} · {original_title}")
                changed = True
        # Preserve original issue-window/unit boundaries while adding their current calendar range.
        first = row.get("first_day")
        last = row.get("last_day")
        if isinstance(first, int) and isinstance(last, int) and 1 <= first <= last <= LEGACY_HORIZON:
            first_study, last_study = study_day_for_legacy(first), study_day_for_legacy(last)
            for key, value in {
                "calendar_first_study_day": first_study,
                "calendar_last_study_day": last_study,
                "start_date": current_date(first_study),
                "end_date": current_date(last_study),
            }.items():
                if row.get(key) != value:
                    row[key] = value
                    changed = True
        # The top-level planned window is derived from its preserved unit IDs.
        day_rows = row.get("days")
        if isinstance(day_rows, list) and day_rows:
            unit_rows = [item.get("day", item.get("day_number")) for item in day_rows if isinstance(item, dict)]
            unit_rows = [n for n in unit_rows if isinstance(n, int) and 71 <= n <= LEGACY_HORIZON]
            if unit_rows:
                a, b = min(unit_rows), max(unit_rows)
                if isinstance(row.get("date_range"), dict):
                    date_range = dict(row["date_range"])
                    for key, value in {"start": current_date(study_day_for_legacy(a)),
                                       "end": current_date(study_day_for_legacy(b))}.items():
                        if date_range.get(key) != value:
                            date_range[key] = value
                            changed = True
                    row["date_range"] = date_range
                if isinstance(row.get("start_date"), str) and isinstance(row.get("end_date"), str):
                    for key, value in {"start_date": current_date(study_day_for_legacy(a)),
                                       "end_date": current_date(study_day_for_legacy(b))}.items():
                        if row.get(key) != value:
                            row[key] = value
                            changed = True
                for start_key, end_key in (("effective_start", "effective_end"),
                                           ("window_start", "window_end")):
                    if isinstance(row.get(start_key), str) and isinstance(row.get(end_key), str):
                        for key, value in {
                            start_key: current_date(study_day_for_legacy(a)),
                            end_key: current_date(study_day_for_legacy(b)),
                        }.items():
                            if row.get(key) != value:
                                row[key] = value
                                changed = True
                # Active planned sprint metadata follows the calendar, while its old
                # sprint assignment remains available as historical provenance.
                sprints = row.get("sprints")
                if isinstance(sprints, list) and len(sprints) == 1 and isinstance(sprints[0], dict):
                    old_sprint = sprints[0]
                    number = sprint_number(study_day_for_legacy(a))
                    revised = {**old_sprint,
                        "name": f"Sprint {number}",
                        "calendar_sprint_number": number,
                        "calendar_dates": f"{current_date((number - 1) * 14 + 1)}..{current_date(min(number * 14, HORIZON))}",
                        "existing_sprint_id_resolution": "reuse_existing_sprint_by_chronological_ordinal" if number <= 21 else "new_sprint_id_unassigned_no_board_write",
                    }
                    if revised != old_sprint:
                        row.setdefault("historical_sprints", sprints)
                        row["sprints"] = [revised]
                        changed = True
                boundaries = row.get("sprint_boundaries")
                if isinstance(boundaries, list):
                    revised_boundaries = []
                    for boundary in boundaries:
                        if not isinstance(boundary, dict):
                            revised_boundaries.append(boundary)
                            continue
                        first_unit = boundary.get("first_day")
                        last_unit = boundary.get("last_day")
                        if not isinstance(first_unit, int) or not isinstance(last_unit, int):
                            revised_boundaries.append(boundary)
                            continue
                        number = sprint_number(study_day_for_legacy(first_unit))
                        first_study = study_day_for_legacy(first_unit)
                        last_study = study_day_for_legacy(last_unit)
                        start_study = (number - 1) * 14 + 1
                        end_study = min(number * 14, HORIZON)
                        revised_boundaries.append({**boundary,
                            "calendar_sprint_number": number,
                            "calendar_first_study_day": max(first_study, start_study),
                            "calendar_last_study_day": min(last_study, end_study),
                            "calendar_start_date": current_date(start_study),
                            "calendar_end_date": current_date(end_study),
                            "existing_sprint_id_resolution": "reuse_existing_sprint_by_chronological_ordinal" if number <= 21 else "new_sprint_id_unassigned_no_board_write",
                        })
                    if revised_boundaries != boundaries:
                        row.setdefault("historical_sprint_boundaries", boundaries)
                        row["sprint_boundaries"] = revised_boundaries
        return row

    updated = walk(payload)
    if isinstance(updated, dict):
        for collection_name, active_name, history_name in (
            ("cards", "sprint_boundaries", "historical_sprint_boundaries"),
            ("days", "sprints", "historical_sprints"),
        ):
            items = updated.get(collection_name)
            if not isinstance(items, list):
                continue
            groups: dict[int, list[dict]] = {}
            for item in items:
                if not isinstance(item, dict):
                    continue
                study = item.get("calendar_study_day")
                if isinstance(study, int) and 1 <= study <= HORIZON:
                    groups.setdefault(sprint_number(study), []).append(item)
            if not groups:
                continue
            prior = updated.get(active_name)
            current_mapping_present = isinstance(prior, list) and all(
                isinstance(item, dict) and "calendar_study_days_in_this_batch" in item for item in prior
            )
            if prior and history_name not in updated and not current_mapping_present:
                updated[history_name] = prior
                changed = True
            active = []
            for number, members in sorted(groups.items()):
                first, last = (number - 1) * 14 + 1, min(number * 14, 305)
                member_days = sorted(item["calendar_study_day"] for item in members)
                dates = f"{current_date(first)}..{current_date(last)}"
                common = {
                    "name": f"Sprint {number}",
                    "calendar_sprint_number": number,
                    "calendar_study_days_in_this_batch": member_days,
                    "calendar_dates": dates,
                    "existing_sprint_id_resolution": "reuse_existing_sprint_by_chronological_ordinal" if number <= 21 else "new_sprint_id_unassigned_no_board_write",
                }
                if collection_name == "days":
                    active.append({**common, "batch_story_count": len(members),
                                   "sprint_total_after_batch": 14 if last - first == 13 else last-first+1})
                else:
                    active.append({**common, "batch_story_count": len(members),
                                   "sprint_story_count_after_batch": 14 if last - first == 13 else last-first+1})
            if updated.get(active_name) != active:
                updated[active_name] = active
                changed = True
    return updated, changed


def migrate_assignment_markdown(path: Path) -> bool:
    match = re.search(r"/contracts/day-(\d+)\.md$", path.as_posix())
    if not match:
        return False
    unit = int(match.group(1))
    if not 71 <= unit <= 284:
        return False
    text = path.read_text(encoding="utf-8")
    lines = text.splitlines()
    if not lines:
        return False
    old = lines[0]
    fields = unit_calendar_fields(unit)
    if re.match(rf"# Day {unit}(?:\s|[:·—-])", old):
        title = re.sub(rf"^# Day {unit}\s*(?:[·—:-]\s*)?", "", old)
        title = re.sub(r"^\d{4}-\d{2}-\d{2}\s*(?:[·—:-]\s*)?", "", title)
        lines[0] = (f"# Engineering unit Day {unit} · Calendar study day "
                    f"{fields['calendar_study_day']} · {fields['date']}: {title}")
    elif not old.startswith(f"# Engineering unit Day {unit} · Calendar study day {fields['calendar_study_day']} · {fields['date']}:"):
        return False
    if 155 <= unit <= 171 and "## Calendar budget transition" not in text:
        shared = None
        command = None
        for manifest_path in [ROOT / "docs/reboot/manifest.json", *sorted((ROOT / "docs/reboot").glob("continuation-*/manifest.json"))]:
            try:
                manifest = json.loads(manifest_path.read_text(encoding="utf-8"))
            except (OSError, json.JSONDecodeError):
                continue
            found = next((item for item in manifest.get("days", []) if item.get("day") == unit), None)
            if found:
                shared = found.get("shared_interview_block")
                eng = found.get("engineering", {})
                commands = eng.get("acceptance_commands") or [eng.get("proposed_acceptance_command")]
                command = " && ".join(value for value in commands if isinstance(value, str) and value)
                break
        qid = shared.get("question_id", "assigned question-bank prompt") if isinstance(shared, dict) else "assigned question-bank prompt"
        lines.extend([
            "",
            "## Calendar budget transition",
            "",
            f"This stable engineering unit is scheduled on calendar study day {fields['calendar_study_day']} ({fields['date']}). Its allocation is **165 engineering minutes** (25 study, 130 implementation/testing, 10 evidence) plus **one separate shared 45-minute question-bank session ({qid})**. The prior interview practice is moved out of the engineering block. Transition task: {THRESHOLD_TASKS[unit]} Run `{command or 'the exact acceptance selector named in the manifest'}`. Use the additional capacity for bounded recovery/integration/negative controls only; do not add feature scope or double-count interview practice. The date does not imply prerequisite evidence passed.",
        ])
    rendered = "\n".join(lines) + ("\n" if text.endswith("\n") else "")
    if rendered != text:
        path.write_text(rendered, encoding="utf-8")
        return True
    return False


def migrate_assignments() -> None:
    targets = []
    for path in (ROOT / "docs/reboot").glob("continuation-*/**/*.json"):
        if path.name not in {"manifest.json", "scrum-owner-manifest.json", "plan-source.json",
                             "transfer-plan.json"} and "transfer-plan" not in path.name:
            continue
        window_match = re.search(r"continuation-(\d+)-(\d+)", path.as_posix())
        if not window_match or int(window_match.group(1)) < 71:
            continue
        targets.append(path)
    for path in sorted(set(targets)):
        if not path.exists():
            continue
        try:
            payload = json.loads(path.read_text(encoding="utf-8"))
        except json.JSONDecodeError:
            continue
        updated, changed = migrate_assignment_json(path, payload)
        if changed:
            path.write_text(json.dumps(updated, indent=2, ensure_ascii=False) + "\n", encoding="utf-8")
    for path in (ROOT / "docs/reboot").glob("continuation-*/contracts/day-*.md"):
        migrate_assignment_markdown(path)

    # Make window-level labels show both stable engineering units and their current dates.
    for path in (ROOT / "docs/reboot").glob("continuation-*/README.md"):
        if path.parent.name == "continuation-271-732":
            continue
        match = re.search(r"continuation-(\d+)-(\d+)", path.parent.name)
        if not match:
            continue
        first, last = map(int, match.groups())
        if first < 71 or last > LEGACY_HORIZON:
            continue
        study_first, study_last = study_day_for_legacy(first), study_day_for_legacy(last)
        text = path.read_text(encoding="utf-8")
        lines = text.splitlines()
        if not lines:
            continue
        if lines[0].startswith("# "):
            original = lines[0]
            try:
                import subprocess
                base_text = subprocess.run(["git", "show", f"HEAD:{path.relative_to(ROOT).as_posix()}"],
                                           cwd=ROOT, capture_output=True, text=True, check=True).stdout
                original = base_text.splitlines()[0]
            except (OSError, subprocess.CalledProcessError, IndexError):
                pass
            title = original.removeprefix("# ").strip()
            title = re.sub(r"^Days?\s+\d+[–-]\d+\s*[—–:-]\s*", "", title, flags=re.I)
            title = re.sub(r"\s*[—–-]\s*Days?\s+\d+[–-]\d+\s*$", "", title, flags=re.I)
            lines[0] = (f"# Engineering units {first}–{last} · calendar study days "
                        f"{study_first}–{study_last} · {current_date(study_first)}–{current_date(study_last)}")
            if title:
                lines[0] += f" — {title}"
        info = (f"Calendar mapping: stable engineering unit IDs {first}–{last} occupy study days "
                f"{study_first}–{study_last} ({current_date(study_first)} through {current_date(study_last)}).")
        for index, line in enumerate(lines[1:], start=1):
            if re.match(r"^\s*\*{0,2}(?:planning )?dates\*{0,2}:", line, flags=re.I):
                lines[index] = info
                break
        remainder = [line for line in lines[1:] if not line.startswith("Calendar mapping:")]
        while remainder and remainder[0] == "":
            remainder.pop(0)
        lines = [lines[0], "", info, ""] + remainder
        path.write_text("\n".join(lines) + ("\n" if text.endswith("\n") else ""), encoding="utf-8")


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--check", action="store_true")
    parser.add_argument("--migrate-assignment-dates", action="store_true")
    args = parser.parse_args()
    if args.migrate_assignment_dates:
        migrate_assignments()
    mismatch = False
    for name, content in render().items():
        path = OUT / name
        if args.check:
            actual = path.read_text(encoding="utf-8") if path.exists() else None
            if actual != content:
                print(f"stale generated file: {path.relative_to(ROOT)}")
                mismatch = True
        else:
            path.parent.mkdir(parents=True, exist_ok=True)
            path.write_text(content, encoding="utf-8")
    return 1 if mismatch else 0


if __name__ == "__main__":
    raise SystemExit(main())
