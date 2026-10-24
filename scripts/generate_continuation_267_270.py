#!/usr/bin/env python3
"""Generate synchronized final-window assignments and aggregate inventory."""
from __future__ import annotations
import json, os, tempfile
from datetime import date, timedelta
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
OUT = ROOT / "docs/reboot/continuation-267-270"
SRC = OUT / "plan-source.json"
LABELS = {
    "project": "project_implementation_tests",
    "ai": "ai_workflow_implementation_evaluation",
    "cert": "certification_labs_questions",
    "ai_study": "ai_concept_source_study",
    "cert_study": "certification_domain_study",
    "core_study": "core_systems_security_cloud_study",
    "evidence": "evidence",
}
DISPLAY = {
    "project_implementation_tests":"Project implementation/tests",
    "ai_workflow_implementation_evaluation":"AI workflow implementation/evaluation",
    "certification_labs_questions":"Certification practice questions",
    "ai_concept_source_study":"AI concept/source study",
    "certification_domain_study":"Certification domain study",
    "core_systems_security_cloud_study":"Core systems/security/cloud study",
    "evidence":"Evidence",
}

def write(path: Path, text: str) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    with tempfile.NamedTemporaryFile("w", encoding="utf-8", dir=path.parent, delete=False) as f:
        f.write(text); temp = Path(f.name)
    try: os.replace(temp, path)
    finally: temp.unlink(missing_ok=True)

