package assignments.learning.fde;

import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import tools.jackson.databind.ObjectMapper;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Executed toy MVC example: equipment loans, not the policy learner solution. */
class Day34EquipmentLoanReferenceTest {
    record LoanRequest(String itemId,String clientRequestId,String pickupDate) {}
    record LoanReceipt(String owner,String key,String itemId,String pickupDate,String loanId) {}
    record LoanBody(String loanId) {}
    record Err(String code,String message) {}
    static class LoanCommand {
        final Map<String,LoanReceipt> receipts=new HashMap<>(); int created;
        LoanReceipt issue(String owner,LoanRequest r) {
            String k=owner+":"+r.clientRequestId(); var prior=receipts.get(k);
            if(prior!=null) {if(!prior.itemId().equals(r.itemId())||!prior.pickupDate().equals(r.pickupDate())) throw new IllegalStateException("conflict");return prior;}
            var next=new LoanReceipt(owner,r.clientRequestId(),r.itemId(),r.pickupDate(),"loan-"+(23+created++));receipts.put(k,next);return next;
        }
    }
    @RestController static class ToyController {
        final LoanCommand command; ToyController(LoanCommand command){this.command=command;}
        @PostMapping("/test/equipment-loans") ResponseEntity<?> create(@RequestBody LoanRequest request, Principal principal) {
            if(principal==null)return ResponseEntity.status(401).body(new Err("UNAUTHENTICATED","Sign in required"));
            if(request==null||blank(request.itemId())||blank(request.clientRequestId())||blank(request.pickupDate()))return ResponseEntity.badRequest().body(new Err("INVALID_REQUEST","Request validation failed"));
            if(!request.itemId().equals("drill-7"))return ResponseEntity.status(404).body(new Err("RESOURCE_NOT_FOUND","Resource not found"));
            try {boolean fresh=!command.receipts.containsKey(principal.getName()+":"+request.clientRequestId());var receipt=command.issue(principal.getName(),request);
                return fresh?ResponseEntity.created(java.net.URI.create("/test/equipment-loans/"+receipt.loanId())).body(new LoanBody(receipt.loanId())):ResponseEntity.ok(new LoanBody(receipt.loanId()));
            } catch(IllegalStateException conflict) {return ResponseEntity.status(409).body(new Err("IDEMPOTENCY_CONFLICT","Request key conflicts with an earlier request"));}
        }
        boolean blank(String value){return value==null||value.isBlank();}
    }
    @Test void createdReplayAndConflictUseRealMockMvcBinding() throws Exception {
        var command=new LoanCommand(); MockMvc mvc=MockMvcBuilders.standaloneSetup(new ToyController(command)).build(); ObjectMapper json=new ObjectMapper();
        String request="{\"itemId\":\"drill-7\",\"clientRequestId\":\"loan-9\",\"pickupDate\":\"2026-12-05\"}";
        var created=mvc.perform(post("/test/equipment-loans").principal(()->"workshop-a").contentType(APPLICATION_JSON).content(request)).andExpect(status().isCreated()).andReturn();
        assertEquals("/test/equipment-loans/loan-23",created.getResponse().getHeader("Location"));
        var replay=mvc.perform(post("/test/equipment-loans").principal(()->"workshop-a").contentType(APPLICATION_JSON).content(request)).andExpect(status().isOk()).andReturn();
        assertNull(replay.getResponse().getHeader("Location")); assertEquals(json.readTree(created.getResponse().getContentAsString()),json.readTree(replay.getResponse().getContentAsString()));
        mvc.perform(post("/test/equipment-loans").principal(()->"workshop-a").contentType(APPLICATION_JSON).content(request.replace("2026-12-05","2026-12-06"))).andExpect(status().isConflict());
        assertEquals(1,command.receipts.size()); assertEquals(1,command.created);
    }
}
