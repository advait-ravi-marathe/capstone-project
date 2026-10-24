package assignments.renewal;

import jakarta.servlet.ServletException;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Tag("learnerAcceptance")
class PreviewCapacityHttpAcceptanceTest {
    @Test
    void mapsOnlyPreviewCapacityErrorsToSanitized503() throws Exception {
        MockMvc mvc = MockMvcBuilders.standaloneSetup(new ThrowingController())
                .setControllerAdvice(new PreviewCapacityExceptionHandler()).build();

        mvc.perform(get("/test/busy"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.code").value("PREVIEW_BUSY"))
                .andExpect(content().string(org.hamcrest.Matchers.not(
                        org.hamcrest.Matchers.containsString("private"))));
        mvc.perform(get("/test/unavailable"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.code").value("PREVIEW_UNAVAILABLE"))
                .andExpect(content().string(org.hamcrest.Matchers.not(
                        org.hamcrest.Matchers.containsString("secret"))));
    }

    @Test
    void unrelatedErrorsAreNotRewrittenAsCapacityResponses() {
        MockMvc mvc = MockMvcBuilders.standaloneSetup(new ThrowingController())
                .setControllerAdvice(new PreviewCapacityExceptionHandler()).build();
        ServletException thrown = assertThrows(ServletException.class, () -> mvc.perform(get("/test/unrelated")));
        assertInstanceOf(IllegalArgumentException.class, thrown.getCause());
    }

    @RestController
    static class ThrowingController {
        @GetMapping("/test/busy") Object busy() { throw new PreviewBusyException(); }
        @GetMapping("/test/unavailable") Object unavailable() {
            throw new PreviewUnavailableException(new IllegalStateException("secret"));
        }
        @GetMapping("/test/unrelated") Object unrelated() { throw new IllegalArgumentException("domain failure"); }
    }
}
