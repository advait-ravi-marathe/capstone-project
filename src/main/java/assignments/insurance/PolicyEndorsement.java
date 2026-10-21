package assignments.insurance;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Allowlisted synthetic change snapshot; never contains customer or property PII. */
public record PolicyEndorsement(
        long version,
        LocalDate effectiveDate,
        String changeCode,
        BigDecimal trainingRebuildAmountRupees) { }
