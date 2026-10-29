"""Instructor mechanics for M2.3; all inputs synthetic and offline."""
import json, unittest
from concurrent.futures import ThreadPoolExecutor
from pathlib import Path
from ai.fde_v2.m23 import learner, reference, workflow
from ai.fde_v2.m23.providers import (BedrockConverseAdapter, EcsDeploymentAdapter,
    ApprovalVault, build_converse_request, build_ecs_service_request, build_ecs_create_service_request,
    build_ecs_update_service_request, build_ecs_task_definition_request,
    request_sha256, build_authorized_action)

FUTURE = 4_102_444_800
AUTH_SCOPE={"account_id":"123","region":"us-west-2","services":["ecs","bedrock-runtime"],
    "model_id":"approved-model","spend_ceiling":5,"currency":"USD","expires_at":FUTURE,
    "teardown_owner":"synthetic-owner","residual_cost_acknowledged":True}

def vault_with_clock(start=100.0, wall=1_800_000_000.0):
    clock={"mono":start,"wall":wall}
    return ApprovalVault(lambda *_args:True,monotonic_clock=lambda:clock["mono"],
                         wall_clock=lambda:clock["wall"]),clock

class FakeSts:
    def __init__(self,account="123",credentials=None):
        self.account=account; self.calls=0
        self._request_signer=type("Signer",(),{"_credentials":credentials if credentials is not None else SHARED_CREDENTIALS})()
    def get_caller_identity(self): self.calls+=1; return {"Account":self.account}

SHARED_CREDENTIALS=object()

class FakeBedrockRuntime:
    def __init__(self,credentials=None):
        self.calls=[]
        self._request_signer=type("Signer",(),{"_credentials":credentials if credentials is not None else SHARED_CREDENTIALS})()
        self.meta=type("Meta",(),{"region_name":"us-west-2",
            "service_model":type("Service",(),{"service_name":"bedrock-runtime"})()})()
    def converse(self,**kwargs):
        self.calls.append(kwargs)
        return {"output":{"message":{"role":"assistant","content":[{"text":"synthetic answer"}]}},
                "usage":{"inputTokens":7,"outputTokens":3,"totalTokens":10},
                "stopReason":"end_turn","ResponseMetadata":{"RequestId":"synthetic-request"}}


class FakeEcs:
    def __init__(self,credentials=None):
        self.calls=[]
        self._request_signer=type("Signer",(),{"_credentials":credentials if credentials is not None else SHARED_CREDENTIALS})()
        self.has_service=False
        self.desired_count=1
        self.running_count=1
        self.meta=type("Meta",(),{"region_name":"us-west-2",
            "service_model":type("Service",(),{"service_name":"ecs"})()})()
    def register_task_definition(self,**kwargs):
        self.calls.append(("register_task_definition",kwargs))
        return {"taskDefinition":{"taskDefinitionArn":"arn:aws:ecs:region:account:task-definition/demo:1"}}
    def describe_services(self,**kwargs):
        self.calls.append(("describe_services",kwargs))
        return {"services":[{"status":"ACTIVE","desiredCount":self.desired_count,
                            "runningCount":self.running_count}] if self.has_service else []}
    def describe_task_definition(self,**kwargs):
        self.calls.append(("describe_task_definition",kwargs))
        return {"taskDefinition":{"taskDefinitionArn":kwargs["taskDefinition"]}}
    def create_service(self,*,cluster,serviceName,taskDefinition,desiredCount,launchType,networkConfiguration,deploymentConfiguration):
        kwargs=locals().copy(); kwargs.pop("self")
        self.calls.append(("create_service",kwargs)); self.has_service=True
        self.desired_count=desiredCount; self.running_count=desiredCount
        return {"service":{"status":"ACTIVE"}}
    def update_service(self,*,cluster,service,taskDefinition=None,desiredCount=None,networkConfiguration=None,deploymentConfiguration=None):
        kwargs=locals().copy(); kwargs.pop("self")
        self.calls.append(("update_service",kwargs)); self.has_service=True
        if "desiredCount" in kwargs:
            self.desired_count=desiredCount
            self.running_count=0 if desiredCount==0 else desiredCount
        return {"service":{"status":"ACTIVE"}}
    def delete_service(self,*,cluster,service,force):
        kwargs=locals().copy(); kwargs.pop("self")
        self.calls.append(("delete_service",kwargs)); self.has_service=False
        return {"service":{"status":"INACTIVE"}}


