# Day 3: translate failures into safe HTTP responses

This focused guide supports the existing `ApiExceptionHandler` learner seam. The reference uses a separate toy error contract; do not copy its problem strings into the capstone. Its MockMvc checks run in-process and do not prove listening-server behavior, authentication, persistence, or the production app's complete exception flow.

## Study first

Use the confirmed-owned in28Minutes course *Master Spring Boot 3 & Spring Framework 6 with Java*, section **Creating a Java REST API with Spring Boot, Spring Framework and Hibernate**. Required: **Step 12 - Implementing Generic Exception Handling for all Resources** (11:29; item `35018806`). The course's social-media example supplies the generic exception-boundary concept. Optional later reference only: **Step 11 - Implementing Exception Handling - 404 Resource Not Found** (7:49; item `35018810`). Do not stack both videos with the full guide inside today's study block. Catalog title/duration metadata was verified; playback is not claimed here.

Prerequisite: understand the Day 1 route/controller/response bridge and Day 2's distinction between a mapped failure and validation before the collaborator. The toy path-variable example is explained below; the optional Day 1 path-variable lesson is not assumed.

## Complete toy reference

The installed test-only reference is `src/test/java/assignments/learning/Day03SafeErrorsReferenceTest.java`. `@GetMapping` binds `{id}` into the `@PathVariable`. For `busy`, the controller throws the known `ShelfBusy` exception; its specific handler returns HTTP 409 and `{"problem":"TRY_LATER"}`. For other identifiers, it throws an unexpected exception containing private fixture text; the generic handler returns HTTP 500 and the fixed body `{"problem":"UNAVAILABLE"}`. The private exception message is not exposed. Here is the complete reference:

```java
package assignments.learning;

import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class Day03SafeErrorsReferenceTest {
    public record Problem(String problem) { }
    static final class ShelfBusy extends RuntimeException { }

    @RestController
    static final class ShelfController {
        @GetMapping("/teaching/shelf/{id}")
        String read(@PathVariable("id") String id) {
            if (id.equals("busy")) throw new ShelfBusy();
            throw new IllegalStateException("private database password-like fixture");
        }
    }

    @RestControllerAdvice
    static final class SafeAdvice {
        @ExceptionHandler(ShelfBusy.class)
        ResponseEntity<Problem> busy() {
            return ResponseEntity.status(409).body(new Problem("TRY_LATER"));
        }

        @ExceptionHandler(Exception.class)
        ResponseEntity<Problem> unexpected(Exception ignored) {
            return ResponseEntity.status(500).body(new Problem("UNAVAILABLE"));
        }
    }

    @Test
    void expectedAndUnexpectedFailuresHaveSafeDifferentOutcomes() throws Exception {
        var mvc = MockMvcBuilders.standaloneSetup(new ShelfController())
                .setControllerAdvice(new SafeAdvice()).build();
        mvc.perform(get("/teaching/shelf/busy"))
                .andExpect(status().isConflict())
                .andExpect(content().string("{\"problem\":\"TRY_LATER\"}"));
        mvc.perform(get("/teaching/shelf/other"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("{\"problem\":\"UNAVAILABLE\"}"));
    }

    @Test
    void aNewIdentifierGetsTheSameSanitizedUnexpectedOutcome() throws Exception {
        var mvc = MockMvcBuilders.standaloneSetup(new ShelfController())
                .setControllerAdvice(new SafeAdvice()).build();
        mvc.perform(get("/teaching/shelf/new-id"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("{\"problem\":\"UNAVAILABLE\"}"))
                .andExpect(content().string(org.hamcrest.Matchers.not(
                        org.hamcrest.Matchers.containsString("private database"))));
    }
}
```

Exception translation works in this order: the path mapping invokes the method; throwing stops its normal return; Spring selects a matching exception handler; `ResponseEntity.status(...)` sets the status separately from the body. Expected failures can have an intentional public response. Unknown failures get a fixed safe envelope while authorized server-side diagnostics remain separate. Do not return `exception.getMessage()` to the client.

A new identifier such as `new-id` follows the same toy unexpected-failure path and receives the same sanitized 500. This is a practice variation, not a claim that an unknown capstone identifier should become a 500. The capstone's 404 behavior is explicitly non-disclosing.

Run the instructor reference:

```sh
./mvnw -Dtest=Day03SafeErrorsReferenceTest test
```

It proves these supplied in-process toy mappings and exact response bodies, not a real server, production logging, auth, or database behavior.

Verified on 2026-10-21 with the repository’s installed Java/Spring/JUnit dependencies: `./mvnw -Dtest=Day03SafeErrorsReferenceTest test` — 2 tests, all passed. This instructor result does not change the learner status.

## Transfer to the fixed capstone contract

The existing learner selector remains:

```sh
./mvnw -Plearner-tests -Dtest=Day03ContractTest test
```

The required capstone mappings are: `ResourceNotFoundException` → HTTP 404 / `RESOURCE_NOT_FOUND` / `Resource was not found`; `VersionConflictException` → HTTP 409 / `VERSION_CONFLICT` / `The resource version conflicts`; unexpected exception → HTTP 500 / `INTERNAL_ERROR` / `The request could not be completed`. Do not return stack traces, SQL, request bodies, or internal exception messages. The supplied malformed-body 400 handler is outside today's learner seam and must remain unchanged.

After studying the example, predict how `new-id` maps, then explain the status and fixed public body. A useful design question is: **Why does the client get a stable error code while internal diagnostic text stays out of the response?** Do not require the later Day 12 401/403 distinction or complete an ADR.

## Timebox and evidence

Within the existing 25-minute study block: required video 11:29, worked trace up to 9:31, readiness prediction 4:00. Use Step 11 only as a later reference, not as additional mandatory study. Within existing 85-minute practice/tests: run and inspect the reference 15 minutes, make/predict the distinct identifier variation 10, implement the existing `handle(Exception)` seam 45, then run selectors/diagnose 15. The existing 10-minute evidence block records exact status/body, one redaction explanation, command/result and assistance used. Preserve today's existing DSA task and date; this reference is not completion credit.

Primary source: Spring MVC [exception handling and `@ExceptionHandler`](https://docs.spring.io/spring-framework/reference/web/webmvc/mvc-controller/ann-exceptionhandler.html), initial handler example and exception mapping. The course lesson and worked example do not authorize changing the repository's fixed error contract.
