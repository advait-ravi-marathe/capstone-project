package coverstory.modules.catalog.api;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ImportDecision(@JsonProperty("editor") String editorFixture,
                             @JsonProperty("source") String sourceId,
                             String decision) { }
