"""Reference mechanics for local change/incident evidence; no production claims."""
from __future__ import annotations
import math
import hashlib
import json
import sqlite3
import subprocess
import sys
import time
import os
from pathlib import Path
from typing import Any, Mapping, Sequence

REQUIRED_CHANGE={"requirement","old_behavior","new_behavior","risk","rollback_target","compatibility"}
INCIDENT_STAGES=("detection","diagnosis","mitigation","recovery","prevention")
SUCCESS_OUTCOMES={"detection":"detected","diagnosis":"identified","mitigation":"mitigated",
                  "recovery":"recovered","prevention":"prevention_added"}
ALLOWED_OUTCOMES={"detection":{"detected","missed","not_observed"},
                  "diagnosis":{"identified","misdiagnosed","unknown"},
                  "mitigation":{"mitigated","not_mitigated","not_attempted"},
                  "recovery":{"recovered","not_recovered","not_attempted"},
                  "prevention":{"prevention_added","not_added","deferred"}}

def validate_change_contract(record: Mapping[str, Any]) -> dict[str, Any]:
    if not isinstance(record,Mapping) or not REQUIRED_CHANGE.issubset(record): raise ValueError("change contract is incomplete")
    if any(not isinstance(record[k],str) or not record[k].strip() for k in REQUIRED_CHANGE): raise ValueError("change fields must be nonempty text")
    if record.get("scope") not in {"minimal","expanded"}: raise ValueError("scope must be explicit")
    if record.get("scope")!="minimal": return {"status":"BLOCKED","reason":"scope is not the minimal requested change"}
    tests=record.get("regression_tests")
    if not isinstance(tests,list) or not tests or any(not isinstance(t,str) or not t for t in tests):
        raise ValueError("regression test selectors are required")
    return {"status":"READY_FOR_TEST_EVIDENCE","requirement":record["requirement"],
            "rollback_target":record["rollback_target"],"risk":record["risk"],"compatibility":record["compatibility"]}

