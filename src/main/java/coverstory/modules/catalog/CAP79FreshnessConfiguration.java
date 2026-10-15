package coverstory.modules.catalog;

import tools.jackson.databind.ObjectMapper;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import java.nio.file.Path;

/** Explicit local-only profile switch; absent configuration keeps the public endpoint unavailable. */
@Configuration
@ConditionalOnProperty(name="coverstory.catalog.freshness.python.enabled", havingValue="true")
public class CAP79FreshnessConfiguration {
    @Bean PythonCAP79FreshnessAdapter pythonCAP79FreshnessAdapter(Environment env, ObjectMapper json) {
        Path root = Path.of(env.getProperty("coverstory.catalog.freshness.python.repository-root", System.getProperty("user.dir"))).toAbsolutePath().normalize();
        Path script = root.resolve(env.getProperty("coverstory.catalog.freshness.python.script", "scripts/cap79_runtime_adapter.py")).normalize();
        if (!script.startsWith(root)) throw new IllegalArgumentException("freshness script must stay inside repository root");
        return new PythonCAP79FreshnessAdapter(env.getProperty("coverstory.catalog.freshness.python.executable", "python3"), root, script, json);
    }
}
