import json
import unittest
from pathlib import Path

ROOT = Path(__file__).resolve().parents[3]


def read(day):
    return json.loads((ROOT / f"evidence/ai-first/day-{day}.json").read_text())


class Day26(unittest.TestCase):
    def test_read_only_request_path_matches_supplied_sources(self):
        answer = read(26)
        self.assertEqual({"day": 26, "frontend_entry": "frontend/src/ai/AiFirstCoverStoryApp.tsx", "backend_route": "POST /api/ai/policy-assistant", "python_service_route": "POST /answer"}, answer)
        app = (ROOT / "frontend/src/ai/AiFirstCoverStoryApp.tsx").read_text()
        controller = (ROOT / "src/main/java/assignments/local/LocalAiAssistantController.java").read_text()
        server = (ROOT / "ai/coverstory_assistant/server.py").read_text()
        self.assertIn("/api/ai/policy-assistant", app)
        self.assertIn('@RequestMapping("/api/ai")', controller)
        self.assertIn('@PostMapping("/policy-assistant")', controller)
        self.assertIn("/answer", server)


class Day27(unittest.TestCase):
    def test_read_only_auth_and_request_boundary_match_supplied_sources(self):
        answer = read(27)
        self.assertEqual({"day": 27, "owner_source": "authenticated session principal", "request_contains_owner_id": False, "csrf_required": True}, answer)
        controller = (ROOT / "src/main/java/assignments/local/LocalAiAssistantController.java").read_text()
        app = (ROOT / "frontend/src/ai/AiFirstCoverStoryApp.tsx").read_text()
        self.assertIn("authentication.getName()", controller)
        self.assertNotIn("ownerId", app.split("fetch('/api/ai/policy-assistant'", 1)[-1].split("})", 1)[0])
        self.assertIn("X-XSRF-TOKEN", app)


class Day28(unittest.TestCase):
    def test_read_only_failure_contract_is_complete_and_non_disclosing(self):
        answer = read(28)
        self.assertEqual({"day": 28, "unauthenticated_status": 401, "provider_unavailable_status": 503, "raw_prompt_in_error": False}, answer)
        controller = (ROOT / "src/main/java/assignments/local/LocalAiAssistantController.java").read_text()
        self.assertIn("HttpStatus.UNAUTHORIZED", controller)
        self.assertIn("HttpStatus.SERVICE_UNAVAILABLE", controller)
        self.assertIn("AI_PROVIDER_UNAVAILABLE", controller)
