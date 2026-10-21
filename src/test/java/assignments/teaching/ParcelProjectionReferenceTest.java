package assignments.teaching;

import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** A small independent example of explicit public fields followed by actual MVC serialization. */
class ParcelProjectionReferenceTest {
    record StoredParcel(String ref, String ownerEmail, int revision, BigDecimal weightKg, String measuredOn) { }
    record ParcelView(String ref, int revision, BigDecimal weightKg, String measuredOn, String notice) { }
    @RestController static class ParcelController {
        private final StoredParcel stored;
        ParcelController(StoredParcel stored) { this.stored = stored; }
        @GetMapping("/teaching/parcels/parcel-17") ResponseEntity<ParcelView> read() {
            return ResponseEntity.ok(new ParcelView(stored.ref(), stored.revision(), stored.weightKg(),
                    stored.measuredOn(), "Recorded measurement only; not a delivery promise."));
        }
    }

    @Test void serializedViewHasOnlyTheFivePublicFieldsAndANumericWeight() throws Exception {
        var stored = new StoredParcel("parcel-17", "private@example.invalid", 4,
                new BigDecimal("2.50"), "2026-10-19");
        var mvc = MockMvcBuilders.standaloneSetup(new ParcelController(stored)).build();
        var response = mvc.perform(get("/teaching/parcels/parcel-17")).andExpect(status().isOk()).andReturn().getResponse();
        var actual = new ObjectMapper().readTree(response.getContentAsString());
        assertEquals(Set.of("ref", "revision", "weightKg", "measuredOn", "notice"), fields(actual));
        assertEquals("parcel-17", actual.get("ref").asString());
        assertEquals(4, actual.get("revision").asInt());
        assertTrue(actual.get("weightKg").isNumber());
        assertEquals(0, new BigDecimal("2.50").compareTo(actual.get("weightKg").decimalValue()),
                "JSON preserves a numeric value, not BigDecimal display scale");
        assertEquals("2026-10-19", actual.get("measuredOn").asString());
        assertEquals("Recorded measurement only; not a delivery promise.", actual.get("notice").asString());
        assertFalse(response.getContentAsString().contains(stored.ownerEmail()));
    }

    private static Set<String> fields(tools.jackson.databind.JsonNode node) {
        var fields = new java.util.HashSet<String>(); fields.addAll(node.propertyNames()); return fields;
    }
}
