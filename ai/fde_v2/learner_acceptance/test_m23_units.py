"""Conditional acceptance for M2.3 learner seams; default candidate is unsolved."""
import importlib, os, unittest
C = importlib.import_module(os.environ.get('M23_CANDIDATE','ai.fde_v2.m23.learner'))

class M23LearnerAcceptance(unittest.TestCase):
    def test_authorization_fails_closed_and_expiry_is_strict(self):
        r={'account_id':'123456789012','region':'us-east-1','services':['ecs'], 'model_id':'approved-model',
           'spend_ceiling':10,'currency':'USD','expires_at':500,'teardown_owner':'learner',
           'residual_cost_acknowledged':True}
        self.assertTrue(C.validate_authorization(r,100))
        for bad in ({**r,'expires_at':100},{**r,'account_id':''},{**r,'residual_cost_acknowledged':False},
                    {k:v for k,v in r.items() if k!='spend_ceiling'}, {**r,'spend_ceiling':0}):
            with self.subTest(bad=bad): self.assertFalse(C.validate_authorization(bad,100))
        for key, value in {'account_id':True,'region':1,'services':'ecs','model_id':[],
                           'currency':[],'expires_at':True,'teardown_owner':1}.items():
            with self.subTest(field=key,value=value): self.assertFalse(C.validate_authorization({**r,key:value},100))
        self.assertFalse(C.validate_authorization({**r,'services':['ecs',1]},100))
        self.assertFalse(C.validate_authorization({**r,'spend_ceiling':float('inf')},100))

    def test_provider_response_does_not_invent_missing_usage(self):
        body={'modelId':'approved-model','outputText':'actual response','usage':{'inputTokens':2,'outputTokens':3}}
        self.assertEqual(C.parse_model_response(body,'approved-model')['usage'],body['usage'])
        self.assertIsNone(C.parse_model_response({'modelId':'approved-model','outputText':'ok'},'approved-model')['usage'])
        for bad in ({**body,'modelId':'other'},{**body,'outputText':' '},
                    {**body,'usage':{'inputTokens':-1,'outputTokens':3}}):
            with self.subTest(bad=bad), self.assertRaises(ValueError): C.parse_model_response(bad,'approved-model')

    def test_inventory_diff_and_live_gates(self):
        self.assertEqual(C.diff_inventory({'a':1,'b':2},{'a':0,'c':3}),
                         {'create':['b'],'change':['a'],'delete':['c']})
        d=C.evaluate_live_gate({})
        self.assertEqual((d['cloud_status'],d['model_status'],d['milestone_pass']),('BLOCKED','BLOCKED',False))

if __name__=='__main__': unittest.main()
