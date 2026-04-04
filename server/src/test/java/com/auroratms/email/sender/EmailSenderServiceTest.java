package com.auroratms.email.sender;

import com.auroratms.AbstractServiceTest;
import com.auroratms.email.campaign.EmailCampaign;
import com.auroratms.email.campaign.EmailCampaignService;
import com.auroratms.users.UserRoles;
import org.assertj.core.util.Lists;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.fail;

@Transactional
public class EmailSenderServiceTest extends AbstractServiceTest {

    @Autowired
    private EmailSenderService emailSenderService;

    @Autowired
    private EmailCampaignService emailCampaignService;

    @Autowired
    private Environment env;

    @BeforeEach
    void setUp() {
        // Manually push environment properties into System properties if the SDK is missing them
        String accessKey = env.getProperty("spring.cloud.aws.credentials.access-key");
        String secretKey = env.getProperty("spring.cloud.aws.credentials.secret-key");
        String sessionToken = env.getProperty("spring.cloud.aws.credentials.session-token");

        if (accessKey != null) System.setProperty("aws.accessKeyId", accessKey);
        if (secretKey != null) System.setProperty("aws.secretAccessKey", secretKey);
        if (sessionToken != null) System.setProperty("aws.sessionToken", sessionToken);
    }

    @Test
    @WithMockUser(username = "swaveklorenc@gmail.com", authorities = {UserRoles.TournamentDirectors})
    public void testRealEmailSendingViaSes() {

        try {
            EmailCampaign emailCampaign = new EmailCampaign();
            CampaignSendingStatus campaignSendingStatus = new CampaignSendingStatus();
            emailCampaign.setName("SES Test Campaign");
            String subject = "${tournament_name} - SES Infrastructure Test";
            String content = "<h1>Success!</h1><p>Hello ${first_name}</p><p>This email proves that Vault, STS, and SES are working together.</p>";
            emailCampaign.setSubject(subject);
            emailCampaign.setBody(content);
            emailCampaign.setHtmlEmail(true);
            emailCampaign.setRemovedRecipients(Lists.emptyList());
            emailCampaign.setRecipientFilters(Lists.newArrayList(909L));

            emailCampaign = emailCampaignService.save(emailCampaign);
            // Replace with your actual personal email address for the test
            String currentUserName = "swaveklorenc@gmail.com";
            Boolean sendTestEmail = true;
            Long tournamentId = 133L;  // aurora cup
            emailSenderService.sendCampaign(tournamentId, emailCampaign, campaignSendingStatus, currentUserName, sendTestEmail);
            System.out.println("Check your inbox! The SES send command was accepted.");
        } catch (Exception e) {
            e.printStackTrace();
            fail("Email sending failed: " + e.getMessage());
        }
    }
}
