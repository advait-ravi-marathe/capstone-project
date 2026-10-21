"""Actual-process acceptance seams; no canned report is supplied."""
from __future__ import annotations
import csv, hashlib, io, json, os, shutil, subprocess, sys, tempfile, unittest, copy
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

def _expected_training_edit_report(input_bytes):
    """Literal independent projection for t03/t04 changing from x/c to d."""
    expected=copy.deepcopy(_expected_complete_report(input_bytes))
    p=expected["predictor"]
    p["profiles"]["v1"]={"genre_counts":[0,0,1,1,0],"weights":["0/1","0/1","1/2","1/2","0/1"],"positive_train_pairs":2}
    p["global_popularity"]["support"]={"a":1,"b":1,"c":0,"d":4,"e":2,"f":0}
    payload={k:v for k,v in p.items() if k!="fingerprint"}
    p["fingerprint"]=_sha(payload)
    for q in expected["queries"]:
        q["state_fingerprint"]=p["fingerprint"]
        if q["viewer_id"]=="v1":
            q["positive_train_pairs"]=2
            q["scores_by_media"]={"a":"1/2","b":"1/2","c":"0/1","d":"1/2","e":"0/1","f":"0/1"}
            q["full_order"]=["a","b","d","c","e","f"]
            q["recommendation_ids"]=["a","b","d"]
        projection={k:v for k,v in q.items() if k!="fingerprint"}
        q["fingerprint"]=_sha(projection)
    ev=expected["evaluations"]
    ev["profile"]["per_viewer_metrics"][1]={"viewer_id":"v1","positive_target_ids":["c","y"],"hit_count":0,"positive_target_count":2,"candidate_unreachable_positive_pairs":1,"precision_at_3":"0/1","recall_at_3":"0/1","status":"available"}
    ev["profile"]["precision_at_3"]="1/6";ev["profile"]["recall_at_3"]="3/8"
    ev["signed_deltas"]={"precision_at_3":"-1/12","recall_at_3":"-1/8"}
    prep=expected["preparation"]
    rows=list(csv.DictReader(io.StringIO(input_bytes["events.csv"].decode())))
    for row in rows:
        if row["event_id"] in {"t03","t04"}: row["media_id"]="d"
    train=[{"event_id":r["event_id"],"viewer_id":r["viewer_id"],"media_id":r["media_id"],"occurred_at_utc":r["occurred_at_utc"],"watch_seconds":int(r["watch_seconds"])} for r in rows if r["event_id"].startswith("t")]
    summary={"status":"available","event_count":11,"distinct_viewer_count":6,"media_event_counts":{"a":2,"b":1,"d":5,"e":2,"f":1},"media_watch_seconds":{"a":30,"b":10,"d":50,"e":20,"f":0}}
    train_projection={"schema_version":1,"window_start_utc":expected["protocol"]["window_start_utc"],"cutoff_utc":expected["protocol"]["cutoff_utc"],"train":train,"training":summary}
    summary["content_sha256"]=_sha(train_projection,trailing=False)
    prep["training"]=summary
    prep["holdout_diagnostics"]={"status":"available","viewer_item_seen_in_train":3,"viewer_seen_item_unseen":3,"viewer_unseen_item_seen":2,"viewer_item_unseen":0}
    return expected

def _expected_holdout_edit_report(input_bytes):
    """Literal independent projection after adding positive v2→y holdout."""
    expected=copy.deepcopy(_expected_complete_report(input_bytes));ev=expected["evaluations"]
    ev["profile"]["per_viewer_metrics"][2]={"viewer_id":"v2","positive_target_ids":["d","e","y"],"hit_count":1,"positive_target_count":3,"candidate_unreachable_positive_pairs":1,"precision_at_3":"1/3","recall_at_3":"1/3","status":"available"}
    ev["profile"]["counts"]["positive_viewer_media_pairs"]=7
    ev["profile"]["counts"]["candidate_unreachable_positive_pairs"]=2
    ev["profile"]["precision_at_3"]="1/4";ev["profile"]["recall_at_3"]="11/24"
    ev["global_popularity"]["counts"]["positive_viewer_media_pairs"]=7
    ev["global_popularity"]["counts"]["candidate_unreachable_positive_pairs"]=2
    ev["global_popularity"]["recall_at_3"]="5/12"
    ev["signed_deltas"]={"precision_at_3":"0/1","recall_at_3":"1/24"}
    prep=expected["preparation"]
    prep["partition_counts"]["holdout"]=9
    prep["holdout_diagnostics"]["viewer_seen_item_unseen"]=2
    return expected

