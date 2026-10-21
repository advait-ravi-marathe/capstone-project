package assignments.insurance;

import java.time.LocalDate;

/** Synthetic annual term, represented with an exclusive next-term boundary. */
public record PolicyRenewalTerm(LocalDate currentTermStart, LocalDate nextTermStart,
                                LocalDate renewalWindowOpensOn) { }
