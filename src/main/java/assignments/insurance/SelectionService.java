package assignments.insurance;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.List;

/** Learner-owned atomic persistence of already normalized selections (Day 10). */
public final class SelectionService {
    private final DataSource dataSource;

    public SelectionService(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void persistBatch(List<QuoteSelectionKey> selections) throws SQLException {
        // LEARNER TODO: persist the supplied, pre-normalized pairs in one real
        // database transaction, assigning contiguous zero-based order. On any
        // failure roll back only this batch and preserve pre-existing rows.
        // Do not call the unfinished Day 1 QuoteSelectionPolicy here.
        throw new UnsupportedOperationException("Day 10 learner TODO: implement atomic selection batch");
    }
}
