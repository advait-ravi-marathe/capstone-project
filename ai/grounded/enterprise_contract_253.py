"""Learner starter for one synthetic enterprise identity/document contract.

The adapter supplies an already-verified fixture result. This module does not
parse SAML, verify signatures, call an identity provider, or access AWS.
"""
from __future__ import annotations
from dataclasses import dataclass

@dataclass(frozen=True)
class Assertion:
    issuer: str
    subject: str
    audience: str
    tenant: str
    groups: tuple[str, ...]
    scopes: tuple[str, ...]
    issued_at: int
    expires_at: int
    adapter_verified: bool
    adapter_status: str = "ok"

@dataclass(frozen=True)
class Document:
    document_id: str
    tenant: str
    revision: str
    allowed_groups: tuple[str, ...]
    active: bool
    excerpt: str

@dataclass(frozen=True)
class AccessRequest:
    action: str
    document_id: str
    revision: str

@dataclass(frozen=True)
class Decision:
    allowed: bool
    reason: str
    document_id: str = ""
    citation: str = ""
    excerpt: str = ""

EXPECTED_ISSUER = "https://idp.example.test/tenant-cedar"
EXPECTED_AUDIENCE = "https://docs.example.test/reader"
EXPECTED_TENANT = "tenant-cedar"
REQUIRED_SCOPE = "document.read"
MAX_ASSERTION_SECONDS = 3600


def evaluate_access(assertion: Assertion, document: Document, request: AccessRequest, *, now: int) -> Decision:
    """TODO Days 253–266: validate the whole fixed synthetic access contract.

    Fail closed on adapter errors, malformed claims, issuer/audience/time,
    tenant, action, scope, document lifecycle/revision and group permission.
    Disclose the exact excerpt only after all checks pass. Denials use stable
    reason codes and disclose no claim or document content.
    """
    raise NotImplementedError("TODO: synthetic identity/document contract")
