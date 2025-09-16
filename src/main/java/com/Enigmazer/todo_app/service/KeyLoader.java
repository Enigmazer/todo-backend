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

/**
 * Component responsible for loading cryptographic keys from the filesystem or classpath.
 * Supports loading both public and private RSA keys in PEM format.
 * <p>
 * The loader first attempts to load keys from the Render platform's secret files directory,
 * and falls back to the classpath resources if running locally.
 */
@Component
public class KeyLoader {

    /**
     * Loads a key file from either the Render platform's secret directory or classpath.
     *
     * @param fileName the name of the key file to load
     * @return an input stream for reading the key file
     * @throws FileNotFoundException if the key file cannot be found in either location
     * @throws IOException if an I/O error occurs while reading the file
     */
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

    /**
     * Loads an RSA public key from a file in PEM format.
     *
     * @param filename the name of the file containing the public key
     * @return the loaded PublicKey
     * @throws RuntimeException if the key cannot be loaded or parsed
     */
    public PublicKey loadPublicKey(String filename) {
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

    /**
     * Loads an RSA private key from a file in PKCS#8 PEM format.
     *
     * @param filename the name of the file containing the private key
     * @return the loaded PrivateKey
     * @throws RuntimeException if the key cannot be loaded or parsed
     */
    public PrivateKey loadPrivateKey(String filename) {
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

    /**
     * Loads a key pair consisting of a public key and a private key.
     *
     * @param publicKeyFile the name of the file containing the public key
     * @param privateKeyFile the name of the file containing the private key
     * @return a KeyPair containing both the public and private keys
     * @throws RuntimeException if either key cannot be loaded
     */
    public KeyPair loadKeyPair(String publicKeyFile, String privateKeyFile) {
        System.out.println("Trying to load keys: " + publicKeyFile + ", " + privateKeyFile);
        return new KeyPair(loadPublicKey(publicKeyFile), loadPrivateKey(privateKeyFile));
    }
}
