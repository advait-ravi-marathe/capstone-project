"""Reference mechanics for exact-action, in-memory single-use approval."""
from __future__ import annotations
import hashlib, json, os, pathlib, secrets, threading, time
from dataclasses import dataclass

def canonical_action_digest(action: str, target: str, content: bytes, source_hash: str, scope_hash: str,
                            source_path: str, session_binding: str) -> str:
    if not all(isinstance(x,str) and x for x in (action,target,source_hash,scope_hash,source_path,session_binding)) or not isinstance(content,bytes):
        raise ValueError('invalid action fields')
    record={'action':action,'target':target,'content_sha256':hashlib.sha256(content).hexdigest(),
            'source_sha256':source_hash,'source_path':source_path,'scope_sha256':scope_hash,
            'session_binding':session_binding}
    return hashlib.sha256(json.dumps(record,sort_keys=True,separators=(',',':')).encode()).hexdigest()

@dataclass
class Approval:
    digest: str
    expires_at: float
    nonce: str

class ApprovalLedger:
    """Process-local only; never load approval authority from workspace files."""
    def __init__(self): self._items={}; self._lock=threading.Lock()
    def issue(self,digest: str,expires_at: float,now: float) -> str:
        if not isinstance(digest,str) or len(digest)!=64 or expires_at<=now: raise ValueError('invalid approval')
        token=secrets.token_urlsafe(24)
        with self._lock: self._items[token]=Approval(digest,expires_at,secrets.token_hex(16))
        return token
    def consume(self,token: str,digest: str,now: float) -> bool:
        with self._lock:
            item=self._items.pop(token,None)
            return bool(item and now < item.expires_at and secrets.compare_digest(item.digest,digest))

def authorize_resource(root: str, relative_path: str) -> str:
    base=pathlib.Path(root).resolve(strict=True)
    if not isinstance(relative_path,str) or not relative_path or pathlib.Path(relative_path).is_absolute():
        raise PermissionError('resource must be relative')
    if '..' in pathlib.PurePath(relative_path).parts: raise PermissionError('parent traversal is denied')
    p=(base/relative_path).resolve(strict=True)
    if not p.is_relative_to(base) or not p.is_file(): raise PermissionError('resource escapes scratch root')
    if p.stat().st_size>100_000: raise ValueError('resource exceeds limit')
    return str(p)

def consume_approval(ledger: ApprovalLedger, approval_id: str, action_digest: str, now: float) -> bool:
    return ledger.consume(approval_id,action_digest,now)

def write_proposal(root: str, ledger: ApprovalLedger, token: str, source_path: str, target_name: str,
                   content: bytes, source_hash: str, scope_hash: str, session_binding: str,
                   now: float | None=None) -> pathlib.Path:
    """Consume authorization before a no-overwrite proposal file write."""
    now=time.time() if now is None else now
    if not isinstance(target_name,str) or not target_name.endswith('.patch') or pathlib.Path(target_name).name!=target_name:
        raise PermissionError('proposal must be a new leaf .patch filename')
    if not isinstance(content,bytes) or len(content)>32_000: raise ValueError('proposal too large')
    actual=hashlib.sha256(pathlib.Path(authorize_resource(root,source_path)).read_bytes()).hexdigest()
    if not secrets.compare_digest(actual,source_hash): raise PermissionError('source changed')
    digest=canonical_action_digest('write_patch_proposal',target_name,content,source_hash,scope_hash,source_path,session_binding)
    if not consume_approval(ledger,token,digest,now): raise PermissionError('missing, changed, expired or replayed approval')
    base=pathlib.Path(root).resolve(strict=True); out=base/'patch-proposals'/target_name
    out.parent.mkdir(mode=0o700,exist_ok=True)
    if not out.parent.resolve(strict=True).is_relative_to(base): raise PermissionError('proposal folder escaped root')
    fd=os.open(out,os.O_WRONLY|os.O_CREAT|os.O_EXCL,0o600)
    try:
        with os.fdopen(fd,'wb') as f: f.write(content); f.flush(); os.fsync(f.fileno())
    except BaseException:
        try: out.unlink()
        except OSError: pass
        raise
    return out

def write_patch_proposal(root: str, ledger: ApprovalLedger, approval_id: str, target_name: str,
                        content: bytes, source_hash: str, scope_hash: str, source_path: str,
                        session_binding: str,
                        now: float | None = None) -> pathlib.Path:
    return write_proposal(root,ledger,approval_id,source_path,target_name,content,source_hash,scope_hash,session_binding,now)
