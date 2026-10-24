"""Synchronize Sprint 11 contracts and ticket export from its canonical manifest.

This deliberately does not recreate learner evidence files or alter dates/status.
The manifest is the source for daily scope, exact commands and interview assignments;
the architecture note document is the assistant-owned implementation guidance.
"""

from __future__ import annotations

import json
import re
from pathlib import Path


ROOT = Path(__file__).resolve().parents[1]
SPRINT = ROOT / "docs/reboot/continuation-141-154"
MANIFEST_PATH = SPRINT / "manifest.json"
EXPORT_PATH = SPRINT / "scrum-owner-manifest.json"
NOTES_PATH = SPRINT / "assistant-architecture-notes.md"
START_MARKER = "<!-- generated sprint11 interview block: start -->"
END_MARKER = "<!-- generated sprint11 interview block: end -->"


def read_json(path: Path) -> dict:
    return json.loads(path.read_text(encoding="utf-8"))


def write_json(path: Path, value: dict) -> None:
    path.write_text(json.dumps(value, indent=2, ensure_ascii=False) + "\n", encoding="utf-8")


def relative_note_link(day: int) -> str:
    return f"../assistant-architecture-notes.md#day-{day}"


def assistant_note_block(day: int, day_date: str) -> str:
    link = relative_note_link(day)
    return (
        f"Date: {day_date} · Sprint 11 · 120 engineering minutes "
        "(25 study / 85 practice / 10 evidence).\n\n"
        f"**Assistant-provided architecture (what and why):** [Day {day} note]({link}). "
        "The assistant supplies the capstone design; implement and test the named seam. "
        "System-design practice is limited to the separate interview question set. "
        "No capstone design proposal or post-hoc justification is assigned."
    )


def interview_block(day: dict, assignment: dict) -> str:
    number = day["day"]
    phases = "; ".join(f"{p['minutes']} minutes {p['phase']}" for p in assignment["mock_phases"])
    question = (
        f"[Q{assignment['question_id'][1:]} — {assignment['question_title']}]"
        f"(../../interview/question-bank.md#q{assignment['question_id'][1:]})"
    )
    patterns = ", ".join(
        f"[{label}](../../interview/pattern-library.md#{ref.rsplit('#', 1)[1]})"
        for label, ref in zip(assignment["pattern_labels"], assignment["pattern_refs"], strict=True)
    )
    slice_minutes = assignment["eai_slice_minutes"]
    task = assignment["bounded_task"]
    deferred = assignment["deferred_work_id"]
    deferred_text = assignment["deferred_work"]
    return f"""{START_MARKER}
## Separate system-design interview practice — 45 minutes of the 85-minute practice block

Attempt this interview question before hints, references, or AI feedback. This is interview practice only; the assistant supplies the capstone design in the linked note. The 45-minute question session and the bounded 40-minute implementation slice replace the existing 85-minute practice allocation.

- **Question:** {question}. {assignment['question_prompt']}
- **Interview patterns:** {patterns}
- **Mock phases (45 minutes total):** {phases}.
- **Remaining engineering slice (40 minutes total):** {slice_minutes['preconditions_and_prediction']} minutes preconditions/prediction; {slice_minutes['implementation_or_observation']} minutes implementation or observation; {slice_minutes['selector_and_blocker_record']} minutes selector and blocker record.
- **Bounded capstone task:** {task}
- **Deferred acceptance:** `{deferred}` — {deferred_text}
- **Runnable selector:** `{assignment['selector_command']}`

The original full acceptance remains the completion gate. This timebox records partial progress only; the named deferred item stays open until its full selector and independent evidence pass.
{END_MARKER}"""


