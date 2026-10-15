#!/usr/bin/env python3
"""Run instructor harnesses and separate learner checks for CoverStory days 11-20."""
from __future__ import annotations

import hashlib
import json
import os
import re
import shlex
import subprocess
import sys
from datetime import datetime, timezone
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
SOURCE = ROOT / "docs/source-inputs/2026-10-14/curriculum/coverstory-final-saved-contracts-594-days.json"
OUT = ROOT / "docs/evidence/coverstory/phase1-days-011-020"
RAW = OUT / "raw"
MAVEN = shlex.split(os.environ.get("COVERSTORY_MAVEN", "mvn"))

DAYS = [
    (11, "CAP-55", "java", "CAP55InstructorInfrastructureTest", "CAP55AcceptanceTest", ["fixtures/coverstory/CAP-55/synthetic-v1.json"],
     ["accepts distinct keys sharing a partition", "validates per-key monotonic offsets and complete synthetic provenance"],
     ["rejects reordered/split-key offsets", "rejects missing provenance and broker-live claims"],
     "Catalog offline trace seam; no Kafka, broker, topic, or cloud resource."),
    (12, "CAP-56", "python", "test_coverstory_cap56_infrastructure.py", "test_cap_56.py", ["fixtures/coverstory/CAP-56/synthetic-v1.json"],
     ["recomputes deterministic baseline metrics from fixed public wording"],
     ["mutated fixture digest or malformed source rows are rejected"],
     "Pure Python synthetic retrieval evaluation; no live retrieval provider."),
    (13, "CAP-57", "java", "CAP57InstructorInfrastructureTest", "CAP57AcceptanceTest", ["fixtures/coverstory/CAP-57/synthetic-v1.json", "src/main/resources/db/schema/catalog_publication.sql", "src/test/resources/coverstory/catalog/cap_57_fixtures.sql"],
     ["embedded PostgreSQL verifies immutable catalog versions, export projection and rollback"],
     ["duplicate/conflict mutation and injected pre-commit failure"],
     "Test-only Catalog publication schema against fresh embedded PostgreSQL; not production persistence."),
    (14, "CAP-166", "frontend", "test:catalog-fixture", "test:catalog-acceptance", ["fixtures/coverstory/CAP-166/synthetic-v1.json"],
     ["fixture digest and schema-major decoder guard execute in Node"],
     ["unsupported major, malformed envelope and unsafe consumer fields are rejected"],
     "React catalog decoder/fixture seam; learner decoder remains TODO and full API/database integration is deferred."),
    (15, "CAP-58", "java", "CAP58InstructorInfrastructureTest", "CAP58AcceptanceTest", ["fixtures/coverstory/CAP-58/synthetic-v1.json"],
     ["independent oracle accepts allowlisted, bounded low-sensitivity event batch"],
     ["rejects oversized body, disallowed fields and non-allowlisted event/screen"],
     "Analytics validator contract only; no HTTP ingestion or persistence service."),
    (16, "CAP-59", "python", "test_coverstory_cap59_infrastructure.py", "test_cap_59.py", ["fixtures/coverstory/CAP-59/synthetic-v1.json"],
     ["exact source/version lookup and Python code-point excerpt offsets"],
     ["rejects unknown version, unauthorized/inapplicable source, excerpt mismatch and digest tampering"],
     "Pure verifier seam over fixed synthetic text; hash validity is not source authority."),
    (17, "CAP-60", "java", "CAP60InstructorInfrastructureTest", "CAP60AcceptanceTest", ["fixtures/coverstory/CAP-60/synthetic-v1.json", "src/test/resources/coverstory/CAP-60/schema.sql"],
     ["fresh embedded PostgreSQL validates owner, sequence and causation uniqueness invariants"],
     ["duplicate transition/effect and invalid ownership/order controls"],
     "Test-only schema/oracle; no application command or endpoint exists."),
    (18, "CAP-61", "python", "test_coverstory_cap61_infrastructure.py", "test_cap_61.py", ["fixtures/coverstory/CAP-61/synthetic-v1.json"],
     ["explicit threshold boundary and extractive citation-preserving answer"],
     ["denied/inapplicable/empty abstention and NaN/out-of-range values"],
     "Synthetic grounded-answer evaluation gate; not calibrated or connected to a production model."),
    (19, "CAP-62", "python", "test_coverstory_cap62_infrastructure.py", "test_cap_62.py", ["fixtures/coverstory/CAP-62/synthetic-v1.json"],
     ["bounded async completion before deadline and late-worker cleanup"],
     ["timeout, outer cancellation propagation, and zero-timeout prevalidation"],
     "Local async fakes only; no broker, cache, retrieval backend, or workflow."),
    (20, "CAP-63", "python", "test_coverstory_cap63_infrastructure.py", "test_coverstory_cap63_acceptance.py", ["fixtures/coverstory/CAP-63/synthetic-v1.json"],
     ["evidenced PASS plus blocked dependency is represented honestly"],
     ["rejects wrong exit, artifact, domain, fixture identity and missing-record ordering"],
     "Pure evidence-index oracle; it does not validate real Java/SQL/deployment/customer evidence."),
]

