#!/usr/bin/env python3
"""Create traceable harness coverage records from the saved assignment contracts.

This generator records authored instructor seams without deriving business
implementations; each entry remains limited to its explicitly documented scope.
"""
from __future__ import annotations

import hashlib
import json
import re
from pathlib import Path

from materialize_coverstory_curriculum import active_task

ROOT = Path(__file__).resolve().parents[1]
SOURCE = ROOT / "docs/source-inputs/2026-10-14/curriculum/coverstory-final-saved-contracts-594-days.json"
INDEX = ROOT / "docs/assignments/coverstory/curriculum-index.json"
OUT = ROOT / "docs/assignments/coverstory/harness-matrix.json"


def section(text: str, title: str) -> str:
    match = re.search(r"^#{1,6} " + re.escape(title) + r"\s*$(.*?)(?=^#{1,6} |\Z)", text, re.M | re.S)
    return match.group(1).strip() if match else ""


def main() -> dict:
    cards = json.loads(SOURCE.read_text())['cards']
    index = json.loads(INDEX.read_text())
    cards_by_id = {card['id']: card for card in cards}
    executable = {
        'CAP-40': 'java_selection_identity',
        'CAP-42': 'postgres_catalog_aggregation',
        'CAP-44': 'python_public_ranking',
        'CAP-46': 'java_bounded_review_queue',
        'CAP-48': 'postgres_utc_daily_aggregation',
        'CAP-50': 'java_import_decision_counts',
        'CAP-165': 'java_source_version_identity',
        'CAP-53': 'python_allowlisted_json_loader',
        'CAP-59': 'python_versioned_citation_integrity',
        'CAP-61': 'python_grounded_abstention_gate',
        'CAP-56': 'python_macro_retrieval_evaluation',
        'CAP-62': 'python_bounded_async_cancellation',
        'CAP-54': 'java_owned_journey_resume',
        'CAP-55': 'java_offline_partition_trace',
        'CAP-57': 'postgres_atomic_catalog_publication',
        'CAP-63': 'python_evidence_provenance_index',
        'CAP-64': 'java_offline_producer_ack',
        'CAP-65': 'python_principal_scoped_retrieval',
        'CAP-66': 'java_offline_consumer_group_trace',
        'CAP-67': 'python_retrieval_rank_metrics',
        'CAP-69': 'python_bounded_burst_simulation',
        'CAP-71': 'python_deterministic_public_text_chunking',
        'CAP-72': 'python_payments_batch_audit_transaction',
        'CAP-74': 'java_owned_payment_attempt_projection',
        'CAP-75': 'python_provider_failure_diagnosis',
        'CAP-76': 'java_bounded_status_retry_policy',
        'CAP-77': 'python_source_bound_claim_consistency',
        'CAP-79': 'python_fixed_clock_source_freshness',
        'CAP-82': 'python_safe_ranker_comparison',
        'CAP-80': 'postgres_ordered_payment_confirmation_feed',
        'CAP-81': 'python_authoritative_audit_completeness',
        'CAP-84': 'python_provider_callback_validation',
        'CAP-78': 'postgres_atomic_payment_intent',
        'CAP-83': 'spring_safe_checkout_error_mapping',
        'CAP-85': 'postgres_payment_batch_effect_reconciliation',
        'CAP-86': 'python_onboarding_answer_gate',
        'CAP-89': 'python_local_evaluation_safety_gate',
        'CAP-92': 'java_synthetic_retention_dry_run',
        'CAP-88': 'java_role_resource_policy',
        'CAP-91': 'java_optional_source_grounded_assistance',
        'CAP-93': 'java_simulated_payment_outbox_outage',
        'CAP-90': 'java_protected_payload_crypto_boundary',
        'CAP-95': 'java_bounded_optional_assistance',
        'CAP-97': 'java_grounded_vs_guessing_evaluation',
        'CAP-94': 'java_reproducible_artifact_evidence_gate',
        'CAP-96': 'local_learner_evidence_gate',
        'CAP-99': 'java_mock_onboarding_readiness_manifest',
        'CAP-103': 'java_onboarding_request_latency_summary',
        'CAP-98': 'postgres_compatible_schema_expansion',
        'CAP-101': 'java_authorized_assistance_context_budget',
        'CAP-102': 'postgres_atomic_reconciliation_cursor',
        'CAP-104': 'java_late_payment_summary_recovery',
        'CAP-105': 'java_evidence_checkpoint_review',
        'CAP-106': 'java_owned_snapshot_read_policy',
        'CAP-107': 'java_review_manifest_validation',
        'CAP-108': 'java_live_backfill_lane_planning',
        'CAP-166': 'typescript_additive_catalog_envelope_decoder',
        'CAP-167': 'java_bounded_publication_trace_diagnostics',
        'CAP-52': 'spring_catalog_import_adapter',
        'CAP-58': 'java_allowlisted_analytics_batch_validation',
        'CAP-60': 'postgres_transaction_race_invariants',
        'CAP-169': 'java_bounded_retry_after_parser',
        'CAP-68': 'postgres_shortlist_commit_response_loss',
        'CAP-70': 'python_immutable_protected_envelope_staging',
    }
    authored_ids = {'CAP-40', 'CAP-42', 'CAP-44', 'CAP-46', 'CAP-48', 'CAP-50', 'CAP-53', 'CAP-54', 'CAP-55', 'CAP-56', 'CAP-57', 'CAP-59', 'CAP-61', 'CAP-62', 'CAP-165', 'CAP-63', 'CAP-64', 'CAP-65', 'CAP-66', 'CAP-67', 'CAP-69', 'CAP-71', 'CAP-72', 'CAP-74', 'CAP-75', 'CAP-76', 'CAP-77', 'CAP-79', 'CAP-82', 'CAP-80', 'CAP-81', 'CAP-84', 'CAP-78', 'CAP-83', 'CAP-85', 'CAP-86', 'CAP-89', 'CAP-92', 'CAP-88', 'CAP-91', 'CAP-93', 'CAP-90', 'CAP-95', 'CAP-97', 'CAP-94', 'CAP-96', 'CAP-99', 'CAP-103', 'CAP-98', 'CAP-101', 'CAP-102', 'CAP-104', 'CAP-105', 'CAP-106', 'CAP-107', 'CAP-108', 'CAP-166', 'CAP-167'}
    authored_ids.update({'CAP-52', 'CAP-58', 'CAP-60', 'CAP-169', 'CAP-68', 'CAP-70'})
    fixture_validated_ids = set(authored_ids)
    derived_paths = {
        'CAP-40': 'src/main/java/coverstory/modules/shortlist/api/CAP40Seam.java',
        'CAP-42': 'src/main/resources/coverstory/catalog/cap_42.sql',
        'CAP-44': 'ai/coverstory/cap_44.py',
        'CAP-46': 'src/main/java/coverstory/modules/catalog/api/CAP46Seam.java',
        'CAP-48': 'src/main/resources/coverstory/catalog/cap_48.sql',
        'CAP-50': 'src/main/java/coverstory/modules/catalog/api/CAP50Seam.java',
        'CAP-165': 'src/main/java/coverstory/modules/catalog/api/CAP165Seam.java',
        'CAP-53': 'ai/coverstory/cap_53.py',
        'CAP-59': 'ai/coverstory/cap_59.py',
        'CAP-61': 'ai/coverstory/cap_61.py',
        'CAP-56': 'ai/coverstory/cap_56.py',
        'CAP-62': 'ai/coverstory/cap_62.py',
        'CAP-54': 'src/main/java/coverstory/modules/application/api/CAP54Seam.java',
        'CAP-55': 'src/main/java/coverstory/modules/catalog/api/CAP55Seam.java',
        'CAP-57': 'src/main/java/coverstory/modules/catalog/api/CAP57Seam.java',
        'CAP-63': 'ai/coverstory/cap_63.py',
        'CAP-64': 'src/main/java/coverstory/modules/catalog/api/CAP64Seam.java',
        'CAP-65': 'ai/coverstory/cap_65.py',
        'CAP-66': 'src/main/java/coverstory/modules/catalog/api/CAP66Seam.java',
        'CAP-67': 'ai/coverstory/cap_67.py',
        'CAP-69': 'ai/coverstory/cap_69.py',
        'CAP-71': 'ai/coverstory/cap_71.py',
        'CAP-72': 'ai/coverstory/cap_72.py',
        'CAP-74': 'src/main/java/coverstory/modules/payments/api/CAP74Seam.java',
        'CAP-75': 'ai/coverstory/cap_75.py',
        'CAP-76': 'src/main/java/coverstory/modules/payments/api/CAP76Seam.java',
        'CAP-77': 'ai/coverstory/cap_77.py',
        'CAP-79': 'ai/coverstory/cap_79.py',
        'CAP-82': 'ai/coverstory/cap_82.py',
        'CAP-80': 'src/main/java/coverstory/modules/payments/api/CAP80Seam.java',
        'CAP-81': 'ai/coverstory/cap_81.py',
        'CAP-84': 'ai/coverstory/cap_84.py',
        'CAP-78': 'src/main/java/coverstory/modules/payments/api/CAP78Seam.java',
        'CAP-83': 'src/main/java/coverstory/application/ReturnSafeCheckoutAndDraftErrors.java',
        'CAP-85': 'src/main/java/coverstory/modules/payments/api/CAP85Seam.java',
        'CAP-86': 'ai/coverstory/cap_86.py',
        'CAP-89': 'ai/coverstory/cap_89.py',
        'CAP-92': 'src/main/java/coverstory/modules/application/api/CAP92Seam.java',
        'CAP-88': 'src/main/java/coverstory/security/CAP88AccessPolicySeam.java',
        'CAP-91': 'src/main/java/coverstory/application/AdaptOnboardingAssistance.java',
        'CAP-93': 'src/main/java/coverstory/modules/payments/api/CAP93Seam.java',
        'CAP-90': 'src/main/java/coverstory/platform/crypto/CAP90ProtectedPayloadSeam.java',
        'CAP-95': 'src/main/java/coverstory/application/BoundOptionalAssistanceRequests.java',
        'CAP-97': 'src/main/java/coverstory/application/CompareGroundedAssistanceWithGuessing.java',
        'CAP-94': 'src/main/java/coverstory/application/CAP94Seam.java',
        'CAP-96': 'scripts/coverstory_local_gate.py',
        'CAP-99': 'src/main/java/coverstory/application/CAP99Seam.java',
        'CAP-103': 'src/main/java/capstone/coverstory/application/MeasureHonestOnboardingRequestLatency.java',
        'CAP-98': 'src/main/java/coverstory/application/RehearseCompatibleApplicationSchemaExpansion.java',
        'CAP-101': 'src/main/java/capstone/coverstory/application/BoundAssistanceContext.java',
        'CAP-102': 'src/main/java/coverstory/modules/payments/api/CAP102Seam.java',
        'CAP-104': 'src/main/java/coverstory/modules/payments/api/CAP104Seam.java',
        'CAP-105': 'src/main/java/coverstory/modules/application/api/CAP105Seam.java',
        'CAP-106': 'src/main/java/coverstory/modules/application/api/CAP106Seam.java',
        'CAP-107': 'src/main/java/coverstory/modules/application/api/CAP107Seam.java',
        'CAP-108': 'src/main/java/coverstory/modules/payments/api/CAP108Seam.java',
        'CAP-166': 'web/src/coverstory/catalog/decodeCatalogEnvelope.ts',
        'CAP-167': 'src/main/java/coverstory/modules/catalog/api/CAP167Seam.java',
        'CAP-52': 'src/main/java/coverstory/modules/catalog/api/CAP52Seam.java',
        'CAP-58': 'src/main/java/coverstory/modules/analytics/api/CAP58Seam.java',
        'CAP-60': 'src/test/resources/coverstory/CAP-60/schema.sql',
        'CAP-169': 'src/main/java/capstone/coverstory/payments/api/CAP169Seam.java',
        'CAP-68': 'src/main/java/capstone/coverstory/shortlist/api/CAP68Seam.java',
        'CAP-70': 'ai/coverstory/cap_70.py',
    }
    acceptance_suites = {
        'CAP-40': 'src/test/java/coverstory/modules/shortlist/CAP40AcceptanceTest.java',
        'CAP-54': 'src/test/java/coverstory/modules/application/CAP54AcceptanceTest.java',
        'CAP-55': 'src/test/java/coverstory/modules/catalog/CAP55AcceptanceTest.java',
        'CAP-57': 'src/test/java/coverstory/modules/catalog/CAP57AcceptanceTest.java',
        'CAP-63': 'ai/tests/test_coverstory_cap63_acceptance.py',
        'CAP-64': 'src/test/java/coverstory/modules/catalog/CAP64AcceptanceTest.java',
        'CAP-65': 'ai/coverstory/tests/test_cap_65.py',
        'CAP-66': 'src/test/java/coverstory/modules/catalog/CAP66AcceptanceTest.java',
        'CAP-67': 'ai/coverstory/tests/test_cap_67.py',
        'CAP-69': 'ai/coverstory/tests/test_cap_69.py',
        'CAP-71': 'ai/coverstory/tests/test_cap_71.py',
        'CAP-72': 'ai/coverstory/tests/test_cap_72.py',
        'CAP-74': 'src/test/java/coverstory/modules/payments/CAP74AcceptanceTest.java',
        'CAP-75': 'ai/coverstory/tests/test_cap_75.py',
        'CAP-76': 'src/test/java/coverstory/modules/payments/CAP76AcceptanceTest.java',
        'CAP-77': 'ai/coverstory/tests/test_cap_77.py',
        'CAP-79': 'ai/coverstory/tests/test_cap_79.py',
        'CAP-82': 'ai/coverstory/tests/test_cap_82.py',
        'CAP-80': 'src/test/java/coverstory/modules/payments/CAP80AcceptanceTest.java',
        'CAP-81': 'ai/tests/test_coverstory_cap81_acceptance.py',
        'CAP-84': 'ai/tests/test_coverstory_cap84_acceptance.py',
        'CAP-78': 'src/test/java/coverstory/modules/payments/CAP78AcceptanceTest.java',
        'CAP-83': 'src/test/java/coverstory/application/CAP83AcceptanceTest.java',
        'CAP-85': 'src/test/java/coverstory/modules/payments/CAP85AcceptanceTest.java',
        'CAP-86': 'ai/tests/test_coverstory_cap86_acceptance.py',
        'CAP-89': 'ai/tests/test_coverstory_cap89_acceptance.py',
        'CAP-92': 'src/test/java/coverstory/modules/application/CAP92AcceptanceTest.java',
        'CAP-88': 'src/test/java/coverstory/security/CAP88AcceptanceTest.java',
        'CAP-91': 'src/test/java/coverstory/application/CAP91AcceptanceTest.java',
        'CAP-93': 'src/test/java/coverstory/modules/payments/CAP93AcceptanceTest.java',
        'CAP-90': 'src/test/java/coverstory/platform/crypto/CAP90AcceptanceTest.java',
        'CAP-95': 'src/test/java/coverstory/application/CAP95AcceptanceTest.java',
        'CAP-97': 'src/test/java/coverstory/application/CAP97AcceptanceTest.java',
        'CAP-42': 'src/test/java/coverstory/modules/catalog/CAP42SqlAcceptanceTest.java',
        'CAP-44': 'ai/coverstory/tests/test_cap_44.py',
        'CAP-46': 'src/test/java/coverstory/modules/catalog/CAP46AcceptanceTest.java',
        'CAP-48': 'src/test/java/coverstory/modules/catalog/CAP48SqlAcceptanceTest.java',
        'CAP-50': 'src/test/java/coverstory/modules/catalog/CAP50AcceptanceTest.java',
        'CAP-53': 'ai/coverstory/tests/test_cap_53.py',
        'CAP-56': 'ai/coverstory/tests/test_cap_56.py',
        'CAP-59': 'ai/coverstory/tests/test_cap_59.py',
        'CAP-61': 'ai/coverstory/tests/test_cap_61.py',
        'CAP-62': 'ai/coverstory/tests/test_cap_62.py',
        'CAP-165': 'src/test/java/coverstory/modules/catalog/CAP165AcceptanceTest.java',
        'CAP-104': 'src/test/java/coverstory/modules/payments/CAP104AcceptanceTest.java',
        'CAP-105': 'src/test/java/coverstory/modules/application/CAP105AcceptanceTest.java',
        'CAP-106': 'src/test/java/coverstory/modules/application/CAP106AcceptanceTest.java',
        'CAP-107': 'src/test/java/coverstory/modules/application/CAP107AcceptanceTest.java',
        'CAP-108': 'src/test/java/coverstory/modules/payments/CAP108AcceptanceTest.java',
        'CAP-166': 'web/scripts/catalog-envelope-acceptance.mjs',
        'CAP-167': 'src/test/java/coverstory/modules/catalog/CAP167AcceptanceTest.java',
        'CAP-52': 'src/test/java/coverstory/modules/catalog/CAP52AcceptanceTest.java',
        'CAP-58': 'src/test/java/coverstory/modules/analytics/CAP58AcceptanceTest.java',
        'CAP-60': 'src/test/java/coverstory/application/CAP60AcceptanceTest.java',
        'CAP-169': 'src/test/java/capstone/coverstory/payments/CAP169Test.java',
        'CAP-68': 'src/test/java/capstone/coverstory/shortlist/CAP68Test.java',
        'CAP-70': 'ai/coverstory/tests/test_cap_70.py',
    }
    current_candidate_runs = set(authored_ids) - {'CAP-96'}
    coverage_notes = {
        'CAP-88': 'Pure policy harness only; Spring authentication and HTTP role-client integration are blocked because no auth adapter or protected module route exists.',
        'CAP-89': 'Local evaluator gate only; no CI workflow integration is implemented.',
        'CAP-90': 'Field-cipher boundary only; protected database adapter, response/log integration and cross-process retained-key restore remain blocked.',
        'CAP-91': 'Typed fake-port scaffold only; no provider or Application pipeline is implemented.',
        'CAP-92': 'Synthetic eligibility dry-run only; no deletion port or legal retention policy is implemented.',
        'CAP-93': 'Local simulated outage model only; integrated worker probe has no observed output and is blocked.',
        'CAP-94': 'Gate oracle only; no actual build artifact pair was supplied, so reproducibility comparison is NOT_RUN.',
        'CAP-95': 'Fake-port timeout/limit scaffold only; no external assistance provider is called.',
        'CAP-96': 'Instructor-owned local evidence evaluator; CI, package and publication integration are absent.',
        'CAP-97': 'Four-case synthetic evaluation only; no model/provider run or general superiority inference.',
        'CAP-101': 'Bounded context policy only; no provider call or Application route is implemented.',
        'CAP-98': 'Disposable PostgreSQL migration and logical restore drill only; no production migration or operational backup service was exercised.',
        'CAP-99': 'Readiness evaluator only; all six actual product gate receipts remain unverified.',
        'CAP-100': 'Synthetic evidence-row classifier only; no live evidence or customer data is used.',
        'CAP-102': 'Embedded PostgreSQL transaction/cursor harness only; no production reconciliation worker is wired.',
        'CAP-103': 'Synthetic latency-math fixture only; no service latency, capacity or availability claim.',
        'CAP-104': 'Deterministic payment effect guard only; no charge, refund or dispatch integration is executed.',
        'CAP-105': 'Evidence checkpoint oracle only; linked CAP-95/102/104 receipts are absent, so no release claim is made.',
        'CAP-106': 'Local owner/version/TLS policy only; authentication filter, HTTP route, storage and transport are not wired.',
        'CAP-107': 'Manifest and disclosure decision oracle only; persistence, quoting and submission effects are not wired.',
        'CAP-108': 'In-memory lane planning only; no queue, worker, published diagnostics or payment integration is exercised.',
        'CAP-52': 'MockMvc transport boundary and fake decision port only; trusted-principal authentication adapter and durable idempotency are not wired.',
        'CAP-166': 'Synthetic fixture and compiled learner seam only; decoder remains TODO and React/API/PostgreSQL customer slice is deferred.',
        'CAP-167': 'Bounded offline trace oracle only; learner checker remains TODO and no broker or publication worker is used.',
        'CAP-58': 'Independent local schema/privacy oracle only; learner validator remains TODO, and no HTTP ingestion, durable idempotency, storage, journey authorization or browser retry queue is wired.',
        'CAP-60': 'Disposable PostgreSQL harness proves uniqueness, rollback and synthetic two-connection races; it is not connected to an application command or service, and learner race assertions remain TODO.',
        'CAP-169': 'Fixed-clock Retry-After parser oracle covers seconds, HTTP dates, past dates and invalid values; parsing does not authorize a retry, and the learner seam remains TODO.',
        'CAP-68': 'Disposable PostgreSQL oracle demonstrates shortlist/audit/journey commit surviving synthetic response loss, same-key and owner-scoped recovery, and a distinct optional-publication gap; it is not an application command or publisher, and the learner seam remains TODO.',
        'CAP-70': 'Instructor fixture oracle checks immutable ciphertext-byte/provenance staging order and safe rejection; existing Java AES-GCM adapter verifies fixed vectors, AAD/tag failure and random nonce generation. No wrapped-DEK/KMS or product staging adapter is implemented, and the learner batch method remains TODO.',
    }
    learner_result_overrides = {'CAP-90': 'PASS_TODO_GUARD_ONLY', 'CAP-101': 'PASS_TODO_GUARD_ONLY'}
    rows = []
    for day in index['days']:
        card = cards_by_id[day['primary_id']]
        scaffold = section(card['description'], 'Instructor scaffold and learner TODO')
        task = active_task(card['description'])
        cases_block = section(card['description'], 'Acceptance cases · expected, NOT_RUN')
        case_ids = re.findall(r"^[-*]\s*([A-Z0-9-]+-CS-\d+):", cases_block, re.M)
        # Some older contracts use a different acceptance heading suffix.
        if not case_ids:
            heading = re.search(r"^#{1,6} Acceptance cases[^\n]*\n(.*?)(?=^#{1,6} |\Z)", card['description'], re.M | re.S)
            cases_block = heading.group(1).strip() if heading else cases_block
            case_ids = re.findall(r"^[-*]\s*([A-Z0-9-]+-CS-\d+):", cases_block, re.M)
        paths = [line.strip()[2:] for line in scaffold.splitlines() if line.strip().startswith('- ') and '/' in line]
        planned_source = next((p for p in paths if p.startswith(("src/main/", "ai/coverstory/", "frontend/src/", "web/src/"))), None)
        authored = card['id'] in authored_ids
        status = 'LEARNER_CONTRACT_AND_FIXTURE_VALIDATED' if card['id'] == 'CAP-40' else (
            'INSTRUCTOR_FIXTURE_HARNESS_AUTHORED' if authored else 'MAPPED_ONLY'
        )
        family = executable.get(card['id'])
        if card['phase'] == 2:
            if 'no feature TODO sequence or implementation outline' in card['description']:
                scaffold_strategy = 'ENVIRONMENT_AND_PUBLIC_CONTRACTS_ONLY'
            elif 'fresh bounded defect' in card['description'] or 'root cause, faulty implementation location and solution outside' in card['description']:
                scaffold_strategy = 'HIDDEN_FRESH_FAULT_SYMPTOMS_ONLY'
            else:
                scaffold_strategy = 'PROGRESSIVELY_FADED_FIXTURE_AND_EVALUATION_CONTRACT'
        else:
            scaffold_strategy = 'INSTRUCTOR_FIXTURE_AND_TEST_HARNESS_WITH_LEARNER_SEAM_TODO'
        rows.append({
            'day_number': day['day_number'], 'date': day['date'], 'phase': day['phase'],
            'card_id': card['id'], 'source_description_sha256': card['description_sha256'],
            'source_imported': True, 'guide_mapped': True,
            'instructor_scaffold': authored,
            'fixture_harness_validated': card['id'] in fixture_validated_ids,
            'historical_learner_contract_evidence': card['id'] == 'CAP-40',
            'assignability': (
                'CONDITIONAL_SCAFFOLD_NOT_REHEARSED' if authored
                else 'CONDITIONAL_EXPERIMENT_PREFLIGHT' if card['phase'] == 2
                else 'NOT_SCAFFOLDED'
            ),
            'learner_result': learner_result_overrides.get(card['id'],
                'EXPECTED_FAILURE_AT_UNIMPLEMENTED_LEARNER_TODO' if card['id'] in current_candidate_runs
                else 'NOT_RUN_ON_CURRENT_CANDIDATE'),
            'learner_readiness': 'READY_TO_ATTEMPT' if card['id'] == 'CAP-40' else 'ENVIRONMENT_PREFLIGHT_NOT_RUN',
            'timebox_status': 'TIMEBOX_NOT_VALIDATED' if card['id'] == 'CAP-40' else 'NOT_REHEARSED',
            'readiness_evidence': 'docs/evidence/coverstory/CAP-40/environment-preflight.json' if card['id'] == 'CAP-40' else None,
            'learner_acceptance_suite': acceptance_suites.get(card['id']),
            'scaffold_status': status, 'harness_family': family,
            'source_planned_primary_path': planned_source,
            'canonical_derived_primary_path': derived_paths.get(card['id']),
            'source_path_reconciled': bool(derived_paths.get(card['id']) and planned_source != derived_paths.get(card['id'])),
            'scaffold_strategy': ('LOCAL_EVIDENCE_GATE_WITH_INSTRUCTOR_ORACLE' if card['id'] == 'CAP-96' else scaffold_strategy),
            'coverage_note': coverage_notes.get(card['id']),
            'acceptance_case_ids': case_ids,
            'task_summary': re.sub(r'\s+', ' ', task)[:700],
        })
    counts = {
        'source_imported': sum(row['source_imported'] for row in rows),
        'guide_mapped': sum(row['guide_mapped'] for row in rows),
        'instructor_scaffolds_authored': sum(row['instructor_scaffold'] for row in rows),
        'historical_learner_contract_evidence': sum(row['historical_learner_contract_evidence'] for row in rows),
        'fixture_harnesses_validated': sum(row['fixture_harness_validated'] for row in rows),
        'conditionally_scaffold_supported': sum(row['assignability'] == 'CONDITIONAL_SCAFFOLD_NOT_REHEARSED' for row in rows),
        'assignable_now': sum(row['assignability'] == 'ASSIGNABLE' for row in rows),
        'learner_acceptance_runs_on_current_candidate': sum(row['learner_result'] != 'NOT_RUN_ON_CURRENT_CANDIDATE' for row in rows),
        'phase2_independent_experiment_contracts_mapped': sum(row['scaffold_strategy'] == 'PROGRESSIVELY_FADED_FIXTURE_AND_EVALUATION_CONTRACT' for row in rows),
        'phase2_hidden_fault_preflights_required': sum(row['scaffold_strategy'] == 'HIDDEN_FRESH_FAULT_SYMPTOMS_ONLY' for row in rows),
        'scaffold_families_started': len(executable),
    }
    result = {'source_curriculum_sha256': hashlib.sha256(SOURCE.read_bytes()).hexdigest(), 'counts': counts, 'assignments': rows}
    OUT.write_text(json.dumps(result, indent=2, ensure_ascii=False) + '\n')
    print(json.dumps(counts, indent=2))
    return result


if __name__ == '__main__':
    main()
