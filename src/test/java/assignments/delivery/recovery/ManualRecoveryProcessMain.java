package assignments.delivery.recovery;

import assignments.delivery.JdbcIssuanceWorkStore;
import assignments.delivery.IssuanceSourceKey;
import org.postgresql.ds.PGSimpleDataSource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import java.util.stream.Collectors;

/** Test child: a genuinely new JVM queries the same disposable PostgreSQL through production filtering. */
public final class ManualRecoveryProcessMain {
 private ManualRecoveryProcessMain() {}
 public static void main(String[] args) {
  PGSimpleDataSource ds=new PGSimpleDataSource(); ds.setURL(args[0]);
  JdbcTemplate jdbc=new JdbcTemplate(ds); var recovery=new JdbcManualRecoveryStore(jdbc);
  var guarded=new RecoveryAwareIssuanceWorkStore(new JdbcIssuanceWorkStore(jdbc),recovery,jdbc);
  if(args.length<2 || args[1].equals("pending")) {
   System.out.println(guarded.findPending(20).stream().map(e->e.policyId()+":"+e.sourceVersion()).collect(Collectors.joining(","))); return;
  }
  PlatformTransactionManager manager=new DataSourceTransactionManager(ds);
  assignments.delivery.IssuanceWorkStore recoveryStore=guarded;
  if(args[1].equals("receipt-commit-block")) {
   recoveryStore=new assignments.delivery.IssuanceWorkStore(){
    public int captureCommittedIssuances(int limit){return guarded.captureCommittedIssuances(limit);}
    public java.util.List<assignments.delivery.IssuanceEnvelope> findPending(int limit){return guarded.findPending(limit);}
    public assignments.delivery.DeliveryReceiptResult acceptReceipt(assignments.delivery.IssuanceEnvelope e){
     var result=guarded.acceptReceipt(e);
     org.springframework.transaction.support.TransactionSynchronizationManager.registerSynchronization(new org.springframework.transaction.support.TransactionSynchronization(){
      @Override public void afterCommit(){System.out.println("RECEIPT_COMMITTED");System.out.flush();try{new java.util.concurrent.CountDownLatch(1).await();}catch(InterruptedException interrupted){Thread.currentThread().interrupt();throw new IllegalStateException(interrupted);}}
     });
     return result;
    }
    public boolean acknowledge(assignments.delivery.IssuanceEnvelope e){return guarded.acknowledge(e);}
   };
  }
  var service=new ManualRecoveryService(recovery,recoveryStore,manager);
  String result=service.retryOne(new IssuanceSourceKey(args[2],Long.parseLong(args[3])),Integer.parseInt(args[4]));
  System.out.println("RECOVERY_RESULT="+result); System.out.flush();
 }
}
