package coverstory.platform.crypto;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Objects;

/** AES-GCM implementation; key creation, persistence, and rotation belong to the key provider. */
public final class AesGcmFieldCipher implements FieldCipher {
    private static final String TRANSFORMATION = "AES/GCM/NoPadding";
    private static final int TAG_BITS = 128;
    private static final int NONCE_BYTES = 12;

    private final EncryptionKeyProvider keys;
    private final SecureRandom random;

    public AesGcmFieldCipher(EncryptionKeyProvider keys) {
        this(keys, new SecureRandom());
    }

    AesGcmFieldCipher(EncryptionKeyProvider keys, SecureRandom random) {
        this.keys = Objects.requireNonNull(keys, "keys");
        this.random = Objects.requireNonNull(random, "random");
    }

    @Override
    public EncryptedField encrypt(String plaintext, byte[] associatedData) throws GeneralSecurityException {
        Objects.requireNonNull(plaintext, "plaintext");
        Objects.requireNonNull(associatedData, "associatedData");
        EncryptionKey key = Objects.requireNonNull(keys.activeKey(), "active encryption key");
        byte[] nonce = new byte[NONCE_BYTES];
        random.nextBytes(nonce);
        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        cipher.init(Cipher.ENCRYPT_MODE, key.key(), new GCMParameterSpec(TAG_BITS, nonce));
        cipher.updateAAD(associatedData);
        return new EncryptedField(1, key.keyId(), nonce, cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8)));
    }

    @Override
    public String decrypt(EncryptedField encrypted, byte[] associatedData) throws GeneralSecurityException {
        Objects.requireNonNull(encrypted, "encrypted");
        Objects.requireNonNull(associatedData, "associatedData");
        EncryptionKey key = keys.findById(encrypted.keyId())
                .orElseThrow(() -> new GeneralSecurityException("Encryption key is unavailable"));
        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        cipher.init(Cipher.DECRYPT_MODE, key.key(), new GCMParameterSpec(TAG_BITS, encrypted.nonce()));
        cipher.updateAAD(associatedData);
        return new String(cipher.doFinal(encrypted.ciphertext()), StandardCharsets.UTF_8);
    }
}
