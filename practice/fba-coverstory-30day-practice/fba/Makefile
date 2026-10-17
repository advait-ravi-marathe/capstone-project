.PHONY: validate test-frontend test-frontend-day build-frontend test-browser
validate:
	python3 scripts/validate_course.py
test-frontend:
	cd frontend && npm test
test-frontend-day:
	@test -n "$(DAY)" || (echo 'Set DAY=1..30'; exit 2)
	cd frontend && DAY=$(DAY) npm run test:contracts
build-frontend:
	cd frontend && npm run build
test-browser:
	cd frontend && npm run test:browser
