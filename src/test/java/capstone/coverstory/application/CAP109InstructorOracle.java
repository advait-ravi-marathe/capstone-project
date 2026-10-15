package capstone.coverstory.application;

import java.util.*;

/** Independent metadata-only oracle; never reads or writes protected payloads or payment systems. */
final class CAP109InstructorOracle {
    record Version(String owner, UUID applicationId, long revision, String sourceVersion, String envelopeRef,
                   Map<String, String> digests, long totalMinor, String currency, String termsDigest) { }
    record Event(long sequence, UUID id, String state, String providerReference, long postedMinor) { }
    record History(List<Event> prior, List<Event> current, long priorJourneyRevision, long currentJourneyRevision,
                   boolean auditPresent, boolean stepPresent, boolean applied, List<String> telemetry) { }
    record Delta(Map<String, String> changed, List<String> added, List<String> removed, long amountDeltaMinor,
                 boolean reviewRequired, long postedEffectMinor, List<String> findings) { }

    static Delta compare(Version before, Version after, History history) {
        List<String> findings = new ArrayList<>();
        if (!before.owner().equals(after.owner()) || !before.applicationId().equals(after.applicationId()))
            return new Delta(Map.of(), List.of(), List.of(), 0, false, 0, List.of("FORBIDDEN"));
        if (!appendOnly(history.prior(), history.current())) findings.add("APPEND_ONLY_VIOLATION");
        if (history.applied() && !history.auditPresent()) findings.add("MISSING_AUDIT");
        if (history.applied() && !history.stepPresent()) findings.add("MISSING_STEP_EVENT");
        if (history.applied() && history.currentJourneyRevision() != history.priorJourneyRevision() + 1) findings.add("REVISION_MISMATCH");

        TreeMap<String, String> changed = new TreeMap<>();
        TreeSet<String> added = new TreeSet<>(after.digests().keySet());
        added.removeAll(before.digests().keySet());
        TreeSet<String> removed = new TreeSet<>(before.digests().keySet());
        removed.removeAll(after.digests().keySet());
        for (String key : before.digests().keySet()) {
            if (after.digests().containsKey(key) && !Objects.equals(before.digests().get(key), after.digests().get(key)))
                changed.put(key, before.digests().get(key) + "->" + after.digests().get(key));
        }
        long delta = after.totalMinor() - before.totalMinor();
        boolean review = delta != 0 || !Objects.equals(before.termsDigest(), after.termsDigest());
        Set<String> postedRefs = new HashSet<>();
        long posted = 0;
        long priorSequence = 0;
        Set<UUID> ids = new HashSet<>();
        for (Event event : history.current()) {
            if (event.sequence() <= priorSequence || !ids.add(event.id())) findings.add("AUDIT_SEQUENCE_OR_ID_INVALID");
            priorSequence = event.sequence();
            if ("RECONCILED_SUCCEEDED".equals(event.state()) && event.providerReference() != null && postedRefs.add(event.providerReference())) posted += event.postedMinor();
        }
        // Telemetry is intentionally not part of the result or any authoritative check.
        return new Delta(changed, List.copyOf(added), List.copyOf(removed), delta, review, posted, findings.stream().distinct().sorted().toList());
    }

    private static boolean appendOnly(List<Event> prior, List<Event> current) {
        if (current.size() < prior.size()) return false;
        for (int i = 0; i < prior.size(); i++) if (!prior.get(i).equals(current.get(i))) return false;
        return true;
    }
}
