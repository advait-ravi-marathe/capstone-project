#!/usr/bin/env python3
"""Run saved-day instructor checks for days 021–040, separate from learner TODOs."""
from __future__ import annotations
import hashlib, json, os, re, shlex, subprocess, sys, tempfile
from datetime import datetime, timezone
from pathlib import Path

ROOT=Path(__file__).resolve().parents[1]
SOURCE=ROOT/"docs/source-inputs/2026-10-14/curriculum/coverstory-final-saved-contracts-594-days.json"
OUT=ROOT/"docs/evidence/coverstory/phase1-days-021-040"
MAVEN=shlex.split(os.environ.get("COVERSTORY_MAVEN","mvn"))
PY=sys.executable

# One exact saved-primary instructor oracle per day; this is not a learner-pass matrix.
DAYS=[
 (21,"CAP-167","java",["CAP167InstructorInfrastructureTest"],[]),
 (22,"CAP-64","java",["CAP64InstructorInfrastructureTest"],[]),
 (23,"CAP-65","python",["ai.tests.test_coverstory_cap65_infrastructure"],[]),
 (24,"CAP-66","java",["CAP66InstructorInfrastructureTest"],[]),
 (25,"CAP-67","python",["ai.tests.test_coverstory_cap67_infrastructure"],[]),
 (26,"CAP-68","java",["capstone.coverstory.shortlist.CAP68InstructorInfrastructureTest"],[]),
 (27,"CAP-69","python",["ai.tests.test_coverstory_cap69_infrastructure"],[]),
 (28,"CAP-168","java",["capstone.coverstory.shortlist.CAP168InstructorInfrastructureTest","OwnedShortlistPersistenceIntegrationTest"],[]),
 (29,"CAP-70","java",["CAP70CryptoInfrastructureTest"],[]),
 (30,"CAP-71","python",["ai.tests.test_coverstory_cap71_infrastructure"],[]),
 (31,"CAP-72","python",["ai.tests.test_coverstory_cap72_infrastructure"],[]),
 (32,"CAP-73","python",["ai.tests.test_coverstory_cap73_infrastructure"],[]),
 (33,"CAP-74","java",["CAP74InstructorInfrastructureTest"],[]),
 (34,"CAP-75","python",["ai.tests.test_coverstory_cap75_infrastructure"],[]),
 (35,"CAP-169","java",["CAP169InstructorInfrastructureTest"],[]),
 (36,"CAP-76","java",["CAP76InstructorInfrastructureTest"],[]),
 (37,"CAP-77","python",["ai.tests.test_coverstory_cap77_infrastructure"],[]),
 (38,"CAP-78","java",["CAP78InstructorInfrastructureTest"],[]),
 (39,"CAP-79","python",["ai.tests.test_coverstory_cap79_infrastructure"],[]),
 (40,"CAP-80","java",["CAP80InstructorInfrastructureTest"],[]),
]

def digest(p:Path)->str:return hashlib.sha256(p.read_bytes()).hexdigest()
def execute(cmd:list[str],log:Path,cwd:Path)->dict:
 r=subprocess.run(cmd,cwd=cwd,text=True,stdout=subprocess.PIPE,stderr=subprocess.STDOUT)
 log.write_text(r.stdout,encoding="utf-8")
 counts=re.findall(r"Tests run: (\d+), Failures: (\d+), Errors: (\d+), Skipped: (\d+).*? -- in ",r.stdout)
 py=re.search(r"Ran (\d+) tests?",r.stdout)
 tests={"tests":sum(int(x[0]) for x in counts),"failures":sum(int(x[1]) for x in counts),"errors":sum(int(x[2]) for x in counts),"skipped":sum(int(x[3]) for x in counts)} if counts else ({"tests":int(py.group(1))} if py else None)
 return {"command":shlex.join(cmd),"exit_code":r.returncode,"status":"PASS" if r.returncode==0 else "FAIL","test_counts":tests,"raw_log_sha256":digest(log),"raw_log_local_only":True}

