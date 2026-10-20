package assignments.reboot;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Safety tests prove refusal occurs before psql/network/data access. */
class Day29ContractTest {
    @TempDir Path temp;

    @Test
    void missingConfirmationRefusesWithoutConnecting() throws Exception {
        assertRefusedWithoutPsql("postgresql://localhost:5432/capstone_test");
    }

    @Test
    void nonTestDatabaseRefusesEvenWhenConfirmed() throws Exception {
        assertRefusedWithoutPsql("postgresql://localhost:5432/production", "--confirm-disposable");
    }

    @Test
    void malformedTargetRefusesBeforeConnection() throws Exception {
        assertRefusedWithoutPsql("postgresql://localhost:5432/bad-name_test", "--confirm-disposable");
    }

    private void assertRefusedWithoutPsql(String url, String... extra) throws Exception {
        Path marker = temp.resolve("psql-was-called");
        Path bin = Files.createDirectory(temp.resolve("bin"));
        Path fakePsql = bin.resolve("psql");
        Files.writeString(fakePsql, "#!/usr/bin/env bash\ntouch '" + marker + "'\nexit 99\n");
        fakePsql.toFile().setExecutable(true);
        String[] args = new String[extra.length + 3];
        args[0] = "./scripts/reset-synthetic-homesecure.sh";
        args[1] = "--database-url";
        args[2] = url;
        System.arraycopy(extra, 0, args, 3, extra.length);
        ProcessBuilder builder = new ProcessBuilder(args).directory(Path.of(".").toFile());
        builder.environment().put("PATH", bin + ":/usr/bin:/bin");
        Process process = builder.start();
        int exit = process.waitFor();
        assertNotEquals(0, exit);
        assertFalse(Files.exists(marker), "refusal must happen before psql is invoked");
    }
}