def sha(path: Path) -> str:
    return hashlib.sha256(path.read_bytes()).hexdigest()

def run(command: list[str], log: Path, cwd: Path = ROOT) -> dict:
    result = subprocess.run(command, cwd=cwd, text=True, stdout=subprocess.PIPE, stderr=subprocess.STDOUT)
    log.write_text(result.stdout, encoding="utf-8")
    text = result.stdout
    m = re.search(r"Ran (\d+) tests?", text)
    if not m:
        m = re.search(r"Tests run: (\d+), Failures: (\d+), Errors: (\d+), Skipped: (\d+)", text)
        counts = {"tests": int(m.group(1)), "failures": int(m.group(2)), "errors": int(m.group(3)), "skipped": int(m.group(4))} if m else None
    else:
        f = re.search(r"failures=(\d+)", text); e = re.search(r"errors=(\d+)", text); s = re.search(r"skipped=(\d+)", text)
        counts = {"tests": int(m.group(1)), "failures": int(f.group(1)) if f else 0, "errors": int(e.group(1)) if e else 0, "skipped": int(s.group(1)) if s else 0}
    return {"command": shlex.join(command), "exit_code": result.returncode, "status": "PASS" if result.returncode == 0 else "FAIL",
            "test_counts": counts, "raw_log": log.relative_to(ROOT).as_posix(), "raw_log_sha256": sha(log)}

