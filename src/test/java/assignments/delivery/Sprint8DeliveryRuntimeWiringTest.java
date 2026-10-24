package assignments.delivery;

import assignments.delivery.recovery.RecoveryAwareIssuanceWorkStore;
import assignments.sprint8.Sprint8IssuanceDeliveryConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

/** Instructor check that the bounded Sprint 8 composition selects the raw store. */
class Sprint8DeliveryRuntimeWiringTest {
    @Test
    void selectedStoreIsRawJdbcAndHasNoDay118RecoveryDecorator() {
        try (var context = new AnnotationConfigApplicationContext()) {
            context.registerBean("issuanceDeliveryJdbcTemplate", JdbcTemplate.class,
                    () -> mock(JdbcTemplate.class));
            context.registerBean("issuanceDeliveryTransactionManager", PlatformTransactionManager.class,
                    () -> mock(PlatformTransactionManager.class));
            context.register(Sprint8IssuanceDeliveryConfiguration.class);
            context.refresh();

            assertInstanceOf(JdbcIssuanceWorkStore.class, context.getBean("sprint8IssuanceWorkStore"));
            assertEquals(1, context.getBeansOfType(IssuanceWorkStore.class).size());
            assertTrue(context.getBeansOfType(RecoveryAwareIssuanceWorkStore.class).isEmpty(),
                    "Days 99–112 must not require the later Day 118 recovery-aware TODO");
            assertNotNull(context.getBean(OneBatchDispatcher.class));
        }
    }
}
