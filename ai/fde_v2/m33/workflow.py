"""Feature path for real scratch reads and replay-safe local patch proposals."""
from __future__ import annotations
import hashlib
import json
import os
import sqlite3
import stat
from pathlib import Path
from typing import Any, Mapping, Sequence
from ai.fde_v2.m31 import reference as m31_application, workflow as m31_workflow
from ai.fde_v2.m32 import reference as m32_reference
from . import learner, reference


def _assert_leaf_beneath(root: Path, leaf: Path, *, allow_missing: bool = True) -> None:
    """Reject symlinks, hard-linked files, non-files and escaped scratch paths."""
    if leaf.parent.resolve(strict=True) != root:
        raise PermissionError("scratch artifact parent escaped the selected root")
    try:
        info=leaf.lstat()
    except FileNotFoundError:
        if allow_missing:
            return
        raise PermissionError("scratch artifact is missing")
    if stat.S_ISLNK(info.st_mode) or not stat.S_ISREG(info.st_mode) or info.st_nlink != 1:
        raise PermissionError("scratch artifact must be a single-link regular file, not a symlink")
    if not leaf.resolve(strict=True).is_relative_to(root):
        raise PermissionError("scratch artifact escaped the selected root")


def _safe_journal(root: Path, *, create: bool) -> Path:
    journal=root/".m33-action-journal.sqlite3"
    # SQLite may create these siblings depending on journal mode. Reject an
    # attacker-planted symlink before sqlite3 can open or mutate any of them.
    for path in (journal,Path(str(journal)+"-journal"),Path(str(journal)+"-wal"),Path(str(journal)+"-shm")):
        _assert_leaf_beneath(root,path)
    if not create:
        return journal
    flags=os.O_RDWR|os.O_CREAT|getattr(os,"O_NOFOLLOW",0)|getattr(os,"O_CLOEXEC",0)
    fd=os.open(journal,flags,0o600)
    try:
        info=os.fstat(fd)
        if not stat.S_ISREG(info.st_mode) or info.st_nlink != 1:
            raise PermissionError("journal must be a single-link regular file")
    finally:
        os.close(fd)
    _assert_leaf_beneath(root,journal,allow_missing=False)
    return journal


def _read_safe_proposal(root: Path, target_name: str) -> bytes | None:
    if (not isinstance(target_name,str) or not target_name.endswith(".patch")
            or Path(target_name).name!=target_name or target_name in {".",".."}):
        raise PermissionError("journal target is not a proposal basename")
    proposal_dir=root/"patch-proposals"
    if not proposal_dir.exists():
        return None
    if proposal_dir.is_symlink() or not proposal_dir.resolve(strict=True).is_relative_to(root):
        raise PermissionError("proposal directory escaped scratch root")
    target=proposal_dir/target_name
    try:
        _assert_leaf_beneath(proposal_dir,target,allow_missing=False)
    except PermissionError:
        raise
    except OSError as exc:
        raise PermissionError("proposal target is unavailable") from exc
    flags=os.O_RDONLY|getattr(os,"O_NOFOLLOW",0)|getattr(os,"O_CLOEXEC",0)
    fd=os.open(target,flags)
    try:
        info=os.fstat(fd)
        if not stat.S_ISREG(info.st_mode) or info.st_nlink != 1:
            raise PermissionError("proposal target is not a safe regular file")
        with os.fdopen(fd,"rb",closefd=False) as stream:
            return stream.read(32_001)
    finally:
        os.close(fd)


def run_controlled_patch(root: str, action: Mapping[str, Any], *, adapter=reference,
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
    supplied=Path(root)
    if supplied.is_symlink():
        raise PermissionError("scratch root cannot be a symlink")
    path=supplied.resolve(strict=True)
    if not path.is_dir(): raise PermissionError("scratch root must be a directory")
    journal=_safe_journal(path,create=False)
    previous=None
    if journal.exists():
        db_read=sqlite3.connect(f"file:{journal}?mode=ro",uri=True)
        try:
            table=db_read.execute("SELECT 1 FROM sqlite_master WHERE type='table' AND name='actions'").fetchone()
            if table:
                previous=db_read.execute("SELECT digest,status,target,content_sha256 FROM actions WHERE action_id=?",(action_id,)).fetchone()
        finally:
            db_read.close()
    if previous:
        if previous[0]!=digest: return {"status":"DENIED_CHANGED_ACTION","effect_count":0,"human_approval_evidenced":False}
        if previous[1]=="completed": return {"status":"REPLAY_RECONCILED","effect_count":0,"target":previous[2],"human_approval_evidenced":False}
        proposal=_read_safe_proposal(path,previous[2])
        observed="effect_present" if proposal is not None and hashlib.sha256(proposal).hexdigest()==previous[3] else "uncertain"
        outcome=adapter.reconcile_action({"observed_status":observed})
        if observed=="effect_present":
            db_write=sqlite3.connect(journal)
            try:
                db_write.execute("UPDATE actions SET status='completed' WHERE action_id=?",(action_id,)); db_write.commit()
            finally: db_write.close()
            return {"status":"RECONCILED_EXISTING_EFFECT","effect_count":0,"target":previous[2],"reconcile":outcome,"human_approval_evidenced":False}
        return {"status":"RECONCILIATION_REQUIRED","effect_count":0,"reconcile":outcome,"human_approval_evidenced":False}
    if not isinstance(approval_fixture,Mapping) or approval_fixture.get("approved") is not True or approval_fixture.get("action_digest")!=digest:
        return {"status":"DENIED","effect_count":0,"human_approval_evidenced":False}
    journal=_safe_journal(path,create=True)
    db=sqlite3.connect(journal)
    try:
        db.execute("CREATE TABLE IF NOT EXISTS actions(action_id TEXT PRIMARY KEY,digest TEXT NOT NULL,status TEXT NOT NULL,target TEXT NOT NULL,content_sha256 TEXT NOT NULL)")
        db.execute("INSERT INTO actions VALUES(?,?, 'pending',?,?)",(action_id,digest,action["target_name"],hashlib.sha256(raw).hexdigest())); db.commit()
        ledger=m32_reference.ApprovalLedger()
        token=ledger.issue(digest,time_now()+30,time_now())
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
                   tool_adapter=m31_application, evaluator=reference,
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