DEPLOYMENT={"family":"coverstory","task_role_arn":"arn:aws:iam::123:role/task",
 "execution_role_arn":"arn:aws:iam::123:role/exec","image_digest":"registry/app@sha256:"+"a"*64,
 "container_name":"api","container_port":8000,"cpu":512,"memory_mib":1024,
 "log_group":"/aws/ecs/coverstory","log_region":"us-west-2","log_prefix":"api",
 "environment":[{"name":"APP_MODE","value":"local-prototype"}],"secrets":[],
 "cluster":"approved-cluster","service_name":"coverstory","subnets":["subnet-private-a"],
 "security_groups":["sg-private-app"],"authorization_scope":AUTH_SCOPE}

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
        good={**AUTH_SCOPE,'expires_at':FUTURE}
        self.assertTrue(reference.validate_authorization(good,1_800_000_000))
        self.assertFalse(reference.validate_authorization({},10))
        self.assertFalse(reference.validate_authorization({**good,'expires_at':1_800_000_000},1_800_000_000))
        self.assertFalse(reference.validate_authorization({**good,'residual_cost_acknowledged':'yes'},1_800_000_000))
        self.assertFalse(reference.validate_authorization({**good,'spend_ceiling':True},1_800_000_000))
        self.assertFalse(reference.validate_authorization({**good,'spend_ceiling':float('inf')},1_800_000_000))
        for field, value in {'account_id': True, 'region': 1, 'services': 'ecs',
                             'services_bad_member': ['ecs', 1], 'model_id': [],
                             'currency': [], 'expires_at': True, 'teardown_owner': 1}.items():
            key = 'services' if field == 'services_bad_member' else field
            with self.subTest(field=field): self.assertFalse(reference.validate_authorization({**good,key:value},1_800_000_000))
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
        good={**AUTH_SCOPE,"services":["bedrock-runtime"],"model_id":"m"}
        calls=[]
        class FixtureTransport:
            def execute(self,scope): calls.append(scope); return {'fixture':'synthetic'}
        blocked=workflow.prepare_live_operation(good,10,adapter=reference)
        self.assertFalse(blocked['external_call_made']); self.assertEqual(calls,[])
        unsupported=workflow.prepare_live_operation(good,10,adapter=reference,transport=FixtureTransport())
        self.assertEqual(unsupported['status'],'BLOCKED'); self.assertEqual(calls,[])
        request=build_converse_request('m','x',2)
        vault,_=vault_with_clock()
        action=build_authorized_action("bedrock_converse","aws-bedrock-converse","bedrock-runtime",good,request,"converse")
        token=vault.issue('bedrock_converse',action)
        scope={**good,'provider':'aws-bedrock-converse','prompt':'x','max_tokens':2,'approval_token':token}
        provider=BedrockConverseAdapter(FakeBedrockRuntime(),vault,FakeSts(),execution_enabled=True)
        result=workflow.prepare_live_operation(scope,10,adapter=reference,transport=provider)
        self.assertEqual(result['status'],'PROVIDER_RESPONSE_REQUIRES_REVIEW')
        self.assertFalse(result['milestone_pass']); self.assertTrue(result['external_call_made'])

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
        vault,_=vault_with_clock()
        scope={**AUTH_SCOPE,"services":["bedrock-runtime"],"provider":"aws-bedrock-converse",
               "model_id":"approved-model","prompt":"hello","max_tokens":32}
        action=build_authorized_action("bedrock_converse","aws-bedrock-converse","bedrock-runtime",scope,expected,"converse")
        token=vault.issue("bedrock_converse",action)
        scope["approval_token"]=token
        blocked=BedrockConverseAdapter(client,vault,FakeSts()).execute(scope)
        self.assertEqual(blocked["status"],"BLOCKED"); self.assertEqual(client.calls,[])
        sts=FakeSts()
        live_test_only=BedrockConverseAdapter(client,vault,sts,execution_enabled=True).execute(scope)
        self.assertEqual(live_test_only["status"],"PROVIDER_RESPONSE_REQUIRES_REVIEW")
        self.assertEqual(client.calls,[expected])
        parsed=live_test_only["provider_result"]
        self.assertEqual(parsed["usage"],{"inputTokens":7,"outputTokens":3,"totalTokens":10})
        self.assertFalse(live_test_only["milestone_pass"])
        malformed_vault,_=vault_with_clock()
        malformed_vault._approval_event=lambda *_args:"true"
        self.assertIsNone(malformed_vault.issue("bedrock_converse",action))
        malformed=BedrockConverseAdapter(client,malformed_vault,FakeSts(),execution_enabled=True).execute(
            {**scope,"approval_token":"fabricated"})
        self.assertEqual(malformed["status"],"BLOCKED"); self.assertEqual(len(client.calls),1)
        replay=BedrockConverseAdapter(client,vault,FakeSts(),execution_enabled=True).execute(scope)
        self.assertEqual(replay["status"],"BLOCKED"); self.assertEqual(len(client.calls),1)

    def test_provider_client_must_share_exact_sdk_credentials_with_sts(self):
        provider_creds=object()
        client=FakeBedrockRuntime(provider_creds)
        sts=FakeSts("123",credentials=object())
        matches,called=__import__("ai.fde_v2.m23.providers",fromlist=["verify_injected_client_scope"]).verify_injected_client_scope(
            client,sts,AUTH_SCOPE,"bedrock-runtime")
        self.assertFalse(matches)
        self.assertFalse(called)
        self.assertEqual(sts.calls,0)

    def test_approval_binds_account_region_model_and_vault_owned_expiry(self):
        request=build_converse_request("approved-model","hello",32)
        scope={**AUTH_SCOPE,"services":["bedrock-runtime"]}
        action=build_authorized_action("bedrock_converse","aws-bedrock-converse","bedrock-runtime",scope,request,"converse")
        vault,clock=vault_with_clock()
        token=vault.issue("bedrock_converse",action,ttl_seconds=30)
        self.assertIsNotNone(token)
        self.assertNotEqual(request_sha256(action),request_sha256({**action,"authorization_scope":{
            **action["authorization_scope"],"account_id":"999"}}))
        # The caller cannot extend expiry by supplying a future timestamp.
        with self.assertRaises(TypeError): vault.issue("bedrock_converse",action,now=clock["mono"]+1_000_000)
        clock["mono"]+=30
        self.assertFalse(vault.consume(token,"bedrock_converse",request_sha256(action)))

        # A separately approved action cannot be sent through a client pointed
        # at another region/account. All clients here are inert local fakes.
        vault,_=vault_with_clock()
        token=vault.issue("bedrock_converse",action)
        other_region=FakeBedrockRuntime()
        other_region.meta.region_name="us-east-1"
        sts=FakeSts()
        result=BedrockConverseAdapter(other_region,vault,sts,execution_enabled=True).execute(
            {**scope,"provider":"aws-bedrock-converse","model_id":"approved-model","prompt":"hello",
             "max_tokens":32,"approval_token":token})
        self.assertEqual(result["status"],"BLOCKED_CLIENT_SCOPE_MISMATCH")
        self.assertEqual(other_region.calls,[]); self.assertEqual(sts.calls,0)

        vault,_=vault_with_clock()
        token=vault.issue("bedrock_converse",action)
        sts=FakeSts("999")
        client=FakeBedrockRuntime()
        result=BedrockConverseAdapter(client,vault,sts,execution_enabled=True).execute(
            {**scope,"provider":"aws-bedrock-converse","model_id":"approved-model","prompt":"hello",
             "max_tokens":32,"approval_token":token})
        self.assertEqual(result["status"],"BLOCKED_CLIENT_SCOPE_MISMATCH")
        self.assertEqual(client.calls,[]); self.assertEqual(sts.calls,1)
        with self.assertRaises(ValueError):
            build_authorized_action("bedrock_converse","aws-bedrock-converse","bedrock-runtime",
                                    {**scope,"model_id":"different-model"},request,"converse")

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
        client=FakeEcs(); vault,_=vault_with_clock()
        adapter=EcsDeploymentAdapter(client,vault,FakeSts())
        plan=adapter.plan(DEPLOYMENT)
        self.assertEqual(plan["status"],"DRY_RUN"); self.assertEqual(client.calls,[])
        denied=adapter.register_revision(DEPLOYMENT)
        self.assertEqual(denied["status"],"BLOCKED"); self.assertEqual(client.calls,[])
        digest_alone=adapter.register_revision(DEPLOYMENT,plan["task_definition_sha256"])
        self.assertEqual(digest_alone["status"],"BLOCKED")
        self.assertEqual(client.calls,[])
        task_hash=plan["task_definition_sha256"]
        reviewed=EcsDeploymentAdapter(client,vault,FakeSts(),execution_enabled=True)
        # Fake injected SDK client only. This test performs no credential lookup or network call.
        task_action=build_authorized_action("register_task_definition","aws-ecs-fargate","ecs",AUTH_SCOPE,
                                             plan["task_definition_request"],"register_task_definition")
        wrong_region_client=FakeEcs(); wrong_region_client.meta.region_name="us-east-1"
        wrong_region_sts=FakeSts()
        wrong_region_vault,_=vault_with_clock()
        wrong_region_token=wrong_region_vault.issue("register_task_definition",task_action)
        wrong_region_result=EcsDeploymentAdapter(wrong_region_client,wrong_region_vault,wrong_region_sts,
                                                  execution_enabled=True).register_revision(DEPLOYMENT,wrong_region_token)
        self.assertEqual(wrong_region_result["status"],"BLOCKED_CLIENT_SCOPE_MISMATCH")
        self.assertFalse(wrong_region_result["external_call_made"])
        self.assertEqual(wrong_region_sts.calls,0); self.assertEqual(wrong_region_client.calls,[])
        wrong_account_client=FakeEcs(); wrong_account_sts=FakeSts("999")
        wrong_account_vault,_=vault_with_clock()
        wrong_account_token=wrong_account_vault.issue("register_task_definition",task_action)
        wrong_account_result=EcsDeploymentAdapter(wrong_account_client,wrong_account_vault,wrong_account_sts,
                                                  execution_enabled=True).register_revision(DEPLOYMENT,wrong_account_token)
        self.assertEqual(wrong_account_result["status"],"BLOCKED_CLIENT_SCOPE_MISMATCH")
        self.assertTrue(wrong_account_result["external_call_made"])
        self.assertEqual(wrong_account_sts.calls,1); self.assertEqual(wrong_account_client.calls,[])
        task_token=vault.issue("register_task_definition",task_action)
        registered=reviewed.register_revision(DEPLOYMENT,task_token)
        self.assertEqual(registered["status"],"TASK_REVISION_REGISTERED_REQUIRES_REVIEW")
        task_arn=registered["task_definition"]
        service_request=build_ecs_create_service_request(DEPLOYMENT,task_arn)
        # The exact returned task ARN is bound in a fresh approval before activation.
        service_action=build_authorized_action("create_service","aws-ecs-fargate","ecs",AUTH_SCOPE,
                                                service_request,"create_service")
        service_token=vault.issue("create_service",service_action)
        deployed=reviewed.activate_revision(DEPLOYMENT,task_arn,service_token)
        self.assertEqual(deployed["status"],"DEPLOYMENT_RESPONSE_REQUIRES_READBACK")
        self.assertEqual([name for name,_ in client.calls],
                         ["register_task_definition","describe_services","create_service",
                          "describe_services","describe_task_definition"])
        self.assertFalse(deployed["milestone_pass"])
        client.calls.clear()
        rollback_request=build_ecs_update_service_request(DEPLOYMENT,"task:previous")
        rollback_action=build_authorized_action("rollback","aws-ecs-fargate","ecs",AUTH_SCOPE,rollback_request,"update_service")
        rollback_token=vault.issue("rollback",rollback_action)
        rollback=reviewed.rollback(DEPLOYMENT,"task:previous",rollback_token)
        self.assertEqual(rollback["status"],"ROLLBACK_RESPONSE_REQUIRES_READBACK")
        self.assertEqual([name for name,_ in client.calls],
                         ["describe_task_definition","update_service","describe_services"])

    def test_ecs_create_and_update_requests_are_distinct_sdk_shapes_and_bound(self):
        created=build_ecs_create_service_request(DEPLOYMENT,"task:2")
        updated=build_ecs_update_service_request(DEPLOYMENT,"task:2")
        self.assertIn("serviceName",created); self.assertIn("launchType",created)
        self.assertNotIn("service",created)
        self.assertIn("service",updated); self.assertNotIn("serviceName",updated)
        self.assertNotIn("launchType",updated)
        client=FakeEcs(); client.has_service=True
        vault,_=vault_with_clock()
        action=build_authorized_action("update_service","aws-ecs-fargate","ecs",AUTH_SCOPE,
                                       updated,"update_service")
        token=vault.issue("update_service",action)
        adapter=EcsDeploymentAdapter(client,vault,FakeSts(),execution_enabled=True)
        result=adapter.activate_revision({**DEPLOYMENT,"service_operation":"update"},"task:2",token)
        self.assertEqual(result["operation"],"updated")
        update_call=[kwargs for name,kwargs in client.calls if name=="update_service"]
        self.assertEqual(update_call,[updated])
        self.assertNotIn("launchType",update_call[0])

    def test_ecs_teardown_requires_two_exact_approvals_and_readback(self):
        client=FakeEcs(); client.has_service=True
        vault,_=vault_with_clock()
        adapter=EcsDeploymentAdapter(client,vault,FakeSts(),execution_enabled=True)
        drain_request={"cluster":DEPLOYMENT["cluster"],"service":DEPLOYMENT["service_name"],"desiredCount":0}
        drain_action=build_authorized_action("drain_service","aws-ecs-fargate","ecs",AUTH_SCOPE,
                                             drain_request,"update_service")
        drain_token=vault.issue("drain_service",drain_action)
        drained=adapter.drain_service(DEPLOYMENT,drain_token)
        self.assertEqual(drained["status"],"DRAINED_REQUIRES_REVIEW")
        delete_request={"cluster":DEPLOYMENT["cluster"],"service":DEPLOYMENT["service_name"],"force":False}
        delete_action=build_authorized_action("delete_service","aws-ecs-fargate","ecs",AUTH_SCOPE,
                                               delete_request,"delete_service")
        delete_token=vault.issue("delete_service",delete_action)
        deleted=adapter.delete_drained_service(DEPLOYMENT,delete_token)
        self.assertEqual(deleted["status"],"SERVICE_REMOVAL_REQUIRES_REVIEW")
        self.assertFalse(deleted["milestone_pass"])
        self.assertEqual([name for name,_ in client.calls],
                         ["describe_services","update_service","describe_services",
                          "describe_services","delete_service","describe_services"])
        not_drained=FakeEcs(); not_drained.has_service=True
        vault,_=vault_with_clock()
        token=vault.issue("delete_service",delete_action)
        blocked=EcsDeploymentAdapter(not_drained,vault,FakeSts(),execution_enabled=True).delete_drained_service(
            DEPLOYMENT,token)
        self.assertEqual(blocked["status"],"BLOCKED_SERVICE_NOT_DRAINED")
        self.assertFalse(any(name=="delete_service" for name,_ in not_drained.calls))

    def test_approval_vault_is_exact_expiring_and_one_use(self):
        vault,clock=vault_with_clock()
        request={"target":"service-a","authorization_scope":AUTH_SCOPE}; digest=request_sha256(request)
        token=vault.issue("deploy",request,ttl_seconds=5)
        self.assertTrue(vault.consume(token,"deploy",digest))
        self.assertFalse(vault.consume(token,"deploy",digest))
        wrong=vault.issue("deploy",{"target":"service-b","authorization_scope":AUTH_SCOPE},ttl_seconds=5)
        self.assertFalse(vault.consume(wrong,"deploy",digest))
        expired=vault.issue("deploy",request,ttl_seconds=2)
        clock["mono"]+=2
        self.assertFalse(vault.consume(expired,"deploy",digest))
        with self.assertRaises(TypeError): vault.issue("deploy",request,now=clock["mono"]+10_000)
        one_use=vault.issue("deploy",request,ttl_seconds=5)
        with ThreadPoolExecutor(max_workers=8) as pool:
            consumed=list(pool.map(lambda _index:vault.consume(one_use,"deploy",digest),range(8)))
        self.assertEqual(sum(consumed),1)
        invalid_wall=ApprovalVault(lambda *_args:True,wall_clock=lambda:float("nan"))
        self.assertIsNone(invalid_wall.issue("deploy",request))
        invalid_monotonic=ApprovalVault(lambda *_args:True,wall_clock=lambda:1_800_000_000,
                                        monotonic_clock=lambda:float("inf"))
        self.assertIsNone(invalid_monotonic.issue("deploy",request))

if __name__=='__main__': unittest.main()
