package coverstory.modules.catalog.api;

/** Immutable exact source/version identity; components are never delimiter encoded. */
public record SourceVersionKey(String sourceId, String sourceVersion) {
    public SourceVersionKey {
        if (!valid(sourceId) || !valid(sourceVersion)) {
            throw new IllegalArgumentException("source and version must be nonblank exact values");
        }
    }

    private static boolean valid(String value) {
        return value != null && !value.isBlank() && value.equals(value.strip());
    }
}
