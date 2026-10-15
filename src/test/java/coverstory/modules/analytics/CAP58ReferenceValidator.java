package coverstory.modules.analytics;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import coverstory.modules.analytics.api.CAP58Seam;
import java.util.*;

/** Independent instructor validator used only to prove the live adapter seam is connected. */
class CAP58ReferenceValidator extends CAP58Seam {
    private static final ObjectMapper JSON=new ObjectMapper();
    private static final Set<String> FIELDS=Set.of("eventId","eventType","schemaVersion","pseudonymousSessionId","pageId","targetId","optionalDurationMs","clientTime","stepCode","journeyId");
    private static final Set<String> PAGES=Set.of("health_discovery","compare","quote_review","home","checkout","onboarding","final_review");
    private static final Set<String> STEPS=Set.of("DISCOVER","COMPARE","QUOTE_REVIEW","ONBOARDING","FINAL_REVIEW");
    @Override public ValidationResult validateAnalyticsBatch(byte[] body,TrustedAnalyticsContext context){
        if(!context.collectionAllowed())return new ValidationResult(ResultStatus.DISCARDED_BY_POLICY,List.of(),null);
        final JsonNode events;
        try{JsonNode root=JSON.readTree(body);events=root.path("events");if(!events.isArray()||events.isEmpty()||events.size()>10)return invalid("INVALID_EVENT_COUNT");}
        catch(Exception bad){return invalid("INVALID_JSON");}
        List<SanitizedEvent> clean=new ArrayList<>();Set<String> ids=new HashSet<>();
        for(JsonNode e:events){
            if(!e.isObject())return invalid("INVALID_EVENT");
            for(String field:e.propertyNames())if(!FIELDS.contains(field))return invalid("UNSAFE_FIELD");
            String id=e.path("eventId").asText(),session=e.path("pseudonymousSessionId").asText();
            if(!uuid(id)||!uuid(session)||!ids.add(id)||e.path("schemaVersion").asInt(-1)!=1)return invalid("INVALID_ID_OR_VERSION");
            String type=e.path("eventType").asText(),page=e.path("pageId").asText();
            if(!PAGES.contains(page)||(!type.equals("PAGE_VIEW")&&!type.equals("CONTROL_CLICK")))return invalid("INVALID_EVENT_TYPE_OR_PAGE");
            String target=e.has("targetId")?e.path("targetId").asText():null;
            if(type.equals("PAGE_VIEW")&&target!=null)return invalid("PAGE_VIEW_TARGET_FORBIDDEN");
            if(type.equals("CONTROL_CLICK")&&(target==null||!Set.of("compare_plan","save_draft").contains(target)))return invalid("INVALID_TARGET");
            String step=e.has("stepCode")?e.path("stepCode").asText():null;
            if(step!=null&&!STEPS.contains(step))return invalid("INVALID_STEP");
            Integer duration=null;if(e.has("optionalDurationMs")){JsonNode n=e.get("optionalDurationMs");if(!n.isIntegralNumber()||n.asInt(-1)<0||n.asInt()>300000)return invalid("INVALID_DURATION");duration=n.asInt();}
            if(e.has("journeyId")){String journey=e.path("journeyId").asText();if(!uuid(journey)||!context.ownedJourneyIds().contains(journey))return invalid("JOURNEY_NOT_AUTHORIZED");}
            clean.add(new SanitizedEvent(id,type,page,step,target,duration,session,context.serverReceivedAt()));
        }
        return new ValidationResult(ResultStatus.VALID,List.copyOf(clean),null);
    }
    private static ValidationResult invalid(String code){return new ValidationResult(ResultStatus.INVALID,List.of(),code);}
    private static boolean uuid(String value){try{UUID.fromString(value);return true;}catch(RuntimeException e){return false;}}
}
