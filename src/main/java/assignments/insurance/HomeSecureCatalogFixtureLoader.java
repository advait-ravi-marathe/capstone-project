package assignments.insurance;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/** Instructor-owned fixture plumbing. It loads fixed data and contains no product decision rules. */
public final class HomeSecureCatalogFixtureLoader {
    private static final String FIXTURE = "/assignments/reboot/homesecure-catalog.json";
    private final ObjectMapper objectMapper = new ObjectMapper();

    public List<ProductCatalogProduct> load() throws IOException {
        try (InputStream input = getClass().getResourceAsStream(FIXTURE)) {
            if (input == null) {
                throw new IOException("Missing synthetic HomeSecure catalog fixture");
            }
            JsonNode root = objectMapper.readTree(input);
            List<CoverageOption> options = new ArrayList<>();
            for (JsonNode option : root.get("coverageOptions")) {
                options.add(new CoverageOption(
                        option.get("code").asString(),
                        option.get("label").asString(),
                        new BigDecimal(option.get("annualAdjustmentRupees").asString())));
            }
            return List.of(new ProductCatalogProduct(
                    root.get("productCode").asString(),
                    root.get("displayName").asString(),
                    root.get("productVersion").asString(),
                    root.get("currency").asString(),
                    options));
        }
    }
}
