# Day 2: JSON request to validated service boundary

This is a focused instructor guide for the existing Day 2 validator assignment. It is not a solution for quote selection, and its MockMvc examples run Spring MVC in-process: they do not open a listening socket, authenticate a caller, or prove database persistence.

## Study first

Use the confirmed-owned course *Master Spring Boot 3 & Spring Framework 6 with Java* by in28Minutes Official, section **Creating a Java REST API with Spring Boot, Spring Framework and Hibernate**. Watch the required **Step 09 - Implementing POST Method to create User Resource** (8:36; item `35018816`). It introduces a POST request body and the route/controller flow. Step 14, **Implementing Validations for REST API** (11:47; item `35018798`), is optional later reference only; the task here uses the existing explicit validator and does not ask you to add `@Valid` or change DTOs. Video playback was not verified by this guide's author.

Prerequisite: complete or reproduce the Day 1 API bridge first. It supplies the path → Java method → response idea needed here. If Day 1 quote selection itself is blocked, the isolated Day 2 tests still use their own validation/service fixtures; integrated application completion remains blocked.

### Constructor and test primer

`new NoteController(service)` passes a collaborator to the controller when it is created. This is constructor injection: the controller uses the supplied service rather than secretly constructing one. A Spring app can assemble objects too; this small test assembles them directly.

An `@Test` is an executable check. Arrange creates known input and a test collaborator; act calls one operation; assert compares the observed result with an expected one. `assertEquals(expected, actual)` compares values. `assertThrows(Type.class, () -> operation())` expects the operation inside the lambda to throw that type. A wrong value is an assertion failure; a marked learner TODO exception and a compile error are different outcomes.

The boundary is `[JSON request] → [Spring converts body to DTO] → [controller validates] → [service] → [Java response] → [JSON response]`. Arrows are calls/data handoffs, not new services. A rejected request crosses no arrow to the service.

## Complete toy reference

The installed instructor reference is `src/test/java/assignments/learning/Day02RequestBoundaryReferenceTest.java`. It uses `NoteRequest`, `NoteReply`, and a recording service so that the call count makes the validation boundary visible. Here is the complete reference:

```java
package assignments.learning;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class Day02RequestBoundaryReferenceTest {
    public record NoteRequest(String text) { }
    public record NoteReply(String text) { }
    public record Problem(String problem) { }

    static final class RecordingService {
        int calls;
        NoteReply accept(String text) {
            calls++;
            return new NoteReply(text);
        }
    }

    @RestController
    static final class NoteController {
        private final RecordingService service;
        NoteController(RecordingService service) { this.service = service; }

        @PostMapping("/teaching/notes")
        NoteReply accept(@RequestBody NoteRequest request) {
            if (request == null || request.text() == null || request.text().isBlank()) {
                throw new IllegalArgumentException("missing teaching text");
            }
            return service.accept(request.text());
        }

        @ExceptionHandler({IllegalArgumentException.class, HttpMessageNotReadableException.class})
        ResponseEntity<Problem> invalid() {
            return ResponseEntity.badRequest().body(new Problem("TEXT_REQUIRED"));
        }
    }

    @Test
    void validTextReachesServiceExactlyOnceAndIsReturnedUnchanged() throws Exception {
        var service = new RecordingService();
        MockMvc mvc = MockMvcBuilders.standaloneSetup(new NoteController(service)).build();
        mvc.perform(post("/teaching/notes").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"text\":\"  blue  \"}"))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"text\":\"  blue  \"}"));
        assertEquals(1, service.calls);
    }

    @Test
    void blankAndUnreadableBodiesDoNotReachService() throws Exception {
        var service = new RecordingService();
        MockMvc mvc = MockMvcBuilders.standaloneSetup(new NoteController(service)).build();
        for (String body : new String[]{"{}", "{\"text\":null}", "{\"text\":\" \"}", "{"}) {
            mvc.perform(post("/teaching/notes").contentType(MediaType.APPLICATION_JSON).content(body))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().json("{\"problem\":\"TEXT_REQUIRED\"}"));
        }
        assertEquals(0, service.calls);
    }

    @Test
    void distinctValueSucceedsAndAbsentBodyFailsBeforeService() throws Exception {
        var service = new RecordingService();
        MockMvc mvc = MockMvcBuilders.standaloneSetup(new NoteController(service)).build();
        mvc.perform(post("/teaching/notes").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"text\":\"green\"}"))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"text\":\"green\"}"));
        assertEquals(1, service.calls);
        mvc.perform(post("/teaching/notes").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().json("{\"problem\":\"TEXT_REQUIRED\"}"));
        assertEquals(1, service.calls);
    }
}
```

