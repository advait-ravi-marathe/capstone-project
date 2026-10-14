package coverstory.modules.catalog;

/** Exact sequential import identity; this key is not authentication or concurrent idempotency. */
public record ImportIdentityKey(String editorFixture, String sourceId, String sourceVersion) { }
