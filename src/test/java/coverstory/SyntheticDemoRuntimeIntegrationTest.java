package coverstory;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.server.context.WebServerApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;

import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.*;
import io.zonky.test.db.postgres.embedded.EmbeddedPostgres;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/** Actual app startup with explicitly isolated synthetic demo profile and the learner freshness process bridge. */
class SyntheticDemoRuntimeIntegrationTest {
    @Test void liveRoutesSwitchDemoOwnerAndRequireExactReviewWithoutCreatingOrders() throws Exception {
        try(ConfigurableApplicationContext context=new SpringApplicationBuilder(CoverStoryApplication.class)
                .web(WebApplicationType.SERVLET).profiles("synthetic-demo")
                .properties("server.port=0","spring.main.banner-mode=off","logging.level.root=ERROR",
                        "coverstory.synthetic-demo.identity.enabled=true")
                .run()) {
            int port=((WebServerApplicationContext)context).getWebServer().getPort();
            CookieManager cookies=new CookieManager(null,CookiePolicy.ACCEPT_ALL);
            HttpClient client=HttpClient.newBuilder().cookieHandler(cookies).build();
            ObjectMapper json=new ObjectMapper();
            HttpResponse<String> selected=send(client,port,"POST","/api/demo/session","{\"account\":\"A\"}");
            assertEquals(200,selected.statusCode());
            assertTrue(send(client,port,"GET","/api/session",null).body().contains("a1680000-0000-4000-8000-000000000001"));

            String pending="{\"quoteId\":\"demo-health-A-v1\",\"displayedRevision\":\"revision-v1\",\"displayedTotalPaise\":100000,\"displayedTermsHash\":\""+"a".repeat(64)+"\"}";
            HttpResponse<String> review=send(client,port,"POST","/api/v1/me/quote-validation",pending);
            assertEquals(409,review.statusCode());
            JsonNode reviewBody=json.readTree(review.body());
            assertEquals(101,reviewBody.path("deltaPaise").asInt());
            assertTrue(reviewBody.path("escalationRequired").asBoolean());
            assertFalse(reviewBody.path("orderCreated").asBoolean());

            String accepted="{\"quoteId\":\"demo-health-A-v1\",\"displayedRevision\":\"revision-v1\",\"displayedTotalPaise\":100000,\"displayedTermsHash\":\""+"a".repeat(64)+"\",\"acceptedRevision\":\"revision-v2\",\"acceptedTotalPaise\":100101,\"acceptedTermsHash\":\""+"b".repeat(64)+"\"}";
            HttpResponse<String> confirmedReview=send(client,port,"POST","/api/v1/me/quote-validation",accepted);
            assertEquals(200,confirmedReview.statusCode());
            assertTrue(confirmedReview.body().contains("REVIEWED_NO_ORDER"));
            assertFalse(json.readTree(confirmedReview.body()).path("orderCreated").asBoolean());

            HttpResponse<String> freshness=send(client,port,"GET","/api/catalog/source-freshness",null);
            assertEquals(501,freshness.statusCode());
            assertTrue(freshness.body().contains("FRESHNESS_NOT_IMPLEMENTED"),"configured bridge reaches the actual CAP-79 TODO");

            send(client,port,"POST","/api/demo/session","{\"account\":\"B\"}");
            assertTrue(send(client,port,"GET","/api/session",null).body().contains("b1680000-0000-4000-8000-000000000002"));
        }
    }

    @Test void profileAloneDoesNotEnableDemoIdentitySelector() throws Exception {
        try(ConfigurableApplicationContext context=new SpringApplicationBuilder(CoverStoryApplication.class)
                .web(WebApplicationType.SERVLET).profiles("synthetic-demo")
                .properties("server.port=0","spring.main.banner-mode=off","logging.level.root=ERROR")
                .run()) {
            int port=((WebServerApplicationContext)context).getWebServer().getPort();
            HttpClient client=HttpClient.newHttpClient();
            assertTrue(context.getBeansOfType(coverstory.api.SyntheticDemoSessionController.class).isEmpty());
            assertTrue(context.getBeansOfType(coverstory.api.SyntheticDemoSessionFilter.class).isEmpty());
            assertEquals(401,send(client,port,"GET","/api/session",null).statusCode());
        }
    }

