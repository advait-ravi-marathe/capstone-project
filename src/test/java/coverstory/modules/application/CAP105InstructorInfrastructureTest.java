package coverstory.modules.application;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/** Independent private reference oracle; it does not invoke the learner TODO. */
class CAP105InstructorInfrastructureTest {
    private static final ObjectMapper JSON = new ObjectMapper();
    private static final Path FIXTURE = Path.of("fixtures/coverstory/CAP-105/synthetic-v1.json");
    private enum State { VERIFIED, UNVERIFIED, BLOCKED, NOT_RUN }
    private record Demo(String id, String command, String commit, String scope, String evidenceLink, String evidence,
                        String output, boolean passed) { }
    private record Finding(String id, State state, String evidenceLink, String scope, String reason) { }
    private record Report(List<Finding> findings, List<String> allDefects,
                          List<String> priorities, List<String> unscheduled, boolean complete) { }

    @Test void fixtureIsSyntheticAndContainsAllRequiredCasesButNoActualReceipts() throws Exception {
        JsonNode f=JSON.readTree(Files.readString(FIXTURE));
        assertEquals("SYNTHETIC_OFFLINE_CONTRACT",f.path("classification").asText());
        assertEquals(4,f.path("cases").size());
        assertFalse(f.path("actual_receipts_present").asBoolean());
        assertEquals(List.of("CAP-95","CAP-102","CAP-104"), ids(f.path("demonstrations")));
    }

    @Test void threePassingLinkedOutputsVerifyWithScopeAndExactSourceIdentity() {
        List<Demo> demos=List.of(
                demo("CAP-95","./mvnw test -Dtest=CAP95InstructorInfrastructureTest","c95","bounded assistance","artifact://synthetic/CAP-95","linked output","BUILD SUCCESS",true),
                demo("CAP-102","./mvnw test -Dtest=CAP102InstructorInfrastructureTest","c102","tie-safe ingestion","artifact://synthetic/CAP-102","linked output","BUILD SUCCESS",true),
                demo("CAP-104","./mvnw test -Dtest=CAP104InstructorInfrastructureTest","c104","late-summary recovery","artifact://synthetic/CAP-104","linked output","BUILD SUCCESS",true));
        Report result=reference(demos,List.of(),List.of(),true,true);
        assertTrue(result.complete());
        assertEquals(3,result.findings().stream().filter(f->f.state()==State.VERIFIED).count());
        assertTrue(result.findings().stream().allMatch(f->!f.scope().isBlank()&&!f.evidenceLink().isBlank()));
        assertFalse(reference(List.of(valid("CAP-95"),valid("CAP-102"),demo("CAP-104","cmd","commit","scope","screenshot",true)),List.of(),List.of(),true,true).complete());
    }

    @Test void screenshotAloneRemainsUnverifiedAndCheckpointIncomplete() {
        List<Demo> demos=List.of(valid("CAP-95"),valid("CAP-102"),demo("CAP-104","cmd","abc123","late-summary","screenshot",true));
        Report result=reference(demos,List.of(),List.of(),true,true);
        assertEquals(State.UNVERIFIED,result.findings().get(2).state());
        assertFalse(result.complete());
        assertFalse(result.findings().get(2).reason().contains("VERIFIED"));
    }

    @Test void moreThanTwoCriticalDefectsAreAllPreservedAndUnselectedRemainUnscheduled() {
        List<String> defects=List.of("critical-evidence","critical-commit","critical-scope","minor-doc");
        Report result=reference(List.of(valid("CAP-95"),valid("CAP-102"),valid("CAP-104")),defects,
                List.of("critical-evidence","critical-commit"),true,true);
        assertEquals(defects,result.allDefects());
        assertEquals(List.of("critical-evidence","critical-commit"),result.priorities());
        assertEquals(List.of("critical-scope","minor-doc"),result.unscheduled());
    }

    @Test void untrustedHarnessBlocksAndUnavailableServiceIsNotRunWithoutInflation() {
        List<Demo> demos=List.of(valid("CAP-95"),valid("CAP-102"),valid("CAP-104"));
        Report untrusted=reference(demos,List.of(),List.of(),false,true);
        Report unavailable=reference(demos,List.of(),List.of(),true,false);
        assertTrue(untrusted.findings().stream().allMatch(f->f.state()==State.BLOCKED));
        assertTrue(unavailable.findings().stream().allMatch(f->f.state()==State.NOT_RUN));
        assertFalse(untrusted.complete());assertFalse(unavailable.complete());
        assertEquals(0,untrusted.findings().stream().filter(f->f.state()==State.VERIFIED).count());
    }

    @Test void malformedOrMissingActualEvidenceCannotPassAndDuplicateDefectIdsReject() {
        Report missing=reference(List.of(valid("CAP-95"),valid("CAP-102"),demo("CAP-104","","","","linked-output",true)),List.of(),List.of(),true,true);
        assertEquals(State.UNVERIFIED,missing.findings().get(2).state());
        assertThrows(IllegalArgumentException.class,()->reference(List.of(valid("CAP-95"),valid("CAP-102"),valid("CAP-104")),List.of("D1","D1"),List.of(),true,true));
    }

    private static Report reference(List<Demo> demos,List<String> defects,List<String> priorities,boolean trusted,boolean available) {
        if(demos==null||demos.size()!=3||new HashSet<>(demos.stream().map(Demo::id).toList()).size()!=3) throw new IllegalArgumentException("exactly three unique demonstrations required");
        if(new HashSet<>(defects).size()!=defects.size()) throw new IllegalArgumentException("duplicate defect id");
        if(priorities.size()>2||!defects.containsAll(priorities)||new HashSet<>(priorities).size()!=priorities.size()) throw new IllegalArgumentException("invalid top-two selection");
        List<Finding> findings=new ArrayList<>();
        for(Demo d:demos) {
            State state;
            String reason;
            if(!trusted) {state=State.BLOCKED;reason="harness untrusted";}
            else if(!available) {state=State.NOT_RUN;reason="service unavailable";}
            else if(d.command().isBlank()||d.commit().isBlank()||d.scope().isBlank()||d.evidenceLink().isBlank()||!d.passed()||!d.evidence().equals("linked output")||d.output().isBlank()) {state=State.UNVERIFIED;reason="missing or non-authoritative actual evidence";}
            else {state=State.VERIFIED;reason="actual command output; commit="+d.commit();}
            findings.add(new Finding(d.id(),state,d.evidenceLink(),d.scope(),reason));
        }
        List<String> unscheduled=defects.stream().filter(d->!priorities.contains(d)).toList();
        return new Report(List.copyOf(findings),List.copyOf(defects),List.copyOf(priorities),unscheduled,
                trusted&&available&&findings.stream().allMatch(f->f.state()==State.VERIFIED));
    }
    private static Demo valid(String id){return demo(id,"./mvnw test -Dtest="+id,"abc123",id+" bounded scope","artifact://synthetic/"+id,"linked output","BUILD SUCCESS",true);}
    private static Demo demo(String id,String command,String commit,String scope,String evidence,boolean passed){return new Demo(id,command,commit,scope,"",evidence,evidence,passed);}
    private static Demo demo(String id,String command,String commit,String scope,String link,String evidence,String output,boolean passed){return new Demo(id,command,commit,scope,link,evidence,output,passed);}
    private static List<String> ids(JsonNode array){List<String> out=new ArrayList<>();array.forEach(n->out.add(n.path("id").asText()));return out;}
}
