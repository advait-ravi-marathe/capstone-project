package assignments.ops;

import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.*;

class Day141To154InfrastructureTest {
    private final ObjectMapper mapper = new ObjectMapper();

    @Test void manifestExportContractsAndSelectorsAreChronologicalAndEqual() throws Exception {
        Path dir=Path.of("docs/reboot/continuation-141-154");
        JsonNode manifest=mapper.readTree(Files.readString(dir.resolve("manifest.json")));
        JsonNode export=mapper.readTree(Files.readString(dir.resolve("scrum-owner-manifest.json")));
        assertEquals(14,manifest.path("days").size()); assertEquals(14,export.path("cards").size());
        assertEquals("105bbc9dd448460f9bd5f7e6b9c67eafa99a16c3",manifest.path("base_commit").asText());
        JsonNode course=manifest.path("course");
        assertEquals("4993276",course.path("course_id").asText());
        assertEquals("Master Spring Boot 3 & Spring Framework 6 with Java",course.path("title").asText());
        assertEquals("in28Minutes Official",course.path("instructor").asText());
        assertFalse(course.path("video_content_audited").asBoolean());
        JsonNode lessons=manifest.path("verified_lessons");
        assertEquals("Step 10 Understanding Versioning Semantic Versioning",lessons.get(0).path("title").asText());
        assertEquals("Learning Maven with Spring and Spring Boot",lessons.get(0).path("section_title").asText());
        assertEquals("54482075",lessons.get(0).path("source_item_id").asText());
        assertEquals(216,lessons.get(0).path("duration_seconds").asInt());
        assertEquals("Step 08 Maven Core Concept 5 The Build Lifecycle",lessons.get(1).path("title").asText());
        assertEquals("54482093",lessons.get(1).path("source_item_id").asText());
        assertEquals(532,lessons.get(1).path("duration_seconds").asInt());
        assertEquals("Step 04 - Exploring other assert methods",lessons.get(2).path("title").asText());
        assertEquals("35017732",lessons.get(2).path("source_item_id").asText());
        assertEquals(481,lessons.get(2).path("duration_seconds").asInt());
        for(int i=0;i<14;i++) {
            JsonNode d=manifest.path("days").get(i), c=export.path("cards").get(i);
            assertEquals(141+i,d.path("day").asInt()); assertEquals(d.path("day").asInt(),c.path("day_number").asInt());
            assertEquals(LocalDate.of(2027,3,15).plusDays(i).toString(),d.path("date").asText());
            assertEquals(d.path("date").asText(),c.path("date").asText());
            assertEquals(d.path("title").asText(),c.path("title").asText());
            assertEquals(d.path("engineering").toString(),c.path("engineering").toString());
            assertEquals(120,d.path("engineering").path("budget_minutes").asInt());
            assertEquals(1500,d.path("engineering").path("study_total_seconds").asInt());
            Path contract=Path.of(d.path("contract_file").asText());
            String text=Files.readString(contract);
            assertTrue(text.contains("course 4993276"),"every daily contract carries preferred-course metadata");
            assertTrue(text.contains("https://www.udemy.com/course/spring-boot-and-spring-framework-tutorial-for-beginners"));
            for(JsonNode command:d.path("engineering").path("acceptance_commands")) {
                String cmd=command.asText();
                int selector=cmd.indexOf("-Dtest=");
                if(selector>=0) for(String item:cmd.substring(selector+7).split("\\s+",2)[0].split(",")) {
                    String[] pair=item.split("#",2); Path test=findTestPath(pair[0]);
                    assertTrue(Files.isRegularFile(test),"selector class exists: "+pair[0]);
                    if(pair.length==2) for(String method:pair[1].split("\\+"))
                        assertTrue(Files.readString(test).contains("void "+method+"("),"selector method exists: "+pair[0]+"#"+method);
                }
            }
        }
        assertEquals(1,manifest.path("days").get(6).path("recovery_day").asBoolean()?1:0);
        assertTrue(manifest.path("days").get(13).path("recovery_day").asBoolean());
        assertEquals(3,manifest.path("verified_lessons").size());
        assertEquals(1229,manifest.path("verified_lessons").get(0).path("duration_seconds").asInt()+manifest.path("verified_lessons").get(1).path("duration_seconds").asInt()+manifest.path("verified_lessons").get(2).path("duration_seconds").asInt());
    }