def main()->int:
 cards=json.loads(SOURCE.read_text())["cards"]
 byid={c["id"]:c for c in cards}
 raw_dir=Path(tempfile.mkdtemp(prefix="coverstory-days-021-040-"))
 report={"title":"CoverStory Phase 1 Days 021–040 contract audit","recorded_at_utc":datetime.now(timezone.utc).isoformat(),"canonical_curriculum_sha256":digest(SOURCE),"readiness_basis":"Instructor command and fixture evidence establish harness setup only. Full saved-contract readiness additionally requires the specific live API/UI/data boundary and separate timebox rehearsal.","raw_output_directory_local_only":str(raw_dir),"learner_assignments":"NOT_RUN_AND_NOT_MARKED_COMPLETE","days":[]}
 for day,card,kind,names,fixtures in DAYS:
  command=(MAVEN+["-Dtest="+",".join(names),"test"]) if kind=="java" else [PY,"-m","unittest",names[0]]
  check=execute(command,raw_dir/f"day-{day:03}-instructor.log",ROOT)
  fixture_paths=fixtures or [f"fixtures/coverstory/{card}/synthetic-v1.json"]
  fixture_records=[{"path":p,"exists":(ROOT/p).is_file(),"sha256":digest(ROOT/p) if (ROOT/p).is_file() else None} for p in fixture_paths]
  source=byid[card]
  harness_ready=check["status"]=="PASS" and all(f["exists"] for f in fixture_records)
  report["days"].append({"day":day,"date":source["date"],"primary":card,"title":source["title"],"instructor_harness_status":"PASS" if harness_ready else "BLOCKED","contract_readiness":"PARTIAL" if harness_ready else "BLOCKED","checks":[check],"fixtures":fixture_records,"learner_assignment_complete":False,"learner_acceptance":"NOT_RUN_BY_THIS_HARNESS","boundary":"Fixture/oracle setup only. A green harness is not full saved-contract readiness: separately exercise the exact product/API/UI/data boundary and timebox before READY."})
 report["summary"]={"days_total":20,"harness_pass":sum(d["instructor_harness_status"]=="PASS" for d in report["days"]),"contract_ready":sum(d["contract_readiness"]=="READY" for d in report["days"]),"partial_oracle_only":sum(d["contract_readiness"]=="PARTIAL" for d in report["days"]),"blocked":sum(d["contract_readiness"]=="BLOCKED" for d in report["days"]),"learner_assignments_completed":0,"instructor_tests_run":sum((d["checks"][0].get("test_counts") or {}).get("tests",0) for d in report["days"])}
 OUT.mkdir(parents=True,exist_ok=True)
 (OUT/"readiness.json").write_text(json.dumps(report,indent=2)+"\n",encoding="utf-8")
 lines=["# CoverStory Phase 1 Days 021–040 contract audit","",f"Instructor harnesses: {report['summary']['harness_pass']}/20 passed across {report['summary']['instructor_tests_run']} tests. Full contract readiness: {report['summary']['contract_ready']} READY, {report['summary']['partial_oracle_only']} PARTIAL, {report['summary']['blocked']} BLOCKED. A fixture oracle is not a full product-path pass. Learner acceptance suites were not run.",""]
 for d in report["days"]:
  c=d["checks"][0]; lines += [f"## Day {d['day']:03} · {d['date']} · {d['primary']} — {d['contract_readiness']}","",f"`{c['command']}` — {c['status']}; tests={c['test_counts']}",f"Output SHA-256: `{c['raw_log_sha256']}` (raw output remains local-only).","Learner acceptance: not run by this harness. The exact saved flow still needs its product/API/UI/data boundary and separate timebox evidence before READY.",""]
 (OUT/"README.md").write_text("\n".join(lines),encoding="utf-8")
 print(json.dumps(report["summary"],indent=2));return 0 if report["summary"]["blocked"]==0 else 1
if __name__=="__main__":raise SystemExit(main())
