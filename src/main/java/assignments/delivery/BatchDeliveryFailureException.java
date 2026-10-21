package assignments.delivery;

/** Stops a manual batch while retaining a truthful summary of already committed work. */
public final class BatchDeliveryFailureException extends IllegalStateException {
    private final BatchDeliveryReport committedProgress;

    public BatchDeliveryFailureException(BatchDeliveryReport committedProgress, Throwable cause) {
        super("delivery batch stopped after committed progress", cause);
        this.committedProgress = committedProgress;
    }

    public BatchDeliveryReport committedProgress() { return committedProgress; }
}
