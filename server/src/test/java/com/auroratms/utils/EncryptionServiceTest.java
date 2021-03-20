package com.auroratms.utils;

import com.auroratms.AbstractServiceTest;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@Transactional
public class EncryptionServiceTest extends AbstractServiceTest {

    @Autowired
    private EncryptionService encryptionService;

    @Test
    public void testEncryptionAndDecryption() {
        String plainText = "my super secret text";
        String encryptText = this.encryptionService.encryptText(plainText);
        assertTrue("Encrypted text is not right", encryptText.startsWith("vault:v1:"));

        String decryptText = this.encryptionService.decryptText(encryptText);
        assertEquals("wrong text", plainText, decryptText);
    }
}
