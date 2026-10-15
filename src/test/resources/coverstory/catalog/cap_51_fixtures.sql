INSERT INTO catalog_import_evidence VALUES
 ('editor-E','source-A','PUBLISHED',10000,'INR','2026-10-13T10:00:00Z'),
 ('editor-E','source-B','PUBLISHED',2000,'INR','2026-10-13T11:00:00Z'),
 ('editor-E','source-C','PUBLISHED',NULL,'INR','2026-10-14T14:00:00Z'),
 ('editor-E','scope-only','PUBLISHED',300,'INR','2026-10-12T12:00:00Z'),
 ('editor-F','scope-only','PUBLISHED',90000,'INR','2026-10-12T12:00:00Z'),
 ('editor-E','draft-D','DRAFT',5000,'INR','2026-10-13T12:00:00Z');
INSERT INTO catalog_import_attempt VALUES
 ('editor-E','source-A','attempt-1',true,'2026-10-13T10:01:00Z'),
 ('editor-E','source-A','attempt-2',true,'2026-10-13T10:02:00Z'),
 ('editor-F','scope-only','attempt-F',true,'2026-10-12T12:01:00Z'),
 ('editor-E','source-B','attempt-failed',false,'2026-10-13T11:01:00Z');
