package assignments.contracts;

import tools.jackson.databind.JsonNode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/** Instructor-only witness, deliberately separate from the learner implementation. */
final class OpenApiCompatibilityReference {
    private OpenApiCompatibilityReference() { }

    static OpenApiCompatibilityAnalyzer.CompatibilityReport compare(JsonNode consumer, JsonNode provider) {
        TreeSet<String> issues = new TreeSet<>();
        JsonNode oldPaths = consumer.path("paths");
        JsonNode newPaths = provider.path("paths");
        for (String route : names(oldPaths)) {
            JsonNode oldPath = oldPaths.path(route);
            JsonNode nextPath = newPaths.path(route);
            if (nextPath.isMissingNode()) {
                issues.add("removed operation path " + route);
                continue;
            }
            for (String verb : names(oldPath)) {
                JsonNode oldOperation = oldPath.path(verb);
                JsonNode newOperation = nextPath.path(verb);
                String prefix = verb.toUpperCase() + " " + route + ": ";
                if (newOperation.isMissingNode()) {
                    issues.add("removed operation " + verb.toUpperCase() + " " + route);
                    continue;
                }
                compareResponseStatuses(oldOperation, newOperation, prefix, issues);
                compareRequestRequirements(oldOperation, newOperation, consumer, provider, prefix, issues);
                compareResponseSchemas(oldOperation, newOperation, consumer, provider, prefix, issues);
            }
        }
        List<String> sorted = List.copyOf(issues);
        return new OpenApiCompatibilityAnalyzer.CompatibilityReport(sorted.isEmpty(), sorted);
    }

    private static void compareResponseStatuses(JsonNode oldOperation, JsonNode newOperation,
                                                String prefix, TreeSet<String> issues) {
        for (String status : names(oldOperation.path("responses"))) {
            if (newOperation.path("responses").path(status).isMissingNode()) {
                issues.add(prefix + "removed response status " + status);
            }
        }
    }

    private static void compareRequestRequirements(JsonNode oldOperation, JsonNode newOperation,
                                                   JsonNode oldDocument, JsonNode newDocument,
                                                   String prefix, TreeSet<String> issues) {
        JsonNode oldBody = resolveSchema(oldDocument, oldOperation.path("requestBody").path("content").path("application/json").path("schema"));
        JsonNode newBody = resolveSchema(newDocument, newOperation.path("requestBody").path("content").path("application/json").path("schema"));
        if (oldBody.isMissingNode()) return;
        if (newBody.isMissingNode()) { issues.add(prefix + "removed request schema"); return; }
        Set<String> oldRequired = stringSet(oldBody.path("required"));
        Set<String> newRequired = stringSet(newBody.path("required"));
        for (String field : newRequired) {
            if (!oldRequired.contains(field)) issues.add(prefix + "new required request field " + field);
        }
        compareSchemaProperties(oldDocument, newDocument, oldBody, newBody, prefix + "request", issues, 0);
    }

    private static void compareResponseSchemas(JsonNode oldOperation, JsonNode newOperation,
                                               JsonNode oldDocument, JsonNode newDocument,
                                               String prefix, TreeSet<String> issues) {
        JsonNode oldResponses = oldOperation.path("responses");
        for (String status : names(oldResponses)) {
            JsonNode nextResponse = newOperation.path("responses").path(status);
            if (nextResponse.isMissingNode()) continue;
            JsonNode oldSchema = oldResponses.path(status).path("content").path("application/json").path("schema");
            JsonNode newSchema = nextResponse.path("content").path("application/json").path("schema");
            if (oldSchema.isMissingNode()) continue;
            if (newSchema.isMissingNode()) { issues.add(prefix + "response " + status + " removed content schema"); continue; }
            JsonNode oldResolved = resolveSchema(oldDocument, oldSchema);
            JsonNode newResolved = resolveSchema(newDocument, newSchema);
            String responsePrefix = prefix + "response " + status;
            if (oldResolved.isMissingNode() || newResolved.isMissingNode()) {
                issues.add(responsePrefix + " unresolved schema reference");
                continue;
            }
            for (String field : stringSet(oldResolved.path("required"))) {
                if (!stringSet(newResolved.path("required")).contains(field) || newResolved.path("properties").path(field).isMissingNode()) {
                    issues.add(responsePrefix + " removed required field " + field);
                }
            }
            compareSchemaProperties(oldDocument, newDocument, oldResolved, newResolved,
                    responsePrefix, issues, 0);
        }
    }

