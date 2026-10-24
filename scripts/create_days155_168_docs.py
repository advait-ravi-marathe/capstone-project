#!/usr/bin/env python3
"""Synchronize Sprint 12 architecture/interview blocks from its canonical manifest."""
from __future__ import annotations

import json
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
BATCH = ROOT / "docs/reboot/continuation-155-168"
MANIFEST = BATCH / "manifest.json"
ARCH_START = "<!-- SPRINT12_ARCHITECTURE_START -->"
ARCH_END = "<!-- SPRINT12_ARCHITECTURE_END -->"
PRACTICE_START = "<!-- SPRINT12_PRACTICE_START -->"
PRACTICE_END = "<!-- SPRINT12_PRACTICE_END -->"
GATE_START = "<!-- SPRINT12_PRIOR_GATE_START -->"
GATE_END = "<!-- SPRINT12_PRIOR_GATE_END -->"


def replace_marked(text: str, start: str, end: str, content: str, *, after: str | None = None) -> str:
    block = f"{start}\n{content.rstrip()}\n{end}"
    if start in text and end in text:
        begin = text.index(start)
        finish = text.index(end, begin) + len(end)
        return text[:begin] + block + text[finish:]
    if after is None or after not in text:
        raise ValueError(f"Cannot place generated block: {start}")
    pos = text.index(after) + len(after)
    return text[:pos] + "\n\n" + block + text[pos:]


def architecture_block(day: dict) -> str:
    e = day["engineering"]
    note = e["assistant_architecture_note"]
    href = "../assistant-architecture-notes.md#" + note.rsplit("#", 1)[1]
    lines = [
        "## Assistant-supplied capstone architecture",
        "",
        f"[What and why](" + href + ") — " + e["architecture_explanation"],
        "",
        "The assistant supplies the implementation architecture; the learner implements and tests the named seam. System-design practice is only in the separate interview question bank. No capstone design proposal or post-hoc justification is assigned.",
    ]
    if day["day"] == 155:
        gate = json.loads(MANIFEST.read_text(encoding="utf-8"))["prior_readiness_gate"]
        lines += ["", f"**Prior A/B readiness gate: {gate['current_status'].replace('_', ' ')}.** " + gate["rule"]]
    return "\n".join(lines)


def gate_block(gate: dict, *, readme: bool = False) -> str:
    status = gate["current_status"].replace("_", " ")
    records = gate["evidence_records"]
    details = "; ".join(
        f"`{row['path']}` records `{row['status']}` / `{row['evidence_status']}`, "
        f"tested-source SHA {row['tested_source_sha'] or 'null'}, "
        f"{len(row['artifact_hashes'])} artifact hashes, and learner completion "
        f"`{str(row['learner_completion']).lower()}`"
        for row in records
    )
    if readme:
        return (f"- The earlier Days 153–154 learner-owned A/B evidence gate has "
                f"status {status}: {details}. The StreamForge scaffold and instructor "
                "witnesses cannot satisfy it. Re-evaluate the authoritative records "
                "when genuine learner evidence is added; keep progression blocked "
                "until both qualify under `manifest.json`.")
    paths = " and ".join(f"`{row['path']}`" for row in records)
    return (f"The authoritative records are {paths}. Current gate status is **{status}**: "
            f"{details}. A starter or assistant/instructor witness cannot satisfy this "
            "gate. Re-evaluate these records when genuine learner evidence is added; "
            "do not freeze the gate as permanently unmet.")


def practice_block(day: dict) -> str:
    a = day["engineering"]["interview_assignment"]
    q = a["question_id"]
    qhref = "../../interview/question-bank.md#" + q.lower()
    patterns = ", ".join(f"[{name}](../../interview/pattern-library.md#{name})" for name in a["pattern_refs"])
    phases = "; ".join(f"{x['minutes']} min {x['name'].lower()}" for x in a["mock_phases"])
    return "\n".join([
        f"**45-minute system-design interview (separate question set):** [{q} — {a['question_title']}]({qhref}). Use the phases {phases}. The learner's interview answer belongs in the question set and is written before hints or AI. Patterns for retrieval: {patterns}. This does not ask the learner to design StreamForge.",
        "",
        f"**40-minute implementation slice:** {a['bounded_task']}",
        "",
        f"**Deferred, still required before day completion:** {a['deferred_work']}",
        "",
        f"Run the complete day acceptance selector: `{a['selector_command']}`. A bounded sub-milestone or a green infrastructure harness does not substitute for the learner-owned selector and evidence.",
    ])


def main() -> None:
    manifest = json.loads(MANIFEST.read_text(encoding="utf-8"))
    gate = manifest["prior_readiness_gate"]
    for day in manifest["days"]:
        path = BATCH / day["contract_file"]
        text = path.read_text(encoding="utf-8")
        title = text.splitlines()[0]
        text = replace_marked(text, ARCH_START, ARCH_END, architecture_block(day), after=title)
        if day["day"] in (155, 159, 163, 167):
            section = "## Practice — 85 minutes"
            if section not in text:
                raise ValueError(f"Missing practice section in {path}")
            start = text.index(section)
            evidence = "## Evidence — 10 minutes"
            end = text.index(evidence, start)
            old = text[start:end].strip()
            # First generation replaces the native slice. Later runs replace only
            # the marked generated section and remain byte-for-byte idempotent.
            if PRACTICE_START in old and PRACTICE_END in old:
                first = old[:old.index(PRACTICE_START)].rstrip()
                last = old[old.index(PRACTICE_END) + len(PRACTICE_END):].strip()
            else:
                first, last = section, ""
            content = practice_block(day)
            generated = PRACTICE_START + "\n" + content + "\n" + PRACTICE_END
            replacement = first + "\n\n" + generated
            if last:
                replacement += "\n\n" + last
            text = text[:start] + replacement + "\n\n" + text[end:]
        path.write_text(text.rstrip() + "\n", encoding="utf-8")

    readme = BATCH / "README.md"
    text = readme.read_text(encoding="utf-8")
    old_gate_line = next((line for line in text.splitlines() if line.startswith("- The earlier Days 153–154 learner-owned A/B evidence gate")), None)
    if old_gate_line is not None:
        text = text.replace(old_gate_line, gate_block(gate, readme=True))
    elif GATE_START in text and GATE_END in text:
        text = replace_marked(text, GATE_START, GATE_END, gate_block(gate, readme=True))
    else:
        raise ValueError("Cannot find prior A/B gate summary in README")
    readme.write_text(text.rstrip() + "\n", encoding="utf-8")

    notes = BATCH / "assistant-architecture-notes.md"
    text = notes.read_text(encoding="utf-8")
    heading = "## Prerequisite gate: earlier A/B learner evidence"
    section = gate_block(gate)
    if GATE_START in text and GATE_END in text:
        text = replace_marked(text, GATE_START, GATE_END, section)
    elif heading in text:
        start = text.index(heading) + len(heading)
        text = text[:start] + "\n\n" + GATE_START + "\n" + section + "\n" + GATE_END
    else:
        raise ValueError("Cannot find prior A/B gate section in architecture notes")
    notes.write_text(text.rstrip() + "\n", encoding="utf-8")
    (BATCH / "streamforge-days-155-168-transfer-plan.md").write_bytes(readme.read_bytes())


if __name__ == "__main__":
    main()
