"""Local SQLite + embedded-Qdrant boundary integration mechanics.

These tests use synthetic identities and the explicit instructor adapter. They
exercise the application path; they are not a real identity or learner receipt.
"""
from __future__ import annotations

import unittest
from unittest.mock import Mock

from fastapi.testclient import TestClient

from ai.fde_v2.m22 import learner, reference
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
        self.claims = {
            "alice-token": {"sub": "alice", "iss": "issuer", "aud": "app", "exp": 2000},
            "mallory-token": {"sub": "mallory", "iss": "issuer", "aud": "app", "exp": 2000},
            "expired-token": {"sub": "alice", "iss": "issuer", "aud": "app", "exp": 1000},
        }
        self.lookup = lambda subject: {
            "enabled": subject in {"alice", "mallory"},
            "tenant_id": {"alice": "tenant-a", "mallory": "tenant-a"}.get(subject),
        }

    def tearDown(self):
        self.store.close()

    def app(self, adapter=reference):
        return create_app(
            store=self.store,
            verified_claims_provider=lambda token: self.claims.get(token),
            subject_lookup=self.lookup,
            expected_issuer="issuer", expected_audience="app",
            adapter=adapter, clock=lambda: 1500,
        )

    def test_real_local_sqlite_qdrant_route_filters_tenant_and_current_acl(self):
        client = TestClient(self.app())
        response = client.post("/search", headers={"Authorization": "Bearer alice-token"},
                               json={"vector": [1.0, 0.0], "limit": 10})
        self.assertEqual(response.status_code, 200, response.text)
        self.assertEqual([item["document_id"] for item in response.json()["results"]], ["doc-a"])
        self.assertEqual(response.json()["results"][0]["text"], "current policy passage")
        self.assertNotIn("foreign tenant passage", response.text)

    def test_auth_and_document_endpoint_fail_closed(self):
        client = TestClient(self.app())
        self.assertEqual(client.get("/documents/doc-a").status_code, 401)
        self.assertEqual(client.get("/documents/doc-a", headers={
            "Authorization": "Bearer expired-token"}).status_code, 401)
        self.assertEqual(client.get("/documents/doc-a", headers={
            "Authorization": "Bearer mallory-token"}).status_code, 404)
        self.assertEqual(client.get("/documents/doc-a", headers={
            "Authorization": "Bearer alice-token"}).json()["body"], "current policy passage")

    def test_cached_qdrant_candidates_are_rechecked_after_acl_revocation(self):
        client = TestClient(self.app())
        headers = {"Authorization": "Bearer alice-token"}
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
            "/search", headers={"Authorization": "Bearer alice-token"},
            json={"vector": [1.0, 0.0]},
        )
        self.assertEqual(response.status_code, 200, response.text)
        adapter.verify_identity.assert_called_once()
        adapter.filter_retrieved_hits.assert_called_once()

        # The default route is wired to learner TODOs; a missing implementation
        # is denied by the request path and is never replaced by reference code.
        default_response = TestClient(self.app(learner)).post(
            "/search", headers={"Authorization": "Bearer alice-token"},
            json={"vector": [1.0, 0.0]},
        )
        self.assertEqual(default_response.status_code, 401)

    def test_search_request_bounds_and_extra_fields(self):
        client = TestClient(self.app())
        headers = {"Authorization": "Bearer alice-token"}
        self.assertEqual(client.post("/search", headers=headers,
                         json={"vector": [1.0, 0.0], "limit": 21}).status_code, 422)
        self.assertEqual(client.post("/search", headers=headers,
                         json={"vector": [1.0, 0.0], "tenant_id": "tenant-b"}).status_code, 422)


if __name__ == "__main__":
    unittest.main()
