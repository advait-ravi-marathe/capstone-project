#!/usr/bin/env python3
"""Generate the first 30 AI-first unit contracts and their manifest."""
from __future__ import annotations

import argparse
import json
from datetime import date, timedelta
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
OUT = ROOT / "docs" / "ai-first-roadmap"

SOURCES = {
    "python-json": {
        "title": "json — JSON encoder and decoder",
        "section": "Basic Usage",
        "url": "https://docs.python.org/3/library/json.html",
    },
    "python-unittest": {
        "title": "unittest — Unit testing framework",
        "section": "Test case",
        "url": "https://docs.python.org/3/library/unittest.html",
    },
    "python-http": {
        "title": "urllib.request — Extensible library for opening URLs",
        "section": "urllib.request.urlopen",
        "url": "https://docs.python.org/3/library/urllib.request.html",
    },
    "text": {
        "title": "Text generation | OpenAI API",
        "section": "Choosing models and APIs; Message roles and instruction following",
        "url": "https://developers.openai.com/api/docs/guides/text",
    },
    "structured": {
        "title": "Structured model outputs | OpenAI API",
        "section": "When to use Structured Outputs via function calling vs via text.format; Structured Outputs vs JSON mode",
        "url": "https://developers.openai.com/api/docs/guides/structured-outputs",
    },
    "prompt": {
        "title": "Prompt engineering | OpenAI API",
        "section": "Prompt engineering",
        "url": "https://developers.openai.com/api/docs/guides/prompt-engineering",
    },
    "retrieval": {
        "title": "Retrieval | OpenAI API",
        "section": "Retrieval",
        "url": "https://developers.openai.com/api/docs/guides/retrieval",
    },
    "safety": {
        "title": "Safety best practices | OpenAI API",
        "section": "Constrain user input and limit output tokens",
        "url": "https://developers.openai.com/api/docs/guides/safety-best-practices",
    },
    "evals": {
        "title": "Working with evals | OpenAI API",
        "section": "Working with evals",
        "url": "https://developers.openai.com/api/docs/guides/evals",
    },
    "local-code": {
        "title": "Repository source",
        "section": "Assistant-supplied local request path",
        "url": "repo://src/main/java/assignments/local/LocalCoverStoryApplication.java;frontend/src/local/LocalCoverStoryApp.tsx",
    },
}

