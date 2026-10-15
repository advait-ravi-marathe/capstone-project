package capstone.coverstory.support;

import capstone.coverstory.support.internal.ExplanationUsageLedger;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.util.List;
import java.util.OptionalLong;
import static org.junit.jupiter.api.Assertions.*;

@Tag("learnerAcceptance")
class CAP113AcceptanceTest {
    private final ExplanationUsageLedger.Tariff tariff=new ExplanationUsageLedger.Tariff("v1","USD",new BigDecimal("2"),new BigDecimal("8"));
    private ExplanationUsageLedger.Usage usage(String owner,String req,String event,OptionalLong in,OptionalLong out){return new ExplanationUsageLedger.Usage(owner,req,event,in,out);}
    @Test void fixtureUsageProducesExactMockCostSeparateFromPremium(){var ledger=new ExplanationUsageLedger.Ledger(List.of());var out=ExplanationUsageLedger.record(ledger,usage("owner-a","r1","event-1",OptionalLong.of(1000),OptionalLong.of(250)),tariff);assertEquals(new BigDecimal("0.004000"),out.entries().get(0).amount());assertEquals("MOCK_ESTIMATE",out.entries().get(0).provenance());}
    @Test void duplicateRequestDoesNotAddAnotherEntry(){var event=usage("owner-a","r1","event-1",OptionalLong.of(1000),OptionalLong.of(250));var once=ExplanationUsageLedger.record(new ExplanationUsageLedger.Ledger(List.of()),event,tariff);var twice=ExplanationUsageLedger.record(once,event,tariff);assertEquals(1,twice.entries().size());}
    @Test void absentUsageRemainsUnknownInsteadOfZero(){var out=ExplanationUsageLedger.record(new ExplanationUsageLedger.Ledger(List.of()),usage("owner-a","r2","event-2",OptionalLong.empty(),OptionalLong.empty()),tariff);assertEquals("UNKNOWN",out.entries().get(0).status());assertNull(out.entries().get(0).amount());}
    @Test void todoGuardKeepsAccountingLogicUnimplemented(){assertThrows(UnsupportedOperationException.class,()->ExplanationUsageLedger.record(new ExplanationUsageLedger.Ledger(List.of()),usage("a","r1","e1",OptionalLong.of(1),OptionalLong.of(1)),tariff));}
}
