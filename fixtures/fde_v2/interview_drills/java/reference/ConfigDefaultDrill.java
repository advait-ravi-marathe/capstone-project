import java.util.Map;
import java.util.Set;

/** Isolated backend interview drill; not capstone application code. */
public class ConfigDefaultDrill {
    static void check(boolean condition, String label) {
        if (!condition) throw new AssertionError(label);
    }
    static int timeoutMillis(Map<String, String> config) {
        String raw = config.getOrDefault("timeout_ms", "3000");
        int value = Integer.parseInt(raw);
        if (value < 1 || value > 30000) throw new IllegalArgumentException("timeout out of range");
        return value;
    }
    public static void main(String[] args) {
        check(timeoutMillis(Map.of()) == 3000, "backward-compatible default");
        check(timeoutMillis(Map.of("timeout_ms", "1250")) == 1250, "explicit value");
        expectInvalid(Map.of("timeout_ms", "-1"));
        System.out.println("PASS");
    }
    static void expectInvalid(Map<String, String> value) {
        try { timeoutMillis(value); throw new AssertionError("invalid timeout accepted"); }
        catch (IllegalArgumentException expected) { }
    }
}
