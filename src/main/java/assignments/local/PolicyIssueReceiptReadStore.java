package assignments.local;

import assignments.insurance.PolicySnapshot;
import assignments.insurance.PolicyStatus;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Optional;

/** Reads the existing issue receipt so the domain service can decide replay status. */
public final class PolicyIssueReceiptReadStore {
    private static final String FIND = """
            SELECT p.policy_id, p.owner_id, p.journey_id, p.quote_version_id, p.product_version,
                   p.rate_version, p.annual_premium_rupees, p.currency, p.effective_date,
                   p.status, p.version, p.cancellation_effective_date
              FROM training_policy_idempotency i
              JOIN training_policy_lifecycle p ON p.policy_id = i.policy_id AND p.owner_id = i.owner_id
             WHERE i.owner_id = ? AND i.client_request_id = ?
            """;
    private final DataSource dataSource;

    public PolicyIssueReceiptReadStore(DataSource dataSource) { this.dataSource = dataSource; }

    public Optional<PolicySnapshot> find(String ownerId, String clientRequestId) {
        try (Connection connection = dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement(FIND)) {
            statement.setString(1, ownerId);
            statement.setString(2, clientRequestId);
            try (ResultSet rows = statement.executeQuery()) {
                if (!rows.next()) return Optional.empty();
                return Optional.of(new PolicySnapshot(rows.getString("policy_id"), rows.getString("owner_id"),
                        rows.getString("journey_id"), rows.getString("quote_version_id"),
                        rows.getString("product_version"), rows.getString("rate_version"),
                        rows.getBigDecimal("annual_premium_rupees"), rows.getString("currency"),
                        rows.getDate("effective_date").toLocalDate(), PolicyStatus.valueOf(rows.getString("status")),
                        rows.getLong("version"), java.util.List.of(),
                        rows.getDate("cancellation_effective_date") == null ? null
                                : rows.getDate("cancellation_effective_date").toLocalDate()));
            }
        } catch (Exception exception) {
            throw new IllegalStateException("Could not read synthetic issue receipt", exception);
        }
    }
}
