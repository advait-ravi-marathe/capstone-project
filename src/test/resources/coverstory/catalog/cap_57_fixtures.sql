INSERT INTO catalog.health_offer_version
    (product_id, version_id, source_version, restrictions, amount_minor, currency, content_sha256)
VALUES
    ('5f1b8b02-1b68-4de8-928c-f3eaa5f80001','a8100000-0000-4000-8000-000000000001','source-A/v1',
     '{"waiting_period_months":24,"exclusions":["synthetic-exclusion-A"]}'::jsonb,123456,'INR',decode('cb4a74791080a86290ead23a0e21db92cb5a0e8a77fd612830683158b5bd8160','hex')),
    ('5f1b8b02-1b68-4de8-928c-f3eaa5f80001','a8100000-0000-4000-8000-000000000002','source-A/v2',
     '{"waiting_period_months":12,"exclusions":["synthetic-exclusion-A"]}'::jsonb,133456,'INR',decode('e51e4dced2050c681003db6715ed4a7365c286ed1dd3c1ce5fae673bfdcfe242','hex')),
    ('5f1b8b02-1b68-4de8-928c-f3eaa5f80001','a8100000-0000-4000-8000-000000000003','source-B/v1',
     '{"waiting_period_months":18,"exclusions":["synthetic-exclusion-B"]}'::jsonb,143456,'INR',decode('7773be7dae3f70e9dde64f20dcb23ea101e6858643ff88937a53b607d2a2cd1b','hex'));
