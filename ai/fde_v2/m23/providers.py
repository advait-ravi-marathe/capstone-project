"""Fail-closed AWS provider adapters; clients are always injected by callers.

The module never imports boto3, discovers credentials, constructs a session,
creates IAM permissions, provisions resources, or performs a request at import.
A deployment owner must supply an SDK client and an exact-scope authorizer after
separate user authorization. Pure request builders are safe for dry-run tests.
"""
from __future__ import annotations

import hashlib
import json
import math
import secrets
import threading
import time
from collections.abc import Callable, Mapping
from typing import Any, Protocol


class BedrockRuntimeClient(Protocol):
    def converse(self, **kwargs: Any) -> Mapping[str, Any]: ...


class EcsClient(Protocol):
    def register_task_definition(self, **kwargs: Any) -> Mapping[str, Any]: ...
    def describe_services(self, **kwargs: Any) -> Mapping[str, Any]: ...
    def describe_task_definition(self, **kwargs: Any) -> Mapping[str, Any]: ...
    def create_service(self, **kwargs: Any) -> Mapping[str, Any]: ...
    def update_service(self, **kwargs: Any) -> Mapping[str, Any]: ...


class StsIdentityClient(Protocol):
    def get_caller_identity(self) -> Mapping[str, Any]: ...


class ApprovalVault:
    """In-memory, one-use approval records issued only by a trusted event hook.

    The approval callback is the trust boundary and must be connected to an
    actual human-controlled UI by an integrating application. A boolean or
    request digest from the model/caller is never sufficient by itself.
    """
    def __init__(self, approval_event: Callable[[str, str, Mapping[str, Any]], bool],
                 *, monotonic_clock: Callable[[], float] = time.monotonic,
                 wall_clock: Callable[[], float] = time.time):
        self._approval_event = approval_event
        self._monotonic_clock = monotonic_clock
        self._wall_clock = wall_clock
        self._records: dict[str, tuple[str, str, float]] = {}
        self._lock = threading.Lock()

    def issue(self, operation: str, request: Mapping[str, Any],
              *, ttl_seconds: int = 120) -> str | None:
        if (not isinstance(operation,str) or not operation.strip()
                or not isinstance(request,Mapping)
                or type(ttl_seconds) is not int or not 1<=ttl_seconds<=300):
            return None
        try:
            digest=request_sha256(request)
            exact_request=json.loads(json.dumps(request,sort_keys=True,separators=(",",":"),allow_nan=False))
        except (TypeError,ValueError):
            return None
        wall_now=self._wall_clock()
        if type(wall_now) not in (int,float) or not math.isfinite(float(wall_now)):
            return None
        scope=exact_request.get("authorization_scope")
        if not isinstance(scope,Mapping):
            return None
        expires_at=scope.get("expires_at")
        if type(expires_at) not in (int,float) or not math.isfinite(float(expires_at)) or expires_at<=wall_now:
            return None
        ttl_seconds=min(ttl_seconds,int(expires_at-wall_now))
        if ttl_seconds<1:
            return None
        monotonic_now=self._monotonic_clock()
        if type(monotonic_now) not in (int,float) or not math.isfinite(float(monotonic_now)):
            return None
        try:
            if self._approval_event(operation,digest,exact_request) is not True:
                return None
        except Exception:
            return None
        token=secrets.token_urlsafe(32)
        with self._lock:
            self._records[token]=(operation,digest,float(monotonic_now)+ttl_seconds)
        return token

    def consume(self, token: str | None, operation: str, request_sha256: str) -> bool:
        # Pop before validating so a malformed attempt cannot reuse a token.
        with self._lock:
            record=self._records.pop(token,None) if isinstance(token,str) else None
        now=self._monotonic_clock()
        return (record is not None and record[0]==operation and record[1]==request_sha256
                and type(now) in (int,float) and math.isfinite(float(now)) and now < record[2])


