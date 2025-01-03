package com.auroratms.email.sender;

import com.auroratms.email.campaign.EmailCampaign;
import com.auroratms.email.campaign.EmailCampaignService;
import com.auroratms.email.campaign.FilterConfiguration;
import com.auroratms.email.config.EmailServerConfigurationEntity;
import com.auroratms.email.config.EmailServerConfigurationService;
import com.auroratms.profile.UserProfile;
import com.auroratms.profile.UserProfileService;
import com.auroratms.tournament.Tournament;
import com.auroratms.tournament.TournamentService;
import com.auroratms.tournamententry.TournamentEntry;
import com.auroratms.tournamententry.TournamentEntryService;
import com.auroratms.tournamentevententry.EventEntryStatus;
import com.auroratms.tournamentevententry.TournamentEventEntry;
import com.auroratms.tournamentevententry.TournamentEventEntryService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.mail.Address;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.util.*;
import java.util.function.Predicate;

@Service
@Slf4j
@Transactional
public class EmailSenderService {

    @Autowired
    private TournamentService tournamentService;

    @Autowired
    private TournamentEntryService entryService;

    @Autowired
    private TournamentEventEntryService tournamentEventEntryService;

    @Autowired
    private UserProfileService userProfileService;

    @Autowired
    private EmailServerConfigurationService emailServerConfigurationService;

    @Autowired
    private EmailCampaignService emailCampaignService;


    public List<FilterConfiguration.Recipient> getFilteredRecipients(long tournamentId, FilterConfiguration filterConfiguration) {

        boolean isGetAllRecipients = Boolean.TRUE.equals(filterConfiguration.getAllRecipients());
        boolean isExcludeRegistered = Boolean.TRUE.equals(filterConfiguration.getExcludeRegistered());

        // if all events
        Map<Long, String> tournamentEntryIdToProfileIdMap = new HashMap<>();
        if (!isGetAllRecipients || isExcludeRegistered) {
            List<TournamentEntry> tournamentEntries = entryService.listForTournament(tournamentId);
            for (TournamentEntry tournamentEntry : tournamentEntries) {
                tournamentEntryIdToProfileIdMap.put(tournamentEntry.getId(), tournamentEntry.getProfileId());
            }
        }

        // get all recipients
        List<UserProfile> filteredAllUserProfiles = new ArrayList<>();
        if (isGetAllRecipients) {
            Collection<UserProfile> userProfileList;
            List<String> stateFilters = filterConfiguration.getStateFilters();
            boolean allStates = stateFilters == null || (stateFilters != null && stateFilters.stream().anyMatch(new Predicate<String>() {
                @Override
                public boolean test(String state) {
                    return "ALL".equals(state);
                }
            }));
            if (allStates) {
                userProfileList = userProfileService.list();
            } else {
                userProfileList = userProfileService.listByStates(stateFilters);
            }
            for (UserProfile userProfile : userProfileList) {
                // skip test user profiles
                if (!userProfile.getEmail().matches("swaveklorenc\\+(.*)@gmail\\.com")) {
                    if (isExcludeRegistered) {
                        boolean alreadyInTournament = tournamentEntryIdToProfileIdMap.values().stream().anyMatch(Predicate.isEqual(userProfile.getUserId()));
                        if (!alreadyInTournament) {
                            filteredAllUserProfiles.add(userProfile);
                        }
                    } else {
                        filteredAllUserProfiles.add(userProfile);
                    }
                }
            }
        }

        List<String> playerProfileIds = new ArrayList<>();
        List<Long> eventIdsToFilterBy = filterConfiguration.getRecipientFilters();
        if (eventIdsToFilterBy.contains(0L)) {
            List<TournamentEventEntry> tournamentEventEntries = tournamentEventEntryService.listAllForTournament(tournamentId);
            for (TournamentEventEntry eventEntry : tournamentEventEntries) {
                long tournamentEntryFk = eventEntry.getTournamentEntryFk();
                if (eventEntry.getStatus() == EventEntryStatus.ENTERED) {
                    String profileId = tournamentEntryIdToProfileIdMap.get(tournamentEntryFk);
                    if (profileId != null && !playerProfileIds.contains(profileId)) {
                        playerProfileIds.add(profileId);
                    }
                }
            }
        } else {
            for (Long eventId : eventIdsToFilterBy) {
                List<TournamentEventEntry> allEventEntries = this.tournamentEventEntryService.listAllForEvent(eventId);
                for (TournamentEventEntry eventEntry : allEventEntries) {
                    long tournamentEntryFk = eventEntry.getTournamentEntryFk();
                    String profileId = tournamentEntryIdToProfileIdMap.get(tournamentEntryFk);
                    if (profileId != null && !playerProfileIds.contains(profileId)) {
                        playerProfileIds.add(profileId);
                    }
                }
            }
        }

        // get profiles which contain email addresses and convert them into recipients (full name + email)
        List<FilterConfiguration.Recipient> recipients = new ArrayList<>(playerProfileIds.size());
        if (isGetAllRecipients) {
            for (UserProfile filteredAllUserProfile : filteredAllUserProfiles) {
                FilterConfiguration.Recipient recipient = new FilterConfiguration.Recipient();
                recipient.setEmailAddress(filteredAllUserProfile.getEmail());
                recipient.setFirstName(filteredAllUserProfile.getFirstName());
                recipient.setLastName(filteredAllUserProfile.getLastName());
                recipients.add(recipient);
            }
        } else {
            Collection<UserProfile> userProfiles = this.userProfileService.listByProfileIds(playerProfileIds);
            List<FilterConfiguration.Recipient> removedRecipients = filterConfiguration.getRemovedRecipients();
            for (UserProfile userProfile : userProfiles) {
                FilterConfiguration.Recipient recipient = new FilterConfiguration.Recipient();
                recipient.setEmailAddress(userProfile.getEmail());
                recipient.setFirstName(userProfile.getFirstName());
                recipient.setLastName(userProfile.getLastName());
                if (!removedRecipients.contains(recipient)) {
                    recipients.add(recipient);
                }
            }
        }

        return recipients;
    }

