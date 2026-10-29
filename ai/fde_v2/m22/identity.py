"""Maintained-library JWT verification with local, explicit key selection.

This adapter verifies cryptographic signatures and required claims through
PyJWT. It has no JWKS/network discovery: callers inject an already trusted key
map, issuer, audience and algorithm allowlist. A local signed fixture is only
integration mechanics, not evidence of a real identity-provider exchange.
"""
from __future__ import annotations

from collections.abc import Mapping, Sequence
from typing import Any

import jwt


class StaticKeyJWTVerifier:
    def __init__(self, keys_by_kid: Mapping[str, Any], *, issuer: str,
                 audience: str, algorithms: Sequence[str]):
        supported = set(jwt.algorithms.get_default_algorithms()) - {"none"}
        if (not isinstance(keys_by_kid, Mapping) or not keys_by_kid
                or any(not isinstance(k, str) or not k.strip() for k in keys_by_kid)
                or any(v is None for v in keys_by_kid.values())):
            raise ValueError("a nonempty trusted key map keyed by kid is required")
        if (not isinstance(issuer, str) or not issuer.strip()
                or not isinstance(audience, str) or not audience.strip()):
            raise ValueError("expected issuer and audience are required")
        if (not isinstance(algorithms, (list, tuple)) or not algorithms
                or any(not isinstance(alg, str) or alg not in supported for alg in algorithms)
                or len(set(algorithms)) != len(algorithms)):
            raise ValueError("a unique explicit allowlist of supported signing algorithms is required")
        self._keys = dict(keys_by_kid)
        self._issuer = issuer
        self._audience = audience
        self._algorithms = tuple(algorithms)

    def __call__(self, token: str) -> dict[str, Any]:
        if not isinstance(token, str) or not token or len(token) > 16_384:
            raise ValueError("JWT must be a bounded nonempty string")
        header = jwt.get_unverified_header(token)
        algorithm, key_id = header.get("alg"), header.get("kid")
        if algorithm not in self._algorithms:
            raise ValueError("JWT algorithm is not allowlisted")
        if not isinstance(key_id, str) or key_id not in self._keys:
            raise ValueError("JWT key id is unknown")
        # PyJWT performs signature, expiry, issuer, audience, and required claim
        # verification. No caller-supplied time or unverified claim is trusted.
        claims = jwt.decode(
            token,
            self._keys[key_id],
            algorithms=list(self._algorithms),
            issuer=self._issuer,
            audience=self._audience,
            leeway=0,
            options={"verify_signature": True, "require": ["sub", "iss", "aud", "exp"]},
        )
        if not isinstance(claims, dict):
            raise ValueError("verified JWT claims must be an object")
        return claims
