#!/usr/bin/env python3
"""Run first-ten-day instructor harnesses and write source/fixture-backed readiness evidence.

This does not run or implement learner behavior. Set COVERSTORY_MAVEN when Maven needs
local settings, for example: COVERSTORY_MAVEN='mvn -s /tmp/maven-settings.xml'.
"""
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
OUT = ROOT / "docs/evidence/coverstory/phase1-days-001-010"
RAW = OUT / "raw"
MAVEN = shlex.split(os.environ.get("COVERSTORY_MAVEN", "mvn"))

DAYS = [
    {
        "day": 1, "date": "2026-10-14", "primary": "CAP-40", "components": ["CAP-41"],
        "java": "CAP40ScaffoldInfrastructureTest,ShortlistSelectionControllerInfrastructureTest,ApplicationRuntimeIntegrationTest",
        "python": ["ai.tests.test_coverstory_cap41_infrastructure", "ai.tests.test_coverstory_cap41_http"],
        "frontend": True,
        "fixtures": ["fixtures/coverstory/CAP-40/synthetic-v1.json", "ai/fixtures/coverstory/CAP-41/synthetic-v1.json"],
        "reset": "Read-only fixed synthetic JSON; Python HTTP harness loads the checked-in fixture per process; no shared mutable database.",
        "positive": ["first-seen composite selection order", "synthetic catalog through CatalogPort -> adapter -> live Spring HTTP", "CAP-41 exact citation transport with fixed documents"],
        "negative": ["version-only and sorted-order defects", "public endpoint safe 501 at learner TODO", "anonymous session/import private routes fail closed", "CAP-41 missing/foreign citation and TODO-safe HTTP 501"],
        "wiring": "Spring app startup, Catalog module, CAP-40 shortlist HTTP route and loopback CAP-41 service; React production build/security checks are run as shared setup checks.",
        "learner": ["CAP-40 uniqueSelections", "CAP-41 retrieve"],
        "learner_java": "CAP40AcceptanceTest", "learner_python": ["ai.tests.test_coverstory_cap41_acceptance"],
        "remaining": ["Implement the two named learner seams; keep public synthetic scope and safe 501 behavior until implemented.", "Learner duration remains unmeasured; this does not block instructor harness readiness."]
    },
    {
        "day": 2, "date": "2026-10-15", "primary": "CAP-42", "components": ["CAP-43"],
        "java": "CAP42InstructorInfrastructureTest,CAP43InstructorInfrastructureTest,CatalogInstructorHarnessTest,ApplicationRuntimeIntegrationTest",
        "fixtures": ["fixtures/coverstory/CAP-42/synthetic-v1.json", "fixtures/coverstory/CAP-43/synthetic-v1.json",
                     "src/test/resources/coverstory/catalog/cap_42_schema.sql", "src/test/resources/coverstory/catalog/cap_42_fixtures.sql"],
        "reset": "Every PostgreSQL instructor test starts and closes a fresh embedded PostgreSQL instance; JSON fixture is read-only.",
        "positive": ["exact numeric control total and half-open UTC window", "Catalog service/port bean graph starts and reaches CAP-43's explicit TODO", "sequential exact editor/source/version/hash reference"],
        "negative": ["wrong editor, inclusive upper bound, invalid currency", "version-only key and whitespace-trimmed opaque hash mutants", "real app does not claim a completed import"],
        "wiring": "CAP-42 use case delegates through the Catalog evidence port; CAP-43 service resolves the Catalog-owned learner port in the actual Spring context.",
        "learner": ["CAP-42 SQL aggregate", "CAP-43 classify/accept policy"],
        "learner_java": "CAP42SqlAcceptanceTest,CAP43AcceptanceTest",
        "remaining": ["Implement saved SQL/map policy only; persistent import storage, authenticated publishing and concurrent idempotency remain later product work.", "Learner duration remains unmeasured and does not block scaffold readiness."]
    },
    {
        "day": 3, "date": "2026-10-16", "primary": "CAP-44", "components": ["CAP-45"],
        "java": "CAP45InstructorInfrastructureTest",
        "python": ["ai.tests.test_coverstory_cap44_infrastructure"],
        "fixtures": ["fixtures/coverstory/CAP-44/synthetic-v1.json", "fixtures/coverstory/CAP-45/synthetic-v1.json",
                     "src/test/resources/coverstory/catalog/cap_45_schema.sql", "src/test/resources/coverstory/catalog/cap_45_fixtures.sql"],
        "reset": "CAP-45 creates a new embedded PostgreSQL schema/fixture per test; CAP-44 reads immutable synthetic public/restricted rows.",
        "positive": ["category/scenario/visibility-constrained stable ranking", "left-join evidence rows preserve nulls, attempts and exact source count"],
        "negative": ["visibility/category/order/duplicate-token ranking mutants", "inner join, source-only owner join and attempt fanout SQL mutants"],
        "wiring": "CAP-44 is a bounded pure Python function seam; CAP-45 test-only SQL adapter executes against real embedded PostgreSQL 14. No external retrieval provider.",
        "learner": ["CAP-44 rank_wording", "CAP-45 SQL reconciliation"],
        "learner_java": "CAP45SqlAcceptanceTest", "learner_python": ["discover:ai/coverstory/tests:test_cap_44.py"],
        "remaining": ["Implement the two saved learner seams; no product import ledger/provider is implied.", "Learner duration remains unmeasured and does not block scaffold readiness."]
    },
    {
        "day": 4, "date": "2026-10-17", "primary": "CAP-46", "components": ["CAP-47"],
        "java": "CAP46InstructorInfrastructureTest,CatalogInstructorHarnessTest",
        "python": ["ai.tests.test_coverstory_cap47_infrastructure"],
        "fixtures": ["fixtures/coverstory/CAP-46/synthetic-v1.json", "fixtures/coverstory/CAP-47/synthetic-v1.json"],
        "reset": "Each oracle replay constructs fresh in-memory queue/evaluation state from immutable fixtures.",
        "positive": ["bounded FIFO operations, empty poll and pending-only uniqueness", "exact source/version support and correct abstention metrics"],
        "negative": ["capacity overflow, LIFO ordering and permanent-duplicate mutants", "presence-only, foreign citation, invalid answer map and abstention-with-citation mutants"],
        "wiring": "Typed Catalog SourceVersionKey and CAP-46 TODO seam are compiled/discoverable; evaluation component is isolated Python fixture code. No broker or model is started.",
        "learner": ["CAP-46 bounded queue methods", "CAP-47 evaluate"],
        "learner_java": "CAP46AcceptanceTest", "learner_python": ["discover:ai/coverstory/tests:test_cap_47.py"],
        "remaining": ["Implement only the saved queue and evaluation seams; a real message broker and factual evaluation are outside these contracts.", "Learner duration remains unmeasured and does not block scaffold readiness."]
    },
    {
        "day": 5, "date": "2026-10-18", "primary": "CAP-48", "components": ["CAP-49"],
        "java": "CAP48InstructorInfrastructureTest",
        "python": ["ai.tests.test_coverstory_cap49_infrastructure"],
        "fixtures": ["fixtures/coverstory/CAP-48/synthetic-v1.json", "fixtures/coverstory/CAP-49/synthetic-v1.json",
                     "src/test/resources/coverstory/catalog/cap_48_schema.sql", "src/test/resources/coverstory/catalog/cap_48_fixtures.sql"],
        "reset": "Fresh embedded PostgreSQL schema and fixed UTC rows for each test; classifier fixture is read-only.",
        "positive": ["explicit UTC date spine under UTC and Asia/Kolkata session zones", "failure taxonomy precedence for correct abstention and citation errors"],
        "negative": ["observed-only dates, local timestamp cast, wrong owner", "unsupported-answer, source/version precedence and false-abstention controls"],
        "wiring": "CAP-48 SQL is exercised through test-only JDBC against embedded PostgreSQL; CAP-49 is a pure Python classification seam.",
        "learner": ["CAP-48 date-spine aggregate SQL", "CAP-49 classify_failure"],
        "learner_java": "CAP48SqlAcceptanceTest", "learner_python": ["discover:ai/coverstory/tests:test_cap_49.py"],
        "remaining": ["Implement only saved SQL/classifier seams; no live wording or provider source is connected.", "Learner duration remains unmeasured and does not block scaffold readiness."]
    },
    {
        "day": 6, "date": "2026-10-19", "primary": "CAP-50", "components": ["CAP-51"],
        "java": "CAP50InstructorInfrastructureTest,CAP51InstructorInfrastructureTest,CatalogInstructorHarnessTest",
        "fixtures": ["fixtures/coverstory/CAP-50/synthetic-v1.json", "fixtures/coverstory/CAP-51/synthetic-v1.json",
                     "src/test/resources/coverstory/catalog/cap_51_schema.sql", "src/test/resources/coverstory/catalog/cap_51_fixtures.sql"],
        "reset": "Fresh fake identity map per CAP-50 oracle; fresh embedded PostgreSQL schema/fixtures per CAP-51 test.",
        "positive": ["ACCEPT counted once through test-only Catalog service fake", "EXISTS-style confirmed-attempt totals for own fixture owner"],
        "negative": ["same-hash duplicate, changed-hash conflict and foreign-editor scope", "raw join fanout and source-only cross-editor attempt mutants"],
        "wiring": "CAP-50 service/fake lives behind Catalog-owned ports; CAP-51 SQL is a test-only adapter on PostgreSQL. No durable production import repository.",
        "learner": ["CAP-50 acceptedCounts", "CAP-51 SQL aggregate"],
        "learner_java": "CAP50AcceptanceTest,CAP51SqlAcceptanceTest",
        "remaining": ["Implement saved learner methods; persistent identity/retries and transaction guarantees remain unclaimed.", "Learner duration remains unmeasured and does not block scaffold readiness."]
    },
    {
        "day": 7, "date": "2026-10-20", "primary": "CAP-165", "components": [],
        "java": "CAP165HarnessDiscriminationTest,CatalogInstructorHarnessTest",
        "fixtures": ["fixtures/coverstory/CAP-165/synthetic-v1.json"],
        "reset": "Fresh pure-value vectors are read from immutable JSON; no database or process state persists.",
        "positive": ["exact source/version pair key creates a typed identity"],
        "negative": ["delimiter-concatenation collision and null/blank components"],
        "wiring": "Catalog-owned SourceVersionKey is the module value type; CAP165Seam remains the learner-owned factory TODO.",
        "learner": ["CAP-165 SourceVersionKey create"],
        "learner_java": "CAP165AcceptanceTest",
        "remaining": ["Implement and use the saved key factory; no persistence is in this day's contract.", "Learner duration remains unmeasured and does not block scaffold readiness."]
    },
    {
        "day": 8, "date": "2026-10-21", "primary": "CAP-52", "components": [],
        "java": "CAP52ControllerInfrastructureTest,ApplicationRuntimeIntegrationTest",
        "fixtures": ["fixtures/coverstory/CAP-52/synthetic-v1.json"],
        "reset": "Each MockMvc request uses fresh in-memory decision fake/counters; actual Spring check uses a fresh random-port application context.",
        "positive": ["trusted test principal reaches the injected fake decision port once", "route is registered in the actual application context"],
        "negative": ["anonymous/applicant denied, spoofed owner and malformed hash rejected before seam", "actual route returns 401 without the trusted-principal adapter"],
        "wiring": "Spring controller is component-scanned and tested; trusted identity adapter and durable decision persistence remain explicitly absent.",
        "learner": ["CAP-52 import accept decision"],
        "learner_java": "CAP52AcceptanceTest",
        "remaining": ["Implement saved decision seam; production authentication and durable idempotency are not supplied by this day.", "Learner duration remains unmeasured and does not block scaffold readiness."]
    },
    {
        "day": 9, "date": "2026-10-22", "primary": "CAP-53", "components": [],
        "python": ["ai.tests.test_coverstory_cap53_infrastructure"],
        "fixtures": ["fixtures/coverstory/CAP-53/synthetic-v1.json", "fixtures/coverstory/CAP-53/corpus/manifest.json", "fixtures/coverstory/CAP-53/corpus/a.json", "fixtures/coverstory/CAP-53/corpus/b.json"],
        "reset": "Immutable checked-in corpus plus TemporaryDirectory cases; every oracle invocation rebuilds its state and does not call the learner loader.",
        "positive": ["allowlisted filename ordering, exact source/version labels and repeatable corpus read", "unlisted file ignored"],
        "negative": ["path traversal rejected before outside read, duplicate file/source-version, restricted content and atomic rejection"],
        "wiring": "Typed PublicWording/LoadError seam and acceptance tests compile; test-only reference loader does not implement the learner function.",
        "learner": ["CAP-53 load_wording"],
        "learner_python": ["discover:ai/coverstory/tests:test_cap_53.py"],
        "remaining": ["Implement allowlisted local-file loading; no online/provider import is in scope.", "Learner duration remains unmeasured and does not block scaffold readiness."]
    },
    {
        "day": 10, "date": "2026-10-23", "primary": "CAP-54", "components": [],
        "java": "CAP54InstructorInfrastructureTest",
        "java_browser": "CAP54HttpRuntimeIntegrationTest",
        "frontend": True,
        "fixtures": ["fixtures/coverstory/CAP-54/synthetic-v1.json"],
        "reset": "Test-only fixture port graph is reconstructed per test from immutable synthetic owner/revision rows.",
        "positive": ["owner-bound lookup and exact authoritative application revision", "safe status/action summaries and bounded owner listing"],
        "negative": ["foreign and absent UUID produce same safe 404 before application read", "UUID-only access, private-field output, oversized page and foreign cursor"],
        "wiring": "Actual Spring /api/session and /api/v1/me/journeys routes resolve only a trusted request principal. Application-owned JDBC reads join the owner-scoped journey to the exact linked application revision. A test-only synthetic session and independent PostgreSQL adapter exercise the actual HTTP/browser path; production auth binding and the saved CAP54Seam remain learner work and fail closed.",
        "contract_ready": True,
        "learner": ["CAP-54 resume and active-list seam"],
        "learner_java": "CAP54AcceptanceTest",
        "remaining": ["Bind the trusted servlet principal to the deployment's authenticated session adapter; no login provider or real credential flow is included. Implement the CAP54Seam learner methods; the configured application route returns safe 501 until they are complete. Learner duration remains unmeasured."]
    },
]


