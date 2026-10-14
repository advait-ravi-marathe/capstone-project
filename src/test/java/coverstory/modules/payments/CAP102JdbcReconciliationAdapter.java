package coverstory.modules.payments;

import coverstory.modules.payments.api.CAP102Seam;

import java.sql.*;
import java.util.function.Function;

/** Instructor-owned JDBC bridge used by both CAP-102 learner contracts and adapter checks. */
final class CAP102JdbcReconciliationAdapter implements CAP102Seam.ReconciliationPort {
    private final Connection connection;
    private final String failId;
    CAP102JdbcReconciliationAdapter(Connection connection,String failId){this.connection=connection;this.failId=failId;}

    @Override public CAP102Seam.CommittedBatch inTransaction(CAP102Seam.Owner owner,Function<CAP102Seam.ReconciliationTransaction,CAP102Seam.CommittedBatch> work){
        try {
            connection.setAutoCommit(false);
            var result=work.apply(new CAP102Seam.ReconciliationTransaction(){
                public CAP102Seam.TupleCursor lockCursor(CAP102Seam.Owner requested){
                    try(PreparedStatement p=connection.prepareStatement("SELECT updated_at,event_id FROM payments.reconciliation_checkpoint WHERE owner_id=? FOR UPDATE")){p.setObject(1,requested.id());try(ResultSet r=p.executeQuery()){if(!r.next())throw new IllegalStateException("missing cursor");return new CAP102Seam.TupleCursor(r.getTimestamp(1).toInstant(),r.getString(2));}}catch(SQLException e){throw new IllegalStateException(e);}
                }
                public void appendObservation(CAP102Seam.Observation row){
                    try(PreparedStatement p=connection.prepareStatement("INSERT INTO payments.reconciliation_observation(owner_id,updated_at,event_id,payload_hash) VALUES(?,?,?,?)")){p.setObject(1,row.owner().id());p.setTimestamp(2,Timestamp.from(row.timestamp()));p.setString(3,row.id());p.setString(4,row.payloadHash());p.executeUpdate();if(row.id().equals(failId))throw new IllegalStateException("injected JDBC writer failure");}catch(SQLException e){throw new IllegalStateException(e);}
                }
                public void advanceCursor(CAP102Seam.Owner requested,CAP102Seam.TupleCursor expected,CAP102Seam.TupleCursor next){
                    try(PreparedStatement p=connection.prepareStatement("UPDATE payments.reconciliation_checkpoint SET updated_at=?,event_id=? WHERE owner_id=? AND updated_at=? AND event_id=?")){p.setTimestamp(1,Timestamp.from(next.timestamp()));p.setString(2,next.id());p.setObject(3,requested.id());p.setTimestamp(4,Timestamp.from(expected.timestamp()));p.setString(5,expected.id());if(p.executeUpdate()!=1)throw new IllegalStateException("stale cursor");}catch(SQLException e){throw new IllegalStateException(e);}
                }
            });
            connection.commit(); return result;
        } catch(RuntimeException|SQLException e){try{connection.rollback();}catch(SQLException rollback){e.addSuppressed(rollback);}throw e instanceof RuntimeException r?r:new IllegalStateException(e);}
        finally {try{connection.setAutoCommit(true);}catch(SQLException e){throw new IllegalStateException(e);}}
    }
}