def sync_contract(day: dict) -> None:
    path = ROOT / day["contract_file"]
    text = path.read_text(encoding="utf-8")
    number = day["day"]
    engineering = day["engineering"]
    course = engineering["concept_course"]
    lesson = engineering.get("lesson")
    if lesson:
        prefix = (
            f"Preferred course reference: **{course['title']}**, course {course['course_id']}, "
            f"instructor {course['instructor']}; [course page]({course['url']}). "
        )
        duplicated = (
            f"Preferred course: {course['title']} (course {course['course_id']}), "
            f"instructor {course['instructor']}, [course page]({course['url']}). "
        )
        text = text.replace(prefix + duplicated, prefix)
    title = re.search(rf"(?m)^# Day {number}[^\n]*\n", text)
    study = re.search(r"(?m)^## Study\b", text)
    if not title or not study or study.start() < title.end():
        raise ValueError(f"Day {number} has no stable title/study sections")
    text = (
        text[:title.end()] + "\n" + assistant_note_block(number, day["date"]) +
        "\n\n" + text[study.start():]
    )
    text = re.sub(
        rf"(?ms)^{re.escape(START_MARKER)}.*?^{re.escape(END_MARKER)}\n?\n?",
        "",
        text,
    )
    assignment = day["engineering"].get("interview_assignment")
    if assignment:
        practice_heading = re.search(r"(?m)^## Practice[^\n]*$", text)
        if not practice_heading:
            raise ValueError(f"Day {number} has no Practice section")
        new_heading = "## Practice — 85 minutes total (45-minute interview + 40-minute implementation)"
        text = text[:practice_heading.start()] + new_heading + text[practice_heading.end():]
        practice_heading = re.search(r"(?m)^## Practice[^\n]*$", text)
        text = text[:practice_heading.start()] + interview_block(day, assignment) + "\n\n" + text[practice_heading.start():]
    # Replace all prior rendered package/selector instructions from the
    # structured preparation and acceptance commands. This prevents stale
    # classifiers or omitted artifact steps from surviving regeneration.
    text = re.sub(
        r"(?m)^Before the packaged Days[^\n]*\n\n?",
        "",
        text,
    )
    text = re.sub(
        r"(?ms)^## Package/artifact preparation\n.*?(?=^## |\Z)",
        "",
        text,
    )
    selector = re.search(r"(?m)^## Selector\s*$", text)
    separate_dsa = re.search(r"(?m)^## Separate DSA track\s*$", text)
    if not selector or not separate_dsa or separate_dsa.start() < selector.start():
        raise ValueError(f"Day {number} has no stable Selector/DSA sections")
    prep = engineering.get("preparation_commands", [])
    tests = engineering.get("test_commands", engineering["acceptance_commands"])
    commands = [*prep, *tests]
    engineering["acceptance_commands"] = commands
    setup = ""
    if prep:
        note = engineering.get("preparation_note", "Build the exact artifact required by the following selector.")
        setup = f"## Package/artifact preparation\n\n{note}\n\n```sh\n" + "\n".join(prep) + "\n```\n\n"
    selector_text = setup + "## Selector\n\n```sh\n" + "\n".join(tests) + "\n```\n\n"
    selector_text += "Preparation and tests are separate evidence. Preserve actual artifact identities; unavailable prerequisites remain blocked, not inferred. Recovery days rerun prior selectors only. The legacy Day 124 command implementation and V001–V005 remain unchanged. No AWS/cloud action is included.\n\n"
    text = text[:selector.start()] + selector_text + text[separate_dsa.start():]
    path.write_text(text.rstrip() + "\n", encoding="utf-8")


def owner_learning_lines(day: dict) -> list[str]:
    engineering = day["engineering"]
    course = engineering["concept_course"]
    lines: list[str] = []
    lesson = engineering.get("lesson")
    if lesson:
        lines.append(
            f"Course reference: {course['title']} — {course.get('instructor', 'in28Minutes Official')}; "
            f"{lesson['title']} ({lesson['seconds']} sec), section {lesson['section']}. Playback not audited."
        )
    else:
        lines.append("No exact course video is required; use the focused official reading and supplied worked example in the 25-minute study block.")
    note_ref = engineering["assistant_architecture_note"]
    lines.append(
        f"Assistant-provided capstone architecture: {note_ref}. Implement and test the supplied design; no capstone proposal or post-hoc justification."
    )
    assignment = engineering.get("interview_assignment")
    if assignment:
        lines.append(
            f"Separate 45-minute interview practice: {assignment['question_id']} — {assignment['question_title']}; "
            f"question-bank only. It replaces practice time, leaving a bounded 40-minute capstone slice: "
            f"{assignment['bounded_task']} Deferred: {assignment['deferred_work_id']}."
        )
    return lines


