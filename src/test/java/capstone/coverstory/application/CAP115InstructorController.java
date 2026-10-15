package capstone.coverstory.application;

import coverstory.modules.application.api.OfferRevisionFactsPort;
import coverstory.modules.payments.api.OfferPaymentFactsPort;
import coverstory.modules.support.api.AuthorizedOfferEvidencePort;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/** Test-only transport. A choice receipt is not an application command, payment or cover activation. */
@RestController
@Profile("cap115-instructor-test")
@RequestMapping("/api/support/__instructor/cap115")
final class CAP115InstructorController {
    static final UUID OWNER_A=UUID.fromString("11500000-0000-4000-8000-000000000001");
    static final UUID SYNTHETIC_CASE=UUID.fromString("11500000-0000-4000-8000-000000000099");
    private final OfferRevisionFactsPort application;
    private final AuthorizedOfferEvidencePort evidence;
    private final OfferPaymentFactsPort payments;
    private final CAP115ReferenceAnswerValidator validator;
    private final CAP115ChoiceStore choices;
    CAP115InstructorController(OfferRevisionFactsPort application,AuthorizedOfferEvidencePort evidence,
            OfferPaymentFactsPort payments,CAP115ReferenceAnswerValidator validator,CAP115ChoiceStore choices) {
        this.application=application;this.evidence=evidence;this.payments=payments;this.validator=validator;this.choices=choices;
    }

    @GetMapping("/summary") ResponseEntity<?> summary(HttpServletRequest request) {
        if(!authorized(request))return ResponseEntity.status(401).body(new SafeError("AUTH_REQUIRED"));
        var pair=application.findCurrentPair(SYNTHETIC_CASE);if(pair==null)return ResponseEntity.notFound().build();
        var revised=pair.revised();var payment=payments.findForOffer(revised.offerId());
        return ResponseEntity.ok(new Summary(view(pair.original()),view(revised),revised.amountMinor()-pair.original().amountMinor(),
                List.copyOf(evidence.authorizedFor(revised.ownerId())),payment.status(),payment.paidAmountMinor(),payment.dispatchConfirmed(),choices.count(),"SYNTHETIC_ONLY"));
    }

    @PostMapping("/validate") ResponseEntity<?> validate(HttpServletRequest request,@RequestBody ValidateRequest input) {
        if(!authorized(request))return ResponseEntity.status(401).body(new SafeError("AUTH_REQUIRED"));
        var pair=application.findCurrentPair(SYNTHETIC_CASE);if(pair==null)return ResponseEntity.notFound().build();
        var result=validator.validate(pair,evidence.authorizedFor(pair.revised().ownerId()),input.variant());
        return ResponseEntity.ok(new Validation(result.valid(),result.code(),result.authorizedCitations()));
    }

    @PostMapping("/choice") ResponseEntity<?> choose(HttpServletRequest request,@RequestBody ChoiceRequest input) {
        if(!authorized(request))return ResponseEntity.status(401).body(new SafeError("AUTH_REQUIRED"));
        var pair=application.findCurrentPair(SYNTHETIC_CASE);if(pair==null)return ResponseEntity.notFound().build();
        var latest=pair.revised();
        if(input.offerId()==null||input.revision()==null||input.contentSha256()==null||
                !input.offerId().equals(latest.offerId())||!input.revision().equals(latest.revision())||
                !input.contentSha256().equals(latest.contentSha256()))return ResponseEntity.status(409).body(new SafeError("STALE_OFFER"));
        if(!Set.of("ACCEPT_OFFER","DECLINE_OFFER").contains(input.choice()))return ResponseEntity.badRequest().body(new SafeError("INVALID_CHOICE"));
        int inserted=choices.record(OWNER_A,latest,input.choice());
        return ResponseEntity.ok(new ChoiceResult(inserted==1?input.choice():"CHOICE_ALREADY_RECORDED",choices.count(),paymentState(latest.offerId())));
    }

    private String paymentState(UUID offerId){return payments.findForOffer(offerId).status();}
    private static boolean authorized(HttpServletRequest request){Principal p=request.getUserPrincipal();return p!=null&&OWNER_A.toString().equals(p.getName());}
    private static OfferView view(OfferRevisionFactsPort.OfferFacts facts){return new OfferView(facts.offerId(),facts.revision(),facts.contentSha256(),facts.amountMinor(),facts.currency(),facts.termCodes().stream().sorted().toList(),facts.expiresAt().toString());}
    record ValidateRequest(String variant) { }
    record ChoiceRequest(UUID offerId,String revision,String contentSha256,String choice) { }
    record OfferView(UUID offerId,String revision,String contentSha256,long amountMinor,String currency,List<String> termCodes,String expiresAt) { }
    record Summary(OfferView original,OfferView revised,long deltaMinor,List<AuthorizedOfferEvidencePort.EvidenceCitation> authorizedCitations,
                   String paymentStatus,long paidAmountMinor,boolean dispatchConfirmed,int explicitChoices,String classification) { }
    record Validation(boolean valid,String code,List<AuthorizedOfferEvidencePort.EvidenceCitation> authorizedCitations) { }
    record ChoiceResult(String choice,int choiceCount,String paymentStatus) { }
    record SafeError(String code) { }
}
