package assignments.learning;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

class Day05DecisionTableReferenceTest {
    static String parcelDecision(String destination, Integer kg) {
        if (destination == null || kg == null) return "MISSING";
        if (!destination.equals("LOCAL")) return "LOCAL_ONLY";
        if (kg < 1 || kg > 20) return "WEIGHT_RANGE";
        return "ACCEPT";
    }

    @Test void frozenTableAndRulePrecedence() {
        Object[][] rows = {
            {null, 30, "MISSING"}, {"REMOTE", null, "MISSING"},
            {"REMOTE", 30, "LOCAL_ONLY"}, {"LOCAL", 0, "WEIGHT_RANGE"},
            {"LOCAL", 1, "ACCEPT"}, {"LOCAL", 20, "ACCEPT"},
            {"LOCAL", 21, "WEIGHT_RANGE"}
        };
        for (Object[] row : rows) {
            assertEquals(row[2], parcelDecision((String) row[0], (Integer) row[1]));
        }
        assertEquals("LOCAL_ONLY", parcelDecision("REMOTE", 30));
        assertEquals("LOCAL_ONLY", parcelDecision("REMOTE", 30));
        assertEquals("ACCEPT", parcelDecision("LOCAL", 19));
        assertEquals("LOCAL_ONLY", parcelDecision("REMOTE", 0));
    }
}
