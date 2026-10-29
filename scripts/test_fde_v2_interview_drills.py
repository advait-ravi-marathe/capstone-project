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

    def test_four_java_backend_drills_fail_then_reference_passes(self):
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
        self.assertIn('3 passed',green.stdout)


if __name__=='__main__': unittest.main()