For `{"text":"  blue  "}`, Spring binds a request DTO; the controller accepts the nonblank text without trimming it; the collaborator is called once; and the response is `{"text":"  blue  "}`. `{}` binds but fails the explicit null-field check; `{"text":" "}` binds but fails the blank check. `{` and a missing body fail during JSON conversion before the controller can call the service. Each invalid case returns HTTP 400 with the toy body's `{"problem":"TEXT_REQUIRED"}` and records zero service calls.

The distinct practice variation is `{"text":"green"}` (200, same value, one call), followed by a request with no body (400, no additional call). Predict each stage first, then run:

```sh
./mvnw -Dtest=Day02RequestBoundaryReferenceTest test
```

This is an in-process MockMvc check, not an actual HTTP server. It proves the mapped MVC/controller boundary and the supplied fake collaborator's call count only.

Verified on 2026-10-21 with the repository’s installed Java/Spring/JUnit dependencies: `./mvnw -Dtest=Day02RequestBoundaryReferenceTest test` — 3 tests, all passed. This instructor result does not change the learner status.

## Transfer to the existing learner task

After the example, inspect `QuoteSelectionRequest`, `QuoteSelectionInput`, `QuoteSelectionRequestValidator.validate`, and the existing Day 2 tests. Follow the request/list/entry/field boundaries. The capstone's empty selection list is valid; a null list is not. Missing or blank key parts and malformed JSON follow the fixed contract. Keep exact identifier strings. Do not add a nonempty-list rule, trim identifiers, replace the DTO, add `@Valid`, or move validation after the service call.

The existing learner selector remains:

```sh
./mvnw -Plearner-tests -Dtest=Day02ContractTest,Day02ApiAcceptanceTest test
```

The isolated API test uses test-owned validation and selection collaborators; it does not show integrated Day 1/2 completion or database writes. A useful design question, after this worked example, is: **Why reject invalid input before invoking the collaborator, and what can a call-count test prove that it cannot prove about persisted data?**

## Timebox and evidence

Within the existing 25-minute study block: required video 8:36, worked guide and constructor/test primer up to 12:24, readiness prediction 4:00. Do not stack the optional validation video into today's study. Within existing 85-minute practice/tests: reference observations 15 minutes, distinct toy variation 10, the existing validator TODO 45, and selectors/diagnosis 15. The existing 10-minute evidence block records the command/result, help used, and a short trace of DTO conversion → validation → service call or rejection. Do not change the separately scheduled DSA block or mark the learner task complete from this reference.

Primary sources: Spring MVC [@RequestBody](https://docs.spring.io/spring-framework/reference/web/webmvc/mvc-controller/ann-methods/requestbody.html); Spring [constructor-based dependency injection](https://docs.spring.io/spring-framework/reference/core/beans/dependencies/factory-collaborators.html); Spring MVC Test [overview](https://docs.spring.io/spring-framework/reference/testing/mockmvc/overview.html); JUnit [assertions and exception testing](https://docs.junit.org/6.1.3/writing-tests/assertions.html). The worked example supplies the concrete bridge; the links alone do not.
