package assignments.insurance;

import java.math.BigDecimal;
import java.time.LocalDate;

public record PolicyEndorsementRequest(
        long expectedVersion,
        LocalDate effectiveDate,
        String changeCode,
        BigDecimal trainingRebuildAmountRupees) { }
