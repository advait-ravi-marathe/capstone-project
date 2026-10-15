-- Optional local-only teaching rows. Apply once to a fresh database after schema/constraints.sql.
-- No PII, credentials, provider records, real payment, insurer dispatch, or active cover.
INSERT INTO application.application_record(application_id,owner_id,revision,quote_id,status) VALUES
 ('a2000000-0000-4000-8000-000000000001','a1680000-0000-4000-8000-000000000001',1,'a4000000-0000-4000-8000-000000000001','AWAITING_PAYMENT'),
 ('b2000000-0000-4000-8000-000000000002','b1680000-0000-4000-8000-000000000002',1,'b4000000-0000-4000-8000-000000000002','AWAITING_PAYMENT');

INSERT INTO application.journey(journey_id,owner_id,application_id,revision,quote_id,status) VALUES
 ('a3000000-0000-4000-8000-000000000001','a1680000-0000-4000-8000-000000000001',
  'a2000000-0000-4000-8000-000000000001',1,'a4000000-0000-4000-8000-000000000001','DRAFT'),
 ('b3000000-0000-4000-8000-000000000002','b1680000-0000-4000-8000-000000000002',
  'b2000000-0000-4000-8000-000000000002',1,'b4000000-0000-4000-8000-000000000002','DRAFT');

INSERT INTO application.onboarding_case(application_id,owner_id,quote_id,state,payment_event_id) VALUES
 ('a2000000-0000-4000-8000-000000000001','a1680000-0000-4000-8000-000000000001','quote-health-A-v1','AWAITING_PAYMENT','pending-A'),
 ('b2000000-0000-4000-8000-000000000002','b1680000-0000-4000-8000-000000000002','quote-health-B-v1','AWAITING_PAYMENT','pending-B');

INSERT INTO payments.payment_checkpoint(owner_id,last_sequence) VALUES
 ('a1680000-0000-4000-8000-000000000001',2),
 ('b1680000-0000-4000-8000-000000000002',2);

INSERT INTO payments.mock_confirmation_feed(event_id,owner_id,sequence,application_id,quote_id,amount_minor,currency,confirmed) VALUES
 ('demo-event-A-seq3','a1680000-0000-4000-8000-000000000001',3,'a2000000-0000-4000-8000-000000000001','quote-health-A-v1',100000,'INR',true),
 ('demo-event-A-seq5','a1680000-0000-4000-8000-000000000001',5,'a2000000-0000-4000-8000-000000000001','quote-health-A-v1',100000,'INR',true);
