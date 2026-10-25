#!/usr/bin/env python3
"""Render synchronized Days 271–284 contracts from the canonical plan source."""
from __future__ import annotations
import json
import os
import re
import sys
import tempfile
from datetime import date, timedelta
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
OUT = ROOT / "docs/reboot/continuation-271-284"
SRC = OUT / "plan-source.json"
CATEGORIES = ("study", "implementation", "reserve", "evidence")
LABELS = {
    "study": "study",
    "implementation": "project_implementation_tests",
    "reserve": "remediation_integration_observation_reserve",
    "evidence": "evidence",
}
DISPLAY = {
    "study": "Official source study",
    "implementation": "Runtime/SQL implementation and tests",
    "reserve": "Recovery/integration/observation reserve",
    "evidence": "Evidence",
}


def read_plan():
    plan = json.loads(SRC.read_text(encoding="utf-8"))
    assert plan["branch"] == "work/eai-sprint2-onward"
    assert plan["start_date"] == "2027-08-13" and plan["end_date"] == "2027-08-26"
    assert [d["day"] for d in plan["days"]] == list(range(271, 285))
    assert [d["date"] for d in plan["days"]] == [
        (date.fromisoformat(plan["start_date"]) + timedelta(days=i)).isoformat()
        for i in range(14)
    ]
    assert len({s["key"] for s in plan["sources"]}) == len(plan["sources"])
    assert all(s["verified_at"] == "2026-10-24" for s in plan["sources"])
    return plan


def sources(plan):
    return {s["key"]: s for s in plan["sources"]}


def task_rows(plan, day):
    source = sources(plan)[day["study_source"]]
    sections = day["study_sections"]
    paths = [
        "labs/runtime_sql/learner_analysis.py",
        "labs/runtime_sql/reference_analysis.py",
        "labs/runtime_sql/tests/test_acceptance.py",
        "labs/runtime_sql/schema.sql",
        "labs/runtime_sql/fixtures/observation.json",
        "labs/runtime_sql/ProfilingWork.java",
        "scripts/capture_runtime_sql_fixture.sh",
        "scripts/compare_runtime_sql_runs.py",
        "scripts/report_runtime_sql_skew.py",
        "scripts/summarize_runtime_sql_runs.py",
        "scripts/test_jfr_runtime_sql.py",
        "scripts/check_continuation_239_entry_gate.py",
        "scripts/check_continuation_253_entry_gate.py",
        "scripts/check_streamforge_169_182_entry_gate.py",
    ]
    study_source_text = f"{source['title']} — {', '.join(sections)} ({source['url']})"
    return {
        "study": {
            "minutes": 35,
            "task": day["study_task"],
            "source_paths": [], "source_keys": [source["key"]],
            "source_sections": {source["key"]: sections},
            "command": f"Open {source['url']} and read {', '.join(sections)}.",
            "evidence": day["study_evidence"],
            "source": study_source_text,
        },
        "implementation": {
            "minutes": 82,
            "task": day["task"],
            "source_paths": paths,
            "source_keys": [day["study_source"]],
            "source_sections": {day["study_source"]: sections},
            "command": day["command"],
            "evidence": day["evidence"],
            "source": "; ".join(paths) + "; " + study_source_text,
        },
        "reserve": {
            "minutes": 33,
            "task": "Check the three authoritative prior gates. If a genuine unmet learner prerequisite exists, use this reserve for its next bounded recovery action and retain raw output. Otherwise perform this contract's preassigned fallback action: " + day["reserve_fallback_task"],
            "source_paths": ["scripts/check_continuation_239_entry_gate.py", "scripts/check_continuation_253_entry_gate.py", "scripts/check_streamforge_169_182_entry_gate.py"],
            "source_keys": [day["study_source"]],
            "source_sections": {day["study_source"]: sections},
            "command": plan["gate_command"],
            "evidence": "Actual gate output and either the bounded recovery result or this contract's fallback evidence: " + day["reserve_fallback_evidence"] + ". Absent evidence stays absent.",
            "fallback_command": day["reserve_fallback_command"],
            "fallback_task": day["reserve_fallback_task"],
            "fallback_evidence": day["reserve_fallback_evidence"],
            "source": "authoritative dynamic gates; " + study_source_text,
        },
        "evidence": {
            "minutes": 15,
            "task": "Record actual command, exit status, changed paths, assistance and blocker. Keep 40-hex source commit separate from 64-hex artifact digest.",
            "source_paths": ["labs/runtime_sql/fixtures/observation.json", "labs/runtime_sql/learner_analysis.py"],
            "source_keys": [], "source_sections": {},
            "command": "git rev-parse HEAD; sha256sum labs/runtime_sql/fixtures/observation.json labs/runtime_sql/learner_analysis.py",
            "evidence": "Timestamped learner-owned note with raw command/output, source commit, separate artifact SHA-256, and truthful status; not written by the instructor harness.",
            "source": "local task artifacts and git source identity",
        },
    }


