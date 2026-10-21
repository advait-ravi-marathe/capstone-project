package assignments.delivery.recovery;

import assignments.delivery.IssuanceSourceKey;

import java.util.Objects;

public record ReleaseInspectionRequest(IssuanceSourceKey key) {
    public ReleaseInspectionRequest { Objects.requireNonNull(key); }
}
