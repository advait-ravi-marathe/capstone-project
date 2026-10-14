package coverstory.modules.payments;

import java.util.Currency;
import java.util.Objects;

/** Integer minor units with explicit currency; no floating-point money. */
public record Money(long amountMinor, Currency currency) {
    public Money { Objects.requireNonNull(currency, "currency"); }
}
