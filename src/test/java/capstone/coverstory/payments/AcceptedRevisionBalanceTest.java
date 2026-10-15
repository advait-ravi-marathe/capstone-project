package capstone.coverstory.payments;

import capstone.coverstory.payments.internal.AcceptedRevisionBalance;
import capstone.coverstory.payments.internal.AcceptedRevisionBalance.*;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/** The assessed business calculation remains a visible learner TODO. */
@Tag("learnerAcceptance")
class AcceptedRevisionBalanceTest {
    private static final UUID OWNER = UUID.fromString("11600000-0000-4000-8000-000000000001");
    private static final UUID APP = UUID.fromString("11600000-0000-4000-8000-000000000101");
    private static final UUID OFFER = UUID.fromString("11600000-0000-4000-8000-000000000112");
    private static final String HASH = "92cc44f29a18ad1016bb90f27ba70dd7593675bda29d5e79eac322ad9bcd2aa9";
    private final AcceptedRevisionBalance learner = new AcceptedRevisionBalance();
    private AcceptedOffer h2() { return new AcceptedOffer(OWNER,APP,OFFER,"o2",HASH,110_000,"INR",true); }
    private ConfirmedEffect capture() { return new ConfirmedEffect(UUID.fromString("11600000-0000-4000-8000-000000000201"),OWNER,APP,"CAPTURE-1",EffectKind.CAPTURE,100_000,"INR",UUID.fromString("11600000-0000-4000-8000-000000000111"),"q1","6a03129b0e896ba0af7fd404e6e614fc67735daafbffcaf5a80c3f65abe55a37",true); }

    @Test void acceptedH2WithOneCaptureIsEligibleForOnlyTenThousandPaise() {
        var result=learner.reconcile(h2(),List.of(capture()),List.of());
        assertEquals(100_000,result.confirmedNetMinor());assertEquals(10_000,result.adjustmentMinor());assertEquals(NextAction.CREATE_ADDITIONAL_PAYMENT,result.nextAction());
        assertEquals(OFFER,result.acceptedOfferId());assertEquals(HASH,result.acceptedContentSha256());
    }
    @Test void pendingOldRevisionPaymentPreventsNewRefundOrCharge() {
        var pending=new PendingAllocation(UUID.fromString("11600000-0000-4000-8000-000000000301"),OWNER,APP,"balance-o2",OFFER,"o2",HASH,AllocationKind.ADDITIONAL_PAYMENT,AllocationStatus.IN_FLIGHT,10_000,"INR");
        var result=learner.reconcile(new AcceptedOffer(OWNER,APP,UUID.fromString("11600000-0000-4000-8000-000000000113"),"o3","9b02bdc68efb12d0a65d85177bfd9027d770c319dc024bd847b5b4a486752cb7",90_000,"INR",true),List.of(capture()),List.of(pending));
        assertEquals(10_000,result.pendingAdditionalMinor());assertEquals(NextAction.WAIT_OR_RECONCILE,result.nextAction());
    }
    @Test void duplicateEffectAndForeignOwnerCannotInflateConfirmedNet() {
        var result=learner.reconcile(h2(),List.of(capture(),capture()),List.of());
        assertEquals(100_000,result.confirmedNetMinor());assertEquals(10_000,result.adjustmentMinor());
    }
    @Test void orphanOrCurrencyMismatchBlocksAReconciliationAction() {
        var orphan=new ConfirmedEffect(null,OWNER,APP,"ORPHAN",EffectKind.CAPTURE,8_000,"INR",OFFER,"o2",HASH,false);
        var result=learner.reconcile(h2(),List.of(capture(),orphan),List.of());
        assertTrue(result.discrepancyCodes().contains("ORPHAN_EFFECT_OBSERVATION"));assertEquals(NextAction.REVIEW_DISCREPANCY,result.nextAction());
    }
    @Test void learnerMethodRemainsTheNamedUnsupportedTodo() {
        assertThrows(UnsupportedOperationException.class,()->learner.reconcile(h2(),List.of(),List.of()));
    }
}
