#!/usr/bin/env python3
"""Render the 21 inserted, explicitly scoped roadmap unit contracts and owner cards."""
from __future__ import annotations

import argparse
import json
import re
from datetime import date, timedelta
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
OUT = ROOT / "docs/reboot/calendar"
START = date(2026, 10, 26)

OPENAPI = "https://spec.openapis.org/oas/v3.2.1.html"
SPRING_JWT = "https://docs.spring.io/spring-security/reference/servlet/oauth2/resource-server/jwt.html"
KEYCLOAK = "https://www.keycloak.org/docs/latest/server_admin/"
PG_ALTER = "https://www.postgresql.org/docs/18/sql-altertable.html"
PG_UPDATE = "https://www.postgresql.org/docs/18/sql-update.html"
FLYWAY_DATA = "https://documentation.red-gate.com/flyway/database-development-using-flyway/working-with-data"
SPRING_MULTI = "https://docs.spring.io/spring-security/reference/servlet/oauth2/resource-server/multitenancy.html"

UNITS = [
    ("GAP-OPENAPI-01", 71, "Observe producer and consumer behavior", "OpenAPI 3.2.1 — 4.1.2 OpenAPI Description Structure", OPENAPI,
     "Inspect LocalPolicyIssueController and PolicyIssueRequest. Record the two operations, auth boundary, request fields, returned status codes, and independent expected field sets. Do not change application behavior.",
     "./mvnw -Dtest=OpenApiCompatibilityInfrastructureTest test", "docs/reboot/evidence/gap-openapi-01.json", "25/85/10", "OpenAPICompatibilityInfrastructureTest: operation set and source DTO fields are fixed independently."),
    ("GAP-OPENAPI-02", 72, "Complete the versioned API description", "OpenAPI 3.2.1 — 4.8 Paths Object; 4.10 Operation Object; 4.24 Schema Object", OPENAPI,
     "Edit src/main/resources/contracts/coverstory-policies-v1.json to describe the existing POST and GET routes, request/response schemas, auth, and exact status variants including POST 200/201/400/401/404/409. Preserve the independent fixture assertions.",
     "./mvnw -Dtest=OpenApiCompatibilityInfrastructureTest test", "docs/reboot/evidence/gap-openapi-02.json", "25/85/10", "Infrastructure checks resolve required fields/statuses and reject undocumented or extra controller operations."),
    ("GAP-OPENAPI-03", 73, "Compare the contract with actual Spring mappings", "OpenAPI 3.2.1 — 4.9 Path Item Object; 4.10 Operation Object", OPENAPI,
     "Extend OpenApiCompatibilityInfrastructureTest to compare documented verb/path pairs against LocalPolicyIssueController annotations; include method, missing-route, and extra-route counterfactuals.",
     "./mvnw -Dtest=OpenApiCompatibilityInfrastructureTest#contractOperationsMatchTheActualSpringControllerMappings test", "docs/reboot/evidence/gap-openapi-03.json", "25/85/10", "Actual Spring annotations and contract operation sets must be equal; mutants adding/removing a route fail."),
    ("GAP-OPENAPI-04", 74, "Evaluate consumer compatibility for provider changes", "OpenAPI 3.2.1 — 4.16 Responses Object; 4.24 Schema Object", OPENAPI,
     "Implement the compatibility learner seam for additive optional response fields versus removed consumed fields, new required request fields, changed response status, and changed field types.",
     "./mvnw -Plearner-tests -Dtest=OpenApiCompatibilityAcceptanceTest test", "docs/reboot/evidence/gap-openapi-04.json", "25/85/10", "Independent expected diagnostics verify exact changed operations and preserve optional additions as compatible."),
    ("GAP-OPENAPI-05", 75, "Run compatibility mutation controls and capture evidence", "OpenAPI 3.2.1 — 4.16 Responses Object; 4.24 Schema Object", OPENAPI,
     "Run the compatibility witness against unchanged, additive, and breaking documents; introduce a deliberate always-compatible mutant and confirm the independent actual-path assertion rejects it.",
     "./mvnw -Dtest=OpenApiCompatibilityInfrastructureTest test", "docs/reboot/evidence/gap-openapi-05.json", "25/85/10", "The immutable expected operation/field/status diagnostics reject always-compatible and wrong-diagnostic mutants."),
    ("GAP-OIDC-01", 76, "Define an isolated resource-server lab seam", "Spring Security — Specifying the Authorization Server", SPRING_JWT,
     "Review the isolated oidc-contract-lab profile and loopback-only synthetic issuer. Trace the learner policy seam; preserve the existing cookie-session configuration and never configure an external issuer.",
     "./mvnw -Dtest=OidcContractLabInfrastructureTest test", "docs/reboot/evidence/gap-oidc-01.json", "25/85/10", "Infrastructure test proves issuer/JWKS URLs are loopback-only and the profile is separate from application sessions."),
    ("GAP-OIDC-02", 77, "Route bearer authentication through Spring Security", "Spring Security — Startup Expectations; Runtime Expectations", SPRING_JWT,
     "Trace the real SecurityFilterChain, issuer/time validator, audience validator, and default-deny route handling. Implement OidcTenantPolicy separately; do not replace token verification with a mock Authentication.",
     "./mvnw -Dtest=OidcContractLabInfrastructureTest test", "docs/reboot/evidence/gap-oidc-02.json", "25/85/10", "Actual MockMvc bearer path proves valid signed tokens reach authorization and invalid tokens fail before the handler."),
    ("GAP-OIDC-03", 78, "Exercise provider discovery and JWKS signature verification", "Spring Security — Specifying the Authorization Server; Startup Expectations", SPRING_JWT,
     "Use the disposable local issuer's discovery metadata and JWKS. Follow one signed synthetic token through discovery, key selection, signature verification, and the protected handler.",
     "./mvnw -Dtest=OidcContractLabInfrastructureTest#signedTokenDiscoveredThroughLocalIssuerReachesProtectedHandler test", "docs/reboot/evidence/gap-oidc-03.json", "25/85/10", "The test observes a real signed bearer request and successful protected response; mock tokens are not accepted by this path."),
    ("GAP-OIDC-04", 79, "Reject issuer, audience, time, and malformed-token failures", "Spring Security — Runtime Expectations; Supplying Audiences", SPRING_JWT,
     "Use independently signed wrong-issuer, wrong-audience, expired, and malformed tokens. Assert HTTP 401, no private body, and no successful handler response.",
     "./mvnw -Dtest=OidcContractLabInfrastructureTest#issuerAudienceAndExpiryFailuresReturn401WithoutClaims test", "docs/reboot/evidence/gap-oidc-04.json", "25/85/10", "Each counterfactual is independently expected to fail authentication and expose no synthetic subject."),
    ("GAP-OIDC-05", 80, "Reject unknown key identifiers and invalid token structure", "Spring Security — Runtime Expectations", SPRING_JWT,
     "Exercise malformed compact token and a validly signed token whose kid is absent from the JWKS. Confirm the filter chain returns 401 without claim disclosure.",
     "./mvnw -Dtest=OidcContractLabInfrastructureTest#rotationAcceptsNewKidAndRejectsUnknownSigningKey test", "docs/reboot/evidence/gap-oidc-05.json", "25/85/10", "Unknown kid and malformed token controls fail on actual decoder path; no unsigned-token bypass."),
    ("GAP-OIDC-06", 81, "Test key rotation and overlap", "Keycloak Server Administration Guide — Configuring realm keys; Rotating keys", KEYCLOAK,
     "Use only the local synthetic issuer. Verify the previously active and newly published key IDs during overlap, then an unknown-key denial; record that this lab does not establish immediate revocation behavior.",
     "./mvnw -Dtest=OidcContractLabInfrastructureTest#rotationAcceptsNewKidAndRejectsUnknownSigningKey test", "docs/reboot/evidence/gap-oidc-06.json", "25/85/10", "Positive controls for both published keys and a negative unknown-key control exercise JWKS retrieval."),
    ("GAP-OIDC-07", 82, "Enforce tenant ownership in the authorization path", "Spring Security — Multi-tenancy", SPRING_MULTI,
     "Implement the learner OidcTenantPolicy using exact tenant claim equality. Reject foreign, absent, blank, and wrongly typed claims; the instructor witness remains separate from learner implementation.",
     "./mvnw -Plearner-tests -Dtest=OidcTenantPolicyAcceptanceTest test", "docs/reboot/evidence/gap-oidc-07.json", "25/85/10", "Same-tenant positive control and malformed/foreign-tenant negative controls use fixed claims."),
    ("GAP-OIDC-08", 83, "Apply least-privilege scope checks", "Spring Security — Runtime Expectations; Supplying Audiences", SPRING_JWT,
     "Require the exact policy.read permission for this GET route. Verify policy.write, unknown scopes, substrings, blank scopes, and tenant mismatch do not authorize.",
     "./mvnw -Plearner-tests -Dtest=OidcTenantPolicyAcceptanceTest test", "docs/reboot/evidence/gap-oidc-08.json", "25/85/10", "The acceptance matrix tests scope tokenization and tenant relation independently."),
    ("GAP-OIDC-09", 84, "Run full protocol and authorization acceptance", "Spring Security — JWT Resource Server", SPRING_JWT,
     "Run the complete isolated protocol suite and learner policy acceptance. Preserve the actual issuer/JWKS test path and record any failure as a blocker, not completion.",
     "./mvnw -Dtest=OidcContractLabInfrastructureTest test && ./mvnw -Plearner-tests -Dtest=OidcTenantPolicyAcceptanceTest test", "docs/reboot/evidence/gap-oidc-09.json", "25/85/10", "Protocol infrastructure and learner authorization acceptance are separately reported."),
    ("GAP-SCHEMA-01", 169, "Pin the baseline rows and compatibility boundary", "PostgreSQL 18 — ALTER TABLE", PG_ALTER,
     "Use disposable embedded PostgreSQL and migration V001 in the isolated schema-evolution lab. Record exact fixture IDs/values and create an independent expected row map for old and new readers.",
     "./mvnw -Dtest=SchemaEvolutionInfrastructureTest test", "docs/reboot/evidence/gap-schema-01.json", "25/85/10", "Fixed expected rows and schema columns are asserted before any candidate migration."),
    ("GAP-SCHEMA-02", 170, "Apply an additive expand migration", "PostgreSQL 18 — ALTER TABLE", PG_ALTER,
     "Apply the lab's versioned nullable column migration. Verify the preexisting rows remain exact, old insert/read SQL still works, and the new column accepts a value.",
     "./mvnw -Dtest=SchemaEvolutionInfrastructureTest#expandMigrationPreservesRowsAndOldWriterCompatibility test", "docs/reboot/evidence/gap-schema-02.json", "25/85/10", "Independent row snapshot is byte-stable across expansion and an old-shape insert remains valid."),
    ("GAP-SCHEMA-03", 171, "Backfill bounded batches with a durable cursor", "PostgreSQL 18 — UPDATE", PG_UPDATE,
     "Implement the resumable backfill learner seam over stable integer IDs using bounded ordered batches and a stored cursor. The expected target values and IDs are fixed independently.",
     "./mvnw -Plearner-tests -Dtest=ResumableBackfillAcceptanceTest test", "docs/reboot/evidence/gap-schema-03.json", "25/85/10", "Acceptance compares exact IDs, values, cursor and total row count after each bounded batch."),
    ("GAP-SCHEMA-04", 172, "Interrupt and resume without skips or duplicates", "PostgreSQL 18 — UPDATE", PG_UPDATE,
     "Run two batches, close the runner, recreate it from the persisted cursor, and finish. Reject malformed/out-of-range cursors and record exact row-state before and after restart.",
     "./mvnw -Dtest=SchemaEvolutionInfrastructureTest#restartResumesFromPersistedCursorWithoutSkippingOrDuplicating test", "docs/reboot/evidence/gap-schema-04.json", "25/130/10 + shared 45", "Independent exact final rows and per-ID update counts prove restart correctness; wrong cursor mutant fails."),
    ("GAP-SCHEMA-05", 173, "Check old/new reader and writer compatibility", "PostgreSQL 18 — ALTER TABLE", PG_ALTER,
     "Execute fixed old-shape read/write SQL before and after backfill, and new-shape reads after it. Verify exact row sets and that the additive field does not break old code paths.",
     "./mvnw -Dtest=SchemaEvolutionInfrastructureTest#oldAndNewReadersRemainCompatibleAcrossBackfill test", "docs/reboot/evidence/gap-schema-05.json", "25/130/10 + shared 45", "Actual SQL queries for both schema generations match independent expected rows."),
    ("GAP-SCHEMA-06", 174, "Reject destructive change and demonstrate forward recovery", "PostgreSQL 18 — ALTER TABLE", PG_ALTER,
     "Prove a destructive candidate migration fails the schema/row acceptance boundary. Recover only by applying a forward migration in a fresh disposable DB and compare exact rows.",
     "./mvnw -Dtest=SchemaEvolutionInfrastructureTest#forwardMigrationRepairsDriftAndContractRequiresRetiredReaders test", "docs/reboot/evidence/gap-schema-06.json", "25/130/10 + shared 45", "A dropped-column mutant is rejected; forward migration restores the expected schema/values without claiming rollback semantics."),
    ("GAP-SCHEMA-07", 175, "Verify cleanup eligibility and publish migration evidence", "PostgreSQL 18 — ALTER TABLE; Flyway — Working with data", FLYWAY_DATA,
     "In a new disposable database, run the full expand/backfill/compatibility sequence and only contract after an independent retired-reader fixture says removal is safe. Capture exact migration history and row digest.",
     "./mvnw -Dtest=SchemaEvolutionInfrastructureTest test", "docs/reboot/evidence/gap-schema-07.json", "25/130/10 + shared 45", "Contract precondition is tested both true and false; exact expected rows, column set, and Flyway history are checked."),
]


