package coverstory.modules.analytics;

import coverstory.modules.analytics.api.AnalyticsScopeHashPort;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

/** Versioned, purpose-separated HMAC pseudonyms. The key is supplied by deployment configuration. */
public final class HmacAnalyticsScopeHasher implements AnalyticsScopeHashPort {
    private final byte[] key;
    public HmacAnalyticsScopeHasher(String base64Key) {
        key = Base64.getDecoder().decode(base64Key);
        if (key.length != 32) throw new IllegalArgumentException("analytics HMAC key must be 32 bytes");
    }
    @Override public byte[] hashOwner(String ownerId) { return hash("owner-v1", ownerId); }
    @Override public byte[] hashSession(String sessionId) { return hash("session-v1", sessionId); }
    private byte[] hash(String purpose, String value) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(key, "HmacSHA256"));
            return mac.doFinal(("coverstory:analytics:" + purpose + ":" + value).getBytes(java.nio.charset.StandardCharsets.UTF_8));
        } catch (java.security.GeneralSecurityException unavailable) {
            throw new IllegalStateException("analytics scope hashing unavailable");
        }
    }
}