    private static void compareSchemaProperties(JsonNode oldDocument, JsonNode newDocument,
                                                JsonNode oldSchema, JsonNode newSchema, String prefix,
                                                TreeSet<String> issues, int depth) {
        if (depth > 12) { issues.add(prefix + " schema reference depth exceeded"); return; }
        JsonNode oldProperties = oldSchema.path("properties"), newProperties = newSchema.path("properties");
        for (String name : names(oldProperties)) {
            JsonNode oldProperty = oldProperties.path(name), newProperty = newProperties.path(name);
            if (newProperty.isMissingNode()) continue;
            compareProperty(oldDocument, newDocument, oldProperty, newProperty, name, prefix, issues, depth + 1);
        }
    }

    private static void compareProperty(JsonNode oldDocument, JsonNode newDocument, JsonNode oldRaw, JsonNode newRaw,
                                        String path, String prefix, TreeSet<String> issues, int depth) {
        JsonNode oldSchema = resolveSchema(oldDocument, oldRaw), newSchema = resolveSchema(newDocument, newRaw);
        if (oldSchema.isMissingNode() || newSchema.isMissingNode()) {
            issues.add(prefix + " unresolved schema for field " + path);
            return;
        }
        if (!schemaType(oldSchema.path("type")).equals(schemaType(newSchema.path("type")))) {
            issues.add(prefix + " changed type for field " + path);
            return;
        }
        if (oldSchema.path("type").asText("").equals("array")) {
            JsonNode oldItems = oldSchema.path("items"), newItems = newSchema.path("items");
            if (!oldItems.isMissingNode() && !newItems.isMissingNode())
                compareProperty(oldDocument, newDocument, oldItems, newItems, path + "[]", prefix, issues, depth + 1);
        }
        if (oldSchema.isObject() || oldSchema.path("type").asText("").equals("object")) {
            for (String field : stringSet(oldSchema.path("required"))) {
                if (!stringSet(newSchema.path("required")).contains(field) || newSchema.path("properties").path(field).isMissingNode())
                    issues.add(prefix + " removed required field " + path + "." + field);
            }
            compareSchemaProperties(oldDocument, newDocument, oldSchema, newSchema, prefix, issues, depth + 1);
        }
    }

    private static String schemaType(JsonNode type) {
        if (type.isArray()) {
            List<String> values = new ArrayList<>();
            type.forEach(value -> values.add(value.asText()));
            Collections.sort(values);
            return String.join("|", values);
        }
        return type.asText();
    }

    private static JsonNode resolve(JsonNode document, String ref) {
        if (!ref.startsWith("#/")) return document.path("__missing__");
        JsonNode node = document;
        for (String token : ref.substring(2).split("/")) node = node.path(token.replace("~1", "/").replace("~0", "~"));
        return node;
    }

    private static JsonNode resolveSchema(JsonNode document, JsonNode schema) {
        String ref = schema.path("$ref").asText("");
        if (ref.isEmpty()) return schema;
        JsonNode resolved = resolve(document, ref);
        return resolved.isMissingNode() ? resolved : resolved;
    }

    private static List<String> names(JsonNode object) {
        List<String> values = new ArrayList<>();
        values.addAll(object.propertyNames());
        return values;
    }

    private static Set<String> stringSet(JsonNode array) {
        TreeSet<String> values = new TreeSet<>();
        if (array.isArray()) array.forEach(value -> values.add(value.asText()));
        return Collections.unmodifiableSet(values);
    }
}
