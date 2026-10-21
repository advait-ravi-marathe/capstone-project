# Day 34: Request to HTTP response

> Project-authored teaching toy; not official course content and not learner completion evidence. Read this before the day's implementation task. All main prerequisite links refer to immutable main `80cb3cd` and commands run only in that main checkout.

## Worked example

Toy equipment POST `/test/equipment-loans` carries `{itemId:drill-7,clientRequestId:loan-9,pickupDate:2026-12-05}`; fixture Principal is `workshop-a`. Domain outcome Created(loan-23) maps to 201, `Location: /test/equipment-loans/loan-23`, JSON `{loanId:loan-23}`. Same intent maps to 200 and same body without creation Location. Blank key maps to 400 without command call. Missing and foreign item map identically to 404 without item details. Changed same-key intent maps to 409 and leaves loan-23 unchanged. Missing fixture principal maps to 401; principal injection does not create login/authentication.

## Transfer task

Transfer: implement `issueResponse` and the supplied isolated MVC fixture; it invokes the actual Day31/32 service and uses principal separately from the body. Assert status, Location, JSON, call counts and safe error envelope. The test-only `/api/policies` route is not production wiring. Run this acceptance with Day35/36/62 TODOs intact.

## Readiness check

Before editing, predict one changed input and its observable result. If the prediction is wrong, repeat this toy trace inside the study block and carry forward the learner seam; do not add time or infer completion from this guide.

## Executable MVC trace

The instructor’s separate `Day34EquipmentLoanReferenceTest#createdReplayAndConflictUseRealMockMvcBinding` runs a real standalone MockMvc POST through request JSON binding, a fixture Principal, a toy receipt command, typed response mapping, status/header/body serialization, and then a replay and changed-intent conflict. The command is `./mvnw -Dtest=Day34EquipmentLoanReferenceTest#createdReplayAndConflictUseRealMockMvcBinding test`. The test-only `/api/policies` acceptance is separate and calls the actual learner `PolicyLifecycleService.issueResponse`; it remains red until that seam is implemented. Do not count the equipment toy as learner acceptance.

For the actual acceptance, the supplied isolated fixture takes owner only from `Principal`, obtains quote facts from fixture data, and scopes any receipt lookup by owner plus request key. First creation must return 201 and `Location: /api/policies/{policyId}`; identical replay returns 200 with the original full snapshot and no creation Location. Invalid shape is 400; inaccessible/absent quote is the same safe 404; changed intent is 409; missing fixture principal is 401. The missing principal response is fixture behavior, not real authentication. Known `ApiError` has exactly `code` and `message`.