def data() -> dict:
    rows = []
    for unit_id, study, title, section, url, assignment, command, evidence, allocation, acceptance in UNITS:
        if unit_id.startswith("GAP-OPENAPI"):
            architecture = ("Treat the versioned OpenAPI file as the consumer contract and compare it with the existing controller/DTO boundary. ")
            architecture += "The assistant supplies the current routes and expected compatibility policy; the learner implements the checker and its tests."
        elif unit_id.startswith("GAP-OIDC"):
            architecture = ("Keep this experiment in an isolated, stateless Spring Security resource-server profile. Spring verifies issuer, time, audience and JWKS signature; a small tenant/scope policy runs only after authentication. ")
            architecture += "The synthetic issuer binds to loopback, preserving the existing cookie-session application. The assistant supplies this architecture; the learner implements and tests its assigned policy seam."
        else:
            architecture = ("Use an expand/backfill/contract sequence: additive schema first, bounded stable-ID batches with row updates and cursor committed together, then destructive cleanup only after old-reader retirement. ")
            architecture += "The assistant supplies this migration sequence; the learner implements the backfill and eligibility seam against disposable PostgreSQL."
        rows.append({
            "unit_id": unit_id,
            "calendar_study_day": study,
            "date": (START + timedelta(days=study - 1)).isoformat(),
            "title": title,
            "category": unit_id.split("-")[1].lower(),
            "assistant_architecture": architecture + " No capstone design proposal or post-hoc design defense is assigned.",
            "assignment": assignment,
            "source": {"title": section.split(" — ")[0], "section": section.split(" — ")[1], "url": url, "status": "official_source_heading_verified"},
            "command": command,
            "evidence_path": evidence,
            "allocation": allocation,
            "acceptance": acceptance,
            "status": "not_attempted",
            "learner_completion_awarded": False,
        })
    return {"schema_version": 1, "status": "planned_contracts_not_learner_completion", "units": rows}


