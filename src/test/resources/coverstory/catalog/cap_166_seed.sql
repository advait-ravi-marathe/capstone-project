INSERT INTO catalog.health_offer_version
    (product_id, version_id, source_version, restrictions, amount_minor, currency, content_sha256)
VALUES
    ('5f1b8b02-1b68-4de8-928c-f3eaa5f80001','16600000-0000-4000-8000-000000000001','v1',
     '{"fixture":"CAP-166"}'::jsonb,0,'INR',decode(repeat('11',32),'hex')),
    ('5f1b8b02-1b68-4de8-928c-f3eaa5f80001','16600000-0000-4000-8000-000000000002','v2',
     '{"fixture":"CAP-166"}'::jsonb,0,'INR',decode(repeat('22',32),'hex')),
    ('5f1b8b02-1b68-4de8-928c-f3eaa5f80002','16600000-0000-4000-8000-000000000003','v1',
     '{"fixture":"CAP-166"}'::jsonb,0,'INR',decode(repeat('33',32),'hex'));

INSERT INTO catalog.health_offer_discovery_metadata
    (product_id, version_id, category, scenario_id, source_id, evidence_status, display_name)
VALUES
    ('5f1b8b02-1b68-4de8-928c-f3eaa5f80001','16600000-0000-4000-8000-000000000001','health','individual','health-example-a','SYNTHETIC','Synthetic health sample A'),
    ('5f1b8b02-1b68-4de8-928c-f3eaa5f80001','16600000-0000-4000-8000-000000000002','health','individual','health-example-a','SYNTHETIC','Synthetic health sample A'),
    ('5f1b8b02-1b68-4de8-928c-f3eaa5f80002','16600000-0000-4000-8000-000000000003','health','individual','health-example-b','SYNTHETIC','Synthetic health sample B');

INSERT INTO catalog.active_health_offer(product_id, version_id)
VALUES
    ('5f1b8b02-1b68-4de8-928c-f3eaa5f80001','16600000-0000-4000-8000-000000000002'),
    ('5f1b8b02-1b68-4de8-928c-f3eaa5f80002','16600000-0000-4000-8000-000000000003');
