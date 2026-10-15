# CAP-112 instructor note

The instructor transaction records unique source event identity before state changes, applies only a newer event to a submitted synthetic application, and leaves earlier events as audit observations. Duplicate delivery emits a bounded dedup outcome. Provider exception text is never serialized; safe diagnostics have a fixed route code, enumerated outcome, bounded duration and request ID. This harness does not prove a production insurer callback pipeline.
