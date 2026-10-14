INSERT INTO catalog.approved_quote(quote_id,owner_id,quote_version,source_illustration_id,source_state,total_minor,currency,terms_hash,quote_revision)
VALUES
('78000000-0000-4000-8000-000000000201','78000000-0000-4000-8000-000000000001','quote-health-A-v1','78000000-0000-4000-8000-000000000301','ACTIVE',100000,'INR','aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa',2);

INSERT INTO application.journey(journey_id,owner_id,application_id,revision,quote_id,status,current_step,updated_at)
VALUES ('78000000-0000-4000-8000-000000000101','78000000-0000-4000-8000-000000000001',
        '78000000-0000-4000-8000-000000000111',4,'78000000-0000-4000-8000-000000000201',
        'DRAFT','QUOTE_REVIEW','2026-11-20T10:00:00Z');

INSERT INTO application.application_record(application_id,owner_id,revision,quote_id,status,updated_at)
VALUES ('78000000-0000-4000-8000-000000000111','78000000-0000-4000-8000-000000000001',
        4,'78000000-0000-4000-8000-000000000201','DRAFT','2026-11-20T10:00:00Z');

INSERT INTO application.quote_validation(validation_id,owner_id,journey_id,quote_id,quote_revision,
        accepted_total_minor,terms_hash,expires_at,accepted)
VALUES
('78000000-0000-4000-8000-000000000401','78000000-0000-4000-8000-000000000001',
 '78000000-0000-4000-8000-000000000101','78000000-0000-4000-8000-000000000201',2,100000,
 'aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa','2026-11-20T10:05:00Z',TRUE);
