package assignments.insurance;

import java.net.URI;

/** Bounded Day34 transport result. Exactly one of policy/error is populated; Location is create-only. */
public record PolicyIssueResponse(int httpStatus, URI location, PolicySnapshot policy, ApiError error) {
    public PolicyIssueResponse {
        if ((policy == null) == (error == null)) throw new IllegalArgumentException("exactly one response body is required");
        if (location != null && httpStatus != 201) throw new IllegalArgumentException("Location is only valid for a new creation");
    }
}
