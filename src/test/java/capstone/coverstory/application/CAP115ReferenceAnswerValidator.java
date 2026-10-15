package capstone.coverstory.application;

import coverstory.modules.application.api.OfferRevisionFactsPort;
import coverstory.modules.support.api.AuthorizedOfferEvidencePort;
import java.util.*;

/** Independent test-only schema/source comparator for fixed synthetic model-shaped responses. */
final class CAP115ReferenceAnswerValidator {
    private static final UUID OWNER_A=UUID.fromString("11500000-0000-4000-8000-000000000001");
    private static final UUID OWNER_B=UUID.fromString("11500000-0000-4000-8000-000000000002");
    Result validate(OfferRevisionFactsPort.OfferPair pair,Set<AuthorizedOfferEvidencePort.EvidenceCitation> allowed,String variant) {
        if(!Set.of("supported","foreign-source","invented-field","missing-status","insufficient","stale-hash").contains(variant))
            return new Result(false,"UNKNOWN_FIXTURE_RESPONSE",List.of());
        Answer answer=responseFor(pair,variant);
        if(answer.status()==null) return new Result(false,"MISSING_STATUS",List.of());
        if(!answer.unknownFields().isEmpty()) return new Result(false,"UNKNOWN_FIELD",List.of());
        if(answer.status()==Status.INSUFFICIENT_EVIDENCE) {
            if(!answer.citations().isEmpty() || answer.deltaMinor()!=null || !answer.changedTermCodes().isEmpty())
                return new Result(false,"INVALID_ABSTENTION",List.of());
            return new Result(true,"INSUFFICIENT_EVIDENCE",List.of());
        }
        var original=pair.original(); var revised=pair.revised();
        if(!revised.offerId().equals(answer.revisedOfferId())) return new Result(false,"WRONG_REVISED_OFFER",List.of());
        if(!revised.contentSha256().equals(answer.revisedContentSha256())) return new Result(false,"STALE_OR_FOREIGN_REVISION",List.of());
        if(answer.deltaMinor()==null || answer.deltaMinor()!=revised.amountMinor()-original.amountMinor())
            return new Result(false,"AMOUNT_DELTA_MISMATCH",List.of());
        var changed=new HashSet<>(original.termCodes()); changed.removeAll(revised.termCodes());
        var added=new HashSet<>(revised.termCodes()); added.removeAll(original.termCodes()); changed.addAll(added);
        if(!changed.equals(answer.changedTermCodes())) return new Result(false,"TERM_CHANGE_MISMATCH",List.of());
        if(!Objects.equals(answer.expiry(),revised.expiresAt().toString())) return new Result(false,"EXPIRY_MISMATCH",List.of());
        if(answer.citations().isEmpty() || answer.citations().stream().anyMatch(c->!allowed.contains(c) || !c.ownerId().equals(revised.ownerId())))
            return new Result(false,"UNAUTHORIZED_CITATION",List.of());
        return new Result(true,"SUPPORTED",answer.citations());
    }

    private static Answer responseFor(OfferRevisionFactsPort.OfferPair pair,String variant) {
        var revised=pair.revised(); var allowed=new AuthorizedOfferEvidencePort.EvidenceCitation(OWNER_A,"offer-source-a","v2");
        if("insufficient".equals(variant)) return new Answer(Status.INSUFFICIENT_EVIDENCE,null,null,null,Set.of(),null,List.of(),Set.of(),"NO_AUTHORIZED_SOURCE");
        if("missing-status".equals(variant)) return new Answer(null,revised.offerId(),revised.contentSha256(),10_000L,
                symmetricChanges(pair),revised.expiresAt().toString(),List.of(allowed),Set.of(),null);
        if("invented-field".equals(variant)) return new Answer(Status.SUPPORTED,revised.offerId(),revised.contentSha256(),10_000L,
                symmetricChanges(pair),revised.expiresAt().toString(),List.of(allowed),Set.of("inventedCoverage"),null);
        if("foreign-source".equals(variant)) return new Answer(Status.SUPPORTED,revised.offerId(),revised.contentSha256(),10_000L,
                symmetricChanges(pair),revised.expiresAt().toString(),List.of(new AuthorizedOfferEvidencePort.EvidenceCitation(OWNER_B,"owner-b-source","v1")),Set.of(),null);
        return new Answer(Status.SUPPORTED,revised.offerId(),"stale-hash".equals(variant)?"00".repeat(32):revised.contentSha256(),
                10_000L,symmetricChanges(pair),revised.expiresAt().toString(),List.of(allowed),Set.of(),null);
    }
    private static Set<String> symmetricChanges(OfferRevisionFactsPort.OfferPair pair) {
        var changes=new HashSet<>(pair.original().termCodes()); changes.removeAll(pair.revised().termCodes());
        var added=new HashSet<>(pair.revised().termCodes()); added.removeAll(pair.original().termCodes()); changes.addAll(added); return changes;
    }

    enum Status { SUPPORTED, INSUFFICIENT_EVIDENCE }
    record Answer(Status status,UUID revisedOfferId,String revisedContentSha256,Long deltaMinor,
                  Set<String> changedTermCodes,String expiry,List<AuthorizedOfferEvidencePort.EvidenceCitation> citations,
                  Set<String> unknownFields,String reasonCode) { }
    record Result(boolean valid,String code,List<AuthorizedOfferEvidencePort.EvidenceCitation> authorizedCitations) { }
}
