#!/usr/bin/env python3
"""Check starter infrastructure while treating recognized learner TODOs separately."""
import re
import subprocess
import tempfile
from pathlib import Path
ROOT=Path(__file__).resolve().parents[1]
with tempfile.TemporaryDirectory(prefix='amazon-sprint-java-') as out:
    sources=[str(p) for p in (ROOT/'sde/src').rglob('*.java')]
    compile_result=subprocess.run(['java','-m','jdk.compiler/com.sun.tools.javac.Main','-d',out,*sources],text=True,capture_output=True)
    if compile_result.returncode:
        raise SystemExit(compile_result.stdout+compile_result.stderr)
    result=subprocess.run(['java','-cp',out,'amazon.sprint.AssignmentAcceptance'],text=True,capture_output=True)
    output=result.stdout+result.stderr
    match=re.search(r'Java SDE acceptance: (\d+) cases, (\d+) passed, (\d+) TODO, (\d+) failed',output)
    if not match:
        raise SystemExit('Java setup check failed; acceptance summary missing.\n'+output)
    cases,passed,todos,failures=map(int,match.groups())
    if failures or cases != passed+todos:
        raise SystemExit('Java setup or learner acceptance failed.\n'+output)
    print(f'PASS Java setup: compiled; {cases} acceptance cases, {passed} passed, {todos} recognized TODO, 0 failed.')
sql=subprocess.run(['python3',str(ROOT/'scripts/test_day19_sql.py')],text=True,capture_output=True)
if sql.returncode not in (0,1) or not any(marker in sql.stdout for marker in ('fixture/schema cases loaded; learner TODO','PASS SQL Day 19:')):
    raise SystemExit('SQL setup or learner acceptance failed.\n'+sql.stdout+sql.stderr)
print('PASS SQL setup: schema and fixtures loaded; query passed or stopped at its recognized learner TODO.')
