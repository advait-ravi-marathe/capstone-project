package coverstory.modules.catalog;

import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;

/** Read-only adapter over Catalog-owned tables; it returns both active and retired source history. */
public final class JdbcCatalogDiscoveryAdapter implements CatalogDiscoveryPort {
    private static final String SQL = """
            SELECT version_id::text, category, scenario_id, source_id, source_version,
                   evidence_status, publication_status
              FROM catalog.health_offer_discovery_export
             ORDER BY source_id, source_version, version_id
            """;
    private final JdbcTemplate jdbc;

    public JdbcCatalogDiscoveryAdapter(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    @Override public List<CatalogDiscoveryVersion> listHealthVersions() {
        return jdbc.query(SQL, (rs, row) -> new CatalogDiscoveryVersion(
                rs.getString("version_id"), rs.getString("category"), rs.getString("scenario_id"),
                rs.getString("source_id"), rs.getString("source_version"),
                rs.getString("evidence_status"), rs.getString("publication_status")));
    }
}