def _refresh_query_hashes(expected):
    for query in expected["queries"]:
        query["state_fingerprint"]=expected["predictor"]["fingerprint"]
        projection={k:v for k,v in query.items() if k!="fingerprint"}
        query["fingerprint"]=_sha(projection)

def _expected_new_viewer_report(input_bytes):
    expected=copy.deepcopy(_expected_complete_report(input_bytes));p=expected["predictor"];ev=expected["evaluations"]
    state_fp=p["fingerprint"]
    projection={"viewer_id":"newer","training_seen":False,"positive_train_pairs":0,"mode":"global_cold_start",
                "scores_by_media":{mid:"0/1" for mid in p["candidate_ids"]},"full_order":p["global_popularity"]["full_order"],
                "recommendation_ids":p["global_popularity"]["recommendation_ids"]}
    query=dict(projection,state_fingerprint=state_fp);query["fingerprint"]=_sha(query)
    expected["queries"].append(query);expected["queries"].sort(key=lambda q:q["viewer_id"])
    profile=ev["profile"]
    profile["per_viewer_metrics"].append({"viewer_id":"newer","positive_target_ids":["a"],"hit_count":1,"positive_target_count":1,
                                          "candidate_unreachable_positive_pairs":0,"precision_at_3":"1/3","recall_at_3":"1/1","status":"available"})
    profile["per_viewer_metrics"].sort(key=lambda row:row["viewer_id"])
    profile["counts"].update({"all_holdout_viewers":6,"evaluated_viewers":5,"positive_viewer_media_pairs":7,
                              "cold_start_evaluated_viewers":2,"returned_ids_total":15})
    profile["selection_mode_counts"]["global_cold_start"]=2
    profile.update({"precision_at_3":"4/15","recall_at_3":"3/5"})
    ev["global_popularity"]["counts"].update({"all_holdout_viewers":6,"evaluated_viewers":5,"positive_viewer_media_pairs":7,"cold_start_evaluated_viewers":2})
    ev["global_popularity"].update({"precision_at_3":"4/15","recall_at_3":"3/5"})
    ev["signed_deltas"]={"precision_at_3":"0/1","recall_at_3":"0/1"}
    prep=expected["preparation"];prep["partition_counts"]["holdout"]=9
    prep["holdout_diagnostics"]["viewer_unseen_item_seen"]=3
    return expected

def _expected_magnitude_edit_report(input_bytes):
    expected=copy.deepcopy(_expected_complete_report(input_bytes));prep=expected["preparation"]
    train=[{"event_id":r["event_id"],"viewer_id":r["viewer_id"],"media_id":r["media_id"],"occurred_at_utc":r["occurred_at_utc"],"watch_seconds":int(r["watch_seconds"])}
           for r in csv.DictReader(io.StringIO(input_bytes["events.csv"].decode())) if r["event_id"].startswith("t")]
    summary=prep["training"];summary["media_watch_seconds"]["a"]=31
    payload={"schema_version":1,"window_start_utc":expected["protocol"]["window_start_utc"],"cutoff_utc":expected["protocol"]["cutoff_utc"],"train":train,
             "training":{k:summary[k] for k in ("status","event_count","distinct_viewer_count","media_event_counts","media_watch_seconds")}}
    summary["content_sha256"]=_sha(payload,trailing=False)
    return expected

