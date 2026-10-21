# Day 70 — Recover, explain and hand over honestly

**Study: 25 minutes.** Review actual pass/fail/blocked/not-run evidence; use the completed fictional parcel handover below; read C4 “Software system”, “Container”, and “Component” to name boundaries. A container in C4 means a deployable/runtime unit, not necessarily Docker.

## Fictional worked handover

**Question:** Can a parcel pickup preview be demonstrated safely on one developer laptop? **Boundary:** browser → one loopback Java process → disposable synthetic database. **Evidence:** archive assets and health returned200; API/DOM/full rows agreed; member-b was concealed with404; owner-switch left old member-a data visible. **Not run:** causal active-cookie restart. **Decision:** not ready to call the demo complete; clear stale UI and rerun owner switching plus a valid active-cookie restart. **Limits:** no real users, deployment, enterprise identity, uptime or production security evidence.

**Recovery within 85 minutes:** 15 reproduce with a delayed response; 25 fix only the state seam; 15 targeted regression; 20 simulated design/demo questions; 10 risk/retest. If recovery needs time, shorten the mock; no overtime.

## Transfer

Replace every fictional result with actual CoverStory evidence, preserve failed/blocked/not-run states, identify one system boundary, one invariant and one limitation. The manifest integrity test checks planning structure; it does not establish mastery.

**Practice 85:** 55 highest-priority recovery/retest, 20 design mock, 10 risk triage. **Evidence 10:** actual result, changed paths, next attempt. **Selector:** `./mvnw -Plearner-tests -Dtest=LocalApplicationManifestInfrastructureTest#day70EvidenceAndDesignMockStayWithinTheDefinedSlice test`.
