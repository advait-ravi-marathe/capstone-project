package assignments.insurance;

/** Stable public error body; internal exception text is never part of this record. */
public record ApiError(String code, String message) { }
