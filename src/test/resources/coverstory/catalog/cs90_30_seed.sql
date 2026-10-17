INSERT INTO catalog.health_offer_version (product_id,version_id,source_version,restrictions,amount_minor,currency,content_sha256) VALUES
('5f1b8b02-1b68-4de8-928c-f3eaa5f80001','a8100000-0000-4000-8000-000000000001','A/v1','{"source":"synthetic"}'::jsonb,123456,'INR',decode('cb4a74791080a86290ead23a0e21db92cb5a0e8a77fd612830683158b5bd8160','hex')),
('5f1b8b02-1b68-4de8-928c-f3eaa5f80002','b8100000-0000-4000-8000-000000000001','B/v1','{"source":"synthetic"}'::jsonb,143456,'INR',decode('7773be7dae3f70e9dde64f20dcb23ea101e6858643ff88937a53b607d2a2cd1b','hex')),
('5f1b8b02-1b68-4de8-928c-f3eaa5f80003','c8100000-0000-4000-8000-000000000001','C/v1','{"source":"synthetic"}'::jsonb,153456,'INR',decode('dd23ecc3efba36f4a9dc41dae3fd1bb5f1f9ecb1be5543eb4d244a4b400b8747','hex'));
INSERT INTO catalog.health_offer_discovery_metadata (product_id,version_id,category,scenario_id,source_id,evidence_status,display_name) VALUES
('5f1b8b02-1b68-4de8-928c-f3eaa5f80001','a8100000-0000-4000-8000-000000000001','health','individual','synthetic-health-A','SYNTHETIC','synthetic-health-A A/v1'),
('5f1b8b02-1b68-4de8-928c-f3eaa5f80002','b8100000-0000-4000-8000-000000000001','health','individual','synthetic-health-B','SYNTHETIC','synthetic-health-B B/v1'),
('5f1b8b02-1b68-4de8-928c-f3eaa5f80003','c8100000-0000-4000-8000-000000000001','health','individual','synthetic-health-C','SYNTHETIC','synthetic-health-C C/v1');
