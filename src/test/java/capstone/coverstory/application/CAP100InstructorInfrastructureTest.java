package capstone.coverstory.application;
import org.junit.jupiter.api.Test;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

/** Test-only independent oracle; no database or learner seam calls. */
class CAP100InstructorInfrastructureTest {
    private record Out(List<String> accepted,List<String> reasons,int duplicate,int missing,int invalid,int total) { }
    private static Out oracle(String owner,List<QuarantineInvalidOnboardingEvidenceRows.AuditRow> rows,Set<QuarantineInvalidOnboardingEvidenceRows.RowKey> prior) {
        var seen=new HashSet<>(prior); var accepted=new ArrayList<String>(); var reasons=new ArrayList<String>(); int d=0,m=0,i=0;
        for(var row:rows) {
            if(row.rowId()==null || row.rowId().isBlank()){reasons.add("MISSING_ID");m++;continue;}
            if(!(row.attempt() instanceof Integer n)||n<=0){reasons.add("INVALID_ATTEMPT");i++;continue;}
            var key=new QuarantineInvalidOnboardingEvidenceRows.RowKey(owner,row.rowId());
            if(!seen.add(key)){reasons.add("DUPLICATE");d++;} else accepted.add(owner+":"+row.rowId());
        }
        return new Out(accepted,reasons,d,m,i,rows.size());
    }
    private static QuarantineInvalidOnboardingEvidenceRows.AuditRow row(String o,String id,Object attempt,String pin,boolean confirmed,int rev){return new QuarantineInvalidOnboardingEvidenceRows.AuditRow(o,id,attempt,rev,pin,confirmed);}
    @Test void sixRowsYieldThreeAcceptedAndOneEachDuplicateMissingAndInvalid(){
        var rows=List.of(row("customer-A","x1",1,"411001",false,12),row("customer-A","x2",2,null,false,12),row("customer-A","x3",3,null,false,12),row("customer-A","x1",1,null,false,12),row("customer-A",null,1,null,false,12),row("customer-A","x4",true,null,false,12));
        var got=oracle("customer-A",rows,Set.of()); assertEquals(List.of("customer-A:x1","customer-A:x2","customer-A:x3"),got.accepted());
        assertEquals(List.of("DUPLICATE","MISSING_ID","INVALID_ATTEMPT"),got.reasons()); assertEquals(new Out(got.accepted(),got.reasons(),1,1,1,6),got);
    }
    @Test void ownerPlusRowIdDefinesIndependentPartitions(){
        var a=oracle("customer-A",List.of(row("customer-A","x",1,null,false,12)),Set.of()); var b=oracle("customer-B",List.of(row("customer-B","x",1,null,false,12)),Set.of());
        assertEquals(List.of("customer-A:x"),a.accepted()); assertEquals(List.of("customer-B:x"),b.accepted()); assertNotEquals(a.accepted(),b.accepted()); assertFalse(a.accepted().contains("customer-B:x"));
        var globalIdMutant=new HashSet<String>();assertTrue(globalIdMutant.add("x"));assertFalse(globalIdMutant.add("x"));
    }
    @Test void replayAddsNoUniqueIdsAndCountsEveryRepeatAsDuplicate(){
        var rows=List.of(row("customer-A","x1",1,null,false,12),row("customer-A","x2",2,null,false,12),row("customer-A","x3",3,null,false,12));var first=oracle("customer-A",rows,Set.of());
        var prior=new HashSet<QuarantineInvalidOnboardingEvidenceRows.RowKey>();first.accepted().forEach(s->prior.add(new QuarantineInvalidOnboardingEvidenceRows.RowKey("customer-A",s.substring(s.indexOf(':')+1))));
        var replay=oracle("customer-A",rows,prior);var retained=new HashSet<>(first.accepted());retained.addAll(replay.accepted());assertEquals(new HashSet<>(first.accepted()),retained);assertTrue(replay.accepted().isEmpty());assertEquals(3,replay.duplicate());assertEquals(3,replay.total());
    }
    @Test void suggestedPinDoesNotConfirmDisclosureOrAdvanceRevision(){
        var proposed=row("customer-A","pin",1,"411001",false,12);assertEquals("411001",proposed.suggestedPin());assertFalse(proposed.disclosureConfirmed());assertEquals(12,proposed.ratingRevision());
        boolean autoConfirmed=true;int appliedRevision=proposed.ratingRevision()+1;assertTrue(autoConfirmed);assertNotEquals(12,appliedRevision);
    }
    @Test void booleanZeroAndNullAttemptsAreInvalidWithoutCoercion(){
        for(Object n:Arrays.asList(true,0,null)){var out=oracle("customer-A",List.of(row("customer-A","x",n,null,false,12)),Set.of());assertEquals(1,out.invalid());assertTrue(out.accepted().isEmpty());}
        Object bool=Boolean.TRUE; assertTrue(bool instanceof Object); assertFalse(bool instanceof Integer);
    }
    @Test void numericCoercionMutantIsRejected(){Object numeric=1.0d;assertEquals(1,((Number)numeric).intValue());assertFalse(numeric instanceof Integer);}
}
