package assignments.delivery;

import java.util.List;

/** Database operations; transaction boundaries are owned by the dispatcher. */
public interface IssuanceWorkStore {
    int captureCommittedIssuances(int limit);
    List<IssuanceEnvelope> findPending(int limit);
    DeliveryReceiptResult acceptReceipt(IssuanceEnvelope envelope);
    boolean acknowledge(IssuanceEnvelope envelope);
}
