# Day 1 API bridge: request, Java method, response

This runnable, test-runtime-only lesson supports the existing Day 1 backend assignment. It does not implement quote selection or change the production application. Keep engineering within its existing 120 minutes: 25 study, 85 practice/tests, 10 evidence. Keep the existing tiered DSA allocation unchanged: 60 minutes for one or two substantive exercises, 75 for three, and 90 for four.

## Study before practice

Use confirmed-owned in28Minutes Official course **Master Spring Boot 3 & Spring Framework 6 with Java** (course ID `4993276`; [course page](https://www.udemy.com/course/spring-boot-and-spring-framework-tutorial-for-beginners/)), section **Creating a Java REST API with Spring Boot, Spring Framework and Hibernate** (section ID `8366018`). Catalog titles were verified on 21 October 2026; video playback has not been checked.

Watch these essential lessons in order:

1. **Step 02 - Creating a Hello World REST API with Spring Boot** (6:21; item `35018836`).
2. **Step 03 - Enhancing the Hello World REST API to return a Bean** (4:30; item `35018834`).

Essential viewing is 10:51. If the word API or request/response is unfamiliar, first add **Step 00 - Creating a REST API with Spring Boot - An Overview** (4:04; item `35018848`); then the total is 14:55. The supplied fixture handles project setup, so **Step 01 - Initializing a REST API Project with Spring Boot** (7:26; item `35018844`) is optional setup reference, not a prerequisite. Step 05's path-variable variation is later work, not a prerequisite for today's practice.

The catalog is for Spring Boot 3, while this repository uses Java 21 and Spring Boot 4.1.1. Follow the HTTP/controller/returned-value concept; do not downgrade dependencies or copy old setup/import instructions. Count pauses and replays inside 25 minutes. Stop at the cap and carry study forward rather than taking time from DSA or adding hours.

## Worked reference

An API is a boundary where one program asks another to return information or perform an action. Here curl is the client and the small Java app is the server. Both run on the same computer: `127.0.0.1` is loopback, and `8081` is the local port.

```java
@RestController
final class Day01GreetingController {
    @GetMapping("/learn/day1/hello")
    String hello() {
        return "Hello from the API";
    }

    @GetMapping("/learn/day1/greeting")
    Greeting greeting() {
        return new Greeting("Hello from the API");
    }

    record Greeting(String message) { }
}
```

`@GetMapping` matches the HTTP method and path. `@RestController` makes a returned value the response body. The `Greeting` record carries a named `message` value; Spring writes it as a JSON object. The instructor fixture contains the runnable imports and loopback guard.

| Step | Example observation |
|---|---|
| Client sends | GET `/learn/day1/greeting`; no request JSON body |
| Spring matches | `@GetMapping("/learn/day1/greeting")` |
| Java method runs | `greeting()` |
| Method returns | `Greeting("Hello from the API")` |
| Client receives | HTTP 200 and JSON `{"message":"Hello from the API"}` |

GET is used because the reference reads a greeting and changes no stored state. The structured record gives the response a named field; the other route shows a plain text value. These are the only design choices to explain now. No architecture decision record or quote-selection algorithm is assigned before the API lesson.

## Start the instructor fixture and inspect actual HTTP

From the repository root, run this command in one terminal. It uses Maven's test runtime so the fixture cannot enter production component scanning. Keep this terminal open while making requests; stop it with Ctrl+C.

```sh
./mvnw test-compile spring-boot:test-run \
  -Dspring-boot.run.main-class=assignments.learning.day01.Day01ApiBridgeApplication \
  -Dspring-boot.run.arguments="--server.address=127.0.0.1 --server.port=8081 --management.endpoints.enabled-by-default=false"
```

Successful startup prints the Spring Boot 4.1.1 banner and a Tomcat started message for port 8081. If that port is occupied, do not stop another process: choose a free local port in the command and use the same port in each URL below. The fixture rejects non-loopback binds.

In a second terminal:

```sh
curl -i http://127.0.0.1:8081/learn/day1/hello
curl -i http://127.0.0.1:8081/learn/day1/greeting
curl -i http://127.0.0.1:8081/learn/day1/missing
curl -i -X POST http://127.0.0.1:8081/learn/day1/hello
```

Expected observations:

- The first response is `200`, `text/plain`, and `Hello from the API`.
- The second is `200`, `application/json`, and `{"message":"Hello from the API"}`.
- The unmapped GET is `404`; no route matched that path.
- POST to the GET-only route is `405` and advertises `Allow: GET`.
- The teaching context does not expose `/api/quote-selections`; that route belongs to the separate application and is absent here.

For each `curl -i` response, the first line gives the status; headers follow; a blank line separates headers from the body. Header ordering and incidental Date headers can differ. A stopped server is different from an unmapped route: after Ctrl+C, curl cannot connect and receives no HTTP status because no server returned a response.

The local smoke was verified with these observed responses on 21 October 2026. Repeat it on the current checkout and record your own result; the existing instructor test is:

```sh
./mvnw -Dtest=Day01ApiBridgeInfrastructureTest test
```

## Separate learner practice

Only after the reference route works, edit the marked method in `src/test/java/assignments/learning/day01/Day01ApiPractice.java`. Make `GET /learn/day1/practice` return a structured response whose existing `message` field is exactly `API practice ready`. The practice test checks the route, 200 status, JSON content type and exact field value:

```sh
./mvnw -Plearner-tests -Dtest=Day01ApiPracticeAcceptanceTest test
```

This practice TODO is intentionally separate from quote selection. The reference is complete; the practice method starts incomplete. A wrong path, status, content type, field or value fails the check. This is a small adaptation exercise, not completion of the original Day 1 assignment.

## Connect the lesson to the real assignment without solving it

Read these existing files in order:

1. `src/main/java/assignments/insurance/QuoteSelectionController.java`: the class route is `POST /api/quote-selections`; `@RequestBody` asks Spring to deserialize the JSON body; the controller validates, then calls a service.
2. `QuoteSelectionRequest.java`: the JSON object has a `selections` list.
3. `QuoteSelectionInput.java`: each selection contains `journeyId` and `quoteVersionId`.
4. `QuoteSelectionPolicy.uniqueSelections`: the separate learner-owned selection rule is still a TODO.

Example request:

```http
POST /api/quote-selections
Content-Type: application/json

{"selections":[{"journeyId":"journey-B","quoteVersionId":"home-secure-v1"}]}
```

The greeting GET has no JSON request body; this POST does. They share the idea that Spring maps HTTP to Java and Java results to HTTP responses, but their request/response contracts are different. The default application currently reaches the Day 2 request-validator TODO before the Day 1 selection TODO. Do not fix Day 2 or production wiring as part of this API bridge. The isolated Day 1 acceptance test has a test-owned validator so its Day 1 learner contract remains independently testable.

The later selection contract identifies a row by the exact pair `(journeyId, quoteVersionId)`. For example, `(journey-A, v1)` and `(journey-B, v1)` are distinct selections even though the quote version matches. This gives context only; do not implement the algorithm or choose a data structure before the prerequisite is understood.

After the bridge, the original domain acceptance command remains authoritative and may still fail at its explicit learner TODO:

```sh
./mvnw -Plearner-tests -Dtest=QuoteSelectionAcceptanceTest,QuoteSelectionApiAcceptanceTest test
```

The prior `./mvnw -Dtest=QuoteSelectionInfrastructureTest test` check covers instructor wiring only. It is not learner completion. A successful practice endpoint is evidence of the API bridge only; quote selection stays incomplete until its original behavior, tests, and explanation are independently demonstrated.

## Timebox and evidence

- **25-minute study:** 10:51 essential videos, or 14:55 with the overview; use the remaining time for this worked guide and a short readiness check. Carry unfinished study forward.
- **85-minute practice/tests:** launch and inspect the reference (up to 20 minutes); reproduce the return-value flow in the teaching fixture (up to 20 minutes); make and test the one-field practice variation (up to 25 minutes); use remaining time for the source walkthrough or prerequisite repair. These are ceilings within the existing block, not extra tasks.
- **10-minute evidence:** record exact command and output, assistance used, and a short request → matched method → Java value → HTTP response explanation. If the bridge is unclear, record the specific blocker and carry the quote-selection task forward. Do not change the DSA allocation or history.

Readiness questions: which path invokes `greeting()`? Which Java value produces the JSON `message` field? Why does `/missing` return an HTTP 404 while a stopped server returns no HTTP response? If any answer is unclear, repeat the reference before adapting.

Primary references: [Spring MVC `@ResponseBody` and `@RestController`](https://docs.spring.io/spring-framework/reference/web/webmvc/mvc-controller/ann-methods/responsebody.html), [Spring MVC request mappings](https://docs.spring.io/spring-framework/reference/web/webmvc/mvc-controller/ann-requestmapping.html), [Spring Boot Maven `test-run`](https://docs.spring.io/spring-boot/maven-plugin/run.html#run.test-run). The runnable example is the worked bridge; links do not substitute for observing it.
