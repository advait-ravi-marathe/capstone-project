package coverstory.modules.catalog;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import coverstory.modules.catalog.api.CAP167Seam;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/** Independent offline oracle. It does not call CAP167Seam. */
class CAP167InstructorInfrastructureTest {
    private static final ObjectMapper JSON=new ObjectMapper();
    private static final Path FIXTURE=Path.of("fixtures/coverstory/CAP-167/synthetic-v1.json");

    @Test void fixtureIsSyntheticAndBoundedWithoutBrokerOrMoneyEffects() throws Exception {
        JsonNode f=fixture(); assertEquals("SYNTHETIC_OFFLINE_TRACE",f.path("classification").asText());
        assertEquals(8,f.path("max_observations").asInt()); assertFalse(f.path("broker_used").asBoolean()); assertFalse(f.path("money_effects").asBoolean());
        assertEquals(4,f.path("observations").size());
    }
    @Test void referenceAcceptsAllFourSavedCasesAgainstTheirExpectedDiagnostics() throws Exception {
        JsonNode f=fixture();
        for(String name:List.of("interleaved_partitions","product_gap","backwards_partition_offset","independent_products"))
            assertEquals(expected(f.path("expected").path(name)),reference(rows(f,name)),name);
    }
    @Test void globalOffsetOrderingMutantIsRejectedByInterleavedPartitions() throws Exception {
        JsonNode f=fixture(); var observations=rows(f,"interleaved_partitions");
        assertEquals(List.of(),reference(observations));
        assertNotEquals(reference(observations),globalOffsetMutant(observations),"partition-local ordering must not become false global ordering");
    }
    @Test void independentProductStreamsDoNotBorrowEachOthersVersionCursor() throws Exception {
        JsonNode f=fixture();var observations=rows(f,"independent_products");
        assertEquals(List.of(),reference(observations));
        assertFalse(reference(observations).stream().anyMatch(d->d.code().equals("PRODUCT_VERSION_GAP")));
        var gap=reference(rows(f,"product_gap"));assertEquals(1,gap.size());assertEquals(2,gap.getFirst().expected());assertEquals(3,gap.getFirst().actual());
    }

    private static JsonNode fixture()throws Exception{return JSON.readTree(Files.readString(FIXTURE));}
    private static List<CAP167Seam.PublicationObservation> rows(JsonNode f,String name){List<CAP167Seam.PublicationObservation> out=new ArrayList<>();f.path("observations").path(name).forEach(n->out.add(new CAP167Seam.PublicationObservation(n.path("partition").asText(),n.path("offset").asLong(),n.path("productId").asText(),n.path("version").asInt())));return out;}
    private static List<CAP167Seam.Diagnostic> expected(JsonNode a){List<CAP167Seam.Diagnostic> out=new ArrayList<>();a.forEach(n->out.add(new CAP167Seam.Diagnostic(n.path("code").asText(),n.path("partition").asText(),n.path("productId").asText(),n.path("expected").asLong(),n.path("actual").asLong())));return out;}
    private static List<CAP167Seam.Diagnostic> reference(List<CAP167Seam.PublicationObservation> rows){
        if(rows==null||rows.size()>8)throw new IllegalArgumentException("trace size must be 0..8");
        Map<String,Long> offsets=new HashMap<>();Map<String,Integer> versions=new HashMap<>();List<CAP167Seam.Diagnostic> out=new ArrayList<>();
        for(var row:rows){if(row==null||row.partition()==null||row.partition().isBlank()||row.productId()==null||row.productId().isBlank()||row.offset()<0||row.version()<1)throw new IllegalArgumentException("invalid observation");
            Long oldOffset=offsets.put(row.partition(),row.offset());if(oldOffset!=null&&row.offset()<oldOffset)out.add(new CAP167Seam.Diagnostic("PARTITION_OFFSET_BACKWARDS",row.partition(),row.productId(),oldOffset+1,row.offset()));
            Integer oldVersion=versions.put(row.productId(),row.version());if(oldVersion!=null&&row.version()>oldVersion+1)out.add(new CAP167Seam.Diagnostic("PRODUCT_VERSION_GAP",row.partition(),row.productId(),oldVersion+1,row.version()));
        }return out;
    }
    private static List<CAP167Seam.Diagnostic> globalOffsetMutant(List<CAP167Seam.PublicationObservation> rows){List<CAP167Seam.Diagnostic> out=new ArrayList<>();Long last=null;for(var row:rows){if(last!=null&&row.offset()<=last)out.add(new CAP167Seam.Diagnostic("GLOBAL_OFFSET_BACKWARDS",row.partition(),row.productId(),last+1,row.offset()));last=row.offset();}return out;}
}
