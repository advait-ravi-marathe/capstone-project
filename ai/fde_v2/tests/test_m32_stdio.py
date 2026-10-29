"""Real official MCP SDK stdio client/server exchange; approval callback is test-only."""
import asyncio,hashlib,json,os,subprocess,sys,tempfile,unittest
from pathlib import Path
from mcp import Client
from mcp.client.stdio import StdioServerParameters
from mcp.types import ElicitResult

ROOT=Path(__file__).resolve().parents[3]

def result_object(result):
 if getattr(result,'structuredContent',None): return dict(result.structuredContent)
 for block in result.content:
  if getattr(block,'text',None):
   try: return json.loads(block.text)
   except json.JSONDecodeError: pass
 raise AssertionError(f'MCP response did not contain a JSON tool result: isError={getattr(result,"isError",None)}, content={result.content!r}')

class M32ActualStdioIntegration(unittest.TestCase):
 def test_initializes_lists_tools_reads_changed_file_and_writes_only_test_approved_scratch_patch(self):
  async def run():
   with tempfile.TemporaryDirectory() as td:
    root=Path(td); source=root/'src.py'; source.write_text('actual source v1')
    env={'PATH':os.environ.get('PATH',''),'FDE_M32_TEST_ONLY':'1','PYTHONPATH':str(ROOT),
         'PYTHONDONTWRITEBYTECODE':'1','PYTHONIOENCODING':'utf-8'}
    server=StdioServerParameters(command=sys.executable,args=['-m','ai.fde_v2.m32.server','--root',td,
      '--reference-fixture','--test-approval'],cwd=str(ROOT),env=env)
    async def approve(_context,params):
     if params.message.startswith('TEST FIXTURE ONLY: approve exact scratch patch one.patch'):
      return ElicitResult(action='accept',content={'approve':True})
     return ElicitResult(action='decline')
    async with Client(server,elicitation_callback=approve) as session:
     self.assertTrue(session.protocol_version)
     tools=await session.list_tools()
     self.assertEqual({x.name for x in tools.tools},{'read_allowed_file','write_patch_proposal'})
     read1=result_object(await session.call_tool('read_allowed_file',{'path':'src.py'}))
     self.assertEqual(read1['text'],'actual source v1')
     source_hash=hashlib.sha256(source.read_bytes()).hexdigest()
     args={'source_path':'src.py','target_name':'one.patch','content':'review-only diff','source_sha256':source_hash}
     out=result_object(await session.call_tool('write_patch_proposal',args))
     self.assertEqual(out['status'],'written_to_scratch')
     self.assertFalse(out['human_approval_evidenced'])
     self.assertEqual((root/'patch-proposals'/'one.patch').read_text(),'review-only diff')
     source.write_text('actual source v2')
     read2=result_object(await session.call_tool('read_allowed_file',{'path':'src.py'}))
     self.assertEqual(read2['text'],'actual source v2')
     stale=result_object(await session.call_tool('write_patch_proposal',{'source_path':'src.py','target_name':'two.patch',
       'content':'stale proposal','source_sha256':source_hash}))
     self.assertEqual(stale['status'],'DENIED')
  asyncio.run(run())

 def test_default_application_server_reads_scratch_without_learner_code_and_blocks_writes(self):
  async def run():
   with tempfile.TemporaryDirectory() as td:
    root=Path(td); (root/'src.py').write_text('source')
    env={'PATH':os.environ.get('PATH',''),'PYTHONPATH':str(ROOT),
         'PYTHONDONTWRITEBYTECODE':'1','PYTHONIOENCODING':'utf-8'}
    server=StdioServerParameters(command=sys.executable,args=['-m','ai.fde_v2.m32.server','--root',td],
                                  cwd=str(ROOT),env=env)
    async with Client(server) as session:
     read=result_object(await session.call_tool('read_allowed_file',{'path':'src.py'}))
     self.assertEqual(read['text'],'source')
     sha=hashlib.sha256((root/'src.py').read_bytes()).hexdigest()
     result=await session.call_tool('write_patch_proposal',{'source_path':'src.py','target_name':'blocked.patch',
       'content':'must not write','source_sha256':sha})
     blocked=result_object(result)
     self.assertEqual(blocked['status'],'BLOCKED')
     self.assertFalse((root/'patch-proposals'/'blocked.patch').exists())
     self.assertTrue(result.is_error is False)
  asyncio.run(run())

if __name__=='__main__': unittest.main()