# Tuple fields: title, concept, prerequisite, task, learner_path, acceptance,
# negative_case, evidence, source_key, kind.
UNITS = [
    ("Build a grounded policy prompt", "Python dictionaries, JSON values, and a bounded prompt", "None; begin with the worked example.", "Implement the exact instructions/input object for one synthetic hail-coverage question. Do not call a model.", "ai/coverstory_assistant/learner.py::build_policy_prompt", "python3 -m unittest ai.coverstory_assistant.learner_acceptance.test_day_01", "Blank question or excerpt must raise ValueError; extra prompt fields fail.", "Command output plus the exact request object; fixture-only evidence.", "text", "ai"),
    ("Serialize the provider request", "Python types, JSON serialization, and request boundaries", "Day 1 prompt object.", "Implement deterministic JSON-safe request construction with an explicit model placeholder and bounded output-token field.", "ai/coverstory_assistant/learner.py::build_responses_request", "python3 -m unittest ai.coverstory_assistant.learner_acceptance.test_day_02", "Reject missing model, unknown fields, and a prompt that exceeds the fixed fixture limit.", "Exact serialized bytes and negative-case results; no provider evidence.", "python-json", "ai"),
    ("Use the real provider adapter safely", "HTTP requests, environment configuration, and response boundaries", "Days 1–2; instructor-owned adapter and local HTTP test server.", "Use the supplied configurable Responses API adapter. The local HTTP fixture verifies request/response behavior. A live call is optional only after separate authorization and user-configured access.", "ai/coverstory_assistant/provider.py (provided); learner evidence only", "python3 -m unittest ai.coverstory_assistant.tests.test_provider_protocol", "Reject absent configuration and non-success/malformed responses without revealing the key; local server is not live-model evidence.", "Local protocol test is infrastructure only; live-provider validation remains unverified until authorized.", "python-http", "integrated"),
    ("Extract the response text", "Read nested JSON objects and distinguish text from other output items", "Day 3 adapter response fixture.", "Implement extraction of completed message output text from a fixed Responses-shaped payload.", "ai/coverstory_assistant/learner.py::extract_response_text", "python3 -m unittest ai.coverstory_assistant.learner_acceptance.test_day_04", "Refusal, incomplete, empty, or tool-only payloads must not be treated as a final answer.", "Exact extracted text and rejected payload cases; fixture evidence.", "text", "ai"),
    ("Validate a structured answer", "JSON object shape, types, and allowed fields", "Day 4 extracted text.", "Implement parsing and exact schema validation for answer, claims, citations, and abstained.", "ai/coverstory_assistant/learner.py::parse_structured_answer", "python3 -m unittest ai.coverstory_assistant.learner_acceptance.test_day_05", "Reject wrong types, unknown keys, duplicate citations, and invalid JSON.", "Observed parsed object and independent malformed-input results.", "structured", "ai"),
    ("Classify provider failures", "Timeout, status, malformed output, and bounded retry policy", "Day 3 protocol adapter; no live call required.", "Map deterministic local adapter failures into safe error classes; never retry authorization or schema errors.", "ai/coverstory_assistant/learner.py::classify_provider_failure", "python3 -m unittest ai.coverstory_assistant.learner_acceptance.test_day_06", "A 401, malformed JSON, and timeout must not disclose response body or loop indefinitely.", "Error classification and retry-count trace from a local fixture.", "text", "ai"),
    ("Create the first evaluation set", "Fixed inputs, independently written labels, and evaluation cases", "Days 1–6; no model-generated labels.", "Add labeled synthetic questions for supported, unsupported, and ambiguous clauses; labels are fixed independent of any model response.", "ai/coverstory_assistant/learner_artifacts/day-07-labels.jsonl", "python3 -m unittest ai.coverstory_assistant.learner_acceptance.test_day_07_fixture", "A label copied from candidate output is rejected; duplicate case IDs fail.", "Fixture digest, case count, and label review record.", "evals", "ai"),
    ("Version one prompt change", "Prompt versioning and controlled comparisons", "Day 7 fixed labels.", "Implement a versioned prompt builder and compare two fixed prompt versions on the same cases.", "ai/coverstory_assistant/learner.py::prompt_for_version", "python3 -m unittest ai.coverstory_assistant.learner_acceptance.test_day_08", "Unknown versions and a changed fixture digest fail closed.", "Two version IDs, same-case comparison output, and digest.", "prompt", "ai"),
    ("Normalize synthetic documents", "Python strings, document IDs, and source metadata", "Days 1–8 Python/JSON basics.", "Implement conservative whitespace normalization while preserving document ID, section, owner, and source version.", "ai/coverstory_assistant/learner.py::normalize_document", "python3 -m unittest ai.coverstory_assistant.learner_acceptance.test_day_09", "Missing/duplicate IDs and metadata loss are rejected.", "Before/after document bytes and field equality assertions.", "retrieval", "ai"),
    ("Split documents into stable chunks", "Deterministic segmentation and boundary metadata", "Day 9 normalized documents.", "Implement a small deterministic sentence-boundary chunker for the supplied synthetic files; no vector database yet.", "ai/coverstory_assistant/learner.py::chunk_document", "python3 -m unittest ai.coverstory_assistant.learner_acceptance.test_day_10", "Empty chunks, changed text, and duplicate chunk IDs fail.", "Exact chunk text/IDs and source section continuity.", "retrieval", "ai"),
    ("Build the lexical retrieval baseline", "Term matching as a simple baseline before embeddings", "Day 10 chunks; Python list/dict/Counter example supplied.", "Implement exact deterministic lexical scoring against the tiny corpus.", "ai/coverstory_assistant/learner.py::lexical_search", "python3 -m unittest ai.coverstory_assistant.learner_acceptance.test_day_11", "Foreign-tenant and unrelated clauses must not win because they contain common words.", "Fixed query ranking and explicit no-match case.", "retrieval", "ai"),
    ("Compare a tiny local embedding proxy", "Vectors and cosine similarity using a tiny fixed vocabulary", "Day 11 lexical baseline; short worked vector example supplied.", "Implement the bounded bag-of-terms vector comparison and report it beside the lexical baseline; label it a teaching proxy, not a production embedding model.", "ai/coverstory_assistant/learner.py::tiny_vector_score", "python3 -m unittest ai.coverstory_assistant.learner_acceptance.test_day_12", "Zero-vector, dimension mismatch, and tied similarity cases have fixed outcomes.", "Both rankings on the same fixed corpus, with limitations stated.", "retrieval", "ai"),
    ("Measure retrieval errors", "Recall-at-k and fixed relevant-document labels", "Days 7 and 11–12.", "Implement a retrieval metric using independent relevant IDs and compare lexical versus tiny-vector rankings.", "ai/coverstory_assistant/learner.py::recall_at_k", "python3 -m unittest ai.coverstory_assistant.learner_acceptance.test_day_13", "Missing relevant IDs and a self-labeled candidate are rejected.", "Exact numerator/denominator by method and fixed case IDs.", "evals", "ai"),
    ("Construct source citations", "Bind each claim to a retrieved document and section", "Days 9–13.", "Implement citation construction from retrieved chunk IDs; do not invent document names or section labels.", "ai/coverstory_assistant/learner.py::build_citations", "python3 -m unittest ai.coverstory_assistant.learner_acceptance.test_day_14", "Unknown chunk ID and mismatched document version are rejected.", "Exact citation rows and source-version digest.", "retrieval", "ai"),
    ("Check citation support", "Citation integrity and claim-to-excerpt evidence", "Day 14 citations.", "Implement checks that every citation resolves to the exact supplied document version and section.", "ai/coverstory_assistant/learner.py::validate_citations", "python3 -m unittest ai.coverstory_assistant.learner_acceptance.test_day_15", "Valid citation plus unknown/foreign/duplicate extra citation must fail.", "All citations checked; exact support and rejection record.", "retrieval", "ai"),
    ("Abstain when evidence is insufficient", "Decision thresholds from fixed evidence, not model confidence alone", "Days 13–15.", "Implement explicit supported/unsupported/ambiguous outcomes from independent retrieval and claim evidence.", "ai/coverstory_assistant/learner.py::decide_abstention", "python3 -m unittest ai.coverstory_assistant.learner_acceptance.test_day_16", "A confident model output without supporting evidence must abstain.", "Decision table results for all three fixed labels.", "safety", "ai"),
    ("Filter retrieval by tenant", "Authorization before ranking or answer generation", "Day 9 source ownership fields; Day 16 decision table.", "Implement pre-retrieval tenant filtering for synthetic user/document memberships.", "ai/coverstory_assistant/learner.py::filter_authorized_chunks", "python3 -m unittest ai.coverstory_assistant.learner_acceptance.test_day_17", "Foreign-owner clauses must not reach the model prompt or citations.", "Allowed and denied IDs plus prompt-input disclosure assertion.", "safety", "ai"),
    ("Return a complete denial", "Fail-closed result shape and non-disclosure", "Day 17 authorization filter.", "Implement a denial result with no answer, citations, or private metadata.", "ai/coverstory_assistant/learner.py::denied_result", "python3 -m unittest ai.coverstory_assistant.learner_acceptance.test_day_18", "Mutant with allowed=true or private citation must be rejected even if reason text is correct.", "Full denial object and unchanged fixture state.", "safety", "ai"),
    ("Keep retrieved instructions untrusted", "Prompt injection boundary and source-data handling", "Days 8 and 17; policy text remains quoted data.", "Implement prompt assembly that separates fixed instructions from untrusted retrieved excerpts.", "ai/coverstory_assistant/learner.py::assemble_grounded_input", "python3 -m unittest ai.coverstory_assistant.learner_acceptance.test_day_19", "A hostile excerpt cannot replace the fixed instruction or cause a tool/action request.", "Exact instruction/input separation and mutation rejection.", "safety", "ai"),
    ("Evaluate document-quality failures", "Language, stale source, OCR-like noise, and ambiguous text slices", "Days 7 and 9–19.", "Add a small independently labeled quality slice and report its outcomes separately.", "ai/coverstory_assistant/learner_artifacts/day-20-quality-labels.jsonl", "python3 -m unittest ai.coverstory_assistant.learner_acceptance.test_day_20_quality_slice", "Dropping a failing quality case or relabeling from model output must fail.", "Per-slice counts, fixture digest, and unresolved cases.", "evals", "ai"),
    ("Combine the evaluation harness", "Independent expected labels and actual pipeline outputs", "Days 7–20 completed locally.", "Implement a deterministic evaluator over saved output records; it must not call a model or grade itself.", "ai/coverstory_assistant/learner.py::evaluate_cases", "python3 -m unittest ai.coverstory_assistant.learner_acceptance.test_day_21", "Echoing expected labels without reading actual output is caught by counterfactual cases.", "Exact per-case decisions and independent totals.", "evals", "ai"),
    ("Report exact metrics", "Confusion counts, citation accuracy, abstention, and denominators", "Day 21 evaluator.", "Implement fixed metric counts from per-case outcomes, retaining denominators and undefined cases.", "ai/coverstory_assistant/learner.py::summarize_metrics", "python3 -m unittest ai.coverstory_assistant.learner_acceptance.test_day_22", "Wrong denominator, omitted failure, or score above 100 percent is rejected.", "Raw confusion table and independently recomputed ratios.", "evals", "ai"),
    ("Challenge the baseline with counterfactuals", "Negative controls and mutation sensitivity", "Days 21–22.", "Run fixed answer/citation counterfactuals and show which invariant each one violates.", "ai/coverstory_assistant/learner.py::run_counterfactuals", "python3 -m unittest ai.coverstory_assistant.learner_acceptance.test_day_23", "A changed claim with unchanged citation must be detected; a no-op control stays unchanged.", "Before/after outputs and exact rejected-mutant list.", "evals", "ai"),
    ("Record a privacy-safe trace", "Trace IDs, prompt versions, and content redaction", "Days 8 and 21–23.", "Implement a trace record that links fixture/model/prompt versions while excluding secrets and user/document text.", "ai/coverstory_assistant/learner.py::make_safe_trace", "python3 -m unittest ai.coverstory_assistant.learner_acceptance.test_day_24", "Trace containing API key, raw prompt, or policy text is rejected.", "Trace schema, redaction assertions, and fixture-only label.", "safety", "ai"),
    ("Account for usage without inventing measurements", "Provider usage fields, bounded token/cost accounting", "Day 3 adapter and Day 24 trace.", "Implement a report that distinguishes provider-reported usage from unknown usage; never infer actual spend from a mock.", "ai/coverstory_assistant/learner.py::usage_report", "python3 -m unittest ai.coverstory_assistant.learner_acceptance.test_day_25", "Missing usage must remain unknown, not zero; negative or malformed counts fail.", "Usage source and exact fields; live usage is unverified without an authorized call.", "text", "ai"),
    ("Read the supplied request path", "Code comprehension: supplied React-to-Spring-to-AI request flow", "Day 1–25 AI component contracts; no FE/BE editing.", "Read the assistant-owned UI/API code and annotate the request path. This is explicitly comprehension-only.", "None; no learner FE/BE file; record evidence/ai-first/day-26.json", "python3 -m unittest ai.coverstory_assistant.learner_acceptance.test_code_reading.Day26", "An annotation naming a learner-owned React/controller/auth edit is rejected as out of scope.", "Short trace note and successful read-only probe; no implementation credit.", "local-code", "comprehension_only"),
    ("Trace a synthetic end-to-end request", "Code comprehension: input, authorization, retrieval, provider adapter, response", "Day 26 trace note; no FE/BE edits.", "Run the supplied offline fixture flow and trace request/response ownership across layers. This is explicitly comprehension-only.", "None; no learner FE/BE file; record evidence/ai-first/day-27.json", "python3 -m unittest ai.coverstory_assistant.learner_acceptance.test_code_reading.Day27", "A fixture or mock result must not be described as a live provider answer.", "Read-only trace output and honest fixture/live classification.", "local-code", "comprehension_only"),
    ("Diagnose one supplied cross-layer failure", "Code comprehension: locate failure without modifying FE/BE", "Day 27 flow; failure is seeded in an instructor fixture.", "Inspect the supplied failure and identify the failing AI boundary; do not change Java, TypeScript, UI, controller, or auth tests. This is explicitly comprehension-only.", "None; no learner FE/BE file; record evidence/ai-first/day-28.json", "python3 -m unittest ai.coverstory_assistant.learner_acceptance.test_code_reading.Day28", "Changing a frontend/controller/auth file is outside the unit; blaming a healthy layer fails the diagnosis rubric.", "Diagnosis record with observed output and code location.", "local-code", "comprehension_only"),
    ("Run the integrated synthetic demonstration", "AI-only integration and reproducible synthetic evidence", "Days 1–25 AI seams; Days 26–28 reading evidence.", "Implement run_offline_pipeline to compose tenant filtering, retrieval, grounded prompt assembly, response parsing, claim-to-source validation, and abstention with a deterministic model fixture. The supplied loopback browser/API/Python service is labeled synthetic; no live call is made.", "ai/coverstory_assistant/learner.py::run_offline_pipeline and integrated_report", "python3 -m unittest ai.coverstory_assistant.learner_acceptance.test_day_29", "A foreign-tenant claim, unsupported answer, or no-evidence query must return a complete abstention; live-provider claims fail the evidence classification.", "Exact integrated outputs, prompt sent to the fixed model fixture, source/version citations, and denied counterfactuals.", "evals", "ai"),
    ("Review the Day 30 evidence gate", "Evidence categories, limitations, and unresolved live validation", "Day 29 integrated synthetic demonstration.", "Generate a local evidence summary that separates code/tests, understanding, offline fixtures, and independently verified live-provider evidence.", "ai/coverstory_assistant/learner.py::closeout_report", "python3 -m unittest ai.coverstory_assistant.learner_acceptance.test_day_30", "Mocks cannot satisfy the live-provider field; missing evidence stays missing rather than becoming pass/fail completion.", "Exact summary and open gates; no learner completion is prefilled.", "evals", "ai"),
]


