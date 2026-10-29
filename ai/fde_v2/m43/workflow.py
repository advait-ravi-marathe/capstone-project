"""Assistant-supplied assessment plumbing; learner evidence/attempts remain unverified."""
from . import reference as application_adapter
from . import exercises


def run_instructor_fixture_suite(root):
    """Materialize and execute A/B plus the unrelated worked-example lab.

    All outputs remain instructor mechanics; no learner attempt is created.
    """
    manifest=exercises.materialize_exercises(root)
    controls=exercises.run_reference_controls(root)
    clean_copy={key:exercises.run_clean_copy(root,key) for key in ("A","B")}
    return {"manifest":manifest,"controls":controls,"clean_copy":clean_copy,
            "learner_completion":False,"status":"INSTRUCTOR_FIXTURES_VERIFIED"}


def run_attempt_tests(root, exercise_id, *, expected_initial_sha256):
    """Run actual current source tests, routed through the selected learner seam."""
    return exercises.run_attempt_tests(root,exercise_id,
        expected_initial_sha256=expected_initial_sha256)
def assess_two_attempts(attempts, handoff, *, adapter=application_adapter):
    if not isinstance(attempts,list) or len(attempts)!=2: raise ValueError("exactly two distinct attempts are required")
    checked=[adapter.validate_attempt(a) for a in attempts]
    if {a["exercise_id"] for a in attempts}!={"A","B"}: raise ValueError("attempts must use separate exercises A and B")
    if attempts[0]["attempt_date"]==attempts[1]["attempt_date"]: raise ValueError("the two original exercises must be attempted on separate actual days")
    if (attempts[0]["initial_hash"]==attempts[1]["initial_hash"]
            or attempts[0]["patch_hash"]==attempts[1]["patch_hash"]
            or attempts[0]["root_cause"]==attempts[1]["root_cause"]):
        raise ValueError("two separate exercises need distinct source, patch and root-defect evidence")
    scored=[adapter.score_attempt(a) for a in attempts]
    return {"attempts":checked,"scores":scored,"handoff":adapter.evaluate_handoff(handoff),"status":"EXERCISE_ONLY","independent_reproduction_claim":False}
