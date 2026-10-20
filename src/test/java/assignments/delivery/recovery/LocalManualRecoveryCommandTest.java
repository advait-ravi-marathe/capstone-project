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
  var command=new LocalManualRecoveryCommand(null,null);
  assertEquals(2,run(command,"--coverstory.recovery.action=bogus","--coverstory.recovery.policy-id=x","--coverstory.recovery.source-version=1"));
  assertEquals(1,run(command,"--coverstory.recovery.action=inspect","--coverstory.recovery.policy-id=x","--coverstory.recovery.source-version=1","--coverstory.delivery.run-once"));
  assertEquals(1,run(command,"--coverstory.recovery.action=inspect","--coverstory.recovery.source-version=1"));
 }
 private static int run(LocalManualRecoveryCommand command,String...args) {
  var out=new ByteArrayOutputStream();var err=new ByteArrayOutputStream();
  int code=command.execute(new DefaultApplicationArguments(args),new PrintStream(out),new PrintStream(err));
  assertTrue(err.size()>0 || code==0,err.toString(StandardCharsets.UTF_8)); return code;
 }
}
