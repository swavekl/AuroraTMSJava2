package com.auroratms.email.campaign;

import com.auroratms.AbstractServiceTest;
import com.auroratms.users.UserRoles;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

@Transactional
public class EmailCampaignServiceTest extends AbstractServiceTest {

    @Autowired
    private EmailCampaignService service;

    @Test
    @WithMockUser(username = "swaveklorenc@gmail.com", authorities = {UserRoles.TournamentDirectors})
    public void testCRUD () {
        EmailCampaign emailCampaign = makeEmailCampaign("2023 Aurora Fall Open Campaign");
        EmailCampaign saved = service.save(emailCampaign);
        assertNotNull("no recipient fiters", saved.getRecipientFilters() != null);
        assertNotNull("no removed recipients", saved.getRemovedRecipients() != null);

        boolean existsById = service.exists(saved.getId());
        assertTrue("doesn't exist but should", existsById);

        EmailCampaign found = service.findById(saved.getId());

        found.setSubject("Registration now closed");
        EmailCampaign updated = service.save(found);

        String subject = updated.getSubject();
        assertEquals("wrong subject", "Registration now closed", subject);

        service.delete(updated.getId());
        existsById = service.exists(updated.getId());
        assertFalse("Exist but shouldn't", existsById);
    }

    @Test
    @WithMockUser(username = "swaveklorenc@gmail.com", authorities = {UserRoles.TournamentDirectors})
    public void testAccess() {
        EmailCampaign emailCampaign = makeEmailCampaign("2024 Aurora Summer Open Campaign");
        EmailCampaign saved = service.save(emailCampaign);
        Page<EmailCampaign> pageWithMyCampaigns = service.findByName(null, Pageable.unpaged());

        assertEquals("wrong number of elements", 1, pageWithMyCampaigns.getTotalElements());
        List<EmailCampaign> campaignList = pageWithMyCampaigns.getContent();
        for (EmailCampaign EmailCampaign : campaignList) {
            assertEquals("wrong campaign name", "2024 Aurora Summer Open Campaign", EmailCampaign.getName());
        }

        Authentication previousUser = switchAuthentication("swaveklorenc+edho@gmail.com", UserRoles.USATTSanctionCoordinators);
        Page<EmailCampaign> pageWithEdsCampaigns = service.findByName(null, Pageable.unpaged());
        assertEquals("wrong number of elements", 0, pageWithEdsCampaigns.getTotalElements());
        List<EmailCampaign> campaignList2 = pageWithEdsCampaigns.getContent();
        assertTrue("empty list", campaignList2.isEmpty());

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
