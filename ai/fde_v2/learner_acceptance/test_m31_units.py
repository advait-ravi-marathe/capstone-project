"""M3.1 learner acceptance; unresolved default seams intentionally fail."""
import importlib, os, tempfile, unittest
from pathlib import Path
C=importlib.import_module(os.environ.get('M31_CANDIDATE','ai.fde_v2.m31.learner'))

class M31LearnerAcceptance(unittest.TestCase):
    def test_path_resolution_allows_only_files_inside_root(self):
        with tempfile.TemporaryDirectory() as td, tempfile.TemporaryDirectory() as od:
            Path(td,'ok.txt').write_text('ok'); Path(od,'secret').write_text('no')
            self.assertEqual(Path(C.resolve_allowed_path(td,'ok.txt')).read_text(),'ok')
            for p in ('../secret','/etc/passwd'):
                with self.subTest(p=p), self.assertRaises((PermissionError,ValueError)): C.resolve_allowed_path(td,p)

    def test_typed_plan_is_bounded_and_denies_unknown_tools(self):
        self.assertEqual(C.validate_plan({'steps':[{'tool':'read_file','arguments':{'path':'x'}}]},
                                         {'read_file'}),{'steps':[{'tool':'read_file','arguments':{'path':'x'}}]})
        for bad in ({'steps':[{'tool':'shell','arguments':{}}]}, {'steps':[{}]}):
            with self.subTest(bad=bad), self.assertRaises((ValueError,PermissionError)): C.validate_plan(bad,{'read_file'})

    def test_failure_classifier_does_not_drop_failed_work(self):
        self.assertEqual(C.classify_failure({'status':'test_failed','returncode':1}),'test_failure')

    def test_review_proposal_does_not_apply_patch(self):
        with tempfile.TemporaryDirectory() as td:
            Path(td,'a.py').write_text('a=1\n')
            p=C.render_patch_proposal(td,'a.py','a=1\n','a=2\n')
            self.assertFalse(p['applied']); self.assertEqual(Path(td,'a.py').read_text(),'a=1\n')

if __name__=='__main__': unittest.main()
