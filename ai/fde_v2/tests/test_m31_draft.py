"""M3.1 reference mechanics use only temporary local fixtures."""
import json, tempfile, threading, unittest
from pathlib import Path
from ai.fde_v2.m31 import learner, reference
from ai.fde_v2.m31 import workflow

class M31Mechanics(unittest.TestCase):
    def test_schedule_preserves_budget_and_partial_stages(self):
        d=json.loads((Path(__file__).parents[1]/'m31'/'draft_schedule.json').read_text())
        self.assertEqual([r['day'] for r in d['days']],list(range(103,124)))
        self.assertEqual(sum(r['substantive_minutes'] for r in d['days']),1500)
        self.assertEqual(sum(r['buffer_minutes'] for r in d['days']),240)
        self.assertEqual(sum(r['visibility_minutes'] for r in d['days']),90)
        self.assertEqual(sum(r['unallocated_minutes'] for r in d['days']),60)
        self.assertTrue(all(r['dsa_minutes']==60 and r['role_bank_minutes']==30 and r['ai_minutes']==90 for r in d['days']))
        self.assertTrue(all(r['substantive_minutes']+r['buffer_minutes']+r['visibility_minutes']+r['unallocated_minutes']==90 for r in d['days']))

    def test_real_scratch_read_and_denied_scope(self):
        with tempfile.TemporaryDirectory() as td, tempfile.TemporaryDirectory() as outside:
            root=Path(td); (root/'src.txt').write_text('actual changed source')
            (Path(outside)/'secret.txt').write_text('private')
            out=reference.execute_tool(td,'read_file',{'path':'src.txt'})
            self.assertEqual(out['text'],'actual changed source'); self.assertEqual(len(out['sha256']),64)
            with self.assertRaises(PermissionError): reference.execute_tool(td,'read_file',{'path':'../secret.txt'})
            link=root/'escape'; link.symlink_to(Path(outside)/'secret.txt')
            with self.assertRaises(PermissionError): reference.execute_tool(td,'read_file',{'path':'escape'})

    def test_plan_bounds_args_and_unapproved_actions(self):
        p={'steps':[{'tool':'read_file','arguments':{'path':'x'}}]}
        self.assertEqual(reference.validate_plan(p),p)
        for bad in ({'steps':[{'tool':'shell','arguments':{'command':'id'}}]},
                    {'steps':[{'tool':[],'arguments':{}}]},
                    {'steps':[{'tool':'read_file','arguments':{'path':'x','root':'/'}}]},
                    {'steps':[{'tool':'write_file','arguments':{'path':'x'}}]}):
            with self.subTest(bad=bad), self.assertRaises((ValueError,PermissionError)): reference.validate_plan(bad)
        with self.assertRaises(PermissionError): reference.execute_tool('.', 'shell', {'command':'id'})
        with self.assertRaises(PermissionError): reference.execute_tool('.', 'write_file', {'path':'x'})
        with self.assertRaises(PermissionError): reference.execute_tool('.', [], {})
        with self.assertRaises(ValueError): reference.execute_tool('.', 'read_file', None)

    def test_workflow_uses_selected_adapter_and_reads_current_file(self):
        with tempfile.TemporaryDirectory() as td:
            (Path(td)/'x.txt').write_text('new content')
            class Adapter:
                def validate_plan(self,plan,allowed): return plan
                execute_tool=staticmethod(reference.execute_tool)
                def classify_failure(self,outcome): return 'learner-seen:'+outcome['status']
            got=workflow.run_workflow(td,{'steps':[{'tool':'read_file','arguments':{'path':'x.txt'}}]},adapter=Adapter())
            self.assertEqual(got['trace'][0]['result']['text'],'new content')
            self.assertEqual(got['trace'][0]['failure_class'],'learner-seen:ok')
        with self.assertRaises(AssertionError): self.assertEqual(got['trace'][0]['failure_class'],'success')

    def test_feature_entrypoint_fails_on_unfinished_learner_instead_of_reference_fallback(self):
        with self.assertRaises(NotImplementedError):
            workflow.run_workflow('.',{'steps':[{'tool':'read_file','arguments':{'path':'README.md'}}]})

    def test_patch_is_review_only_and_stale_or_escape_is_rejected(self):
        with tempfile.TemporaryDirectory() as td, tempfile.TemporaryDirectory() as outside:
            p=Path(td)/'code.py'; p.write_text('x = 1\n')
            proposal=reference.render_patch_proposal(td,'code.py','x = 1\n','x = 2\n')
            self.assertFalse(proposal['applied']); self.assertIn('-x = 1',proposal['diff'])
            self.assertEqual(p.read_text(),'x = 1\n')
            with self.assertRaises(ValueError): reference.render_patch_proposal(td,'code.py','old','new')
            with self.assertRaises(PermissionError): reference.render_patch_proposal(td,'../out/secret','x','y')

    def test_cancellation_and_failures_are_not_successes(self):
        cancel=threading.Event(); cancel.set()
        self.assertEqual(reference.execute_tool('.', 'read_file', {'path':'does-not-matter'},cancel=cancel)['status'],'cancelled')
        self.assertEqual(reference.classify_failure({'status':'test_failed','returncode':1}),'test_failure')
        self.assertEqual(reference.classify_failure({'status':'tool_denied'}),'authorization_denial')
        self.assertEqual(reference.classify_failure({'status':'timeout'}),'timeout')
        self.assertEqual(reference.classify_failure({'status':'ok'}),'success')
        with self.assertRaises(AssertionError): self.assertEqual(reference.classify_failure({'status':'test_failed'}),'success')
        with self.assertRaises(NotImplementedError): learner.resolve_allowed_path('.', 'x')

if __name__=='__main__': unittest.main()