def build_manifest() -> dict:
    start = date(2026, 10, 27)
    detailed = []
    for index, item in enumerate(UNITS, 1):
        title, concept, prerequisite, task, path, command, negative, evidence, source_key, kind = item
        detailed.append({
            "day": index,
            "unit_id": f"AI-COVER-{index:03d}",
            "date": (start + timedelta(days=index - 1)).isoformat(),
            "title": title,
            "category": kind,
            "minutes": 120,
            "concept": concept,
            "prerequisite": prerequisite,
            "learner_scope": task,
            "learner_owned_file": path,
            "acceptance_command": command,
            "negative_case": negative,
            "evidence": evidence,
            "evidence_artifact": f"evidence/ai-first/day-{index:02d}.json",
            "source": SOURCES[source_key],
            "supplemental_sources": [SOURCES["python-json"]] if index == 1 else [],
            "setup_command": "python3 -m ai.coverstory_assistant.day01_example" if index == 1 else None,
            "dsa_reference": {
                "mode": "external_daily_problem",
                "dsa_calendar_day": index,
                "repository": "https://github.com/advait-ravi-marathe/dsa-prep",
                "source_commit": "c2caee396002bb48b2a34d407545ecd96ecdb1ab",
                "copied_content": False,
                "solutions_copied": False,
            },
            "status": "not_started",
            "learner_completion": False,
        })
    # Explicitly expose the supplied local vertical-slice launcher for the Day 30 closeout.
    detailed[-1]["demo_commands"] = ["scripts/package-local-demo.sh", "scripts/local-demo.sh"]
    detailed[-1]["demo_url"] = "http://127.0.0.1:<printed-app-port>/#/ai-first/coverstory"
    return {
        "schema_version": 1,
        "title": "AI-first roadmap: detailed first 30 study days",
        "status": "local_review_draft",
        "start_date": start.isoformat(),
        "timezone": "Asia/Kolkata",
        "end_date": (start + timedelta(days=364)).isoformat(),
        "horizon_days": 365,
        "budget": {
            "days_1_to_171": {"engineering_minutes": 120, "dsa_separate": True},
            "days_172_to_365": {"engineering_minutes": 165, "shared_minutes": 45, "dsa_separate": True},
            "engineering_hours": 875.5,
            "shared_hours": 145.5,
            "no_extra_oa_or_certification_hours": True,
        },
        "projects": [
            {"days": [1, 150], "dates": ["2026-10-27", "2027-03-25"], "engineering_hours": 300.0, "reserved_hours": 60.0, "planned_build_hours": 240.0, "id": "coverstory-ai", "title": "CoverStory grounded document/policy assistant", "entry": "Begin with Python/JSON basics and fixed synthetic policy excerpts; no live call needed.", "exit": "A substantial assistant with ingestion, permission-aware retrieval, grounded citations, abstention, safe failures, independently labeled evaluation, and a real configured provider path. Offline demo does not satisfy live-provider evidence."},
            {"days": [151, 255], "dates": ["2027-03-26", "2027-07-08"], "engineering_hours": 273.0, "reserved_hours": 54.6, "planned_build_hours": 218.4, "id": "streamforge-ai", "title": "AI-powered StreamForge search/recommendation/ranking", "entry": "CoverStory evidence is recorded or bounded recovery is completed; use the provided media/platform application code.", "exit": "A distinct recommendation/ranking capability over public/synthetic data, with fixed baselines, cold-start/failure slices, evaluation, and no fabricated user/adoption metrics."},
            {"days": [256, 340], "dates": ["2027-07-09", "2027-10-01"], "engineering_hours": 233.75, "reserved_hours": 46.75, "planned_build_hours": 187.0, "id": "developer-workflow-assistant", "title": "Developer workflow assistant", "entry": "The two project artifacts and shared evaluation approach are reviewed; use read-only tools first.", "exit": "A distinct workflow assistant with read-only tools, approval-gated writes only where explicitly safe, durable recovery, injection/authorization tests, and measured task outcomes."},
            {"days": [341, 365], "dates": ["2027-10-02", "2027-10-26"], "engineering_hours": 68.75, "reserved_hours": 13.75, "planned_build_hours": 55.0, "id": "shared-integration-closeout", "title": "Shared evaluation and portfolio integration", "entry": "Prior project evidence is recorded or open gates have recovery routes.", "exit": "One shared evaluation/observability capability applied across the three projects; reproducible portfolio artifacts and honest limitations. No fourth flagship, live-provider claim, customer adoption, certification pass, or professional ownership claim."},
        ],
        "shared_capabilities": [
            "Evaluation and observability begin in the CoverStory first month and are reused across StreamForge and Developer Workflow Assistant; they are not a separate flagship.",
            "Python/math foundations, held-out labels, uncertainty, prompt/data governance, and model/retrieval comparisons are integrated into project study time.",
            "Reserve remediation/integration/reassessment time inside each phase's existing budget; do not add hours or interpret every day as feature work.",
        ],
        "assistant_selected_architecture": {
            "ui": "Existing React application, with a supplied AI-first route.",
            "session_and_api": "Existing Spring Boot local application owns synthetic session/CSRF and derives tenant from authenticated principal; supplied controller forwards only question and server-derived tenant.",
            "ai_application": "Python package owns prompt/retrieval/evaluation seams; deterministic loopback fixture service provides an offline vertical slice.",
            "provider": "A supplied, configurable OpenAI Responses API adapter is used only when separately authorized and explicitly invoked; no credentials are read from disk or created.",
            "rationale": "Reuse the existing application/session boundary, keep AI experiments in Python, preserve a clear provider boundary, and make the default demo synthetic and local.",
            "learner_design_work": "None required; the assistant selects and explains architecture. Learner owns bounded AI implementation, tests, and code comprehension only.",
        },
        "capacity_policy": {
            "reserve_fraction": 0.20,
            "reserve_total_hours": 175.1,
            "planned_build_total_hours": 700.4,
            "no_hours_added": True,
            "note": "The reserve is a planning allocation inside the engineering hours above for remediation, integration, observation, and reassessment; it is not extra study time or a commitment that every phase spends exactly this amount."
        },
        "nine_month_checkpoint": {
            "date": "2027-07-27",
            "study_day": 274,
            "status": "conditional_evidence_review_not_completion",
            "expected_phase_position": "CoverStory and StreamForge exit gates may be reviewed; Developer Workflow Assistant is early/in progress.",
            "role_or_job_guarantee": False,
        },
        "certification_targets": [
            {"target": "NVIDIA Certified Associate: Generative AI LLMs (NCA-GENL)", "mode": "study target; recheck exam availability and blueprint; exam booking/purchase not authorized", "official_reference": "https://www.nvidia.com/en-us/learn/certification/generative-ai-llm-associate/"},
            {"target": "AWS Certified Solutions Architect - Associate (SAA-C03)", "mode": "first AWS associate target using the already-owned Stephane Maarek material; recheck before study; exam booking/purchase not authorized", "official_reference": "https://docs.aws.amazon.com/aws-certification/latest/solutions-architect-associate-03/solutions-architect-associate-03.html"},
            {"target": "AWS Certified Generative AI Developer - Professional (AIP-C01)", "mode": "later readiness-based target after substantial project evidence; not automatic at Day 365; recheck before study; no booking", "official_reference": "https://docs.aws.amazon.com/aws-certification/latest/ai-professional-01/ai-professional-01.html"},
            {"target": "NVIDIA Building RAG Agents with LLMs course certificate", "mode": "optional only if access is already available; verify current course/assessment access; no enrollment/purchase authorized", "official_reference": "https://learn.nvidia.com/"},
        ],
        "preservation": {
            "legacy_export": "docs/reboot/calendar/story-export-1-305.json",
            "legacy_issue_ids_and_definitions_mutated": False,
            "board_writes": False,
            "dsa_repository_changed": False,
            "active_issue_migration": "separate explicit export; requires parent review before any board sync",
            "prior_learner_completion_carried": False,
        },
        "detailed_days": detailed,
    }


