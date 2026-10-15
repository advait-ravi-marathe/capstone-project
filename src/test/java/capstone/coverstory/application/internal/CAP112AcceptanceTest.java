package capstone.coverstory.application.internal;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

@Tag("learnerAcceptance")
class CAP112AcceptanceTest {
    private static final UUID OWNER=UUID.fromString("11200000-0000-4000-8000-000000000001");
    private static final UUID APP=UUID.fromString("11200000-0000-4000-8000-000000000002");
    private InsurerReviewEventReducer.Snapshot initial(){return new InsurerReviewEventReducer.Snapshot(OWNER,APP,"SUBMITTED",1,Set.of(),List.of());}
    @Test void duplicateReviewEventCreatesExactlyOneTransition(){var event=new InsurerReviewEventReducer.Event(OWNER,APP,"rv1",2,"REVIEW");var once=InsurerReviewEventReducer.apply(initial(),event,UUID.randomUUID(),5,null);var twice=InsurerReviewEventReducer.apply(once.snapshot(),event,UUID.randomUUID(),4,null);assertEquals("REVIEW",twice.snapshot().status());assertEquals(1,twice.snapshot().timeline().size());}
    @Test void olderEventIsAuditedWithoutRegressingReview(){var stale=new InsurerReviewEventReducer.Event(OWNER,APP,"rv0",1,"NEEDS_INFORMATION");var result=InsurerReviewEventReducer.apply(new InsurerReviewEventReducer.Snapshot(OWNER,APP,"REVIEW",2,Set.of(),List.of("rv1")),stale,UUID.randomUUID(),3,null);assertEquals("REVIEW",result.snapshot().status());}
    @Test void providerExceptionTextIsNeverReturnedInDiagnostic(){var failure=new IllegalStateException("SECRET_SENTINEL\\nprivate answer");var result=InsurerReviewEventReducer.apply(initial(),new InsurerReviewEventReducer.Event(OWNER,APP,"rv1",2,"REVIEW"),UUID.randomUUID(),3,failure);assertFalse(result.diagnostic().toString().contains("SECRET_SENTINEL"));}
    @Test void todoGuardKeepsReducerUnimplemented(){assertThrows(UnsupportedOperationException.class,()->InsurerReviewEventReducer.apply(initial(),new InsurerReviewEventReducer.Event(OWNER,APP,"rv1",2,"REVIEW"),UUID.randomUUID(),1,null));}
}