def sha256(path: Path) -> str:
    return hashlib.sha256(path.read_bytes()).hexdigest()


def run(command: list[str], log_path: Path, *, cwd: Path = ROOT, env: dict[str, str] | None = None) -> dict:
    result = subprocess.run(command, cwd=cwd, text=True, stdout=subprocess.PIPE,
                            stderr=subprocess.STDOUT, env=env, check=False)
    log_path.parent.mkdir(parents=True, exist_ok=True)
    log_path.write_text(result.stdout, encoding="utf-8")
    tests = None
    java = re.findall(r"Tests run: (\d+), Failures: (\d+), Errors: (\d+), Skipped: (\d+)", result.stdout)
    if java:
        row = java[-1]  # Maven's last match is its aggregate suite total.
        tests = {"tests": int(row[0]), "failures": int(row[1]),
                 "errors": int(row[2]), "skipped": int(row[3])}
    python = re.findall(r"Ran (\d+) tests?", result.stdout)
    if python:
        tests = {"tests": sum(int(n) for n in python), "failures": 0, "errors": 0, "skipped": 0}
    return {"command": shlex.join(command), "exit_code": result.returncode,
            "status": "PASS" if result.returncode == 0 else "FAIL",
            "test_counts": tests, "raw_log": log_path.relative_to(ROOT).as_posix(),
            "raw_log_sha256": sha256(log_path)}


