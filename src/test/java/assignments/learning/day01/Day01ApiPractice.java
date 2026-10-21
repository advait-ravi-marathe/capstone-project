package assignments.learning.day01;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/** Separate learner variation. Do not change the reference controller or production API. */
@RestController
final class Day01ApiPractice {
    @GetMapping("/learn/day1/practice")
    PracticeResponse practice() {
        // LEARNER PRACTICE TODO: return a PracticeResponse whose message is "API practice ready".
        throw new UnsupportedOperationException("Day 1 API practice TODO: return the requested response");
    }

    record PracticeResponse(String message) { }
}