def render_contract(day: dict) -> str:
    source = day["source"]
    supplemental = "".join(
        f"\nSupplemental: [{item['title']}]({item['url']}) — **{item['section']}**."
        for item in day.get("supplemental_sources", [])
    )
    setup = ""
    if day.get("setup_command"):
        setup = f"\nInstructor-owned setup command:\n\n```sh\n{day['setup_command']}\n```\n"
    demo = ""
    if day.get("demo_commands"):
        demo = "\n## Supplied offline vertical slice\n\n" + "\n".join(
            f"```sh\n{command}\n```" for command in day["demo_commands"]
        ) + f"\n\nOpen the printed app origin and append `/#/ai-first/coverstory` (the fragment stays in the browser). The launcher starts disposable local PostgreSQL and a loopback-only synthetic fixture service; it sends no provider request and records no learner evidence.\n"
    return f"""# Day {day['day']}: {day['title']}

**Date:** {day['date']} (Asia/Kolkata)

**Time:** 120 engineering minutes

**Status:** not started; no completion is recorded

**Category:** {day['category']}

## Concept and source

{day['concept']}

Read the exact source: [{source['title']}]({source['url']}) — **{source['section']}**.
{supplemental}

## Prerequisite

{day['prerequisite']}

## Task

{day['learner_scope']}
{setup}

Learner-owned scope: `{day['learner_owned_file']}`. For comprehension-only units,
there is no learner FE/BE code change.

## Acceptance

Run from the repository root:

```sh
{day['acceptance_command']}
```

Negative case: {day['negative_case']}

## Evidence

{day['evidence']}

Keep implementation evidence, code understanding, offline/synthetic output,
and independently verified live-provider or external-use evidence distinct.
Mocks and local HTTP fixtures do not count as live model experience.
{demo}
""".rstrip() + "\n"


