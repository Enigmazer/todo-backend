package com.Enigmazer.todo_app.service;

import org.springframework.stereotype.Component;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

@Component
public class KeyLoader {

    private InputStream loadKeyFile(String fileName) throws IOException {
        // Try to load from Render's secret files directory
        String renderSecretPath = "/etc/secrets/" + fileName;
        Path path = Path.of(renderSecretPath);
        if (Files.exists(path)) {
            return Files.newInputStream(path);
        }

        // Fallback: try to load from resources in the JAR (local dev)
        InputStream is = getClass().getClassLoader().getResourceAsStream("keys/" + fileName);
        if (is != null) {
            return is;
        }

        // If neither works → throw error
        throw new FileNotFoundException("Key file not found: " + fileName);
    }

    public PublicKey loadPublicKey(String filename){
        try {
            InputStream is = loadKeyFile(filename);
            String key = new String(is.readAllBytes(), StandardCharsets.UTF_8)
                    .replace("-----BEGIN PUBLIC KEY-----", "")
                    .replace("-----END PUBLIC KEY-----", "")
                    .replaceAll("\\s", "");

            byte[] keyBytes = Base64.getDecoder().decode(key);

            X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
            return KeyFactory.getInstance("RSA").generatePublic(spec);
        }catch (Exception e){
            throw new RuntimeException("Failed to load public key");
        }
    }

    public PrivateKey loadPrivateKey(String filename){
        try {
            InputStream is = loadKeyFile(filename);
            String key = new String(is.readAllBytes(), StandardCharsets.UTF_8)
                    .replace("-----BEGIN PRIVATE KEY-----", "")
                    .replace("-----END PRIVATE KEY-----", "")
                    .replaceAll("\\s", "");

            byte[] keyBytes = Base64.getDecoder().decode(key);

            PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
            return KeyFactory.getInstance("RSA").generatePrivate(spec);
        }catch (Exception e){
            throw new RuntimeException("Failed to load private key");
        }
    }

    public KeyPair loadKeyPair(String publicKeyFile, String privateKeyFile){
        System.out.println("Trying to load keys: " + publicKeyFile + ", " + privateKeyFile);
        return new KeyPair(loadPublicKey(publicKeyFile), loadPrivateKey(privateKeyFile));
    }
}