def generate() -> dict:
    p = json.loads(SRC.read_text())
    days = p["days"]
    assert [d["day"] for d in days] == [267, 268, 269, 270]
    expected = {"project":150,"ai":180,"cert":130,"ai_study":90,"cert_study":30,"core_study":20}
    assert {k:sum(d[k] for d in days) for k in expected} == expected
    assert sum(d["evidence"] for d in days) == 60
    assert all(d["shared"]["mode"]=="certification_retrieval_review" for d in days)
    assert len({d["shared"]["cert_section"] for d in days})==4
    srcs = {s["key"]:s for s in p["sources"]}
    reverse={value:key for key,value in LABELS.items()}
    manifest_days=[]; cards=[]; source_out=[]
    start=date.fromisoformat(p["start_date"])
    for i,d in enumerate(days):
        day=d["day"]; dt=(start+timedelta(days=i)).isoformat()
        assert d["date"] == dt
        alloc={LABELS[k]:d[k] for k in ("project","ai","cert","ai_study","cert_study","core_study")}
        alloc[LABELS["evidence"]]=d["evidence"]
        assert sum(alloc.values())==165
        assert sum(d[k] for k in ("project","ai","cert"))==115
        assert sum(d[k] for k in ("ai_study","cert_study","core_study"))==35
        assert set(d["activities"])==set(LABELS.values()),(set(d["activities"]),set(LABELS.values()))
        for category,activity in d["activities"].items():
            activity["minutes"]=d[reverse[category]]
            assert all(activity.get(k) for k in ("task","command","evidence")),(day,category)
            assert activity.get("minutes",-1)>=0,(day,category)
            assert all(k in srcs for k in activity.get("source_keys",[])),(day,category)
        assert sum(a["minutes"] for a in d["activities"].values())==165
        refs=[]
        for key in d["source_keys"]:
            s=srcs[key]
            if s not in refs: refs.append(s)
        activities={}
        for key,a in d["activities"].items():
            links=[f"{srcs[s]['title']} — {', '.join(a['sections'].get(s,srcs[s]['sections']))} ({srcs[s]['url']})" for s in a["source_keys"]]
            source_markdown=[]
            for path in a["paths"]:
                candidate=ROOT/path.split(" ",1)[0]
                if candidate.is_file():
                    relative=os.path.relpath(candidate,OUT/"contracts")
                    source_markdown.append(f"[{path}]({relative})")
                else:
                    source_markdown.append(f"`{path}`")
            source_markdown += [
                f"[{srcs[s]['title']} — {', '.join(a['sections'].get(s,srcs[s]['sections']))}]({srcs[s]['url']})"
                for s in a["source_keys"]]
            activities[key]={"minutes":a["minutes"],"task":a["task"],"source_paths":a["paths"],
                "source_keys":a["source_keys"],"source_sections":a["sections"],"command":a["command"],
                "evidence":a["evidence"],"source":"; ".join(a["paths"]+links) or "No external source; see named task and repository evidence.",
                "source_markdown":"; ".join(source_markdown) or "No external source; see the task's repository evidence."}
        shared={"minutes":45,"within_engineering_minutes":False,**d["shared"]}
        if shared["mode"]=="system_design_question_session":
            shared["source"]="docs/reboot/interview/question-bank.md"
            shared["source_markdown"]="[Separate interview question bank](../../interview/question-bank.md)"
            shared["allocation_minutes"]={"requirements":5,"constraints":5,"architecture_or_review":15,"failure_modes":10,"tradeoffs_and_next_step":10}
        elif shared["mode"]=="certification_retrieval_review":
            shared["source_keys"]=[shared.pop("cert_source")]
            section=shared.pop("cert_section")
            ss=srcs[shared["source_keys"][0]]
            shared["source"]=f"{ss['title']} — {section} ({ss['url']})"
            shared["source_markdown"]=f"[{ss['title']} — {section}]({ss['url']})"
        else:
            shared["source"]="actual current gate outputs, learner evidence records and question-bank notes"
            shared["source_markdown"]="[Current continuation gate](../../../../scripts/check_continuation_253_entry_gate.py) and actual evidence records"
        arch=("Assistant-supplied closeout design: no capstone feature expansion. Read existing evidence gates and accepted local checks first; "
              "hash exact portfolio bytes against the checked-out 40-hex source commit; assess certification evidence against the rechecked official guide. "
              "Classify missing evidence as carryover, never as success. The learner implements/tests the offline receipt and readiness seams. "
              "This produces neither learner completion, an AWS exam pass, production/customer ownership, nor Staff/Principal/FDE readiness.")
        row={"day":day,"date":dt,"title":d["title"],"learner_seam":d["seam"],"signature":", ".join(d["files"]),
             "selectors":list(dict.fromkeys(a["command"] for a in activities.values())),"depends_on_days":[day-1] if day>267 else [],"recovery_day":day==267,
             "study_refs":[srcs[k]["url"] for k in d["source_keys"]],"study_guidance":refs,
             "engineering_architecture":{"decision":arch,"note_ref":f"assistant-architecture-notes.md#day-{day}",
                "mode":"assistant_supplied_capstone_architecture; learner_implements_and_tests","capstone_proposal_required":False,
                "system_design_scope":"separate_interview_question_bank_only"},
             "budget":{"engineering_minutes":165,"study":35,"practice_and_tests":115,"evidence":15,
                       "shared_interview_or_remaining_reviews_minutes":45,"total":210,"workstream_minutes":alloc},
             "workstream_minutes":alloc,"task_minutes":activities,"shared_interview_block":shared,
             "learner_status":"unobserved_not_implied_by_scaffold","separate_learning_repository":p["separate_learning_repository"],"task":d}
        manifest_days.append(row)
        cards.append({"day_number":day,"date":dt,"title":d["title"],"learner_seam":d["seam"],
            "signature":row["signature"],"selectors":row["selectors"],"prerequisite_days":row["depends_on_days"],
            "recovery_day":row["recovery_day"],"engineering":{"minutes":165,"allocation":alloc},
            "budget_minutes":{"engineering":165,"shared_interview_or_remaining_reviews":45,"total":210},
            "activities":activities,"study_guidance":refs,"shared_interview_block":shared,
            "assistant_architecture_note":row["engineering_architecture"],"status":"unobserved_not_implied_by_scaffold",
            "separate_learning_repository":p["separate_learning_repository"]})
        lines=[f'<a id="day-{day}"></a>',f'# Day {day}: {d["title"]}',"",f"Date: {dt}","",
            f"**Assistant-supplied architecture — what and why:** {arch}","","## Engineering tasks",""]
        for k,a in activities.items():
            lines += [f"### {DISPLAY[k]} — {a['minutes']} minutes","",
                f"**Task:** {a['task']}",f"**Source:** {a['source_markdown']}",f"**Command / source action:** {a['command']}",
                f"**Evidence:** {a['evidence']}",""]
        lines += ["## Shared block — 45 minutes (outside engineering)","",f"**{shared['mode']}:** {shared['task']}",
            f"**Source:** {shared['source_markdown']}","","## Gate, status and scope","",
            f"Before any gated learner workflow, run `{p['gate_command']}`. It evaluates the recorded Day 210/238 evidence and assigned Day 252 closeout dynamically. "
            "Current missing or failed evidence becomes an explicit recovery item; it is not a permanent failure and the checker must permit valid future evidence. "
            "Run only selectors whose prerequisites are eligible. An unimplemented learner TODO is an expected learner-task outcome, not CI/infrastructure failure or completion. "
            "The closeout inventory records assignments only; it must not create learner evidence. The exact 40-hex source commit is distinct from each 64-hex artifact digest. "
            f"AWS study is offline and guide-version checked at study time; no exam booking, paid call, credential or cloud action. System design remains only in separate question-bank sessions. Algorithm practice remains in the separate [DSA preparation repository]({p['separate_learning_repository']['algorithm_practice']}); this repo adds no DSA scripts or tracker. "
            "Local portfolio work is not professional production ownership or a Staff/Principal/FDE readiness claim.","",
            "**Learner status:** unobserved_not_implied_by_scaffold",""]
        write(OUT/f"contracts/day-{day}.md","\n".join(lines))
    work={LABELS[k]:v for k,v in expected.items()}; work[LABELS["evidence"]]=60
    base={"title":p["title"],"status":p["status"],"branch":p["branch"],"base_commit":p["base_commit"],
          "date_range":{"start":p["start_date"],"end":p["end_date"]},"scope":p["scope"],"gate":p["gate"],
          "engineering_minutes_per_day":165,"shared_minutes_per_day":45,"separate_learning_repository":p["separate_learning_repository"],"engineering_minutes_total":660,
          "engineering_allocation":work,"shared_allocation_reconciliation":p["shared_allocation_reconciliation"],
          "days":manifest_days,"sources":p["sources"]}
    write(OUT/"manifest.json",json.dumps(base,indent=2,ensure_ascii=False)+"\n")
    transfer={**base,"budget":{"engineering_minutes_per_day":165,"allocation":{"study":35,"practice_and_tests":115,"evidence":15},
        "shared_interview_or_remaining_reviews_minutes":45,"engineering_minutes_total":660,"window_workstreams":work},
        "days":manifest_days}
    write(OUT/"days-267-270-transfer-plan.json",json.dumps(transfer,indent=2,ensure_ascii=False)+"\n")
    owner={"title":p["title"],"status":p["status"],"branch":p["branch"],"base_commit":p["base_commit"],
           "separate_learning_repository":p["separate_learning_repository"],
           "board_writes":False,"story_count":4,"shared_allocation_reconciliation":p["shared_allocation_reconciliation"],"cards":cards}
    write(OUT/"scrum-owner-manifest.json",json.dumps(owner,indent=2,ensure_ascii=False)+"\n")
    write(OUT/"verified-sources.json",json.dumps({"verified_at":p["sources_verified_at"],"recheck_before_study":True,
         "scope":"Official source titles and headings checked on 2026-10-24; recheck on the assigned 2027 study date, especially the Developer Associate guide version. Current version labels are planning references, not future guarantees. No cloud use, paid practice, exam booking or credential claim.","sources":p["sources"]},indent=2,ensure_ascii=False)+"\n")
    notes=[]
    for d in days: notes.append(f'<a id="day-{d["day"]}"></a>\n## Day {d["day"]} — {d["title"]}\n\nAssistant-selected closeout path: retain the actual evidence state; do not broaden features or infer readiness.\n')
    write(OUT/"assistant-architecture-notes.md","# Assistant-supplied closeout architecture\n\nThe closeout is a local evidence review: actual gate outputs, exact source and artifact digests, and official-guide-aligned certification evidence. It makes no learner completion, exam, customer, production-ownership or seniority claim. Learner implements the bounded offline receipt/readiness seams; no design proposal is required.\n\n"+"\n".join(notes))
    write(OUT/"README.md",f"# Days 267–270: evidence-based closeout\n\nDates: 17–20 July 2027. This is the proposal's final four-day closeout, not feature expansion. It re-runs only accepted selectors whose prerequisites are eligible, assembles a local reproducibility receipt from actual file bytes, checks official certification study evidence, and records the next queue.\n\nThe current Day 210/238 prerequisites and assigned Day 252 record are absent in the inspected repository. Run `{p['gate_command']}` when working; preserve its dynamic result and exact recovery selectors. No completion evidence is created by these assignments.\n\nEngineering: 660 minutes. Per workstream: project/tests 150; AI workflow/evaluation 180; certification practice 130; AI concept/source study 90; certification domain study 30; core systems/security/cloud study 20; evidence 60. Daily engineering is 165 minutes plus one shared 45-minute block.\n\nThe previously published Days 197–266 manifests allocate shared blocks as 27 interview-question sessions, 12 certification reviews and 31 unfinished reviews, versus the proposal split of 24, 24 and 26. These prior reviewed assignments remain unchanged. To reduce the certification-review shortfall, all four closeout shared blocks are official-guide retrieval; this leaves cumulative shared allocation at 27/16/31 sessions. Each block is still exactly 45 minutes, and system-design practice remains confined to the separate question bank.\n\nThe two confirmed targets remain AWS Certified Solutions Architect – Associate and AWS Certified Developer – Associate. The [official SAA-C03 guide](https://docs.aws.amazon.com/aws-certification/latest/solutions-architect-associate-03/solutions-architect-associate-03.html) and [currently linked Developer Associate guide](https://docs.aws.amazon.com/aws-certification/latest/developer-associate-02/developer-associate-02.html) were checked 24 October 2026; the latter identifies DVA-C02 at this check. Recheck exact guide title/version and domain headings in July 2027. If AWS has changed a guide, update that credential's evidence and carry unfinished coverage forward. The internal proposal threshold requires two distinct timed official practice assessments at 80% or higher, reviewed misses, no unstudied domain, and a full-length official practice exam when actually available without purchase; supplied official domain diagnostics should be at least 70%. Missing or unavailable evidence stays not-ready. The result only supports a separate booking review; it never means an exam pass or authorizes a booking. No paid practice, exam, or lab is assigned.\n\n**Application checkpoints are conditional on verified learner-owned evidence:**\n\n- **SDE II knowledge/application comparison:** compare actual accepted backend/API/data work, relevant tests, debugging notes and safe local operation with current postings. If those outputs are absent, keep the checkpoint unverified.\n- **Senior/SDE III knowledge/application comparison:** require a reproducible integrated workflow, permission/failure/replay behavior, evaluation results and a truthful handoff before comparing roles. This still does not prove workplace ownership, incident duty, mentoring or cross-team delivery.\n- **Staff/Principal/FDE:** no readiness claim from this solo portfolio. Continue only when real external evidence demonstrates repeated service or customer ownership, production incident accountability, adoption/impact, design-review influence and mentorship.\n\nAlgorithm practice remains only in the separate [DSA preparation repository]({p['separate_learning_repository']['algorithm_practice']}); this repo does not add DSA exercises or trackers. Portfolio evidence is local/synthetic only. Calendar completion does not satisfy any checkpoint.\n\nGenerate with `python3 scripts/generate_continuation_267_270.py`; validate with `python3 -m unittest scripts.test_continuation_267_270`. The aggregate assignment inventory is generated only after all four contracts and allocations validate; it never records learner completion.\n")
    # Do not advance the assignment inventory unless every generated record is coherent.
    for row in manifest_days:
        assert set(row["task_minutes"])==set(work)
        assert row["workstream_minutes"]=={k:v["minutes"] for k,v in row["task_minutes"].items()}
        assert sum(row["task_minutes"][k]["minutes"] for k in work)==165
        contract_path=OUT/f"contracts/day-{row['day']}.md"
        contract=contract_path.read_text()
        assert f'id="day-{row["day"]}"' in contract and "Learner status" in contract
        assert "Assistant-supplied architecture" in contract
        assert f"certification-review.schema.json" in contract or row["day"]!=269
        for category,task in row["task_minutes"].items():
            assert task["task"].strip() and task["command"].strip() and task["evidence"].strip()
            assert task["minutes"]>=0
    assert (OUT/"certification-review.schema.json").is_file()
    json.loads((OUT/"certification-review.schema.json").read_text())
    # Validate all previously reviewed window inventories before advertising 270.
    prior_ranges=((197,210),(211,224),(225,238),(239,252),(253,266))
    prior=[]
    for first,last in prior_ranges:
        path=ROOT/f"docs/reboot/continuation-{first}-{last}/manifest.json"
        obj=json.loads(path.read_text())
        assert [x["day"] for x in obj["days"]]==list(range(first,last+1))
        prior.append({"start_day":first,"end_day":last,"manifest":str(path.relative_to(ROOT)),"day_count":last-first+1})
    inventory={"title":"Continuation assignment inventory, Days 197–270","status":"planned_assignments_only_not_completion",
        "branch":p["branch"],"through_day":270,"learner_completion_awarded":False,
        "windows":prior+[ {"start_day":267,"end_day":270,"manifest":"docs/reboot/continuation-267-270/manifest.json","day_count":4} ]}
    write(ROOT/"docs/reboot/continuation-197-270-inventory.json",json.dumps(inventory,indent=2)+"\n")
    return base

if __name__=="__main__":
    result=generate(); print(f"generated {len(result['days'])} closeout days; engineering={result['engineering_minutes_total']} min")
