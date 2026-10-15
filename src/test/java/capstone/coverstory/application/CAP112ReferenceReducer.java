package capstone.coverstory.application;

import javax.sql.DataSource;
import java.sql.*;
import java.util.UUID;

/** Test-only JDBC reference reducer. It records synthetic events but makes no provider calls. */
public final class CAP112ReferenceReducer {
    private CAP112ReferenceReducer() { }
    public static String apply(DataSource ds,UUID owner,UUID application,String eventId,long version,String nextStatus,
                        UUID requestId,int durationMillis,Throwable providerFailure) throws SQLException {
        try(Connection c=ds.getConnection()) {
            c.setAutoCommit(false);
            try {
                if(providerFailure!=null) { diagnostic(c,requestId,"PROVIDER_FAILURE",durationMillis); c.commit(); return "PROVIDER_FAILURE"; }
                String status; long currentVersion;
                try(PreparedStatement q=c.prepareStatement("SELECT status,version FROM cap112_harness.application_review_state WHERE owner_id=? AND application_id=? FOR UPDATE")) {
                    q.setObject(1,owner);q.setObject(2,application);try(ResultSet r=q.executeQuery()){if(!r.next()){diagnostic(c,requestId,"OWNER_NOT_FOUND",durationMillis);c.commit();return "OWNER_NOT_FOUND";}status=r.getString(1);currentVersion=r.getLong(2);}
                }
                try(PreparedStatement q=c.prepareStatement("INSERT INTO cap112_harness.source_event(owner_id,event_id,application_id,version,received_status,outcome) VALUES(?,?,?,?,?,'APPLIED') ON CONFLICT(owner_id,event_id) DO NOTHING")) {
                    q.setObject(1,owner);q.setString(2,eventId);q.setObject(3,application);q.setLong(4,version);q.setString(5,nextStatus);
                    if(q.executeUpdate()==0){diagnostic(c,requestId,"DUPLICATE",durationMillis);c.commit();return "DUPLICATE";}
                }
                String outcome;
                if(version<=currentVersion) {
                    outcome="STALE_RECORDED";
                    try(PreparedStatement q=c.prepareStatement("UPDATE cap112_harness.source_event SET outcome='STALE_RECORDED' WHERE owner_id=? AND event_id=?")){q.setObject(1,owner);q.setString(2,eventId);q.executeUpdate();}
                } else if("SUBMITTED".equals(status) && ("REVIEW".equals(nextStatus)||"NEEDS_INFORMATION".equals(nextStatus))) {
                    outcome="APPLIED";
                    try(PreparedStatement q=c.prepareStatement("UPDATE cap112_harness.application_review_state SET status=?,version=? WHERE owner_id=? AND application_id=? AND version=?")){q.setString(1,nextStatus);q.setLong(2,version);q.setObject(3,owner);q.setObject(4,application);q.setLong(5,currentVersion);if(q.executeUpdate()!=1)throw new SQLException("revision race");}
                    try(PreparedStatement q=c.prepareStatement("INSERT INTO cap112_harness.review_timeline(timeline_id,owner_id,application_id,version,status,source_event_id) VALUES(?,?,?,?,?,?)")){q.setObject(1,UUID.randomUUID());q.setObject(2,owner);q.setObject(3,application);q.setLong(4,version);q.setString(5,nextStatus);q.setString(6,eventId);q.executeUpdate();}
                } else outcome="STALE_RECORDED";
                diagnostic(c,requestId,outcome,durationMillis);c.commit();return outcome;
            } catch(SQLException|RuntimeException e) {c.rollback();throw e;}
        }
    }
    private static void diagnostic(Connection c,UUID requestId,String outcome,int duration) throws SQLException {
        try(PreparedStatement q=c.prepareStatement("INSERT INTO cap112_harness.safe_diagnostic(request_id,route_code,outcome,duration_millis) VALUES(?,'INSURER_REVIEW_CALLBACK',?,?)")){q.setObject(1,requestId);q.setString(2,outcome);q.setInt(3,Math.max(0,Math.min(duration,60000)));q.executeUpdate();}
    }
}
