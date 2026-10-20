package assignments.insurance;

/** Injectable contract used by later quote creation; tests can supply a fixed decision. */
@FunctionalInterface
public interface HomeSecureEligibilityCheck {
    EligibilityDecision decide(HomeSecureEligibilityInput input);
}
