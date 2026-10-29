"""Instructor mechanics for M2.3; all inputs synthetic and offline."""
import json, unittest
from pathlib import Path
from ai.fde_v2.m23 import learner, reference, workflow

class M23Mechanics(unittest.TestCase):
    def test_schedule_matches_accepted_budget(self):
        p=Path(__file__).parents[1]/'m23'/'draft_schedule.json'; d=json.loads(p.read_text())
        self.assertEqual([x['day'] for x in d['days']],list(range(82,103)))
        self.assertTrue(all(x['dsa_minutes']==60 and x['role_bank_minutes']==30 and x['ai_minutes']==90 for x in d['days']))
        self.assertEqual(sum(x['substantive_minutes'] for x in d['days']),1440)
        self.assertEqual(sum(x['buffer_minutes'] for x in d['days']),300)
        self.assertEqual(sum(x['visibility_minutes'] for x in d['days']),90)
        self.assertEqual(sum(x['unallocated_minutes'] for x in d['days']),60)
        self.assertTrue(all(x['substantive_minutes']+x['buffer_minutes']+x['visibility_minutes']+x['unallocated_minutes']==90 for x in d['days']))

    def test_authorization_rejects_missing_expired_and_forged_scope(self):
        good={'account_id':'123','region':'us-west-2','services':['ecs'],'model_id':'m','spend_ceiling':5,
              'currency':'USD','expires_at':20,'teardown_owner':'owner','residual_cost_acknowledged':True}
        self.assertTrue(reference.validate_authorization(good,10))
        self.assertFalse(reference.validate_authorization({},10))
        self.assertFalse(reference.validate_authorization({**good,'expires_at':10},10))
        self.assertFalse(reference.validate_authorization({**good,'residual_cost_acknowledged':'yes'},10))
        self.assertFalse(reference.validate_authorization({**good,'spend_ceiling':True},10))
        self.assertFalse(reference.validate_authorization({**good,'spend_ceiling':float('inf')},10))
        for field, value in {'account_id': True, 'region': 1, 'services': 'ecs',
                             'services_bad_member': ['ecs', 1], 'model_id': [],
                             'currency': [], 'expires_at': True, 'teardown_owner': 1}.items():
            key = 'services' if field == 'services_bad_member' else field
            with self.subTest(field=field): self.assertFalse(reference.validate_authorization({**good,key:value},10))
        with self.assertRaises(AssertionError): self.assertFalse(reference.validate_authorization(good,10))

    def test_inventory_diff_is_read_only_deterministic_and_mutation_sensitive(self):
        got=reference.diff_inventory({'web':{'v':2},'db':{'v':1}},{'web':{'v':1},'old':{'v':1}})
        self.assertEqual(got,{'create':['db'],'change':['web'],'delete':['old']})
        with self.assertRaises(AssertionError): self.assertEqual(got,{'create':[],'change':[],'delete':[]})

    def test_provider_wire_fixture_usage_missing_is_null_not_zero(self):
        # This is a local mechanics fixture only, not a Bedrock Converse receipt.
        self.assertEqual(reference.parse_model_response({'modelId':'m','outputText':'ok'},'m'),
                         {'model_id':'m','output':'ok','usage':None,'usage_status':'provider_field_unavailable'})
        with self.assertRaises(ValueError): reference.parse_model_response({'modelId':'wrong','outputText':'ok'},'m')
        with self.assertRaises(ValueError): reference.parse_model_response({'modelId':'m','outputText':'ok',
                    'usage':{'inputTokens':0,'outputTokens':-1}},'m')

    def test_live_cloud_and_model_evidence_never_self_grants_mastery(self):
        out=reference.evaluate_live_gate({})
        self.assertEqual((out['cloud_status'],out['model_status'],out['milestone_pass']),('BLOCKED','BLOCKED',False))
        with self.assertRaises(NotImplementedError): learner.evaluate_live_gate({})

    def test_runtime_preparation_routes_through_learner_and_never_calls_cloud(self):
        with self.assertRaises(NotImplementedError): workflow.prepare_live_operation({},0)
        class Reject:
            @staticmethod
            def validate_authorization(_record,_now): return False
        denied=workflow.prepare_live_operation({},0,adapter=Reject)
        self.assertFalse(denied['external_call_made']); self.assertEqual(denied['status'],'BLOCKED')
        with self.assertRaises(AssertionError): self.assertEqual(denied['status'],'PASS')

    def test_transport_is_never_constructed_implicitly_and_injected_fixture_is_review_only(self):
        good={'account_id':'123','region':'us-west-2','services':['ecs'],'model_id':'m','spend_ceiling':5,
              'currency':'USD','expires_at':20,'teardown_owner':'owner','residual_cost_acknowledged':True}
        calls=[]
        class FixtureTransport:
            def execute(self,scope): calls.append(scope); return {'fixture':'synthetic'}
        blocked=workflow.prepare_live_operation(good,10,adapter=reference)
        self.assertFalse(blocked['external_call_made']); self.assertEqual(calls,[])
        result=workflow.prepare_live_operation(good,10,adapter=reference,transport=FixtureTransport())
        self.assertEqual(result['status'],'PROVIDER_RESPONSE_REQUIRES_REVIEW')
        self.assertFalse(result['milestone_pass']); self.assertEqual(len(calls),1)

if __name__=='__main__': unittest.main()
