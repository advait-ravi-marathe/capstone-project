import java.util.Map;
import java.util.Set;

/** Isolated backend interview drill; not capstone application code. */
public class TenantScopeDrill {
    static void check(boolean condition, String label) {
        if (!condition) throw new AssertionError(label);
    }
    static boolean mayRead(String requestTenant, String documentTenant, String subject,
                           String owner, boolean isReader) {
        return requestTenant.equals(documentTenant)
                && (subject.equals(owner) || isReader);
    }
    public static void main(String[] args) {
        check(mayRead("t1", "t1", "owner", "owner", false), "same-tenant owner");
        check(!mayRead("t2", "t1", "owner", "owner", false), "cross-tenant owner denied");
        check(!mayRead("t2", "t1", "reader", "owner", true), "cross-tenant reader denied");
        System.out.println("PASS");
    }
}
