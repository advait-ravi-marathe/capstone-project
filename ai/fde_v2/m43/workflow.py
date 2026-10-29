"""Feature assessment routes through the learner adapter, never a reference fallback."""
from . import learner
def assess_two_attempts(attempts, handoff, *, adapter=learner):
    if not isinstance(attempts,list) or len(attempts)!=2: raise ValueError("exactly two distinct attempts are required")
    checked=[adapter.validate_attempt(a) for a in attempts]
    if {a["exercise_id"] for a in attempts}!={"A","B"}: raise ValueError("attempts must use separate exercises A and B")
    if attempts[0]["attempt_date"]==attempts[1]["attempt_date"]: raise ValueError("the two original exercises must be attempted on separate actual days")
    scored=[adapter.score_attempt(a) for a in attempts]
    return {"attempts":checked,"scores":scored,"handoff":adapter.evaluate_handoff(handoff),"status":"EXERCISE_ONLY","independent_reproduction_claim":False}
