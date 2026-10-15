package coverstory.modules.catalog.api;

import capstone.coverstory.shortlist.api.CAP168ClassificationAction;
import capstone.coverstory.shortlist.api.CAP168Seam;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.ServletInputStream;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** Bounded private import boundary; no raw line is persisted or included in diagnostics. */
@RestController
public final class QuoteReviewLinesController {
    private static final int MAX_BYTES=8192;
    private final ObjectProvider<CAP168ClassificationAction> classifier;
    public QuoteReviewLinesController(ObjectProvider<CAP168ClassificationAction> classifier){this.classifier=classifier;}

    @PostMapping(path="/api/v1/me/quote-review/lines",consumes=MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<?> classify(HttpServletRequest request) {
        Principal principal=request.getUserPrincipal();
        if(principal==null || principal.getName()==null) return error(401,"AUTH_REQUIRED");
        try { UUID.fromString(principal.getName()); } catch(IllegalArgumentException invalid){return error(401,"AUTH_REQUIRED");}
        long contentLength=request.getContentLengthLong(); if(contentLength>MAX_BYTES) return error(413,"BODY_TOO_LARGE");
        final String body;
        try {
            ServletInputStream input=request.getInputStream(); ByteArrayOutputStream bytes=new ByteArrayOutputStream();
            byte[] buffer=new byte[1024]; int read;
            while((read=input.read(buffer))!=-1) { if(bytes.size()+read>MAX_BYTES) return error(413,"BODY_TOO_LARGE"); bytes.write(buffer,0,read); }
            body=StandardCharsets.UTF_8.newDecoder().onMalformedInput(CodingErrorAction.REPORT)
                    .onUnmappableCharacter(CodingErrorAction.REPORT).decode(ByteBuffer.wrap(bytes.toByteArray())).toString();
        } catch(IOException unreadable) { return error(400,"BODY_UNREADABLE"); }
        List<String> lines=java.util.Arrays.stream(body.split("\\n",-1)).map(line->line.endsWith("\r")?line.substring(0,line.length()-1):line).toList();
        CAP168ClassificationAction action=classifier.getIfAvailable(); if(action==null) return error(503,"CLASSIFIER_UNAVAILABLE");
        try {
            CAP168Seam.Classification result=action.classify(lines);
            return ResponseEntity.ok(Map.of("schemaMajor",1,"classification",result,"orderCreated",false));
        } catch(UnsupportedOperationException todo) { return error(501,"CLASSIFIER_NOT_IMPLEMENTED"); }
        catch(RuntimeException invalid) { return error(422,"CLASSIFICATION_FAILED"); }
    }
    private static ResponseEntity<Map<String,String>> error(int status,String code){return ResponseEntity.status(status).body(Map.of("code",code));}
}
