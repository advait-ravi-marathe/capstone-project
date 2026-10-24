#!/usr/bin/env python3
"""Generate synchronized Days 225–238 artifacts from plan-source.json."""
from __future__ import annotations

import json
import os
import tempfile
from datetime import date, timedelta
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
OUT = ROOT / "docs/reboot/continuation-225-238"
SOURCE = OUT / "plan-source.json"
GATE = "python3 scripts/check_continuation_211_entry_gate.py"
EVIDENCE_INSTRUCTION = (
    "Record actual learner status; command and output; 40-character source commit SHA; "
    "64-character artifact SHA-256; changed paths; assistance; first unmet assertion; "
    "next action. Keep scaffold, CI and eligibility separate from learner completion."
)


def atomic_write(path: Path, data: str) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    with tempfile.NamedTemporaryFile("w", encoding="utf-8", dir=path.parent, delete=False) as f:
        f.write(data)
        temp = Path(f.name)
    try:
        os.replace(temp, path)
    finally:
        temp.unlink(missing_ok=True)


def source_by_key(sources):
    return {source["key"]: source for source in sources}


def study_guidance(task, sources):
    by_key = source_by_key(sources)
    keys = list(dict.fromkeys(task["study_source_keys"]))
    result = []
    for key in keys:
        src = by_key[key]
        sections = list(src["sections"])
        if key == task["cert_source_key"]:
            sections = [task["cert_source_section"]]
        result.append({**src, "sections": sections})
    return result


def make_workstream_notes(task, refs):
    source_map = {s["key"]: s for s in refs}

    def source_ref(key):
        s = source_map[key]
        return f"{s['title']} — {', '.join(s['sections'])} ({s['url']})"

    n = task["day"]
    selector = task["selector"]
    recovery = "; ".join(f"`{selector}`" for selector in task["recovery_selectors"])
    cert_line = (
        f"{task['cert']}m: offline certification retrieval, {task['cert_prompt']} "
        f"Source: {source_ref(task['cert_source_key'])}. Evidence: exact guide version, heading, "
        "answer and limitation; no readiness claim or booking."
        if task["cert"] else
        "0m: no certification question slice today. Source: none. Command: none. "
        "Evidence: retain the zero allocation; no extra study."
    )
    return {
        "project_implementation_tests": (
            f"{task['project']}m: {task['project_task']} Source: existing CoverStory recovery contract "
            f"and ai/grounded/continuation_225.py. First run `{GATE}`. If the gate is absent/blocked, "
            f"run the first unmet recovery selector, in this order: {recovery}; do not run the new workflow test. "
            f"Only if genuine Day 210 evidence makes the task eligible, run `{selector}`. "
            "Evidence: exact selector/result, state before/after, changed paths and first failing assertion."
        ),
        "ai_workflow_implementation_evaluation": (
            f"{task['ai']}m: {task['ai_task']} Source: {source_ref(task['ai_source_key'])}. "
            f"Command: `{selector}` only when the gate is eligible; otherwise use a fixed local-label "
            "fixture without invoking the workflow or any provider. Evidence: per-case expected and "
            "actual labels, counts and explicit negative control."
        ),
        "certification_labs_questions": cert_line,
        "ai_concept_source_study": (
            f"{task['ai_study']}m: read {source_ref(task['ai_source_key'])} and note one design concept "
            "plus one limitation. Command: open the cited official page and the exact named section; "
            "record the title/heading in the daily evidence note. No Evals API/platform use, provider call, "
            "or cloud service."
        ),
        "certification_domain_study": (
            f"{task['cert_study']}m: study {source_ref(task['cert_source_key'])}; recheck the currently "
            "published guide/version at study time, then record one concept and its limit. Command: open "
            "the official source and exact heading, recording the guide version in the evidence note. "
            "No readiness or pass claim."
        ),
        "core_systems_security_cloud_study": (
            f"{task['core_study']}m: study {source_ref(task['core_source_key'])} as a local design "
            "comparison. Command: read the exact official heading and record one implementation implication "
            "and one boundary. No cloud calls, credentials or deployment."
        ),
        "evidence": f"15m: {EVIDENCE_INSTRUCTION}",
    }


