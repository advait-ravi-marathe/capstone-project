"""Instructor mechanics for M2.3; all inputs synthetic and offline."""
import json, unittest
from pathlib import Path
from ai.fde_v2.m23 import learner, reference, workflow
from ai.fde_v2.m23.providers import (BedrockConverseAdapter, EcsDeploymentAdapter,
    build_converse_request, build_ecs_service_request, build_ecs_task_definition_request)


class FakeBedrockRuntime:
    def __init__(self): self.calls=[]
    def converse(self,**kwargs):
        self.calls.append(kwargs)
        return {"output":{"message":{"role":"assistant","content":[{"text":"synthetic answer"}]}},
                "usage":{"inputTokens":7,"outputTokens":3,"totalTokens":10},
                "stopReason":"end_turn","ResponseMetadata":{"RequestId":"synthetic-request"}}


class FakeEcs:
    def __init__(self): self.calls=[]
    def register_task_definition(self,**kwargs):
        self.calls.append(("register_task_definition",kwargs))
        return {"taskDefinition":{"taskDefinitionArn":"arn:aws:ecs:region:account:task-definition/demo:1"}}
    def describe_services(self,**kwargs):
        self.calls.append(("describe_services",kwargs))
        return {"services":[]}
    def describe_task_definition(self,**kwargs):
        self.calls.append(("describe_task_definition",kwargs))
        return {"taskDefinition":{"taskDefinitionArn":kwargs["taskDefinition"]}}
    def create_service(self,**kwargs):
        self.calls.append(("create_service",kwargs)); return {"service":{"status":"ACTIVE"}}
    def update_service(self,**kwargs):
        self.calls.append(("update_service",kwargs)); return {"service":{"status":"ACTIVE"}}