    @Test void unchangedSchemaAndLegacyCommandRemainBoundToBase() throws Exception {
        String[] migrationHashes={
                "cfb4aa4204ab90c3d5965347824c5c26399345cc06e8582686af17a9baefa805",
                "af921dbef43a3c773524876131a840a5f39135030ec5721465a5cf0f9ba31cf2",
                "0e32121d204842d9ecf1f7e63ccf0c30eb7c23337f1949d9ed718ebb56a12786",
                "1b0d1375928c4239f57742490a541ac6852f913c7841f26e27fd3151feb8b882",
                "30cc98cc015e48ac04a8e8e607cb075ecd8f397462f36d548121904ff40bb9b3"};
        for(int i=1;i<=5;i++) {
            String name=String.format("V%03d__",i);
            try(var paths=Files.list(Path.of("src/main/resources/db/migration"))) {
                Path current=paths.filter(p->p.getFileName().toString().startsWith(name)).findFirst().orElseThrow();
                String relative=Path.of("src/main/resources/db/migration").resolve(current.getFileName()).toString();
                assertEquals(migrationHashes[i-1],sha256(Files.readAllBytes(current)),"existing migration is immutable: "+relative);
            }
        }
        assertEquals("f68ac9a522a18a38fc888e4d2dac937738936bb0c8faba5ee77f5d4edf0a759a",
                sha256(Files.readAllBytes(Path.of("src/main/java/assignments/delivery/recovery/LocalManualRecoveryCommand.java"))));
        try(var paths=Files.list(Path.of("src/main/resources/db/migration"))) { assertFalse(paths.anyMatch(p->p.getFileName().toString().startsWith("V006"))); }
    }

    @Test void assistantArchitectureInterviewSessionsAndPackagePreparationAreSynchronized() throws Exception {
        Path dir=Path.of("docs/reboot/continuation-141-154");
        JsonNode manifest=mapper.readTree(Files.readString(dir.resolve("manifest.json")));
        JsonNode export=mapper.readTree(Files.readString(dir.resolve("scrum-owner-manifest.json")));
        JsonNode days=manifest.path("days"), cards=export.path("cards");
        assertEquals(120,manifest.path("engineering_budget_minutes").asInt());
        assertTrue(manifest.path("daily_budget_excludes_separate_dsa").asBoolean());
        assertEquals("859e161f6553e9aca8dc501fda152780cec66ef0",
                manifest.path("separate_learning_repository").path("source_commit").asText());
        Path notes=dir.resolve("assistant-architecture-notes.md");
        assertTrue(Files.isRegularFile(notes));
        String noteText=Files.readString(notes);
        int[] mockDays={141,144,148,151};
        for(int i=0;i<days.size();i++) {
            JsonNode day=days.get(i), card=cards.get(i), engineering=day.path("engineering");
            int number=day.path("day").asInt();
            assertEquals(engineering.toString(),card.path("engineering").toString());
            String noteRef=engineering.path("assistant_architecture_note").asText();
            assertTrue(noteRef.endsWith("#day-"+number));
            assertTrue(noteText.contains("## Day "+number+" —"));
            String contract=Files.readString(Path.of(day.path("contract_file").asText()));
            assertTrue(contract.contains(noteRef.substring(noteRef.indexOf('#')+1)));
            assertTrue(contract.contains("No capstone design proposal"));
            assertEquals(noteRef,card.path("learning_block").path("architecture_note").asText());
            JsonNode assignment=engineering.path("interview_assignment");
            boolean expected=java.util.Arrays.stream(mockDays).anyMatch(x->x==number);
            assertEquals(expected,!assignment.isMissingNode()&&!assignment.isNull());
            if(expected) {
                assertEquals("system_design_interview_question_only",assignment.path("proposal_scope").asText());
                assertEquals("assistant_supplied",assignment.path("capstone_architecture").asText());
                assertEquals(45,assignment.path("mock_minutes").asInt());
                assertEquals(85,assignment.path("practice_minutes_available").asInt());
                int mock=0; for(JsonNode phase:assignment.path("mock_phases")) mock+=phase.path("minutes").asInt();
                int slice=0; for(JsonNode minutes:assignment.path("eai_slice_minutes")) slice+=minutes.asInt();
                assertEquals(45,mock); assertEquals(40,slice); assertEquals(85,mock+slice);
                assertEquals(assignment,card.path("engineering").path("interview_assignment"));
            }
            JsonNode prep=engineering.path("preparation_commands"), tests=engineering.path("test_commands"), all=engineering.path("acceptance_commands");
            assertEquals(prep.size()+tests.size(),all.size());
            for(int j=0;j<prep.size();j++) assertEquals(prep.get(j).asText(),all.get(j).asText());
            for(int j=0;j<tests.size();j++) assertEquals(tests.get(j).asText(),all.get(prep.size()+j).asText());
        }
        for(int number:new int[]{146,147,151}) {
            JsonNode commands=days.get(number-141).path("engineering").path("acceptance_commands");
            assertTrue(commands.get(0).asText().contains("-Plocal-app -DskipTests package"));
            assertTrue(commands.get(1).asText().contains("-Dtest="));
        }
        for(int number:new int[]{148,149,153,154}) {
            JsonNode commands=days.get(number-141).path("engineering").path("acceptance_commands");
            assertTrue(commands.get(0).asText().contains("prepare-release-artifacts.sh"));
            assertTrue(commands.get(1).asText().contains("artifact-pair-manifest.json"));
        }
    }

    private static String sha256(byte[] value) throws Exception {
        byte[] digest=java.security.MessageDigest.getInstance("SHA-256").digest(value);
        return java.util.HexFormat.of().formatHex(digest);
    }
    private static Path findTestPath(String className) throws Exception {
        try(var walk=Files.walk(Path.of("src/test/java"))) {
            return walk.filter(p->p.getFileName().toString().equals(className+".java")).findFirst().orElse(Path.of("__missing__"));
        }
    }
}