TEST_WRAPPERS = (2, 4, 5, 6, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 21, 22, 23, 24, 25, 29, 30)


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--check", action="store_true")
    args = parser.parse_args()
    manifest = build_manifest()
    outputs = {OUT / "manifest.json": json.dumps(manifest, indent=2, ensure_ascii=False) + "\n"}
    for day in manifest["detailed_days"]:
        outputs[OUT / "contracts" / f"day-{day['day']:02d}.md"] = render_contract(day)
    for day_number in TEST_WRAPPERS:
        class_name = f"Day{day_number:02d}AcceptanceTests"
        outputs[ROOT / "ai" / "coverstory_assistant" / "learner_acceptance" / f"test_day_{day_number:02d}.py"] = (
            f"from ai.coverstory_assistant.learner_acceptance.test_acceptance_cases import {class_name}\n"
        )
    bad = []
    for path, content in outputs.items():
        if args.check:
            if not path.exists() or path.read_text() != content:
                bad.append(str(path.relative_to(ROOT)))
        else:
            path.parent.mkdir(parents=True, exist_ok=True)
            path.write_text(content)
    if bad:
        print("stale generated files:")
        print("\n".join(bad))
        return 1
    print(f"checked/wrote {len(outputs)} generated files for {len(manifest['detailed_days'])} units")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