def _expected_global_training_contrast_report(input_bytes):
    expected=copy.deepcopy(_expected_complete_report(input_bytes));p=expected["predictor"];ev=expected["evaluations"]
    p["profiles"]["v3"]={"genre_counts":[0,0,0,1,0],"weights":["0/1","0/1","0/1","1/1","0/1"],"positive_train_pairs":1}
    p["profiles"]["v4"]={"genre_counts":[0,0,0,1,0],"weights":["0/1","0/1","0/1","1/1","0/1"],"positive_train_pairs":1}
    p["global_popularity"]={"support":{"a":3,"b":1,"c":1,"d":1,"e":2,"f":0},"full_order":["a","e","b","c","d","f"],"recommendation_ids":["a","e","b"],"fallback_status":"popularity_ranked"}
    p["fingerprint"]=_sha({k:v for k,v in p.items() if k!="fingerprint"})
    for q in expected["queries"]:
        if q["mode"] in {"global_cold_start","global_no_positive_profile"}:
            q["full_order"]=["a","e","b","c","d","f"];q["recommendation_ids"]=["a","e","b"]
    _refresh_query_hashes(expected)
    profile=ev["profile"];z=next(r for r in profile["per_viewer_metrics"] if r["viewer_id"]=="z")
    z.update({"hit_count":1,"precision_at_3":"1/3","recall_at_3":"1/1"})
    profile.update({"precision_at_3":"1/3","recall_at_3":"3/4"})
    ev["global_popularity"].update({"recommendation_ids":["a","e","b"],"precision_at_3":"1/4","recall_at_3":"5/8"})
    ev["signed_deltas"]={"precision_at_3":"1/12","recall_at_3":"1/8"}
    prep=expected["preparation"];rows=list(csv.DictReader(io.StringIO(input_bytes["events.csv"].decode())))
    for row in rows:
        if row["event_id"] in {"t08","t09"}:row["media_id"]="a"
    train=[{"event_id":r["event_id"],"viewer_id":r["viewer_id"],"media_id":r["media_id"],"occurred_at_utc":r["occurred_at_utc"],"watch_seconds":int(r["watch_seconds"])} for r in rows if r["event_id"].startswith("t")]
    summary=prep["training"];summary["media_event_counts"]={"a":4,"x":1,"c":1,"b":1,"d":1,"e":2,"f":1};summary["media_watch_seconds"]={"a":50,"x":10,"c":10,"b":10,"d":10,"e":20,"f":0}
    train_projection={"schema_version":1,"window_start_utc":expected["protocol"]["window_start_utc"],"cutoff_utc":expected["protocol"]["cutoff_utc"],"train":train,
                     "training":{k:summary[k] for k in ("status","event_count","distinct_viewer_count","media_event_counts","media_watch_seconds")}}
    summary["content_sha256"]=_sha(train_projection,trailing=False)
    train_viewers={r["viewer_id"] for r in train};train_media={r["media_id"] for r in train};diag={"viewer_item_seen_in_train":0,"viewer_seen_item_unseen":0,"viewer_unseen_item_seen":0,"viewer_item_unseen":0}
    for row in rows:
        if not row["event_id"].startswith("h"):continue
        vs=row["viewer_id"] in train_viewers;ms=row["media_id"] in train_media
        key="viewer_item_seen_in_train" if vs and ms else "viewer_seen_item_unseen" if vs else "viewer_unseen_item_seen" if ms else "viewer_item_unseen"
        diag[key]+=1
    prep["holdout_diagnostics"]={"status":"available",**diag}
    return expected

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
            "import os, sys, json, socket, urllib.request, http.client\n"
            "sys.path.insert(0, os.environ['CONTENT_COPY_ROOT'])\n"
            "def _blocked(*args, **kwargs): raise RuntimeError('network API access blocked by test guard')\n"
            "socket.create_connection = _blocked\n"
            "socket.socket.connect = _blocked\n"
            "urllib.request.urlopen = _blocked\n"
            "http.client.HTTPConnection.connect = _blocked\n"
            "def _record(kind, **data):\n"
            "    with open(os.environ['CONTENT_TRACE'], 'a', encoding='utf-8') as out: out.write(json.dumps({'kind':kind,**data},sort_keys=True)+'\\n')\n"
            "import ai.streamforge.evaluation_adapter as adapter\n"
            "_original = adapter.prepare_dataset\n"
            "def _counted(name):\n"
            "    _record('prepare_dataset', fixture=name)\n"
            "    return _original(name)\n"
            "adapter.prepare_dataset = _counted\n"
            "_typed = adapter.prepare_evaluation_input\n"
            "def _typed_counted(name):\n"
            "    result = _typed(name)\n"
            "    _record('prepare_evaluation_input', fixture=name, train=len(result.train), holdout=len(result.holdout), catalog=len(result.catalog))\n"
            "    if os.environ.get('CONTENT_ADAPTER_PROBE') == '1':\n"
            "        from dataclasses import replace\n"
            "        result = replace(result, lineage={**result.lineage, 'adapter_return_probe':'consumed'})\n"
            "    return result\n"
            "adapter.prepare_evaluation_input = _typed_counted\n"
            "import ai.streamforge.baseline_contracts as baseline\n"
            "_baseline_eval = baseline.evaluate_baseline\n"
            "def _targets(t): return {'targets':{k:sorted(v) for k,v in sorted(t.targets_by_viewer.items())},'viewers':sorted(t.all_holdout_viewer_ids),'zero':t.zero_positive_viewer_count,'status':t.status}\n"
            "def _baseline(*args, **kwargs):\n"
            "    _record('evaluate_baseline', candidates=list(args[1]), ranked=list(args[0].recommendation_ids), targets=_targets(args[2]))\n"
            "    return _baseline_eval(*args, **kwargs)\n"
            "baseline.evaluate_baseline = _baseline\n"
            "import ai.streamforge.content_contracts as content\n"
            "_profile_eval = content.evaluate_profiles\n"
            "def _profiles(*args, **kwargs):\n"
            "    _record('evaluate_profiles', candidates=list(args[0].candidate_ids), targets=_targets(args[1]))\n"
            "    return _profile_eval(*args, **kwargs)\n"
            "content.evaluate_profiles = _profiles\n", encoding="utf-8")
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
        self.trace.write_text("",encoding="utf-8")
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
        records=[json.loads(line) for line in self.trace.read_text(encoding="utf-8").splitlines()]
        self.assertEqual(["prepare_dataset","prepare_evaluation_input","evaluate_profiles","evaluate_baseline"],[r["kind"] for r in records])
        self.assertEqual(("basic",11,8,8),(records[1]["fixture"],records[1]["train"],records[1]["holdout"],records[1]["catalog"]))
        self.assertEqual(records[2]["targets"],records[3]["targets"],"profile and global evaluation must receive identical HoldoutTargets")
        self.assertEqual(records[2]["candidates"],records[3]["candidates"],"both methods must receive identical cutoff candidates")

    def test_actual_cli_consumes_the_typed_adapter_return_value(self):
        before=self._input_bytes();expected=_expected_complete_report(before)
        self.env["CONTENT_ADAPTER_PROBE"]="1"
        expected["preparation"]["adapter_return_probe"]="consumed"
        proc=self.run_cli()
        self.assertEqual(0,proc.returncode,proc.stderr.decode("utf-8","replace"))
        self.assertEqual(expected,json.loads(proc.stdout))
        records=[json.loads(line) for line in self.trace.read_text(encoding="utf-8").splitlines()]
        self.assertEqual(1,sum(r["kind"]=="prepare_evaluation_input" for r in records))

