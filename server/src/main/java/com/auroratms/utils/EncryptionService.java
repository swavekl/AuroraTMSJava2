package com.auroratms.utils;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.vault.core.VaultOperations;
import org.springframework.vault.core.VaultTransitOperations;

/**
 * Encryption service for encrypting and decrypting sensitive values at rest (in storage)
 */
@Service
public class EncryptionService {

    // autowired via constructor
    private VaultOperations vaultOperations;

    @Value("${transit-key-name}")
    private String keyName;

    public EncryptionService(VaultOperations vaultOperations) {
        this.vaultOperations = vaultOperations;
    }

    /**
     *
     * @param plainText
     * @return
     */
    public String encryptText(String plainText) {
        VaultTransitOperations transitOperations = vaultOperations.opsForTransit();
        return transitOperations.encrypt(keyName, plainText);
    }

    /**
     *
     * @param ciphertext
     * @return
     */
    public String decryptText(String ciphertext) {
        VaultTransitOperations transitOperations = vaultOperations.opsForTransit();
        return transitOperations.decrypt(keyName, ciphertext);
    }
}
