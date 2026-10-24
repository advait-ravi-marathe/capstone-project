"""Fixed, synthetic, non-customer clause fixture."""
from .evidence import Clause

CLAUSES = (
    Clause("synthetic-renewal-01", "v1", "owner-a", "Renewal requires a current account."),
    Clause("synthetic-claim-02", "v1", "owner-a", "Claims must include a receipt."),
    Clause("synthetic-private-03", "v1", "owner-b", "Private account condition for owner B."),
)
