package assignments.oidc;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import tools.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.interfaces.RSAPublicKey;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/** Loopback-only test issuer implementing discovery and JWKS; all keys and claims are synthetic. */
final class LocalOidcTestProvider implements AutoCloseable {
    private static final ObjectMapper JSON = new ObjectMapper();
    private final HttpServer server;
    private final Map<String, KeyPair> keys = new ConcurrentHashMap<>();
    private volatile String currentKid = "lab-key-1";
    private final String issuer;

    private LocalOidcTestProvider() throws Exception {
        keys.put(currentKid, newKeyPair());
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        int port = server.getAddress().getPort();
        issuer = "http://127.0.0.1:" + port + "/issuer";
        server.createContext("/issuer/.well-known/openid-configuration", this::discovery);
        server.createContext("/issuer/jwks", this::jwks);
        server.start();
    }

    static LocalOidcTestProvider start() {
        try { return new LocalOidcTestProvider(); }
        catch (Exception exception) { throw new IllegalStateException("Unable to start loopback OIDC fixture", exception); }
    }

    String issuer() { return issuer; }

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

    private String issueAs(String tenant, String audience, String scope, String signingKid, String headerKid,
                           String tokenIssuer, Instant issuedAt, Instant expiresAt) throws Exception {
        KeyPair key = keys.get(signingKid);
        if (key == null) throw new IllegalArgumentException("Unknown local signing key " + signingKid);
        Map<String, Object> header = Map.of("alg", "RS256", "typ", "JWT", "kid", headerKid);
        Map<String, Object> claims = new LinkedHashMap<>();
        claims.put("iss", tokenIssuer); claims.put("sub", "synthetic-advait");
        claims.put("aud", List.of(audience)); claims.put("tenant_id", tenant); claims.put("scope", scope);
        claims.put("iat", issuedAt.getEpochSecond()); claims.put("exp", expiresAt.getEpochSecond());
        String first = base64(JSON.writeValueAsBytes(header));
        String second = base64(JSON.writeValueAsBytes(claims));
        String signingInput = first + "." + second;
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