def request_sha256(request: Mapping[str, Any]) -> str:
    """Canonical hash for the exact structured SDK call arguments."""
    encoded=json.dumps(request,sort_keys=True,separators=(",",":"),ensure_ascii=False,allow_nan=False).encode("utf-8")
    return hashlib.sha256(encoded).hexdigest()


AUTHORIZATION_FIELDS=("account_id","region","services","model_id","spend_ceiling","currency",
                      "expires_at","teardown_owner","residual_cost_acknowledged")


def build_authorized_action(operation: str, provider: str, sdk_service: str,
                            scope: Mapping[str, Any], sdk_request: Mapping[str, Any]) -> dict[str, Any]:
    """Bind the exact SDK call to provider, account, region and spend authority."""
    if not isinstance(scope,Mapping) or not isinstance(sdk_request,Mapping):
        raise ValueError("scope and SDK request must be mappings")
    auth={key:scope.get(key) for key in AUTHORIZATION_FIELDS}
    services=auth["services"]
    if isinstance(services,(list,tuple,set,frozenset)) and all(isinstance(item,str) for item in services):
        services=sorted(set(services))
        auth["services"]=services
    if (not isinstance(operation,str) or not operation or not isinstance(provider,str) or not provider
            or not isinstance(sdk_service,str) or not sdk_service
            or not isinstance(auth["account_id"],str) or not auth["account_id"].strip()
            or not isinstance(auth["region"],str) or not auth["region"].strip()
            or not isinstance(services,(list,tuple,set,frozenset)) or sdk_service not in services
            or not services or any(not isinstance(item,str) or not item.strip() for item in services)
            or not isinstance(auth["model_id"],str) or not auth["model_id"].strip()
            or type(auth["spend_ceiling"]) not in (int,float) or not math.isfinite(float(auth["spend_ceiling"]))
            or auth["spend_ceiling"]<=0 or auth["currency"] not in {"USD","CAD","EUR","GBP"}
            or type(auth["expires_at"]) not in (int,float) or not isinstance(auth["teardown_owner"],str)
            or not auth["teardown_owner"].strip() or auth["residual_cost_acknowledged"] is not True):
        raise ValueError("complete provider/account/region/spend authorization scope is required")
    if sdk_service == "bedrock-runtime" and sdk_request.get("modelId") != auth["model_id"]:
        raise ValueError("Bedrock request model must match the approved model scope")
    return {"operation":operation,"provider":provider,"sdk_service":sdk_service,
            "authorization_scope":auth,"sdk_request":dict(sdk_request)}


def verify_injected_client_scope(client: Any, identity_client: StsIdentityClient | None,
                                scope: Mapping[str, Any], sdk_service: str) -> tuple[bool,bool]:
    """Check client service/region and read back caller account through injected STS.

    Returns (matches, identity_request_made). The caller must first consume the
    exact approval token before invoking this verifier.
    """
    meta=getattr(client,"meta",None)
    service_model=getattr(meta,"service_model",None)
    if (meta is None or getattr(meta,"region_name",None)!=scope.get("region")
            or getattr(service_model,"service_name",None)!=sdk_service or identity_client is None):
        return False,False
    try:
        identity=identity_client.get_caller_identity()
    except Exception:
        return False,True
    account=identity.get("Account") if isinstance(identity,Mapping) else None
    return account==scope.get("account_id"),True


def build_converse_request(model_id: str, prompt: str, max_tokens: int) -> dict[str, Any]:
    """Build the documented Bedrock Runtime Converse kwargs without invoking it."""
    if not isinstance(model_id, str) or not model_id.strip() or len(model_id) > 2048:
        raise ValueError("model_id must be a bounded nonempty string")
    if not isinstance(prompt, str) or not prompt.strip() or len(prompt.encode("utf-8")) > 32_000:
        raise ValueError("prompt must be nonblank and at most 32 KB")
    if type(max_tokens) is not int or not 1 <= max_tokens <= 4096:
        raise ValueError("max_tokens must be an integer from 1 through 4096")
    return {
        "modelId": model_id,
        "messages": [{"role": "user", "content": [{"text": prompt}]}],
        "inferenceConfig": {"maxTokens": max_tokens},
    }