def main() -> int:
    source = json.loads(SOURCE.read_text(encoding="utf-8"))
    cards = {c["id"]: c for c in source["cards"]}
    RAW.mkdir(parents=True, exist_ok=True)
    report = {"title": "CoverStory Phase 1 Days 011–020 instructor readiness", "recorded_at_utc": datetime.now(timezone.utc).isoformat(),
              "canonical_curriculum_sha256": sha(SOURCE), "readiness_basis": "Current instructor setup commands, immutable synthetic fixture bytes, positive/negative controls, and declared module boundaries. Learner completion and elapsed task time are separate.",
              "timebox_status": "UNMEASURED_NOT_GATING_INSTRUCTOR_READINESS", "days": []}
    for day, card, kind, instructor, learner, fixture_paths, positive, negative, wiring in DAYS:
        if kind == "java":
            instructor_cmd = MAVEN + [f"-Dtest={instructor}", "test"]
            learner_cmd = MAVEN + ["-Plearner-tests", f"-Dtest={learner}", "test"]
            ichecks = [run(instructor_cmd, RAW / f"day-{day:03}-instructor.log")]
            lchecks = [run(learner_cmd, RAW / f"day-{day:03}-learner.log")]
        elif kind == "frontend":
            ichecks = [run(["npm", "run", instructor], RAW / f"day-{day:03}-instructor.log", ROOT / "web")]
            lchecks = [run(["npm", "run", learner], RAW / f"day-{day:03}-learner.log", ROOT / "web")]
        else:
            ip = "ai.tests." + instructor[:-3]
            lp = "ai.coverstory.tests." + learner[:-3]
            if card == "CAP-63": lp = "ai.tests." + learner[:-3]
            ichecks = [run([sys.executable, "-m", "unittest", ip], RAW / f"day-{day:03}-instructor.log")]
            lchecks = [run([sys.executable, "-m", "unittest", lp], RAW / f"day-{day:03}-learner.log")]
        fixture_records = [{"path": p, "exists": (ROOT / p).is_file(), "sha256": sha(ROOT / p) if (ROOT / p).is_file() else None} for p in fixture_paths]
        learner_text = (ROOT / lchecks[0]["raw_log"]).read_text(encoding="utf-8", errors="replace")
        todo = any(token.lower() in learner_text.lower() for token in ("todo", "notimplementederror", "unsupportedoperationexception", "not implemented"))
        expected_learner = "EXPECTED_UNIMPLEMENTED_LEARNER_OUTCOME" if lchecks[0]["exit_code"] and todo else ("PASS" if lchecks[0]["exit_code"] == 0 else "UNEXPECTED_FAILURE")
        source_cards = [{"id": card, "role": "primary", "primary_id": card, "description_sha256": hashlib.sha256(cards[card]["description"].encode()).hexdigest()}]
        checks_ok = all(x["status"] == "PASS" for x in ichecks) and all(x["exists"] for x in fixture_records)
        report["days"].append({"day": day, "date": cards[card]["date"], "primary": card, "components": [], "source_cards": source_cards,
            "title": cards[card]["title"], "instructor_scaffold_readiness": "READY" if checks_ok else "BLOCKED",
            "learner_assignment_complete": False, "timebox_measured": False, "timebox_blocks_instructor_readiness": False,
            "checks": ichecks, "learner_acceptance_runs": [{**lchecks[0], "status": expected_learner, "todo_signal_observed": todo}],
            "fixtures": fixture_records, "reset_and_isolation": "Each command executes in a fresh process/test fixture; PostgreSQL cases use a fresh embedded instance per test." if card in ("CAP-57", "CAP-60") else "Read-only synthetic fixture and per-run pure/fake state; no customer data.",
            "positive_controls": positive, "negative_controls": negative, "module_wiring": wiring,
            "learner_todos": [f"{card} saved learner behavior"],
            "remaining_requirements": ["Implement the saved learner seam and rerun acceptance separately.", "The saved 150-minute budget remains unmeasured and does not establish task duration."]})
    (OUT / "readiness.json").write_text(json.dumps(report, indent=2) + "\n", encoding="utf-8")
    lines = ["# CoverStory Phase 1 Days 011–020 readiness", "", "Instructor readiness is based on current setup checks and checked synthetic fixtures. Learner runs are separate and do not determine this result. The saved 150-minute budget is unmeasured.", ""]
    for x in report["days"]:
        check_summary = ", ".join("`" + c["command"] + "` (" + c["status"] + ")" for c in x["checks"])
        lines += [f"## Day {x['day']:03} · {x['date']} · {x['primary']} — {x['instructor_scaffold_readiness']}", "", f"Instructor checks: {check_summary}.", f"Learner acceptance: `{x['learner_acceptance_runs'][0]['status']}`; TODO signal={x['learner_acceptance_runs'][0]['todo_signal_observed']}.", "Fixtures:"]
        lines += [f"- `{f['path']}` — SHA-256 `{f['sha256']}`" for f in x["fixtures"]]
        lines += [f"Reset/isolation: {x['reset_and_isolation']}", f"Module boundary: {x['module_wiring']}", f"Remaining: {' '.join(x['remaining_requirements'])}", ""]
    (OUT / "README.md").write_text("\n".join(lines), encoding="utf-8")
    summary = {"days_ready": sum(x["instructor_scaffold_readiness"] == "READY" for x in report["days"]), "days_total": 10,
               "instructor_commands": sum(len(x["checks"]) for x in report["days"]),
               "learner_commands": sum(len(x["learner_acceptance_runs"]) for x in report["days"]),
               "learner_todo_signals": sum(x["learner_acceptance_runs"][0]["todo_signal_observed"] for x in report["days"])}
    report["summary"] = summary
    (OUT / "readiness.json").write_text(json.dumps(report, indent=2) + "\n", encoding="utf-8")
    print(json.dumps(summary, indent=2))
    return 0 if summary["days_ready"] == 10 else 1

if __name__ == "__main__":
    raise SystemExit(main())
