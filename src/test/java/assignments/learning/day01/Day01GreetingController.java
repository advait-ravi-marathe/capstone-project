package assignments.learning.day01;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/** Complete instructor-owned example used to teach a request, Java method, and response. */
@RestController
final class Day01GreetingController {
    @GetMapping("/learn/day1/hello")
    String hello() {
        return "Hello from the API";
    }

    @GetMapping("/learn/day1/greeting")
    Greeting greeting() {
        return new Greeting("Hello from the API");
    }

    record Greeting(String message) { }
}
