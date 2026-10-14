# CAP-78 — Atomic mock purchase and journey transaction

**Saved contract:** Day 038 · 20 Nov 2026 · 150 minutes maximum. Learner behavior remains a TODO. The checked-in PostgreSQL witnesses are instructor evidence only.

Implement `CAP78Seam.createMockPaymentIntent` in [`src/main/java/coverstory/modules/payments/api/CAP78Seam.java`](../../../src/main/java/coverstory/modules/payments/api/CAP78Seam.java). The saved source planned `src/main/java/capstone/coverstory/payments/api/CAP78Seam.java`; this repository's actual module namespace is `coverstory.modules.payments.api`, so the implementation seam follows the current checkout package.

## Source and module boundaries

The exact saved task, paths, and acceptance criteria are in [Day 038](saved-days/day-038-cap-78.md) and the [curriculum index](curriculum-index.json). Current public module contracts were inspected before adding this seam:

- [`ApplicationPort`](../../../src/main/java/coverstory/modules/application/ApplicationPort.java) supports owned resume and revision-checked submission only.
- [`PaymentPort`](../../../src/main/java/coverstory/modules/payments/PaymentPort.java) supports simulated begin/reconcile/cancel, but its current result enum has no pending-attempt command for this SQL transaction.
- [`BusinessAuditPort`](../../../src/main/java/coverstory/modules/audit/BusinessAuditPort.java) appends through the caller's transaction.
- [`AllowlistedAnalyticsPort`](../../../src/main/java/coverstory/modules/analytics/AllowlistedAnalyticsPort.java) is best-effort.
- No public journey write port or quote-validation port is present in this checkout.

CAP78 declares narrow transaction-aware public ports for the missing operations. A production adapter still needs to bind them to Application, Payments, quote validation, journey, and business-audit owners under one PostgreSQL transaction. Do not import another module's repository or use `PaymentPort.begin` to make an external call from this transaction.

The validated quote read returns only safe quote facts, accepted terms, source status, and proposed five-minute expiry. Checkout has no decrypt or provider capability. Use integer INR paise. Application becomes `AWAITING_PAYMENT`; the attempt remains `PENDING`; journey moves to `PAYMENT_PENDING`. No policy is issued and payment does not unlock onboarding.

## Instructor schema and evidence

The SQL extension is [`src/main/resources/db/schema/cap78_purchase.sql`](../../../src/main/resources/db/schema/cap78_purchase.sql); fixture rows are [`src/test/resources/coverstory/payments/cap_78_fixtures.sql`](../../../src/test/resources/coverstory/payments/cap_78_fixtures.sql). The JSON fixture is [`fixtures/coverstory/CAP-78/synthetic-v1.json`](../../../fixtures/coverstory/CAP-78/synthetic-v1.json). All identities and amounts are synthetic. The private marker is a test canary and is not used as a checkout value.

The PostgreSQL instructor suite executes the schemas and test-only SQL oracle against embedded PostgreSQL. It covers a seven-write commit, same-key replay and changed-payload conflict, active source and journey version checks, withdrawal rejection, safe foreign-owner not-found before validation reads, best-effort analytics failure, no fake policy issuance, privacy canary absence, and rollback injected after each of the seven write boundaries. This verifies SQL fixture/test infrastructure; it does not call or complete the learner seam.

## Run checks separately

```bash
# Embedded PostgreSQL schema, fixture, and instructor reference transaction
PATH=/tmp/javac-launcher:/tmp/apache-maven-3.10.0/bin:$PATH mvn -q \
  -s /tmp/maven-settings.xml -Dmaven.repo.local=/tmp/maven-repo \
  -Dmaven.compiler.fork=true -Dmaven.compiler.executable=/tmp/javac-launcher/javac \
  -Dmaven.compiler.release= -Dmaven.compiler.source=21 -Dmaven.compiler.target=21 \
  -Dtest=CAP78InstructorInfrastructureTest test

# Learner acceptance seam (explicit TODO errors expected until implementation)
PATH=/tmp/javac-launcher:/tmp/apache-maven-3.10.0/bin:$PATH mvn -q \
  -s /tmp/maven-settings.xml -Dmaven.repo.local=/tmp/maven-repo \
  -Dmaven.compiler.fork=true -Dmaven.compiler.executable=/tmp/javac-launcher/javac \
  -Dmaven.compiler.release= -Dmaven.compiler.source=21 -Dmaven.compiler.target=21 \
  -Plearner-tests -Dtest=CAP78AcceptanceTest test
```

The learner suite uses a test-only transaction adapter and contains the saved positive, replay, rollback, conflict, owner-scope, telemetry, and privacy cases. Its expected result today is a TODO error at `createMockPaymentIntent`. Do not treat a passing instructor suite as learner completion or independent mastery.
