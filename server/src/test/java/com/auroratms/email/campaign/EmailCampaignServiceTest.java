package com.auroratms.email.campaign;

import com.auroratms.AbstractServiceTest;
import com.auroratms.users.UserRoles;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Transactional
public class EmailCampaignServiceTest extends AbstractServiceTest {

    @Autowired
    private EmailCampaignService service;

    @Test
    @WithMockUser(username = "swaveklorenc@gmail.com", authorities = {UserRoles.TournamentDirectors})
    public void testCRUD () {
        EmailCampaign emailCampaign = makeEmailCampaign("2023 Aurora Fall Open Campaign");
        EmailCampaign saved = service.save(emailCampaign);
        assertNotNull(saved.getRecipientFilters() != null, "no recipient fiters");
        assertNotNull(saved.getRemovedRecipients() != null, "no removed recipients");

        boolean existsById = service.exists(saved.getId());
        assertTrue(existsById, "doesn't exist but should");

        EmailCampaign found = service.findById(saved.getId());

        found.setSubject("Registration now closed");
        EmailCampaign updated = service.save(found);

        String subject = updated.getSubject();
        assertEquals("Registration now closed", subject, "wrong subject");

        service.delete(updated.getId());
        existsById = service.exists(updated.getId());
        assertFalse(existsById, "Exist but shouldn't");
    }

    @Test
    @WithMockUser(username = "swaveklorenc@gmail.com", authorities = {UserRoles.TournamentDirectors})
    public void testAccess() {
        EmailCampaign emailCampaign = makeEmailCampaign("2024 Aurora Summer Open Campaign");
        EmailCampaign saved = service.save(emailCampaign);
        Page<EmailCampaign> pageWithMyCampaigns = service.findByName(null, Pageable.unpaged());

        assertEquals(1, pageWithMyCampaigns.getTotalElements(), "wrong number of elements");
        List<EmailCampaign> campaignList = pageWithMyCampaigns.getContent();
        for (EmailCampaign EmailCampaign : campaignList) {
            assertEquals("2024 Aurora Summer Open Campaign", EmailCampaign.getName(), "wrong campaign name");
        }

        Authentication previousUser = switchAuthentication("swaveklorenc+edho@gmail.com", UserRoles.USATTSanctionCoordinators);
        Page<EmailCampaign> pageWithEdsCampaigns = service.findByName(null, Pageable.unpaged());
        assertEquals(0, pageWithEdsCampaigns.getTotalElements(), "wrong number of elements");
        List<EmailCampaign> campaignList2 = pageWithEdsCampaigns.getContent();
        assertTrue(campaignList2.isEmpty(), "empty list");

        SecurityContextHolder.getContext().setAuthentication(previousUser);
    }

    private EmailCampaign makeEmailCampaign(String name) {
        EmailCampaign emailCampaign = new EmailCampaign();
        emailCampaign.setName(name);
        emailCampaign.setSubject("Registration now open");
        emailCampaign.setBody("Hello Robert,\nThis is just to let you know that we are ...");

        List<Long> filters = new ArrayList<Long>();
        filters.add(12L); // some event id

        FilterConfiguration.Recipient recipient1 = new FilterConfiguration.Recipient();
        recipient1.setEmailAddress("jtravolta1234@gmail.com");
        recipient1.setFirstName("John");
        recipient1.setLastName("Travolta");
        FilterConfiguration.Recipient recipient2 = new FilterConfiguration.Recipient();
        recipient2.setEmailAddress("barry.gibbs.@gmail.com");
        recipient2.setFirstName("Barry");
        recipient2.setLastName("Gibbs");
        emailCampaign.setRecipientFilters(filters);

        List<FilterConfiguration.Recipient> removedRecipientsList = new ArrayList<>();
        removedRecipientsList.add(recipient1);
        removedRecipientsList.add(recipient2);
        emailCampaign.setRemovedRecipients(removedRecipientsList);

        return emailCampaign;
    }
}
