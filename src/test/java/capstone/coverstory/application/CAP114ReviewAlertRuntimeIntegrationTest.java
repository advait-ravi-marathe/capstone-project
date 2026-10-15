package capstone.coverstory.application;

import coverstory.CoverStoryApplication;
import coverstory.modules.application.api.ReviewStatusFactsPort;
import coverstory.modules.payments.api.ReviewPaymentFactsPort;
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
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Path;
import java.security.Principal;
import java.sql.Connection;
import java.time.Duration;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/** React browser -> actual test-profile Spring endpoints -> disposable PostgreSQL alert state. */
class CAP114ReviewAlertRuntimeIntegrationTest {
    private static final String OWNER="11400000-0000-4000-8000-000000000999";
    private static final String HEADER="X-CAP114-Instructor-Session";

    @Test void alertWindowIsVisibleThroughReactAndPersistsOnlySyntheticPostgresState() throws Exception {
        try (EmbeddedPostgres postgres=EmbeddedPostgres.builder().start()) {
            String url,user;
            try(Connection c=postgres.getPostgresDatabase().getConnection()) {
                url=c.getMetaData().getURL(); user=c.getMetaData().getUserName();
            }
            new ResourceDatabasePopulator(new ClassPathResource("/coverstory/cap_114_review_alert.sql"))
                    .execute(new DriverManagerDataSource(url,user,""));
            int apiPort;
            try(ConfigurableApplicationContext app=start(url,user)) {
                apiPort=((WebServerApplicationContext)app).getWebServer().getPort();
                assertEquals(401,request(apiPort,"GET","/summary",null,false).statusCode());
                assertEquals(400,request(apiPort,"POST","/windows","{\"windowId\":\"invalid\",\"eligibleRequests\":2,\"errorRequests\":3,\"needsInformationCount\":0}",true).statusCode());
                String output=runBrowser(apiPort);
                assertTrue(output.contains("\"qualifying\":[\"PENDING\",\"PENDING\",\"FIRING\"]"),output);
                assertTrue(output.contains("\"resolved\":\"RESOLVED\""),output);
                assertTrue(output.contains("\"exactFive\":\"NORMAL\""),output);
                assertTrue(output.contains("\"lowThenHealthy\":\"NORMAL\""),output);
                assertTrue(output.contains("\"applicationReviewStatus\":\"NEEDS_INFORMATION\""),output);
                assertTrue(output.contains("\"paymentStatus\":\"NOT_STARTED\""),output);
                JdbcTemplate jdbc=app.getBean(JdbcTemplate.class);
                assertEquals(2,jdbc.queryForObject("SELECT count(*) FROM cap114_harness.window_sample",Integer.class));
                assertEquals(0,jdbc.queryForObject("SELECT count(*) FROM cap114_harness.alert_transition",Integer.class));
                assertEquals(0,jdbc.queryForObject("SELECT count(*) FROM cap114_harness.window_sample WHERE window_id LIKE '%pii%'",Integer.class));
            }
            try(ConfigurableApplicationContext restarted=start(url,user)) {
                var persisted=request(((WebServerApplicationContext)restarted).getWebServer().getPort(),"GET","/summary",null,true);
                assertEquals(200,persisted.statusCode(),persisted.body());
                assertTrue(persisted.body().contains("\"recordedWindows\":2"),persisted.body());
                assertTrue(persisted.body().contains("\"classification\":\"SYNTHETIC_ONLY\""),persisted.body());
            }
            try(Connection c=postgres.getPostgresDatabase().getConnection();var s=c.createStatement();
                var rows=s.executeQuery("SELECT table_name FROM information_schema.tables WHERE table_schema='cap114_harness'")) {
                List<String> names=new java.util.ArrayList<>(); while(rows.next())names.add(rows.getString(1));
                assertEquals(List.of("alert_state","alert_transition","window_sample"),names.stream().sorted().toList());
            }
        }
    }

