package coverstory.modules.payments.api;

import coverstory.modules.payments.api.CAP80Seam.OwnerPaymentItem;
import coverstory.modules.payments.api.CAP80Seam.PaymentCheckpoint;
import coverstory.modules.payments.api.CAP80Seam.PaymentEffectPort;
import coverstory.modules.payments.api.CAP80Seam.ReplayResult;

/** Injectable operation boundary; production wiring is a method reference to the learner seam. */
@FunctionalInterface
public interface CAP80ReplayAction {
    ReplayResult apply(OwnerPaymentItem item, PaymentCheckpoint checkpoint, PaymentEffectPort effects);
}
