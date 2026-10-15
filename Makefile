.PHONY: validate test-sde test-frontend build-frontend
validate:
	python3 scripts/validate_sprint.py
# Learner tests intentionally fail until TODO functions are implemented.
test-sde:
	python3 -m unittest sde.test_assignments -v
# Learner tests intentionally fail until TODO functions are implemented.
test-frontend:
	cd frontend && npm test
build-frontend:
	cd frontend && npm run build
