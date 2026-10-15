.PHONY: validate scaffold test-sde test-sde-day test-sql test-frontend test-frontend-day test-frontend-contracts test-frontend-browser build-frontend
validate:
	python3 scripts/validate_sprint.py
scaffold:
	python3 scripts/check_scaffold.py
# Full SDE learner suite; passes when solutions pass, reports recognized TODOs otherwise.
test-sde:
	mkdir -p /tmp/amazon-sprint-java-classes
	java -m jdk.compiler/com.sun.tools.javac.Main -d /tmp/amazon-sprint-java-classes $$(find sde/src -name '*.java')
	java -cp /tmp/amazon-sprint-java-classes amazon.sprint.AssignmentAcceptance
# Isolated per-day Java contract. Use e.g. make test-sde-day DAY=1. Day 19 is SQL.
test-sde-day:
	@test -n "$(DAY)" || (echo 'Set DAY=1..30; Day 19 uses make test-sql.'; exit 2)
	@test "$(DAY)" != "19" || (echo 'Day 19 is SQL; use make test-sql.'; exit 2)
	mkdir -p /tmp/amazon-sprint-java-classes
	java -m jdk.compiler/com.sun.tools.javac.Main -d /tmp/amazon-sprint-java-classes $$(find sde/src -name '*.java')
	java -cp /tmp/amazon-sprint-java-classes amazon.sprint.AssignmentAcceptance --day=$(DAY)
# Day 19 learner SQL contract; passes on correct query, fails for TODOs or wrong results.
test-sql:
	python3 scripts/test_day19_sql.py
# Component/async smoke suite is independent of learner solution correctness.
test-frontend:
	cd frontend && npm test
# Isolated pure-function contract suite; use e.g. make test-frontend-day DAY=1.
test-frontend-day:
	@test -n "$(DAY)" || (echo 'Set DAY=1..30.'; exit 2)
	cd frontend && DAY=$(DAY) npm run test:contracts
# Full pure learner helper suite.
test-frontend-contracts:
	cd frontend && npm run test:contracts
test-frontend-browser:
	cd frontend && npm run test:browser
build-frontend:
	cd frontend && npm run build