    /**
     * Sends test email via SMTP host defined by config
     * @param config
     * @throws MessagingException
     */
    public void sendTestEmail(EmailServerConfigurationEntity config) throws MessagingException {
        JavaMailSenderImpl javaMailSender = getJavaMailSender(config);
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        mimeMessage.setText("Test email body");
        mimeMessage.setSubject("Email server test from ttaurora.com");
        Address emailAddress = new InternetAddress(config.getUserId());
        mimeMessage.setFrom(emailAddress);
        mimeMessage.setRecipient(Message.RecipientType.TO, emailAddress);
        javaMailSender.send(mimeMessage);
    }

    private JavaMailSenderImpl getJavaMailSender(EmailServerConfigurationEntity config) {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost(config.getServerHost());
        mailSender.setPort(config.getServerPort());

        mailSender.setUsername(config.getUserId());
        mailSender.setPassword(config.getPassword());

        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
//        props.put("mail.debug", "true");
        props.put("mail.smtp.connectiontimeout", 5000);
        props.put("mail.smtp.timeout", 5000);

        return mailSender;
    }

    public void sendCampaign(Long tournamentId, EmailCampaign emailCampaign, CampaignSendingStatus campaignSendingStatus, String currentUserName, Boolean sendTestEmail) {
        campaignSendingStatus.phase = "Getting recipients and variables information";
        log.info("Getting recipients and variables information for campaign " + emailCampaign.toString());

        // retrieve email server configuration for current user (i.e. Tournament director)
        String profileByLoginId = userProfileService.getProfileByLoginId(currentUserName);
        EmailServerConfigurationEntity emailServerConfiguration = emailServerConfigurationService.findById(profileByLoginId);

        JavaMailSenderImpl javaMailSender = getJavaMailSender(emailServerConfiguration);

        List<FilterConfiguration.Recipient> recipients = new ArrayList<>();
        if (sendTestEmail) {
            UserProfile userProfile = userProfileService.getProfile(profileByLoginId);
            FilterConfiguration.Recipient recipient = new FilterConfiguration.Recipient();
            recipient.setEmailAddress(userProfile.getEmail());
            recipient.setFirstName(userProfile.getFirstName());
            recipient.setLastName(userProfile.getLastName());
            recipients.add(recipient);
        } else {
            // get recipients per filter configuration
            FilterConfiguration filterConfiguration = emailCampaign.getFilterConfiguration();
            recipients = getFilteredRecipients(tournamentId, filterConfiguration);
        }

        Tournament tournament = tournamentService.getByKey(tournamentId);
        String tournamentName = tournament.getName();
        Map<String, String> variables = new HashMap<>();
        variables.put("${tournament_name}", tournamentName);

        log.info(String.format("Got %d recipients. Sending emails", recipients.size()));
        campaignSendingStatus.phase = String.format("Got %d recipients. Sending emails", recipients.size());
        campaignSendingStatus.totalSent = 0;
        campaignSendingStatus.totalErrors = 0;

        emailCampaign.setDateSent(new Date());
        emailCampaign.setTournamentName(tournamentName);
        emailCampaign.setEmailsCount(0);
        this.emailCampaignService.save(emailCampaign);

        boolean htmlEmail = emailCampaign.isHtmlEmail();

        for (FilterConfiguration.Recipient recipient : recipients) {
            try {
                variables.put("${last_name}", recipient.getLastName());
                variables.put("${first_name}", recipient.getFirstName());

                String subject = emailCampaign.getSubject();
                subject = replaceVariables(subject, variables);

                String body = emailCampaign.getBody();
                body = replaceVariables(body, variables);

                if (htmlEmail) {
                    MimeMessage message = javaMailSender.createMimeMessage();
                    message.setFrom(emailServerConfiguration.getUserId());
                    message.setRecipient(Message.RecipientType.TO, new InternetAddress(recipient.getEmailAddress()));
                    message.setSubject(subject);

                    MimeBodyPart mimeBodyPart = new MimeBodyPart();
                    mimeBodyPart.setContent(body, "text/html; charset=utf-8");

                    Multipart multipart = new MimeMultipart();
                    multipart.addBodyPart(mimeBodyPart);

                    message.setContent(multipart);
                    javaMailSender.send(message);
                } else {
                    SimpleMailMessage message = new SimpleMailMessage();
                    message.setFrom(emailServerConfiguration.getUserId());
                    message.setTo(recipient.getEmailAddress());
                    message.setSubject(subject);
                    message.setText(body);
                    javaMailSender.send(message);
                }

                campaignSendingStatus.totalSent++;
                if (campaignSendingStatus.totalSent % 5 == 0) {
                    log.info("Sent " + campaignSendingStatus.totalSent + " emails");
                }
                // throttle sending so we get fewer errors
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException ignored) {

                }
            } catch (MailException | MessagingException e) {
                log.error("Error sending email to " + recipient.getFirstName() + " " + recipient.getLastName() + " email " + recipient.getEmailAddress() + " cause " + e.getMessage());
                if (campaignSendingStatus.totalErrors == 0) {
                    log.error("Email error", e);
                }
                campaignSendingStatus.totalErrors++;
            }
        }
        campaignSendingStatus.endTime = System.currentTimeMillis();
        long duration = campaignSendingStatus.endTime - campaignSendingStatus.startTime;
        log.info("Finished sending emails in " + duration + " ms. Total sent " + campaignSendingStatus.totalSent + ", errors " + campaignSendingStatus.totalErrors);
        // update status
        emailCampaign.setEmailsCount(campaignSendingStatus.totalSent);
        this.emailCampaignService.save(emailCampaign);
    }

    private String replaceVariables(String text, Map<String, String> variables) {
        String [] searchList = new String[variables.size()];
        String [] replacementList = new String[variables.size()];
        int i = 0;
        for (String variable : variables.keySet()) {
            String value = variables.get(variable);
            searchList[i] = variable;
            replacementList[i] = value;
            i++;
        }
        return StringUtils.replaceEachRepeatedly(text, searchList, replacementList);
    }
}
