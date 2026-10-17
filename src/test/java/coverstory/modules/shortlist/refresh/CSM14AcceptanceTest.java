package coverstory.modules.shortlist.refresh;

import coverstory.modules.shortlist.refresh.CheckpointTypes.*;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.*;

/** Behavioral learner acceptance contracts for CAP-632–645; CAP-638 explicitly names its currently covered UI subset. */
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
        var b=base();var immutableBefore=List.copyOf(b.slots());
        var localSlots=List.of(b.slots().get(0),new Slot("s2",b.scenarioId(),ref(102,"v2",'f')),b.slots().get(2));
        var local=new Branch(1,localSlots,List.of());
        var snapshot=new CatalogSnapshot(20,Map.of("s1",new SourceLookup(LookupStatus.AVAILABLE,ref(101,"v2",'d')),"s2",new SourceLookup(LookupStatus.AVAILABLE,ref(102,"v3",'e')),"s3",new SourceLookup(LookupStatus.UNAVAILABLE,null)));
        var result=new ThreeWayCheckpointReconciler().reconcile(b,local,snapshot);
        assertEquals(List.of("s2"),result.conflicts().stream().map(Conflict::slotId).toList());
        assertEquals(List.of("s3"),result.unavailableSlotIds());
        assertEquals(ref(101,"v2",'d'),result.proposedSlots().stream().filter(s->s.slotId().equals("s1")).findFirst().orElseThrow().source(),"L=B takes upstream");
        assertEquals(ref(102,"v2",'f'),result.proposedSlots().stream().filter(s->s.slotId().equals("s2")).findFirst().orElseThrow().source(),"divergent L and U stays unresolved; no side is silently selected");
        var same=new Branch(1,List.of(new Slot("s1",b.scenarioId(),ref(101,"v2",'d')),b.slots().get(1),b.slots().get(2)),List.of());
        var equalLocalUpstream=new CatalogSnapshot(20,Map.of("s1",new SourceLookup(LookupStatus.AVAILABLE,ref(101,"v2",'d')),"s2",new SourceLookup(LookupStatus.AVAILABLE,ref(102,"v1",'b'))));
        var shared=new ThreeWayCheckpointReconciler().reconcile(b,same,equalLocalUpstream);
        assertEquals(ref(101,"v2",'d'),shared.proposedSlots().get(0).source(),"L=U shares one exact source identity");
        var upstreamUnchanged=new CatalogSnapshot(20,Map.of("s1",new SourceLookup(LookupStatus.AVAILABLE,ref(101,"v1",'a')),"s2",new SourceLookup(LookupStatus.AVAILABLE,ref(102,"v1",'b'))));
        var takeLocal=new ThreeWayCheckpointReconciler().reconcile(b,new Branch(1,List.of(new Slot("s1",b.scenarioId(),ref(101,"v3",'g')),b.slots().get(1),b.slots().get(2)),List.of()),upstreamUnchanged);
        assertEquals(ref(101,"v3",'g'),takeLocal.proposedSlots().get(0).source(),"U=B takes local");
        assertEquals(immutableBefore,b.slots(),"reconciliation never mutates the source checkpoint");
    }
    private static Checkpoint revision(long rev, UUID checkpointId, UUID owner) {
        var b=base();return new Checkpoint(checkpointId,SHORTLIST,owner,rev,b.scenarioId(),b.slots());
    }
    private static final class AtomicCheckpointStore implements CheckpointRepositoryAdapter.Transactions {
        private Checkpoint head=base();private final List<Checkpoint> history=new java.util.ArrayList<>(List.of(head));private int requiredEffects;
        synchronized long headRevision(){return head.revision();} synchronized List<Checkpoint> history(){return List.copyOf(history);} synchronized int effects(){return requiredEffects;}
        @Override public synchronized <T>T inTransaction(Function<CheckpointRepositoryAdapter.Transaction,T> work){
            var priorHead=head;var priorHistory=List.copyOf(history);int priorEffects=requiredEffects;
            try{return work.apply(new CheckpointRepositoryAdapter.Transaction(){
                public Optional<Checkpoint> ownedHead(UUID owner,UUID shortlist){return head.ownerId().equals(owner)&&head.shortlistId().equals(shortlist)?Optional.of(head):Optional.empty();}
                public boolean appendImmutableRevision(Checkpoint candidate,long parent){if(candidate.revision()!=parent+1||history.stream().anyMatch(x->x.checkpointId().equals(candidate.checkpointId())))return false;history.add(candidate);return true;}
                public boolean advanceOwnedHead(UUID owner,UUID shortlist,long expected,long next){if(!head.ownerId().equals(owner)||!head.shortlistId().equals(shortlist)||head.revision()!=expected||history.stream().noneMatch(x->x.revision()==next))return false;head=history.stream().filter(x->x.revision()==next).findFirst().orElseThrow();return true;}
                public void appendRequiredAuditAndJourney(UUID owner,Checkpoint candidate){if(!head.ownerId().equals(owner)||head.revision()!=candidate.revision())throw new IllegalStateException("required effect owner/head mismatch");requiredEffects++;}
            });}catch(RuntimeException failure){head=priorHead;history.clear();history.addAll(priorHistory);requiredEffects=priorEffects;throw failure;}
        }
    }
    @Test void cap634_appendsHistoryAndRequiredEffectsAtomically() throws Exception {
        CSM14CheckpointSchemaIntegrationTest.assertPostgresRollbackPreservesHeadAndNoRevision();
        var store=new AtomicCheckpointStore();var adapter=new CheckpointRepositoryAdapter(store);
        var r2=revision(2,UUID.fromString("00000000-0000-4000-8000-000000000022"),OWNER);
        var accepted=adapter.append(OWNER,r2,1,false);
        assertEquals(CheckpointRepositoryAdapter.AppendStatus.APPENDED,accepted.status());assertEquals(2,store.headRevision());assertEquals(List.of(1L,2L),store.history().stream().map(Checkpoint::revision).toList());assertEquals(1,store.effects());assertEquals(base(),store.history().get(0),"revision r1 remains immutable");
        var rollbackStore=new AtomicCheckpointStore();var rollbackAdapter=new CheckpointRepositoryAdapter(rollbackStore);
        var aborted=rollbackAdapter.append(OWNER,r2,1,true);
        assertEquals(CheckpointRepositoryAdapter.AppendStatus.ROLLED_BACK,aborted.status());assertEquals(1,rollbackStore.headRevision());assertEquals(List.of(1L),rollbackStore.history().stream().map(Checkpoint::revision).toList());assertEquals(0,rollbackStore.effects(),"required effects and history share the rollback boundary");
        var stale=rollbackAdapter.append(OWNER,r2,0,false);assertEquals(CheckpointRepositoryAdapter.AppendStatus.STALE_HEAD,stale.status());assertEquals(1,rollbackStore.headRevision());assertEquals(0,rollbackStore.effects());
        var foreign=rollbackAdapter.append(UUID.fromString("00000000-0000-4000-8000-000000000002"),r2,1,false);assertEquals(CheckpointRepositoryAdapter.AppendStatus.NOT_OWNED,foreign.status());assertEquals(1,rollbackStore.headRevision());
        var alteredSlots=new java.util.ArrayList<>(r2.slots());alteredSlots.set(0,new Slot("s1",r2.scenarioId(),ref(101,"v9",'9')));var sameIdentityChangedContent=new Checkpoint(r2.checkpointId(),SHORTLIST,OWNER,3,r2.scenarioId(),alteredSlots);
        var identityConflict=adapter.append(OWNER,sameIdentityChangedContent,2,false);assertEquals(CheckpointRepositoryAdapter.AppendStatus.IDENTITY_CONFLICT,identityConflict.status());assertEquals(2,store.headRevision());assertEquals(2,store.history().size());assertEquals(1,store.effects(),"duplicate immutable identity cannot add another required effect");
    }
    @Test void cap635_twoConcurrentSavesHaveOneWinnerAndSameKeyReplay() throws Exception {
        CSM14CheckpointSchemaIntegrationTest.assertPostgresRaceHasOneWinner();
        var ready=new CountDownLatch(2);var release=new CountDownLatch(1);var head=new AtomicLong(1);var appendCount=new AtomicInteger();
        var stored=new ConcurrentHashMap<String,SaveCheckpoint.StoredOperation>();var lookupCount=new AtomicInteger();var saveCount=new AtomicInteger();
        var port=new SaveCheckpoint.Port(){
            private String scope(SaveCheckpoint.Operation op){return op.ownerId()+"/"+op.shortlistId()+"/"+op.operationKind()+"/"+op.idempotencyKey();}
            public Optional<SaveCheckpoint.StoredOperation> findOperation(SaveCheckpoint.Operation op){lookupCount.incrementAndGet();return Optional.ofNullable(stored.get(scope(op)));}
            public SaveCheckpoint.SaveResult save(SaveCheckpoint.Operation op,long expected,Checkpoint candidate){
                saveCount.incrementAndGet();
                ready.countDown();try{if(!release.await(5,java.util.concurrent.TimeUnit.SECONDS))throw new AssertionError("two writer barrier timed out");}catch(InterruptedException e){Thread.currentThread().interrupt();throw new AssertionError(e);}
                synchronized(this){String key=scope(op);var prior=stored.get(key);if(prior!=null)return prior.canonicalRequestSha256().equals(op.canonicalRequestSha256())?prior.result():new SaveCheckpoint.SaveResult(Outcome.CONFLICT,head.get(),"IDEMPOTENCY_KEY_REUSED");if(head.get()!=expected)return new SaveCheckpoint.SaveResult(Outcome.CONFLICT,head.get(),"STALE_HEAD");long next=head.incrementAndGet();appendCount.incrementAndGet();var accepted=new SaveCheckpoint.SaveResult(Outcome.RESOLVED,next,"ACCEPTED");stored.put(key,new SaveCheckpoint.StoredOperation(op.canonicalRequestSha256(),accepted));return accepted;}
            }
        };
        var service=new SaveCheckpoint(port);var one=new SaveCheckpoint.Operation(OWNER,SHORTLIST,"CHECKPOINT_REFRESH_ACCEPT","SYNTHETIC_RACE_T1","a".repeat(64));var two=new SaveCheckpoint.Operation(OWNER,SHORTLIST,"CHECKPOINT_REFRESH_ACCEPT","SYNTHETIC_RACE_T2","b".repeat(64));
        try(var pool=Executors.newFixedThreadPool(2)){
            var f1=pool.submit(()->service.execute(OWNER,base(),1,one));var f2=pool.submit(()->service.execute(OWNER,base(),1,two));
            assertTrue(ready.await(5,java.util.concurrent.TimeUnit.SECONDS),"both expected-r1 writers must reach the shared barrier");release.countDown();
            var r1=f1.get();var r2=f2.get();assertEquals(1,List.of(r1,r2).stream().filter(r->r.outcome()==Outcome.RESOLVED&&r.revision()==2).count(),"exactly one competing save advances r1 to r2");
            assertEquals(1,List.of(r1,r2).stream().filter(r->r.outcome()==Outcome.CONFLICT&&r.revision()==2&&"STALE_HEAD".equals(r.safeCode())).count(),"loser receives a stale-head conflict and retains the draft");
        }
        assertEquals(2,head.get());assertEquals(1,appendCount.get(),"only one immutable revision/effect is appended");
        var winner=stored.containsKey(OWNER+"/"+SHORTLIST+"/CHECKPOINT_REFRESH_ACCEPT/SYNTHETIC_RACE_T1")?one:two;
        var replay=service.execute(OWNER,base(),1,winner);assertEquals(Outcome.RESOLVED,replay.outcome());assertEquals(2,replay.revision());assertEquals(1,appendCount.get(),"same operation replay has no duplicate append");
        var changed=new SaveCheckpoint.Operation(winner.ownerId(),winner.shortlistId(),winner.operationKind(),winner.idempotencyKey(),"c".repeat(64));
        var reused=service.execute(OWNER,base(),1,changed);assertEquals(Outcome.CONFLICT,reused.outcome());assertEquals("IDEMPOTENCY_KEY_REUSED",reused.safeCode());assertEquals(2,head.get());
        int lookupsBefore=lookupCount.get(),savesBefore=saveCount.get();
        assertEquals(Outcome.INVALID,service.execute(OWNER,base(),1,new SaveCheckpoint.Operation(UUID.fromString("00000000-0000-4000-8000-000000000002"),SHORTLIST,winner.operationKind(),"FOREIGN_OWNER_KEY","d".repeat(64))).outcome());
        assertEquals(Outcome.INVALID,service.execute(OWNER,revision(1,base().checkpointId(),UUID.fromString("00000000-0000-4000-8000-000000000002")),1,new SaveCheckpoint.Operation(OWNER,SHORTLIST,winner.operationKind(),"FOREIGN_CANDIDATE_KEY","e".repeat(64))).outcome());
        assertEquals(Outcome.INVALID,service.execute(OWNER,base(),1,new SaveCheckpoint.Operation(OWNER,SHORTLIST,"OTHER_OPERATION","WRONG_KIND_KEY","f".repeat(64))).outcome());
        assertEquals(Outcome.INVALID,service.execute(OWNER,base(),1,new SaveCheckpoint.Operation(OWNER,SHORTLIST,winner.operationKind(),"BAD_HASH_KEY","no-digest")).outcome());
        assertEquals(lookupsBefore,lookupCount.get(),"invalid owner/resource/kind/digest must be rejected before durable lookup");assertEquals(savesBefore,saveCount.get());assertEquals(2,head.get());assertEquals(1,appendCount.get());
    }
    @Test void cap636_previewAuthorizesBeforeReadsAndNeverMutates() {
        var service=new PreviewCheckpointRefresh((owner,id)->Optional.of(base()),requested->new CatalogSnapshot(20,Map.of()),new ThreeWayCheckpointReconciler());
        assertEquals(200,service.preview(OWNER,SHORTLIST,new PreviewCheckpointRefresh.Request(null,1)).status());
    }
    @Test void cap637_staleGenerationAndUnresolvedChoiceCannotAppend() {
        var service=new AcceptCheckpointRefresh((owner,id,request)->new RefreshResult(Outcome.RESOLVED,1,21,List.of(),List.of(),List.of(),"fingerprint",List.of()),new SaveCheckpoint(null));
        assertEquals("STALE_PREVIEW",service.accept(OWNER,SHORTLIST,new AcceptCheckpointRefresh.Request(1,20,"old",Map.of(),"SYNTHETIC_KEY")).safeCode());
    }
    @Test void cap638_browserShellBlocksUnsafeStatesAndSubmitsExplicitDecisionSubset() throws Exception {
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
    private static final String CANDIDATE="a".repeat(64), OWNER_TEXT="SYNTHETIC_OWNER_A";
    private static List<CheckpointRefreshAdmissionRunner.ExpectedEvent> admissionSchedule(){
        return List.of(
          new CheckpointRefreshAdmissionRunner.ExpectedEvent("01-load",Map.of(CheckpointRefreshAdmissionRunner.Layer.UI,"head=r1;order=s1,s2,s3",CheckpointRefreshAdmissionRunner.Layer.API,"200;head=r1",CheckpointRefreshAdmissionRunner.Layer.DATABASE,"head=r1;revisions=1")),
          new CheckpointRefreshAdmissionRunner.ExpectedEvent("02-reorder",Map.of(CheckpointRefreshAdmissionRunner.Layer.UI,"draft-order=s3,s1,s2",CheckpointRefreshAdmissionRunner.Layer.API,"none",CheckpointRefreshAdmissionRunner.Layer.DATABASE,"head=r1;revisions=1")),
          new CheckpointRefreshAdmissionRunner.ExpectedEvent("03-preview",Map.of(CheckpointRefreshAdmissionRunner.Layer.UI,"preview=FP20",CheckpointRefreshAdmissionRunner.Layer.API,"200;generation=20",CheckpointRefreshAdmissionRunner.Layer.DATABASE,"head=r1;revisions=1")),
          new CheckpointRefreshAdmissionRunner.ExpectedEvent("04-other-tab-accept",Map.of(CheckpointRefreshAdmissionRunner.Layer.UI,"receipt=r2",CheckpointRefreshAdmissionRunner.Layer.API,"200;revision=2",CheckpointRefreshAdmissionRunner.Layer.DATABASE,"head=r2;revisions=2")),
          new CheckpointRefreshAdmissionRunner.ExpectedEvent("05-stale-submit",Map.of(CheckpointRefreshAdmissionRunner.Layer.UI,"stale-review-required;draft-retained",CheckpointRefreshAdmissionRunner.Layer.API,"409;code=STALE_PREVIEW",CheckpointRefreshAdmissionRunner.Layer.DATABASE,"head=r2;revisions=2")),
          new CheckpointRefreshAdmissionRunner.ExpectedEvent("06-refresh-review",Map.of(CheckpointRefreshAdmissionRunner.Layer.UI,"preview=FP21;order=s3,s1,s2",CheckpointRefreshAdmissionRunner.Layer.API,"200;generation=21",CheckpointRefreshAdmissionRunner.Layer.DATABASE,"head=r2;revisions=2")));
    }
    private static List<CheckpointRefreshAdmissionRunner.Observation> admissionObservations(List<CheckpointRefreshAdmissionRunner.ExpectedEvent> schedule){
        var out=new java.util.ArrayList<CheckpointRefreshAdmissionRunner.Observation>();for(var e:schedule)for(var layer:CheckpointRefreshAdmissionRunner.Layer.values())out.add(new CheckpointRefreshAdmissionRunner.Observation(e.eventId(),layer,CANDIDATE,OWNER_TEXT,e.values().get(layer)));return out;
    }
    private static String evidenceDocument(List<CheckpointRefreshAdmissionRunner.Observation> rows){var b=new StringBuilder("candidate_sha256=").append(CANDIDATE).append('\n').append("owner_id=").append(OWNER_TEXT).append('\n');for(var o:rows)b.append(o.eventId()).append('\t').append(o.layer()).append('\t').append(o.candidateSha256()).append('\t').append(o.ownerId()).append('\t').append(o.observedValue()).append('\n');return b.toString();}
    @Test void cap645_admissionIsDerivedFromBoundUiApiAndDatabaseEvidence(@TempDir Path temp) throws Exception {
        var schedule=admissionSchedule();var rows=admissionObservations(schedule);Path evidenceFile=temp.resolve("candidate-evidence.tsv");Files.writeString(evidenceFile,evidenceDocument(rows));
        var evidence=new CheckpointRefreshAdmissionRunner.CandidateEvidence(CANDIDATE,OWNER_TEXT,evidenceFile,CheckpointRefreshAdmissionRunner.sha256(evidenceFile));
        var runner=new CheckpointRefreshAdmissionRunner();var pass=runner.evaluate(evidence,schedule);
        assertEquals("PASS",pass.result());assertTrue(pass.violations().isEmpty());assertEquals(18,pass.checkedLayerObservations());assertEquals(CANDIDATE,pass.candidateSha256());assertEquals(evidence.evidenceSha256(),pass.evidenceSha256());
        var defective=new java.util.ArrayList<>(rows);int staleDb=defective.indexOf(defective.stream().filter(o->o.eventId().equals("05-stale-submit")&&o.layer()==CheckpointRefreshAdmissionRunner.Layer.DATABASE).findFirst().orElseThrow());var old=defective.get(staleDb);defective.set(staleDb,new CheckpointRefreshAdmissionRunner.Observation(old.eventId(),old.layer(),old.candidateSha256(),old.ownerId(),"head=r3;revisions=3"));
        Path defectFile=temp.resolve("stale-overwrite-evidence.tsv");Files.writeString(defectFile,evidenceDocument(defective));var defect=runner.evaluate(new CheckpointRefreshAdmissionRunner.CandidateEvidence(CANDIDATE,OWNER_TEXT,defectFile,CheckpointRefreshAdmissionRunner.sha256(defectFile)),schedule);
        assertEquals("FAIL",defect.result());assertTrue(defect.violations().stream().anyMatch(x->x.contains("05-stale-submit")&&x.contains("DATABASE")),"stale overwrite must be diagnosed from durable DB evidence");
        var missing=new java.util.ArrayList<>(rows);missing.removeIf(o->o.eventId().equals("05-stale-submit")&&o.layer()==CheckpointRefreshAdmissionRunner.Layer.DATABASE);
        Path missingFile=temp.resolve("missing-db-evidence.tsv");Files.writeString(missingFile,evidenceDocument(missing));assertEquals("BLOCKED",runner.evaluate(new CheckpointRefreshAdmissionRunner.CandidateEvidence(CANDIDATE,OWNER_TEXT,missingFile,CheckpointRefreshAdmissionRunner.sha256(missingFile)),schedule).result(),"missing independent DB evidence cannot pass");
        var crossedOwner=new java.util.ArrayList<>(rows);var idx=crossedOwner.indexOf(crossedOwner.stream().filter(o->o.eventId().equals("04-other-tab-accept")&&o.layer()==CheckpointRefreshAdmissionRunner.Layer.DATABASE).findFirst().orElseThrow());var previous=crossedOwner.get(idx);crossedOwner.set(idx,new CheckpointRefreshAdmissionRunner.Observation(previous.eventId(),previous.layer(),previous.candidateSha256(),"SYNTHETIC_OWNER_B",previous.observedValue()));
        Path crossedFile=temp.resolve("cross-owner-evidence.tsv");Files.writeString(crossedFile,evidenceDocument(crossedOwner));assertEquals("BLOCKED",runner.evaluate(new CheckpointRefreshAdmissionRunner.CandidateEvidence(CANDIDATE,OWNER_TEXT,crossedFile,CheckpointRefreshAdmissionRunner.sha256(crossedFile)),schedule).result(),"cross-owner persistence evidence cannot be admitted");
        Files.writeString(evidenceFile,"tampered");assertEquals("BLOCKED",runner.evaluate(evidence,schedule).result(),"changed evidence bytes invalidate the saved digest");
    }

    private static int runBrowserContract(String cap) throws Exception {
        Process p=new ProcessBuilder("node","web/scripts/csm14-learner-acceptance.mjs",cap).inheritIO().start();
        return p.waitFor();
    }
}
