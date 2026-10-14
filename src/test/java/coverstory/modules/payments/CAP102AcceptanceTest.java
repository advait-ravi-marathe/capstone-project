package coverstory.modules.payments;

import coverstory.modules.payments.api.CAP102Seam;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.*;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;

@Tag("learnerAcceptance")
class CAP102AcceptanceTest {
    private static final CAP102Seam.Owner A = owner("10200000-0000-4000-8000-000000000001");
    private static final CAP102Seam.Owner B = owner("10200000-0000-4000-8000-000000000002");
    private static Instant t(String value) { return Instant.parse("2026-12-18T" + value + "Z"); }
    private static CAP102Seam.Owner owner(String id) { return new CAP102Seam.Owner(UUID.fromString(id)); }
    private static CAP102Seam.Observation row(CAP102Seam.Owner o, Instant at, String id) { return new CAP102Seam.Observation(o,at,id,"sha256:synthetic-"+id); }
    private static CAP102Seam.TupleCursor start() { return new CAP102Seam.TupleCursor(t("10:00:00"),"x1"); }
    private static List<CAP102Seam.Observation> fixture() { return List.of(row(A,t("10:00:00"),"x1"),row(A,t("10:00:00"),"x2"),row(A,t("10:01:00"),"x3")); }
    private static Fake fake() { return new Fake(); }

    @Test void emitsX2AndX3AndCommitsLastTuple() {
        Fake f=fake(); var result=new CAP102Seam(f).ingestAfter(A,start(),new CAP102Seam.Rows(fixture()));
        assertEquals(List.of("x2","x3"),result.emitted().stream().map(CAP102Seam.Observation::id).toList());
        assertEquals(new CAP102Seam.TupleCursor(t("10:01:00"),"x3"),result.cursor()); assertEquals(2,f.stored.size());
    }
    @Test void failureAtX3RollsBackRowsAndCursor() {
        Fake f=fake(); f.failId="x3"; assertThrows(IllegalStateException.class,()->new CAP102Seam(f).ingestAfter(A,start(),new CAP102Seam.Rows(fixture())));
        assertTrue(f.stored.isEmpty());assertEquals(start(),f.cursor);
    }
    @Test void rerunAtCommittedCursorHasNoDuplicateEffects() {
        Fake f=fake(); var seam=new CAP102Seam(f);var first=seam.ingestAfter(A,start(),new CAP102Seam.Rows(fixture()));
        var second=seam.ingestAfter(A,first.cursor(),new CAP102Seam.Rows(fixture()));assertTrue(second.emitted().isEmpty());assertEquals(2,f.stored.size());
    }
    @Test void equalTimestampUsesLexicographicId() {
        Fake f=fake(); var result=new CAP102Seam(f).ingestAfter(A,start(),new CAP102Seam.Rows(List.of(row(A,t("10:00:00"),"x2"),row(A,t("10:00:00"),"x10"))));
        assertEquals(List.of("x10","x2"),result.emitted().stream().map(CAP102Seam.Observation::id).toList());
    }
    @Test void foreignOwnerOrNullOrderingKeyRejectsBeforeTransaction() {
        Fake f=fake();var seam=new CAP102Seam(f);
        assertThrows(IllegalArgumentException.class,()->seam.ingestAfter(A,start(),new CAP102Seam.Rows(List.of(row(B,t("10:02:00"),"foreign")))));
        assertThrows(IllegalArgumentException.class,()->seam.ingestAfter(A,start(),new CAP102Seam.Rows(List.of(row(A,null,"bad")))));
        assertThrows(IllegalArgumentException.class,()->seam.ingestAfter(A,start(),new CAP102Seam.Rows(List.of(row(A,t("10:02:00"),null)))));
        assertEquals(0,f.transactions);assertTrue(f.stored.isEmpty());assertEquals(start(),f.cursor);
    }
    private static final class Fake implements CAP102Seam.ReconciliationPort {
        CAP102Seam.TupleCursor cursor=start(); final List<CAP102Seam.Observation> stored=new ArrayList<>(); int transactions; String failId;
        @Override public CAP102Seam.CommittedBatch inTransaction(CAP102Seam.Owner owner,Function<CAP102Seam.ReconciliationTransaction,CAP102Seam.CommittedBatch> work) {
            transactions++; var oldCursor=cursor; var oldRows=new ArrayList<>(stored);
            try { return work.apply(new CAP102Seam.ReconciliationTransaction() {
                public CAP102Seam.TupleCursor lockCursor(CAP102Seam.Owner requested){if(!owner.equals(requested))throw new IllegalArgumentException();return cursor;}
                public void appendObservation(CAP102Seam.Observation value){if(value.id().equals(failId))throw new IllegalStateException("injected x3 failure");stored.add(value);}
                public void advanceCursor(CAP102Seam.Owner requested,CAP102Seam.TupleCursor expected,CAP102Seam.TupleCursor next){if(!owner.equals(requested)||!cursor.equals(expected))throw new IllegalStateException("cursor changed");cursor=next;}
            }); } catch(RuntimeException e){cursor=oldCursor;stored.clear();stored.addAll(oldRows);throw e;}
        }
    }
}
