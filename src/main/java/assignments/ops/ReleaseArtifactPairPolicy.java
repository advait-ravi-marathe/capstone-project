package assignments.ops;

import java.util.Objects;

/** Binds a compatibility rehearsal to two independently built, demonstrably different artifacts. */
public final class ReleaseArtifactPairPolicy {
    public ReleaseArtifactPair validate(ReleaseArtifactPair pair) {
        // LEARNER TODO Day 141: require distinct valid jar digests, real source/build provenance,
        // and a capability difference; semantic labels or filenames alone do not count.
        throw new UnsupportedOperationException("Day 141 learner TODO: bind a real baseline/candidate pair");
    }
}
