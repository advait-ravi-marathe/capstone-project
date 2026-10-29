"""Feature path routes change/recovery assessments through learner-owned seams."""
from __future__ import annotations
import tempfile
from pathlib import Path
from typing import Any, Mapping, Sequence
from . import reference as application_adapter
from . import reference

def review_change(change: Mapping[str, Any], observations: Sequence[Mapping[str, Any]], incident: Mapping[str, Any],
                  test_receipt: Mapping[str, Any], *, adapter=application_adapter) -> dict[str, Any]:
    c=adapter.validate_change_contract(change)
    o=adapter.summarize_observations(observations)
    i=adapter.validate_incident(incident)
    gate=adapter.evaluate_change_gate(change,{**test_receipt,"status":test_receipt.get("status")},incident)
    return {"change":c,"observations":o,"incident":i,"gate":gate}

def run_local_exercise(root: str | None = None, *, adapter=application_adapter) -> dict[str, Any]:
    """Run the instructor-owned disposable regression/migration/recovery lab.

    The selected adapter supplies every learner-owned operation. A temporary
    root is created if omitted; local synthetic receipts never imply production
    reliability or learner completion.
    """
    if root is None:
        root=tempfile.mkdtemp(prefix="fde-v2-m42-")
    supplied=Path(root)
    if supplied.is_symlink(): raise PermissionError("M4.2 lab root cannot be a symlink")
    supplied.mkdir(parents=True,exist_ok=True)
    base=supplied.resolve(strict=True)
    if any(base.iterdir()): raise PermissionError("M4.2 lab requires a new empty disposable scratch directory")
    code_root=base/"code"; code_root.mkdir(exist_ok=True)
    source_hash=reference.prepare_local_fixture(str(code_root))
    before=reference.run_regression(str(code_root))
    if before["returncode"]==0:
        raise RuntimeError("regression fixture unexpectedly passed before the change")
    patch=adapter.apply_minimal_fixture_patch(str(code_root),source_hash)
    after=reference.run_regression(str(code_root))
    db_path=str(base/"data.sqlite3")
    migration=adapter.migrate_fixture_database(db_path)
    rollback=adapter.rollback_fixture_database(db_path)
    incident=adapter.run_fixture_incident(str(base))
    retention=adapter.verify_fixture_retention(str(base/"retention.sqlite3"))
    return {"classification":"local_synthetic_drill","change":{"patch":patch,"before":before,"after":after},
            "migration":migration,"rollback":rollback,"incident":incident,"retention":retention,
            "local_mechanics_pass":(after["returncode"]==0 and migration.get("status")=="PASS"
                                    and rollback.get("status")=="PASS"
                                    and incident.get("status")=="PASS" and retention.get("status")=="PASS"),
            "live_cloud_status":"BLOCKED","learner_completion":False,
            "production_reliability_claim":False}
