#!/usr/bin/env python3
"""Run chronological Day 041–060 instructor setups and emit a limited-scope receipt.

This runner distinguishes green fixture/harness setup from saved-contract readiness.
It never runs learner acceptance suites and never calls a harness-only result READY.
Raw test output is kept in the caller's temporary directory, outside the repository.
"""
from __future__ import annotations

import hashlib
import json
import os
import re
import shlex
import subprocess
import sys
import tempfile
from datetime import datetime, timezone
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
SOURCE = ROOT / "docs/source-inputs/2026-10-14/curriculum/coverstory-final-saved-contracts-594-days.json"
MAVEN = shlex.split(os.environ.get("COVERSTORY_MAVEN", "mvn"))
JAVA = [sys.executable, "-m", "unittest"]

# Exact Day -> primary mapping from the saved calendar. Each command is an
# instructor-owned setup/oracle only; the full saved flow is assessed below.
DAYS = [
    (41, "CAP-81", "python", "ai.tests.test_coverstory_cap81_infrastructure"),
    (42, "CAP-170", "java", "CAP170InstructorInfrastructureTest"),
    (43, "CAP-82", "python", "ai.tests.test_coverstory_cap82_infrastructure"),
    (44, "CAP-83", "java", "CAP83InstructorInfrastructureTest"),
    (45, "CAP-84", "python", "ai.tests.test_coverstory_cap84_infrastructure"),
    (46, "CAP-85", "java", "CAP85InstructorInfrastructureTest"),
    (47, "CAP-86", "python", "ai.tests.test_coverstory_cap86_infrastructure"),
    (48, "CAP-87", "java", "capstone.coverstory.Day48To56InstructorScaffoldTest"),
    (49, "CAP-171", "java", "capstone.coverstory.Day48To56InstructorScaffoldTest"),
    (50, "CAP-88", "java", "CAP88InstructorInfrastructureTest"),
    (51, "CAP-89", "python", "ai.tests.test_coverstory_cap89_infrastructure"),
    (52, "CAP-90", "java", "CAP90InstructorInfrastructureTest"),
    (53, "CAP-91", "java", "CAP91InstructorInfrastructureTest"),
    (54, "CAP-92", "java", "CAP92InstructorInfrastructureTest"),
    (55, "CAP-93", "java", "CAP93InstructorInfrastructureTest"),
    (56, "CAP-172", "java", "capstone.coverstory.Day48To56InstructorScaffoldTest"),
    (57, "CAP-94", "java", "CAP94InstructorInfrastructureTest"),
    (58, "CAP-95", "java", "CAP95InstructorInfrastructureTest"),
    (59, "CAP-96", "python", "ai.tests.test_coverstory_cap96_infrastructure"),
    (60, "CAP-97", "java", "CAP97InstructorInfrastructureTest"),
]


def digest(path: Path) -> str:
    return hashlib.sha256(path.read_bytes()).hexdigest()


def command_for(kind: str, target: str) -> list[str]:
    if kind == "python":
        return JAVA + [target]
    return MAVEN + ["-Dtest=" + target, "test"]


def run(cmd: list[str], log_path: Path) -> dict:
    result = subprocess.run(cmd, cwd=ROOT, text=True, stdout=subprocess.PIPE, stderr=subprocess.STDOUT)
    log_path.write_text(result.stdout, encoding="utf-8")
    java = re.findall(r"Tests run: (\d+), Failures: (\d+), Errors: (\d+), Skipped: (\d+).*? -- in ", result.stdout)
    py = re.search(r"Ran (\d+) tests?", result.stdout)
    counts = ({"tests": sum(int(r[0]) for r in java), "failures": sum(int(r[1]) for r in java),
               "errors": sum(int(r[2]) for r in java), "skipped": sum(int(r[3]) for r in java)}
              if java else ({"tests": int(py.group(1)), "failures": 0, "errors": 0, "skipped": 0} if py else None))
    return {"command": shlex.join(cmd), "exit_code": result.returncode,
            "status": "PASS" if result.returncode == 0 else "FAIL", "counts": counts,
            "raw_output_sha256": digest(log_path)}