def run_learner(command: list[str], log_path: Path) -> dict:
    result = subprocess.run(command, cwd=ROOT, text=True, stdout=subprocess.PIPE,
                            stderr=subprocess.STDOUT, check=False)
    log_path.parent.mkdir(parents=True, exist_ok=True)
    log_path.write_text(result.stdout, encoding="utf-8")
    tests = None
    java = re.findall(r"Tests run: (\d+), Failures: (\d+), Errors: (\d+), Skipped: (\d+)", result.stdout)
    if java:
        row = java[-1]
        tests = {"tests": int(row[0]), "failures": int(row[1]), "errors": int(row[2]), "skipped": int(row[3])}
    else:
        python = re.findall(r"Ran (\d+) tests?", result.stdout)
        if python:
            failure_match = re.search(r"FAILED \(failures=(\d+)(?:, errors=(\d+))?\)", result.stdout)
            errors_match = re.search(r"FAILED \(errors=(\d+)\)", result.stdout)
            tests = {"tests": int(python[-1]),
                     "failures": int(failure_match.group(1)) if failure_match else 0,
                     "errors": int(failure_match.group(2) or 0) if failure_match else int(errors_match.group(1)) if errors_match else 0,
                     "skipped": 0}
    has_todo = bool(re.search(
        r"LEARNER TODO|TODO\(|NotImplementedError|not implemented|empty shape-only query|"
        r"expected when learner SQL remains the empty TODO|learner query must emit the date spine|"
        r"learner SQL remains the empty TODO",
        result.stdout, re.I))
    if result.returncode == 0:
        status = "PASS_NO_TODO_FAILURE" if not has_todo else "TODO_GUARD_PASS_NOT_LEARNER_COMPLETION"
    elif has_todo:
        status = "EXPECTED_UNIMPLEMENTED_LEARNER_OUTCOME"
    else:
        status = "UNEXPECTED_LEARNER_RUN_FAILURE"
    return {"command": shlex.join(command), "exit_code": result.returncode, "status": status,
            "todo_signal_observed": has_todo, "test_counts": tests,
            "raw_log": log_path.relative_to(ROOT).as_posix(), "raw_log_sha256": sha256(log_path)}