def generate():
    plan = json.loads(SOURCE.read_text(encoding="utf-8"))
    tasks = plan["tasks"]
    if len(tasks) != 14 or [t["day"] for t in tasks] != list(range(225, 239)):
        raise ValueError("plan must contain exactly Days 225–238")
    totals = {k: sum(t[k] for t in tasks) for k in ("project", "ai", "cert", "ai_study", "cert_study", "core_study")}
    expected = {"project": 1140, "ai": 360, "cert": 110, "ai_study": 150, "cert_study": 120, "core_study": 220}
    if totals != expected:
        raise ValueError(f"window allocation mismatch: {totals}")
    allocation_names = {
        "project": "project_implementation_tests",
        "ai": "ai_workflow_implementation_evaluation",
        "cert": "certification_labs_questions",
        "ai_study": "ai_concept_source_study",
        "cert_study": "certification_domain_study",
        "core_study": "core_systems_security_cloud_study",
        "evidence": "evidence",
    }
    workstream_totals = {allocation_names[k]: v for k, v in expected.items()}
    workstream_totals["evidence"] = 210
    dates = [date(2027, 6, 5) + timedelta(days=i) for i in range(14)]
    question_titles = {"Q07": "API gateway: route and protect", "Q14": "Booking: retry after a lost response", "Q23": "Event pipeline: ingest and process", "Q16": "Messaging: ordering and replay"}
    days, cards, notes = [], [], []
    for index, (day_date, task) in enumerate(zip(dates, tasks)):
        n = task["day"]
        refs = study_guidance(task, plan["sources"])
        task_minutes = make_workstream_notes(task, refs)
        alloc = {allocation_names[k]: task[k] for k in allocation_names if k != "evidence"}
        alloc["evidence"] = 15
        if sum(alloc.values()) != 165 or sum(task[k] for k in ("ai_study", "cert_study", "core_study")) != 35:
            raise ValueError(f"Day {n} does not have the fixed 165/35/115/15 budget")
        if sum(task[k] for k in ("project", "ai", "cert")) != 115:
            raise ValueError(f"Day {n} practice/test allocation does not equal 115 minutes")
        if task["shared"] == "question":
            shared = {"minutes": 45, "within_engineering_minutes": False, "mode": "system_design_question_session", "question_id": task["question_id"], "question_title": task["question_title"], "review_task": None}
        elif task["shared"] == "cert":
            shared = {"minutes": 45, "within_engineering_minutes": False, "mode": "certification_retrieval_review", "question_id": None, "question_title": None, "review_task": f"Retrieve and answer one offline question from {task['cert_source_title']} / {task['cert_source_section']}; record version, exact heading, checked answer and gap. No exam booking or readiness claim."}
        else:
            shared = {"minutes": 45, "within_engineering_minutes": False, "mode": "unfinished_review", "question_id": None, "question_title": None, "review_task": f"Review {', '.join(task['files'])}; record the first failing assertion or unresolved invariant and next selector. No capstone design proposal or post-hoc justification."}
        shared["allocation_minutes"] = {"requirements": 5, "constraints": 5, "architecture_or_review": 15, "failure_modes": 10, "tradeoffs_and_next_step": 10}
        architecture = (f"Assistant-supplied choice: {task['title']}. Keep the workflow offline and synthetic. "
                        "Generation creates only a candidate; bind exact owner, source/version and snapshot digest before staging a DRAFT. "
                        "A separately invoked human review must be owner-scoped, version/revision-bound, idempotent and audited before same-owner exposure. "
                        "If the Day 210 evidence gate is absent or blocked, route project minutes to the first unmet CoverStory selector and keep AI evaluation on fixed local labels. "
                        "No model can approve, mutate policy, or cause an external action; learner implements and tests these supplied boundaries.")
        day = {
            "day": n, "date": day_date.isoformat(), "title": task["title"], "learner_seam": task["seam"],
            "module": "CoverStory bounded synthetic workflow", "signature": ", ".join(task["files"]),
            "selectors": [GATE, task["selector"], *task["recovery_selectors"]],
            "depends_on_days": [n - 1] if index else [], "recovery_day": task["shared"] == "review",
            "study_refs": [ref["url"] for ref in refs], "study_guidance": refs,
            "engineering_architecture": {"decision": architecture, "note_ref": f"assistant-architecture-notes.md#day-{n}", "mode": "assistant_supplied_capstone_architecture; learner_implements_and_tests", "capstone_proposal_required": False, "system_design_scope": "separate_interview_question_bank_only"},
            "budget": {"engineering_minutes": 165, "study": 35, "practice_and_tests": 115, "evidence": 15, "shared_interview_or_remaining_reviews_minutes": 45, "total": 210, "workstream_minutes": alloc},
            "workstream_minutes": alloc, "task_minutes": task_minutes, "shared_interview_block": shared,
            "learner_status": "unobserved_not_implied_by_scaffold", "task": task,
            "separate_learning_repository": plan["separate_learning_repository"],
        }
        days.append(day)
        cards.append({"day_number": n, "date": day_date.isoformat(), "title": task["title"], "learner_seam": task["seam"], "signature": day["signature"], "selectors": day["selectors"], "prerequisite_days": day["depends_on_days"], "recovery_day": day["recovery_day"], "engineering": {"minutes": 165, "allocation": alloc}, "budget_minutes": {"engineering": 165, "shared_interview_or_remaining_reviews": 45, "total": 210}, "study_guidance": refs, "shared_interview_block": shared, "status": "unobserved_not_implied_by_scaffold", "separate_learning_repository": plan["separate_learning_repository"]})
        notes.append(f'<a id="day-{n}"></a>\n## Day {n} — {task["title"]}\n\n**What:** {architecture}\n\n**Why:** Keep evidence authorization and workflow state separate. Any unmet prerequisite routes to the existing recovery queue; acceptance results never become learner completion.\n')
        source_lines = "; ".join(f"{ref['title']} — {', '.join(ref['sections'])} ({ref['url']})" for ref in refs)
        question = f"{shared['question_id']} — {shared['question_title']}" if shared["question_id"] else shared["review_task"]
        contract = [f'<a id="day-{n}"></a>', f'# Day {n}: {task["title"]}', "", f"Date: {day_date.isoformat()}", "", f"**Assistant-supplied architecture — what and why:** {architecture}", "", f"**Project task and acceptance:** {task_minutes['project_implementation_tests']}", "", f"**AI workflow/evaluation task:** {task_minutes['ai_workflow_implementation_evaluation']}", "", f"**Acceptance command:** `{task['selector']}` (run only if the entry gate is eligible)", "", f"**Engineering budget (165m):** project/tests {task['project']}m; AI workflow/evaluation {task['ai']}m; certification practice {task['cert']}m; AI concept study {task['ai_study']}m; certification study {task['cert_study']}m; core systems/security/cloud study {task['core_study']}m; evidence 15m.", "", f"**Study references:** {source_lines}", "", f"**Study tasks:** {task_minutes['ai_concept_source_study']} {task_minutes['certification_domain_study']} {task_minutes['core_systems_security_cloud_study']}", "", f"**Shared 45-minute block:** {shared['mode']} — {question}; requirements 5m, constraints 5m, design/review 15m, failure modes 10m, trade-offs and next step 10m.", "", f"**Recovery:** first run `{GATE}`. If absent/blocked, use the first unmet selector in [`README.md`](../README.md#entry-gate-and-recovery); do not run the new workflow acceptance selector. DSA remains external in [{plan['separate_learning_repository']['algorithm_practice']}]({plan['separate_learning_repository']['algorithm_practice']}).", "", f"**Learner evidence:** {task_minutes['evidence']}", ""]
        atomic_write(OUT / f"contracts/day-{n}.md", "\n".join(contract))
    manifest = {"title": plan["title"], "status": plan["status"], "branch": plan["branch"], "base_commit": plan["base_commit"], "engineering_minutes": 165, "engineering_allocation": workstream_totals, "engineering_minutes_total": 2310, "shared_interview_or_remaining_reviews_minutes": 45, "total_daily_minutes": 210, "scope": plan["scope"], "gate_policy": plan["gate"], "source_keys": [s["key"] for s in plan["sources"]], "days": days, "separate_learning_repository": plan["separate_learning_repository"]}
    transfer = {"title": plan["title"], "status": plan["status"], "branch": plan["branch"], "source": {"base_commit": plan["base_commit"], "runtime": "Python standard library; synthetic/offline only"}, "dates": {"start": plan["start_date"], "end": plan["end_date"], "recovery_days": [d["day"] for d in days if d["recovery_day"]]}, "budget": {"engineering_minutes": 165, "allocation": {"study": 35, "practice_and_tests": 115, "evidence": 15}, "shared_interview_or_remaining_reviews_minutes": 45, "total": 210, "window_workstreams": workstream_totals}, "gate_policy": plan["gate"], "recovery_selectors": plan["recovery_selectors"], "days": days}
    owner = {"title": plan["title"], "status": plan["status"], "branch": plan["branch"], "base_commit": plan["base_commit"], "board_writes": False, "story_count": 14, "cards": cards, "separate_learning_repository": plan["separate_learning_repository"]}
    for name, obj in (("manifest.json", manifest), ("days-225-238-transfer-plan.json", transfer), ("scrum-owner-manifest.json", owner)):
        atomic_write(OUT / name, json.dumps(obj, indent=2, ensure_ascii=False) + "\n")
    atomic_write(OUT / "assistant-architecture-notes.md", "# Assistant-supplied architecture notes\n\nThis window keeps the workflow synthetic, offline and reference-only until actual evidence satisfies its entry gate. The assistant chooses and explains the architecture; the learner implements and tests it. No capstone design proposal or post-hoc architecture justification is assigned. System-design practice remains in the separate interview question bank.\n\n" + "\n".join(notes))
    atomic_write(OUT / "verified-sources.json", json.dumps({"verified_at": "2026-10-24", "recheck_before_study": True, "scope": "Exact current titles and named sections were checked against official source pages on 2026-10-24. Recheck in June 2027; versions may change. All AWS/OpenAI service material is comparison-only. No deprecated Evals API/platform, provider call, cloud resource or spend is assigned.", "sources": plan["sources"]}, indent=2, ensure_ascii=False) + "\n")
    return manifest


if __name__ == "__main__":
    result = generate()
    print(f"generated {len(result['days'])} days; engineering={result['engineering_minutes_total']} min")
