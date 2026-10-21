package assignments.learning;

import org.junit.jupiter.api.Test;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicInteger;
import static org.junit.jupiter.api.Assertions.assertEquals;

class Day11FreshnessReferenceTest {
    private static final Instant NOW=Instant.parse("2026-10-30T10:00:00Z");
    record ReservationState(String requestedMember,String recordMember,String requestedEdition,String currentEdition,Instant expiresAt) { }
    static String reservationDecision(ReservationState s,Instant now) {
        if(!s.requestedMember().equals(s.recordMember())) return "HIDDEN";
        if(!now.isBefore(s.expiresAt())) return "EXPIRED";
        if(!s.requestedEdition().equals(s.currentEdition())) return "SUPERSEDED";
        return "ALLOW";
    }
    private static ReservationState state(String member,String stored,String requested,String current,Instant expires) {
        return new ReservationState(member,stored,requested,current,expires);
    }

    @Test void frozenDecisionTableAndCallbackCounts() {
        Object[][] cases={
            {state("a","a","e2","e2",NOW.plusSeconds(30)),"ALLOW",1},
            {state("a","a","e2","e2",NOW),"EXPIRED",0},
            {state("a","a","e1","e2",NOW.plusSeconds(30)),"SUPERSEDED",0},
            {state("b","a","e1","e2",NOW),"HIDDEN",0},
            {state("a","a","e1","e2",NOW),"EXPIRED",0}
        };
        for(Object[] row:cases) {
            var writes=new AtomicInteger();
            String outcome=reservationDecision((ReservationState)row[0],NOW);
            if(outcome.equals("ALLOW")) writes.incrementAndGet();
            assertEquals(row[1],outcome); assertEquals(row[2],writes.get());
        }
    }

    @Test void validityChangesAtStrictExpiryWithoutClockSleep() {
        var before=state("a","a","e2","e2",NOW.plusNanos(1));
        var equal=state("a","a","e2","e2",NOW);
        var after=state("a","a","e2","e2",NOW.minusNanos(1));
        assertEquals("ALLOW",reservationDecision(before,NOW));
        assertEquals("EXPIRED",reservationDecision(equal,NOW));
        assertEquals("EXPIRED",reservationDecision(after,NOW));
    }
}
