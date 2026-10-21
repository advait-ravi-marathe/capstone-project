"""Independent daily acceptance oracles for Days183–193.

Expected values below are literal arithmetic, never produced by learner helpers.
"""
from __future__ import annotations

import copy
import unittest
from datetime import datetime, timezone
from fractions import Fraction as F

from ai.streamforge.models import CatalogItem
from ai.streamforge.evaluation_models import PopularityState, HoldoutTargets, BaselineEvaluation, RankedList
from ai.streamforge.content_models import ProfileCounts, ProfileWeights, PredictorState, ProfileEvaluation, ProfileComparison
from ai.streamforge.content_io import rendered_protocol
from ai.streamforge import content_contracts as c

UTC = timezone.utc
START = datetime(2027, 1, 1, tzinfo=UTC)
END = datetime(2027, 3, 1, tzinfo=UTC)
GENRES = ("animation", "comedy", "documentary", "drama", "science-fiction")

def cat(mid, genre, start=START, end=END):
    return CatalogItem(mid, "Synthetic " + mid, genre, 100, start, end)

def _frozen_protocol():
    p=rendered_protocol("2027-01-01T00:00:00Z","2027-01-20T00:00:00Z","2027-02-01T00:00:00Z")
    p["genres"]=tuple(p["genres"])
    return tuple(sorted(p.items()))

class Day183Test(unittest.TestCase):
    def test_fixed_one_hot_vocabulary(self):
        expected = ((1,0,0,0,0),(0,1,0,0,0),(0,0,1,0,0),(0,0,0,1,0),(0,0,0,0,1))
        self.assertEqual(tuple(c.encode_genre(g) for g in GENRES), expected)
        for bad in ("", "Drama", "other"):
            with self.subTest(bad=bad), self.assertRaises((ValueError, TypeError)):
                c.encode_genre(bad)

class Day184Test(unittest.TestCase):
    def test_catalog_projection_keeps_history_and_is_immutable(self):
        rows = [cat("x", "comedy", START, datetime(2027,1,20,tzinfo=UTC)), cat("a", "drama")]
        before = copy.deepcopy(rows)
        self.assertEqual(c.catalog_features(rows), (("a",(0,0,0,1,0)),("x",(0,1,0,0,0))))
        self.assertEqual(rows, before)
        with self.assertRaises(ValueError): c.catalog_features([cat("x","drama"),cat("x","comedy")])
        with self.assertRaises(ValueError): c.catalog_features([cat("u","unknown")])

class Day185Test(unittest.TestCase):
    def test_distinct_positive_training_media_per_genre(self):
        feats = {"a":(0,0,0,1,0),"c":(0,1,0,0,0),"x":(0,1,0,0,0)}
        pairs = (("v1","a"),("v1","a"),("v1","c"),("v1","x"))
        state = c.profile_counts(pairs, feats, ("v1","zero"))
        self.assertEqual(state, ProfileCounts((("v1",(0,2,0,1,0),3),("zero",(0,0,0,0,0),0))))
        self.assertEqual(pairs, (("v1","a"),("v1","a"),("v1","c"),("v1","x")))
        with self.assertRaises(ValueError): c.profile_counts((("intruder","a"),),feats,("v1",))

class Day186Test(unittest.TestCase):
    def test_exact_fraction_normalization_and_zero_profile(self):
        expected = ProfileWeights((("v1",(F(0),F(2,3),F(0),F(1,3),F(0)),3),("z",(F(0),)*5,0)))
        self.assertEqual(c.normalize_profiles(ProfileCounts((("v1",(0,2,0,1,0),3),("z",(0,0,0,0,0),0)))),expected)
        for bad in (ProfileCounts((("v",(0,True,0,0,0),1),)), ProfileCounts((("v",(0,-1,0,0,0),1),)), ProfileCounts((("v",(1,0),1),))):
            with self.subTest(bad=bad), self.assertRaises((ValueError, TypeError)):
                c.normalize_profiles(bad)