def parse_converse_response(response: Mapping[str, Any], model_id: str) -> dict[str, Any]:
    """Parse documented Converse output; absent usage remains unknown (None)."""
    if not isinstance(response, Mapping):
        raise ValueError("provider response must be a mapping")
    output = response.get("output")
    message = output.get("message") if isinstance(output, Mapping) else None
    content = message.get("content") if isinstance(message, Mapping) else None
    if not isinstance(content, list):
        raise ValueError("provider response has no output message content")
    pieces = []
    for block in content:
        if not isinstance(block, Mapping):
            raise ValueError("provider content blocks must be mappings")
        if "text" in block:
            if not isinstance(block["text"], str):
                raise ValueError("text content must be a string")
            pieces.append(block["text"])
    text = "".join(pieces)
    if not text.strip() or len(text) > 20_000:
        raise ValueError("provider text is missing or oversized")
    usage = response.get("usage")
    if usage is not None:
        if not isinstance(usage, Mapping):
            raise ValueError("provider usage must be a mapping")
        for field in ("inputTokens", "outputTokens"):
            value = usage.get(field)
            if type(value) is not int or value < 0:
                raise ValueError("provider token usage is invalid")
        usage_value = {key: usage[key] for key in ("inputTokens", "outputTokens", "totalTokens") if key in usage}
    else:
        usage_value = None
    metadata = response.get("ResponseMetadata")
    request_id = metadata.get("RequestId") if isinstance(metadata, Mapping) else None
    return {"model_id": model_id, "output": text, "usage": usage_value,
            "usage_status": "observed" if usage_value is not None else "provider_field_unavailable",
            "request_id": request_id if isinstance(request_id, str) and request_id else None,
            "stop_reason": response.get("stopReason") if isinstance(response.get("stopReason"), str) else None}


class BedrockConverseAdapter:
    """Optional live adapter. Default construction cannot execute a request."""
    def __init__(self, client: BedrockRuntimeClient | None = None,
                 approval_vault: ApprovalVault | None = None,
                 identity_client: StsIdentityClient | None = None,
                 *, execution_enabled: bool = False):
        self._client = client
        self._approval_vault = approval_vault
        self._identity_client = identity_client
        self._execution_enabled = execution_enabled

    def execute(self, scope: Mapping[str, Any]) -> dict[str, Any]:
        if (self._execution_enabled is not True or self._client is None or self._approval_vault is None
                or self._identity_client is None):
            return {"status": "BLOCKED", "reason": "Bedrock/STS clients and separately authorized live gate are required",
                    "external_call_made": False}
        if scope.get("provider") != "aws-bedrock-converse":
            return {"status": "BLOCKED", "reason": "provider scope mismatch", "external_call_made": False}
        request = build_converse_request(scope.get("model_id"), scope.get("prompt"), scope.get("max_tokens"))
        action=build_authorized_action("bedrock_converse","aws-bedrock-converse","bedrock-runtime",scope,request)
        digest=request_sha256(action)
        if not self._approval_vault.consume(scope.get("approval_token"),"bedrock_converse",digest):
            return {"status":"BLOCKED","reason":"missing, mismatched, expired, or replayed approval",
                    "external_call_made":False}
        matches,identity_call=verify_injected_client_scope(self._client,self._identity_client,scope,"bedrock-runtime")
        if not matches:
            return {"status":"BLOCKED_CLIENT_SCOPE_MISMATCH","reason":"client service/region/account did not match approved scope",
                    "identity_request_made":identity_call,"external_call_made":identity_call,
                    "provider_invocation_made":False,"milestone_pass":False}
        response = self._client.converse(**request)
        parsed = parse_converse_response(response, request["modelId"])
        return {"status": "PROVIDER_RESPONSE_REQUIRES_REVIEW", "external_call_made": True,
                "identity_request_made":True,"provider_invocation_made":True,
                "provider_result": parsed, "milestone_pass": False}


def _bounded_positive_int(value: Any, name: str, maximum: int) -> int:
    if type(value) is not int or not 1 <= value <= maximum:
        raise ValueError(f"{name} must be an integer in [1,{maximum}]")
    return value


