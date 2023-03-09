package com.ms.springbootlambda.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

@Component
@Slf4j
public class CryptoUtils {
    private final byte[] key;
    private static final String KEY_LENGTH_EXCEPTION = "Key should be in 32 bytes.";
    @Value("${length.prefix:}") String lengthPrefix;
    @Value("${encrypt.delimiter:}") String encryptDelimiter;
    @Value("${cipher.transformation:}") String cipherTransformation;
    @Value("${secret.key.spec.algorithm:}") String secretKeySpecAlgorithm;
    @Value("${key.length}") int keyLength;
    @Value("${gcm.parameter.spec.tlen}") int gcmParameterSpecTlen;

    @Autowired
    public CryptoUtils(@Value("${encrypted.aes:}") String encryptedAes) {
        this.key = Base64.getDecoder().decode(encryptedAes);
    }

    public String encrypt(String originalStr) throws IllegalAccessException {
        Base64.Encoder encoder = Base64.getEncoder();
        String originalLength =lengthPrefix + originalStr;
        return encoder.encodeToString(originalLength.getBytes()) + encryptDelimiter + encoder.encodeToString(encrypt(originalStr.getBytes(), originalLength.getBytes(), key));
    }

    public byte[] encrypt(byte[] originalStr, byte[] associatedData, byte[] key) throws IllegalAccessException {
        if (key.length != keyLength)
            throw new IllegalAccessException(KEY_LENGTH_EXCEPTION);
        if (originalStr.length > 2147483646)
            throw new IllegalAccessException("originalStr exceeds memory limit");
        SecretKey secretKey = new SecretKeySpec(key, 0, keyLength, secretKeySpecAlgorithm);
        byte[] iv = new byte[12];
        SecureRandom secureRandom = new SecureRandom();
        secureRandom.nextBytes(iv);
        GCMParameterSpec gcmParameterSpec = new GCMParameterSpec(gcmParameterSpecTlen, iv);
        Cipher cipher;
        try {
            cipher = Cipher.getInstance(cipherTransformation);
            cipher.init(1, secretKey, gcmParameterSpec);
            if (associatedData != null) {
                cipher.updateAAD(associatedData);
            }
            byte[] cipherText = cipher.doFinal(originalStr);
            ByteBuffer byteBuffer = ByteBuffer.allocate(4 + iv.length + cipherText.length);
            byteBuffer.putInt(iv.length);
            byteBuffer.put(iv);
            byteBuffer.put(cipherText);
            return byteBuffer.array();
        } catch (Exception e) {
            throw new RuntimeException("Encrypt failed", e);
        }
    }

    public String decrypt(String cipherTextWithADD) {
        String[] splitCipherText = cipherTextWithADD.split(encryptDelimiter);
        Base64.Decoder decoder = Base64.getDecoder();
        return decrypt(decoder.decode(splitCipherText[1]), decoder.decode(splitCipherText[0]), key);
    }

    private String decrypt(byte[] cipherMessage, byte[] associatedData, byte[] key) throws IllegalArgumentException {
        if (key.length != keyLength) throw new IllegalArgumentException(KEY_LENGTH_EXCEPTION);
        ByteBuffer byteBuffer = ByteBuffer.wrap(cipherMessage);
        int ivLength = byteBuffer.getInt();
        if ((ivLength < 12) || (ivLength >= 16)) throw new IllegalArgumentException("Invalid iv length");
        byte[] iv = new byte[ivLength];
        byteBuffer.get(iv);
        byte[] cipherText = new byte[byteBuffer.remaining()];
        byteBuffer.get(cipherText);
        try {
            Cipher cipher = Cipher.getInstance(cipherTransformation);
            cipher.init(2, new SecretKeySpec(key, secretKeySpecAlgorithm), new GCMParameterSpec(gcmParameterSpecTlen, iv));
            if (associatedData != null) {
                cipher.updateAAD(associatedData);
            }
            byte[] plainText = cipher.doFinal(cipherText);
            return new String(plainText, StandardCharsets.UTF_8);

        } catch (Exception e) {
            throw new RuntimeException("Decrypt failed", e);
        }
    }
}
