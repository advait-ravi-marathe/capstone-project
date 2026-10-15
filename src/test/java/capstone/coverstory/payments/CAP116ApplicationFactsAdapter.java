package capstone.coverstory.payments;

import coverstory.modules.application.api.AcceptedOfferRevisionFactsPort;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.UUID;

/** Test-only fixture adapter standing in for Application's accepted-revision facts API. */
final class CAP116ApplicationFactsAdapter implements AcceptedOfferRevisionFactsPort {
    private final JdbcTemplate jdbc;
    CAP116ApplicationFactsAdapter(javax.sql.DataSource dataSource) { jdbc = new JdbcTemplate(dataSource); }

    @Override public AcceptedOfferFacts findAcceptedOffer(UUID applicationId) {
        return jdbc.query("SELECT owner_id,application_id,offer_id,revision,content_sha256,total_minor,currency,accepted FROM cap116_harness.accepted_offer_revision WHERE application_id=?",
                rs -> rs.next() ? new AcceptedOfferFacts(rs.getObject("owner_id", UUID.class), rs.getObject("application_id", UUID.class),
                        rs.getObject("offer_id", UUID.class), rs.getString("revision"), rs.getString("content_sha256"),
                        rs.getLong("total_minor"), rs.getString("currency").trim(), rs.getBoolean("accepted")) : null, applicationId);
    }
}
