package com.auroratms.utils;

import com.auroratms.server.ServerApplication;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithSecurityContextTestExecutionListener;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;
import org.springframework.test.context.web.ServletTestExecutionListener;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = {ServerApplication.class})
@ContextConfiguration
@WebAppConfiguration(value = "src/test/resources")
@TestExecutionListeners(listeners={ServletTestExecutionListener.class,
        DependencyInjectionTestExecutionListener.class,
        DirtiesContextTestExecutionListener.class,
        TransactionalTestExecutionListener.class,
        WithSecurityContextTestExecutionListener.class})
@Transactional
public class EncryptionServiceTest extends AbstractJUnit4SpringContextTests {

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
