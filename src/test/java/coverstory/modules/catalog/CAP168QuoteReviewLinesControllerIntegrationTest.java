package coverstory.modules.catalog;

import capstone.coverstory.shortlist.api.CAP168ClassificationAction;
import capstone.coverstory.shortlist.api.CAP168Seam;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import coverstory.modules.catalog.api.QuoteReviewLinesController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

/** Real HTTP adapter invokes the learner seam; independent reference results prove the transport can pass. */
class CAP168QuoteReviewLinesControllerIntegrationTest {
    private static final UUID OWNER=UUID.fromString("a1680000-0000-4000-8000-000000000001");
    private static final Path FIXTURE=Path.of("fixtures/coverstory/CAP-168/synthetic-v1.json");

    @Test void actualLearnerClassifierIsInvokedAndItsTodoIsASeparate501() throws Exception {
        List<String> lines=fixtureLines();
        MockMvc mvc=mvc(new CAP168Seam()::classifyQuoteReviewLines);
        MvcResult result=post(mvc,String.join("\n",lines));
        assertEquals(501,result.getResponse().getStatus());
        assertTrue(result.getResponse().getContentAsString().contains("CLASSIFIER_NOT_IMPLEMENTED"));
        assertFalse(result.getResponse().getContentAsString().contains("synthetic-token-CAP168"));
    }

    @Test void independentPositiveWitnessReturnsTypedSafeLineageThroughTheSameRoute() throws Exception {
        List<String> expected=fixtureLines(); AtomicReference<List<String>> supplied=new AtomicReference<>();
        CAP168ClassificationAction reference=lines->{
            supplied.set(List.copyOf(lines));
            return new CAP168Seam.Classification(
                    List.of(new CAP168Seam.QuoteReviewRecord(1,"quote-A","motor-renewal","sandbox-A","v3","a".repeat(64),100000,"INR",false),
                            new CAP168Seam.QuoteReviewRecord(3,"quote-B","health-basic","sandbox-B","v1","b".repeat(64),25100,"INR",false)),
                    List.of(new CAP168Seam.SafeRejection(2,"MALFORMED_JSON"),new CAP168Seam.SafeRejection(5,"MISSING_TERMS_HASH")),1,false);
        };
        MvcResult result=post(mvc(reference),String.join("\n",expected));
        assertEquals(200,result.getResponse().getStatus());
        assertEquals(expected,supplied.get());
        JsonNode payload=new ObjectMapper().readTree(result.getResponse().getContentAsString());
        assertEquals(List.of(1,3),List.of(payload.path("classification").path("validRecords").get(0).path("sourceLine").asInt(),
                payload.path("classification").path("validRecords").get(1).path("sourceLine").asInt()));
        assertEquals(List.of(2,5),List.of(payload.path("classification").path("rejections").get(0).path("lineNumber").asInt(),
                payload.path("classification").path("rejections").get(1).path("lineNumber").asInt()));
        assertTrue(payload.toString().contains("\"orderCreated\":false"));
        assertFalse(result.getResponse().getContentAsString().contains("synthetic-token-CAP168"));
    }

    @Test void oversizedOrAnonymousUploadsDoNotCallTheClassifier() throws Exception {
        AtomicReference<Boolean> called=new AtomicReference<>(false);
        MockMvc mvc=mvc(lines->{called.set(true);return new CAP168Seam.Classification(List.of(),List.of(),0,false);});
        MvcResult tooLarge=post(mvc,"x".repeat(8193));
        assertEquals(413,tooLarge.getResponse().getStatus()); assertFalse(called.get());
        MvcResult anonymous=mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/v1/me/quote-review/lines").contentType("text/plain").content("safe" )).andReturn();
        assertEquals(401,anonymous.getResponse().getStatus()); assertFalse(called.get());
    }

    private static MockMvc mvc(CAP168ClassificationAction action){
        DefaultListableBeanFactory beans=new DefaultListableBeanFactory(); beans.registerSingleton("classifier",action);
        return MockMvcBuilders.standaloneSetup(new QuoteReviewLinesController(beans.getBeanProvider(CAP168ClassificationAction.class))).build();
    }
    private static MvcResult post(MockMvc mvc,String body)throws Exception {
        return mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/v1/me/quote-review/lines").contentType("text/plain").content(body).with(auth())).andReturn();
    }
    private static RequestPostProcessor auth(){return request->{request.setUserPrincipal(()->OWNER.toString());return request;};}
    private static List<String> fixtureLines()throws Exception {
        JsonNode node=new ObjectMapper().readTree(Files.readString(FIXTURE)); List<String> lines=new ArrayList<>();
        node.path("source_lines").forEach(value->lines.add(value.asText()));return lines;
    }
}
