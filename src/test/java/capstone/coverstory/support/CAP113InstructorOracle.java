package capstone.coverstory.support;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import capstone.coverstory.support.internal.ExplanationUsageLedger;

/** Independent exact-decimal fixture oracle; no model/provider or insurance ledger is touched. */
final class CAP113InstructorOracle {
    record Key(String owner,String request) { }
    record Snapshot(Map<Key,ExplanationUsageLedger.Entry> entries) { }
    static Snapshot empty(){return new Snapshot(Map.of());}
    static Snapshot record(Snapshot s,ExplanationUsageLedger.Usage u,ExplanationUsageLedger.Tariff t){
        Key key=new Key(u.ownerId(),u.requestId());if(s.entries().containsKey(key))return s;
        boolean known=u.inputTokens().isPresent()&&u.outputTokens().isPresent();
        BigDecimal amount=known?t.inputPerMillion().multiply(BigDecimal.valueOf(u.inputTokens().getAsLong()))
                .add(t.outputPerMillion().multiply(BigDecimal.valueOf(u.outputTokens().getAsLong())))
                .divide(BigDecimal.valueOf(1_000_000),6,RoundingMode.HALF_UP):null;
        var e=new ExplanationUsageLedger.Entry(u.ownerId(),u.requestId(),u.eventId(),known?"ESTIMATED":"UNKNOWN",amount,t.currency(),t.version(),"MOCK_ESTIMATE");
        var copy=new HashMap<>(s.entries());copy.put(key,e);return new Snapshot(Map.copyOf(copy));
    }
    static BigDecimal knownTotal(Snapshot s,String owner){return s.entries().values().stream().filter(e->e.ownerId().equals(owner)&&"ESTIMATED".equals(e.status())).map(ExplanationUsageLedger.Entry::amount).reduce(new BigDecimal("0.000000"),BigDecimal::add);}
    static long missingCount(Snapshot s,String owner){return s.entries().values().stream().filter(e->e.ownerId().equals(owner)&&"UNKNOWN".equals(e.status())).count();}
}
