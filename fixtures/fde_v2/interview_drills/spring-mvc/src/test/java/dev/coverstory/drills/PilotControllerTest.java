package dev.coverstory.drills;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(PilotController.class)
class PilotControllerTest {
    @Autowired MockMvc mvc;
    @MockBean PilotPreviewService service;

    @Test
    void validRequestPassesVerifiedTenantAndValidatedBodyToInjectedService() throws Exception {
        PilotRequest request = new PilotRequest("invoice-triage", 5);
        given(service.preview(eq("tenant-7"), eq(request)))
            .willReturn(new PilotPreview("tenant-7", "invoice-triage", 5, "preview"));

        mvc.perform(post("/pilot/preview").header("X-Tenant-ID", "tenant-7")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"workflow\":\"invoice-triage\",\"limit\":5}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.tenantId").value("tenant-7"))
            .andExpect(jsonPath("$.mode").value("preview"));
        verify(service).preview(eq("tenant-7"), eq(request));
    }

    @Test
    void invalidBodyReturnsStableFieldErrorBeforeServiceInvocation() throws Exception {
        mvc.perform(post("/pilot/preview").header("X-Tenant-ID", "tenant-7")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"workflow\":\"\",\"limit\":0}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("invalid_request"))
            .andExpect(jsonPath("$.field").value("limit"));
        org.mockito.Mockito.verifyNoInteractions(service);
    }

    @Test
    void absentTenantHeaderIsRejectedBeforeServiceInvocation() throws Exception {
        mvc.perform(post("/pilot/preview").contentType(MediaType.APPLICATION_JSON)
                .content("{\"workflow\":\"invoice-triage\",\"limit\":5}"))
            .andExpect(status().isBadRequest());
        org.mockito.Mockito.verifyNoInteractions(service);
    }
}
