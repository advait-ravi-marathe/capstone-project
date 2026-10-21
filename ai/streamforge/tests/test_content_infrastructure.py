"""Default-green supplied content-profile boundary checks."""
import unittest
from dataclasses import replace
from fractions import Fraction
from ai.streamforge.content_io import ensure_work_bound, rendered_protocol, validate_ratio, validate_profile_evaluation, validate_predictor_state, LIMITS
from ai.streamforge.content_models import GENRES, MAX_SCORED_PAIRS, PROFILE_PROTOCOL_ID, ProfileEvaluation, PredictorState
from ai.streamforge.content_models import QueryPrediction, ViewerMetric

class ContentInfrastructureTest(unittest.TestCase):
    @staticmethod
    def valid_state():
        p=rendered_protocol("2027-01-01T00:00:00Z","2027-01-20T00:00:00Z","2027-02-01T00:00:00Z");p["genres"]=tuple(p["genres"])
        return PredictorState(tuple(sorted(p.items())),("a",),(("a",(1,0,0,0,0)),),("v",),
            (("v",(1,0,0,0,0),(Fraction(1),Fraction(0),Fraction(0),Fraction(0),Fraction(0)),1),),
            (("a",1),),("a",),("a",),"popularity_ranked","0"*64)
    @staticmethod
    def valid_evaluation():
        query=QueryPrediction("v",True,1,"genre_profile",(("a",Fraction(1)),),("a",),("a",))
        row=ViewerMetric("v",("a",),1,1,0,Fraction(1,3),Fraction(1))
        counts=("all_holdout_viewers",1),("evaluated_viewers",1),("zero_positive_viewers",0),("positive_viewer_media_pairs",1),("candidate_unreachable_positive_pairs",0),("training_viewer_count",1),("cold_start_evaluated_viewers",0),("known_zero_profile_evaluated_viewers",0),("positive_profile_evaluated_viewers",1),("candidate_count",1),("returned_ids_total",1)
        modes=("genre_profile",1),("global_cold_start",0),("global_no_positive_profile",0),("no_candidates",0)
        return ProfileEvaluation(("a",),(query,),(row,),Fraction(1,3),Fraction(1),counts,modes,"available")
    def test_fixed_policy_and_canonical_ratio_boundary(self):
        protocol=rendered_protocol("2027-01-01T00:00:00Z","2027-01-20T00:00:00Z","2027-02-01T00:00:00Z")
        self.assertEqual(PROFILE_PROTOCOL_ID,protocol["id"])
        self.assertEqual(3,protocol["k"])
        self.assertEqual(list(GENRES),protocol["genres"])
        self.assertEqual(8,len(LIMITS))
        for ratio in ("0/1","2/3","11/24"): validate_ratio(ratio)
        for bad in ("2/6","01/2","1/0","-1/2","1.0"):
            with self.subTest(bad=bad),self.assertRaises(ValueError):validate_ratio(bad)
    def test_exact_work_bound_rejects_without_truncation(self):
        self.assertEqual(MAX_SCORED_PAIRS,ensure_work_bound(50,50))
        with self.assertRaisesRegex(ValueError,"2550"):ensure_work_bound(51,50)
        with self.assertRaisesRegex(ValueError,"2501"):ensure_work_bound(2501,1)
        self.assertEqual(0,ensure_work_bound(2501,0))

    def test_available_evaluation_cannot_hide_null_metrics(self):
        counts=(("all_holdout_viewers",1),("evaluated_viewers",1),("zero_positive_viewers",0),("positive_viewer_media_pairs",1),("candidate_unreachable_positive_pairs",0),("training_viewer_count",1),("cold_start_evaluated_viewers",0),("known_zero_profile_evaluated_viewers",0),("positive_profile_evaluated_viewers",1),("candidate_count",1),("returned_ids_total",1))
        modes=(("genre_profile",1),("global_cold_start",0),("global_no_positive_profile",0),("no_candidates",0))
        invalid=ProfileEvaluation(("a",),(),(),None,None,counts,modes,"available")
        with self.assertRaisesRegex(ValueError,"both metrics"):
            validate_profile_evaluation(invalid)

    def test_frozen_predictor_rejects_nested_mutable_protocol_values(self):
        state=PredictorState((("genres",["animation"]),),(),(),(),(),(),(),(),"id_order_no_positive_training_signal","0"*64)
        with self.assertRaisesRegex(ValueError,"deeply immutable"):
            validate_predictor_state(state)

    def test_predictor_rejects_mutable_numeric_and_policy_defects(self):
        good=self.valid_state();validate_predictor_state(good)
        bad_states=(
            replace(good,profiles=(("v",(1,0,0,0,0),(1.0,Fraction(0),Fraction(0),Fraction(0),Fraction(0)),1),)),
            replace(good,candidate_features=(("a",[1,0,0,0,0]),)),
            replace(good,profiles=(("v",[1,0,0,0,0],(Fraction(1),Fraction(0),Fraction(0),Fraction(0),Fraction(0)),1),)),
            replace(good,global_support=(("a",-1),)),replace(good,global_support=(("a",True),)),
            replace(good,global_fallback_status="wrong-fallback"),
            replace(good,protocol=tuple((k,("wrong-protocol" if k=="id" else v)) for k,v in good.protocol)),
        )
        for bad in bad_states:
            with self.subTest(bad=bad),self.assertRaises((ValueError,TypeError)):validate_predictor_state(bad)

    def test_evaluation_rejects_cohort_identity_score_and_fraction_defects(self):
        good=self.valid_evaluation();validate_profile_evaluation(good)
        bad_values=[]
        counts=dict(good.counts)
        counts["evaluated_viewers"]=99
        bad_values.append(replace(good,counts=tuple(sorted(counts.items()))))
        counts=dict(good.counts);counts["cold_start_evaluated_viewers"]=1;counts["positive_profile_evaluated_viewers"]=0
        bad_values.append(replace(good,counts=tuple(sorted(counts.items()))))
        q=good.queries[0]
        bad_values.append(replace(good,queries=(replace(q,scores_by_media=(("a",Fraction(-1)),)),)))
        bad_values.append(replace(good,recall_at_3=0.5))
        bad_values.append(replace(good,queries=(replace(q,mode="global_cold_start"),)))
        for bad in bad_values:
            with self.subTest(bad=bad),self.assertRaises((ValueError,TypeError)):validate_profile_evaluation(bad)