class Day187Test(unittest.TestCase):
    def test_dot_product_for_every_candidate(self):
        features = {"a":(1,0,0,0,0),"b":(0,1,0,0,0),"c":(0,0,1,0,0)}
        expected=(("a",F(2,3)),("b",F(1,3)),("c",F(0)))
        self.assertEqual(c.score_candidates((F(2,3),F(1,3),F(0),F(0),F(0)),features),expected)
        self.assertEqual(dict(c.score_candidates((F(2,3),F(1,3),F(0),F(0),F(0)),dict(reversed(tuple(features.items()))))),dict(expected))
        changed=dict(features);changed["a"]=(0,1,0,0,0)
        self.assertEqual(F(1,3),dict(c.score_candidates((F(2,3),F(1,3),F(0),F(0),F(0)),changed))["a"])

class Day188Test(unittest.TestCase):
    def test_personal_ranking_and_distinct_fallback_modes(self):
        counts = ProfileCounts((("v1",(0,2,0,1,0),3),("zero",(0,0,0,0,0),0)))
        weights = ProfileWeights((("v1",(F(0),F(2,3),F(0),F(1,3),F(0)),3),("zero",(F(0),)*5,0)))
        features={"a":(0,0,0,1,0),"b":(0,0,0,1,0),"c":(0,1,0,0,0),"d":(0,0,1,0,0)}
        actual=c.rank_for_viewer("v1",weights,counts,("a","b","c","d"),features,("d","c","a","b"))
        self.assertEqual(actual.full_order,("c","a","b","d"))
        cold=c.rank_for_viewer("cold",weights,counts,("a","b","c","d"),features,("d","c","a","b"))
        zero=c.rank_for_viewer("zero",weights,counts,("a","b","c","d"),features,("d","c","a","b"))
        self.assertEqual((cold.mode,cold.recommendation_ids),("global_cold_start",("d","c","a")))
        self.assertEqual((zero.mode,zero.recommendation_ids),("global_no_positive_profile",("d","c","a")))
        no_overlap=c.rank_for_viewer("v1",weights,counts,("d","e"),{"d":features["d"],"e":(1,0,0,0,0)},("e","d"))
        self.assertEqual((no_overlap.viewer_id,no_overlap.training_seen,no_overlap.positive_train_pairs,no_overlap.mode,
                          no_overlap.scores_by_media,no_overlap.full_order,no_overlap.recommendation_ids),
                         ("v1",True,3,"genre_profile",(("d",F(0)),("e",F(0))),("d","e"),("d","e")))
        empty=c.rank_for_viewer("v1",weights,counts,(),{},())
        self.assertEqual((empty.mode,empty.recommendation_ids),("no_candidates",()))

class Day190Test(unittest.TestCase):
    def test_owned_state_excludes_query_and_raw_hash_inputs(self):
        import hashlib, json
        protocol={"id":"streamforge-cutoff-genre-profile-v1","k":3,"genres":["animation","comedy","documentary","drama","science-fiction"],"window_start_utc":"2027-01-01T00:00:00Z","cutoff_utc":"2027-01-20T00:00:00Z","window_end_utc":"2027-02-01T00:00:00Z","training_signal":"distinct_positive_train_viewer_media","normalization":"l1_exact_fraction","similarity":"dot_product","tie_break":"media_id_ascending","fallback_rule":"cold_or_zero_profile_to_current_global","availability_rule":"from_lte_cutoff_lt_to","evaluation_role":"development_comparison_on_previously_inspected_synthetic_holdout","max_scored_pairs":2500}
        f={"a":(0,0,0,1,0)}; counts=ProfileCounts((("v",(0,0,0,1,0),1),)); weights=ProfileWeights((("v",(F(0),F(0),F(0),F(1),F(0)),1),))
        gs=PopularityState({"a":1},False)
        viewers=("v",); candidates=("a",); rank=("a",)
        before=(copy.deepcopy(protocol),copy.deepcopy(f),copy.deepcopy(counts),copy.deepcopy(weights),copy.deepcopy(viewers),copy.deepcopy(candidates),copy.deepcopy(gs),copy.deepcopy(rank))
        frozen_protocol=copy.deepcopy(protocol);frozen_protocol["genres"]=tuple(frozen_protocol["genres"])
        payload={"protocol":copy.deepcopy(protocol),"candidate_ids":["a"],"candidate_features":{"a":[0,0,0,1,0]},
                 "training_viewer_ids":["v"],"profiles":{"v":{"genre_counts":[0,0,0,1,0],"weights":["0/1","0/1","0/1","1/1","0/1"],"positive_train_pairs":1}},
                 "global_popularity":{"support":{"a":1},"full_order":["a"],"recommendation_ids":["a"],"fallback_status":"popularity_ranked"}}
        canonical=json.dumps(payload,ensure_ascii=False,allow_nan=False,sort_keys=True,separators=(",",":")).encode("utf-8")+b"\n"
        fingerprint=hashlib.sha256(canonical).hexdigest()
        expected=PredictorState(tuple(sorted(frozen_protocol.items())),("a",),(("a",(0,0,0,1,0)),),("v",),
            (("v",(0,0,0,1,0),(F(0),F(0),F(0),F(1),F(0)),1),),(('a',1),),("a",),("a",),"popularity_ranked",fingerprint)
        state=c.assemble_predictor_state(protocol,f,counts,weights,viewers,candidates,gs,rank)
        self.assertEqual(before,(protocol,f,counts,weights,viewers,candidates,gs,rank),"state assembly must preserve all caller inputs")
        self.assertEqual(expected,state,"compare every state field and independently computed digest")
        protocol["extra"]="mutated";protocol["genres"].append("mutated");f["a"]=(1,0,0,0,0);gs.support_by_media["a"]=77
        self.assertEqual(expected,state,"assembled state must own all nested values")

