"""Fail-closed AWS provider adapters; clients are always injected by callers.

The module never imports boto3, discovers credentials, constructs a session,
creates IAM permissions, provisions resources, or performs a request at import.
A deployment owner must supply an SDK client and an exact-scope authorizer after
separate user authorization. Pure request builders are safe for dry-run tests.
"""
from __future__ import annotations

import hashlib
import json
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
                 authorize: Callable[[Mapping[str, Any]], bool] | None = None,
                 *, execution_enabled: bool = False):
        self._client = client
        self._authorize = authorize
        self._execution_enabled = execution_enabled

    def execute(self, scope: Mapping[str, Any]) -> dict[str, Any]:
        if self._execution_enabled is not True or self._client is None or self._authorize is None:
            return {"status": "BLOCKED", "reason": "Bedrock client and separately authorized live gate are required",
                    "external_call_made": False}
        if self._authorize(scope) is not True:
            return {"status": "BLOCKED", "reason": "exact Bedrock request scope was not authorized",
                    "external_call_made": False}
        if scope.get("provider") != "aws-bedrock-converse":
            return {"status": "BLOCKED", "reason": "provider scope mismatch", "external_call_made": False}
        request = build_converse_request(scope.get("model_id"), scope.get("prompt"), scope.get("max_tokens"))
        response = self._client.converse(**request)
        parsed = parse_converse_response(response, request["modelId"])
        return {"status": "PROVIDER_RESPONSE_REQUIRES_REVIEW", "external_call_made": True,
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
                 authorize: Callable[[str, Mapping[str, Any], str], bool] | None = None,
                 *, execution_enabled: bool = False):
        self._client, self._authorize, self._enabled = client, authorize, execution_enabled

    def plan(self, manifest: Mapping[str, Any]) -> dict[str, Any]:
        task = build_ecs_task_definition_request(manifest)
        service = build_ecs_service_request(manifest, manifest.get("task_definition_revision", "PLANNED_REVISION"))
        task_digest=hashlib.sha256(json.dumps(task,sort_keys=True,separators=(",",":")).encode()).hexdigest()
        service_digest=hashlib.sha256(json.dumps(service,sort_keys=True,separators=(",",":")).encode()).hexdigest()
        return {"status": "DRY_RUN", "task_definition_request": task, "service_request": service,
                "task_definition_sha256":task_digest,"service_preview_sha256":service_digest,
                "request_sha256": hashlib.sha256(json.dumps([task,service],sort_keys=True,separators=(",",":")).encode()).hexdigest(),
                "external_call_made": False}

    def _ready(self, operation: str, manifest: Mapping[str, Any], digest: str) -> bool:
        return (self._enabled is True and self._client is not None and self._authorize is not None
                and isinstance(digest,str) and len(digest)==64
                and self._authorize(operation,manifest,digest) is True)

    def register_revision(self, manifest: Mapping[str, Any],
                          approved_request_sha256: str | None = None) -> dict[str, Any]:
        """Register one task revision only; activation needs a second exact approval."""
        request=build_ecs_task_definition_request(manifest)
        digest=hashlib.sha256(json.dumps(request,sort_keys=True,separators=(",",":")).encode()).hexdigest()
        if approved_request_sha256 != digest or not self._ready("register_task_definition",manifest,digest):
            return {"status":"BLOCKED","reason":"exact task-definition request is not separately authorized",
                    "request":request,"request_sha256":digest,"external_call_made":False}
        response=self._client.register_task_definition(**request)
        task_def=(response.get("taskDefinition") or {}).get("taskDefinitionArn") if isinstance(response,Mapping) else None
        if not isinstance(task_def,str) or not task_def:
            return {"status":"INCOMPLETE_PROVIDER_READBACK","provider_response":response,
                    "external_call_made":True,"milestone_pass":False}
        return {"status":"TASK_REVISION_REGISTERED_REQUIRES_REVIEW","task_definition":task_def,
                "request_sha256":digest,"provider_response":response,"external_call_made":True,
                "milestone_pass":False}

    def activate_revision(self, manifest: Mapping[str, Any], task_definition: str,
                          approved_request_sha256: str | None = None) -> dict[str, Any]:
        """Activate a registered, exact task revision under a second approval."""
        request=build_ecs_service_request(manifest,task_definition)
        digest=hashlib.sha256(json.dumps(request,sort_keys=True,separators=(",",":")).encode()).hexdigest()
        if approved_request_sha256 != digest or not self._ready("activate_service_revision",manifest,digest):
            return {"status":"BLOCKED","reason":"exact service activation request is not separately authorized",
                    "request":request,"request_sha256":digest,"external_call_made":False}
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
                 approved_request_sha256: str | None = None) -> dict[str, Any]:
        if not isinstance(previous_task_definition,str) or not previous_task_definition.strip():
            raise ValueError("observed previous task definition revision is required")
        request=build_ecs_service_request(manifest,previous_task_definition)
        digest=hashlib.sha256(json.dumps(request,sort_keys=True,separators=(",",":")).encode()).hexdigest()
        if approved_request_sha256 != digest or not self._ready("rollback",manifest,digest):
            return {"status":"BLOCKED","reason":"exact rollback plan is not separately authorized",
                    "request":request,"request_sha256":digest,"external_call_made":False}
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
