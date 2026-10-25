package assignments.oidc;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import tools.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.interfaces.RSAPublicKey;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/** Loopback-only synthetic OIDC issuer with authorization-code exchange for the Spring client flow. */
final class LocalOidcTestProvider implements AutoCloseable {
    private static final ObjectMapper JSON = new ObjectMapper();
    private final HttpServer server;
    private final Map<String, KeyPair> keys = new ConcurrentHashMap<>();
    private record AuthorizationCode(String clientId, String redirectUri, String nonce) { }
    private final Map<String, AuthorizationCode> codes = new ConcurrentHashMap<>();
    private final AtomicLong nextCode = new AtomicLong();
    private volatile String currentKid = "lab-key-1";
    private volatile boolean mismatchNextNonce;
    private final String issuer;

    private LocalOidcTestProvider() throws Exception {
        keys.put(currentKid, newKeyPair());
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        int port = server.getAddress().getPort();
        issuer = "http://127.0.0.1:" + port + "/issuer";
        server.createContext("/issuer/.well-known/openid-configuration", this::discovery);
        server.createContext("/issuer/jwks", this::jwks);
        server.createContext("/issuer/authorize", this::authorize);
        server.createContext("/issuer/token", this::token);
        server.start();
    }

    static LocalOidcTestProvider start() {
        try { return new LocalOidcTestProvider(); }
        catch (Exception exception) { throw new IllegalStateException("Unable to start loopback OIDC fixture", exception); }
    }

    String issuer() { return issuer; }
    void mismatchNextNonce() { mismatchNextNonce = true; }

    synchronized String issue(String tenant, String audience, String scope, String kid, String tokenIssuer,
                              Instant issuedAt, Instant expiresAt) throws Exception {
        return issueAs(tenant, audience, scope, kid, kid, tokenIssuer, issuedAt, expiresAt);
    }

    synchronized String issueWithUnknownKid(String tenant) throws Exception {
        Instant now = Instant.now();
        return issueAs(tenant, "coverstory-oidc-lab", "policy.read", currentKid, "unknown-kid",
                issuer, now.minusSeconds(1), now.plusSeconds(60));
    }

    synchronized String validTokenWithScope(String tenant, String scope) throws Exception {
        Instant now = Instant.now();
        return issue(tenant, "coverstory-oidc-lab", scope, currentKid, issuer, now.minusSeconds(1), now.plusSeconds(60));
    }

    synchronized String tokenWithoutAudience(String tenant) throws Exception {
        Instant now = Instant.now();
        Map<String, Object> claims = new LinkedHashMap<>();
        claims.put("iss", issuer); claims.put("sub", "synthetic-advait"); claims.put("tenant_id", tenant);
        claims.put("scope", "policy.read"); claims.put("iat", now.minusSeconds(1).getEpochSecond());
        claims.put("exp", now.plusSeconds(60).getEpochSecond());
        return signClaims(claims, currentKid);
    }

    synchronized String tokenWithInvalidSignature() throws Exception {
        String valid = validToken("tenant-a");
        int signatureStart = valid.lastIndexOf('.') + 1;
        String signature = valid.substring(signatureStart);
        char replacement = signature.charAt(0) == 'A' ? 'B' : 'A';
        return valid.substring(0, signatureStart) + replacement + signature.substring(1);
    }

    synchronized String unsignedToken(String tenant) throws Exception {
        Instant now = Instant.now();
        Map<String, Object> header = Map.of("alg", "none", "typ", "JWT");
        Map<String, Object> claims = Map.of("iss", issuer, "sub", "synthetic-advait", "aud", List.of("coverstory-oidc-lab"),
                "tenant_id", tenant, "scope", "policy.read", "iat", now.minusSeconds(1).getEpochSecond(),
                "exp", now.plusSeconds(60).getEpochSecond());
        return base64(JSON.writeValueAsBytes(header)) + "." + base64(JSON.writeValueAsBytes(claims)) + ".";
    }

