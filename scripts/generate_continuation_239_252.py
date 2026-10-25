#!/usr/bin/env python3
"""Generate synchronized Days 239–252 exports, contracts and architecture notes."""
from __future__ import annotations

import json
import os
import tempfile
from datetime import date, timedelta
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
OUT = ROOT / "docs/reboot/continuation-239-252"
SOURCE = OUT / "plan-source.json"
GATE = "python3 scripts/check_continuation_239_entry_gate.py"
EXPECTED = {"project_implementation_tests": 840, "ai_workflow_implementation_evaluation": 660,
            "certification_labs_questions": 110, "ai_concept_source_study": 210,
            "certification_domain_study": 150, "core_systems_security_cloud_study": 130,
            "evidence": 210}
NAMES = {"project": "project_implementation_tests", "ai": "ai_workflow_implementation_evaluation",
         "cert": "certification_labs_questions", "ai_study": "ai_concept_source_study",
         "cert_study": "certification_domain_study", "core_study": "core_systems_security_cloud_study",
         "evidence": "evidence"}
QUESTION_TITLES = {"Q08": "API gateway: safe error contract", "Q14": "Booking: retry after a lost response",
                   "Q20": "Payments: reconcile timeout", "Q16": "Messaging: ordering and replay"}


def atomic_write(path: Path, content: str) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    with tempfile.NamedTemporaryFile("w", encoding="utf-8", dir=path.parent, delete=False) as handle:
        handle.write(content)
        temp = Path(handle.name)
    try:
        os.replace(temp, path)
    finally:
        temp.unlink(missing_ok=True)


def refs_for(task: dict, plan: dict) -> list[dict]:
    source_map = {item["key"]: item for item in plan["sources"]}
    selected: dict[str, list[str]] = {}
    for activity in task["activities"].values():
        for key in activity["source_keys"]:
            headings = activity.get("source_sections", {}).get(key, source_map[key]["sections"][:1])
            selected.setdefault(key, [])
            selected[key].extend(section for section in headings if section not in selected[key])
    refs = []
    for key, headings in selected.items():
        src = source_map[key]
        refs.append({**src, "sections": headings})
    return refs


def source_text(keys: list[str], section_by_key: dict[str, list[str]]) -> str:
    chunks = []
    for key in keys:
        headings = section_by_key.get(key, [])
        src = SOURCE_MAP[key]
        chunks.append(f"{src['title']} — {', '.join(headings)} ({src['url']})" if headings
                      else f"{src['title']} ({src['url']})")
    return "; ".join(chunks)