def sync() -> None:
    manifest = read_json(MANIFEST_PATH)
    export = read_json(EXPORT_PATH)
    days = manifest["days"]
    cards = {card["day_number"]: card for card in export["cards"]}
    if [day["day"] for day in days] != list(range(141, 155)):
        raise ValueError("Sprint 11 must contain exactly Days 141–154 in order")
    if set(cards) != set(range(141, 155)):
        raise ValueError("Sprint 11 owner export must contain exactly Days 141–154")

    for day in days:
        number = day["day"]
        engineering = day["engineering"]
        engineering["assistant_architecture_note"] = (
            f"docs/reboot/continuation-141-154/assistant-architecture-notes.md#day-{number}"
        )
        engineering["evidence_reference"] = f"evidence/continuation-141-154/day-{number}.json"
        engineering["instructor_harness_test_path"] = "src/test/java/assignments/ops/Day141To154InfrastructureTest.java"
        engineering.setdefault("test_commands", list(engineering["acceptance_commands"]))
        engineering.setdefault("preparation_commands", [])
        engineering["acceptance_commands"] = [
            *engineering["preparation_commands"], *engineering["test_commands"]
        ]
        scope_note = engineering["concept_course"].get("scope_note", "")
        if engineering["assistant_architecture_note"] not in scope_note:
            engineering["concept_course"]["scope_note"] = (
                (scope_note.rstrip() + " ") if scope_note else ""
            ) + f"Assistant-provided capstone architecture: {engineering['assistant_architecture_note']}"
        sync_contract(day)
        card = cards[number]
        card["engineering"] = engineering
        card["learning_block"] = {
            "heading": "Learning resources — 25 minutes total",
            "lines": owner_learning_lines(day),
            "access_note": "Course access or catalog metadata is not a viewing or completion claim.",
            "architecture_note": engineering["assistant_architecture_note"],
        }

    # Engineering is budgeted separately from the DSA repository's time tier.
    # Do not present the engineering slice as the learner's combined daily total.
    manifest["daily_budget_minutes"] = None
    manifest["engineering_budget_minutes"] = 120
    manifest["daily_budget_excludes_separate_dsa"] = True
    manifest["separate_learning_repository"] = {
        "name": "dsa-prep",
        "url": "https://github.com/advait-ravi-marathe/dsa-prep/tree/859e161f6553e9aca8dc501fda152780cec66ef0",
        "algorithm_practice": "https://github.com/advait-ravi-marathe/dsa-prep/tree/859e161f6553e9aca8dc501fda152780cec66ef0",
        "source_commit": "859e161f6553e9aca8dc501fda152780cec66ef0",
        "assignment_note": "DSA assignments, supporting code, review schedule and attempt history are maintained only in this repository.",
    }
    manifest["interview_practice_summary"] = {
        "days": [day["day"] for day in days if day["engineering"].get("interview_assignment")],
        "sessions_per_week": 2,
        "minutes_each": 45,
        "remaining_implementation_minutes": 40,
        "practice_minutes_total": 85,
        "design_practice_location": "separate system-design interview question bank only",
        "capstone_architecture": "assistant supplied; learner implements and tests",
    }
    export["interview_practice_summary"] = manifest["interview_practice_summary"]
    export["separate_learning_repository"] = manifest["separate_learning_repository"]

    readme_path = SPRINT / "README.md"
    readme = readme_path.read_text(encoding="utf-8")
    summary = """## Interview practice and supplied capstone architecture

Days 141, 144, 148, and 151 reserve four spaced 45-minute system-design question sessions, two per week. Each replaces 45 minutes of the existing 85-minute engineering practice block and leaves a task-specific 40-minute implementation slice. Interview answers stay in the separate question bank; assistant notes supply capstone architecture for all 14 days. No extra daily time or capstone design proposal is assigned.

DSA assignments, supporting code, review schedules, and attempt history remain exclusively in the [DSA preparation repository](https://github.com/advait-ravi-marathe/dsa-prep/tree/859e161f6553e9aca8dc501fda152780cec66ef0).
"""
    readme = re.sub(r"(?ms)^## Interview practice and supplied capstone architecture\n.*$", "", readme).rstrip()
    readme += "\n\n" + summary
    readme_path.write_text(readme, encoding="utf-8")

    write_json(MANIFEST_PATH, manifest)
    write_json(EXPORT_PATH, export)


if __name__ == "__main__":
    sync()
