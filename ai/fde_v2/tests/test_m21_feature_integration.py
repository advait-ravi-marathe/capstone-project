"""Persisted M2.1 lifecycle workflow mechanics with a fixed vector fixture."""
from __future__ import annotations

import hashlib
import tempfile
import unittest
from pathlib import Path
from fastapi.testclient import TestClient

from ai.fde_v2.coverstory.contracts import Chunk, Document
from ai.fde_v2.coverstory.vector_store import PersistentVectorStore
from ai.fde_v2.m21.api import create_app
from ai.fde_v2.m21 import learner, reference
from ai.fde_v2.m21.ledger import VersionLedger
from ai.fde_v2.m21.workflow import VersionedWorkflow


class FixtureEmbedder:
    dimension = 2
    def encode(self, texts):
        return [[1.0, 0.0] if "current" in t or "query" in t else [0.0, 1.0] for t in texts]


def make_doc(version: int, text: str):
    document = Document("policy", version, "Synthetic Policy", "CC0 fixture", text,
                        hashlib.sha256(text.encode()).hexdigest())
    chunk = Chunk(f"policy:v{version}:0", "policy", version, document.title,
                  document.source, 0, len(text), text)
    return document, [chunk]


class M21FeatureIntegration(unittest.TestCase):
    def test_http_version_update_and_delete_use_persistent_lifecycle(self):
        with tempfile.TemporaryDirectory() as td:
            root = Path(td)
            ledger = VersionLedger(root / "versions.sqlite3")
            vectors = PersistentVectorStore(root / "qdrant", 2)
            class Adapter:
                stage_version_update = staticmethod(reference.stage_version_update)
                delete_document = staticmethod(reference.delete_document)
                filter_current_candidates = staticmethod(reference.filter_current_candidates)
            client = TestClient(create_app(VersionedWorkflow(
                ledger, vectors, FixtureEmbedder(), adapter=Adapter)))
            payload = {"version": 1, "title": "Synthetic Policy", "source": "CC0 fixture",
                       "text": "current content", "chunks": [{"chunk_id": "policy:v1:0",
                       "start_char": 0, "end_char": 15, "text": "current content"}]}
            response = client.put("/documents/policy/versions", json=payload)
            self.assertEqual(response.status_code, 200, response.text)
            self.assertEqual(response.json()["active_version"], 1)
            hits = client.post("/search", json={"query": "query", "limit": 3})
            self.assertEqual([row["version"] for row in hits.json()["results"]], [1])
            self.assertEqual(client.delete("/documents/policy").json()["status"], "deleted")
            self.assertEqual(client.post("/search", json={"query": "query"}).json()["results"], [])
            vectors.close()

    def test_feature_path_routes_publish_delete_and_search_through_candidate(self):
        with tempfile.TemporaryDirectory() as td:
            root = Path(td)
            ledger = VersionLedger(root / "versions.sqlite3")
            vectors = PersistentVectorStore(root / "qdrant", 2)
            class Adapter:
                stage_version_update = staticmethod(reference.stage_version_update)
                delete_document = staticmethod(reference.delete_document)
                filter_current_candidates = staticmethod(reference.filter_current_candidates)
            service = VersionedWorkflow(ledger, vectors, FixtureEmbedder(), adapter=Adapter)
            v1, c1 = make_doc(1, "old content")
            self.assertEqual(service.publish(v1, c1)["active_version"], 1)
            v2, c2 = make_doc(2, "current content")
            self.assertEqual(service.publish(v2, c2)["active_version"], 2)
            self.assertEqual([hit.chunk.version for hit in service.search("query")], [2])
            self.assertEqual(service.delete("policy")["status"], "deleted")
            vectors.close()
            vectors = PersistentVectorStore(root / "qdrant", 2)
            reopened = VersionedWorkflow(ledger, vectors, FixtureEmbedder(), adapter=Adapter)
            self.assertEqual(reopened.search("query"), [])
            vectors.close()

    def test_failed_vector_stage_keeps_previous_active_revision_searchable(self):
        with tempfile.TemporaryDirectory() as td:
            root = Path(td)
            ledger = VersionLedger(root / "versions.sqlite3")
            vectors = PersistentVectorStore(root / "qdrant", 2)
            class Adapter:
                stage_version_update = staticmethod(reference.stage_version_update)
                delete_document = staticmethod(reference.delete_document)
                filter_current_candidates = staticmethod(reference.filter_current_candidates)
            service = VersionedWorkflow(ledger, vectors, FixtureEmbedder(), adapter=Adapter)
            v1, c1 = make_doc(1, "old content")
            service.publish(v1, c1)
            original_upsert = vectors.upsert
            vectors.upsert = lambda *_args: (_ for _ in ()).throw(RuntimeError("injected index failure"))
            v2, c2 = make_doc(2, "new current content")
            with self.assertRaisesRegex(RuntimeError, "injected index failure"):
                service.publish(v2, c2)
            vectors.upsert = original_upsert
            self.assertEqual(ledger.active_version("policy"), 1)
            self.assertEqual([hit.chunk.version for hit in service.search("query")], [1])
            vectors.close()

    def test_default_application_path_uses_assistant_supplied_lifecycle(self):
        self.assertIs(VersionedWorkflow.__init__.__kwdefaults__["adapter"], reference)
        with tempfile.TemporaryDirectory() as td:
            ledger = VersionLedger(Path(td) / "versions.sqlite3")
            vectors = PersistentVectorStore(Path(td) / "qdrant", 2)
            service = VersionedWorkflow(ledger, vectors, FixtureEmbedder())
            doc, chunks = make_doc(1, "current content")
            receipt=service.publish(doc, chunks)
            self.assertEqual(receipt["status"],"activated")
            self.assertEqual([hit.chunk.version for hit in service.search("query")],[1])
            vectors.close()


if __name__ == "__main__":
    unittest.main()
