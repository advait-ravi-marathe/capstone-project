package coverstory.modules.shortlist;

import coverstory.modules.catalog.CatalogOfferRef;
import coverstory.modules.catalog.CatalogPort;
import coverstory.modules.catalog.CatalogListing;
import coverstory.modules.shortlist.api.OwnedShortlistController;
import io.zonky.test.db.postgres.embedded.EmbeddedPostgres;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.sql.Statement;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

/** Real Spring HTTP boundary and PostgreSQL-owned adapter; authentication is a synthetic test principal. */
class OwnedShortlistPersistenceIntegrationTest {
    private static final UUID OWNER_A = UUID.fromString("a1680000-0000-4000-8000-000000000001");
    private static final UUID OWNER_B = UUID.fromString("b1680000-0000-4000-8000-000000000002");
    private static final UUID OFFER = UUID.fromString("c1680000-0000-4000-8000-000000000003");

    @Test void authenticatedShortlistSurvivesAdapterRecreationAndCannotBeReadAcrossOwners() throws Exception {
        try (EmbeddedPostgres pg = EmbeddedPostgres.builder().start()) {
            JdbcTemplate jdbc = template(pg);
            try (Statement sql = pg.getPostgresDatabase().getConnection().createStatement()) {
                sql.execute("CREATE SCHEMA shortlist");
                sql.execute("CREATE TABLE shortlist.saved_offer(owner_id UUID NOT NULL,offer_id UUID NOT NULL,catalog_version TEXT NOT NULL,created_at TIMESTAMPTZ NOT NULL DEFAULT now(),PRIMARY KEY(owner_id,offer_id))");
            }
            var adapter = new JdbcShortlistAdapter(jdbc, catalog());
            MockMvc mvc = mvc(adapter);
            MvcResult add = mvc.perform(post("/api/v1/me/shortlist/items").contentType("application/json")
                    .content("{\"offerId\":\""+OFFER+"\",\"catalogVersion\":\"health-v3\"}").with(auth(OWNER_A))).andReturn();
            assertEquals(201, add.getResponse().getStatus());

            // Recreate the adapter, equivalent to a new request/process after refresh.
            MockMvc afterRefresh = mvc(new JdbcShortlistAdapter(jdbc, catalog()));
            MvcResult own = afterRefresh.perform(get("/api/v1/me/shortlist").with(auth(OWNER_A))).andReturn();
            assertEquals(200, own.getResponse().getStatus());
            assertTrue(own.getResponse().getContentAsString().contains("health-v3"));
            MvcResult foreign = afterRefresh.perform(get("/api/v1/me/shortlist").with(auth(OWNER_B))).andReturn();
            assertEquals(200, foreign.getResponse().getStatus());
            assertFalse(foreign.getResponse().getContentAsString().contains(OFFER.toString()));
            assertTrue(foreign.getResponse().getContentAsString().contains("\"items\":[]"));
            assertEquals(401, afterRefresh.perform(get("/api/v1/me/shortlist")).andReturn().getResponse().getStatus());
            assertEquals(204, afterRefresh.perform(delete("/api/v1/me/shortlist/items").param("offerId", OFFER.toString()).with(auth(OWNER_A))).andReturn().getResponse().getStatus());
            assertTrue(new JdbcShortlistAdapter(jdbc, catalog()).listForOwner(OWNER_A).isEmpty());
        }
    }

    @Test void cannotSaveCatalogVersionThatIsNotInTheOwningCatalog() throws Exception {
        try (EmbeddedPostgres pg = EmbeddedPostgres.builder().start()) {
            JdbcTemplate jdbc = template(pg);
            jdbc.execute("CREATE SCHEMA shortlist");
            jdbc.execute("CREATE TABLE shortlist.saved_offer(owner_id UUID NOT NULL,offer_id UUID NOT NULL,catalog_version TEXT NOT NULL,created_at TIMESTAMPTZ NOT NULL DEFAULT now(),PRIMARY KEY(owner_id,offer_id))");
            MockMvc mvc = mvc(new JdbcShortlistAdapter(jdbc, catalog()));
            MvcResult response = mvc.perform(post("/api/v1/me/shortlist/items").contentType("application/json")
                    .content("{\"offerId\":\""+OFFER+"\",\"catalogVersion\":\"missing\"}").with(auth(OWNER_A))).andReturn();
            assertEquals(422, response.getResponse().getStatus());
            assertTrue(new JdbcShortlistAdapter(jdbc, catalog()).listForOwner(OWNER_A).isEmpty());
        }
    }

    private static MockMvc mvc(JdbcShortlistAdapter adapter) {
        DefaultListableBeanFactory beans = new DefaultListableBeanFactory();
        beans.registerSingleton("shortlist", (ShortlistPort) adapter);
        return MockMvcBuilders.standaloneSetup(new OwnedShortlistController(beans.getBeanProvider(ShortlistPort.class))).build();
    }
    private static CatalogPort catalog() {
        return new CatalogPort() {
            public java.util.List<CatalogListing> search(String category, String query) { return java.util.List.of(); }
            public java.util.Optional<CatalogOfferRef> findVersion(UUID id,String version) {
                return OFFER.equals(id) && "health-v3".equals(version) ? java.util.Optional.of(new CatalogOfferRef(id,version)) : java.util.Optional.empty();
            }
        };
    }
    private static JdbcTemplate template(EmbeddedPostgres pg) throws SQLException {
        String url;
        try (Connection connection = pg.getPostgresDatabase().getConnection()) { url = connection.getMetaData().getURL(); }
        return new JdbcTemplate(new SimpleDriverDataSource(new org.postgresql.Driver(), url, "postgres", "postgres"));
    }
    private static RequestPostProcessor auth(UUID owner) { return request -> { request.setUserPrincipal(owner::toString); return request; }; }
}
