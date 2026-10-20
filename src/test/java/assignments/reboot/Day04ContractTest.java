package assignments.reboot;

import assignments.insurance.HomeSecureCatalogFixtureLoader;
import assignments.insurance.ProductCatalogProduct;
import assignments.insurance.ProductCatalogService;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Day 4 exercises only ProductCatalogService; prior code is fixture plumbing. */
@Tag("learnerAcceptance")
class Day04ContractTest {
    private ProductCatalogService service() throws IOException {
        List<ProductCatalogProduct> fixture = new HomeSecureCatalogFixtureLoader().load();
        return new ProductCatalogService(fixture);
    }

    @Test
    void returnsOneHomeSecureProductAndExactOptionOrder() throws IOException {
        var product = service().findByCode("homesecure").orElseThrow();
        assertEquals("HomeSecure Training Product", product.displayName());
        assertEquals("home-secure-v1", product.productVersion());
        assertEquals("INR", product.currency());
        assertEquals(List.of("BUILDING", "BUILDING_CONTENTS"),
                product.coverageOptions().stream().map(option -> option.code()).toList());
    }

    @Test
    void unknownCodeIsAbsentAndReturnedOptionsAreImmutable() throws IOException {
        var service = service();
        assertFalse(service.findByCode("another-product").isPresent());
        var product = service.findByCode("homesecure").orElseThrow();
        assertTrue(product.coverageOptions().size() > 0);
        assertThrows(UnsupportedOperationException.class, () -> product.coverageOptions().clear());
    }
}