    @Test void postgresBackedDemoWiresOwnedShortlistAndReplayFeedWithoutCrossOwnerEffects() throws Exception {
        try(EmbeddedPostgres pg=EmbeddedPostgres.builder().start()) {
            String url; try(Connection connection=pg.getPostgresDatabase().getConnection()){url=connection.getMetaData().getURL();}
            DataSource dataSource=new SimpleDriverDataSource(new org.postgresql.Driver(),url,"postgres","postgres");
            try(Connection connection=pg.getPostgresDatabase().getConnection()) {
                executeScript(connection,"/db/schema/ownership.sql");
                executeScript(connection,"/db/schema/constraints.sql");
                executeScript(connection,"/db/schema/cap78_purchase.sql");
                executeScript(connection,"/db/fixtures/synthetic-demo.sql");
            }
            try(ConfigurableApplicationContext context=new SpringApplicationBuilder(CoverStoryApplication.class)
                    .web(WebApplicationType.SERVLET).profiles("synthetic-demo")
                    .properties("server.port=0","spring.main.banner-mode=off","logging.level.root=ERROR",
                            "coverstory.synthetic-demo.identity.enabled=true",
                            "coverstory.application.jdbc.url="+url,"coverstory.application.jdbc.username=postgres",
                            "coverstory.application.jdbc.password=postgres")
                    .run()) {
                int port=((WebServerApplicationContext)context).getWebServer().getPort();
                CookieManager cookies=new CookieManager(null,CookiePolicy.ACCEPT_ALL);
                HttpClient client=HttpClient.newBuilder().cookieHandler(cookies).build();
                send(client,port,"POST","/api/demo/session","{\"account\":\"A\"}");
                assertEquals(200,send(client,port,"GET","/api/v1/me/shortlist",null).statusCode());
                String save="{\"offerId\":\"10000000-0000-4000-8000-000000000001\",\"catalogVersion\":\"v1\"}";
                assertEquals(201,send(client,port,"POST","/api/v1/me/shortlist/items",save).statusCode());
                assertTrue(send(client,port,"GET","/api/v1/me/shortlist",null).body().contains("10000000-0000-4000-8000-000000000001"));
                HttpResponse<String> journeys=send(client,port,"GET","/api/v1/me/journeys?limit=20",null);
                assertEquals(501,journeys.statusCode(),"the runtime route reaches the preserved CAP-54 learner seam");

                HttpResponse<String> learner=send(client,port,"POST","/api/v1/me/payments/mock-confirmations/demo-event-A-seq3/replay","");
                assertEquals(501,learner.statusCode(),"real application wiring reaches the preserved CAP-80 learner seam");
                try(Connection connection=dataSource.getConnection(); Statement statement=connection.createStatement();
                    ResultSet result=statement.executeQuery("SELECT last_sequence FROM payments.payment_checkpoint WHERE owner_id='a1680000-0000-4000-8000-000000000001'")) {
                    assertTrue(result.next()); assertEquals(2,result.getInt(1));
                }

                send(client,port,"POST","/api/demo/session","{\"account\":\"B\"}");
                assertTrue(send(client,port,"GET","/api/v1/me/shortlist",null).body().contains("\"items\":[]"));
                assertEquals(404,send(client,port,"POST","/api/v1/me/payments/mock-confirmations/demo-event-A-seq3/replay","").statusCode());
                send(client,port,"POST","/api/demo/session","{\"account\":\"A\"}");
                assertTrue(send(client,port,"GET","/api/v1/me/shortlist",null).body().contains("10000000-0000-4000-8000-000000000001"));
            }
        }
    }
    private static HttpResponse<String> send(HttpClient client,int port,String method,String path,String body) throws Exception {
        var builder=HttpRequest.newBuilder(URI.create("http://127.0.0.1:"+port+path)).header("Accept","application/json");
        if(body==null) builder.GET(); else builder.header("Content-Type","application/json").POST(HttpRequest.BodyPublishers.ofString(body));
        return client.send(builder.build(),HttpResponse.BodyHandlers.ofString());
    }
    private static void executeScript(Connection connection,String resource)throws Exception {
        String script;
        try(var input=SyntheticDemoRuntimeIntegrationTest.class.getResourceAsStream(resource)) {
            if(input==null) throw new IllegalStateException("Missing test schema "+resource);
            script=new String(input.readAllBytes(),StandardCharsets.UTF_8);
        }
        try(Statement statement=connection.createStatement()) {
            for(String sql:splitStatements(script)) if(!sql.isBlank()) statement.execute(sql);
        }
    }
    private static List<String> splitStatements(String script) {
        List<String> statements=new ArrayList<>(); StringBuilder current=new StringBuilder(); boolean dollar=false;
        for(int i=0;i<script.length();i++) {
            if(script.startsWith("$$",i)){dollar=!dollar;current.append("$$");i++;}
            else if(script.charAt(i)==';'&&!dollar){addStatement(statements,current.toString());current.setLength(0);}
            else current.append(script.charAt(i));
        }
        if(!current.isEmpty())addStatement(statements,current.toString());return statements;
    }
    private static void addStatement(List<String> statements,String candidate) {
        String sql=candidate.lines().filter(line->!line.stripLeading().startsWith("--"))
                .reduce((left,right)->left+"\n"+right).orElse("").trim();
        if(!sql.isEmpty())statements.add(sql);
    }
}
