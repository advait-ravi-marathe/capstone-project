package coverstory.modules.analytics;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class BehaviorEventContractTest {
    @Test
    void requiresAHashSizedValueAndCopiesMutableBytes() {
        byte[] suppliedHash = new byte[32];
        suppliedHash[0] = 7;
        BehaviorEvent event = new BehaviorEvent(
                UUID.randomUUID(),
                BehaviorEvent.EventName.CATALOG_VIEWED,
                BehaviorEvent.ScreenKey.CATALOG,
                suppliedHash,
                Instant.now());

        suppliedHash[0] = 1;
        assertEquals(7, event.sessionScopeHash()[0]);
        byte[] exposedHash = event.sessionScopeHash();
        exposedHash[0] = 2;
        assertEquals(7, event.sessionScopeHash()[0]);
        assertThrows(IllegalArgumentException.class, () -> new BehaviorEvent(
                UUID.randomUUID(),
                BehaviorEvent.EventName.CATALOG_VIEWED,
                BehaviorEvent.ScreenKey.CATALOG,
                new byte[31],
                Instant.now()));
    }
}