def build_ecs_task_definition_request(manifest: Mapping[str, Any]) -> dict[str, Any]:
    """Create dry-run SDK kwargs for one pinned, non-root Fargate task revision."""
    if not isinstance(manifest, Mapping):
        raise ValueError("deployment manifest must be a mapping")
    required = {"family", "task_role_arn", "execution_role_arn", "image_digest", "container_name",
                "container_port", "cpu", "memory_mib", "log_group", "log_region", "log_prefix"}
    if not required.issubset(manifest):
        raise ValueError("deployment manifest is incomplete")
    family, name = manifest["family"], manifest["container_name"]
    role_arns = (manifest["task_role_arn"], manifest["execution_role_arn"])
    image = manifest["image_digest"]
    if not isinstance(family, str) or not family or len(family) > 255:
        raise ValueError("invalid ECS task family")
    if not isinstance(name, str) or not name or len(name) > 255:
        raise ValueError("invalid container name")
    if any(not isinstance(arn, str) or not arn.startswith("arn:") or "*" in arn for arn in role_arns):
        raise ValueError("explicit non-wildcard task/execution role ARNs are required")
    if not isinstance(image, str) or "@sha256:" not in image or len(image.rsplit("@sha256:",1)[-1]) != 64:
        raise ValueError("container image must be pinned by sha256 digest")
    port = _bounded_positive_int(manifest["container_port"], "container_port", 65535)
    cpu = _bounded_positive_int(manifest["cpu"], "cpu", 4096)
    memory = _bounded_positive_int(manifest["memory_mib"], "memory_mib", 30720)
    for key in ("log_group", "log_region", "log_prefix"):
        if not isinstance(manifest[key], str) or not manifest[key].strip():
            raise ValueError(f"{key} is required")
    environment = manifest.get("environment", [])
    if not isinstance(environment, list) or any(not isinstance(row, Mapping) or set(row) != {"name", "value"}
                                                  or not all(isinstance(row[k], str) for k in ("name", "value"))
                                                  for row in environment):
        raise ValueError("environment must contain only explicit nonsecret name/value rows")
    if any(any(token in row["name"].lower() for token in ("password", "secret", "token", "credential", "access_key"))
           for row in environment):
        raise ValueError("secret-like values must use explicit secret references")
    secrets = manifest.get("secrets", [])
    if not isinstance(secrets, list) or any(not isinstance(row, Mapping) or set(row) != {"name", "valueFrom"}
                                            or not isinstance(row["name"], str)
                                            or not isinstance(row["valueFrom"], str)
                                            or not row["valueFrom"].startswith("arn:")
                                            for row in secrets):
        raise ValueError("secrets must be explicit ARN references; raw secret values are forbidden")
    container = {"name": name, "image": image, "essential": True, "user": "10001",
                 "readonlyRootFilesystem": True,
                 "portMappings": [{"containerPort": port, "protocol": "tcp"}],
                 "environment": list(environment), "secrets": list(secrets),
                 "logConfiguration": {"logDriver": "awslogs", "options": {
                     "awslogs-group": manifest["log_group"], "awslogs-region": manifest["log_region"],
                     "awslogs-stream-prefix": manifest["log_prefix"]}}}
    return {"family": family, "taskRoleArn": role_arns[0], "executionRoleArn": role_arns[1],
            "networkMode": "awsvpc", "requiresCompatibilities": ["FARGATE"],
            "cpu": str(cpu), "memory": str(memory), "containerDefinitions": [container]}


