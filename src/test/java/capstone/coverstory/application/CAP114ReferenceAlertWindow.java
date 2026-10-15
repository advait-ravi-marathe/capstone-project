package capstone.coverstory.application;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.support.TransactionTemplate;

import javax.sql.DataSource;
import java.util.Map;

/** Test-only reducer for instructor exercises; it is deliberately separate from the learner seam. */
final class CAP114ReferenceAlertWindow {
    private final JdbcTemplate jdbc;
    private final TransactionTemplate tx;

    CAP114ReferenceAlertWindow(DataSource dataSource) {
        this.jdbc = new JdbcTemplate(dataSource);
        this.tx = new TransactionTemplate(new org.springframework.jdbc.datasource.DataSourceTransactionManager(dataSource));
    }

    Result record(String windowId, int eligible, int errors, int needsInformation) {
        if (windowId == null || !windowId.matches("[a-z0-9-]{1,40}") || eligible < 0 || errors < 0 ||
                errors > eligible || needsInformation < 0) throw new IllegalArgumentException("invalid synthetic window");
        return tx.execute(status -> {
            Map<String,Object> before = jdbc.queryForMap("SELECT state,qualifying_windows,firing_transitions,resolution_transitions FROM cap114_harness.alert_state WHERE singleton=TRUE FOR UPDATE");
            int inserted = jdbc.update("INSERT INTO cap114_harness.window_sample(window_id,eligible_requests,error_requests,needs_information_count) VALUES(?,?,?,?) ON CONFLICT(window_id) DO NOTHING",
                    windowId, eligible, errors, needsInformation);
            if (inserted == 0) {
                Map<String,Object> existing = jdbc.queryForMap("SELECT eligible_requests,error_requests,needs_information_count FROM cap114_harness.window_sample WHERE window_id=?", windowId);
                if (((Number) existing.get("eligible_requests")).intValue() != eligible ||
                        ((Number) existing.get("error_requests")).intValue() != errors ||
                        ((Number) existing.get("needs_information_count")).intValue() != needsInformation)
                    throw new IllegalArgumentException("window key reused with different fixture values");
                return snapshot(true);
            }

            String old = (String) before.get("state");
            int streak = ((Number) before.get("qualifying_windows")).intValue();
            int fires = ((Number) before.get("firing_transitions")).intValue();
            int resolves = ((Number) before.get("resolution_transitions")).intValue();
            String next = old;
            if (eligible >= 100 && errors * 100L > eligible * 5L) {
                streak++;
                next = streak >= 3 ? "FIRING" : "PENDING";
                if (!"FIRING".equals(old) && "FIRING".equals(next)) fires++;
            } else if (eligible >= 100 && errors == 0) {
                streak = 0;
                next = "FIRING".equals(old) ? "RESOLVED" : "NORMAL";
                if ("FIRING".equals(old)) resolves++;
            } else if (eligible >= 100) {
                streak = 0;
                next = "FIRING".equals(old) ? "FIRING" : "NORMAL";
            }
            if (!old.equals(next)) jdbc.update("INSERT INTO cap114_harness.alert_transition(from_state,to_state,window_id) VALUES(?,?,?)", old,next,windowId);
            jdbc.update("UPDATE cap114_harness.alert_state SET state=?,qualifying_windows=?,firing_transitions=?,resolution_transitions=?,last_window_id=?,updated_at=now() WHERE singleton=TRUE",
                    next,streak,fires,resolves,windowId);
            return snapshot(false);
        });
    }

    Result snapshot() { return snapshot(false); }

    void reset() {
        tx.executeWithoutResult(status -> {
            jdbc.update("DELETE FROM cap114_harness.alert_transition");
            jdbc.update("DELETE FROM cap114_harness.window_sample");
            jdbc.update("UPDATE cap114_harness.alert_state SET state='NORMAL',qualifying_windows=0,firing_transitions=0,resolution_transitions=0,last_window_id=NULL,updated_at=now() WHERE singleton=TRUE");
        });
    }

    private Result snapshot(boolean duplicate) {
        Map<String,Object> state=jdbc.queryForMap("SELECT state,qualifying_windows,firing_transitions,resolution_transitions FROM cap114_harness.alert_state WHERE singleton=TRUE");
        Integer windows=jdbc.queryForObject("SELECT count(*) FROM cap114_harness.window_sample",Integer.class);
        Integer info=jdbc.queryForObject("SELECT COALESCE(sum(needs_information_count),0) FROM cap114_harness.window_sample",Integer.class);
        return new Result((String)state.get("state"),((Number)state.get("qualifying_windows")).intValue(),
                ((Number)state.get("firing_transitions")).intValue(),((Number)state.get("resolution_transitions")).intValue(),
                windows==null?0:windows,info==null?0:info,duplicate);
    }

    record Result(String alertState,int qualifyingWindows,int firingTransitions,int resolutionTransitions,
                  int recordedWindows,int needsInformationCount,boolean duplicate) { }
}
