package com.auroratms.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring5.SpringTemplateEngine;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.io.File;
import java.util.List;
import java.util.Map;

/**
 * Basic email service using one of the SMTP providers
 */
@Service
@Slf4j
public class EmailService {

    // Spring's email service
    @Autowired
    private JavaMailSender javaMailSender;

    // template engine used to format the body of the email
    @Autowired
    private SpringTemplateEngine thymeleafTemplateEngine;

    // address of where the email is coming from
    @Value("${spring.mail.username}")
    private String fromAddress;

    /**
     *
     * @param toAddress
     * @param subject
     * @param emailBody
     */
    public void sendEmail(String toAddress, String subject, String emailBody) {
        SimpleMailMessage simpleMailMessage = new SimpleMailMessage();
        simpleMailMessage.setFrom(fromAddress);
        simpleMailMessage.setTo(toAddress);
        simpleMailMessage.setSubject(subject);
        simpleMailMessage.setText(emailBody);
        this.javaMailSender.send(simpleMailMessage);
    }

    /**
     *
     * @param toAddress
     * @param ccAddress
     * @param subject
     * @param htmlBody
     * @throws MessagingException
     */
    private void sendHtmlEmail(String toAddress, String ccAddress, String subject, String htmlBody) throws MessagingException {
        MimeMessage message = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        helper.setTo(toAddress);
        if (ccAddress != null) {
            helper.setCc(ccAddress);
        }
        helper.setSubject(subject);
        helper.setText(htmlBody, true);
        javaMailSender.send(message);
    }

    /**
     *
     * @param toAddress
     * @param ccAddress
     * @param subject
     * @param templateName
     * @param templateModel
     * @throws MessagingException
     */
    public void sendMessageUsingThymeleafTemplate(String toAddress, String ccAddress, String subject, String templateName, Map<String, Object> templateModel)
            throws MessagingException {

        Context thymeleafContext = new Context();
        thymeleafContext.setVariables(templateModel);
        String htmlBody = thymeleafTemplateEngine.process(templateName, thymeleafContext);

        sendHtmlEmail(toAddress, ccAddress, subject, htmlBody);
    }

    /**
     * Sends email in HTML format with attachments
     * @param toAddress
     * @param ccAddress
     * @param subject
     * @param templateName
     * @param templateModel
     * @param attachmentFilenames
     * @throws MessagingException
     */
    public void sendHtmlMessageUsingThymeleafTemplateWithAttachments(String toAddress,
                                                                     String ccAddress,
                                                                     String subject,
                                                                     String templateName,
                                                                     Map<String, Object> templateModel,
                                                                     List<String> attachmentFilenames)
            throws MessagingException {
        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(toAddress);
            if (ccAddress != null) {
                helper.setCc(ccAddress);
            }
            helper.setSubject(subject);

            // prepare the HTML email body
            Context thymeleafContext = new Context();
            thymeleafContext.setVariables(templateModel);
            String htmlBody = thymeleafTemplateEngine.process(templateName, thymeleafContext);
            helper.setText(htmlBody, true);

            // add attachments
            for (String attachmentFilename : attachmentFilenames) {
                File attachmentFile = new File(attachmentFilename);
                String shortAttachmentFilename = attachmentFile.getName();
                helper.addAttachment(shortAttachmentFilename, attachmentFile);
            }
            javaMailSender.send(message);
        } catch (MessagingException | MailException e) {
            log.error("Error sending email with attachments");
        }
    }
}
