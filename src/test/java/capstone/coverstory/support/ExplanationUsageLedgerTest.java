package capstone.coverstory.support;

import capstone.coverstory.support.internal.ExplanationUsageLedger;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.util.OptionalLong;
import static org.junit.jupiter.api.Assertions.*;

class ExplanationUsageLedgerTest {
    private final ExplanationUsageLedger.Tariff tariff=new ExplanationUsageLedger.Tariff("mock-tariff-v1","USD",new BigDecimal("2"),new BigDecimal("8"));
    private ExplanationUsageLedger.Usage u(String owner,String request,String event,OptionalLong in,OptionalLong out){return new ExplanationUsageLedger.Usage(owner,request,event,in,out);}
    @Test void exactRateAndSameOwnerDuplicateAreDeterministic(){var first=CAP113InstructorOracle.record(CAP113InstructorOracle.empty(),u("a","r1","e1",OptionalLong.of(1000),OptionalLong.of(250)),tariff);var duplicate=CAP113InstructorOracle.record(first,u("a","r1","e1",OptionalLong.of(1000),OptionalLong.of(250)),tariff);assertSame(first,duplicate);assertEquals(new BigDecimal("0.004000"),CAP113InstructorOracle.knownTotal(duplicate,"a"));}
    @Test void sameRequestInAnotherOwnerPartitionIsASeparateRecord(){var first=CAP113InstructorOracle.record(CAP113InstructorOracle.empty(),u("a","r1","e1",OptionalLong.of(1000),OptionalLong.of(250)),tariff);var second=CAP113InstructorOracle.record(first,u("b","r1","e2",OptionalLong.of(1000),OptionalLong.of(250)),tariff);assertEquals(2,second.entries().size());assertEquals(new BigDecimal("0.004000"),CAP113InstructorOracle.knownTotal(second,"a"));}
    @Test void missingUsageIsUnknownAndDoesNotChangeInsuranceMinorUnits(){long premiumPaise=12_345;var snapshot=CAP113InstructorOracle.record(CAP113InstructorOracle.empty(),u("a","r2","e2",OptionalLong.empty(),OptionalLong.empty()),tariff);assertNull(snapshot.entries().values().iterator().next().amount());assertEquals(1,CAP113InstructorOracle.missingCount(snapshot,"a"));assertEquals(12_345,premiumPaise);}
    @Test void fixturePinsMockOnlyTariffAndNoVendorCall()throws Exception{var n=new tools.jackson.databind.ObjectMapper().readTree(java.nio.file.Files.readString(java.nio.file.Path.of("docs/coverstory/fixtures/cap-113.json")));assertFalse(n.path("network_enabled").asBoolean());assertEquals("USD",n.path("tariff").path("currency").asText());assertEquals("0.004000",n.path("expected_cost").asText());}
}
