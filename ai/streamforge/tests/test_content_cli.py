"""Actual-process acceptance seams; no canned report is supplied."""
from __future__ import annotations
import csv, hashlib, io, json, os, shutil, subprocess, sys, tempfile, unittest
from pathlib import Path
from ai.streamforge.tests.golden import install_fixture

def _canonical(value, *, trailing=True):
    raw=json.dumps(value,ensure_ascii=False,allow_nan=False,sort_keys=True,separators=(",",":")).encode("utf-8")
    return raw+(b"\n" if trailing else b"")

def _sha(value, *, trailing=True):
    return hashlib.sha256(_canonical(value,trailing=trailing)).hexdigest()

def _expected_complete_report(input_bytes):
    # Independent literal state and arithmetic from the published hand oracle.
    protocol={"id":"streamforge-cutoff-genre-profile-v1","k":3,"genres":["animation","comedy","documentary","drama","science-fiction"],"window_start_utc":"2027-01-01T00:00:00Z","cutoff_utc":"2027-01-20T00:00:00Z","window_end_utc":"2027-02-01T00:00:00Z","training_signal":"distinct_positive_train_viewer_media","normalization":"l1_exact_fraction","similarity":"dot_product","tie_break":"media_id_ascending","fallback_rule":"cold_or_zero_profile_to_current_global","availability_rule":"from_lte_cutoff_lt_to","evaluation_role":"development_comparison_on_previously_inspected_synthetic_holdout","max_scored_pairs":2500}
    features={"a":[0,0,0,1,0],"b":[0,0,0,1,0],"c":[0,1,0,0,0],"d":[0,0,1,0,0],"e":[1,0,0,0,0],"f":[0,0,0,0,1]}
    training_viewers=["v1","v2","v3","v4","v5","z"]
    profiles={"v1":{"genre_counts":[0,2,0,1,0],"weights":["0/1","2/3","0/1","1/3","0/1"],"positive_train_pairs":3},"v2":{"genre_counts":[1,0,1,1,0],"weights":["1/3","0/1","1/3","1/3","0/1"],"positive_train_pairs":3},"v3":{"genre_counts":[0,0,1,0,0],"weights":["0/1","0/1","1/1","0/1","0/1"],"positive_train_pairs":1},"v4":{"genre_counts":[0,0,1,0,0],"weights":["0/1","0/1","1/1","0/1","0/1"],"positive_train_pairs":1},"v5":{"genre_counts":[1,0,0,0,0],"weights":["1/1","0/1","0/1","0/1","0/1"],"positive_train_pairs":1},"z":{"genre_counts":[0,0,0,0,0],"weights":["0/1","0/1","0/1","0/1","0/1"],"positive_train_pairs":0}}
    global_pop={"support":{"a":1,"b":1,"c":1,"d":3,"e":2,"f":0},"full_order":["d","e","a","b","c","f"],"recommendation_ids":["d","e","a"],"fallback_status":"popularity_ranked"}
    payload={"protocol":protocol,"candidate_ids":["a","b","c","d","e","f"],"candidate_features":features,"training_viewer_ids":training_viewers,"profiles":profiles,"global_popularity":global_pop}
    state_fp=_sha(payload)
    rows=[
        ("n",False,0,"global_cold_start",["0/1"]*6,["d","e","a","b","c","f"],["d","e","a"]),
        ("v1",True,3,"genre_profile",["1/3","1/3","2/3","0/1","0/1","0/1"],["c","a","b","d","e","f"],["c","a","b"]),
        ("v2",True,3,"genre_profile",["1/3","1/3","0/1","1/3","1/3","0/1"],["a","b","d","e","c","f"],["a","b","d"]),
        ("z",True,0,"global_no_positive_profile",["0/1"]*6,["d","e","a","b","c","f"],["d","e","a"]),
    ]
    queries=[]
    for viewer,seen,pairs,mode,scores,order,recs in rows:
        projection={"viewer_id":viewer,"training_seen":seen,"positive_train_pairs":pairs,"mode":mode,"scores_by_media":dict(zip(payload["candidate_ids"],scores)),"full_order":order,"recommendation_ids":recs}
        to_hash=dict(projection,state_fingerprint=state_fp)
        queries.append(dict(projection,state_fingerprint=state_fp,fingerprint=_sha(to_hash)))
    train=[{"event_id":eid,"viewer_id":v,"media_id":m,"occurred_at_utc":"2027-01-10T00:00:00Z","watch_seconds":w} for eid,v,m,w in [("t01","v1","a",10),("t02","v1","a",20),("t03","v1","x",10),("t04","v1","c",10),("t05","v2","b",10),("t06","v2","d",10),("t07","v2","e",10),("t08","v3","d",10),("t09","v4","d",10),("t10","v5","e",10),("t11","z","f",0)]]
    training_summary={"status":"available","event_count":11,"distinct_viewer_count":6,"media_event_counts":{"a":2,"x":1,"c":1,"b":1,"d":3,"e":2,"f":1},"media_watch_seconds":{"a":30,"x":10,"c":10,"b":10,"d":30,"e":20,"f":0}}
    train_hash_payload={"schema_version":1,"window_start_utc":protocol["window_start_utc"],"cutoff_utc":protocol["cutoff_utc"],"train":train,"training":training_summary}
    training_summary["content_sha256"]=_sha(train_hash_payload,trailing=False)
    source_hashes={name:hashlib.sha256(raw).hexdigest() for name,raw in input_bytes.items()}
    lineage={"schema_version":1,"data_version":1,"fixture_id":"basic","window_start_utc":protocol["window_start_utc"],"cutoff_utc":protocol["cutoff_utc"],"window_end_utc":protocol["window_end_utc"],"preparation_status":{"training":"available","holdout":"available"},"training":training_summary,"holdout_diagnostics":{"status":"available","viewer_item_seen_in_train":5,"viewer_seen_item_unseen":1,"viewer_unseen_item_seen":2,"viewer_item_unseen":0},"source_sha256":source_hashes,"duplicate_events_collapsed":0,"manifest_sha256":source_hashes["manifest.json"],"catalog_count":8,"partition_counts":{"train":11,"holdout":8,"outside_window":0}}
    counts={"all_holdout_viewers":5,"evaluated_viewers":4,"zero_positive_viewers":1,"positive_viewer_media_pairs":6,"candidate_unreachable_positive_pairs":1,"training_viewer_count":6,"cold_start_evaluated_viewers":1,"known_zero_profile_evaluated_viewers":1,"positive_profile_evaluated_viewers":2,"candidate_count":6,"returned_ids_total":12}
    per_viewer=[{"viewer_id":"n","positive_target_ids":["a"],"hit_count":1,"positive_target_count":1,"candidate_unreachable_positive_pairs":0,"precision_at_3":"1/3","recall_at_3":"1/1","status":"available"},{"viewer_id":"v1","positive_target_ids":["c","y"],"hit_count":1,"positive_target_count":2,"candidate_unreachable_positive_pairs":1,"precision_at_3":"1/3","recall_at_3":"1/2","status":"available"},{"viewer_id":"v2","positive_target_ids":["d","e"],"hit_count":1,"positive_target_count":2,"candidate_unreachable_positive_pairs":0,"precision_at_3":"1/3","recall_at_3":"1/2","status":"available"},{"viewer_id":"z","positive_target_ids":["b"],"hit_count":0,"positive_target_count":1,"candidate_unreachable_positive_pairs":0,"precision_at_3":"0/1","recall_at_3":"0/1","status":"available"}]
    profile_eval={"status":"available","precision_at_3":"1/4","recall_at_3":"1/2","counts":counts,"selection_mode_counts":{"genre_profile":2,"global_cold_start":1,"global_no_positive_profile":1,"no_candidates":0},"per_viewer_metrics":per_viewer}
    global_eval={"recommendation_ids":["d","e","a"],"status":"available","precision_at_3":"1/4","recall_at_3":"1/2","counts":{"all_holdout_viewers":5,"evaluated_viewers":4,"zero_positive_viewers":1,"positive_viewer_media_pairs":6,"candidate_unreachable_positive_pairs":1,"training_viewer_count":6,"cold_start_evaluated_viewers":1,"candidate_count":6,"returned_count":3}}
    limits=["The profile is an arithmetic feature aggregation, not an optimizer-trained model.","The previously inspected holdout is development evaluation, not an untouched final test.","Synthetic observed-positive overlap does not establish preference, unbiased quality, causal lift, fairness, or business success.","Single-genre features are deliberately coarse and may miss interests; scores are not calibrated preference probabilities.","Catalog schedule is assumed known at cutoff; no historical catalog snapshot is established.","No real dataset, new dependency, model optimizer, cloud resource, service, paid enrollment, or new registered fixture dataset.","Content hashes identify bytes/semantic projections, not authenticity, privacy, rights or quality.","Lists are static at cutoff; no per-event availability reranking."]
    return {"schema_version":1,"protocol":protocol,"predictor":dict(payload,fingerprint=state_fp),"queries":queries,"evaluations":{"profile":profile_eval,"global_popularity":global_eval,"signed_deltas":{"precision_at_3":"0/1","recall_at_3":"0/1"},"status":"available"},"preparation":lineage,"limits":limits}