def main() -> int:
    source = json.loads(SOURCE.read_text(encoding="utf-8"))
    cards = {row["id"]: row for row in source["cards"]}
    evidence = ROOT / "docs/evidence/coverstory/phase1-days-041-060"
    evidence.mkdir(parents=True, exist_ok=True)
    tmp = Path(tempfile.mkdtemp(prefix="coverstory-days-041-060-"))
    report = {"title": "CoverStory Phase 1 Days 041–060 instructor scaffold evidence",
              "recorded_at_utc": datetime.now(timezone.utc).isoformat(),
              "canonical_curriculum_sha256": digest(SOURCE),
              "scope": "Instructor setup/oracle checks only. Harness PASS does not establish full saved-contract readiness.",
              "raw_output_directory": str(tmp), "learner_assignments_completed": 0,
              "learner_acceptance_runs": 0, "days": []}
    for day, card_id, kind, target in DAYS:
        card = cards[card_id]
        cmd = command_for(kind, target)
        check = run(cmd, tmp / f"day-{day:03}-{card_id}.log")
        fixture_path = {"CAP-89": "ai/fixtures/coverstory/CAP-89/synthetic-v1.json",
                        "CAP-96": "fixtures/coverstory/CAP-96/cases.json"}.get(
                            card_id, f"fixtures/coverstory/{card_id}/synthetic-v1.json")
        fixture = ROOT / fixture_path
        fixture_record = {"path": fixture.relative_to(ROOT).as_posix(), "exists": fixture.is_file(),
                          "sha256": digest(fixture) if fixture.is_file() else None}
        status = "PARTIAL" if check["status"] == "PASS" and fixture_record["exists"] else "BLOCKED"
        if card_id in {"CAP-87", "CAP-171", "CAP-172"}:
            case_coverage = "fixture_oracle_only; customer/service boundary remains absent"
        else:
            case_coverage = "saved family oracle only; not full live application path"
        report["days"].append({"day": day, "date": card["date"], "primary": card_id,
                               "title": card["title"], "instructor_scaffold_status": check["status"],
                               "contract_readiness": status, "check": check, "fixture": fixture_record,
                               "learner_logic_status": "TODO_RETAINED", "learner_acceptance": "NOT_RUN",
                               "saved_contract_execution_scope": case_coverage,
                               "remaining": "Run a contract-specific application/API/UI acceptance path and timebox rehearsal before READY."})
    counts = [d["check"]["counts"] for d in report["days"] if d["check"]["counts"]]
    report["summary"] = {"days_total": 20,
                         "harness_pass": sum(d["instructor_scaffold_status"] == "PASS" for d in report["days"]),
                         "contract_ready": sum(d["contract_readiness"] == "READY" for d in report["days"]),
                         "partial_oracle_only": sum(d["contract_readiness"] == "PARTIAL" for d in report["days"]),
                         "blocked": sum(d["contract_readiness"] == "BLOCKED" for d in report["days"]),
                         "instructor_tests": sum(c["tests"] for c in counts),
                         "learner_assignments_completed": 0}
    (evidence / "readiness.json").write_text(json.dumps(report, indent=2) + "\n", encoding="utf-8")
    lines = ["# CoverStory Phase 1 Days 041–060 scaffold evidence", "",
             f"Harness commands: {report['summary']['harness_pass']}/20 passed ({report['summary']['instructor_tests']} tests). Contract readiness: {report['summary']['contract_ready']} READY, {report['summary']['partial_oracle_only']} PARTIAL, {report['summary']['blocked']} BLOCKED.",
             "A passing harness is not a full-day contract pass. No learner acceptance suite ran and no learner assignment is complete.", ""]
    for row in report["days"]:
        check = row["check"]
        lines += [f"## Day {row['day']:03} · {row['date']} · {row['primary']} — {row['contract_readiness']}", "",
                  f"`{check['command']}` — {check['status']}; counts `{check['counts']}`; output SHA-256 `{check['raw_output_sha256']}`.",
                  f"Fixture: `{row['fixture']['path']}` SHA-256 `{row['fixture']['sha256']}`.",
                  f"Contract scope: {row['saved_contract_execution_scope']}. Learner: {row['learner_logic_status']}; acceptance: not run.",
                  row["remaining"], ""]
    (evidence / "README.md").write_text("\n".join(lines), encoding="utf-8")
    print(json.dumps(report["summary"], indent=2))
    return 0 if report["summary"]["harness_pass"] == 20 else 1


if __name__ == "__main__":
    raise SystemExit(main())
