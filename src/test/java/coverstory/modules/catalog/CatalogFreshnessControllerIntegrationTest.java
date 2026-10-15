package coverstory.modules.catalog;

import coverstory.modules.catalog.api.CatalogFreshnessController;
import coverstory.modules.catalog.api.CatalogFreshnessPort;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Map;
import java.nio.file.Files;
import java.nio.file.Path;
import tools.jackson.databind.ObjectMapper;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

class CatalogFreshnessControllerIntegrationTest {
    private static MockMvc mvcWith(CatalogFreshnessPort port) {
        DefaultListableBeanFactory beans = new DefaultListableBeanFactory();
        if (port != null) beans.registerSingleton("freshness", port);
        return MockMvcBuilders.standaloneSetup(new CatalogFreshnessController(beans.getBeanProvider(CatalogFreshnessPort.class))).build();
    }

    @Test void missingLearnerFreshnessProviderIsExplicit501() throws Exception {
        MvcResult response = mvcWith(null).perform(get("/api/catalog/source-freshness")).andReturn();
        assertEquals(501, response.getResponse().getStatus());
        assertTrue(response.getResponse().getContentAsString().contains("FRESHNESS_NOT_IMPLEMENTED"));
    }

    @Test void typedSyntheticProjectionIsReturnedWithoutQuoteEligibility() throws Exception {
        Map<String, Object> report = Map.of("schemaMajor", 1, "status", "FRESH", "rows", java.util.List.of(), "quoteOrderEligible", false);
        MvcResult response = mvcWith(() -> report).perform(get("/api/catalog/source-freshness")).andReturn();
        assertEquals(200, response.getResponse().getStatus());
        assertTrue(response.getResponse().getContentAsString().contains("\"quoteOrderEligible\":false"));
    }

    @Test void localBridgeCallsActualLearnerSeamAndExposesItsTodoAs501() throws Exception {
        Path root = Path.of(System.getProperty("user.dir"));
        var adapter = new coverstory.modules.catalog.PythonCAP79FreshnessAdapter("python3", root,
                root.resolve("scripts/cap79_runtime_adapter.py"), new ObjectMapper());
        MvcResult response = mvcWith(adapter).perform(get("/api/catalog/source-freshness")).andReturn();
        assertEquals(501, response.getResponse().getStatus());
        assertTrue(response.getResponse().getContentAsString().contains("FRESHNESS_NOT_IMPLEMENTED"));
    }

    @Test void localBridgeRendersAValidReferenceOutputThroughTheSameHttpProjection() throws Exception {
        Path root = Files.createTempDirectory("cap79-reference-bridge-");
        Path output = root.resolve("reference.py");
        Files.writeString(output, "print('{\\\"schemaMajor\\\":1,\\\"status\\\":\\\"FRESH\\\",\\\"rows\\\":[{\\\"sourceId\\\":\\\"health-plan-A\\\",\\\"version\\\":\\\"v2\\\",\\\"status\\\":\\\"FRESH\\\",\\\"evidenceType\\\":\\\"HEALTH_SOURCE_EXCERPT\\\",\\\"contentSha256\\\":\\\""+"6".repeat(64)+"\\\"}],\\\"quoteOrderEligible\\\":false}')\n");
        try {
            var adapter = new coverstory.modules.catalog.PythonCAP79FreshnessAdapter("python3", root, output, new ObjectMapper());
            MvcResult response = mvcWith(adapter).perform(get("/api/catalog/source-freshness")).andReturn();
            assertEquals(200, response.getResponse().getStatus());
            assertTrue(response.getResponse().getContentAsString().contains("health-plan-A"));
            assertTrue(response.getResponse().getContentAsString().contains("\"quoteOrderEligible\":false"));
        } finally {
            Files.deleteIfExists(output); Files.deleteIfExists(root);
        }
    }
}
