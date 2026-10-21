package assignments.learning.fde;

import io.zonky.test.db.postgres.embedded.EmbeddedPostgres;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

/** Independent instructor toys; no PolicyLifecycleService TODO is invoked. */
class LifecycleWorkedReferenceTest {
    record SeatIntent(String seat, LocalDate session) {}
    record ReceiptKey(String owner, String requestId) {}
    record SeatReceipt(SeatIntent intent, String reservationId) {}
    static final class IntentConflict extends RuntimeException {}
    static final class WorkshopReservations {
        final Map<ReceiptKey, SeatReceipt> receipts = new HashMap<>(); int nextId = 7; int constructions;
        SeatReceipt reserve(String owner, String key, SeatIntent intent) {
            var k = new ReceiptKey(owner, key); var prior = receipts.get(k);
            if (prior != null) { if (!prior.intent().equals(intent)) throw new IntentConflict(); return prior; }
            constructions++; var created = new SeatReceipt(intent, "reservation-r" + nextId++); receipts.put(k, created); return created;
        }
    }
    @Test void day32WorkshopReceiptsPreserveIntentAndScope() {
        var db = new WorkshopReservations(); var intent = new SeatIntent("woodwork-17", LocalDate.parse("2026-12-05"));
        var first = db.reserve("workshop-a", "reserve-7", intent);
        assertEquals(new SeatReceipt(intent, "reservation-r7"), first);
        assertEquals(first, db.reserve("workshop-a", "reserve-7", intent)); assertEquals(1, db.receipts.size());
        assertThrows(IntentConflict.class, () -> db.reserve("workshop-a", "reserve-7", new SeatIntent(intent.seat(), LocalDate.parse("2026-12-06"))));
        assertEquals(first, db.reserve("workshop-a", "reserve-7", intent));
        var otherOwner = db.reserve("workshop-b", "reserve-7", new SeatIntent("woodwork-22", intent.session()));
        var otherKey = db.reserve("workshop-a", "reserve-8", new SeatIntent("woodwork-23", intent.session()));
        assertNotEquals(first, otherOwner); assertNotEquals(first, otherKey); assertEquals(3, db.receipts.size()); assertEquals(3, db.constructions);
    }

    record BookingSnapshot(String id, String lessonVersion, String priceVersion, BigDecimal price, String currency, List<String> notes) {
        BookingSnapshot { notes = List.copyOf(notes); }
    }
    @Test void day33BookingSnapshotOwnsItsNotes() {
        var notes = new ArrayList<>(List.of("captured at booking"));
        var original = new BookingSnapshot("b7", "lesson-v1", "price-v1", new BigDecimal("80.00"), "EUR", notes);
        notes.add("later caller change");
        assertEquals(List.of("captured at booking"), original.notes());
        assertThrows(UnsupportedOperationException.class, () -> original.notes().add("mutation"));
        var catalogNow = new BookingSnapshot("b8", "lesson-v2", "price-v2", new BigDecimal("95.00"), "EUR", List.of());
        assertEquals("price-v1", original.priceVersion()); assertEquals(new BigDecimal("80.00"), original.price());
        assertEquals("price-v2", catalogNow.priceVersion());
    }

    @Test void day35BookingReceiptConflictRollsBackEveryNewRow() throws Exception {
        try (EmbeddedPostgres pg = EmbeddedPostgres.builder().start()) {
            DataSource ds = pg.getPostgresDatabase();
            try (Connection c = ds.getConnection(); var s = c.createStatement()) {
                s.execute("CREATE TABLE toy_booking(id text PRIMARY KEY,label text NOT NULL)");
                s.execute("CREATE TABLE toy_receipt(owner_id text,request_id text,booking_id text REFERENCES toy_booking(id),PRIMARY KEY(owner_id,request_id))");
                s.execute("CREATE TABLE toy_booking_event(booking_id text REFERENCES toy_booking(id),version int,kind text NOT NULL,PRIMARY KEY(booking_id,version))");
                s.execute("INSERT INTO toy_booking VALUES ('old','already booked')");
                s.execute("INSERT INTO toy_receipt VALUES ('member-a','key-7','old')");
                s.execute("INSERT INTO toy_booking_event VALUES ('old',1,'BOOKED')");
            }
            var publisher = new AtomicReference<BookingReceipt>(); var constructed = new AtomicInteger();
            SQLException conflict=assertThrows(SQLException.class, () -> atomicBooking(ds, "key-7", publisher, constructed));
            assertEquals("23505", conflict.getSQLState());
            assertNull(publisher.get(), "failed transaction must not publish replayable success");
            assertEquals(List.of("old"), strings(ds, "SELECT id FROM toy_booking ORDER BY id"));
            assertEquals(List.of("old"), strings(ds, "SELECT booking_id FROM toy_receipt ORDER BY booking_id"));
            assertEquals(List.of("old"), strings(ds, "SELECT booking_id FROM toy_booking_event ORDER BY booking_id"));
            assertEquals(1, constructed.get());
            var committed = atomicBooking(ds, "key-8", publisher, constructed);
            assertEquals("new", committed.bookingId()); assertEquals(2, count(ds, "toy_booking"));
            assertEquals(2, count(ds, "toy_receipt")); assertEquals(2, count(ds, "toy_booking_event"));
            assertEquals(committed, retryAfterCommit(publisher, constructed)); assertEquals(2, constructed.get());
        }
    }
    record BookingReceipt(String owner, String key, String bookingId) {}
    private static BookingReceipt atomicBooking(DataSource ds, String key, AtomicReference<BookingReceipt> visible, AtomicInteger made) throws SQLException {
        made.incrementAndGet(); var candidate = new BookingReceipt("member-a", key, "new");
        try (var c = ds.getConnection()) {
            c.setAutoCommit(false);
            try {
                try (var p=c.prepareStatement("INSERT INTO toy_booking VALUES (?,?)")) { p.setString(1,"new");p.setString(2,"new booking");p.executeUpdate(); }
                try (var p=c.prepareStatement("INSERT INTO toy_booking_event VALUES (?,1,'BOOKED')")) { p.setString(1,"new");p.executeUpdate(); }
                try (var p=c.prepareStatement("INSERT INTO toy_receipt VALUES (?,?,?)")) { p.setString(1,"member-a");p.setString(2,key);p.setString(3,"new");p.executeUpdate(); }
                c.commit();
            } catch (SQLException ex) { c.rollback(); throw ex; }
        }
        visible.set(candidate); // Publish only after commit completed.
        return candidate;
    }
    private static BookingReceipt retryAfterCommit(AtomicReference<BookingReceipt> visible, AtomicInteger made) { return visible.get(); }

