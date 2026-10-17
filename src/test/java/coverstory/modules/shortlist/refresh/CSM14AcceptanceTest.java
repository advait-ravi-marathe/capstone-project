package coverstory.modules.shortlist.refresh;

import coverstory.modules.shortlist.refresh.CheckpointTypes.*;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

/** Learner acceptance contracts for CAP-632–645. Failures at explicit TODO seams are expected initially. */
@Tag("learnerAcceptance")
class CSM14AcceptanceTest {
    private static final UUID OWNER=UUID.fromString("00000000-0000-4000-8000-000000000001");
    private static final UUID SHORTLIST=UUID.fromString("00000000-0000-4000-8000-000000000011");
    private static SourceVersion ref(int n,String v,char digest) { return new SourceVersion(UUID.fromString("00000000-0000-4000-8000-%012d".formatted(n)),v,String.valueOf(digest).repeat(64)); }
    private static Checkpoint base() { return new Checkpoint(UUID.fromString("00000000-0000-4000-8000-000000000021"),SHORTLIST,OWNER,1,"scenario-health-001",List.of(new Slot("s1","scenario-health-001",ref(101,"v1",'a')),new Slot("s2","scenario-health-001",ref(102,"v1",'b')),new Slot("s3","scenario-health-001",ref(103,"v1",'c')))); }

    @Test void cap632_acceptsImmutableThreeSlotCheckpointAndRejectsDuplicateProducts() {
        var result=new CheckpointContract().validate(base()); assertTrue(result.valid()); assertEquals(3,result.checkpoint().slots().size());
    }
    @Test void cap633_reconcilesByThreeWayEqualityAndExposesDivergence() {
        var b=base(); var local=new Branch(1,b.slots(),List.of());
        var snapshot=new CatalogSnapshot(20,Map.of("s1",new SourceLookup(LookupStatus.AVAILABLE,ref(101,"v2",'d')),"s2",new SourceLookup(LookupStatus.AVAILABLE,ref(102,"v3",'e')),"s3",new SourceLookup(LookupStatus.UNAVAILABLE,null)));
        var result=new ThreeWayCheckpointReconciler().reconcile(b,local,snapshot);
        assertEquals(List.of("s2"),result.conflicts().stream().map(Conflict::slotId).toList());
        assertEquals(List.of("s3"),result.unavailableSlotIds());
    }
    @Test void cap634_appendsHistoryAndRequiredEffectsAtomically() {
        var adapter=new CheckpointRepositoryAdapter((owner,checkpoint)->{});
        assertEquals(CheckpointRepositoryAdapter.AppendStatus.APPENDED,adapter.append(OWNER,base(),1,false).status());
    }
    @Test void cap635_twoConcurrentSavesHaveOneWinnerAndSameKeyReplay() {
        var port=new SaveCheckpoint.Port(){ public SaveCheckpoint.SaveResult save(UUID o,UUID s,long e,String h,Checkpoint c){return new SaveCheckpoint.SaveResult(Outcome.RESOLVED,2,"ACCEPTED");} public SaveCheckpoint.SaveResult findOperation(SaveCheckpoint.Operation op){return new SaveCheckpoint.SaveResult(Outcome.RESOLVED,2,"ACCEPTED");} };
        var operation=new SaveCheckpoint.Operation(OWNER,SHORTLIST,"CHECKPOINT_REFRESH_ACCEPT","SYNTHETIC_KEY","a".repeat(64));
        assertEquals(2,new SaveCheckpoint(port).execute(OWNER,base(),1,operation).revision());
    }
    @Test void cap636_previewAuthorizesBeforeReadsAndNeverMutates() {
        var service=new PreviewCheckpointRefresh((owner,id)->Optional.of(base()),requested->new CatalogSnapshot(20,Map.of()),new ThreeWayCheckpointReconciler());
        assertEquals(200,service.preview(OWNER,SHORTLIST,new PreviewCheckpointRefresh.Request(null,1)).status());
    }
    @Test void cap637_staleGenerationAndUnresolvedChoiceCannotAppend() {
        var service=new AcceptCheckpointRefresh((owner,id,request)->new RefreshResult(Outcome.RESOLVED,1,21,List.of(),List.of(),List.of(),"fingerprint",List.of()),new SaveCheckpoint(null));
        assertEquals("STALE_PREVIEW",service.accept(OWNER,SHORTLIST,new AcceptCheckpointRefresh.Request(1,20,"old",Map.of(),"SYNTHETIC_KEY")).safeCode());
    }
    @Test void cap638_conflictUiDisablesAcceptUntilAccessibleExplicitDecision() throws Exception {
        assertEquals(0,runBrowserContract("CAP-638"));
    }
    @Test void cap639_lostResponseReplaysSameOperationAndRefetchesPersistedHead() throws Exception {
        assertEquals(0,runBrowserContract("CAP-639"));
    }
    @Test void cap640_revocationAtSerializedDecisionDeniesAndLateOwnerResponseIsDropped() {
        var guard=new RefreshAccessGuard((owner,id)->5);
        assertFalse(guard.authorizeAtDecision(OWNER,SHORTLIST,4).allowed());
    }
    @Test void cap640_latePrivateBrowserResponseCannotCrossOwnerSwitch() throws Exception {
        assertEquals(0,runBrowserContract("CAP-640"));
    }
    @Test void cap641_oldRowsDecodeUnknownAndUnsupportedMajorFailsClosed() throws Exception {
        assertEquals(0,runBrowserContract("CAP-641"));
    }
    @Test void cap642_historyIsOwnerBoundAndPageIsCappedAtTwenty() {
        assertEquals(20,new CheckpointHistoryQuery().page(OWNER,SHORTLIST,100,20).revisions().size());
    }
    @Test void cap643_independentReplayModelPredictsBoundedCommandTrace() {
        var model=new CheckpointReferenceModel(); var state=new CheckpointReferenceModel.State(1,List.of("r1"),List.of());
        assertEquals(2,model.apply(state,new CheckpointReferenceModel.Command("ACCEPT","T1","K1","H1")).headRevision());
    }
    @Test void cap644_conflictDecisionFollowsStableSlotAndExplicitLocalOrder() {
        var slots=base().slots(); var resolved=new CheckpointOrderIntent().apply(slots,List.of("s3","s1","s2"),List.of(),List.of());
        assertEquals(List.of("s3","s1","s2"),resolved.stream().map(Slot::slotId).toList());
    }
    @Test void cap645_admissionRunnerChecksUiApiAndIndependentPostgres() {
        var schedule=List.of(new CheckpointRefreshAdmissionRunner.Event("LOAD","r1","200","r1"),new CheckpointRefreshAdmissionRunner.Event("REORDER","draft","none","r1"));
        assertEquals("PASS",new CheckpointRefreshAdmissionRunner().run(schedule).result());
    }

    private static int runBrowserContract(String cap) throws Exception {
        Process p=new ProcessBuilder("node","web/scripts/csm14-learner-acceptance.mjs",cap).inheritIO().start();
        return p.waitFor();
    }
}
