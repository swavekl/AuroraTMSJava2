package com.auroratms.utils;

import com.auroratms.AbstractServiceTest;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class EmailServiceTest extends AbstractServiceTest {

    @Autowired
    private EmailService emailService;

    @Test
    public void sendTestEmail () {
        this.emailService.sendEmail("swaveklorenc@yahoo.com", "Silly subject", "Simple Body of email");
    }
}