    private String issueAs(String tenant, String audience, String scope, String signingKid, String headerKid,
                           String tokenIssuer, Instant issuedAt, Instant expiresAt) throws Exception {
        KeyPair key = keys.get(signingKid);
        if (key == null) throw new IllegalArgumentException("Unknown local signing key " + signingKid);
        Map<String, Object> header = Map.of("alg", "RS256", "typ", "JWT", "kid", headerKid);
        Map<String, Object> claims = new LinkedHashMap<>();
        claims.put("iss", tokenIssuer); claims.put("sub", "synthetic-advait");
        claims.put("aud", List.of(audience)); claims.put("tenant_id", tenant); claims.put("scope", scope);
        claims.put("iat", issuedAt.getEpochSecond()); claims.put("exp", expiresAt.getEpochSecond());
        return signClaims(claims, signingKid, headerKid);
    }

    private String signClaims(Map<String, Object> claims, String kid) throws Exception { return signClaims(claims, kid, kid); }
    private String signClaims(Map<String, Object> claims, String signingKid, String headerKid) throws Exception {
        KeyPair key = keys.get(signingKid);
        Map<String, Object> header = Map.of("alg", "RS256", "typ", "JWT", "kid", headerKid);
        String signingInput = base64(JSON.writeValueAsBytes(header)) + "." + base64(JSON.writeValueAsBytes(claims));
        Signature signer = Signature.getInstance("SHA256withRSA");
        signer.initSign(key.getPrivate()); signer.update(signingInput.getBytes(StandardCharsets.US_ASCII));
        return signingInput + "." + Base64.getUrlEncoder().withoutPadding().encodeToString(signer.sign());
    }

    synchronized String currentKid() { return currentKid; }

    synchronized String rotateAndIssue(String tenant) throws Exception {
        String nextKid = "lab-key-" + (keys.size() + 1);
        keys.put(nextKid, newKeyPair());
        currentKid = nextKid;
        Instant now = Instant.now();
        return issue(tenant, "coverstory-oidc-lab", "policy.read", nextKid, issuer,
                now.minusSeconds(2), now.plusSeconds(300));
    }

    synchronized String validToken(String tenant) throws Exception {
        Instant now = Instant.now();
        return issue(tenant, "coverstory-oidc-lab", "policy.read", currentKid, issuer,
                now.minusSeconds(2), now.plusSeconds(300));
    }

    private void discovery(HttpExchange exchange) throws IOException {
        write(exchange, 200, Map.of("issuer", issuer, "jwks_uri", issuer + "/jwks",
                "authorization_endpoint", issuer + "/authorize", "token_endpoint", issuer + "/token",
                "response_types_supported", List.of("code"), "subject_types_supported", List.of("public"),
                "id_token_signing_alg_values_supported", List.of("RS256")));
    }

    private synchronized void jwks(HttpExchange exchange) throws IOException {
        List<Map<String, Object>> publicKeys = new ArrayList<>();
        for (var entry : keys.entrySet()) publicKeys.add(jwk(entry.getKey(), (RSAPublicKey) entry.getValue().getPublic()));
        write(exchange, 200, Map.of("keys", publicKeys));
    }

    private void authorize(HttpExchange exchange) throws IOException {
        Map<String, String> query = form(exchange.getRequestURI().getRawQuery());
        String client = query.get("client_id"), redirect = query.get("redirect_uri");
        String state = query.get("state"), nonce = query.get("nonce");
        if (!"GET".equals(exchange.getRequestMethod()) || !"code".equals(query.get("response_type"))
                || !"coverstory-login-lab".equals(client) || redirect == null || state == null || state.isBlank()
                || nonce == null || nonce.isBlank() || !isAllowedRedirect(redirect)) {
            write(exchange, 400, Map.of("error", "invalid_request"));
            return;
        }
        String code = "synthetic-code-" + nextCode.incrementAndGet();
        codes.put(code, new AuthorizationCode(client, redirect, nonce));
        exchange.getResponseHeaders().set("Cache-Control", "no-store");
        exchange.getResponseHeaders().set("Location", redirect + "?code=" + code + "&state=" + encode(state));
        exchange.sendResponseHeaders(302, -1);
        exchange.close();
    }