    private static ConfigurableApplicationContext start(String url,String user) {
        return new SpringApplicationBuilder(CoverStoryApplication.class,RuntimeConfiguration.class)
                .web(WebApplicationType.SERVLET)
                .properties("server.port=0","spring.main.banner-mode=off","logging.level.root=ERROR",
                        "spring.profiles.active=cap114-instructor-test","spring.datasource.url="+url,
                        "spring.datasource.username="+user,"spring.datasource.password=")
                .run();
    }

    private static HttpResponse<String> request(int port,String method,String path,String body,boolean authenticated) throws Exception {
        var builder=HttpRequest.newBuilder(URI.create("http://127.0.0.1:"+port+"/api/support/__instructor/cap114"+path))
                .header("Accept","application/json");
        if(authenticated)builder.header(HEADER,OWNER);
        if("POST".equals(method))builder.header("Content-Type","application/json").POST(HttpRequest.BodyPublishers.ofString(body==null?"{}":body));
        else builder.GET();
        return HttpClient.newHttpClient().send(builder.build(),HttpResponse.BodyHandlers.ofString());
    }

    private static String runBrowser(int apiPort) throws Exception {
        ProcessBuilder builder=new ProcessBuilder("node","scripts/cap114-browser-flow.mjs","http://127.0.0.1:"+apiPort)
                .directory(Path.of("web").toFile()).redirectErrorStream(true);
        builder.environment().put("npm_config_cache","/tmp/npm-cache");
        Process process=builder.start();
        if(!process.waitFor(120,TimeUnit.SECONDS)) {
            process.toHandle().descendants().forEach(ProcessHandle::destroyForcibly); process.destroyForcibly();
            fail("CAP-114 React/Spring/PostgreSQL browser test timed out");
        }
        String output=new String(process.getInputStream().readAllBytes(),java.nio.charset.StandardCharsets.UTF_8);
        assertEquals(0,process.exitValue(),output); assertTrue(output.contains("CAP114_BROWSER_FLOW"),output); return output;
    }

    @Configuration @Import(CAP114InstructorController.class) @Profile("cap114-instructor-test")
    static class RuntimeConfiguration {
        @Bean javax.sql.DataSource cap114DataSource(org.springframework.core.env.Environment environment) {
            return new DriverManagerDataSource(environment.getRequiredProperty("spring.datasource.url"),
                    environment.getRequiredProperty("spring.datasource.username"),
                    environment.getProperty("spring.datasource.password", ""));
        }
        @Bean JdbcTemplate cap114JdbcTemplate(javax.sql.DataSource dataSource) { return new JdbcTemplate(dataSource); }
        @Bean CAP114ReferenceAlertWindow referenceWindow(javax.sql.DataSource dataSource) { return new CAP114ReferenceAlertWindow(dataSource); }
        @Bean ReviewStatusFactsPort reviewFacts() { return id -> new ReviewStatusFactsPort.ReviewStatusFact(id,"NEEDS_INFORMATION",List.of("policy_start_date","income_band")); }
        @Bean ReviewPaymentFactsPort paymentFacts() { return id -> new ReviewPaymentFactsPort.PaymentFact("NOT_STARTED",false); }
        @Bean FilterRegistrationBean<Filter> instructorSessionFixture() {
            FilterRegistrationBean<Filter> registration=new FilterRegistrationBean<>();
            registration.setFilter((request,response,chain)->{
                String supplied=((HttpServletRequest)request).getHeader(HEADER);
                if(OWNER.equals(supplied))chain.doFilter(new HttpServletRequestWrapper((HttpServletRequest)request) {
                    @Override public Principal getUserPrincipal(){return ()->OWNER;}
                },response); else chain.doFilter(request,response);
            });
            registration.setUrlPatterns(List.of("/api/support/__instructor/cap114/*")); return registration;
        }
    }
}
