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

/** Instructor-owned in-process example. It is not a real server or the quote-selection solution. */
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

        NoteController(RecordingService service) {
            this.service = service;
        }

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
