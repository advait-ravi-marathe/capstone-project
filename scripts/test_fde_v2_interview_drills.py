"""Expected-red starter / green instructor-reference timed interview fixtures."""
from __future__ import annotations
import os
import shutil
import subprocess
import unittest
from pathlib import Path

ROOT=Path(__file__).resolve().parents[1]
JAVA=ROOT/'fixtures/fde_v2/interview_drills/java'
FRONTEND=ROOT/'frontend'


class InterviewDrillRedGreenTests(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        if not shutil.which('java'):
            raise RuntimeError('Java source launcher (JDK compiler module) is required by the bounded drill harness')
        if not (FRONTEND/'node_modules/.bin/vitest').is_file():
            raise RuntimeError('run npm ci in frontend before the bounded frontend drill harness')
        if not shutil.which('curl') or not shutil.which('unzip') or not shutil.which('sha512sum'):
            raise RuntimeError('curl, unzip, and sha512sum are needed by the workspace-local Spring Maven wrapper')

    def test_java_backend_drills_fail_then_reference_passes(self):
        fixtures=sorted(JAVA.glob('*Drill.java'))
        self.assertEqual(len(fixtures),5)
        for starter in fixtures:
            name=starter.name
            with self.subTest(drill=name):
                red=subprocess.run(['java',str(starter)],capture_output=True,text=True,timeout=15)
                self.assertNotEqual(red.returncode,0,red.stdout)
                self.assertNotIn('compilation failed',red.stderr.lower())
                self.assertIn('Exception in thread "main"',red.stderr)
                green=subprocess.run(['java',str(JAVA/'reference'/name)],capture_output=True,text=True,timeout=15)
                self.assertEqual(green.returncode,0,green.stderr)
                self.assertIn('PASS',green.stdout)

    def test_actual_spring_mvc_validation_drill_is_red_then_reference_green(self):
        project=ROOT/'fixtures/fde_v2/interview_drills/spring-mvc'
        run=subprocess.run(['bash','verify.sh'],cwd=project,capture_output=True,text=True,timeout=900)
        output=run.stdout+'\n'+run.stderr
        if run.returncode and ('Temporary failure in name resolution' in output or 'Unknown host repo.maven.apache.org' in output):
            self.skipTest('BLOCKED: Maven Central hostname could not resolve in this execution environment')
        self.assertEqual(run.returncode,0,output)
        self.assertIn('BUILD SUCCESS',run.stdout)
        guide=(ROOT/'docs/fde-v2/interview-drills/README.md').read_text()
        self.assertIn('spring-mvc/verify.sh',guide)

    def _vitest(self, implementation):
        env=os.environ.copy()
        env['VITE_DRILL_IMPLEMENTATION']=implementation
        return subprocess.run(['npx','--no-install','vitest','run',
            'src/fde-v2/interview-drills/InterviewDrills.test.tsx'],cwd=FRONTEND,env=env,
            capture_output=True,text=True,timeout=60)

    def test_frontend_starters_fail_regression_and_reference_passes(self):
        red=self._vitest('starter')
        self.assertNotEqual(red.returncode,0,red.stdout)
        self.assertIn('failed',red.stdout.lower()+red.stderr.lower())
        green=self._vitest('reference')
        self.assertEqual(green.returncode,0,green.stdout+green.stderr)
        self.assertIn('5 passed',green.stdout)

    def test_sql_role_bank_lab_exposes_scope_and_plan_without_performance_claim(self):
        from scripts.fde_v2_query_plan_lab import run_lab
        baseline=run_lab('missing-tenant-filter')
        repaired=run_lab('none')
        self.assertGreater(baseline['cross_tenant_rows'],0)
        self.assertFalse(baseline['tenant_filter_enforced'])
        self.assertTrue(repaired['tenant_filter_enforced'])
        self.assertTrue(repaired['uses_named_index'])
        self.assertTrue(repaired['same_results_before_after_index'])
        self.assertFalse(repaired['elapsed_performance_claim'])


if __name__=='__main__': unittest.main()
