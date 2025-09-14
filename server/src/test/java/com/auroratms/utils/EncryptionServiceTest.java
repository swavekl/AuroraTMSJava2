package com.auroratms.utils;

import com.auroratms.AbstractServiceTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Transactional
public class EncryptionServiceTest extends AbstractServiceTest {

    @Autowired
    private EncryptionService encryptionService;

    @Test
    public void testEncryptionAndDecryption() {
        String plainText = "my super secret text";
        String encryptText = this.encryptionService.encryptText(plainText);
        assertTrue(encryptText.startsWith("vault:v1:"), "Encrypted text is not right");

        String decryptText = this.encryptionService.decryptText(encryptText);
        assertEquals(plainText, decryptText, "wrong text");
    }
}
