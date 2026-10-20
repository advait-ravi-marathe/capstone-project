package assignments.renewal;
import org.junit.jupiter.api.Tag; import org.junit.jupiter.api.Test; import org.springframework.http.MediaType; import org.springframework.web.bind.annotation.*; import org.springframework.test.web.servlet.MockMvc; import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get; import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
@Tag("learnerAcceptance") class PreviewCapacityHttpAcceptanceTest {
 @Test void mapsOnlyPreviewCapacityErrorsToSanitized503() throws Exception {
  MockMvc mvc=MockMvcBuilders.standaloneSetup(new ThrowingController()).setControllerAdvice(new PreviewCapacityExceptionHandler()).build();
  mvc.perform(get("/test/busy")).andExpect(status().isServiceUnavailable()).andExpect(jsonPath("$.code").value("PREVIEW_BUSY")).andExpect(content().string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("private"))));
  mvc.perform(get("/test/unavailable")).andExpect(status().isServiceUnavailable()).andExpect(jsonPath("$.code").value("PREVIEW_UNAVAILABLE")).andExpect(content().string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("secret"))));
 }
 @RestController static class ThrowingController { @GetMapping("/test/busy") Object busy(){throw new PreviewBusyException();} @GetMapping("/test/unavailable") Object down(){throw new PreviewUnavailableException(new IllegalStateException("secret"));} }
}
