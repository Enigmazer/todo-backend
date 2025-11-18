package com.Enigmazer.todo_app.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class KeyLoaderTest {

    private KeyLoader keyLoader;

    @BeforeEach
    void setUp() {
        keyLoader = new KeyLoader();
    }

    @Test
    void loadPublicAndPrivateKeys_ShouldThrowRuntimeException_WhenFileNotFound() {
        RuntimeException pubex = assertThrows(RuntimeException.class,
                () -> keyLoader.loadPublicKey("missing_public.pem"));
        assertTrue(pubex.getMessage().contains("Failed to load public key"));

        RuntimeException privex = assertThrows(RuntimeException.class,
                () -> keyLoader.loadPrivateKey("missing_private.pem"));
        assertTrue(privex.getMessage().contains("Failed to load private key"));
    }

    @Test
    void loadPublicAndPrivateKeys_ShouldFailGracefully_WhenInvalidPEM() throws Exception {
        // Create temporary invalid key files
        Path publicFile = Files.createTempFile("invalid", ".pub");
        Path privateFile = Files.createTempFile("invalid", ".priv");

        Files.writeString(publicFile, "-----BEGIN PUBLIC KEY-----\nINVALID\n-----END PUBLIC KEY-----");
        Files.writeString(privateFile, "-----BEGIN PRIVATE KEY-----\nINVALID\n-----END PRIVATE KEY-----");

        // Because the PEM is not valid Base64, decoding should fail
        assertThrows(RuntimeException.class, () -> keyLoader.loadPublicKey(publicFile.getFileName().toString()));
        assertThrows(RuntimeException.class, () -> keyLoader.loadPrivateKey(privateFile.getFileName().toString()));
    }

    @Test
    void loadKeyPair_ShouldThrow_WhenEitherKeyFails() {
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> keyLoader.loadKeyPair("fake_public.pem", "fake_private.pem"));
        assertTrue(ex.getMessage().contains("Failed to load public key"));
    }
}
