#!/usr/bin/env python3
"""Create traceable assignment harness records and case fixtures from saved contracts.

This generator derives structural cases, never implementations. It intentionally
limits executable coverage to the catalog/identity/import seam family first.
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
        'CAP-52': 'spring_catalog_import_adapter',
        'CAP-53': 'python_allowlisted_json_loader',
        'CAP-59': 'python_versioned_citation_integrity',
        'CAP-61': 'python_grounded_abstention_gate',
        'CAP-56': 'python_macro_retrieval_evaluation',
        'CAP-62': 'python_bounded_async_cancellation',
    }
    authored_ids = {'CAP-40', 'CAP-42', 'CAP-44', 'CAP-46', 'CAP-48', 'CAP-50', 'CAP-53', 'CAP-56', 'CAP-59', 'CAP-61', 'CAP-62', 'CAP-165'}
    fixture_validated_ids = {'CAP-40', 'CAP-42', 'CAP-44', 'CAP-46', 'CAP-48', 'CAP-50', 'CAP-53', 'CAP-56', 'CAP-59', 'CAP-61', 'CAP-62', 'CAP-165'}
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
    }
    acceptance_suites = {
        'CAP-40': 'src/test/java/coverstory/modules/shortlist/CAP40AcceptanceTest.java',
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
    }
    current_candidate_runs = {'CAP-42', 'CAP-44', 'CAP-46', 'CAP-48', 'CAP-50', 'CAP-53', 'CAP-56', 'CAP-59', 'CAP-61', 'CAP-62', 'CAP-165'}
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
                'BLOCKED_TIMEBOX_REHEARSAL' if card['id'] == 'CAP-40'
                else 'CONDITIONAL_SCAFFOLD_NOT_REHEARSED' if authored
                else 'CONDITIONAL_EXPERIMENT_PREFLIGHT' if card['phase'] == 2
                else 'NOT_SCAFFOLDED'
            ),
            'learner_result': (
                'EXPECTED_FAILURE_AT_UNIMPLEMENTED_LEARNER_TODO' if card['id'] in current_candidate_runs
                else 'NOT_RUN_ON_CURRENT_CANDIDATE'
            ),
            'learner_acceptance_suite': acceptance_suites.get(card['id']),
            'scaffold_status': status, 'harness_family': family,
            'source_planned_primary_path': planned_source,
            'canonical_derived_primary_path': derived_paths.get(card['id']),
            'source_path_reconciled': bool(derived_paths.get(card['id']) and planned_source != derived_paths.get(card['id'])),
            'scaffold_strategy': scaffold_strategy,
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
