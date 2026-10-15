package capstone.coverstory.application.internal;

import java.util.*;

/** Independent read-only reference for persisted trace correlation. */
final class CAP175InstructorOracle {
    record Result(List<SubmissionTraceLinker.SpanLink> links,String unresolved,boolean authorized) { }
    static Result correlate(UUID owner,String submitId,String dispatchId,String investigationId,List<SubmissionTraceLinker.Span> spans) {
        Map<String,SubmissionTraceLinker.Span> byId=new HashMap<>(); spans.forEach(s->byId.put(s.spanId(),s));
        var submit=byId.get(submitId); var dispatch=byId.get(dispatchId); var investigation=byId.get(investigationId);
        if(submit==null)return new Result(List.of(),submitId,true);
        if(!submit.ownerId().equals(owner))return new Result(List.of(),submitId,false);
        if(dispatch==null)return new Result(List.of(),dispatchId,true);
        if(!dispatch.ownerId().equals(owner))return new Result(List.of(),dispatchId,false);
        if(investigation==null)return new Result(List.of(),investigationId,true);
        if(!investigation.ownerId().equals(owner))return new Result(List.of(),investigationId,false);
        return new Result(List.of(new SubmissionTraceLinker.SpanLink(dispatchId,submitId,"SUBMISSION_TO_DISPATCH"),
                new SubmissionTraceLinker.SpanLink(investigationId,dispatchId,"LATER_SUPPORT_INVESTIGATION")),null,true);
    }
}
