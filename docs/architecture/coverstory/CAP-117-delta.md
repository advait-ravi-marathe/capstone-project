# CAP-117 evidence bundle delta — proposed

```mermaid
flowchart LR
  R[CAP-112 review result] --> M[ReviewEvidenceManifest]
  X[CAP-113 explanation ledger] --> M
  A[CAP-114 review alert] --> M
  O[CAP-115 accepted offer o2] --> M
  B[CAP-116 balance reconciliation] --> M
  M --> V{hash, revision, expiry valid?}
  V -->|yes| N[RECONCILE_BALANCE; review still required]
  V -->|no| I[INVALID with named evidence issue]
```

The Application manifest binds each artifact path to a SHA-256 and revision. It reuses CAP-115 `o2` owner/offer terms hash. Verification establishes synthetic fixture integrity and scoped facts only; it is not evidence of insurer approval, policy issuance, payment dispatch or active cover. The production `verify` method is an explicit learner TODO. Only the independent instructor oracle is runnable.
