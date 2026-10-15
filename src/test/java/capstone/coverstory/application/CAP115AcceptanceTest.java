package capstone.coverstory.application;

import capstone.coverstory.application.internal.ReviewedOfferAnswerValidator;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@Tag("learnerAcceptance")
class CAP115AcceptanceTest {
    private static final UUID OWNER=UUID.fromString("11500000-0000-4000-8000-000000000001");
    private ReviewedOfferAnswerValidator.OfferPair pair(){
        var original=new ReviewedOfferAnswerValidator.Offer(OWNER,CAP115TestDatabase.ORIGINAL,"q1",CAP115TestDatabase.ORIGINAL_HASH,100000,"INR",Set.of("copay_10_percent","waiting_30_days"),Instant.parse("2027-01-15T00:00:00Z"));
        var revised=new ReviewedOfferAnswerValidator.Offer(OWNER,CAP115TestDatabase.REVISED,"o2",CAP115TestDatabase.REVISED_HASH,110000,"INR",Set.of("copay_0_percent","restore_benefit","waiting_30_days"),Instant.parse("2027-01-20T00:00:00Z"));
        return new ReviewedOfferAnswerValidator.OfferPair(original,revised);
    }
    @Test void exactAuthorizedAnswerRendersDifferencesButCannotAccept(){
        var validator=new ReviewedOfferAnswerValidator();var p=pair();
        var citation=new ReviewedOfferAnswerValidator.Citation(OWNER,"offer-source-a","v2");
        var answer=new ReviewedOfferAnswerValidator.Explanation(ReviewedOfferAnswerValidator.AnswerStatus.SUPPORTED,p.revised().offerId(),
                p.revised().contentSha256(),10_000L,Set.of("copay_10_percent","copay_0_percent","restore_benefit"),p.revised().expiresAt(),List.of(citation),Set.of(),null);
        var result=validator.validate(p,answer,Set.of(citation));
        assertEquals(ReviewedOfferAnswerValidator.ValidationStatus.SUPPORTED,result.status());
    }
    @Test void foreignCitationOrInventedFieldMustBeRejected(){
        var validator=new ReviewedOfferAnswerValidator();var p=pair();
        var foreign=new ReviewedOfferAnswerValidator.Citation(UUID.fromString("11500000-0000-4000-8000-000000000002"),"owner-b-source","v1");
        var answer=new ReviewedOfferAnswerValidator.Explanation(ReviewedOfferAnswerValidator.AnswerStatus.SUPPORTED,p.revised().offerId(),p.revised().contentSha256(),10_000L,
                Set.of("copay_10_percent","copay_0_percent","restore_benefit"),p.revised().expiresAt(),List.of(foreign),Set.of("inventedCoverage"),null);
        assertEquals(ReviewedOfferAnswerValidator.ValidationStatus.REJECTED,validator.validate(p,answer,Set.of()).status());
    }
    @Test void noAuthorizedSourceIsAnEmptyCitationAbstentionAndCannotCreatePayment(){
        var validator=new ReviewedOfferAnswerValidator();var p=pair();
        var answer=new ReviewedOfferAnswerValidator.Explanation(ReviewedOfferAnswerValidator.AnswerStatus.INSUFFICIENT_EVIDENCE,p.revised().offerId(),
                p.revised().contentSha256(),null,Set.of(),null,List.of(),Set.of(),"NO_AUTHORIZED_SOURCE");
        assertEquals(ReviewedOfferAnswerValidator.ValidationStatus.INSUFFICIENT_EVIDENCE,validator.validate(p,answer,Set.of()).status());
    }
    @Test void TODOIsVisibleAndNoAcceptanceOrPaymentOperationLivesOnTheAnswerSeam(){
        var p=pair();var validator=new ReviewedOfferAnswerValidator();
        assertThrows(UnsupportedOperationException.class,()->validator.validate(p,null,Set.of()));
    }
}
