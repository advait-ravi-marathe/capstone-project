package coverstory.modules.catalog;

import coverstory.modules.catalog.api.CAP52Controller;
import coverstory.modules.catalog.api.CAP52Seam;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

@Tag("learnerAcceptance")
class CAP52AcceptanceTest {
    private static final String VALID = "{\"sourceId\":\"synthetic-source\",\"sourceVersion\":\"v1\",\"canonicalHash\":\"aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa\"}";
    private final MockMvc mvc = standaloneSetup(new CAP52Controller(new CAP52Seam())).build();

    @Test void firstEditorImportIsCreatedAndSameContentRetryIsDuplicate() throws Exception {
        mvc.perform(post("/api/v1/catalog/imports").with(actor()).contentType("application/json").content(VALID))
                .andExpect(status().isCreated());
        mvc.perform(post("/api/v1/catalog/imports").with(actor()).contentType("application/json").content(VALID))
                .andExpect(status().isOk());
    }

    @Test void sameSourceVersionWithChangedHashConflictsWithoutReplacingOriginal() throws Exception {
        mvc.perform(post("/api/v1/catalog/imports").with(actor()).contentType("application/json").content(VALID))
                .andExpect(status().isCreated());
        String changed = VALID.replace("aaaa", "bbbb");
        mvc.perform(post("/api/v1/catalog/imports").with(actor()).contentType("application/json").content(changed))
                .andExpect(status().isConflict());
    }

    private static org.springframework.test.web.servlet.request.RequestPostProcessor actor() {
        return request -> { request.setAttribute("trustedEditor", new CAP52Seam.Principal("editor-1", CAP52Seam.Role.EDITOR)); return request; };
    }
}
