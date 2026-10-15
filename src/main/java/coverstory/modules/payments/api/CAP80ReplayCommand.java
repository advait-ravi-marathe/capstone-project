package coverstory.modules.payments.api;

import coverstory.modules.payments.api.CAP80Seam.ReplayResult;

import java.util.UUID;

/** Server-authoritative command: client supplies only an event identifier. */
public final class CAP80ReplayCommand {
    private final MockConfirmationFeedPort feed;
    private final CAP80ReplayAction action;

    public CAP80ReplayCommand(MockConfirmationFeedPort feed, CAP80Seam learner) {
        this(feed, learner::applyConfirmation);
    }

    public CAP80ReplayCommand(MockConfirmationFeedPort feed, CAP80ReplayAction action) {
        this.feed = feed;
        this.action = action;
    }

    public ReplayResult replay(UUID authenticatedOwner, String eventId) {
        if (authenticatedOwner == null || eventId == null || !eventId.matches("[A-Za-z0-9._:-]{1,120}"))
            throw new IllegalArgumentException("invalid replay reference");
        var item = feed.findOwned(authenticatedOwner, eventId).orElse(null);
        if (item == null) throw new ReplayNotFoundException();
        var checkpoint = feed.currentCheckpoint(authenticatedOwner).orElseThrow(ReplayNotFoundException::new);
        return action.apply(item, checkpoint, feed.effectPort());
    }

    public static final class ReplayNotFoundException extends RuntimeException { }
}
