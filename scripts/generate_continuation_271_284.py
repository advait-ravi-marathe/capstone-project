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
    assert plan["start_date"] == "2027-07-21" and plan["end_date"] == "2027-08-03"
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
    paths = [
        "labs/runtime_sql/learner_analysis.py",
        "labs/runtime_sql/reference_analysis.py",
        "labs/runtime_sql/tests/test_acceptance.py",
        "labs/runtime_sql/schema.sql",
        "labs/runtime_sql/fixtures/observation.json",
        "labs/runtime_sql/ProfilingWork.java",
        "scripts/capture_runtime_sql_fixture.sh",
        "scripts/check_continuation_239_entry_gate.py",
        "scripts/check_continuation_253_entry_gate.py",
        "scripts/check_streamforge_169_182_entry_gate.py",
    ]
    study_source_text = f"{source['title']} — {', '.join(source['sections'])} ({source['url']})"
    return {
        "study": {
            "minutes": 35,
            "task": "Read the assigned official section; record three source-backed notes: what is measured, assumptions, and which conclusion the source does not support.",
            "source_paths": [], "source_keys": [source["key"]],
            "source_sections": {source["key"]: source["sections"]},
            "command": f"Open {source['url']} and read {', '.join(source['sections'])}.",
            "evidence": "Three bounded notes with exact page title/heading; distinguish source fact from inference and record version/date.",
            "source": study_source_text,
        },
        "implementation": {
            "minutes": 82,
            "task": day["task"],
            "source_paths": paths,
            "source_keys": [day["study_source"]],
            "source_sections": {day["study_source"]: source["sections"]},
            "command": day["command"],
            "evidence": day["evidence"],
            "source": "; ".join(paths) + "; " + study_source_text,
        },
        "reserve": {
            "minutes": 33,
            "task": "Check the three authoritative prior gates. If one contains a genuine unmet learner prerequisite, perform only its next named bounded recovery action and retain the exact raw output. If none is unmet, use this time for the sprint-specific repeatability/hardening named in today's contract; do not create a fake evidence record.",
            "source_paths": ["scripts/check_continuation_239_entry_gate.py", "scripts/check_continuation_253_entry_gate.py", "scripts/check_streamforge_169_182_entry_gate.py"],
            "source_keys": [day["study_source"]],
            "source_sections": {day["study_source"]: source["sections"]},
            "command": plan["gate_command"],
            "evidence": "Actual gate output and either the named recovery diff/result or assigned repeatability observation; absent evidence stays absent.",
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
    lines = [f'<a id="day-{day["day"]}"></a>', f'# Day {day["day"]}: {day["title"]}', "",
             f"Date: {day['date']}", "",
             "**Assistant-supplied architecture — what and why:** Use one loopback-only disposable PostgreSQL 18.6 workload, preserve its immutable synthetic facts, compare complete result rows to independent expected identities, and analyze observed plan fields separately. A small local Java process provides a bounded JFR rehearsal. The learner implements the analyzer and tests; planner shape and elapsed time are observations, not correctness rules. No design proposal or post-hoc architecture justification is assigned.", "",
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
        study = {"key": src["key"], "title": src["title"], "url": src["url"],
                 "sections": src["sections"], "provider": src["provider"],
                 "use": src["use"], "verified_at": src["verified_at"]}
        rowtasks = {}
        for k, task in tasks.items():
            rowtasks[LABELS[k]] = {**task, "source_keys": task["source_keys"],
                                   "source_sections": task["source_sections"]}
        row = {"day": day["day"], "date": day["date"], "title": day["title"],
               "learner_seam": "runtime_sql_observation_analysis", "signature": "; ".join(tasks["implementation"]["source_paths"]),
               "selectors": list(dict.fromkeys(t["command"] for t in tasks.values())),
               "depends_on_days": [], "recovery_day": False,
               "study_refs": [src["url"]], "study_guidance": [study],
               "engineering_architecture": {"decision": "Assistant supplies the disposable workload → actual local PostgreSQL plan/result capture → independent analyzer → bounded evidence report architecture. The learner implements/tests only assigned seams.",
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
        cards.append({"day_number": day["day"], "date": day["date"], "title": day["title"],
                      "learner_seam": row["learner_seam"], "signature": row["signature"],
                      "selectors": row["selectors"], "prerequisite_days": [], "recovery_day": False,
                      "engineering": {"minutes": 165, "allocation": alloc},
                      "budget_minutes": {"engineering": 165, "shared_interview_or_remaining_reviews": 45, "total": 210},
                      "activities": rowtasks, "study_guidance": [study],
                      "shared_interview_block": row["shared_interview_block"],
                      "assistant_architecture_note": row["engineering_architecture"],
                      "status": "unobserved_not_implied_by_scaffold"})
        owners["cards"].append(cards[-1])
        contract_files[f"contracts/day-{day['day']}.md"] = markdown_contract(plan, day, tasks)
    total = {LABELS[k]: sum(task_rows(plan, d)[k]["minutes"] for d in plan["days"]) for k in CATEGORIES}
    manifest = {"title": plan["title"], "status": plan["status"], "branch": plan["branch"],
                "base_commit": plan["base_commit"], "date_range": {"start": plan["start_date"], "end": plan["end_date"]},
                "engineering_minutes_per_day": 165, "shared_minutes_per_day": 45,
                "engineering_minutes_total": 2310, "shared_minutes_total": 630,
                "daily_allocation": plan["daily_budget"], "sprint_allocation_minutes": total,
                "remediation_reserve_minutes": 462, "learner_completion_awarded": False,
                "sources": plan["sources"], "separate_learning_repository": plan["separate_learning_repository"], "days": manifest_days}
    transfer = {"title": plan["title"], "status": plan["status"], "budget":
                {"engineering_minutes": 2310, "shared_minutes": 630, "reserve_minutes": 462, "days": 14},
                "days": manifest_days}
    sources_doc = {"verified_at": plan["sources_verified_at"], "recheck_before_study": True,
                   "scope": "Official source titles/headings verified 2026-10-24; recheck exact version and heading on the planned study date. No new course or invented lecture.",
                   "sources": plan["sources"]}
    architecture = ["# Assistant-supplied architecture notes", "",
        "The assistant selects and explains the capstone architecture; the learner implements and tests the assigned seams. System-design practice occurs only in separate interview question-bank sessions.", "",
        "The lab architecture is one immutable synthetic workload, one loopback-only disposable PostgreSQL instance, an exact row readback and EXPLAIN JSON capture, a pure independent analyzer, and a report separating facts from hypotheses. JFR is a separate bounded local Java rehearsal. No public endpoint, cloud provider, credentials, customer data or deployment.", ""]
    for d in plan["days"]:
        architecture.extend([f'<a id="day-{d["day"]}"></a>', f"## Day {d['day']} — {d['title']}", "",
                              "Assistant-selected task boundary: preserve actual rows and plan observations; compare against independently fixed facts; interpret measurements with limitations. No learner design proposal.", ""])
    owner_manifest = {"title": plan["title"], "status": plan["status"], "branch": plan["branch"],
                      "base_commit": plan["base_commit"], "board_writes": False, "story_count": 14,
                      "shared_block_count": 14, "separate_learning_repository": plan["separate_learning_repository"], "cards": owners["cards"]}
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
