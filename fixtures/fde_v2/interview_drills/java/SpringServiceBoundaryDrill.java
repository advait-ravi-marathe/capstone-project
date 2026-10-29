/** Framework-free Spring-style controller/service boundary contract. */
public final class SpringServiceBoundaryDrill {
    record Document(String id, String tenantId, String body) {}

    static String getDocument(Document document, String authenticatedTenant) {
        return document.body(); // seeded defect: service ignores authenticated tenant
    }

    public static void main(String[] args) {
        Document foreign = new Document("doc-7", "tenant-b", "private");
        String result = getDocument(foreign, "tenant-a");
        if (!"DENIED".equals(result)) {
            throw new AssertionError("controller/service boundary returned cross-tenant data: " + result);
        }
        System.out.println("PASS");
    }
}
