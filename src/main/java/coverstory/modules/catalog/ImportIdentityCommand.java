package coverstory.modules.catalog;

/** Synthetic editor import command; canonicalHash is opaque exact input. */
public record ImportIdentityCommand(String editorFixture, String sourceId,
                                    String sourceVersion, String canonicalHash) { }