class Day191Test(unittest.TestCase):
    def test_macro_metrics_keep_unreachable_targets_and_zero_only_users(self):
        # Literal small typed state: two evaluated users, one cold user; y is unreachable.
        state = PredictorState(
            protocol=_frozen_protocol(),
            candidate_ids=("a","b","c"),
            candidate_features=(("a",(1,0,0,0,0)),("b",(0,1,0,0,0)),("c",(0,0,1,0,0))),
            training_viewer_ids=("v1","z"), profiles=(("v1",(1,0,0,0,0),(F(1),F(0),F(0),F(0),F(0)),1),("z",(0,0,0,0,0),(F(0),)*5,0)),
            global_support=(("a",1),("b",0),("c",0)), global_order=("a","b","c"),
            global_recommendation_ids=("a","b","c"), global_fallback_status="popularity_ranked", fingerprint="0"*64)
        targets=HoldoutTargets({"v1":frozenset(("a","y")),"cold":frozenset(("b",)),"z":frozenset(("a",))},frozenset(("v1","cold","z","zero-only")),1,"available")
        from ai.streamforge.content_io import validate_predictor_state, validate_profile_evaluation
        validate_predictor_state(state)
        result=c.evaluate_profiles(state,targets)
        validate_profile_evaluation(result)
        self.assertEqual(("available",F(1,3),F(5,6)),(result.status,result.precision_at_3,result.recall_at_3))
        self.assertEqual((4,3,1,1,1,1),tuple(dict(result.counts)[x] for x in ("all_holdout_viewers","evaluated_viewers","zero_positive_viewers","candidate_unreachable_positive_pairs","cold_start_evaluated_viewers","known_zero_profile_evaluated_viewers")))
        self.assertEqual(1,dict(result.counts)["positive_profile_evaluated_viewers"])
        self.assertIn("y",result.per_viewer_metrics[1].positive_target_ids)

    def test_empty_cohort_status_precedence_and_empty_candidate_scores(self):
        empty_state=PredictorState(_frozen_protocol(),(),(),(),(),(),(),(),"id_order_no_positive_training_signal","0"*64)
        from ai.streamforge.content_io import validate_predictor_state
        validate_predictor_state(empty_state)
        no_holdout=c.evaluate_profiles(empty_state,HoldoutTargets({},frozenset(),0,"no_holdout"))
        self.assertEqual(("no_holdout",None,None),(no_holdout.status,no_holdout.precision_at_3,no_holdout.recall_at_3))
        no_positive=c.evaluate_profiles(empty_state,HoldoutTargets({},frozenset(("q",)),1,"no_positive_holdout"))
        self.assertEqual(("no_positive_holdout",None,None),(no_positive.status,no_positive.precision_at_3,no_positive.recall_at_3))
        no_candidate=c.evaluate_profiles(empty_state,HoldoutTargets({"new":frozenset(("future",))},frozenset(("new",)),0,"available"))
        self.assertEqual(("no_candidates",F(0),F(0)),(no_candidate.status,no_candidate.precision_at_3,no_candidate.recall_at_3))
        self.assertEqual((0,1,F(0),F(0)),(no_candidate.per_viewer_metrics[0].hit_count,no_candidate.per_viewer_metrics[0].positive_target_count,no_candidate.per_viewer_metrics[0].precision_at_3,no_candidate.per_viewer_metrics[0].recall_at_3))

