package com.auroratms.utils;

import com.auroratms.AbstractServiceTest;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import jakarta.mail.MessagingException;
import java.util.HashMap;
import java.util.Map;

public class EmailServiceTest extends AbstractServiceTest {

    @Autowired
    private EmailService emailService;

    @Test
    @Disabled
    public void sendTestEmail () {
        this.emailService.sendEmail("swaveklorenc@yahoo.com", "Silly subject", "Simple Body of email");
    }

    @Test
    @Disabled
    public void sendHtmlEmail () throws MessagingException {
        Map<String, Object> templateModel = new HashMap<>();
        templateModel.put("recipientName", "Swavek Lorenc");
        templateModel.put("text", "This confirms your registration for the Aurora Cup tournament");
        templateModel.put ("senderName", "Slawomir Lorenc");
        String templateName = "registration-complete-template.html";
        this.emailService.sendMessageUsingThymeleafTemplate(
                "swaveklorenc@yahoo.com", null, "Thank you for registering",
                templateName, templateModel);
    }

    @Test
    @Disabled
    public void sentRegistrationEmail () throws MessagingException {
        Map<String, Object> templateModel = new HashMap<>();
        templateModel.put("playerName", "Julia");
        templateModel.put("tournamentName", "2022 Aurora Cup");
        templateModel.put("tournamentDirectorName", "Swavek Lorenc");
        templateModel.put("tournamentDirectorEmail", "swavek@gmail.com");
        templateModel.put("tournamentDirectorPhone", "630-888-9999");
        String templateName = "tournament-entry-started.html";
        this.emailService.sendMessageUsingThymeleafTemplate(
                "swaveklorenc@yahoo.com", null, "Thank you for registering",
                templateName, templateModel);
    }

    @Test
    public void sendPaymentEmail () throws MessagingException {
        Map<String, Object> templateModel = new HashMap<>();
        templateModel.put("playerFirstName", "Julia");
        templateModel.put("playerLastName", "Lorenc");
        templateModel.put("tournamentName", "2022 Aurora Cup");
        templateModel.put("tournamentDirectorName", "Swavek Lorenc");
        templateModel.put("tournamentDirectorEmail", "swavek@gmail.com");
        templateModel.put("tournamentDirectorPhone", "630-888-9999");
        Double amount = (double)12545L / 100;
        templateModel.put("amount", amount);
        templateModel.put("currency", "USD");

        String templateName = "tournament-payment-confirmation.html";
        this.emailService.sendMessageUsingThymeleafTemplate(
                "swaveklorenc@yahoo.com", null, "Tournament Payment Received",
                templateName, templateModel);
    }
}
