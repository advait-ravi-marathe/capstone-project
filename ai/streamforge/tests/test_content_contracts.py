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
from ai.streamforge import content_contracts as c

UTC = timezone.utc
START = datetime(2027, 1, 1, tzinfo=UTC)
END = datetime(2027, 3, 1, tzinfo=UTC)
GENRES = ("animation", "comedy", "documentary", "drama", "science-fiction")

def cat(mid, genre, start=START, end=END):
    return CatalogItem(mid, "Synthetic " + mid, genre, 100, start, end)

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
        no_overlap=c.rank_for_viewer("v1",weights,counts,("d",),{"d":features["d"]},("d",))
        self.assertEqual((no_overlap.mode,no_overlap.full_order),("genre_profile",("d",)))
        empty=c.rank_for_viewer("v1",weights,counts,(),{},())
        self.assertEqual((empty.mode,empty.recommendation_ids),("no_candidates",()))

class Day190Test(unittest.TestCase):
    def test_owned_state_excludes_query_and_raw_hash_inputs(self):
        protocol={"id":"streamforge-cutoff-genre-profile-v1","k":3,"genres":["animation","comedy","documentary","drama","science-fiction"],"window_start_utc":"2027-01-01T00:00:00Z","cutoff_utc":"2027-01-20T00:00:00Z","window_end_utc":"2027-02-01T00:00:00Z","training_signal":"distinct_positive_train_viewer_media","normalization":"l1_exact_fraction","similarity":"dot_product","tie_break":"media_id_ascending","fallback_rule":"cold_or_zero_profile_to_current_global","availability_rule":"from_lte_cutoff_lt_to","evaluation_role":"development_comparison_on_previously_inspected_synthetic_holdout","max_scored_pairs":2500}
        f={"a":(0,0,0,1,0)}; counts=ProfileCounts((("v",(0,0,0,1,0),1),)); weights=ProfileWeights((("v",(F(0),F(0),F(0),F(1),F(0)),1),))
        gs=PopularityState({"a":1},False)
        state=c.assemble_predictor_state(protocol,f,counts,weights,("v",),("a",),gs,("a",))
        self.assertEqual(state.candidate_ids,("a",)); self.assertEqual(state.profiles[0][0],"v")
        self.assertTrue(state.fingerprint)
        protocol["extra"]="mutated"; protocol["genres"].append("mutated"); f["a"]=(1,0,0,0,0)
        self.assertEqual(dict(state.protocol).get("extra"),None)
        self.assertEqual(dict(state.protocol)["genres"],("animation","comedy","documentary","drama","science-fiction"))
        self.assertEqual(dict(state.candidate_features)["a"],(0,0,0,1,0))

class Day191Test(unittest.TestCase):
    def test_macro_metrics_keep_unreachable_targets_and_zero_only_users(self):
        # Literal small typed state: two evaluated users, one cold user; y is unreachable.
        state = PredictorState(
            protocol=(("id","streamforge-cutoff-genre-profile-v1"),("k",3)),
            candidate_ids=("a","b","c"),
            candidate_features=(("a",(1,0,0,0,0)),("b",(0,1,0,0,0)),("c",(0,0,1,0,0))),
            training_viewer_ids=("v1","z"), profiles=(("v1",(1,0,0,0,0),(F(1),F(0),F(0),F(0),F(0)),1),("z",(0,0,0,0,0),(F(0),)*5,0)),
            global_support=(("a",1),("b",0),("c",0)), global_order=("a","b","c"),
            global_recommendation_ids=("a","b","c"), global_fallback_status="popularity_ranked", fingerprint="state")
        targets=HoldoutTargets({"v1":frozenset(("a","y")),"cold":frozenset(("b",)),"z":frozenset(("a",))},frozenset(("v1","cold","z","zero-only")),1,"available")
        result=c.evaluate_profiles(state,targets)
        self.assertEqual(("available",F(1,3),F(5,6)),(result.status,result.precision_at_3,result.recall_at_3))
        self.assertEqual((4,3,1,1,1,1),tuple(dict(result.counts)[x] for x in ("all_holdout_viewers","evaluated_viewers","zero_positive_viewers","candidate_unreachable_positive_pairs","cold_start_evaluated_viewers","known_zero_profile_evaluated_viewers")))
        self.assertEqual(1,dict(result.counts)["positive_profile_evaluated_viewers"])
        self.assertIn("y",result.per_viewer_metrics[1].positive_target_ids)

    def test_empty_cohort_status_precedence_and_empty_candidate_scores(self):
        empty_state=PredictorState((("id","streamforge-cutoff-genre-profile-v1"),),(),(),(),(),(),(),(),"id_order_no_positive_training_signal","0"*64)
        no_holdout=c.evaluate_profiles(empty_state,HoldoutTargets({},frozenset(),0,"no_holdout"))
        self.assertEqual(("no_holdout",None,None),(no_holdout.status,no_holdout.precision_at_3,no_holdout.recall_at_3))
        no_positive=c.evaluate_profiles(empty_state,HoldoutTargets({},frozenset(("q",)),1,"no_positive_holdout"))
        self.assertEqual(("no_positive_holdout",None,None),(no_positive.status,no_positive.precision_at_3,no_positive.recall_at_3))
        no_candidate=c.evaluate_profiles(empty_state,HoldoutTargets({"new":frozenset(("future",))},frozenset(("new",)),0,"available"))
        self.assertEqual(("no_candidates",F(0),F(0)),(no_candidate.status,no_candidate.precision_at_3,no_candidate.recall_at_3))
        self.assertEqual((0,1,F(0),F(0)),(no_candidate.per_viewer_metrics[0].hit_count,no_candidate.per_viewer_metrics[0].positive_target_count,no_candidate.per_viewer_metrics[0].precision_at_3,no_candidate.per_viewer_metrics[0].recall_at_3))

