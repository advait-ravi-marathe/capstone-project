"""Learner acceptance selectors for Days 253–266; all data is synthetic."""
from __future__ import annotations
import unittest
from dataclasses import replace
from ai.grounded import enterprise_contract_253 as contract

ISSUER=contract.EXPECTED_ISSUER
AUDIENCE=contract.EXPECTED_AUDIENCE
TENANT=contract.EXPECTED_TENANT
NOW=1_800_000_000
ASSERTION=contract.Assertion(ISSUER,"user-synth-042",AUDIENCE,TENANT,("claims-readers",),
                             ("document.read",),NOW-60,NOW+600,True)
DOCUMENT=contract.Document("policy-synth-042",TENANT,"r3",("claims-readers",),True,
                           "Synthetic clause: review date is 2027-07-16.")
REQUEST=contract.AccessRequest("document.read",DOCUMENT.document_id,"r3",TENANT)
ALLOW=(True,"allowed",DOCUMENT.document_id,"policy-synth-042@r3",DOCUMENT.excerpt)
DENY=(False,)

def outcome(row):
    return (row.allowed,row.reason,row.document_id,row.citation,row.excerpt)

def decide(assertion=ASSERTION, document=DOCUMENT, request=REQUEST, now=NOW):
    return contract.evaluate_access(assertion,document,request,now=now)

def assert_denied(testcase, row, reason):
    testcase.assertEqual((False,reason,"","",""),outcome(row))

