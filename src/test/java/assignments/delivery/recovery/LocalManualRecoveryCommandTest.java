package assignments.delivery.recovery;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.boot.DefaultApplicationArguments;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import static org.junit.jupiter.api.Assertions.*;

@Tag("learnerAcceptance")
class LocalManualRecoveryCommandTest {
 @Test void oneActionCommandRejectsInvalidAndConflictingFlags() {
  var service=org.mockito.Mockito.mock(ManualRecoveryService.class);
  var command=new LocalManualRecoveryCommand(service,null);
  assertEquals(2,run(command,"--coverstory.recovery.action=bogus","--coverstory.recovery.policy-id=x","--coverstory.recovery.source-version=1"));
  assertEquals(1,run(command,"--coverstory.recovery.action=inspect","--coverstory.recovery.policy-id=x","--coverstory.recovery.source-version=1","--coverstory.delivery.run-once"));
  assertEquals(1,run(command,"--coverstory.recovery.action=inspect","--coverstory.recovery.source-version=1"));
  org.mockito.Mockito.verifyNoInteractions(service);
 }

 @Test void oneExplicitInspectDispatchesOnlyTheRequestedExactKey() {
  var service=org.mockito.Mockito.mock(ManualRecoveryService.class);
  var command=new LocalManualRecoveryCommand(service,null);
  var key=new assignments.delivery.IssuanceSourceKey("inspect-target",7);
  org.mockito.Mockito.when(service.inspect(key)).thenReturn(new RecoveryReadResult(RecoveryObservation.notFound(key),java.util.List.of()));
  var out=new ByteArrayOutputStream();var err=new ByteArrayOutputStream();
  int code=command.execute(new DefaultApplicationArguments("--coverstory.recovery.action=inspect",
          "--coverstory.recovery.policy-id=inspect-target","--coverstory.recovery.source-version=7"),
          new PrintStream(out),new PrintStream(err));
  assertEquals(0,code,err.toString(StandardCharsets.UTF_8));
  org.mockito.Mockito.verify(service).inspect(key);
  org.mockito.Mockito.verifyNoMoreInteractions(service);
 }

 @Test void ordinaryStartupDoesNotDispatchRecovery() {
  var service=org.mockito.Mockito.mock(ManualRecoveryService.class);
  new LocalManualRecoveryCommand(service,null).run(new DefaultApplicationArguments());
  org.mockito.Mockito.verifyNoInteractions(service);
 }
 private static int run(LocalManualRecoveryCommand command,String...args) {
  var out=new ByteArrayOutputStream();var err=new ByteArrayOutputStream();
  int code=command.execute(new DefaultApplicationArguments(args),new PrintStream(out),new PrintStream(err));
  assertTrue(err.size()>0 || code==0,err.toString(StandardCharsets.UTF_8)); return code;
 }
}
