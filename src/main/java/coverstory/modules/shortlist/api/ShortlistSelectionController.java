package coverstory.modules.shortlist.api;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Orders only public synthetic catalog references for the in-memory compare
 * preview. It never reads or writes a saved shortlist and deviceScope is not
 * an authentication credential.
 */
@RestController
@RequestMapping("/api/shortlist")
public final class ShortlistSelectionController {
    private final SelectionOrderer seam;

    public ShortlistSelectionController(SelectionOrderer seam) {
        this.seam = seam;
    }

    @PostMapping("/selection-order")
    public ResponseEntity<SelectionOrderResponse> order(
            @RequestBody SelectionOrderRequest request, HttpServletRequest httpRequest) {
        if (httpRequest.getContentLengthLong() > 4096 || request == null
                || request.selections() == null || request.selections().size() > 3) {
            return ResponseEntity.badRequest().build();
        }
        try {
            return ResponseEntity.ok(new SelectionOrderResponse(1,
                    seam.uniqueSelections(request.selections())));
        } catch (IllegalArgumentException invalidSelection) {
            return ResponseEntity.badRequest().build();
        } catch (UnsupportedOperationException learnerTodo) {
            // Keep the app's learner gap explicit and safe until CAP-40 is implemented.
            return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
        }
    }

    public record SelectionOrderRequest(List<SelectionInput> selections) { }
    public record SelectionOrderResponse(int schemaMajor, List<SelectionKey> selections) { }
}
