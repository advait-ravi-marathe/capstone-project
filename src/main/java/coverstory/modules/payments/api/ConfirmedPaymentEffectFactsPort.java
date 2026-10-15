package coverstory.modules.payments.api;

import capstone.coverstory.payments.internal.AcceptedRevisionBalance.ConfirmedEffect;
import java.util.List;
import java.util.UUID;

/** Payments-owned confirmed immutable effects and receipt observations, read only. */
public interface ConfirmedPaymentEffectFactsPort {
    List<ConfirmedEffect> findObservedEffects(UUID applicationId);
}
