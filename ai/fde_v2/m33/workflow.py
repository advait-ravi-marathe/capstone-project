"""Feature path for real scratch reads and replay-safe local patch proposals."""
from __future__ import annotations
import hashlib
import json
import sqlite3
from typing import Any, Mapping, Sequence
from ai.fde_v2.m31 import learner as m31_learner, workflow as m31_workflow
from ai.fde_v2.m32 import reference as m32_reference
from . import learner, reference


def run_controlled_patch(root: str, action: Mapping[str, Any], *, adapter=learner,
                         approval_fixture=None, crash_after_effect=False) -> dict[str, Any]:
    """Execute one test-authorized scratch proposal and reconcile replay/uncertainty.

    `approval_fixture` is an instructor test callback only. This function never
    treats model/tool text as consent and never claims human approval evidence.
    """
    required={"action_id","source_path","target_name","content","source_sha256","scope_sha256","session_binding"}
    if not isinstance(action,Mapping) or set(action)!=required:
        raise ValueError("controlled patch action fields are incomplete or unknown")
    action_id=action["action_id"]
    if not isinstance(action_id,str) or not action_id.strip(): raise ValueError("action_id is required")
    content=action["content"]
    if not isinstance(content,str): raise ValueError("patch content must be text")
    raw=content.encode("utf-8")
    digest=m32_reference.canonical_action_digest("write_patch_proposal", action["target_name"], raw,
        action["source_sha256"], action["scope_sha256"], action["source_path"], action["session_binding"])
    path=__import__("pathlib").Path(root).resolve(strict=True)
    journal=path/".m33-action-journal.sqlite3"
    db=sqlite3.connect(journal)
    try:
        db.execute("CREATE TABLE IF NOT EXISTS actions(action_id TEXT PRIMARY KEY,digest TEXT NOT NULL,status TEXT NOT NULL,target TEXT NOT NULL,content_sha256 TEXT NOT NULL)")
        previous=db.execute("SELECT digest,status,target,content_sha256 FROM actions WHERE action_id=?",(action_id,)).fetchone()
        if previous:
            if previous[0]!=digest: return {"status":"DENIED_CHANGED_ACTION","effect_count":0,"human_approval_evidenced":False}
            if previous[1]=="completed": return {"status":"REPLAY_RECONCILED","effect_count":0,"target":previous[2],"human_approval_evidenced":False}
            output=path/"patch-proposals"/previous[2]
            observed="effect_present" if output.is_file() and hashlib.sha256(output.read_bytes()).hexdigest()==previous[3] else "uncertain"
            outcome=adapter.reconcile_action({"observed_status":observed})
            if observed=="effect_present":
                db.execute("UPDATE actions SET status='completed' WHERE action_id=?",(action_id,)); db.commit()
                return {"status":"RECONCILED_EXISTING_EFFECT","effect_count":0,"target":previous[2],"reconcile":outcome,"human_approval_evidenced":False}
            return {"status":"RECONCILIATION_REQUIRED","effect_count":0,"reconcile":outcome,"human_approval_evidenced":False}
        if not isinstance(approval_fixture,Mapping) or approval_fixture.get("approved") is not True or approval_fixture.get("action_digest")!=digest:
            return {"status":"DENIED","effect_count":0,"human_approval_evidenced":False}
        ledger=m32_reference.ApprovalLedger()
        token=ledger.issue(digest,time_now()+30,time_now())
        db.execute("INSERT INTO actions VALUES(?,?, 'pending',?,?)",(action_id,digest,action["target_name"],hashlib.sha256(raw).hexdigest())); db.commit()
        try:
            out=m32_reference.write_patch_proposal(root,ledger,token,action["target_name"],raw,
                action["source_sha256"],action["scope_sha256"],action["source_path"],action["session_binding"],time_now())
        except (ValueError,PermissionError,FileExistsError) as exc:
            db.execute("DELETE FROM actions WHERE action_id=?",(action_id,)); db.commit()
            return {"status":"DENIED","effect_count":0,"error_type":type(exc).__name__,"human_approval_evidenced":False}
        if crash_after_effect:
            # Simulate a lost response after the filesystem effect but before
            # the durable journal completion update.
            return {"status":"OUTCOME_UNKNOWN","effect_count":1,"target":out.name,"human_approval_evidenced":False}
        db.execute("UPDATE actions SET status='completed' WHERE action_id=?",(action_id,)); db.commit()
        return {"status":"COMPLETED","effect_count":1,"target":out.name,"human_approval_evidenced":False}
    finally:
        db.close()


def time_now():
    import time
    return time.time()


def run_task_suite(root: str, cases: Sequence[Mapping[str, Any]], *,
                   tool_adapter=m31_learner, evaluator=learner,
                   approval_fixture_provider=None) -> list[dict[str, Any]]:
    rows=[]
    for case in cases:
        if case.get("kind")=="controlled_patch":
            action=case.get("action",{})
            approval=(approval_fixture_provider(action) if approval_fixture_provider else None)
            attempts=[run_controlled_patch(root,action,adapter=evaluator,approval_fixture=approval,
                      crash_after_effect=case.get("simulate_lost_response") is True)]
            if case.get("simulate_lost_response") is True or case.get("expect_replay") is True:
                attempts.append(run_controlled_patch(root,action,adapter=evaluator,approval_fixture=approval))
            observed={"trace":[{"tool":"write_patch_proposal","result":attempts[-1],"attempts":attempts,
                                "effect_count":sum(row.get("effect_count",0) for row in attempts)}]}
        else:
            observed=m31_workflow.run_workflow(root,case.get("plan",{}),adapter=tool_adapter)
        rows.append(evaluator.score_case(case,observed))
    return rows
