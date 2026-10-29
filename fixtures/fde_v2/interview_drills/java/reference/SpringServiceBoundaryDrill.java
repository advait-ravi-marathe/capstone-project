/** Instructor reference for the bounded service-boundary drill. */
public final class SpringServiceBoundaryDrill {
    record Document(String id, String tenantId, String body) {}

    static String getDocument(Document document, String authenticatedTenant) {
        if (!document.tenantId().equals(authenticatedTenant)) return "DENIED";
        return document.body();
    }

    public static void main(String[] args) {
        Document foreign = new Document("doc-7", "tenant-b", "private");
        String result = getDocument(foreign, "tenant-a");
        if (!"DENIED".equals(result)) throw new AssertionError("cross-tenant result leaked");
        System.out.println("PASS");
    }
}
