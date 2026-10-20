package assignments.insurance;

import java.util.List;

/** Allowlisted journey response projection. */
public record JourneySummary(String journeyId, JourneyQuoteSummary quote,
                             List<JourneySelectionSummary> selections) {
    public JourneySummary {
        selections = List.copyOf(selections);
    }
}
