package assignments.delivery;

/** Stable source identity shared by capture, delivery receipt, and acknowledgment. */
public record IssuanceSourceKey(String policyId, long sourceVersion) { }