DEPLOYMENT={"family":"coverstory","task_role_arn":"arn:aws:iam::123:role/task",
 "execution_role_arn":"arn:aws:iam::123:role/exec","image_digest":"registry/app@sha256:"+"a"*64,
 "container_name":"api","container_port":8000,"cpu":512,"memory_mib":1024,
 "log_group":"/aws/ecs/coverstory","log_region":"us-west-2","log_prefix":"api",
 "environment":[{"name":"APP_MODE","value":"local-prototype"}],"secrets":[],
 "cluster":"approved-cluster","service_name":"coverstory","subnets":["subnet-private-a"],
 "security_groups":["sg-private-app"]}

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

    def test_malformed_truthy_authorization_never_invokes_transport(self):
        calls=[]
        class MalformedPolicy:
            @staticmethod
            def validate_authorization(_record,_now): return "false"
        class Transport:
            def execute(self,_scope): calls.append("called"); return {"fixture":True}
        out=workflow.prepare_live_operation({},10,adapter=MalformedPolicy,transport=Transport())
        self.assertEqual(out["status"],"BLOCKED")
        self.assertFalse(out["external_call_made"])
        self.assertEqual(calls,[])

    def test_bedrock_converse_sdk_request_shape_is_bounded_and_injected(self):
        expected={"modelId":"approved-model","messages":[{"role":"user","content":[{"text":"hello"}]}],
                  "inferenceConfig":{"maxTokens":32}}
        self.assertEqual(build_converse_request("approved-model","hello",32),expected)
        for args in (("","hello",32),("m"," ",32),("m","x",0),("m","x",True),("m","x",4097)):
            with self.subTest(args=args),self.assertRaises(ValueError): build_converse_request(*args)
        client=FakeBedrockRuntime()
        blocked=BedrockConverseAdapter(client,lambda _scope:True).execute(
            {"provider":"aws-bedrock-converse","model_id":"m","prompt":"x","max_tokens":2})
        self.assertEqual(blocked["status"],"BLOCKED"); self.assertEqual(client.calls,[])
        live_test_only=BedrockConverseAdapter(client,lambda _scope:True,execution_enabled=True).execute(
            {"provider":"aws-bedrock-converse","model_id":"approved-model","prompt":"hello","max_tokens":32})
        self.assertEqual(live_test_only["status"],"PROVIDER_RESPONSE_REQUIRES_REVIEW")
        self.assertEqual(client.calls,[expected])
        parsed=live_test_only["provider_result"]
        self.assertEqual(parsed["usage"],{"inputTokens":7,"outputTokens":3,"totalTokens":10})
        self.assertFalse(live_test_only["milestone_pass"])
        malformed=BedrockConverseAdapter(client,lambda _scope:"true",execution_enabled=True).execute(
            {"provider":"aws-bedrock-converse","model_id":"m","prompt":"x","max_tokens":2})
        self.assertEqual(malformed["status"],"BLOCKED"); self.assertEqual(len(client.calls),1)

    def test_ecs_deployment_plan_is_private_pinned_and_never_calls_implicitly(self):
        task=build_ecs_task_definition_request(DEPLOYMENT)
        service=build_ecs_service_request(DEPLOYMENT,"task:1")
        self.assertEqual(task["networkMode"],"awsvpc"); self.assertEqual(task["requiresCompatibilities"],["FARGATE"])
        container=task["containerDefinitions"][0]
        self.assertTrue(container["readonlyRootFilesystem"]); self.assertEqual(container["user"],"10001")
        self.assertEqual(container["image"],DEPLOYMENT["image_digest"])
        self.assertEqual(service["networkConfiguration"]["awsvpcConfiguration"]["assignPublicIp"],"DISABLED")
        for bad in ({**DEPLOYMENT,"image_digest":"registry/app:latest"},
                    {**DEPLOYMENT,"environment":[{"name":"API_TOKEN","value":"plaintext"}]}):
            with self.subTest(bad=bad),self.assertRaises(ValueError): build_ecs_task_definition_request(bad)
        with self.assertRaises(ValueError): build_ecs_service_request({**DEPLOYMENT,"security_groups":["*"]},"task:1")
        client=FakeEcs(); adapter=EcsDeploymentAdapter(client,lambda *_args:True)
        plan=adapter.plan(DEPLOYMENT)
        self.assertEqual(plan["status"],"DRY_RUN"); self.assertEqual(client.calls,[])
        denied=adapter.register_revision(DEPLOYMENT,plan["task_definition_sha256"])
        self.assertEqual(denied["status"],"BLOCKED"); self.assertEqual(client.calls,[])
        task_hash=plan["task_definition_sha256"]
        approved={"register_task_definition":task_hash}
        reviewed=EcsDeploymentAdapter(client,lambda op,_manifest,digest:approved.get(op)==digest,
                                      execution_enabled=True)
        # Fake injected SDK client only. This test performs no credential lookup or network call.
        registered=reviewed.register_revision(DEPLOYMENT,task_hash)
        self.assertEqual(registered["status"],"TASK_REVISION_REGISTERED_REQUIRES_REVIEW")
        task_arn=registered["task_definition"]
        service_request=build_ecs_service_request(DEPLOYMENT,task_arn)
        import hashlib
        import json
        service_hash=hashlib.sha256(json.dumps(service_request,sort_keys=True,separators=(",",":")).encode()).hexdigest()
        # The exact returned task ARN is bound in a fresh approval before activation.
        approved["activate_service_revision"]=service_hash
        deployed=reviewed.activate_revision(DEPLOYMENT,task_arn,service_hash)
        self.assertEqual(deployed["status"],"DEPLOYMENT_RESPONSE_REQUIRES_READBACK")
        self.assertEqual([name for name,_ in client.calls],
                         ["register_task_definition","describe_services","create_service",
                          "describe_services","describe_task_definition"])
        self.assertFalse(deployed["milestone_pass"])
        client.calls.clear()
        rollback_request=build_ecs_service_request(DEPLOYMENT,"task:previous")
        rollback_hash=hashlib.sha256(json.dumps(rollback_request,sort_keys=True,separators=(",",":")).encode()).hexdigest()
        approved["rollback"]=rollback_hash
        rollback=reviewed.rollback(DEPLOYMENT,"task:previous",rollback_hash)
        self.assertEqual(rollback["status"],"ROLLBACK_RESPONSE_REQUIRES_READBACK")
        self.assertEqual([name for name,_ in client.calls],
                         ["describe_task_definition","update_service","describe_services"])

if __name__=='__main__': unittest.main()