def markdown_contract(plan, day, row):
    source_map = sources(plan)
    calendar_day = day["day"] + 21
    lines = [f'<a id="day-{day["day"]}"></a>',
             f'# Engineering unit Day {day["day"]} · Calendar study day {calendar_day} · {day["date"]}: {day["title"]}', "",
             f"Date: {day['date']}", "",
             f"Stable engineering unit {day['day']} maps to calendar study day {calendar_day}; see the [canonical calendar crosswalk](../../calendar/study-calendar.json).", "",
             f"**Assistant-supplied architecture — what and why:** {day['architecture']} No learner design proposal or post-hoc architecture justification is assigned.", "",
             "## Engineering allocation — 165 minutes", ""]
    for key in CATEGORIES:
        task = row[key]
        links = []
        for path in task["source_paths"]:
            dest = ROOT / path
            rel = os.path.relpath(dest, OUT / "contracts")
            links.append(f"[{path}]({rel})" if dest.is_file() else f"`{path}`")
        for keysource in task["source_keys"]:
            src = source_map[keysource]
            headings = task["source_sections"].get(keysource, src["sections"])
            links.append(f"[{src['title']} — {', '.join(headings)}]({src['url']})")
        source_md = "; ".join(links) or task["source"]
        lines.extend([f"### {DISPLAY[key]} — {task['minutes']} minutes", "",
                      f"**Task:** {task['task']}", f"**Source:** {source_md}",
                      f"**Command/action:** `{task['command']}`", f"**Evidence:** {task['evidence']}", ""])
        if key == "reserve":
            lines[-1:-1] = [f"**Preassigned no-prerequisite fallback:** {task['fallback_task']}",
                            f"**Fallback command:** `{task['fallback_command']}`",
                            f"**Fallback evidence:** {task['fallback_evidence']}", ""]
    lines.extend(["## Shared block — exactly 45 minutes", "",
                  f"**Question-bank practice:** {day['shared_task']}",
                  "**Source:** [separate interview question bank](../../interview/question-bank.md). This block is separate from engineering; no capstone design assignment.", "",
                  "## Gate and status", "",
                  f"Re-evaluate `{plan['gate_command']}` from the authoritative evidence files. The local lab can run without StreamForge eligibility; no gated learner selector may run unless its current prerequisite passes. Missing evidence is not a permanent failure. Learner TODO red is not infrastructure failure or completion. Calendar dates do not override evidence or external authorization. No cloud/model spending, credentials, exam bookings, customer data or deployment.", "",
                  f"Algorithm practice remains solely in the separate [DSA preparation repository]({plan['separate_learning_repository']['algorithm_practice']}); this sprint adds no DSA tasks or tracker.", "",
                  "**Learner status:** unobserved_not_implied_by_scaffold", ""])
    return "\n".join(lines)