class ContentCliHarness(unittest.TestCase):
    def setUp(self):
        self.tmp=tempfile.TemporaryDirectory(prefix="streamforge-content-")
        self.root=Path(self.tmp.name)
        repo=Path(__file__).resolve().parents[3]
        shutil.copytree(repo/"ai"/"streamforge",self.root/"ai"/"streamforge",ignore=shutil.ignore_patterns("__pycache__","*.pyc"))
        self.fixture=self.root/"ai"/"streamforge"/"fixtures"/"basic"
        install_fixture(self.fixture)
        self._install_oracle_fixture()
        self.env=dict(os.environ);self.env["PYTHONPATH"]=str(self.root)
        self.trace=self.root/"prepare-calls.txt"
        guard=self.root/"traceguard";guard.mkdir()
        (guard/"sitecustomize.py").write_text(
            "import os, sys\n"
            "sys.path.insert(0, os.environ['CONTENT_COPY_ROOT'])\n"
            "import ai.streamforge.evaluation_adapter as adapter\n"
            "_original = adapter.prepare_dataset\n"
            "def _counted(name):\n"
            "    with open(os.environ['CONTENT_TRACE'], 'a', encoding='utf-8') as out: out.write(name+'\\n')\n"
            "    return _original(name)\n"
            "adapter.prepare_dataset = _counted\n", encoding="utf-8")
        self.env["CONTENT_COPY_ROOT"]=str(self.root)
        self.env["CONTENT_TRACE"]=str(self.trace)
        self.env["PYTHONPATH"]=str(guard)+os.pathsep+str(self.root)
    def tearDown(self): self.tmp.cleanup()
    def _install_oracle_fixture(self):
        catalog=[("a","drama"),("b","drama"),("c","comedy"),("d","documentary"),("e","animation"),("f","science-fiction"),("x","comedy"),("y","science-fiction")]
        start="2027-01-01T00:00:00Z"; end="2027-03-01T00:00:00Z"
        rows=[]
        for mid,genre in catalog:
            available_from="2027-01-21T00:00:00Z" if mid=="y" else start
            available_to="2027-01-20T00:00:00Z" if mid=="x" else end
            rows.append([mid,"Synthetic "+mid,genre,"100",available_from,available_to])
        def csv_bytes(header,values):
            stream=io.StringIO(newline="");writer=csv.writer(stream,lineterminator="\n");writer.writerow(header);writer.writerows(values);return stream.getvalue().encode()
        (self.fixture/"catalog.csv").write_bytes(csv_bytes(["media_id","title","genre","duration_s","available_from_utc","available_to_utc"],rows))
        train=[("t01","v1","a",10),("t02","v1","a",20),("t03","v1","x",10),("t04","v1","c",10),("t05","v2","b",10),("t06","v2","d",10),("t07","v2","e",10),("t08","v3","d",10),("t09","v4","d",10),("t10","v5","e",10),("t11","z","f",0)]
        hold=[("h01","v1","c",10),("h02","v1","c",20),("h03","v1","y",10),("h04","v2","d",10),("h05","v2","e",10),("h06","n","a",10),("h07","z","b",10),("h08","q","d",0)]
        event_rows=[]
        for eid,viewer,mid,watch in train: event_rows.append([eid,viewer,mid,"2027-01-10T00:00:00Z",str(watch)])
        for eid,viewer,mid,watch in hold: event_rows.append([eid,viewer,mid,"2027-01-22T00:00:00Z",str(watch)])
        (self.fixture/"events.csv").write_bytes(csv_bytes(["event_id","viewer_id","media_id","occurred_at_utc","watch_seconds"],event_rows))
    def _input_bytes(self):
        return {p.name:p.read_bytes() for p in self.fixture.iterdir() if p.is_file()}
    def run_cli(self):
        before=self._input_bytes()
        proc=subprocess.run([sys.executable,"-m","ai.streamforge.content_evaluate","--fixture","basic"],cwd=self.root,env=self.env,capture_output=True,timeout=20)
        after=self._input_bytes()
        self.assertEqual(before,after,"copied registered inputs must be byte-identical")
        return proc

