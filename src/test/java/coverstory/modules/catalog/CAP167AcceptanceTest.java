package coverstory.modules.catalog;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import coverstory.modules.catalog.api.CAP167Seam;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@Tag("learnerAcceptance")
class CAP167AcceptanceTest {
    private static final ObjectMapper JSON=new ObjectMapper();
    private static final Path FIXTURE=Path.of("fixtures/coverstory/CAP-167/synthetic-v1.json");
    private final CAP167Seam seam=new CAP167Seam();

    @Test void interleavedPartitionOffsetsDoNotCreateFalseGlobalOrderFailure() throws Exception {
        JsonNode f=fixture(); assertEquals(List.of(),seam.checkTrace(rows(f,"interleaved_partitions")));
    }
    @Test void productVersionGapNamesTheMissingNextVersion() throws Exception {
        JsonNode f=fixture(); assertEquals(diagnostics(f.path("expected").path("product_gap")),seam.checkTrace(rows(f,"product_gap")));
    }
    @Test void backwardsOffsetIsCheckedWithinItsPartition() throws Exception {
        JsonNode f=fixture(); assertEquals(diagnostics(f.path("expected").path("backwards_partition_offset")),seam.checkTrace(rows(f,"backwards_partition_offset")));
    }
    @Test void productVersionsAreIndependentAcrossAggregates() throws Exception {
        JsonNode f=fixture(); assertEquals(List.of(),seam.checkTrace(rows(f,"independent_products")));
    }
    @Test void rejectsMoreThanEightAndMalformedObservationBeforeReturningPartialDiagnostics() throws Exception {
        JsonNode f=fixture(); var base=rows(f,"independent_products"); var tooMany=new ArrayList<>(base); while(tooMany.size()<=8)tooMany.add(base.get(0));
        expectExceptionOrTodo(()->seam.checkTrace(tooMany));
        expectExceptionOrTodo(()->seam.checkTrace(List.of(new CAP167Seam.PublicationObservation("p0",-1,"health-A",1))));
        expectExceptionOrTodo(()->seam.checkTrace(List.of(new CAP167Seam.PublicationObservation("p0",0,"health-A",0))));
    }

    private static JsonNode fixture()throws Exception{return JSON.readTree(Files.readString(FIXTURE));}
    private static List<CAP167Seam.PublicationObservation> rows(JsonNode f,String name){List<CAP167Seam.PublicationObservation> out=new ArrayList<>();f.path("observations").path(name).forEach(n->out.add(new CAP167Seam.PublicationObservation(n.path("partition").asText(),n.path("offset").asLong(),n.path("productId").asText(),n.path("version").asInt())));return out;}
    private static List<CAP167Seam.Diagnostic> diagnostics(JsonNode array){List<CAP167Seam.Diagnostic> out=new ArrayList<>();array.forEach(n->out.add(new CAP167Seam.Diagnostic(n.path("code").asText(),n.path("partition").asText(),n.path("productId").asText(),n.path("expected").asLong(),n.path("actual").asLong())));return out;}
    private static void expectExceptionOrTodo(Runnable action){try{action.run();fail("expected invalid trace rejection");}catch(UnsupportedOperationException todo){throw todo;}catch(IllegalArgumentException expected){}}
}