class Day192Test(unittest.TestCase):
    def test_compare_same_cohort_with_signed_profile_minus_global_delta(self):
        from dataclasses import replace
        from ai.streamforge.content_models import QueryPrediction, ViewerMetric
        from ai.streamforge.evaluation_io import validate_evaluation
        from ai.streamforge.content_io import validate_profile_evaluation
        candidates=("a","b","c","d")
        query=QueryPrediction("viewer",True,1,"genre_profile",tuple((m,F(1) if m=="a" else F(0)) for m in candidates),candidates,("a","b","c"))
        metric=ViewerMetric("viewer",("a","x"),1,2,1,F(1,3),F(1,2))
        counts=(("all_holdout_viewers",1),("evaluated_viewers",1),("zero_positive_viewers",0),("positive_viewer_media_pairs",2),("candidate_unreachable_positive_pairs",1),("training_viewer_count",1),("cold_start_evaluated_viewers",0),("known_zero_profile_evaluated_viewers",0),("positive_profile_evaluated_viewers",1),("candidate_count",4),("returned_ids_total",3))
        modes=(("genre_profile",1),("global_cold_start",0),("global_no_positive_profile",0),("no_candidates",0))
        profile=ProfileEvaluation(candidates,(query,),(metric,),F(1,3),F(1,2),counts,modes,"available")
        validate_profile_evaluation(profile)
        # Same targets {a,x}; popularity returns b,c,d, so its true hit count is zero.
        ranked=RankedList(("b","c","d","a"),("b","c","d"),"popularity_ranked")
        global_eval=BaselineEvaluation(ranked,F(0),F(0),1,1,0,2,1,1,0,4,3,"available")
        validate_evaluation(global_eval,candidates)
        compared=c.compare_against_global(profile,global_eval)
        self.assertEqual((F(1,3),F(1,2)),(compared.precision_delta,compared.recall_delta))

        # Observable single-field mismatches with otherwise valid baseline records.
        bad_rows=(
            replace(global_eval,positive_viewer_media_pairs=3,recall_at_3=F(1,3)),
            replace(global_eval,candidate_unreachable_positive_pairs=0),
            replace(global_eval,training_viewer_count=2),
            replace(global_eval,cold_start_evaluated_viewers=1),
        )
        for bad in bad_rows:
            with self.subTest(bad=bad):
                validate_evaluation(bad,candidates)
                with self.assertRaises((ValueError,TypeError)):
                    c.compare_against_global(profile,bad)

        # Evaluated and zero-positive counts are mathematically coupled to the
        # fixed all-viewer total, so exercise their valid alternate cohort together.
        empty_cohort=replace(global_eval,evaluated_viewers=0,zero_positive_viewers=1,
            positive_viewer_media_pairs=0,candidate_unreachable_positive_pairs=0,
            cold_start_evaluated_viewers=0,precision_at_3=None,recall_at_3=None,status="no_positive_holdout")
        validate_evaluation(empty_cohort,candidates)
        with self.assertRaises((ValueError,TypeError)):
            c.compare_against_global(profile,empty_cohort)

        wrong_candidates=replace(global_eval,ranked_list=RankedList(("a","b","x"),("a","b","x"),"popularity_ranked"),candidate_count=3,returned_count=3)
        with self.assertRaises((ValueError,TypeError)):
            c.compare_against_global(profile,wrong_candidates)
        wrong_status=replace(empty_cohort,all_holdout_viewers=0,zero_positive_viewers=0,status="no_holdout")
        validate_evaluation(wrong_status,candidates)
        with self.assertRaises((ValueError,TypeError)):
            c.compare_against_global(profile,wrong_status)

    def test_non_evaluable_comparison_preserves_undefined_deltas(self):
        from ai.streamforge.evaluation_models import BaselineEvaluation, RankedList
        candidates=("a","b","c")
        counts=("all_holdout_viewers",1), ("evaluated_viewers",0), ("zero_positive_viewers",1), ("positive_viewer_media_pairs",0), ("candidate_unreachable_positive_pairs",0), ("training_viewer_count",1), ("cold_start_evaluated_viewers",0), ("known_zero_profile_evaluated_viewers",0), ("positive_profile_evaluated_viewers",0), ("candidate_count",3), ("returned_ids_total",0)
        modes=("genre_profile",0),("global_cold_start",0),("global_no_positive_profile",0),("no_candidates",0)
        profile=ProfileEvaluation(candidates,(),(),None,None,counts,modes,"no_positive_holdout")
        ranked=RankedList(candidates,candidates,"popularity_ranked")
        global_eval=BaselineEvaluation(ranked,None,None,1,0,1,0,0,1,0,3,3,"no_positive_holdout")
        result=c.compare_against_global(profile,global_eval)
        self.assertEqual((None,None,"no_positive_holdout"),(result.precision_delta,result.recall_delta,result.status))

