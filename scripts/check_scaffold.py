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
    if not match or tuple(map(int,match.groups())) != (87,0,87,0):
        raise SystemExit('Java setup check failed; expected 87 recognized learner TODOs and no other failure.\n'+output)
    print('PASS Java setup: compiled; 87 acceptance cases cleanly reached their expected learner TODOs.')
sql=subprocess.run(['python3',str(ROOT/'scripts/test_day19_sql.py')],text=True,capture_output=True)
if sql.returncode != 1 or '3 fixture/schema cases loaded; learner TODO' not in sql.stdout:
    raise SystemExit('SQL setup check failed; expected recognized learner TODO.\n'+sql.stdout+sql.stderr)
print('PASS SQL setup: fixture/schema contract loaded; learner query TODO is distinguished from setup failure.')
