"""Interview rehearsal path delegates scoring to the learner-owned adapter."""
from . import learner
def assess(attempts_by_gate, formal_ledger=None, *, adapter=learner):
 if not isinstance(attempts_by_gate,dict): raise ValueError("attempt mapping required")
 reports=[]
 for gate,attempts in attempts_by_gate.items():
  if gate not in {"coding_dsa","unfamiliar_repository","system_design","ai_evaluation_delivery","discovery_ambiguity","behavioral_project"}: raise ValueError("unknown gate")
  reports.append(adapter.score_gate(attempts))
 missing=sorted({"coding_dsa","unfamiliar_repository","system_design","ai_evaluation_delivery","discovery_ambiguity","behavioral_project"}-set(attempts_by_gate))
 repair=adapter.remediation(reports)
 ledger=adapter.validate_formal_assessment_ledger(formal_ledger or [])
 all_gates_pass=not missing and all(r.get('status')=='REHEARSAL_THRESHOLD_MET' for r in reports)
 overall="INCOMPLETE" if missing or not all_gates_pass or ledger['status']!="20_HOUR_TARGET_MET" else "REHEARSAL_THRESHOLD_MET"
 return {"gate_reports":reports,"remediation":repair,"missing_gates":missing,"overall_status":overall,"formal_assessment_ledger":ledger,"employer_cutoff":False}
