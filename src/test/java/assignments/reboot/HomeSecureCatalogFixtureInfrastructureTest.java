package assignments.reboot;

import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Checks only the instructor-owned synthetic fixture; it does not implement learner policy. */
class HomeSecureCatalogFixtureInfrastructureTest {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void fixturePinsOneSyntheticProductAndTrainingRules() throws IOException {
        try (InputStream input = getClass().getResourceAsStream(
                "/assignments/reboot/homesecure-catalog.json")) {
            assertNotNull(input, "synthetic HomeSecure fixture must be on the test classpath");
            JsonNode fixture = objectMapper.readTree(input);

            assertTrue(fixture.get("syntheticOnly").asBoolean());
            assertEquals("homesecure", fixture.get("productCode").asString());
            assertEquals("home-secure-v1", fixture.get("productVersion").asString());
            assertEquals("INR", fixture.get("currency").asString());
            assertEquals("BUILDING", fixture.get("coverageOptions").get(0).get("code").asString());
            assertEquals("BUILDING_CONTENTS", fixture.get("coverageOptions").get(1).get("code").asString());
            assertEquals("100000.00", fixture.get("eligibility")
                    .get("minimumRebuildAmountRupees").asString());
            assertEquals("10000000.00", fixture.get("eligibility")
                    .get("maximumRebuildAmountRupees").asString());
            assertEquals("0.00125", fixture.get("premiumTrainingRule").get("rate").asString());
            assertEquals("HALF_UP", fixture.get("premiumTrainingRule").get("roundingMode").asString());
            assertEquals(15, fixture.get("quoteLifetimeMinutes").asInt());
            assertTrue(fixture.get("disclaimer").asString().contains("not an insurance offer"));
        }
    }
}