def main() -> int:
    source = json.loads(SOURCE.read_text(encoding="utf-8"))
    cards = {card["id"]: card for card in source["cards"]}
    RAW.mkdir(parents=True, exist_ok=True)
    report = {"title": "CoverStory Phase 1 Days 001–010 instructor readiness", "recorded_at_utc": datetime.now(timezone.utc).isoformat(),
              "canonical_curriculum_sha256": sha256(SOURCE), "readiness_basis": "Instructor harness executability, deterministic synthetic inputs, positive/negative controls, and declared module wiring only. Learner completion and unmeasured task duration do not gate this status.",
              "timebox_status": "UNMEASURED_NOT_GATING_INSTRUCTOR_READINESS", "days": []}
    for day in DAYS:
        number = day["day"]
        checks = []
        if day.get("java"):
            command = MAVEN + [f"-Dtest={day['java']}", "test"]
            checks.append(run(command, RAW / f"day-{number:03}-maven.log"))
        if day.get("java_browser"):
            command = MAVEN + ["-Dcoverstory.cap54.browser=true", f"-Dtest={day['java_browser']}", "test"]
            checks.append(run(command, RAW / f"day-{number:03}-http-browser.log"))
        for module in day.get("python", []):
            command = [sys.executable, "-m", "unittest", module]
            checks.append(run(command, RAW / f"day-{number:03}-{module.split('.')[-1]}.log"))
        if day.get("frontend"):
            for args, label in ((["npm", "run", "build"], "frontend-build"), (["npm", "run", "test:security"], "frontend-security")):
                checks.append(run(args, RAW / f"day-{number:03}-{label}.log", cwd=ROOT / "web",
                                  env={**os.environ, "npm_config_cache": "/tmp/capstone-npm-cache"}))
        learner_checks = []
        if day.get("learner_java"):
            command = MAVEN + ["-Plearner-tests", f"-Dtest={day['learner_java']}", "test"]
            learner_checks.append(run_learner(command, RAW / f"day-{number:03}-learner-maven.log"))
        for learner_spec in day.get("learner_python", []):
            if learner_spec.startswith("discover:"):
                _, start_dir, pattern = learner_spec.split(":", 2)
                command = [sys.executable, "-m", "unittest", "discover", "-s", start_dir, "-p", pattern]
            else:
                command = [sys.executable, "-m", "unittest", learner_spec]
            learner_checks.append(run_learner(command, RAW / f"day-{number:03}-learner-{Path(command[-1]).stem}.log"))
        primary_card = cards[day["primary"]]
        card_ids = [day["primary"], *day["components"]]
        source_cards = []
        for card_id in card_ids:
            card = cards[card_id]
            source_cards.append({"id": card_id, "role": card["role"], "primary_id": card["primary_id"],
                                 "description_sha256": hashlib.sha256(card["description"].encode("utf-8")).hexdigest()})
        fixture_hashes = []
        for item in day["fixtures"]:
            path = ROOT / item
            fixture_hashes.append({"path": item, "sha256": sha256(path) if path.is_file() else None,
                                   "exists": path.is_file()})
        learner_receipts = []
        for card_id in card_ids:
            receipt_path = ROOT / "docs/evidence/coverstory" / card_id / "result.json"
            supplementary = ROOT / "docs/evidence/coverstory" / card_id / "learner-acceptance.json"
            receipt = json.loads(receipt_path.read_text(encoding="utf-8")) if receipt_path.is_file() else {}
            learner_receipts.append({
                "card_id": card_id,
                "result_receipt": receipt_path.relative_to(ROOT).as_posix() if receipt_path.is_file() else None,
                "supplementary_acceptance_receipt": supplementary.relative_to(ROOT).as_posix() if supplementary.is_file() else None,
                "previously_recorded_learner_logic_status": receipt.get("learner_logic_status") or receipt.get("learner_result") or receipt.get("learner_decision"),
                "previously_recorded_acceptance": receipt.get("learner_acceptance") or receipt.get("learner_acceptance_runs") or None
            })
        checks_pass = all(check["status"] == "PASS" for check in checks) and all(f["exists"] for f in fixture_hashes)
        ready = checks_pass and day.get("contract_ready", True)
        readiness = "READY" if ready else "PARTIAL" if checks_pass else "BLOCKED"
        report["days"].append({"day": number, "date": day["date"], "primary": day["primary"], "components": day["components"],
                               "title": primary_card["title"], "source_cards": source_cards,
                               "instructor_scaffold_readiness": readiness,
                               "learner_assignment_complete": False, "timebox_measured": False,
                               "timebox_blocks_instructor_readiness": False, "checks": checks,
                               "fixtures": fixture_hashes, "reset_and_isolation": day["reset"],
                               "positive_controls": day["positive"], "negative_controls": day["negative"],
                               "module_wiring": day["wiring"], "learner_todos": day["learner"], "learner_result_receipts": learner_receipts,
                               "learner_acceptance_runs": learner_checks,
                               "remaining_requirements": day["remaining"]})
        print(f"Day {number:03}: {readiness} " + "; ".join(f"{c['status']}:{c['command']}" for c in checks))
    report["summary"] = {
        "days_ready": sum(day["instructor_scaffold_readiness"] == "READY" for day in report["days"]),
        "days_partial": sum(day["instructor_scaffold_readiness"] == "PARTIAL" for day in report["days"]),
        "days_total": len(report["days"]),
        "commands_run": sum(len(day["checks"]) for day in report["days"]),
        "tests_run": sum((check.get("test_counts") or {}).get("tests", 0)
                         for day in report["days"] for check in day["checks"]),
        "test_failures": sum((check.get("test_counts") or {}).get("failures", 0)
                             for day in report["days"] for check in day["checks"]),
        "test_errors": sum((check.get("test_counts") or {}).get("errors", 0)
                           for day in report["days"] for check in day["checks"]),
        "frontend_commands_passed": sum(check["status"] == "PASS" and check.get("test_counts") is None
                                        for day in report["days"] for check in day["checks"]),
        "learner_acceptance_commands": sum(len(day["learner_acceptance_runs"]) for day in report["days"]),
        "learner_commands_with_todo_signal": sum(check["todo_signal_observed"] for day in report["days"] for check in day["learner_acceptance_runs"]),
    }
    OUT.mkdir(parents=True, exist_ok=True)
    destination = OUT / "readiness.json"
    destination.write_text(json.dumps(report, indent=2, ensure_ascii=False) + "\n", encoding="utf-8")
    md = ["# Phase 1 Days 001–010 instructor readiness", "",
          "This status is based on executable instructor scaffolds, resettable synthetic inputs, positive/negative controls, and the module boundary named for each day. It is independent of learner implementation and elapsed-time measurement. A READY scaffold is not learner completion or mastery.", "",
          f"Recorded UTC: `{report['recorded_at_utc']}`  ",
          f"Canonical source SHA-256: `{report['canonical_curriculum_sha256']}`", "",
          f"**Instructor result:** {report['summary']['days_ready']}/{report['summary']['days_total']} days READY; {report['summary']['commands_run']} commands, {report['summary']['tests_run']} tests, {report['summary']['test_failures']} test failures, {report['summary']['test_errors']} test errors, and {report['summary']['frontend_commands_passed']} frontend build/security checks passed.", "",
          f"**Separate learner checks:** {report['summary']['learner_acceptance_commands']} commands; {report['summary']['learner_commands_with_todo_signal']} observed the preserved TODO/empty learner seam. These red outcomes do not determine instructor readiness and are not completed assignments.", "",
          "| Day | Primary / linked cards | Instructor status | Instructor tests | Learner-owned seams still TODO |", "|---:|---|---|---:|---|"]
    for day in report["days"]:
        test_total = sum((check.get("test_counts") or {}).get("tests", 0) for check in day["checks"])
        md.append(f"| {day['day']:03} | {day['primary']} / {', '.join(day['components']) or '—'} | {day['instructor_scaffold_readiness']} | {test_total} | {', '.join(day['learner_todos'])} |")
    md.extend(["", "## Per-day evidence", ""])
    for day in report["days"]:
        md.extend([f"### Day {day['day']:03} · {day['date']} · {day['primary']}", "",
                   f"**Instructor readiness:** `{day['instructor_scaffold_readiness']}`. Learner complete: `false`; timebox measured: `false`; unmeasured timebox blocks this instructor status: `false`.", "",
                   "**Executed checks:**"])
        for check in day["checks"]:
            counts = check.get("test_counts")
            suffix = (f" — {counts['tests']} tests, {counts['failures']} failures, {counts['errors']} errors, {counts['skipped']} skipped" if counts else "")
            md.append(f"- `{check['command']}`: **{check['status']}**{suffix}; raw output [log](raw/{Path(check['raw_log']).name}) (`{check['raw_log_sha256']}`).")
        md.append("")
        md.extend(["**Separate learner acceptance runs (not used to decide scaffold readiness):**"])
        for check in day["learner_acceptance_runs"]:
            counts = check.get("test_counts")
            suffix = (f" — {counts['tests']} tests, {counts['failures']} failures, {counts['errors']} errors" if counts else "")
            md.append(f"- `{check['command']}`: **{check['status']}**{suffix}; TODO signal: `{check['todo_signal_observed']}`; [raw log](raw/{Path(check['raw_log']).name}) (`{check['raw_log_sha256']}`).")
        md.append("")
        md.append("**Fixture SHA-256:**")
        for fixture in day["fixtures"]:
            md.append(f"- `{fixture['path']}` — `{fixture['sha256']}`")
        md.extend(["", f"**Reset/isolation:** {day['reset_and_isolation']}", "",
                   f"**Wiring:** {day['module_wiring']}", "",
                   f"**Positive controls:** {'; '.join(day['positive_controls'])}.", "",
                   f"**Negative controls:** {'; '.join(day['negative_controls'])}.", "",
                   f"**Learner TODOs:** {', '.join(day['learner_todos'])}. These are deliberately not implemented by the instructor harness.", "",
                   "**Last recorded learner acceptance receipts:**"])
        for receipt in day["learner_result_receipts"]:
            link = f"[{receipt['card_id']} result](../{receipt['card_id']}/result.json)" if receipt["result_receipt"] else f"{receipt['card_id']} receipt missing"
            if receipt["supplementary_acceptance_receipt"]:
                link += f"; [supplementary acceptance](../{receipt['card_id']}/learner-acceptance.json)"
            md.append(f"- {link}; recorded status `{receipt['previously_recorded_learner_logic_status']}`. These are prior separated TODO evidence, not counted in today's instructor PASS.")
        md.extend([
                   f"**Remaining requirements:** {'; '.join(day['remaining_requirements'])}", ""])
    (OUT / "README.md").write_text("\n".join(md), encoding="utf-8")
    print(f"Evidence: {destination.relative_to(ROOT)}")
    return 0 if all(d["instructor_scaffold_readiness"] in {"READY", "PARTIAL"} for d in report["days"]) else 1


if __name__ == "__main__":
    raise SystemExit(main())
