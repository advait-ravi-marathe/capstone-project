.PHONY: validate scaffold test-sde test-sql test-frontend test-frontend-contracts test-frontend-browser build-frontend
validate:
	python3 scripts/validate_sprint.py
scaffold:
	python3 scripts/check_scaffold.py
# This learner acceptance command exits non-zero while the 29 Java methods are TODO.
test-sde:
	mkdir -p /tmp/amazon-sprint-java-classes
	java -m jdk.compiler/com.sun.tools.javac.Main -d /tmp/amazon-sprint-java-classes $$(find sde/src -name '*.java')
	java -cp /tmp/amazon-sprint-java-classes amazon.sprint.AssignmentAcceptance
# This learner SQL contract exits with a recognized TODO until day19.sql is implemented.
test-sql:
	python3 scripts/test_day19_sql.py
# React component and async behavior tests; these verify the instructor scaffold, not mastery.
test-frontend:
	cd frontend && npm test
# Pure learner helper contract tests intentionally fail with 60 TODO errors until implemented.
test-frontend-contracts:
	cd frontend && npm run test:contracts
test-frontend-browser:
	cd frontend && npm run test:browser
build-frontend:
	cd frontend && npm run build
