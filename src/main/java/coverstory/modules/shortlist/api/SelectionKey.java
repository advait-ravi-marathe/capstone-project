package coverstory.modules.shortlist.api;

/** Exact composite identity; device scope is local identity, never authentication. */
public record SelectionKey(String deviceScope, String catalogVersionId) { }