    private synchronized void token(HttpExchange exchange) throws IOException {
        if (!"POST".equals(exchange.getRequestMethod())) { write(exchange, 405, Map.of("error", "invalid_request")); return; }
        Map<String, String> form = form(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
        String authorization = exchange.getRequestHeaders().getFirst("Authorization");
        String basic = authorization != null && authorization.startsWith("Basic ")
                ? new String(Base64.getDecoder().decode(authorization.substring(6)), StandardCharsets.UTF_8) : "";
        String codeValue = form.get("code");
        String redirect = form.get("redirect_uri");
        if (!"authorization_code".equals(form.get("grant_type"))
                || !"coverstory-login-lab:synthetic-secret".equals(basic) || codeValue == null) {
            write(exchange, 400, Map.of("error", "invalid_grant"));
            return;
        }
        AuthorizationCode code = codes.remove(codeValue);
        if (code == null || !code.clientId().equals("coverstory-login-lab") || !code.redirectUri().equals(redirect)) {
            write(exchange, 400, Map.of("error", "invalid_grant"));
            return;
        }
        Instant now = Instant.now();
        String idToken;
        String nonce = mismatchNextNonce ? code.nonce() + "-wrong" : code.nonce();
        mismatchNextNonce = false;
        try { idToken = issueOidcIdToken(code.clientId(), nonce, now.minusSeconds(1), now.plusSeconds(300)); }
        catch (Exception failure) { write(exchange, 500, Map.of("error", "server_error")); return; }
        write(exchange, 200, Map.of("access_token", "synthetic-access-token", "token_type", "Bearer",
                "expires_in", 300, "scope", "openid profile", "id_token", idToken));
    }

    private String issueOidcIdToken(String audience, String nonce, Instant issuedAt, Instant expiresAt) throws Exception {
        KeyPair key = keys.get(currentKid);
        Map<String, Object> header = Map.of("alg", "RS256", "typ", "JWT", "kid", currentKid);
        Map<String, Object> claims = new LinkedHashMap<>();
        claims.put("iss", issuer); claims.put("sub", "synthetic-advait"); claims.put("aud", audience);
        claims.put("iat", issuedAt.getEpochSecond()); claims.put("exp", expiresAt.getEpochSecond());
        claims.put("nonce", nonce); claims.put("name", "Synthetic Learner");
        String signingInput = base64(JSON.writeValueAsBytes(header)) + "." + base64(JSON.writeValueAsBytes(claims));
        Signature signer = Signature.getInstance("SHA256withRSA");
        signer.initSign(key.getPrivate()); signer.update(signingInput.getBytes(StandardCharsets.US_ASCII));
        return signingInput + "." + Base64.getUrlEncoder().withoutPadding().encodeToString(signer.sign());
    }

    private static Map<String, String> form(String raw) {
        Map<String, String> result = new HashMap<>();
        if (raw == null || raw.isEmpty()) return result;
        for (String pair : raw.split("&")) {
            String[] parts = pair.split("=", 2);
            result.put(decode(parts[0]), parts.length == 2 ? decode(parts[1]) : "");
        }
        return result;
    }

    private static boolean isAllowedRedirect(String value) {
        try {
            URI uri = URI.create(value);
            return "http".equals(uri.getScheme()) && "localhost".equals(uri.getHost())
                    && uri.getPath() != null && uri.getPath().startsWith("/login/oauth2/code/");
        } catch (RuntimeException invalid) { return false; }
    }

    private static String decode(String value) { return URLDecoder.decode(value, StandardCharsets.UTF_8); }
    private static String encode(String value) { return java.net.URLEncoder.encode(value, StandardCharsets.UTF_8); }

    private static Map<String, Object> jwk(String kid, RSAPublicKey key) {
        return Map.of("kty", "RSA", "use", "sig", "alg", "RS256", "kid", kid,
                "n", unsignedBase64(key.getModulus().toByteArray()), "e", unsignedBase64(key.getPublicExponent().toByteArray()));
    }

    private static void write(HttpExchange exchange, int status, Object body) throws IOException {
        byte[] bytes = JSON.writeValueAsBytes(body);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        exchange.getResponseHeaders().set("Cache-Control", "no-store");
        exchange.sendResponseHeaders(status, bytes.length);
        try (var output = exchange.getResponseBody()) { output.write(bytes); }
    }

    private static KeyPair newKeyPair() throws GeneralSecurityException {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA"); generator.initialize(2048); return generator.generateKeyPair();
    }
    private static String unsignedBase64(byte[] bytes) {
        int offset = bytes.length > 1 && bytes[0] == 0 ? 1 : 0;
        return Base64.getUrlEncoder().withoutPadding().encodeToString(Arrays.copyOfRange(bytes, offset, bytes.length));
    }
    private static String base64(byte[] bytes) { return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes); }
    @Override public void close() { server.stop(0); }
}