class Day193Test(unittest.TestCase):
    def test_complete_report_matches_independent_schema_and_preserves_lineage(self):
        import hashlib, json
        lineage={"preparation_status":{"training":"insufficient_training_data","holdout":"no_holdout"},"source_sha256":{"events.csv":"a"},"training":{"content_sha256":"b"}}
        before=copy.deepcopy(lineage)
        protocol={"id":"streamforge-cutoff-genre-profile-v1","k":3,"genres":["animation","comedy","documentary","drama","science-fiction"],"window_start_utc":"2027-01-01T00:00:00Z","cutoff_utc":"2027-01-20T00:00:00Z","window_end_utc":"2027-02-01T00:00:00Z","training_signal":"distinct_positive_train_viewer_media","normalization":"l1_exact_fraction","similarity":"dot_product","tie_break":"media_id_ascending","fallback_rule":"cold_or_zero_profile_to_current_global","availability_rule":"from_lte_cutoff_lt_to","evaluation_role":"development_comparison_on_previously_inspected_synthetic_holdout","max_scored_pairs":2500}
        global_rank=RankedList((),(),"id_order_no_positive_training_signal")
        global_eval=BaselineEvaluation(global_rank,None,None,0,0,0,0,0,0,0,0,0,"no_holdout")
        counts=("v",)
        state_payload={"protocol":protocol,"candidate_ids":[],"candidate_features":{},"training_viewer_ids":[],"profiles":{},"global_popularity":{"support":{},"full_order":[],"recommendation_ids":[],"fallback_status":"id_order_no_positive_training_signal"}}
        canonical=lambda x: json.dumps(x,ensure_ascii=False,allow_nan=False,sort_keys=True,separators=(",",":" )).encode()+b"\n"
        expected_state_hash=hashlib.sha256(canonical(state_payload)).hexdigest()
        frozen_protocol=copy.deepcopy(protocol);frozen_protocol["genres"]=tuple(frozen_protocol["genres"])
        state=PredictorState(tuple(sorted(frozen_protocol.items())),(),(),(),(),(),(),(),"id_order_no_positive_training_signal",expected_state_hash)
        empty=ProfileEvaluation((),(),(),None,None,(("all_holdout_viewers",0),("evaluated_viewers",0),("zero_positive_viewers",0),("positive_viewer_media_pairs",0),("candidate_unreachable_positive_pairs",0),("training_viewer_count",0),("cold_start_evaluated_viewers",0),("known_zero_profile_evaluated_viewers",0),("positive_profile_evaluated_viewers",0),("candidate_count",0),("returned_ids_total",0)),(("genre_profile",0),("global_cold_start",0),("global_no_positive_profile",0),("no_candidates",0)),"no_holdout")
        comparison=ProfileComparison(empty,global_eval,None,None,"no_holdout")
        protocol_before=copy.deepcopy(protocol);state_before=copy.deepcopy(state);empty_before=copy.deepcopy(empty);comparison_before=copy.deepcopy(comparison)
        # Independent state digest; never call production fingerprint helpers.
        expected_global={"recommendation_ids":[],"status":"no_holdout","precision_at_3":None,"recall_at_3":None,"counts":{"all_holdout_viewers":0,"evaluated_viewers":0,"zero_positive_viewers":0,"positive_viewer_media_pairs":0,"candidate_unreachable_positive_pairs":0,"training_viewer_count":0,"cold_start_evaluated_viewers":0,"candidate_count":0,"returned_count":0}}
        expected={"schema_version":1,"protocol":protocol_before,"predictor":dict(state_payload,fingerprint=expected_state_hash),"queries":[],"evaluations":{"profile":{"status":"no_holdout","precision_at_3":None,"recall_at_3":None,"counts":dict(empty.counts),"selection_mode_counts":dict(empty.selection_mode_counts),"per_viewer_metrics":[]},"global_popularity":expected_global,"signed_deltas":{"precision_at_3":None,"recall_at_3":None},"status":"no_holdout"},"preparation":before,"limits":["The profile is an arithmetic feature aggregation, not an optimizer-trained model.","The previously inspected holdout is development evaluation, not an untouched final test.","Synthetic observed-positive overlap does not establish preference, unbiased quality, causal lift, fairness, or business success.","Single-genre features are deliberately coarse and may miss interests; scores are not calibrated preference probabilities.","Catalog schedule is assumed known at cutoff; no historical catalog snapshot is established.","No real dataset, new dependency, model optimizer, cloud resource, service, paid enrollment, or new registered fixture dataset.","Content hashes identify bytes/semantic projections, not authenticity, privacy, rights or quality.","Lists are static at cutoff; no per-event availability reranking."]}
        from ai.streamforge.content_io import validate_predictor_state, validate_profile_evaluation
        validate_predictor_state(state);validate_profile_evaluation(empty)
        report=c.assemble_profile_report(protocol,state,empty,comparison,lineage)
        self.assertEqual(before,lineage);self.assertEqual(protocol_before,protocol)
        self.assertEqual(state_before,state);self.assertEqual(empty_before,empty);self.assertEqual(comparison_before,comparison)
        self.assertEqual(expected,report)
        self.assertEqual({"schema_version","protocol","predictor","queries","evaluations","preparation","limits"},set(report))
        report["protocol"]["k"]=99;report["protocol"]["genres"][0]="mutated"
        self.assertEqual(protocol_before,protocol,"returned protocol must not alias caller protocol")
        self.assertEqual(state_before,state,"returned protocol mutation must not alias predictor state")
        report["predictor"]["protocol"]["k"]=99
        report["predictor"]["protocol"]["genres"][0]="mutated"
        self.assertEqual(protocol_before,protocol,"predictor protocol must not alias caller protocol")
        self.assertEqual(state_before,state,"predictor protocol must not alias predictor state")
        report["preparation"]["training"]["content_sha256"]="mutated"
        self.assertEqual(before,lineage)
        self.assertEqual(empty_before,empty);self.assertEqual(comparison_before,comparison)

    def test_nonempty_report_matches_independent_full_literal_before_call(self):
        from ai.streamforge.tests.test_content_cli import Day194Test, _expected_complete_report
        from ai.streamforge.content_models import QueryPrediction, ViewerMetric
        from ai.streamforge.evaluation_io import evaluation_json
        harness=Day194Test("test_actual_child_process_composition_matches_literal_oracle")
        harness.setUp()
        try:
            expected=_expected_complete_report(harness._input_bytes())
            protocol=copy.deepcopy(expected["protocol"])
            predictor=expected["predictor"];candidate_ids=tuple(predictor["candidate_ids"])
            frozen_protocol=copy.deepcopy(protocol);frozen_protocol["genres"]=tuple(frozen_protocol["genres"])
            state=PredictorState(tuple(sorted(frozen_protocol.items())),candidate_ids,
                tuple((mid,tuple(predictor["candidate_features"][mid])) for mid in candidate_ids),
                tuple(predictor["training_viewer_ids"]),
                tuple((viewer,tuple(profile["genre_counts"]),tuple(F(x) for x in profile["weights"]),profile["positive_train_pairs"])
                      for viewer,profile in sorted(predictor["profiles"].items())),
                tuple(sorted(predictor["global_popularity"]["support"].items())),
                tuple(predictor["global_popularity"]["full_order"]),tuple(predictor["global_popularity"]["recommendation_ids"]),
                predictor["global_popularity"]["fallback_status"],predictor["fingerprint"])
            parse=lambda x: F(x)
            query_rows=tuple(QueryPrediction(q["viewer_id"],q["training_seen"],q["positive_train_pairs"],q["mode"],
                tuple((mid,parse(score)) for mid,score in sorted(q["scores_by_media"].items())),tuple(q["full_order"]),tuple(q["recommendation_ids"]))
                for q in expected["queries"])
            pe=expected["evaluations"]["profile"]
            metric_rows=tuple(ViewerMetric(row["viewer_id"],tuple(row["positive_target_ids"]),row["hit_count"],row["positive_target_count"],
                row["candidate_unreachable_positive_pairs"],parse(row["precision_at_3"]),parse(row["recall_at_3"]),row["status"])
                for row in pe["per_viewer_metrics"])
            profile_eval=ProfileEvaluation(candidate_ids,query_rows,metric_rows,parse(pe["precision_at_3"]),parse(pe["recall_at_3"]),
                tuple(sorted(pe["counts"].items())),tuple(sorted(pe["selection_mode_counts"].items())),pe["status"])
            ge=expected["evaluations"]["global_popularity"];gc=ge["counts"]
            global_rank=RankedList(tuple(predictor["global_popularity"]["full_order"]),tuple(ge["recommendation_ids"]),"popularity_ranked")
            global_eval=BaselineEvaluation(global_rank,parse(ge["precision_at_3"]),parse(ge["recall_at_3"]),gc["all_holdout_viewers"],gc["evaluated_viewers"],
                gc["zero_positive_viewers"],gc["positive_viewer_media_pairs"],gc["candidate_unreachable_positive_pairs"],gc["training_viewer_count"],
                gc["cold_start_evaluated_viewers"],gc["candidate_count"],gc["returned_count"],ge["status"])
            delta=expected["evaluations"]["signed_deltas"]
            comparison=ProfileComparison(profile_eval,global_eval,parse(delta["precision_at_3"]),parse(delta["recall_at_3"]),expected["evaluations"]["status"])
            lineage=copy.deepcopy(expected["preparation"])
            before=(copy.deepcopy(protocol),copy.deepcopy(state),copy.deepcopy(profile_eval),copy.deepcopy(comparison),copy.deepcopy(lineage))
            from ai.streamforge.content_io import validate_predictor_state, validate_profile_evaluation
            from ai.streamforge.evaluation_io import validate_evaluation
            validate_predictor_state(state);validate_profile_evaluation(profile_eval);validate_evaluation(global_eval,candidate_ids)
            report=c.assemble_profile_report(protocol,state,profile_eval,comparison,lineage)
            self.assertEqual(expected,report)
            self.assertEqual(before,(protocol,state,profile_eval,comparison,lineage))
            report["preparation"]["training"]["event_count"]=-1
            self.assertEqual(before[-1],lineage)
            report["protocol"]["k"]=99;report["protocol"]["genres"][0]="mutated"
            self.assertEqual(before,(protocol,state,profile_eval,comparison,lineage),"mutating returned report must not mutate any caller input")
            report["predictor"]["protocol"]["k"]=99
            report["predictor"]["protocol"]["genres"][0]="mutated"
            self.assertEqual(before,(protocol,state,profile_eval,comparison,lineage),"predictor protocol output must not alias any caller input")
        finally:
            harness.tearDown()

if __name__ == "__main__": unittest.main()