class ContentManifestPreflightTest(ContentCliHarness):
    def test_actual_cli_rejects_ambiguous_manifest_without_success_output(self):
        p=self.fixture/"manifest.json"
        raw=p.read_text(encoding="utf-8")
        p.write_text(raw.replace('"schema_version": 1,','"schema_version": 1,\n  "schema_version": 2,',1),encoding="utf-8")
        proc=self.run_cli()
        self.assertNotEqual(0,proc.returncode)
        self.assertEqual(b"",proc.stdout)
        self.assertTrue(proc.stderr)

    def test_python_network_api_guard_blocks_an_attempt(self):
        probe=subprocess.run([sys.executable,"-c","import urllib.request; urllib.request.urlopen('http://127.0.0.1:1')"],cwd=self.root,env=self.env,capture_output=True,timeout=5)
        self.assertNotEqual(0,probe.returncode)
        self.assertIn(b"network API access blocked by test guard",probe.stderr)

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
        expected=_expected_holdout_edit_report(self._input_bytes())
        changed=self._report()
        self.assertEqual(expected,changed)
        self.assertEqual(base["predictor"],changed["predictor"])
        self.assertEqual(base["queries"],changed["queries"])
        self.assertEqual(("1/4","11/24"),(changed["evaluations"]["profile"]["precision_at_3"],changed["evaluations"]["profile"]["recall_at_3"]))
        self.assertEqual(("1/4","5/12"),(changed["evaluations"]["global_popularity"]["precision_at_3"],changed["evaluations"]["global_popularity"]["recall_at_3"]))
        self.assertEqual("1/24",changed["evaluations"]["signed_deltas"]["recall_at_3"])
        self.assertEqual(2,changed["evaluations"]["profile"]["counts"]["candidate_unreachable_positive_pairs"])
    def test_positive_watch_magnitude_changes_provenance_not_prediction(self):
        base=self._report();rows=self._rows()
        next(row for row in rows if row["event_id"]=="t01")["watch_seconds"]="11";self._save_rows(rows)
        expected=_expected_magnitude_edit_report(self._input_bytes())
        changed=self._report()
        self.assertEqual(expected,changed)
        self.assertEqual(base["predictor"],changed["predictor"])
        self.assertEqual(base["queries"],changed["queries"])
        self.assertEqual(base["evaluations"],changed["evaluations"])
        self.assertNotEqual(base["preparation"]["training"]["content_sha256"],changed["preparation"]["training"]["content_sha256"])
    def test_same_candidate_training_edit_changes_profile_and_current_baseline(self):
        base=self._report();rows=self._rows()
        next(row for row in rows if row["event_id"]=="t03")["media_id"]="d"
        next(row for row in rows if row["event_id"]=="t04")["media_id"]="d";self._save_rows(rows)
        expected=_expected_training_edit_report(self._input_bytes())
        changed=self._report()
        self.assertEqual(expected,changed)
        self.assertEqual(base["predictor"]["candidate_ids"],changed["predictor"]["candidate_ids"])
        self.assertEqual(["a","b","d"],next(q["recommendation_ids"] for q in changed["queries"] if q["viewer_id"]=="v1"))
        self.assertEqual([0,0,1,1,0],changed["predictor"]["profiles"]["v1"]["genre_counts"])
        self.assertEqual("-1/12",changed["evaluations"]["signed_deltas"]["precision_at_3"])
        self.assertEqual("-1/8",changed["evaluations"]["signed_deltas"]["recall_at_3"])
        self.assertNotEqual(base["predictor"]["fingerprint"],changed["predictor"]["fingerprint"])
    def test_shuffled_raw_rows_keep_semantics_and_change_source_hash(self):
        self._report();rows=self._rows();self._save_rows(list(reversed(rows)))
        expected=_expected_complete_report(self._input_bytes())
        shuffled=self._report()
        self.assertEqual(expected,shuffled)
    def test_duplicate_replay_collapses_without_semantic_change(self):
        base=self._report();rows=self._rows();rows.append(dict(next(row for row in rows if row["event_id"]=="t01")));self._save_rows(rows)
        expected=_expected_complete_report(self._input_bytes());expected["preparation"]["duplicate_events_collapsed"]=1
        replay=self._report()
        self.assertEqual(expected,replay)
        self.assertEqual(base["predictor"],replay["predictor"])
        self.assertEqual(base["queries"],replay["queries"])
        self.assertEqual(base["evaluations"],replay["evaluations"])
        self.assertEqual(1,replay["preparation"]["duplicate_events_collapsed"])

    def test_new_holdout_viewer_changes_cohort_not_predictor_state(self):
        base=self._report();rows=self._rows();rows.append({"event_id":"h09","viewer_id":"newer","media_id":"a","occurred_at_utc":"2027-01-22T00:00:00Z","watch_seconds":"10"});self._save_rows(rows)
        expected=_expected_new_viewer_report(self._input_bytes())
        changed=self._report()
        self.assertEqual(expected,changed)
        self.assertEqual(base["predictor"],changed["predictor"])
        self.assertEqual(1,sum(q["viewer_id"]=="newer" for q in changed["queries"]))

    def test_distinct_training_change_recomputes_global_order_and_full_report(self):
        base=self._report();rows=self._rows()
        for event_id in ("t08","t09"):next(row for row in rows if row["event_id"]==event_id)["media_id"]="a"
        self._save_rows(rows);expected=_expected_global_training_contrast_report(self._input_bytes())
        changed=self._report()
        self.assertEqual(expected,changed)
        self.assertEqual(["a","e","b","c","d","f"],changed["predictor"]["global_popularity"]["full_order"])
        records=[json.loads(line) for line in self.trace.read_text(encoding="utf-8").splitlines()]
        self.assertEqual(["a","e","b"],records[-1]["ranked"])
        self.assertNotEqual(base["predictor"]["global_popularity"]["full_order"],changed["predictor"]["global_popularity"]["full_order"])

    def test_identical_input_replays_have_identical_fresh_process_bytes(self):
        first=self.run_cli();self.assertEqual(0,first.returncode,first.stderr.decode("utf-8","replace"))
        second=self.run_cli();self.assertEqual(0,second.returncode,second.stderr.decode("utf-8","replace"))
        self.assertEqual(first.stdout,second.stdout)
    def test_invalid_holdout_and_cross_cutoff_conflict_fail_before_json(self):
        rows=self._rows();next(row for row in rows if row["event_id"]=="h03")["occurred_at_utc"]="2027-01-20T12:00:00Z";self._save_rows(rows)
        invalid=self.run_cli();self.assertNotEqual(0,invalid.returncode);self.assertEqual(b"",invalid.stdout)
        self._install_oracle_fixture();rows=self._rows();rows.append({"event_id":"t01","viewer_id":"other","media_id":"a","occurred_at_utc":"2027-01-22T00:00:00Z","watch_seconds":"10"});self._save_rows(rows)
        conflict=self.run_cli();self.assertNotEqual(0,conflict.returncode);self.assertEqual(b"",conflict.stdout)
    def test_scored_pair_limit_rejects_without_truncating_or_emitting_json(self):
        with (self.fixture/"catalog.csv").open(newline="",encoding="utf-8") as stream:
            catalog_rows=list(csv.DictReader(stream))
        genres=("animation","comedy","documentary","drama","science-fiction")
        for i in range(22):
            media_id=f"g{i:02d}"
            catalog_rows.append({"media_id":media_id,"title":f"Synthetic {media_id}","genre":genres[i%len(genres)],"duration_s":"100","available_from_utc":"2027-01-01T00:00:00Z","available_to_utc":"2027-03-01T00:00:00Z"})
        with (self.fixture/"catalog.csv").open("w",newline="",encoding="utf-8") as stream:
            writer=csv.DictWriter(stream,fieldnames=["media_id","title","genre","duration_s","available_from_utc","available_to_utc"],lineterminator="\n");writer.writeheader();writer.writerows(catalog_rows)
        train_rows=[row for row in self._rows() if row["event_id"].startswith("t")]
        candidates=[row["media_id"] for row in catalog_rows if row["media_id"] not in {"x","y"}]
        holdout=[{"event_id":f"bound-{i:03d}","viewer_id":f"bound-viewer-{i:03d}","media_id":candidates[i%len(candidates)],"occurred_at_utc":"2027-01-22T00:00:00Z","watch_seconds":"10"} for i in range(90)]
        self._save_rows(train_rows+holdout)
        result=self.run_cli()
        self.assertNotEqual(0,result.returncode)
        self.assertEqual(b"",result.stdout)
        self.assertIn(b"scored-pair limit exceeded",result.stderr)
