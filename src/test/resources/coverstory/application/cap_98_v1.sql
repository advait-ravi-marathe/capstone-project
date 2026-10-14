CREATE SCHEMA application;
CREATE SCHEMA payments;

CREATE TABLE application.application_revision (
  revision_id UUID PRIMARY KEY,
  application_id UUID NOT NULL,
  revision INTEGER NOT NULL CHECK (revision > 0),
  quote_hash CHAR(64) NOT NULL,
  revision_hash CHAR(64) NOT NULL,
  terms_hash CHAR(64) NOT NULL,
  UNIQUE (application_id, revision)
);

CREATE TABLE payments.payment_reference (
  payment_id UUID PRIMARY KEY,
  application_id UUID NOT NULL,
  revision INTEGER NOT NULL,
  CONSTRAINT payment_reference_owned_revision_fk
    FOREIGN KEY (application_id, revision)
    REFERENCES application.application_revision (application_id, revision)
);

INSERT INTO application.application_revision(revision_id,application_id,revision,quote_hash,revision_hash,terms_hash) VALUES
 ('98000000-0000-4000-8000-000000000101','98000000-0000-4000-8000-000000000001',1,repeat('a',64),repeat('4',64),repeat('1',64)),
 ('98000000-0000-4000-8000-000000000102','98000000-0000-4000-8000-000000000001',2,repeat('b',64),repeat('5',64),repeat('2',64)),
 ('98000000-0000-4000-8000-000000000103','98000000-0000-4000-8000-000000000002',1,repeat('c',64),repeat('6',64),repeat('3',64));

INSERT INTO payments.payment_reference(payment_id,application_id,revision) VALUES
 ('98000000-0000-4000-8000-000000000201','98000000-0000-4000-8000-000000000001',1),
 ('98000000-0000-4000-8000-000000000202','98000000-0000-4000-8000-000000000001',2),
 ('98000000-0000-4000-8000-000000000203','98000000-0000-4000-8000-000000000002',1);
