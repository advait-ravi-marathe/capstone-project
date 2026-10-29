"""Local SQLite + embedded-Qdrant boundary integration mechanics.

These tests use synthetic identities and the explicit instructor adapter. They
exercise the application path; they are not a real identity or learner receipt.
"""
from __future__ import annotations

import unittest
import time
from unittest.mock import Mock

import jwt
from fastapi.testclient import TestClient

from ai.fde_v2.m22 import learner, reference
from ai.fde_v2.m22.identity import StaticKeyJWTVerifier
from ai.fde_v2.m22.workflow import LocalSqlQdrantStore, create_app


class M22LocalIntegration(unittest.TestCase):
    def setUp(self):
        self.store = LocalSqlQdrantStore(collection="m22_test")
        self.store.put_document(
            document_id="doc-a", tenant_id="tenant-a", owner_id="bob",
            reader_ids=["alice"], writer_ids=[], version=2,
            chunks=[("a-current", "current policy passage", [1.0, 0.0])],
        )
        self.store.put_document(
            document_id="doc-b", tenant_id="tenant-b", owner_id="alice",
            reader_ids=["alice"], writer_ids=["alice"], version=1,
            chunks=[("b-current", "foreign tenant passage", [0.99, 0.01])],
        )
        self.now = int(time.time())
        self.jwt_key = "local-test-only-key-with-at-least-384-bits-0001-xxxxxxxxxxxxxxxx"
        self.verifier = StaticKeyJWTVerifier({"local-test-k1": self.jwt_key}, issuer="issuer",
            audience="app", algorithms=("HS256",))
        self.tokens = {
            "alice-token": self.signed_token("alice"),
            "bob-token": self.signed_token("bob"),
            "mallory-token": self.signed_token("mallory"),
            "expired-token": self.signed_token("alice", exp=self.now - 1),
            "wrong-issuer-token": self.signed_token("alice", iss="other-issuer"),
            "wrong-audience-token": self.signed_token("alice", aud="other-app"),
            "bad-signature-token": self.signed_token("alice", key="wrong-local-test-key-with-at-least-384-bits-0002-yyyyyyyyyyyyyyyy"),
            "unknown-kid-token": self.signed_token("alice", kid="unknown-kid"),
            "disallowed-alg-token": jwt.encode({"sub":"alice","iss":"issuer","aud":"app",
                "exp":self.now + 60}, self.jwt_key, algorithm="HS384", headers={"kid":"local-test-k1"}),
        }
        self.lookup = lambda subject: {
            "enabled": subject in {"alice", "bob", "mallory"},
            "tenant_id": {"alice": "tenant-a", "bob": "tenant-a", "mallory": "tenant-a"}.get(subject),
        }

    def signed_token(self, subject, *, iss="issuer", aud="app", exp=None,
                     key=None, kid="local-test-k1"):
        return jwt.encode({"sub":subject,"iss":iss,"aud":aud,
                           "exp":self.now + 60 if exp is None else exp},
                          self.jwt_key if key is None else key, algorithm="HS256",
                          headers={"kid":kid})

    def tearDown(self):
        self.store.close()

    def app(self, adapter=reference):
        return create_app(
            store=self.store,
            verified_claims_provider=self.verifier,
            subject_lookup=self.lookup,
            expected_issuer="issuer", expected_audience="app",
            adapter=adapter, clock=lambda: self.now,
        )

    def test_real_local_sqlite_qdrant_route_filters_tenant_and_current_acl(self):
        client = TestClient(self.app())
        response = client.post("/search", headers={"Authorization": f"Bearer {self.tokens['alice-token']}"},
                               json={"vector": [1.0, 0.0], "limit": 10})
        self.assertEqual(response.status_code, 200, response.text)
        self.assertEqual([item["document_id"] for item in response.json()["results"]], ["doc-a"])
        self.assertEqual(response.json()["results"][0]["text"], "current policy passage")
        self.assertNotIn("foreign tenant passage", response.text)

    def test_auth_and_document_endpoint_fail_closed(self):
        client = TestClient(self.app())
        self.assertEqual(client.get("/documents/doc-a").status_code, 401)
        for name in ("expired-token","wrong-issuer-token","wrong-audience-token",
                     "bad-signature-token","unknown-kid-token","disallowed-alg-token"):
            with self.subTest(token=name):
                self.assertEqual(client.get("/documents/doc-a",headers={
                    "Authorization":f"Bearer {self.tokens[name]}"}).status_code,401)
        self.assertEqual(client.get("/documents/doc-a", headers={
            "Authorization": f"Bearer {self.tokens['mallory-token']}"}).status_code, 404)
        self.assertEqual(client.get("/documents/doc-a", headers={
            "Authorization": f"Bearer {self.tokens['alice-token']}"}).json()["body"], "current policy passage")

    def test_malformed_truthy_authorization_result_never_discloses_document(self):
        class MalformedPolicy:
            verify_identity = staticmethod(reference.verify_identity)
            authorize_document = staticmethod(lambda *_args: "false")
            filter_retrieved_hits = staticmethod(reference.filter_retrieved_hits)
        response = TestClient(self.app(MalformedPolicy)).get(
            "/documents/doc-a", headers={"Authorization": f"Bearer {self.tokens['alice-token']}"})
        self.assertEqual(response.status_code, 404)
        self.assertNotIn("current policy passage", response.text)

    def test_cached_qdrant_candidates_are_rechecked_after_acl_revocation(self):
        client = TestClient(self.app())
        headers = {"Authorization": f"Bearer {self.tokens['alice-token']}"}
        payload = {"vector": [1.0, 0.0], "limit": 10}
        first = client.post("/search", headers=headers, json=payload)
        self.assertEqual(len(first.json()["results"]), 1)
        # raw Qdrant candidates are deliberately cacheable; SQL remains authority.
        cache_size_before = len(self.store._hit_cache)
        self.store.revoke_reader("doc-a", "alice")
        second = client.post("/search", headers=headers, json=payload)
        self.assertEqual(second.status_code, 200)
        self.assertEqual(second.json()["results"], [])
        self.assertEqual(len(self.store._hit_cache), cache_size_before)

    def test_application_calls_injected_learner_policy_and_never_falls_back(self):
        adapter = Mock(wraps=reference)
        response = TestClient(self.app(adapter)).post(
            "/search", headers={"Authorization": f"Bearer {self.tokens['alice-token']}"},
            json={"vector": [1.0, 0.0]},
        )
        self.assertEqual(response.status_code, 200, response.text)
        adapter.verify_identity.assert_called_once()
        adapter.filter_retrieved_hits.assert_called_once()

        # The default route is wired to learner TODOs; a missing implementation
        # is denied by the request path and is never replaced by reference code.
        default_response = TestClient(self.app(learner)).post(
            "/search", headers={"Authorization": f"Bearer {self.tokens['alice-token']}"},
            json={"vector": [1.0, 0.0]},
        )
        self.assertEqual(default_response.status_code, 401)

    def test_search_request_bounds_and_extra_fields(self):
        client = TestClient(self.app())
        headers = {"Authorization": f"Bearer {self.tokens['alice-token']}"}
        self.assertEqual(client.post("/search", headers=headers,
                         json={"vector": [1.0, 0.0], "limit": 21}).status_code, 422)
        self.assertEqual(client.post("/search", headers=headers,
                         json={"vector": [1.0, 0.0], "tenant_id": "tenant-b"}).status_code, 422)

    def test_delete_route_tombstones_and_purges_cached_document_vectors(self):
        client=TestClient(self.app())
        headers={"Authorization":f"Bearer {self.tokens['bob-token']}"}
        self.assertEqual(client.post("/search",headers=headers,json={"vector":[1.0,0.0]}).status_code,200)
        self.assertTrue(self.store._hit_cache)
        deleted=client.delete("/documents/doc-a",headers=headers)
        self.assertEqual(deleted.status_code,200,deleted.text)
        self.assertTrue(deleted.json()["logical_access_revoked"])
        self.assertTrue(deleted.json()["physical_vectors_removed"])
        self.assertEqual(client.get("/documents/doc-a",headers=headers).status_code,404)
        self.assertEqual(client.post("/search",headers=headers,json={"vector":[1.0,0.0]}).json()["results"],[])
        self.assertEqual(self.store.document("doc-a")["body"],"")

    def test_failed_physical_purge_keeps_tombstone_and_denies_stale_qdrant_hits(self):
        client=TestClient(self.app())
        headers={"Authorization":f"Bearer {self.tokens['bob-token']}"}
        client.post("/search",headers=headers,json={"vector":[1.0,0.0]})
        delete=self.store.client.delete
        self.store.client.delete=Mock(side_effect=RuntimeError("synthetic vector cleanup failure"))
        try:
            result=client.delete("/documents/doc-a",headers=headers)
        finally:
            self.store.client.delete=delete
        self.assertEqual(result.status_code,200,result.text)
        self.assertEqual(result.json()["status"],"CLEANUP_PENDING")
        self.assertTrue(self.store.document("doc-a")["deleted"])
        self.assertEqual(client.get("/documents/doc-a",headers=headers).status_code,404)
        self.assertEqual(client.post("/search",headers=headers,json={"vector":[1.0,0.0]}).json()["results"],[])


if __name__ == "__main__":
    unittest.main()
