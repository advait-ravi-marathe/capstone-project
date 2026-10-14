package coverstory.modules.audit;

/** Implementations append through the caller's transaction with the business mutation. */
public interface BusinessAuditPort {
    void append(BusinessEvent event);
}
