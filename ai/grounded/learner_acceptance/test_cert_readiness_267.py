"""Learner acceptance for the internal AWS readiness threshold; no booking/pass claim."""
from __future__ import annotations
import unittest
from ai.grounded import cert_readiness_267 as readiness

GUIDE = {"title": "AWS Certified Solutions Architect - Associate (SAA-C03)",
         "version": "SAA-C03", "checked_on": "2027-07-19",
         "domains": ["Content Domain 1: Design Secure Architectures",
                     "Content Domain 2: Design Resilient Architectures",
                     "Content Domain 3: Design High-Performing Architectures",
                     "Content Domain 4: Design Cost-Optimized Architectures"]}

def attempt(name, correct=84, total=100):
    return {"attempt_id": name, "attempted_on": "2027-07-19", "material_title": "Official practice material checked against current guide",
            "material_url": "https://aws.amazon.com/certification/certification-prep/",
            "material_type": "current_official_practice", "timed": True,
            "full_length_official": name=="official-a",
            "correct": correct, "total": total, "domain_scores": {d: 75 for d in GUIDE["domains"]},
            "missed_topics": ["storage encryption"], "reviewed_missed_topics": ["storage encryption"],
            "unstudied_domains": []}

RECORD = {"guide_title": GUIDE["title"], "guide_version": GUIDE["version"],
          "guide_checked_on": GUIDE["checked_on"],
          "full_length_official_exam_available": True,
          "full_length_availability_note": "Official full-length practice available at no additional cost for this fixture.",
          "domains": GUIDE["domains"], "attempts": [attempt("official-a"), attempt("official-b")]}

class CertificationReadinessAcceptance(unittest.TestCase):
    def test_day269_two_current_independent_results_are_only_booking_review_eligibility(self):
        result = readiness.review_readiness(RECORD, GUIDE)
        self.assertEqual("ready_for_separate_booking_review", result.status)
        self.assertEqual(2, result.assessment_count)
        self.assertEqual((), result.reason_codes)
        self.assertFalse(result.exam_passed)
        self.assertFalse(result.booking_authorized)

    def test_day269_exactly_eighty_percent_meets_internal_score_floor(self):
        row = dict(RECORD, attempts=[attempt("official-a", 80), attempt("official-b", 80)])
        result = readiness.review_readiness(row, GUIDE)
        self.assertEqual("ready_for_separate_booking_review", result.status)

    def test_day269_stale_guide_duplicate_attempt_and_bad_scores_fail_closed(self):
        cases = ((dict(RECORD, guide_version="DVA-C02"), "guide_mismatch"),
                 (dict(RECORD, guide_checked_on="2026-10-24"), "guide_mismatch"),
                 (dict(RECORD, attempts=[dict(attempt("a"),full_length_official=False), dict(attempt("b"),full_length_official=False)]), "full_length_assessment_missing"),
                 (dict(RECORD, full_length_official_exam_available="false"), "full_length_availability_unverified"),
                 (dict(RECORD, attempts=[attempt("same"), attempt("same")]), "attempt_not_distinct"),
                 (dict(RECORD, attempts=[attempt("a", 79), attempt("b")]), "assessment_below_threshold"),
                 (dict(RECORD, attempts=[attempt("a"), dict(attempt("b"),material_url="https://example.test/quiz")]), "practice_source_or_timing_unverified"),
                 (dict(RECORD, attempts=[attempt("a"), dict(attempt("b"), timed=1)]), "practice_source_or_timing_unverified"),
                 (dict(RECORD, attempts=[attempt("a"), dict(attempt("b"), reviewed_missed_topics=[])]), "misses_not_fully_reviewed"),
                 (dict(RECORD, attempts=[attempt("a"), dict(attempt("b"), domain_scores={**{d:75 for d in GUIDE["domains"]}, GUIDE["domains"][0]:69})]), "domain_below_threshold"),
                 (dict(RECORD, attempts=[attempt("a"), dict(attempt("b"), attempted_on="not-a-date")]), "assessment_date_invalid"),
                 (dict(RECORD, attempts=[attempt("a"), dict(attempt("b"), unstudied_domains=[GUIDE["domains"][0]])]), "unstudied_domains_remain"))
        for row, reason in cases:
            with self.subTest(reason=reason):
                result = readiness.review_readiness(row, GUIDE)
                self.assertEqual("not_ready", result.status)
                self.assertIn(reason, result.reason_codes)
                self.assertFalse(result.exam_passed)
                self.assertFalse(result.booking_authorized)


if __name__ == "__main__":
    unittest.main()
