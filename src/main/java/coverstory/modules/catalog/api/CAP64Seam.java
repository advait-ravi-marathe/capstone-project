package coverstory.modules.catalog.api;

import java.time.Duration;
import java.util.concurrent.CompletionStage;
import java.util.function.BiConsumer;

/** CAP-64 offline producer callback seam. No broker client belongs in this module. */
public final class CAP64Seam {
    public static final String TOPIC = "cs.catalog.publication.v1";

    public record CatalogPublication(String catalogVersionId, String sourceId, String sourceVersion,
                                     long catalogRevision, String evidenceStatus, String payloadHash) { }

    public record ProducerAck(int partition, long offset) { }

    public enum Outcome { SIMULATED_ACK, PUBLISH_TIMEOUT, PUBLISH_FAILED, VALIDATION_ERROR }

    public record PublishResult(Outcome outcome, Integer partition, Long offset, String safeCode) { }

    /** The callback is supplied by the fake adapter; its values are not broker evidence. */
    @FunctionalInterface
    public interface AckCallback extends BiConsumer<ProducerAck, Throwable> { }

    /** Transport boundary for deterministic tests. Implementations must not contact Kafka. */
    @FunctionalInterface
    public interface FakeProducerPort {
        void send(String topic, SourceVersionKey key, byte[] serializedEnvelope, AckCallback callback);
    }

    private final FakeProducerPort producer;
    private final Duration callbackTimeout;

    public CAP64Seam(FakeProducerPort producer, Duration callbackTimeout) {
        this.producer = producer;
        this.callbackTimeout = callbackTimeout;
    }

    public CompletionStage<PublishResult> publish(CatalogPublication event) {
        // LEARNER TODO: validate the version UUID and all required envelope/key
        // fields before sending; serialize schemaVersion=1 exactly; encode the
        // typed SourceVersionKey; complete only from a bounded callback outcome.
        // Guard result completion against duplicate callbacks. One completion
        // does not establish exactly-once physical delivery.
        throw new UnsupportedOperationException("CAP-64 learner TODO: implement offline publication callback contract");
    }
}