def build_ecs_service_request(manifest: Mapping[str, Any], task_definition: str) -> dict[str, Any]:
    """Build a private one-task service request; caller must dry-run/approve it."""
    if not isinstance(manifest, Mapping): raise ValueError("deployment manifest must be a mapping")
    required = ("cluster", "service_name", "subnets", "security_groups", "container_name", "container_port")
    if any(not isinstance(manifest.get(key),str) or not manifest[key].strip()
           for key in ("cluster", "service_name", "container_name")):
        raise ValueError("service/cluster/container names must be nonempty strings")
    if any(not manifest.get(key) for key in ("subnets", "security_groups", "container_port")):
        raise ValueError("service network manifest is incomplete")
    if not isinstance(task_definition, str) or not task_definition.strip(): raise ValueError("task definition revision required")
    subnets, groups = manifest["subnets"], manifest["security_groups"]
    if not isinstance(subnets, list) or not subnets or any(not isinstance(x,str) or not x.startswith("subnet-") for x in subnets):
        raise ValueError("explicit private subnet IDs are required")
    if not isinstance(groups, list) or not groups or any(not isinstance(x,str) or not x.startswith("sg-") for x in groups):
        raise ValueError("explicit security group IDs are required")
    return {"cluster": manifest["cluster"], "serviceName": manifest["service_name"],
            "taskDefinition": task_definition, "desiredCount": 1, "launchType": "FARGATE",
            "networkConfiguration": {"awsvpcConfiguration": {"subnets": list(subnets),
                "securityGroups": list(groups), "assignPublicIp": "DISABLED"}},
            "deploymentConfiguration": {"minimumHealthyPercent": 0, "maximumPercent": 100}}