def rendered(plan):
    srcs = sources(plan)
    manifest_days, cards, contract_files = [], [], {}
    owners = {"title": plan["title"], "status": plan["status"], "branch": plan["branch"],
              "base_commit": plan["base_commit"], "board_writes": False, "story_count": 14,
              "cards": []}
    for day in plan["days"]:
        tasks = task_rows(plan, day)
        alloc = {LABELS[k]: tasks[k]["minutes"] for k in CATEGORIES}
        assert sum(alloc.values()) == 165
        src = srcs[day["study_source"]]
        sections = day["study_sections"]
        study = {"key": src["key"], "title": src["title"], "url": src["url"],
                 "sections": sections, "provider": src["provider"],
                 "use": src["use"], "verified_at": src["verified_at"]}
        rowtasks = {}
        for k, task in tasks.items():
            rowtasks[LABELS[k]] = {**task, "source_keys": task["source_keys"],
                                   "source_sections": task["source_sections"]}
        calendar_day = day["day"] + 21
        row = {"day": day["day"], "date": day["date"], "title": day["title"],
               "unit_id": f"EAI-UNIT-{day['day']:03}", "engineering_unit_number": day["day"],
               "legacy_engineering_unit_number": day["day"], "calendar_study_day": calendar_day,
               "calendar_sprint": (calendar_day - 1) // 14 + 1,
               "learner_seam": "runtime_sql_observation_analysis", "signature": "; ".join(tasks["implementation"]["source_paths"]),
               "selectors": list(dict.fromkeys(t["command"] for t in tasks.values())),
               "depends_on_days": day["prerequisite_days"], "recovery_day": False,
               "study_refs": [src["url"]], "study_guidance": [study],
               "engineering_architecture": {"decision": day["architecture"],
                   "mode": "assistant_supplied_capstone_architecture; learner_implements_and_tests",
                   "capstone_proposal_required": False, "system_design_scope": "separate_interview_question_bank_only"},
               "budget": {"engineering_minutes": 165, "study": 35, "practice_and_tests": 115,
                          "evidence": 15, "shared_interview_or_remaining_reviews_minutes": 45,
                          "total": 210, "workstream_minutes": alloc},
               "workstream_minutes": alloc, "task_minutes": rowtasks,
               "shared_interview_block": {"minutes": 45, "within_engineering_minutes": False,
                   "mode": "system_design_question_session", "task": day["shared_task"],
                   "source": "docs/reboot/interview/question-bank.md", "allocation_minutes":
                   {"requirements": 5, "constraints": 5, "architecture_or_review": 15, "failure_modes": 10, "tradeoffs_and_next_step": 10}},
               "learner_status": "unobserved_not_implied_by_scaffold"}
        manifest_days.append(row)
        cards.append({"day_number": day["day"], "unit_id": f"EAI-UNIT-{day['day']:03}",
                      "engineering_unit_number": day["day"],
                      "legacy_engineering_unit_number": day["day"], "calendar_study_day": calendar_day,
                      "calendar_sprint": (calendar_day - 1) // 14 + 1, "date": day["date"],
                      "title": f"Calendar study day {calendar_day} · engineering unit {day['day']} · {day['title']}",
                      "learner_seam": row["learner_seam"], "signature": row["signature"],
                      "selectors": row["selectors"], "prerequisite_days": row["depends_on_days"], "recovery_day": False,
                      "engineering": {"minutes": 165, "allocation": alloc},
                      "budget_minutes": {"engineering": 165, "shared_interview_or_remaining_reviews": 45, "total": 210},
                      "activities": rowtasks, "study_guidance": [study],
                      "shared_interview_block": row["shared_interview_block"],
                      "assistant_architecture_note": row["engineering_architecture"],
                      "status": "unobserved_not_implied_by_scaffold"})
        owners["cards"].append(cards[-1])
        contract_files[f"contracts/day-{day['day']}.md"] = markdown_contract(plan, day, tasks)
    total = {LABELS[k]: sum(task_rows(plan, d)[k]["minutes"] for d in plan["days"]) for k in CATEGORIES}
    sprint_groups = {}
    for day in plan["days"]:
        calendar_day = day["day"] + 21
        sprint_no = (calendar_day - 1) // 14 + 1
        sprint_groups.setdefault(sprint_no, []).append(calendar_day)
    manifest_sprints = []
    owner_sprints = []
    for sprint_no, study_days in sorted(sprint_groups.items()):
        sprint_start = (sprint_no - 1) * 14 + 1
        sprint_end = min(sprint_no * 14, 305)
        common = {
            "name": f"Sprint {sprint_no}", "calendar_sprint_number": sprint_no,
            "calendar_study_days_in_this_batch": study_days,
            "calendar_dates": f"{(date(2026,10,26) + timedelta(days=sprint_start - 1)).isoformat()}..{(date(2026,10,26) + timedelta(days=sprint_end - 1)).isoformat()}",
            "batch_story_count": len(study_days),
            "existing_sprint_id_resolution": "reuse_existing_sprint_by_chronological_ordinal" if sprint_no <= 21 else "new_sprint_id_unassigned_no_board_write",
        }
        manifest_sprints.append({**common, "sprint_total_after_batch": sprint_end - sprint_start + 1})
        owner_sprints.append({**common, "sprint_story_count_after_batch": sprint_end - sprint_start + 1})
    manifest = {"title": plan["title"], "status": plan["status"], "branch": plan["branch"],
                "base_commit": plan["base_commit"], "date_range": {"start": plan["start_date"], "end": plan["end_date"]},
                "engineering_minutes_per_day": 165, "shared_minutes_per_day": 45,
                "engineering_minutes_total": 2310, "shared_minutes_total": 630,
                "daily_allocation": plan["daily_budget"], "sprint_allocation_minutes": total,
                "sprints": manifest_sprints,
                "remediation_reserve_minutes": 462, "learner_completion_awarded": False,
                "sources": plan["sources"], "separate_learning_repository": plan["separate_learning_repository"], "days": manifest_days}
    transfer = {"title": plan["title"], "status": plan["status"], "budget":
                {"engineering_minutes": 2310, "shared_minutes": 630, "reserve_minutes": 462, "days": 14},
                "sprints": manifest_sprints, "days": manifest_days}
    sources_doc = {"verified_at": plan["sources_verified_at"], "recheck_before_study": True,
                   "scope": "Official source titles/headings verified 2026-10-24; recheck exact version and heading on the planned study date. No new course or invented lecture.",
                   "sources": plan["sources"]}
    architecture = ["# Assistant-supplied architecture notes", "",
        "The assistant chooses the architecture and supplies concise what/why context. The learner implements and tests assigned seams. System-design practice remains only in separate interview question-bank sessions.", "",
        "The PostgreSQL experiment uses pinned PostgreSQL 18.6, a deterministic synthetic seed, loopback-only disposable instance, EXPLAIN JSON plus exact result readback, and a pure analyzer compared to independent expected facts. Planner shape, cost, buffer counts and elapsed time may vary with cache/statistics/host; correctness is determined by exact rows and analyzer arithmetic, not timing. JFR is a separate bounded 500-million-iteration local CPU workload. Default recording settings avoid adding event volume; sampled events can miss brief work and recording itself has overhead. Neither rehearsal represents service performance, production operation or learner completion.", ""]
    for d in plan["days"]:
        architecture.extend([f'<a id="day-{d["day"]}"></a>', f"## Day {d['day']} — {d['title']}", "",
                              d["architecture"], ""])
    owner_manifest = {"title": plan["title"], "status": plan["status"], "branch": plan["branch"],
                      "base_commit": plan["base_commit"], "board_writes": False, "story_count": 14,
                      "shared_block_count": 14, "sprint_boundaries": owner_sprints,
                      "separate_learning_repository": plan["separate_learning_repository"], "cards": owners["cards"]}
    files = {
        "manifest.json": json.dumps(manifest, indent=2, ensure_ascii=False) + "\n",
        "days-271-284-transfer-plan.json": json.dumps(transfer, indent=2, ensure_ascii=False) + "\n",
        "scrum-owner-manifest.json": json.dumps(owner_manifest, indent=2, ensure_ascii=False) + "\n",
        "verified-sources.json": json.dumps(sources_doc, indent=2, ensure_ascii=False) + "\n",
        "assistant-architecture-notes.md": "\n".join(architecture),
        **contract_files,
    }
    return files


def write_all(files):
    for rel, content in files.items():
        path = OUT / rel
        path.parent.mkdir(parents=True, exist_ok=True)
        with tempfile.NamedTemporaryFile("w", encoding="utf-8", dir=path.parent, delete=False) as f:
            f.write(content)
            tmp = Path(f.name)
        os.replace(tmp, path)


def main():
    plan = read_plan()
    expected = rendered(plan)
    core_only = "--check-core" in sys.argv or "--core" in sys.argv
    if core_only:
        expected.pop("scrum-owner-manifest.json", None)
    if "--check" in sys.argv or "--check-core" in sys.argv:
        stale = [rel for rel, content in expected.items() if not (OUT / rel).is_file() or (OUT / rel).read_text(encoding="utf-8") != content]
        if stale:
            raise SystemExit("stale generated files: " + ", ".join(stale))
        print(f"consistent: {len(expected)} generated files, Days 271–284")
    else:
        write_all(expected)
        print(f"generated {len(plan['days'])} daily contracts and synchronized manifests")


if __name__ == "__main__":
    main()