class EnterpriseContractAcceptance(unittest.TestCase):
    def test_day253_schema(self):
        self.assertEqual(ALLOW,outcome(decide()))
        invalid_claims=(replace(ASSERTION,subject=""),replace(ASSERTION,subject=42),
            replace(ASSERTION,issuer=42),replace(ASSERTION,audience=None),replace(ASSERTION,tenant=42),
            replace(ASSERTION,groups="claims-readers"),replace(ASSERTION,groups=("claims-readers",1)),
            replace(ASSERTION,scopes=("document.read",True)),replace(ASSERTION,issued_at="yesterday"),
            replace(ASSERTION,issued_at=True),replace(ASSERTION,expires_at=NOW+0.5),
            replace(ASSERTION,adapter_status=1))
        for bad in invalid_claims:
            with self.subTest(assertion=bad):
                row=decide(bad)
                self.assertIn(row.reason,{"claim_invalid","identity_untrusted"})
                self.assertEqual((False,row.reason,"","",""),outcome(row))

    def test_day254_trusted_adapter(self):
        self.assertEqual(ALLOW,outcome(decide()))
        for bad in (replace(ASSERTION,adapter_verified=False),replace(ASSERTION,adapter_verified="false"),
                    replace(ASSERTION,adapter_verified=1),replace(ASSERTION,adapter_status="unverified"),
                    replace(ASSERTION,issuer="https://evil.example.test")):
            row=decide(bad); assert_denied(self,row,"identity_untrusted")

    def test_day255_claim_window(self):
        for bad in (replace(ASSERTION,audience="https://other.example.test"),
                    replace(ASSERTION,issued_at=NOW+1),replace(ASSERTION,expires_at=NOW)):
            assert_denied(self,decide(bad),"claim_invalid")
        self.assertEqual("allowed",decide(replace(ASSERTION,issued_at=NOW)).reason)

    def test_day256_tenant_isolation(self):
        for a,d,r in ((replace(ASSERTION,tenant="tenant-maple"),DOCUMENT,REQUEST),
                    (ASSERTION,replace(DOCUMENT,tenant="tenant-maple"),REQUEST),
                    (ASSERTION,DOCUMENT,replace(REQUEST,tenant="tenant-maple"))):
            row=decide(a,d,r); assert_denied(self,row,"tenant_mismatch")

    def test_day257_permission_intersection(self):
        no_group=replace(ASSERTION,groups=("billing-readers",))
        no_scope=replace(ASSERTION,scopes=("profile.read",))
        assert_denied(self,decide(no_group),"permission_denied")
        assert_denied(self,decide(no_scope),"scope_denied")

    def test_day258_document_lifecycle(self):
        revoked=replace(DOCUMENT,active=False)
        stale=replace(REQUEST,revision="r2")
        before=(DOCUMENT,REQUEST)
        assert_denied(self,decide(document=revoked),"document_inactive")
        assert_denied(self,decide(request=stale),"revision_mismatch")
        self.assertEqual(before,(DOCUMENT,REQUEST))

    def test_day259_safe_denials(self):
        row=decide(replace(ASSERTION,subject="secret-subject",adapter_verified=False),replace(DOCUMENT,excerpt="secret-doc"))
        assert_denied(self,row,"identity_untrusted")
        self.assertNotIn("secret",repr(row))

    def test_day260_bounded_action(self):
        for action in ("document.write","*","Document.read"):
            row=decide(request=replace(REQUEST,action=action)); assert_denied(self,row,"action_denied")
        row=decide(request=replace(REQUEST,document_id="other-doc")); assert_denied(self,row,"document_not_found")

    def test_day261_adapter_failure(self):
        for status in ("unavailable","malformed","unverified"):
            row=decide(replace(ASSERTION,adapter_status=status,adapter_verified=False))
            assert_denied(self,row,"adapter_unavailable" if status=="unavailable" else "identity_untrusted")

    def test_day262_exact_render(self):
        self.assertEqual(ALLOW,outcome(decide()))
        row=decide(document=replace(DOCUMENT,excerpt="altered"))
        self.assertEqual("altered",row.excerpt)
        self.assertEqual("policy-synth-042@r3",row.citation)

    def test_day263_time_boundaries(self):
        self.assertEqual("allowed",decide(replace(ASSERTION,issued_at=NOW,expires_at=NOW+1)).reason)
        assert_denied(self,decide(now=NOW+600),"claim_invalid")
        assert_denied(self,decide(replace(ASSERTION,expires_at=NOW+contract.MAX_ASSERTION_SECONDS+1)),"claim_invalid")

    def test_day264_independent_matrix(self):
        rows=(decide(),decide(replace(ASSERTION,tenant="tenant-maple")),
              decide(replace(ASSERTION,scopes=())),decide(request=replace(REQUEST,action="document.write")),
              decide(document=replace(DOCUMENT,active=False)))
        expected=((True,"allowed",DOCUMENT.document_id,"policy-synth-042@r3",DOCUMENT.excerpt),
                  (False,"tenant_mismatch","","",""),(False,"scope_denied","","",""),
                  (False,"action_denied","","",""),(False,"document_inactive","","",""))
        self.assertEqual(expected,tuple(outcome(r) for r in rows))
        self.assertEqual(1,sum(bool(r.excerpt) for r in rows))

    def test_day265_request_isolation(self):
        other=replace(ASSERTION,tenant="tenant-maple")
        first=(decide(),decide(other)); reverse=(decide(other),decide())
        self.assertEqual(tuple(map(outcome,first)),tuple(map(outcome,reverse[::-1])))
        self.assertEqual(ALLOW,outcome(first[0])); self.assertFalse(first[1].allowed)

    def test_day266_reproducible_handoff(self):
        rows=tuple(decide(*case) for case in ((ASSERTION,DOCUMENT,REQUEST,NOW),
                    (replace(ASSERTION,tenant="tenant-maple"),DOCUMENT,REQUEST,NOW),
                    (ASSERTION,DOCUMENT,replace(REQUEST,action="document.write"),NOW),
                    (ASSERTION,replace(DOCUMENT,active=False),REQUEST,NOW)))
        expected=((True,"allowed",DOCUMENT.document_id,"policy-synth-042@r3",DOCUMENT.excerpt),
                  (False,"tenant_mismatch","","",""),(False,"action_denied","","",""),
                  (False,"document_inactive","","",""))
        actual=tuple(outcome(r) for r in rows)
        self.assertEqual(expected,actual)
        self.assertTrue(all((r.allowed or (not r.document_id and not r.citation and not r.excerpt)) for r in rows))
        self.assertEqual({"allowed":1,"denied":3,"disclosures":1},
                         {"allowed":sum(r.allowed for r in rows),"denied":sum(not r.allowed for r in rows),
                          "disclosures":sum(bool(r.excerpt) for r in rows)})
        self.assertTrue(all(x in " ".join(r.reason for r in rows) for x in ("allowed","tenant_mismatch","action_denied","document_inactive")))

if __name__=="__main__": unittest.main()
