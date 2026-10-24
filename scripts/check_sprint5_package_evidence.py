#!/usr/bin/env python3
"""Reject missing/stale Sprint 5 JAR or receipt before packaged browser acceptance."""
import hashlib, json, subprocess, sys
from pathlib import Path
ROOT=Path(__file__).resolve().parents[1]
JAR=ROOT/'target/capstone-starter-1.0-SNAPSHOT-sprint5.jar'
RECEIPT=ROOT/'target/sprint5-demo/package-evidence.json'
def sha(path):
 h=hashlib.sha256()
 with path.open('rb') as f:
  for chunk in iter(lambda:f.read(1024*1024),b''): h.update(chunk)
 return h.hexdigest()
def run(*args): return subprocess.check_output(args,cwd=ROOT,text=True).strip()
if not JAR.is_file() or not RECEIPT.is_file(): raise SystemExit('Fresh Sprint 5 JAR and receipt required; run scripts/package-sprint5-demo.sh first.')
r=json.loads(RECEIPT.read_text())
assert r['sourceCommit']==run('git','rev-parse','HEAD'), 'receipt commit does not match current HEAD'
assert r['sourceContentSha256']==run('python3','scripts/sprint5-source-fingerprint.py'), 'current source differs from receipt fingerprint'
assert r['artifact']=='target/capstone-starter-1.0-SNAPSHOT-sprint5.jar'
assert r['artifactSha256']==sha(JAR), 'JAR digest differs from receipt'
assert r['manifestSha256'], 'missing manifest digest'
manifest=subprocess.check_output(['unzip','-p',str(JAR),'META-INF/MANIFEST.MF'],cwd=ROOT)
from check_sprint5_manifest_hash import builder_manifest_hash
assert builder_manifest_hash(manifest)==r['manifestSha256'], 'JAR manifest digest differs from receipt'
assert r['workingTreeStatus'] in ('clean','dirty') and r['workingTreeStatusSha256']
assert r['startClass']=='assignments.sprint5.Sprint5LocalApplication'
print('fresh Sprint 5 package receipt matches current HEAD, source fingerprint and JAR SHA-256')
