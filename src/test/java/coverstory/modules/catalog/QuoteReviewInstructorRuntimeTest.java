package coverstory.modules.catalog;

import coverstory.modules.catalog.api.CurrentQuotePort;
import coverstory.modules.catalog.api.QuoteReviewController;
import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

/** Independent fixed-clock positive and failure witnesses for the instructor quote-review boundary. */
class QuoteReviewInstructorRuntimeTest {
    private static final UUID A=UUID.fromString("a1680000-0000-4000-8000-000000000001");
    private static final UUID B=UUID.fromString("b1680000-0000-4000-8000-000000000002");
    private static final String H1="a".repeat(64), H2="b".repeat(64);
    private static final Instant NOW=Instant.parse("2026-11-10T12:00:00Z");

    @Test void unchangedQuoteIsCurrentAndNeverCreatesAnOrder() throws Exception {
        Port quotes=new Port(); MockMvc mvc=mvc(quotes);
        MvcResult result=validate(mvc,A,"quote-base",100000,H1,null,null,null);
        assertEquals(200,result.getResponse().getStatus());
        assertTrue(result.getResponse().getContentAsString().contains("\"deltaPaise\":0"));
        assertTrue(result.getResponse().getContentAsString().contains("\"expiresAt\":"));
        assertTrue(result.getResponse().getContentAsString().contains("\"orderCreated\":false"));
    }

    @Test void everyPaiseAndTermsChangeRequiresExplicitReviewAndLargeChangeEscalates() throws Exception {
        MockMvc mvc=mvc(new Port());
        for(long change: new long[]{1,100,101}) {
            var result=validate(mvc,A,"quote-drift-"+change,100000,H1,null,null,null);
            assertEquals(409,result.getResponse().getStatus());
            assertTrue(result.getResponse().getContentAsString().contains("\"explicitReviewRequired\":true"));
            assertTrue(result.getResponse().getContentAsString().contains("\"deltaPaise\":"+change));
            assertEquals(change>100,result.getResponse().getContentAsString().contains("\"escalationRequired\":true"));
        }
        var terms=validate(mvc,A,"quote-terms",100000,H1,null,null,null);
        assertEquals(409,terms.getResponse().getStatus());
        var accepted=validate(mvc,A,"quote-terms",100000,H1,"r2",100000L,H2);
        assertEquals(200,accepted.getResponse().getStatus());
        assertTrue(accepted.getResponse().getContentAsString().contains("REVIEWED_NO_ORDER"));
        assertTrue(accepted.getResponse().getContentAsString().contains("\"orderCreated\":false"));
    }

    @Test void unknownMissingWithdrawnIllustrationAndExpiredQuotesFailClosed() throws Exception {
        MockMvc mvc=mvc(new Port());
        assertEquals(422,validate(mvc,A,"quote-unknown-tax",100000,H1,null,null,null).getResponse().getStatus());
        assertEquals(422,validate(mvc,A,"quote-missing-total",100000,H1,null,null,null).getResponse().getStatus());
        assertEquals(422,validate(mvc,A,"quote-illustration",100000,H1,null,null,null).getResponse().getStatus());
        assertEquals(409,validate(mvc,A,"quote-withdrawn",100000,H1,null,null,null).getResponse().getStatus());
        assertEquals(409,validate(mvc,A,"quote-expired",100000,H1,null,null,null).getResponse().getStatus());
        assertEquals(404,validate(mvc,A,"quote-owned-by-B",100000,H1,null,null,null).getResponse().getStatus());
    }

    @Test void authenticationPrecedesAnyQuoteLookup() throws Exception {
        Port quotes=new Port(); MockMvc mvc=mvc(quotes);
        var result=mvc.perform(post("/api/v1/me/quote-validation").contentType("application/json")
                .content(request("quote-base",100000,H1,null,null,null))).andReturn();
        assertEquals(401,result.getResponse().getStatus()); assertEquals(0,quotes.lookups.get());
    }

    private static MockMvc mvc(Port quotes) {
        DefaultListableBeanFactory beans=new DefaultListableBeanFactory();
        beans.registerSingleton("quotePort",(CurrentQuotePort)quotes);
        QuoteReviewService service=new QuoteReviewService(quotes,Clock.fixed(NOW,ZoneOffset.UTC));
        beans.registerSingleton("quoteReviewService",service);
        return MockMvcBuilders.standaloneSetup(new QuoteReviewController(beans.getBeanProvider(QuoteReviewService.class))).build();
    }
    private static MvcResult validate(MockMvc mvc,UUID owner,String quote,long total,String terms,String rev,Long acceptedTotal,String acceptedTerms) throws Exception {
        String acceptedRev=rev;
        return mvc.perform(post("/api/v1/me/quote-validation").contentType("application/json")
                .content(request(quote,total,terms,acceptedRev,acceptedTotal,acceptedTerms)).with(auth(owner))).andReturn();
    }
    private static String request(String quote,long total,String terms,String revision,Long acceptedTotal,String acceptedTerms) throws Exception {
        var mapper=new ObjectMapper();
        return mapper.writeValueAsString(new QuoteReviewService.Request(quote,"r1",total,terms,revision,acceptedTotal,acceptedTerms));
    }
    private static RequestPostProcessor auth(UUID owner) { return request->{request.setUserPrincipal(owner::toString);return request;}; }

    private static final class Port implements CurrentQuotePort {
        private final AtomicInteger lookups=new AtomicInteger();
        @Override public Optional<Snapshot> findCurrent(UUID owner,String id) {
            lookups.incrementAndGet();
            UUID actualOwner=id.equals("quote-owned-by-B")?B:A;
            long total=100000; String revision="r1", terms=H1; boolean available=true,tax=true,illustration=false; Long totalValue=total; Instant expiry=NOW.plusSeconds(3600);
            if(id.startsWith("quote-drift-")) totalValue=100000+Long.parseLong(id.substring("quote-drift-".length()));
            if(id.equals("quote-terms")) { revision="r2"; terms=H2; }
            if(id.equals("quote-unknown-tax")) tax=false;
            if(id.equals("quote-missing-total")) totalValue=null;
            if(id.equals("quote-illustration")) illustration=true;
            if(id.equals("quote-withdrawn")) available=false;
            if(id.equals("quote-expired")) expiry=NOW.minusSeconds(1);
            if(!actualOwner.equals(owner)) return Optional.of(new Snapshot(actualOwner,id,revision,"health-basic","sandbox-A","v1",totalValue,"INR",tax,available,illustration,terms,expiry));
            return Optional.of(new Snapshot(owner,id,revision,"health-basic","sandbox-A","v1",totalValue,"INR",tax,available,illustration,terms,expiry));
        }
    }
}
