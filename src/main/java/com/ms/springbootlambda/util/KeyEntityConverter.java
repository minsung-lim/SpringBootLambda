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

import static java.util.Base64.getDecoder;

@AllArgsConstructor(onConstructor = @__(@Autowired))
@Component
public class KeyEntityConverter {
    @Value("${key.entity.algorithm:}")
    String keyEntityAlgorithm;
    final CryptoUtils cryptoUtils;

    public JSONObject getJwk(KeyEntity keyEntity) {
        try {
            KeyFactory keyFactory = KeyFactory.getInstance(keyEntityAlgorithm);
            RSAPublicKey rsaPublicKey = (RSAPublicKey) keyFactory.generatePublic(new X509EncodedKeySpec(getDecoder().decode(keyEntity.getPublicKey())));
            RSAPrivateKey rsaPrivateKey = (RSAPrivateKey) keyFactory.generatePrivate(new PKCS8EncodedKeySpec(getDecoder().decode(cryptoUtils.decrypt(keyEntity.getEncPrivateKey()))));
            return new RSAKey.Builder(rsaPublicKey).privateKey(rsaPrivateKey).keyID(keyEntity.getId()).algorithm(JWSAlgorithm.RS256).keyUse(KeyUse.SIGNATURE).build().toPublicJWK().toJSONObject();
        } catch (Exception ignore) {
        }
        return null;
    }
}
