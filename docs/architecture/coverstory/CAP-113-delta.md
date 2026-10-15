# CAP-113 mock explanation usage ledger

```mermaid
erDiagram
  EXPLANATION_REQUEST ||--o| USAGE_RECEIPT : records
  USAGE_RECEIPT }o--|| TARIFF_VERSION : priced_by
  OWNER_USAGE_AGGREGATE }o--o{ USAGE_RECEIPT : aggregates
  PREMIUM_PAYMENT_LEDGER {
    long amount_minor INR
    string payment_state
  }
```

The usage key is `(owner_id, request_id)`. Fixed token counts and a versioned synthetic rate produce an exact decimal estimate. Missing token counts remain UNKNOWN. The usage currency/provenance is separate from integer paise premium/payment accounting. This is an offline mock; no model/provider call or vendor billing record is present.
