package com.vcs.flowpilot.action.http.service;

import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;
import java.util.Base64;

@Service
public class EncryptionService {
    private static final String ALG = "AES";
    private static final String TRANS = "AES/GCM/NoPadding";
    private static final int IV_LEN = 12;      // 96-bit IV
    private static final int TAG_BITS = 128;   // 128-bit tag
    private final SecretKey key;
    private final SecureRandom rnd = new SecureRandom();

    public EncryptionService(byte[] rawKey) { this.key = new SecretKeySpec(rawKey, ALG); }

    public String enc(String plain) {
        if (plain == null) return null;
        byte[] iv = new byte[IV_LEN]; rnd.nextBytes(iv);
        try {
            Cipher c = Cipher.getInstance(TRANS);
            c.init(Cipher.ENCRYPT_MODE, key, new GCMParameterSpec(TAG_BITS, iv));
            byte[] ct = c.doFinal(plain.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            byte[] out = new byte[iv.length + ct.length];
            System.arraycopy(iv, 0, out, 0, iv.length);
            System.arraycopy(ct, 0, out, iv.length, ct.length);
            return Base64.getEncoder().encodeToString(out);
        } catch (Exception e) { throw new IllegalStateException("encrypt failed", e); }
    }

    public String dec(String encoded) {
        if (encoded == null) return null;
        byte[] in = Base64.getDecoder().decode(encoded);
        byte[] iv = java.util.Arrays.copyOfRange(in, 0, IV_LEN);
        byte[] ct = java.util.Arrays.copyOfRange(in, IV_LEN, in.length);
        try {
            Cipher c = Cipher.getInstance(TRANS);
            c.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(TAG_BITS, iv));
            byte[] pt = c.doFinal(ct);
            return new String(pt, java.nio.charset.StandardCharsets.UTF_8);
        } catch (Exception e) { throw new IllegalStateException("decrypt failed", e); }
    }
}
