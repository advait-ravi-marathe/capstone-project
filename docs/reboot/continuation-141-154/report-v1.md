# Release inspection report v1

The process emits exactly one compact framed record: `COVERSTORY_INSPECTION_REPORT ` followed by one JSON object on a single physical line. Logs remain separate diagnostics. The parent accepts no report on timeout, output overflow, or nonzero exit.

Required allowlisted fields: integer `schemaVersion` (1); exact `key` (`policyId`, integer `sourceVersion`); `workPresent`; nullable captured `envelope` (`policyId`, `sourceVersion`, `eventType`, `effectiveDate`, integer envelope `schemaVersion`); `controlRevision`; nullable `acknowledgedAt`, `quarantinedAt`, and `quarantineReason`; `receiptRelation`; `recommendation` from the existing recovery classifier; `history` (at most 20 entries); and `historySupplementary: true`.

A missing work row is a successful explicit NOT_FOUND observation. Database/read failure exits nonzero and emits no success frame. A matching receipt without acknowledgement means RECONCILE_ACKNOWLEDGMENT. The history is supplementary, separately read, and is not claimed to be atomic with the exact-key observation.

Consumers require one frame, supported schema version, matching requested key, required types and enum/null combinations, integer precision without coercion, and classifier agreement. Duplicate members, malformed/truncated/oversized/deep documents, unknown versions and contradictory evidence fail. Field order is irrelevant; additive optional fields in v1 are ignored. The 64 KiB document and depth-16 limits bound parsing; process output is bounded separately.

Never emit credentials, connection URLs, raw SQL, raw exception text, private owner data, or a made-up deployment-safe decision. This projection is not the persisted receipt payload; the independent database snapshot remains authoritative.

## Standards and framework references

- [Spring Boot JSON support](https://docs.spring.io/spring-boot/reference/features/json.html) documents the project-managed JSON mapper.
- [RFC 8259](https://www.rfc-editor.org/rfc/rfc8259), especially sections 4, 6, 7, and 9, defines the JSON object, number, string, and parser rules used by this local protocol.
