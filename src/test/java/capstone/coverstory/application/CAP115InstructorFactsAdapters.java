package capstone.coverstory.application;

import coverstory.modules.application.api.OfferRevisionFactsPort;
import coverstory.modules.payments.api.OfferPaymentFactsPort;
import coverstory.modules.support.api.AuthorizedOfferEvidencePort;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.sql.Array;
import java.sql.ResultSet;
import java.util.*;

/** Test-only adapters keep each fake read behind the module that owns those facts. */
final class CAP115ApplicationFactsAdapter implements OfferRevisionFactsPort {
    private final JdbcTemplate jdbc;
    CAP115ApplicationFactsAdapter(DataSource ds){jdbc=new JdbcTemplate(ds);}
    @Override public OfferPair findCurrentPair(UUID caseId) {
        if(!UUID.fromString("11500000-0000-4000-8000-000000000099").equals(caseId))return null;
        return new OfferPair(find("ORIGINAL"),find("REVISED"));
    }
    private OfferFacts find(String role) {
        return jdbc.queryForObject("SELECT owner_id,offer_id,revision,content_sha256,amount_minor,currency,term_codes,expires_at FROM cap115_harness.offer_snapshot WHERE snapshot_role=?",(rs,n)->facts(rs),role);
    }
    private static OfferFacts facts(ResultSet rs) throws java.sql.SQLException {
        Array codes=rs.getArray("term_codes");
        @SuppressWarnings("unchecked") String[] values=(String[])codes.getArray();
        return new OfferFacts(rs.getObject("owner_id",UUID.class),rs.getObject("offer_id",UUID.class),rs.getString("revision"),
                rs.getString("content_sha256").trim(),rs.getLong("amount_minor"),rs.getString("currency"),Set.of(values),rs.getTimestamp("expires_at").toInstant());
    }
}

final class CAP115SupportEvidenceAdapter implements AuthorizedOfferEvidencePort {
    private final JdbcTemplate jdbc;
    CAP115SupportEvidenceAdapter(DataSource ds){jdbc=new JdbcTemplate(ds);}
    @Override public Set<EvidenceCitation> authorizedFor(UUID ownerId) {
        return Set.copyOf(jdbc.query("SELECT owner_id,source_id,version FROM cap115_harness.authorized_source WHERE owner_id=?",
                (rs,n)->new EvidenceCitation(rs.getObject("owner_id",UUID.class),rs.getString("source_id"),rs.getString("version")),ownerId));
    }
}

final class CAP115PaymentsFactsAdapter implements OfferPaymentFactsPort {
    @Override public PaymentFact findForOffer(UUID offerId) { return new PaymentFact("NOT_STARTED",0,false); }
}

final class CAP115ChoiceStore {
    private final JdbcTemplate jdbc;
    CAP115ChoiceStore(DataSource ds){jdbc=new JdbcTemplate(ds);}
    int record(UUID owner,OfferRevisionFactsPort.OfferFacts offer,String choice) {
        return jdbc.update("INSERT INTO cap115_harness.explicit_quote_choice(choice_id,owner_id,offer_id,revision,content_sha256,choice) VALUES(?,?,?,?,?,?) ON CONFLICT(owner_id,offer_id,revision) DO NOTHING",
                UUID.randomUUID(),owner,offer.offerId(),offer.revision(),offer.contentSha256(),choice);
    }
    int count(){return jdbc.queryForObject("SELECT count(*) FROM cap115_harness.explicit_quote_choice",Integer.class);}
}
