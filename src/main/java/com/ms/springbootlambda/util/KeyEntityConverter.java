package com.ms.springbootlambda.util;

import com.ms.springbootlambda.model.KeyEntity;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.KeyUse;
import com.nimbusds.jose.jwk.RSAKey;
import lombok.AllArgsConstructor;
import net.minidev.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.KeyFactory;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

@AllArgsConstructor(onConstructor = @__(@Autowired))
@Component
public class KeyEntityConverter {
    @Value("${key.entity.algorithm:}")
    String keyEntityAlgorithm;
    final CryptoUtils cryptoUtils;

    public JSONObject getJwk(KeyEntity keyEntity) {
        try {
            X509EncodedKeySpec x509Certificate = new X509EncodedKeySpec(Base64.getDecoder().decode(keyEntity.getPublicKey()));
            KeyFactory keyFactory = KeyFactory.getInstance(keyEntityAlgorithm);
            RSAPublicKey rsaPublicKey = (RSAPublicKey) keyFactory.generatePublic(x509Certificate);
            String decryptedKey = cryptoUtils.decrypt(keyEntity.getEncPrivateKey());
            PKCS8EncodedKeySpec pkcs8EncodedKeySpec = new PKCS8EncodedKeySpec(Base64.getDecoder().decode(decryptedKey));
            RSAPrivateKey rsaPrivateKey = (RSAPrivateKey) keyFactory.generatePrivate(pkcs8EncodedKeySpec);
            return new RSAKey.Builder(rsaPublicKey).privateKey(rsaPrivateKey).keyID(keyEntity.getId()).algorithm(JWSAlgorithm.RS256).keyUse(KeyUse.SIGNATURE).build().toPublicJWK().toJSONObject();
        } catch (Exception ignore) {
        }
        return null;
    }
}