class Day194Test(ContentCliHarness):
    def test_actual_child_process_composition_matches_literal_oracle(self):
        before=self._input_bytes()
        expected=_expected_complete_report(before)  # independent full object constructed before learner call
        proc=self.run_cli()
        self.assertEqual(0,proc.returncode,proc.stderr.decode("utf-8","replace"))
        report=json.loads(proc.stdout,parse_constant=lambda x: (_ for _ in ()).throw(ValueError(x)))
        self.assertEqual(expected,report)
        self.assertEqual(1,len(self.trace.read_text(encoding="utf-8").splitlines()),"real prior preparation must run exactly once")

class ContentManifestPreflightTest(ContentCliHarness):
    def test_actual_cli_rejects_ambiguous_manifest_without_success_output(self):
        p=self.fixture/"manifest.json"
        raw=p.read_text(encoding="utf-8")
        p.write_text(raw.replace('"schema_version": 1,','"schema_version": 1,\n  "schema_version": 2,',1),encoding="utf-8")
        proc=self.run_cli()
        self.assertNotEqual(0,proc.returncode)
        self.assertEqual(b"",proc.stdout)
        self.assertTrue(proc.stderr)

class Day195Test(ContentCliHarness):
    def _report(self):
        result=self.run_cli()
        self.assertEqual(0,result.returncode,result.stderr.decode("utf-8","replace"))
        return json.loads(result.stdout)
    def _rows(self):
        with (self.fixture/"events.csv").open(newline="",encoding="utf-8") as stream:
            return list(csv.DictReader(stream))
    def _save_rows(self,rows):
        header=["event_id","viewer_id","media_id","occurred_at_utc","watch_seconds"]
        stream=io.StringIO(newline="");writer=csv.DictWriter(stream,fieldnames=header,lineterminator="\n");writer.writeheader();writer.writerows(rows)
        (self.fixture/"events.csv").write_text(stream.getvalue(),encoding="utf-8",newline="")
    def test_holdout_addition_changes_metrics_not_prediction_state(self):
        base=self._report();rows=self._rows();rows.append({"event_id":"h09","viewer_id":"v2","media_id":"y","occurred_at_utc":"2027-01-22T00:00:00Z","watch_seconds":"10"});self._save_rows(rows)
        changed=self._report()
        self.assertEqual(base["predictor"],changed["predictor"])
        self.assertEqual(base["queries"],changed["queries"])
        self.assertEqual(("1/4","11/24"),(changed["evaluations"]["profile"]["precision_at_3"],changed["evaluations"]["profile"]["recall_at_3"]))
        self.assertEqual(("1/4","5/12"),(changed["evaluations"]["global_popularity"]["precision_at_3"],changed["evaluations"]["global_popularity"]["recall_at_3"]))
        self.assertEqual("1/24",changed["evaluations"]["signed_deltas"]["recall_at_3"])
        self.assertEqual(2,changed["evaluations"]["profile"]["counts"]["candidate_unreachable_positive_pairs"])
    def test_positive_watch_magnitude_changes_provenance_not_prediction(self):
        base=self._report();rows=self._rows()
        next(row for row in rows if row["event_id"]=="t01")["watch_seconds"]="11";self._save_rows(rows)
        changed=self._report()
        self.assertEqual(base["predictor"],changed["predictor"])
        self.assertEqual(base["queries"],changed["queries"])
        self.assertEqual(base["evaluations"],changed["evaluations"])
        self.assertNotEqual(base["preparation"]["training"]["content_sha256"],changed["preparation"]["training"]["content_sha256"])
    def test_same_candidate_training_edit_changes_profile_and_current_baseline(self):
        base=self._report();rows=self._rows()
        next(row for row in rows if row["event_id"]=="t03")["media_id"]="d"
        next(row for row in rows if row["event_id"]=="t04")["media_id"]="d";self._save_rows(rows)
        changed=self._report()
        self.assertEqual(base["predictor"]["candidate_ids"],changed["predictor"]["candidate_ids"])
        self.assertEqual(["a","b","d"],next(q["recommendation_ids"] for q in changed["queries"] if q["viewer_id"]=="v1"))
        self.assertEqual([0,0,1,1,0],changed["predictor"]["profiles"]["v1"]["genre_counts"])
        self.assertEqual("-1/12",changed["evaluations"]["signed_deltas"]["precision_at_3"])
        self.assertEqual("-1/8",changed["evaluations"]["signed_deltas"]["recall_at_3"])
        self.assertNotEqual(base["predictor"]["fingerprint"],changed["predictor"]["fingerprint"])
    def test_shuffled_raw_rows_keep_semantics_and_change_source_hash(self):
        base=self._report();rows=self._rows();self._save_rows(list(reversed(rows)))
        shuffled=self._report()
        self.assertEqual(base["predictor"],shuffled["predictor"])
        self.assertEqual(base["queries"],shuffled["queries"])
        self.assertEqual(base["evaluations"],shuffled["evaluations"])
        self.assertNotEqual(base["preparation"]["source_sha256"],shuffled["preparation"]["source_sha256"])
    def test_duplicate_replay_collapses_without_semantic_change(self):
        base=self._report();rows=self._rows();rows.append(dict(next(row for row in rows if row["event_id"]=="t01")));self._save_rows(rows)
        replay=self._report()
        self.assertEqual(base["predictor"],replay["predictor"])
        self.assertEqual(base["queries"],replay["queries"])
        self.assertEqual(base["evaluations"],replay["evaluations"])
        self.assertEqual(1,replay["preparation"]["duplicate_events_collapsed"])
    def test_invalid_holdout_and_cross_cutoff_conflict_fail_before_json(self):
        rows=self._rows();next(row for row in rows if row["event_id"]=="h03")["occurred_at_utc"]="2027-01-20T12:00:00Z";self._save_rows(rows)
        invalid=self.run_cli();self.assertNotEqual(0,invalid.returncode);self.assertEqual(b"",invalid.stdout)
        self._install_oracle_fixture();rows=self._rows();rows.append({"event_id":"t01","viewer_id":"other","media_id":"a","occurred_at_utc":"2027-01-22T00:00:00Z","watch_seconds":"10"});self._save_rows(rows)
        conflict=self.run_cli();self.assertNotEqual(0,conflict.returncode);self.assertEqual(b"",conflict.stdout)