def generate() -> dict:
    plan = json.loads(SOURCE.read_text(encoding="utf-8"))
    tasks = plan["tasks"]
    if len(tasks) != 14 or [task["day"] for task in tasks] != list(range(239, 253)):
        raise ValueError("plan must contain exactly Days 239–252")
    totals = {key: sum(task[key] for task in tasks)
              for key in ("project", "ai", "cert", "ai_study", "cert_study", "core_study")}
    expected = {"project": 840, "ai": 660, "cert": 110, "ai_study": 210, "cert_study": 150, "core_study": 130}
    if totals != expected:
        raise ValueError(f"allocation mismatch: {totals}")
    global SOURCE_MAP
    SOURCE_MAP = {item["key"]: item for item in plan["sources"]}
    day_rows, cards, notes = [], [], []
    start = date.fromisoformat(plan["start_date"])
    for index, task in enumerate(tasks):
        day = task["day"]
        day_date = start + timedelta(days=index)
        refs = refs_for(task, plan)
        workstream = {NAMES[key]: task[key] for key in ("project", "ai", "cert", "ai_study", "cert_study", "core_study")}
        workstream["evidence"] = 15
        if sum(workstream.values()) != 165 or sum(task[k] for k in ("ai_study", "cert_study", "core_study")) != 35:
            raise ValueError(f"Day {day} does not fit the 165/35/115/15 budget")
        if task["project"] + task["ai"] + task["cert"] != 115:
            raise ValueError(f"Day {day} practice minutes do not equal 115")
        sections = {item["key"]: item["sections"] for item in refs}
        activity_rows = {}
        for category, activity in task["activities"].items():
            docs = source_text(activity["source_keys"], activity.get("source_sections") or sections)
            paths = "; ".join(activity.get("source_paths", []))
            source_desc = "; ".join(x for x in (paths, docs) if x) or "No external source allocated; use the named task/evidence record."
            activity_rows[category] = {**activity, "source": source_desc}
        if task["shared"] == "question":
            shared = {"minutes": 45, "within_engineering_minutes": False,
                      "mode": "system_design_question_session", "question_id": task["question_id"],
                      "question_title": QUESTION_TITLES[task["question_id"]], "task": task["shared_task"],
                      "source": "docs/reboot/interview/question-bank.md"}
        elif task["shared"] == "cert":
            shared = {"minutes": 45, "within_engineering_minutes": False,
                      "mode": "certification_retrieval_review", "question_id": None,
                      "question_title": None, "task": task["shared_task"],
                      "source": f"{task['cert_source_title']} — {task['cert_source_section']}"}
        else:
            shared = {"minutes": 45, "within_engineering_minutes": False,
                      "mode": "unfinished_review", "question_id": None, "question_title": None,
                      "task": task["shared_task"], "source": "Days 225–252 actual evidence and notes"}
        if shared["mode"] == "system_design_question_session":
            shared["allocation_minutes"] = {"requirements": 5, "constraints": 5,
                "architecture_or_review": 15, "failure_modes": 10, "tradeoffs_and_next_step": 10}
        sections_note = "; ".join(f"{ref['title']} — {', '.join(ref['sections'])} ({ref['url']})" for ref in refs)
        architecture = (
            "Assistant-supplied choice: one deterministic HomeSecure explanation workflow with authorization filtering before a local fake-provider adapter; "
            "the provider receives only owner-approved synthetic excerpts and cannot mutate policy or trigger action. The response gate accepts exact in-scope citations and exact policy text, otherwise abstains. "
            "Retries are at most one for timeout/unavailable only; malformed responses fail closed. Trace records carry an opaque stable ID and allowlisted counts/status/latency, never owner, question, source text or answer. "
            "Expected labels are fixed independently from observed outputs. All examples use synthetic customer cust-synth-01 / owner synthetic-owner-a. Both prior evidence gates must be genuinely met before learner workflow acceptance; if blocked, use the named recovery queue and do not treat scaffold or CI as learner completion.")
        row = {"day": day, "date": day_date.isoformat(), "title": task["title"], "learner_seam": task["seam"],
               "module": "single synthetic HomeSecure policy explanation evaluation", "signature": ", ".join(task["files"]),
               "selectors": [GATE, task["selector"], *task["recovery_selectors"]],
               "depends_on_days": [day - 1] if index else [], "recovery_day": task["shared"] == "review",
               "study_refs": [ref["url"] for ref in refs], "study_guidance": refs,
               "engineering_architecture": {"decision": architecture, "note_ref": f"assistant-architecture-notes.md#day-{day}",
                   "mode": "assistant_supplied_capstone_architecture; learner_implements_and_tests",
                   "capstone_proposal_required": False, "system_design_scope": "separate_interview_question_bank_only"},
               "budget": {"engineering_minutes": 165, "study": 35, "practice_and_tests": 115,
                   "evidence": 15, "shared_interview_or_remaining_reviews_minutes": 45, "total": 210,
                   "workstream_minutes": workstream}, "workstream_minutes": workstream,
               "task_minutes": activity_rows, "shared_interview_block": shared,
               "learner_status": "unobserved_not_implied_by_scaffold", "task": task,
               "synthetic_context": plan["synthetic_context"],
               "separate_learning_repository": plan["separate_learning_repository"]}
        day_rows.append(row)
        card = {"day_number": day, "date": day_date.isoformat(), "title": task["title"],
                "learner_seam": task["seam"], "signature": row["signature"], "selectors": row["selectors"],
                "prerequisite_days": row["depends_on_days"], "recovery_day": row["recovery_day"],
                "engineering": {"minutes": 165, "allocation": workstream},
                "budget_minutes": {"engineering": 165, "shared_interview_or_remaining_reviews": 45, "total": 210},
                "activities": activity_rows, "study_guidance": refs, "shared_interview_block": shared,
                "assistant_architecture_note": row["engineering_architecture"],
                "status": "unobserved_not_implied_by_scaffold",
                "separate_learning_repository": plan["separate_learning_repository"]}
        cards.append(card)
        notes.append(f'<a id="day-{day}"></a>\n## Day {day} — {task["title"]}\n\n**Architecture:** {architecture}\n')
        contract_lines = [f'<a id="day-{day}"></a>', f'# Engineering unit Day {day} · Calendar study day {day+21} · {day_date.isoformat()}: {task["title"]}', "",
            f"Date: {day_date.isoformat()}", "", f"**Context:** {task['context']}", "",
            f"**Synthetic data and expected outcomes:** customer `{plan['synthetic_context']['customer_id']}`; owner `{plan['synthetic_context']['owner_id']}`; product `{plan['synthetic_context']['product']}`; policy `{plan['synthetic_context']['policy_version']}`; renewal `{plan['synthetic_context']['renewal_date']}`; account status `{plan['synthetic_context']['status']}`. These are invented local fixture values only. Fixed outcomes: exact policy citation for the supported renewal question, abstention for unsupported evidence, owner filtering before the adapter, and abstention when a response follows a hostile retrieved instruction or requests an action.", "",
            f"**Assistant-supplied architecture — what and why:** {architecture}", "",
            "## Engineering workstreams", ""]
        for key in ("project_implementation_tests", "ai_workflow_implementation_evaluation",
                    "certification_labs_questions", "ai_concept_source_study", "certification_domain_study",
                    "core_systems_security_cloud_study", "evidence"):
            activity = activity_rows[key]
            contract_lines.extend([f"### {key.replace('_', ' ').title()} — {activity['minutes']} minutes", "",
                f"**Task:** {activity['task']}", f"**Source:** {activity['source']}",
                f"**Command:** {activity['command']}", f"**Evidence:** {activity['evidence']}", ""])
        contract_lines.extend(["## Shared block — 45 minutes (outside engineering)", "",
            f"**{shared['mode']}:** {shared['task']}", f"**Source:** {shared['source']}", "",
            "## Gate and scope", "", f"Before any learner acceptance command, run `{GATE}`. If blocked, use only the first unmet recovery selector in the plan source; do not execute gated workflow tests. "
            "The gate establishes eligibility only. Future learner evidence must be genuine; no scaffold, CI result or planned task awards completion. System-design practice is only the assigned question-bank session. DSA remains in the separate [dsa-prep repository](" + plan["separate_learning_repository"]["algorithm_practice"] + ").", "",
            f"**Learner status:** {row['learner_status']}", ""])
        atomic_write(OUT / f"contracts/day-{day}.md", "\n".join(contract_lines))

    allocation = {NAMES[key]: value for key, value in expected.items()}
    allocation["evidence"] = 210
    manifest = {"title": plan["title"], "status": plan["status"], "branch": plan["branch"],
        "base_commit": plan["base_commit"], "engineering_minutes": 165, "engineering_allocation": allocation,
        "engineering_minutes_total": 2310, "shared_interview_or_remaining_reviews_minutes": 45,
        "total_daily_minutes": 210, "scope": plan["scope"], "gate_policy": plan["gate"],
        "source_keys": [item["key"] for item in plan["sources"]], "days": day_rows,
        "shared_allocation": plan["shared_allocation"], "synthetic_context": plan["synthetic_context"],
        "separate_learning_repository": plan["separate_learning_repository"]}
    transfer = {"title": plan["title"], "status": plan["status"], "branch": plan["branch"],
        "source": {"base_commit": plan["base_commit"], "runtime": "Python standard library; offline synthetic fixture only"},
        "dates": {"start": plan["start_date"], "end": plan["end_date"],
                  "recovery_days": [row["day"] for row in day_rows if row["recovery_day"]]},
        "budget": {"engineering_minutes": 165,
                   "allocation": {"study": 35, "practice_and_tests": 115, "evidence": 15},
                   "shared_interview_or_remaining_reviews_minutes": 45, "total": 210,
                   "window_workstreams": allocation}, "gate_policy": plan["gate"],
        "recovery_selectors": plan["recovery_selectors"], "days": day_rows}
    owner = {"title": plan["title"], "status": plan["status"], "branch": plan["branch"],
        "base_commit": plan["base_commit"], "board_writes": False, "story_count": 14,
        "cards": cards, "shared_allocation": plan["shared_allocation"],
        "separate_learning_repository": plan["separate_learning_repository"]}
    for filename, obj in (("manifest.json", manifest), ("days-239-252-transfer-plan.json", transfer),
                          ("scrum-owner-manifest.json", owner),
                          ("verified-sources.json", {"verified_at": "2026-10-24", "recheck_before_study": True,
                              "scope": "Official titles and named sections were checked on 2026-10-24; recheck in June 2027. AWS service material is comparison-only and does not authorize service use; the OpenAI evaluation reference is methodological guidance, not a deprecated platform workflow.",
                              "sources": plan["sources"]})):
        atomic_write(OUT / filename, json.dumps(obj, indent=2, ensure_ascii=False) + "\n")
    atomic_write(OUT / "assistant-architecture-notes.md", "# Assistant-supplied architecture notes\n\nThe assistant selects and explains one bounded local architecture; the learner implements and tests it. No capstone design proposals or post-hoc justifications are assigned. System-design practice remains in the separate interview question bank.\n\n" + "\n".join(notes))
    return manifest


if __name__ == "__main__":
    generated = generate()
    import sys
    sys.path.insert(0, str(ROOT))
    from scripts.generate_study_calendar import migrate_assignments
    migrate_assignments()
    print(f"generated {len(generated['days'])} days; engineering={generated['engineering_minutes_total']} min")