def render() -> dict[str, str]:
    payload = data()
    results = {"detailed-insertions.json": json.dumps(payload, indent=2) + "\n"}
    plan_path = OUT / "insertion-plan.json"
    plan = json.loads(plan_path.read_text(encoding="utf-8"))
    by_id = {unit["unit_id"]: unit for unit in payload["units"]}
    for unit in plan["detailed_inserted_units"]:
        detail = by_id[unit["unit_id"]]
        unit.update({key: detail[key] for key in ("title", "category", "assignment", "source", "command", "evidence_path", "allocation", "acceptance")})
        unit["task"] = detail["title"]
        unit["contract_path"] = f"docs/reboot/contracts/{unit['unit_id'].lower()}.md"
        unit["learner_completion_awarded"] = False
    results["insertion-plan.json"] = json.dumps(plan, indent=2) + "\n"
    owners = []
    question_bank = (ROOT / "docs/reboot/interview/question-bank.md").read_text(encoding="utf-8")
    for unit in payload["units"]:
        path = f"docs/reboot/contracts/{unit['unit_id'].lower()}.md"
        lines = [
            f"# Calendar study day {unit['calendar_study_day']} — {unit['unit_id']}: {unit['title']}", "",
            f"**Date:** {unit['date']}  ", f"**Budget:** {unit['allocation']} minutes  ",
            "**Status:** Not attempted; planning assignment only.", "",
            "## Assistant-provided architecture", "", unit["assistant_architecture"], "",
            "## Task", "", unit["assignment"], "",
            "## Source", "", f"[{unit['source']['title']} — {unit['source']['section']}]({unit['source']['url']})", "",
            "## Acceptance", "", unit["acceptance"], "",
            "## Command and evidence", "", f"`{unit['command']}`", "",
            f"Record the actual command, outcome, changed paths, blockers, and exact evidence at `{unit['evidence_path']}`. This is not learner completion until genuinely attempted and independently reviewed.", "",
        ]
        results[path.removeprefix("docs/reboot/")] = "\n".join(lines)
        owner = {
            "id": unit["unit_id"], "sequence": unit["calendar_study_day"], "day_number": unit["calendar_study_day"],
            "calendar_study_day": unit["calendar_study_day"], "date": unit["date"], "title": unit["title"],
            "status": "not_attempted", "contract_path": path, "category": unit["category"],
            "allocation": unit["allocation"], "assignment": unit["assignment"], "command": unit["command"],
            "evidence_path": unit["evidence_path"], "acceptance": unit["acceptance"],
            "source": unit["source"], "learner_completion_awarded": False,
        }
        if unit["calendar_study_day"] >= 172:
            q_number = 18 + unit["calendar_study_day"] - 172
            qid = f"Q{q_number:02}"
            match = re.search(rf"\*\*{qid} — ([^*]+)\*\*", question_bank)
            if not match:
                raise ValueError(f"question bank is missing {qid}")
            owner["shared_interview_block"] = {
                "minutes": 45,
                "mode": "separate_system_design_question_bank_session",
                "question_id": qid,
                "question_title": match.group(1).strip().rstrip("."),
                "question_bank": "docs/reboot/interview/question-bank.md",
                "task": f"Practice {qid} only in the interview question bank; record an answer before hints. No capstone design decision or proposal.",
                "inside_engineering_budget": False,
            }
        owners.append(owner)
    results["gap-owner-manifest.json"] = json.dumps({"schema_version": 1, "status": "planned_not_attempted", "cards": owners}, indent=2) + "\n"
    return results


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--check", action="store_true")
    args = parser.parse_args()
    failures = []
    for relative, content in render().items():
        if relative == "insertion-plan.json":
            path = OUT / relative
        else:
            path = (ROOT / "docs/reboot" / relative) if relative.endswith(".md") and relative.startswith("contracts/") else OUT / relative
        if args.check:
            if not path.exists() or path.read_text(encoding="utf-8") != content:
                failures.append(relative)
        else:
            path.parent.mkdir(parents=True, exist_ok=True)
            path.write_text(content, encoding="utf-8")
    if failures:
        print("stale generated gap assignment files: " + ", ".join(failures))
        return 1
    print(f"checked/rendered {len(render())} generated gap contract, owner, and source records")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