def summarize_observations(rows: Sequence[Mapping[str, Any]]) -> dict[str, Any]:
    if not isinstance(rows,(list,tuple)) or not rows: raise ValueError("nonempty complete observation rows required")
    ids=[]; durations=[]; successes=0; errors=0; timeouts=0
    for row in rows:
        if not isinstance(row,Mapping) or not isinstance(row.get("request_id"),str) or not row["request_id"]:
            raise ValueError("each observation needs a request id")
        ids.append(row["request_id"]); status=row.get("status"); ms=row.get("elapsed_ms")
        if status not in {"success","error","timeout"}: raise ValueError("unknown request status")
        if not isinstance(ms,(int,float)) or isinstance(ms,bool) or not math.isfinite(float(ms)) or ms<0:
            raise ValueError("elapsed milliseconds must be finite and nonnegative")
        if status=="success": successes+=1
        elif status=="error": errors+=1
        else: timeouts+=1
        if status=="timeout":
            penalty=row.get("timeout_penalty_ms")
            if not isinstance(penalty,(int,float)) or isinstance(penalty,bool) or not math.isfinite(float(penalty)) or penalty<ms:
                raise ValueError("timeout needs its declared censoring penalty")
            durations.append(float(penalty))
        else: durations.append(float(ms))
    if len(set(ids))!=len(ids): raise ValueError("duplicate request id")
    ordered=sorted(durations); p95=ordered[max(0,math.ceil(.95*len(ordered))-1)]
    successful=sorted(float(r["elapsed_ms"]) for r in rows if r["status"]=="success")
    median=(successful[(len(successful)-1)//2] if len(successful)%2 else
            (successful[len(successful)//2-1]+successful[len(successful)//2])/2) if successful else None
    return {"request_count":len(rows),"success_count":successes,"error_count":errors,"timeout_count":timeouts,
            "availability_fraction":successes/len(rows),"all_request_p95_ms":p95,"success_only_median_ms":median,
            "cost":None,"cost_status":"not_observed","claim_scope":"short_local_observation_only"}

def validate_incident(record: Mapping[str, Any]) -> dict[str, Any]:
    if not isinstance(record,Mapping) or record.get("classification") not in {"local_drill","real_incident"}:
        raise ValueError("incident must be labeled local_drill or real_incident")
    events=record.get("events")
    if not isinstance(events,list) or len(events)!=len(INCIDENT_STAGES): return {"status":"INCOMPLETE","missing":list(INCIDENT_STAGES)}
    observed=[]
    for idx,(event,stage) in enumerate(zip(events,INCIDENT_STAGES)):
        if not isinstance(event,Mapping) or event.get("stage")!=stage or not isinstance(event.get("evidence_ref"),str) or not event["evidence_ref"]:
            return {"status":"INCOMPLETE","missing":list(INCIDENT_STAGES[idx:])}
        time_kind=event.get("time_kind")
        if time_kind not in {"observed","estimated"}: raise ValueError("event time provenance must be explicit")
        if time_kind=="estimated" and not event.get("estimate_reason"): raise ValueError("estimated time needs a reason")
        outcome=event.get("observed_status")
        if not isinstance(outcome,str) or outcome not in ALLOWED_OUTCOMES[stage]:
            raise ValueError(f"{stage} outcome must use a declared value")
        observed.append(outcome)
        if outcome!=SUCCESS_OUTCOMES[stage]:
            return {"status":"INCOMPLETE","missing":[stage],"reason":f"{stage} outcome is {outcome}; incident is not recovered/prevented"}
    return {"status":"COMPLETE_REQUIRES_REVIEW","classification":record["classification"],
            "events":len(events),"outcomes":observed,
            "cloud_incident_evidence":"BLOCKED" if record["classification"]=="local_drill" else "REVIEW_REQUIRED"}

def evaluate_change_gate(change: Mapping[str, Any], observation: Mapping[str, Any], incident: Mapping[str, Any]) -> dict[str, Any]:
    c=validate_change_contract(change)
    if c["status"]!="READY_FOR_TEST_EVIDENCE": return {"status":"BLOCKED","reason":c.get("reason","change contract incomplete")}
    if not isinstance(observation,Mapping) or observation.get("status")!="PASS" or not observation.get("source_hash") or not observation.get("test_run_id"):
        return {"status":"BLOCKED","reason":"actual scoped regression-test receipt is missing"}
    i=validate_incident(incident)
    if i["status"]!="COMPLETE_REQUIRES_REVIEW": return {"status":"BLOCKED","reason":"incident recovery sequence is incomplete"}
    return {"status":"EVIDENCE_REQUIRES_REVIEW","change_status":"TEST_RECEIPT_PRESENT",
            "incident_classification":i["classification"],"production_reliability_claim":False,
            "live_cloud_status":"BLOCKED" if i["classification"]=="local_drill" else "REVIEW_REQUIRED",
            "cost_observed":False}


def prepare_local_fixture(root: str) -> str:
    """Create a disposable service with a real failing regression test."""
    path=Path(root).resolve(); path.mkdir(parents=True,exist_ok=True)
    source=path/"service.py"; source.write_text("def answer():\n    return 'old'\n",encoding="utf-8")
    (path/"test_service.py").write_text("import unittest\nfrom service import answer\nclass T(unittest.TestCase):\n    def test_requested_behavior(self): self.assertEqual(answer(), 'new')\n",encoding="utf-8")
    return hashlib.sha256(source.read_bytes()).hexdigest()

def apply_minimal_fixture_patch(root: str, source_sha256: str) -> dict[str, Any]:
    source=Path(root).resolve(strict=True)/"service.py"; raw=source.read_bytes()
    if hashlib.sha256(raw).hexdigest()!=source_sha256 or raw!=b"def answer():\n    return 'old'\n":
        raise ValueError("fixture source changed; patch is stale or broader than expected")
    source.write_text("def answer():\n    return 'new'\n",encoding="utf-8")
    return {"path":"service.py","before_sha256":source_sha256,
            "after_sha256":hashlib.sha256(source.read_bytes()).hexdigest(),"scope":"one_function"}

def run_regression(root: str) -> dict[str, Any]:
    path=Path(root).resolve(strict=True)
    result=subprocess.run([sys.executable,"-m","unittest","-v","test_service"],cwd=path,
        capture_output=True,text=True,timeout=20,env={**__import__("os").environ,"PYTHONDONTWRITEBYTECODE":"1"})
    return {"returncode":result.returncode,"status":"PASS" if result.returncode==0 else "FAIL",
            "test_run_id":hashlib.sha256((str(path)+result.stdout+result.stderr).encode()).hexdigest(),
            "source_hash":hashlib.sha256((path/"service.py").read_bytes()).hexdigest(),
            "output":(result.stdout+result.stderr)[-4000:]}

def migrate_fixture_database(path: str) -> dict[str, Any]:
    db_path=Path(path)
    backup_path=Path(path+".pre-migration")
    if db_path.is_symlink() or backup_path.is_symlink() or backup_path.exists():
        raise PermissionError("migration requires a fresh disposable database and absent backup path")
    db=sqlite3.connect(path)
    try:
        with db:
            db.execute("CREATE TABLE IF NOT EXISTS docs(doc_id TEXT PRIMARY KEY, body TEXT NOT NULL)")
            db.execute("INSERT OR IGNORE INTO docs VALUES('fixture-doc','synthetic')")
        backup=sqlite3.connect(path+".pre-migration")
        try: db.backup(backup)
        finally: backup.close()
        with db:
            columns={row[1] for row in db.execute("PRAGMA table_info(docs)")}
            if "retention_state" not in columns:
                db.execute("ALTER TABLE docs ADD COLUMN retention_state TEXT NOT NULL DEFAULT 'active'")
        before=sqlite3.connect(path+".pre-migration")
        try:
            old_rows=before.execute("SELECT doc_id,body FROM docs ORDER BY doc_id").fetchall()
        finally: before.close()
        new_rows=db.execute("SELECT doc_id,body,retention_state FROM docs ORDER BY doc_id").fetchall()
        if ([(r[0],r[1]) for r in new_rows]!=old_rows
                or any(r[2]!="active" for r in new_rows)):
            raise RuntimeError("migration did not preserve every original row")
        return {"status":"PASS","row_count":len(old_rows),"schema_version":2,
                "pre_migration_backup":path+".pre-migration"}
    finally: db.close()

def rollback_fixture_database(path: str) -> dict[str, Any]:
    backup_path=path+".pre-migration"
    if Path(path).is_symlink() or Path(backup_path).is_symlink() or not Path(backup_path).is_file():
        return {"status":"FAIL","restored":False,"reason":"database or backup path is unsafe or absent"}
    backup=sqlite3.connect(backup_path)
    try:
        schema=_sqlite_schema(backup)
        if not schema or not any(row[1]=="docs" and row[0]=="table" for row in schema):
            return {"status":"FAIL","restored":False,"reason":"backup schema is incomplete"}
    finally: backup.close()
    expected=_snapshot_database(backup_path)
    restore=sqlite3.connect(path)
    try:
        source=sqlite3.connect(backup_path)
        try: source.backup(restore)
        finally: source.close()
        observed=_snapshot_connection(restore)
        ok=observed==expected
        return {"status":"PASS" if ok else "FAIL","restored":ok,"schema_version":1,
                "data_preserved":ok,"restored_schema_objects":len(observed[0]),
                "restored_table_rows":sum(len(rows) for _name,rows in observed[1])}
    finally: restore.close()


def _sqlite_schema(db):
    return db.execute("SELECT type,name,tbl_name,sql FROM sqlite_master WHERE name NOT LIKE 'sqlite_%' ORDER BY type,name").fetchall()


def _snapshot_connection(db):
    schema=[tuple(row) for row in _sqlite_schema(db)]
    tables=[]
    for kind,name,_table,_sql in schema:
        if kind!="table": continue
        quoted='"'+name.replace('"','""')+'"'
        rows=[tuple(row) for row in db.execute(f"SELECT * FROM {quoted}")]
        tables.append((name,sorted(rows,key=repr)))
    return schema,tables


def _snapshot_database(path):
    db=sqlite3.connect(path)
    try: return _snapshot_connection(db)
    finally: db.close()

def run_fixture_incident(root: str) -> dict[str, Any]:
    path=Path(root).resolve(strict=True); config=path/"dependency.json"
    if config.is_symlink() or config.exists(): raise PermissionError("incident lab requires an unused scratch root")
    config.write_text(json.dumps({"dependency":"ok"}),encoding="utf-8")
    probe_path=path/"probe.py"
    probe_path.write_text(
        "import json,sys\n"
        "d=json.load(open(sys.argv[1],encoding='utf-8'))\n"
        "if d.get('dependency')!='ok':\n"
        " print('dependency_guard=reject')\n"
        " sys.exit(2)\n"
        "print('dependency_guard=ready')\n",encoding="utf-8")
    def observe(target=config):
        completed=subprocess.run([sys.executable,str(probe_path),str(target)],cwd=path,capture_output=True,text=True,timeout=10)
        return {"returncode":completed.returncode,"stdout":completed.stdout,"stderr":completed.stderr,
                "run_sha256":hashlib.sha256((completed.stdout+completed.stderr+str(completed.returncode)).encode()).hexdigest()}
    def event(stage,outcome,evidence):
        return {"stage":stage,"observed_status":outcome,"time_kind":"observed",
                "evidence_ref":evidence}
    events=[]
    config.write_text(json.dumps({"dependency":"injected-failure"}),encoding="utf-8")
    failed=observe()
    events.append(event("detection","detected" if failed["returncode"]!=0 else "missed",failed["run_sha256"]))
    if failed["returncode"]==0: return {"classification":"local_drill","status":"FAIL","events":events}
    diagnosed=(failed["returncode"]==2 and "dependency_guard=reject" in failed["stdout"])
    events.append(event("diagnosis","identified" if diagnosed else "unknown",failed["run_sha256"]))
    config.write_text(json.dumps({"dependency":"ok"}),encoding="utf-8")
    mitigated=json.loads(config.read_text(encoding="utf-8")).get("dependency")=="ok"
    events.append(event("mitigation","mitigated" if mitigated else "not_mitigated",
                        hashlib.sha256(config.read_bytes()).hexdigest()))
    recovered=observe()
    events.append(event("recovery","recovered" if recovered["returncode"]==0 else "not_recovered",
                        recovered["run_sha256"]))
    # The regression test injects a bad config copy and passes only if the
    # runtime guard rejects it. This is an executed prevention witness.
    bad_copy=path/"dependency-bad-case.json"
    bad_copy.write_text(json.dumps({"dependency":"injected-failure"}),encoding="utf-8")
    guard_test=path/"test_guard.py"
    guard_test.write_text(
        "import subprocess,sys\n"
        "result=subprocess.run([sys.executable,'probe.py',sys.argv[1]],capture_output=True,text=True)\n"
        "assert result.returncode==2 and 'dependency_guard=reject' in result.stdout\n"
        "print('guard regression passed')\n",encoding="utf-8")
    regression=subprocess.run([sys.executable,str(guard_test),str(bad_copy)],cwd=path,
        capture_output=True,text=True,timeout=10)
    guard_test_passed=(regression.returncode==0 and "guard regression passed" in regression.stdout)
    events.append(event("prevention","prevention_added" if guard_test_passed else "not_added",
                        hashlib.sha256((regression.stdout+regression.stderr+str(regression.returncode)).encode()).hexdigest()))
    bad_copy.unlink(missing_ok=True)
    all_observed=[e["observed_status"] for e in events]
    passed=all_observed==[SUCCESS_OUTCOMES[stage] for stage in INCIDENT_STAGES]
    return {"classification":"local_drill","status":"PASS" if passed else "FAIL","events":events,
            "probe_returncodes":[failed["returncode"],recovered["returncode"]],
            "prevention_test_returncode":regression.returncode,
            "cloud_incident_evidence":"BLOCKED","diagnosis_observed":diagnosed,
            "prevention_regression_executed":True,"all_stages_observed":passed}

def verify_fixture_retention(path: str) -> dict[str, Any]:
    db=sqlite3.connect(path)
    try:
        with db:
            db.execute("CREATE TABLE IF NOT EXISTS retained_docs(doc_id TEXT PRIMARY KEY,body TEXT NOT NULL,deleted INTEGER NOT NULL DEFAULT 0)")
            db.execute("INSERT OR REPLACE INTO retained_docs VALUES('delete-me','SYNTHETIC-CANARY',0)")
        visible_before=db.execute("SELECT body FROM retained_docs WHERE doc_id='delete-me' AND deleted=0").fetchone()
        with db:
            db.execute("UPDATE retained_docs SET deleted=1 WHERE doc_id='delete-me'")
        visible_after_tombstone=db.execute("SELECT body FROM retained_docs WHERE doc_id='delete-me' AND deleted=0").fetchone()
        with db:
            db.execute("DELETE FROM retained_docs WHERE doc_id='delete-me'")
        physical_after_delete=db.execute("SELECT body FROM retained_docs WHERE doc_id='delete-me'").fetchone()
        return {"status":"PASS" if visible_before and visible_after_tombstone is None and physical_after_delete is None else "FAIL",
                "visible_before":bool(visible_before),"hidden_after_tombstone":visible_after_tombstone is None,
                "physically_removed":physical_after_delete is None,"data_classification":"synthetic_fixture"}
    finally: db.close()