class EcsDeploymentAdapter:
    """Guarded deployment/readback/rollback using an injected ECS SDK client.

    No boto3 session or client is constructed. `execution_enabled` defaults to
    false; a separately verified authorizer must approve the exact operation
    and request digest before any injected client method is called.
    """
    def __init__(self, client: EcsClient | None = None,
                 approval_vault: ApprovalVault | None = None,
                 identity_client: StsIdentityClient | None = None,
                 *, execution_enabled: bool = False):
        self._client, self._approval_vault = client, approval_vault
        self._identity_client, self._enabled = identity_client, execution_enabled

    def plan(self, manifest: Mapping[str, Any]) -> dict[str, Any]:
        task = build_ecs_task_definition_request(manifest)
        service = build_ecs_service_request(manifest, manifest.get("task_definition_revision", "PLANNED_REVISION"))
        task_digest=request_sha256(task)
        service_digest=request_sha256(service)
        return {"status": "DRY_RUN", "task_definition_request": task, "service_request": service,
                "task_definition_sha256":task_digest,"service_preview_sha256":service_digest,
                "request_sha256": hashlib.sha256(json.dumps([task,service],sort_keys=True,separators=(",",":")).encode()).hexdigest(),
                "external_call_made": False}

    def _ready(self, operation: str, action: Mapping[str, Any], approval_token: str | None) -> bool:
        return (self._enabled is True and self._client is not None and self._approval_vault is not None
                and self._approval_vault.consume(approval_token,operation,request_sha256(action)) is True)

    def _client_matches(self, manifest: Mapping[str, Any]) -> bool:
        scope=manifest.get("authorization_scope",manifest)
        matches,_identity_call=verify_injected_client_scope(self._client,self._identity_client,scope,"ecs")
        return matches

    def register_revision(self, manifest: Mapping[str, Any],
                          approval_token: str | None = None) -> dict[str, Any]:
        """Register one task revision only; activation needs a second exact approval."""
        request=build_ecs_task_definition_request(manifest)
        action=build_authorized_action("register_task_definition","aws-ecs-fargate","ecs",
                                       manifest.get("authorization_scope",manifest),request)
        digest=request_sha256(action)
        if not self._ready("register_task_definition",action,approval_token):
            return {"status":"BLOCKED","reason":"exact task-definition request is not separately authorized",
                    "request":request,"request_sha256":digest,"external_call_made":False}
        if not self._client_matches(manifest):
            return {"status":"BLOCKED_CLIENT_SCOPE_MISMATCH","request_sha256":digest,
                    "external_call_made":self._client is not None and self._identity_client is not None,
                    "milestone_pass":False}
        response=self._client.register_task_definition(**request)
        task_def=(response.get("taskDefinition") or {}).get("taskDefinitionArn") if isinstance(response,Mapping) else None
        if not isinstance(task_def,str) or not task_def:
            return {"status":"INCOMPLETE_PROVIDER_READBACK","provider_response":response,
                    "external_call_made":True,"milestone_pass":False}
        return {"status":"TASK_REVISION_REGISTERED_REQUIRES_REVIEW","task_definition":task_def,
                "request_sha256":digest,"provider_response":response,"external_call_made":True,
                "milestone_pass":False}

    def activate_revision(self, manifest: Mapping[str, Any], task_definition: str,
                          approval_token: str | None = None) -> dict[str, Any]:
        """Activate a registered, exact task revision under a second approval."""
        request=build_ecs_service_request(manifest,task_definition)
        action=build_authorized_action("activate_service_revision","aws-ecs-fargate","ecs",
                                       manifest.get("authorization_scope",manifest),request)
        digest=request_sha256(action)
        if not self._ready("activate_service_revision",action,approval_token):
            return {"status":"BLOCKED","reason":"exact service activation request is not separately authorized",
                    "request":request,"request_sha256":digest,"external_call_made":False}
        if not self._client_matches(manifest):
            return {"status":"BLOCKED_CLIENT_SCOPE_MISMATCH","request_sha256":digest,
                    "external_call_made":self._client is not None and self._identity_client is not None,
                    "milestone_pass":False}
        existing=self._client.describe_services(cluster=manifest["cluster"],services=[manifest["service_name"]])
        services=existing.get("services",[]) if isinstance(existing,Mapping) else []
        if not isinstance(services,list):
            return {"status":"INVALID_PROVIDER_READBACK","external_call_made":True,"milestone_pass":False}
        if services:
            response=self._client.update_service(**request); operation="updated"
        else:
            response=self._client.create_service(**request); operation="created"
        service_readback=self._client.describe_services(cluster=manifest["cluster"],services=[manifest["service_name"]])
        task_readback=self._client.describe_task_definition(taskDefinition=task_definition)
        return {"status":"DEPLOYMENT_RESPONSE_REQUIRES_READBACK","operation":operation,
                "task_definition":task_definition,"provider_response":response,
                "service_readback":service_readback,"task_definition_readback":task_readback,
                "request_sha256":digest,"external_call_made":True,"milestone_pass":False}

    def rollback(self, manifest: Mapping[str, Any], previous_task_definition: str,
                 approval_token: str | None = None) -> dict[str, Any]:
        if not isinstance(previous_task_definition,str) or not previous_task_definition.strip():
            raise ValueError("observed previous task definition revision is required")
        request=build_ecs_service_request(manifest,previous_task_definition)
        action=build_authorized_action("rollback","aws-ecs-fargate","ecs",
                                       manifest.get("authorization_scope",manifest),request)
        digest=request_sha256(action)
        if not self._ready("rollback",action,approval_token):
            return {"status":"BLOCKED","reason":"exact rollback plan is not separately authorized",
                    "request":request,"request_sha256":digest,"external_call_made":False}
        if not self._client_matches(manifest):
            return {"status":"BLOCKED_CLIENT_SCOPE_MISMATCH","request_sha256":digest,
                    "external_call_made":self._client is not None and self._identity_client is not None,
                    "milestone_pass":False}
        observed=self._client.describe_task_definition(taskDefinition=previous_task_definition)
        observed_task=(observed.get("taskDefinition") or {}).get("taskDefinitionArn") if isinstance(observed,Mapping) else None
        if observed_task != previous_task_definition:
            return {"status":"BLOCKED_PREVIOUS_REVISION_NOT_VERIFIED","task_definition_readback":observed,
                    "request_sha256":digest,"external_call_made":True,"milestone_pass":False}
        response=self._client.update_service(**request)
        readback=self._client.describe_services(cluster=manifest["cluster"],services=[manifest["service_name"]])
        return {"status":"ROLLBACK_RESPONSE_REQUIRES_READBACK","provider_response":response,
                "service_readback":readback,"request_sha256":digest,
                "external_call_made":True,"milestone_pass":False}
