"""Actual MCP stdio server. Writes are disabled unless test-only UI approval is explicit."""
import argparse, asyncio, hashlib, os, pathlib, secrets, time
from typing import Annotated
from pydantic import BaseModel
from mcp.server import MCPServer
from mcp.server.mcpserver import Context
from mcp.server.mcpserver import AcceptedElicitation, DeclinedElicitation, Elicit, ElicitationResult, Resolve
from ai.fde_v2.m32 import learner, reference

class HumanApproval(BaseModel):
    approve: bool

def build_server(root: str, adapter=reference, *, enable_test_approval=False):
    base=pathlib.Path(root).resolve(strict=True)
    app=MCPServer('fde-v2-scratch-tools',instructions='Reads are confined to the owned scratch root. No network tools.')
    ledger=reference.ApprovalLedger(); scope_hash=hashlib.sha256(str(base).encode()).hexdigest()
    session_binding=secrets.token_urlsafe(24)

    async def ask_approval(source_path: str, target_name: str, content: str, source_sha256: str,
                           ctx: Context) -> ElicitationResult[HumanApproval] | Elicit[HumanApproval]:
        if not enable_test_approval:
            return DeclinedElicitation()
        if (not isinstance(content,str) or len(content.encode('utf-8'))>32_000
                or not isinstance(target_name,str) or not target_name.endswith('.patch')
                or pathlib.Path(target_name).name!=target_name):
            return DeclinedElicitation()
        source=adapter.authorize_resource(str(base),source_path)
        observed=hashlib.sha256(pathlib.Path(source).read_bytes()).hexdigest()
        if not isinstance(source_sha256,str) or not __import__('hmac').compare_digest(observed,source_sha256):
            return DeclinedElicitation()
        raw=content.encode('utf-8') if isinstance(content,str) else b''
        digest=adapter.canonical_action_digest('write_patch_proposal',target_name,raw,source_sha256,scope_hash,source_path,session_binding)
        return Elicit(f'TEST FIXTURE ONLY: approve exact scratch patch {target_name}, action SHA256 {digest}?',HumanApproval)

    @app.tool()
    def read_allowed_file(path: str) -> dict:
        """Read a UTF-8 file under the configured scratch root."""
        resolved=adapter.authorize_resource(str(base),path)
        raw=pathlib.Path(resolved).read_bytes()
        return {'path':path,'sha256':hashlib.sha256(raw).hexdigest(),'text':raw.decode('utf-8')}

    @app.tool()
    async def write_patch_proposal(source_path: str, target_name: str, content: str, source_sha256: str,
                                   approval: Annotated[ElicitationResult[HumanApproval], Resolve(ask_approval)]) -> dict:
        """Write a new .patch file into scratch only after exact-action elicitation."""
        if not enable_test_approval:
            return {'status':'BLOCKED','reason':'trusted human approval UI is not configured'}
        if not isinstance(content,str) or len(content.encode('utf-8'))>32_000:
            return {'status':'DENIED','reason':'proposal too large or invalid'}
        source=adapter.authorize_resource(str(base),source_path)
        observed=hashlib.sha256(pathlib.Path(source).read_bytes()).hexdigest()
        if not isinstance(source_sha256,str) or not __import__('hmac').compare_digest(observed,source_sha256):
            return {'status':'DENIED','reason':'source changed or source digest mismatch'}
        raw=content.encode('utf-8')
        digest=adapter.canonical_action_digest('write_patch_proposal',target_name,raw,source_sha256,scope_hash,source_path,session_binding)
        # MCP clients may be agents. Test-only input is never human-consent evidence.
        if not isinstance(approval,AcceptedElicitation) or approval.data.approve is not True:
            return {'status':'DENIED','reason':'approval missing, declined or cancelled'}
        token=ledger.issue(digest,time.time()+30,time.time())
        try:
            out=adapter.write_patch_proposal(str(base),ledger,token,target_name,raw,source_sha256,scope_hash,source_path,session_binding)
        except (ValueError,PermissionError,FileExistsError) as exc:
            return {'status':'DENIED','reason':type(exc).__name__}
        return {'status':'written_to_scratch','filename':out.name,'sha256':hashlib.sha256(raw).hexdigest(),
                'approval_consumed':True,'human_approval_evidenced':False}
    return app

def main(argv=None):
    p=argparse.ArgumentParser(); p.add_argument('--root',required=True); p.add_argument('--test-approval',action='store_true')
    p.add_argument('--reference-fixture',action='store_true')
    p.add_argument('--learner-adapter',action='store_true',help='explicitly run unfinished learner adapter for review')
    a=p.parse_args(argv)
    # The explicit reference/test switches are denied unless called by the offline test harness.
    if (a.test_approval or a.reference_fixture) and os.environ.get('FDE_M32_TEST_ONLY')!='1':
        raise SystemExit('reference/test mode requires FDE_M32_TEST_ONLY=1')
    adapter=learner if a.learner_adapter else reference
    app=build_server(a.root,adapter,enable_test_approval=bool(a.test_approval))
    asyncio.run(app.run_stdio_async())
if __name__=='__main__': main()
