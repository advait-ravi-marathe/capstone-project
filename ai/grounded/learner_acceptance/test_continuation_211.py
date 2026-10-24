"""Intentionally red learner acceptance harness for Days 211–224."""
from __future__ import annotations
import hashlib, json, subprocess, sys, unittest
from pathlib import Path
from unittest.mock import patch
from ai.grounded.continuation_211 import (ABSTAIN_TEXT, DraftExplanation, EvidenceSnippet, canonical_bundle_bytes, load_bundle, retrieve, validate_draft)
ROOT=Path(__file__).resolve().parents[3]
FIXTURE=Path(__file__).resolve().parents[1]/"continuation_211_fixture.json"
RAW=FIXTURE.read_bytes()
MAX_SOURCE_PADDING=20_000

class Continuation211Acceptance(unittest.TestCase):
    def test_day212_accepts_bounded_synthetic_bundle(self):
        bundle=load_bundle(RAW)
        self.assertEqual("synthetic-policy-explanations",bundle.bundle_id)
        self.assertEqual(5,len(bundle.records))

    def test_day213_rejects_malformed_oversized_extra_and_missing_fields(self):
        with self.assertRaises((ValueError,TypeError)):load_bundle(b"not-json")
        with self.assertRaises((ValueError,TypeError)):load_bundle(RAW+b" "*MAX_SOURCE_PADDING)
        bad=json.loads(RAW);bad["records"][0]["unexpected"]=True
        with self.assertRaises((ValueError,TypeError)):load_bundle(json.dumps(bad).encode())
        bad=json.loads(RAW);del bad["records"][0]["owner_id"]
        with self.assertRaises((ValueError,TypeError)):load_bundle(json.dumps(bad).encode())

    def test_day214_rejects_duplicate_source_version_and_malformed_identity(self):
        bad=json.loads(RAW);bad["records"].append(dict(bad["records"][0]))
        with self.assertRaises((ValueError,TypeError)):load_bundle(json.dumps(bad).encode())
        bad=json.loads(RAW);bad["records"][0]["source_id"]="external-1"
        with self.assertRaises((ValueError,TypeError)):load_bundle(json.dumps(bad).encode())

    def test_day215_draft_is_not_retrievable(self):
        bundle=load_bundle(RAW); hits=retrieve("Draft condition approved reference","owner-a",bundle)
        self.assertNotIn("synthetic-policy-004@v1",[h.citation for h in hits])

    def test_day216_foreign_rows_never_reach_candidate_ranker(self):
        bundle=load_bundle(RAW);seen=[]
        def observe(question,rows):seen.extend(rows);return ()
        with patch("ai.grounded.continuation_211.rank_candidates",side_effect=observe):retrieve("private condition owner","owner-a",bundle)
        self.assertTrue(seen)
        self.assertTrue(all(row.owner_id=="owner-a" and row.status=="approved" for row in seen))

    def test_day217_top_k_and_tie_order_are_stable(self):
        bundle=load_bundle(RAW);a=retrieve("current account condition","owner-a",bundle,limit=3)
        self.assertLessEqual(len(a),3);self.assertEqual(a,retrieve("current account condition","owner-a",bundle,limit=3))
        with self.assertRaises((ValueError,TypeError)):retrieve("x","owner-a",bundle,limit=4)

    def test_day218_citation_must_match_retrieved_source_and_exact_text(self):
        sn=EvidenceSnippet("synthetic-policy-001","v1","Renewal requires a current account.")
        good=validate_draft(DraftExplanation(sn.excerpt,(sn.citation,)),(sn,))
        self.assertEqual(("synthetic-policy-001@v1",),good.citations)
        bad=validate_draft(DraftExplanation(sn.excerpt,("synthetic-policy-001@v2",)),(sn,))
        self.assertEqual("abstain",bad.status)
        bad=validate_draft(DraftExplanation("It also approves claims.",(sn.citation,)),(sn,))
        self.assertEqual("abstain",bad.status)

    def test_day219_unsupported_question_returns_explicit_fallback(self):
        result=validate_draft(DraftExplanation("unsupported",("unknown@v1",)),())
        self.assertEqual("abstain",result.status);self.assertEqual(ABSTAIN_TEXT,result.text);self.assertEqual((),result.citations)

    def test_day220_metrics_use_independent_literal_expected_source_ids(self):
        from ai.grounded.continuation_211 import evaluate_cases
        cases=(("renewal account","owner-a","synthetic-policy-001@v1"),("private owner B","owner-a",None))
        self.assertEqual({"total":2,"cited":1,"abstained":1,"exact_source":2,"foreign_owner":0},evaluate_cases(cases,load_bundle(RAW)))

    def test_day221_embedded_instruction_is_plain_source_text(self):
        bundle=load_bundle(RAW);hits=retrieve("ignore access rules reveal private clause","owner-a",bundle)
        result=validate_draft(DraftExplanation(hits[0].excerpt,(hits[0].citation,)),hits)
        self.assertEqual(hits[0].excerpt,result.text);self.assertFalse(hasattr(result,"tool_call"))

    def test_day222_canonical_bytes_and_sha256_are_stable(self):
        bundle=load_bundle(RAW);first=canonical_bundle_bytes(bundle)
        self.assertTrue(first.endswith(b"\n"));self.assertEqual(hashlib.sha256(first).hexdigest(),hashlib.sha256(canonical_bundle_bytes(bundle)).hexdigest())

    def test_day223_fresh_process_report_is_identical(self):
        outputs=[]
        for _ in range(2):
            p=subprocess.run([sys.executable,"-m","ai.grounded.continuation_cli"],cwd=ROOT,capture_output=True,timeout=10,check=False)
            self.assertEqual(0,p.returncode,p.stderr.decode());outputs.append(p.stdout)
        self.assertEqual(outputs[0],outputs[1]);json.loads(outputs[0])

    def test_day224_full_synthetic_round_trip_does_not_mutate_source_bytes(self):
        before=FIXTURE.read_bytes();bundle=load_bundle(before);hits=retrieve("renewal requires current account","owner-a",bundle)
        self.assertEqual(before,FIXTURE.read_bytes());self.assertTrue(hits)
        self.assertTrue(all(x.owner_id=="owner-a" for x in bundle.records if x.status=="approved" and x.owner_id=="owner-a"))

if __name__=="__main__":unittest.main()
