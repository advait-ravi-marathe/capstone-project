package assignments.reboot;

import java.io.IOException;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

/** Small real-HTTP browser-session helper for the Day 21 test runtime. */
final class Day21HttpTestSession {
    private final int port;
    private final CookieManager cookies = new CookieManager(null, CookiePolicy.ACCEPT_ALL);
    private final HttpClient client = HttpClient.newBuilder().cookieHandler(cookies)
            .followRedirects(HttpClient.Redirect.NEVER).build();
    private String csrfToken;

    private Day21HttpTestSession(int port) { this.port = port; }

    static Day21HttpTestSession anonymous(int port) throws Exception {
        var session = new Day21HttpTestSession(port);
        session.refreshCsrfCookie();
        return session;
    }

    static Day21HttpTestSession login(int port, String username) throws Exception {
        var session = new Day21HttpTestSession(port);
        session.refreshCsrfCookie();
        var form = "username=" + encode(username) + "&password=" + encode("test-password");
        var login = HttpRequest.newBuilder(session.uri("/login"))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .header("X-XSRF-TOKEN", session.csrfToken)
                .POST(HttpRequest.BodyPublishers.ofString(form)).build();
        HttpResponse<String> result = session.client.send(login, HttpResponse.BodyHandlers.ofString());
        if (result.statusCode() != 302) {
            throw new AssertionError("Expected test form-login redirect, got " + result.statusCode());
        }
        // Authentication success clears the previous CSRF token; fetch the newly issued cookie.
        session.refreshCsrfCookie();
        return session;
    }

    HttpResponse<String> csrfWrite(String suppliedHeader) throws IOException, InterruptedException {
        HttpRequest.Builder request = HttpRequest.newBuilder(uri("/test/day21/write"))
                .POST(HttpRequest.BodyPublishers.noBody());
        if (suppliedHeader != null) request.header("X-XSRF-TOKEN", suppliedHeader);
        return client.send(request.build(), HttpResponse.BodyHandlers.ofString());
    }

    HttpResponse<String> ownerRead(String journeyId, String requestOwnerId) throws IOException, InterruptedException {
        var form = "ownerId=" + encode(requestOwnerId);
        var request = HttpRequest.newBuilder(uri("/test/day21/journeys/" + encodePath(journeyId) + "/read"))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .header("X-XSRF-TOKEN", csrfToken)
                .POST(HttpRequest.BodyPublishers.ofString(form)).build();
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    String csrfToken() { return csrfToken; }

    private void refreshCsrfCookie() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder(uri("/test/day21/csrf")).GET().build();
        HttpResponse<String> result = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (result.statusCode() != 204) throw new AssertionError("CSRF bootstrap did not return 204");
        Optional<String> value = cookies.getCookieStore().getCookies().stream()
                .filter(cookie -> cookie.getName().equals("XSRF-TOKEN"))
                .map(java.net.HttpCookie::getValue).findFirst();
        csrfToken = value.orElseThrow(() -> new AssertionError("Server did not issue XSRF-TOKEN cookie"));
        if (csrfToken.isBlank()) throw new AssertionError("Server issued an empty XSRF-TOKEN");
    }

    private URI uri(String path) { return URI.create("http://127.0.0.1:" + port + path); }
    private static String encode(String value) { return URLEncoder.encode(value, StandardCharsets.UTF_8); }
    private static String encodePath(String value) { return encode(value).replace("+", "%20"); }
}