class Day192Test(unittest.TestCase):
    def test_compare_same_cohort_with_signed_profile_minus_global_delta(self):
        profile=ProfileEvaluation(("a","b","c"),(),(),F(1,3),F(1,2),
            (("all_holdout_viewers",1),("evaluated_viewers",1),("zero_positive_viewers",0),("positive_viewer_media_pairs",2),("candidate_unreachable_positive_pairs",0),("training_viewer_count",1),("cold_start_evaluated_viewers",0),("known_zero_profile_evaluated_viewers",0),("positive_profile_evaluated_viewers",1),("candidate_count",3),("returned_ids_total",3)),
            (("genre_profile",1),("global_cold_start",0),("global_no_positive_profile",0),("no_candidates",0)),"available")
        ranked=RankedList(("a","b","c"),("a","b","c"),"popularity_ranked")
        global_eval=BaselineEvaluation(ranked,F(1,4),F(1,4),1,1,0,2,0,1,0,3,3,"available")
        compared=c.compare_against_global(profile,global_eval)
        self.assertEqual((F(1,12),F(1,4)),(compared.precision_delta,compared.recall_delta))
        bad=BaselineEvaluation(ranked,F(1,3),F(1,4),2,1,1,2,0,1,0,3,3,"available")
        with self.assertRaises((ValueError,TypeError)): c.compare_against_global(profile,bad)

class Day193Test(unittest.TestCase):
    def test_complete_report_matches_independent_schema_and_preserves_lineage(self):
        import hashlib, json
        from ai.streamforge.evaluation_io import evaluation_json
        from ai.streamforge.content_io import LIMITS
        lineage={"preparation_status":{"training":"insufficient_training_data","holdout":"no_holdout"},"source_sha256":{"events.csv":"a"},"training":{"content_sha256":"b"}}
        before=copy.deepcopy(lineage)
        protocol={"id":"streamforge-cutoff-genre-profile-v1","k":3,"genres":["animation","comedy","documentary","drama","science-fiction"],"window_start_utc":"2027-01-01T00:00:00Z","cutoff_utc":"2027-01-20T00:00:00Z","window_end_utc":"2027-02-01T00:00:00Z","training_signal":"distinct_positive_train_viewer_media","normalization":"l1_exact_fraction","similarity":"dot_product","tie_break":"media_id_ascending","fallback_rule":"cold_or_zero_profile_to_current_global","availability_rule":"from_lte_cutoff_lt_to","evaluation_role":"development_comparison_on_previously_inspected_synthetic_holdout","max_scored_pairs":2500}
        global_rank=RankedList((),(),"id_order_no_positive_training_signal")
        global_eval=BaselineEvaluation(global_rank,None,None,0,0,0,0,0,0,0,0,0,"no_holdout")
        counts=("v",)
        state_payload={"protocol":protocol,"candidate_ids":[],"candidate_features":{},"training_viewer_ids":[],"profiles":{},"global_popularity":{"support":{},"full_order":[],"recommendation_ids":[],"fallback_status":"id_order_no_positive_training_signal"}}
        canonical=lambda x: json.dumps(x,ensure_ascii=False,allow_nan=False,sort_keys=True,separators=(",",":" )).encode()+b"\n"
        expected_state_hash=hashlib.sha256(canonical(state_payload)).hexdigest()
        state=PredictorState(tuple(sorted(protocol.items())),(),(),(),(),(),(),(),"id_order_no_positive_training_signal",expected_state_hash)
        empty=ProfileEvaluation((),(),(),None,None,(("all_holdout_viewers",0),("evaluated_viewers",0),("zero_positive_viewers",0),("positive_viewer_media_pairs",0),("candidate_unreachable_positive_pairs",0),("training_viewer_count",0),("cold_start_evaluated_viewers",0),("known_zero_profile_evaluated_viewers",0),("positive_profile_evaluated_viewers",0),("candidate_count",0),("returned_ids_total",0)),(("genre_profile",0),("global_cold_start",0),("global_no_positive_profile",0),("no_candidates",0)),"no_holdout")
        comparison=ProfileComparison(empty,global_eval,None,None,"no_holdout")
        report=c.assemble_profile_report(protocol,state,empty,comparison,lineage)
        self.assertEqual(before,lineage)
        # Independent state digest; never call production fingerprint helpers.
        expected_global={"recommendation_ids":[],"status":"no_holdout","precision_at_3":None,"recall_at_3":None,"counts":{"all_holdout_viewers":0,"evaluated_viewers":0,"zero_positive_viewers":0,"positive_viewer_media_pairs":0,"candidate_unreachable_positive_pairs":0,"training_viewer_count":0,"cold_start_evaluated_viewers":0,"candidate_count":0,"returned_count":0}}
        expected={"schema_version":1,"protocol":protocol,"predictor":dict(state_payload,fingerprint=expected_state_hash),"queries":[],"evaluations":{"profile":{"status":"no_holdout","precision_at_3":None,"recall_at_3":None,"counts":dict(empty.counts),"selection_mode_counts":dict(empty.selection_mode_counts),"per_viewer_metrics":[]},"global_popularity":expected_global,"signed_deltas":{"precision_at_3":None,"recall_at_3":None},"status":"no_holdout"},"preparation":before,"limits":list(LIMITS)}
        self.assertEqual(expected,report)
        self.assertEqual({"schema_version","protocol","predictor","queries","evaluations","preparation","limits"},set(report))
        report["preparation"]["training"]["content_sha256"]="mutated"
        self.assertEqual(before,lineage)

if __name__ == "__main__": unittest.main()
