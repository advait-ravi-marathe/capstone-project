package coverstory.platform.crypto;

import java.util.Objects;

/** Versioned authenticated ciphertext; byte arrays are copied on ingress and egress. */
public record EncryptedField(int formatVersion, String keyId, byte[] nonce, byte[] ciphertext) {
    public EncryptedField {
        if (formatVersion != 1) throw new IllegalArgumentException("Unsupported ciphertext format");
        Objects.requireNonNull(keyId, "keyId");
        Objects.requireNonNull(nonce, "nonce");
        Objects.requireNonNull(ciphertext, "ciphertext");
        if (keyId.isBlank() || nonce.length != 12 || ciphertext.length < 16) {
            throw new IllegalArgumentException("Malformed encrypted field");
        }
        nonce = nonce.clone();
        ciphertext = ciphertext.clone();
    }

    @Override public byte[] nonce() { return nonce.clone(); }
    @Override public byte[] ciphertext() { return ciphertext.clone(); }
}
