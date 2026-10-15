package capstone.coverstory.application;

import coverstory.CoverStoryApplication;
import coverstory.modules.application.api.OfferRevisionFactsPort;
import coverstory.modules.payments.api.OfferPaymentFactsPort;
import coverstory.modules.support.api.AuthorizedOfferEvidencePort;
import io.zonky.test.db.postgres.embedded.EmbeddedPostgres;
import jakarta.servlet.Filter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import org.junit.jupiter.api.Test;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.server.context.WebServerApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Path;
import java.security.Principal;
import java.sql.Connection;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/** Browser -> React/Vite -> real Spring listener -> Application/Support/Payments ports -> disposable PostgreSQL. */
class CAP115ReviewOfferRuntimeIntegrationTest {
    private static final String HEADER="X-CAP115-Instructor-Session";

    @Test void typedAnswerCannotChooseOrPayAndExplicitChoiceUsesAuthoritativeRevision() throws Exception {
        try(CAP115TestDatabase database=CAP115TestDatabase.fresh()) {
            String url,user;
            try(Connection c=database.postgres().getPostgresDatabase().getConnection()) {url=c.getMetaData().getURL();user=c.getMetaData().getUserName();}
            int port;
            try(ConfigurableApplicationContext app=start(url,user)) {
                port=((WebServerApplicationContext)app).getWebServer().getPort();
                assertEquals(401,request(port,"GET","/summary",null,false).statusCode());
                assertEquals(409,request(port,"POST","/choice","{\"offerId\":\"11500000-0000-4000-8000-000000000012\",\"revision\":\"old\",\"contentSha256\":\""+CAP115TestDatabase.REVISED_HASH+"\",\"choice\":\"ACCEPT_OFFER\"}",true).statusCode());
                assertEquals(400,request(port,"POST","/choice","{\"offerId\":\"11500000-0000-4000-8000-000000000012\",\"revision\":\"o2\",\"contentSha256\":\""+CAP115TestDatabase.REVISED_HASH+"\",\"choice\":\"PAY_NOW\"}",true).statusCode());
                String output=runBrowser(port);
                assertTrue(output.contains("\"outcomes\":{\"foreign\":\"UNAUTHORIZED_CITATION\",\"invented\":\"UNKNOWN_FIELD\",\"missing-status\":\"MISSING_STATUS\",\"stale\":\"STALE_OR_FOREIGN_REVISION\"}"),output);
                assertTrue(output.contains("\"abstention\":\"INSUFFICIENT_EVIDENCE\""),output);
                JdbcTemplate jdbc=app.getBean(JdbcTemplate.class);
                assertEquals(1,jdbc.queryForObject("SELECT count(*) FROM cap115_harness.explicit_quote_choice",Integer.class));
                assertEquals("ACCEPT_OFFER",jdbc.queryForObject("SELECT choice FROM cap115_harness.explicit_quote_choice",String.class));
                assertEquals(0,jdbc.queryForObject("SELECT count(*) FROM information_schema.tables WHERE table_schema='cap115_harness' AND table_name LIKE '%payment%'",Integer.class));
            }
            try(ConfigurableApplicationContext restarted=start(url,user)) {
                var after=request(((WebServerApplicationContext)restarted).getWebServer().getPort(),"GET","/summary",null,true);
                assertEquals(200,after.statusCode(),after.body());assertTrue(after.body().contains("\"explicitChoices\":1"),after.body());
                assertTrue(after.body().contains("\"paymentStatus\":\"NOT_STARTED\""),after.body());
            }
        }
    }

    private static ConfigurableApplicationContext start(String url,String user) {
        return new SpringApplicationBuilder(CoverStoryApplication.class,RuntimeConfiguration.class)
                .web(WebApplicationType.SERVLET).properties("server.port=0","spring.main.banner-mode=off","logging.level.root=ERROR",
                        "spring.profiles.active=cap115-instructor-test","spring.datasource.url="+url,"spring.datasource.username="+user,"spring.datasource.password=")
                .run();
    }
    private static HttpResponse<String> request(int port,String method,String path,String body,boolean authenticated)throws Exception {
        var builder=HttpRequest.newBuilder(URI.create("http://127.0.0.1:"+port+"/api/support/__instructor/cap115"+path)).header("Accept","application/json");
        if(authenticated)builder.header(HEADER,CAP115TestDatabase.OWNER_A.toString());
        if("POST".equals(method))builder.header("Content-Type","application/json").POST(HttpRequest.BodyPublishers.ofString(body==null?"{}":body));else builder.GET();
        return HttpClient.newHttpClient().send(builder.build(),HttpResponse.BodyHandlers.ofString());
    }
    private static String runBrowser(int apiPort)throws Exception {
        ProcessBuilder builder=new ProcessBuilder("node","scripts/cap115-browser-flow.mjs","http://127.0.0.1:"+apiPort).directory(Path.of("web").toFile()).redirectErrorStream(true);
        builder.environment().put("npm_config_cache","/tmp/npm-cache");Process process=builder.start();
        if(!process.waitFor(120,TimeUnit.SECONDS)){process.toHandle().descendants().forEach(ProcessHandle::destroyForcibly);process.destroyForcibly();fail("CAP-115 browser-to-API test timed out");}
        String output=new String(process.getInputStream().readAllBytes(),java.nio.charset.StandardCharsets.UTF_8);
        assertEquals(0,process.exitValue(),output);assertTrue(output.contains("CAP115_BROWSER_FLOW"),output);return output;
    }

    @Configuration @Profile("cap115-instructor-test") @Import(CAP115InstructorController.class)
    static class RuntimeConfiguration {
        @Bean javax.sql.DataSource cap115DataSource(org.springframework.core.env.Environment env){return new DriverManagerDataSource(env.getRequiredProperty("spring.datasource.url"),env.getRequiredProperty("spring.datasource.username"),"");}
        @Bean JdbcTemplate cap115JdbcTemplate(javax.sql.DataSource ds){return new JdbcTemplate(ds);}
        @Bean OfferRevisionFactsPort applicationFacts(javax.sql.DataSource ds){return new CAP115ApplicationFactsAdapter(ds);}
        @Bean AuthorizedOfferEvidencePort supportEvidence(javax.sql.DataSource ds){return new CAP115SupportEvidenceAdapter(ds);}
        @Bean OfferPaymentFactsPort paymentsFacts(){return new CAP115PaymentsFactsAdapter();}
        @Bean CAP115ReferenceAnswerValidator referenceValidator(){return new CAP115ReferenceAnswerValidator();}
        @Bean CAP115ChoiceStore choiceStore(javax.sql.DataSource ds){return new CAP115ChoiceStore(ds);}
        @Bean FilterRegistrationBean<Filter> syntheticPrincipal(){
            FilterRegistrationBean<Filter> registration=new FilterRegistrationBean<>();
            registration.setFilter((request,response,chain)->{String header=((HttpServletRequest)request).getHeader(HEADER);
                if(CAP115TestDatabase.OWNER_A.toString().equals(header))chain.doFilter(new HttpServletRequestWrapper((HttpServletRequest)request){
                    @Override public Principal getUserPrincipal(){return ()->CAP115TestDatabase.OWNER_A.toString();}},response);else chain.doFilter(request,response);});
            registration.setUrlPatterns(List.of("/api/support/__instructor/cap115/*"));return registration;
        }
    }
}