    record RaceResult(String body, int affected) {}
    @Test void day39DocumentVersionRaceMatchesWinningEvent() throws Exception {
        try (EmbeddedPostgres pg=EmbeddedPostgres.builder().start()) {
            DataSource ds=pg.getPostgresDatabase();
            try(var c=ds.getConnection();var s=c.createStatement()) {
                s.execute("CREATE TABLE toy_document(id text PRIMARY KEY,revision int,body text)");
                s.execute("CREATE TABLE toy_document_event(id text,revision int,body text,PRIMARY KEY(id,revision))");
                s.execute("INSERT INTO toy_document VALUES ('d7',4,'original')");
                s.execute("INSERT INTO toy_document_event VALUES ('d7',4,'original')");
            }
            var read= new CountDownLatch(2); var update = new CountDownLatch(1); var pool=Executors.newFixedThreadPool(2);
            try {
                var a=pool.submit(()->raceWriter(ds,"A",read,update)); var b=pool.submit(()->raceWriter(ds,"B",read,update));
                assertTrue(read.await(5,TimeUnit.SECONDS)); update.countDown();
                RaceResult ra=a.get(10,TimeUnit.SECONDS), rb=b.get(10,TimeUnit.SECONDS);
                assertEquals(1, (ra.affected()==1?1:0)+(rb.affected()==1?1:0));
                assertEquals(1, (ra.affected()==0?1:0)+(rb.affected()==0?1:0));
                String winner=ra.affected()==1?ra.body():rb.body();
                assertEquals(5, scalar(ds,"SELECT revision FROM toy_document WHERE id='d7'"));
                assertEquals(winner, scalarText(ds,"SELECT body FROM toy_document WHERE id='d7'"));
                assertEquals(List.of("original",winner), strings(ds,"SELECT body FROM toy_document_event ORDER BY revision"));
            } finally { pool.shutdownNow(); }
        }
    }
    private static RaceResult raceWriter(DataSource ds,String label,CountDownLatch ready,CountDownLatch go) throws Exception {
        try(var c=ds.getConnection()) {
            c.setAutoCommit(false);
            try(var q=c.createStatement().executeQuery("SELECT revision FROM toy_document WHERE id='d7'")){q.next();assertEquals(4,q.getInt(1));}
            ready.countDown(); if(!go.await(5,TimeUnit.SECONDS))throw new TimeoutException("barrier timeout");
            int changed;
            try(var u=c.prepareStatement("UPDATE toy_document SET revision=5,body=? WHERE id='d7' AND revision=4")){u.setString(1,label);changed=u.executeUpdate();}
            if(changed==1) try(var e=c.prepareStatement("INSERT INTO toy_document_event VALUES ('d7',5,?)")){e.setString(1,label);e.executeUpdate();}
            c.commit(); return new RaceResult(label,changed);
        }
    }

    @Test void day41FixedClockTransitionIsStable() {
        record State(String status,int version,LocalDate effective) {}
        var scheduled=new State("SCHEDULED",2,LocalDate.parse("2026-12-10"));
        java.util.function.BiFunction<State,Clock,State> apply=(state,clock)-> state.status().equals("SCHEDULED") && !LocalDate.now(clock).isBefore(state.effective()) ? new State("CANCELLED",3,state.effective()):state;
        assertEquals(scheduled,apply.apply(scheduled,Clock.fixed(java.time.Instant.parse("2026-12-09T12:00:00Z"),ZoneOffset.UTC)));
        var cancelled=new State("CANCELLED",3,scheduled.effective());
        assertEquals(cancelled,apply.apply(scheduled,Clock.fixed(java.time.Instant.parse("2026-12-10T12:00:00Z"),ZoneOffset.UTC)));
        assertEquals(cancelled,apply.apply(cancelled,Clock.fixed(java.time.Instant.parse("2026-12-11T12:00:00Z"),ZoneOffset.UTC)));
    }

    private static long count(DataSource ds,String table)throws Exception{try(var c=ds.getConnection();var s=c.createStatement();var r=s.executeQuery("SELECT count(*) FROM "+table)){r.next();return r.getLong(1);}}
    private static int scalar(DataSource ds,String sql)throws Exception{try(var c=ds.getConnection();var s=c.createStatement();var r=s.executeQuery(sql)){r.next();return r.getInt(1);}}
    private static String scalarText(DataSource ds,String sql)throws Exception{try(var c=ds.getConnection();var s=c.createStatement();var r=s.executeQuery(sql)){r.next();return r.getString(1);}}
    private static List<String> strings(DataSource ds,String sql)throws Exception{try(var c=ds.getConnection();var s=c.createStatement();var r=s.executeQuery(sql)){var out=new ArrayList<String>();while(r.next())out.add(r.getString(1));return out;}}
}
