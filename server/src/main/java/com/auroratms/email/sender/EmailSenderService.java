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
import com.auroratms.utils.filerepo.FileInfo;
import com.auroratms.utils.filerepo.FileRepositoryException;
import com.auroratms.utils.filerepo.FileRepositoryFactory;
import com.auroratms.utils.filerepo.IFileRepository;
import com.opencsv.CSVReader;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
import java.io.InputStreamReader;
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

    @Autowired
    private FileRepositoryFactory fileRepositoryFactory;

    @Value("${client.host.url}")
    private String clientHostUrl;

    public List<FilterConfiguration.Recipient> getFilteredRecipients(long tournamentId, FilterConfiguration filterConfiguration) {

        boolean isGetAllRecipients = Boolean.TRUE.equals(filterConfiguration.getAllRecipients());
        boolean isExcludeRegistered = Boolean.TRUE.equals(filterConfiguration.getExcludeRegistered());
        log.info("isGetAllRecipients = " + isGetAllRecipients + ", isExcludeRegistered = " + isExcludeRegistered);

        // not all users (i.e. only selected events) or exclude those already registered
        Map<Long, String> tournamentEntryIdToProfileIdMap = new HashMap<>();
        if (!isGetAllRecipients || isExcludeRegistered) {
            tournamentEntryIdToProfileIdMap = getTournamentEntryToProfileIdMap(tournamentId);
        }

        // get user profiles
        Collection<UserProfile> userProfileList = new ArrayList<>();
        if (isGetAllRecipients) {
            userProfileList = getUserProfiles(filterConfiguration.getStateFilters(), isExcludeRegistered, tournamentEntryIdToProfileIdMap);
        } else {
            // or filter by events
            log.info ("Getting players in all or selected events");
            List<String> playerProfileIds = new ArrayList<>();
            List<Long> eventIdsToFilterBy = filterConfiguration.getRecipientFilters();
            if (!eventIdsToFilterBy.isEmpty()) {
                playerProfileIds = getProfilesOfPlayersInEvents(tournamentId, eventIdsToFilterBy, tournamentEntryIdToProfileIdMap);
                log.info("playerProfileIds with " + playerProfileIds.size() + " profiles");
            }
            userProfileList = userProfileService.listByProfileIds(playerProfileIds);
            log.info("Got " + userProfileList.size() + " user profiles for events");
        }

        // convert profiles which contain email addresses and convert them into recipients (full name + email)
        log.info("Converting " + userProfileList.size() + " user profiles to recipients");
        List<FilterConfiguration.Recipient> removedRecipients = filterConfiguration.getRemovedRecipients();
        List<FilterConfiguration.Recipient> recipients = new ArrayList<>();
        for (UserProfile userProfile : userProfileList) {
            FilterConfiguration.Recipient recipient = new FilterConfiguration.Recipient();
            recipient.setEmailAddress(userProfile.getEmail());
            recipient.setFirstName(userProfile.getFirstName());
            recipient.setLastName(userProfile.getLastName());
            if (!removedRecipients.contains(recipient)) {
                recipients.add(recipient);
            }
        }

        // load recipients from a file if requested
        boolean includeUploadedRecipients = Boolean.TRUE.equals(filterConfiguration.isIncludeUploadedRecipients());
        if (includeUploadedRecipients && StringUtils.isNotEmpty(filterConfiguration.getUploadedRecipientsFile())) {
            int recipientsBefore = recipients.size();
            try {
                log.info("Reading additional recipients file from " + filterConfiguration.getUploadedRecipientsFile());
                IFileRepository fileRepository = this.fileRepositoryFactory.getFileRepository();
                FileInfo fileInfo = fileRepository.read(filterConfiguration.getUploadedRecipientsFile());
                List<FilterConfiguration.Recipient> recipientsFromFile = readRecipientsFile(fileInfo);
                int skippedRecipients = 0;
                for (FilterConfiguration.Recipient recipient : recipientsFromFile) {
                    if (!removedRecipients.contains(recipient) && !recipients.contains(recipient)) {
                        recipients.add(recipient);
                    } else {
                        skippedRecipients++;
                    }
                }
                if (skippedRecipients > 0) {
                    log.info("Didn't add " + skippedRecipients + " uploaded recipients because they were either removed or duplicate");
                }
            } catch (FileRepositoryException e) {
                log.error("Error opening uploaded recipients file", e);
            }
            log.info("Added " + (recipients.size() - recipientsBefore) + " additional recipients from file. Total recipients " + recipients.size());
        }

        return recipients;
    }

    /**
     * Gets a list of profile
     *
     * @param tournamentId
     * @param eventIdsToFilterBy
     * @param tournamentEntryIdToProfileIdMap
     * @return
     */
    private List<String> getProfilesOfPlayersInEvents(long tournamentId,
                                                      List<Long> eventIdsToFilterBy,
                                                      Map<Long, String> tournamentEntryIdToProfileIdMap) {
        List<String> playerProfileIds = new ArrayList<>();
        // get players from all events
        if (eventIdsToFilterBy.contains(0L)) {
            log.info("Getting all events");
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
            log.info("Filtering users by selected events only " + eventIdsToFilterBy);
            // get players from selected events only
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
        return playerProfileIds;
    }

    /**
     * Gets a map of tournament entry id to profile id
     *
     * @param tournamentId tournament id of a tournament to get entries for
     * @return
     */
    private Map<Long, String> getTournamentEntryToProfileIdMap(long tournamentId) {
        Map<Long, String> tournamentEntryIdToProfileIdMap = new HashMap<>();
        // get all tournament entries which still have events entered
        Set<Long> entriesWithEventsIds = new HashSet<>();
        List<TournamentEventEntry> tournamentEventEntries = tournamentEventEntryService.listAllForTournament(tournamentId);
        for (TournamentEventEntry eventEntry : tournamentEventEntries) {
            if (eventEntry.getStatus() == EventEntryStatus.ENTERED) {
                entriesWithEventsIds.add(eventEntry.getTournamentEntryFk());
            }
        }
        log.info("Players with valid entries in tournament: " + entriesWithEventsIds.size());

        // get all entries
        List<TournamentEntry> tournamentEntries = entryService.listForTournament(tournamentId);
        log.info("All entries in tournament " + tournamentEntries.size());
        for (TournamentEntry tournamentEntry : tournamentEntries) {
            // only add those who are still entered in some event
            if (entriesWithEventsIds.contains(tournamentEntry.getId())) {
                tournamentEntryIdToProfileIdMap.put(tournamentEntry.getId(), tournamentEntry.getProfileId());
            }
        }
        log.info("Entry id to profile map for valid entries size: " + tournamentEntryIdToProfileIdMap.size());

        return tournamentEntryIdToProfileIdMap;
    }

    /**
     * Gets profiles of users and optionally removes those who are already in the tournament
     *
     * @param stateFilters                    state names or ALL states
     * @param isExcludeRegistered             if true excludes profiles of players already registered for this tournament
     * @param tournamentEntryIdToProfileIdMap tournament entry id to profile id map
     */
    private List<UserProfile> getUserProfiles(List<String> stateFilters, boolean isExcludeRegistered, Map<Long, String> tournamentEntryIdToProfileIdMap) {
        List<UserProfile> filteredAllUserProfiles = new ArrayList<>();
        Collection<UserProfile> userProfileList;
        boolean allStates = stateFilters == null || stateFilters.stream().anyMatch(new Predicate<String>() {
            @Override
            public boolean test(String state) {
                return "ALL".equals(state);
            }
        });
        log.info("Getting user profiles list.  allStates is " + allStates);
        if (allStates) {
            userProfileList = userProfileService.list();
        } else {
            userProfileList = userProfileService.listByStates(stateFilters);
        }
        log.info("Got user profile list with " + userProfileList.size() + " user profiles.");
        log.info("Removing test users and isExcludeRegistered = " + isExcludeRegistered);
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
        log.info("Final filtered user profiles list " + filteredAllUserProfiles.size());
        return filteredAllUserProfiles;
    }

    /**
     * @param fileInfo
     * @return
     */
    private List<FilterConfiguration.Recipient> readRecipientsFile(FileInfo fileInfo) {
        List<FilterConfiguration.Recipient> recipientList = new ArrayList();
        try (CSVReader csvReader = new CSVReader(new InputStreamReader(fileInfo.getFileInputStream(), "UTF-8"));) {
            String[] values = null;
            int rowNumber = 0;
            while ((values = csvReader.readNext()) != null) {
                int columnNum = 0;
                rowNumber++;
                FilterConfiguration.Recipient recipient = new FilterConfiguration.Recipient();
                for (String text : values) {
                    switch (columnNum) {
                        case 0:
                            recipient.setLastName(text);
                            break;
                        case 1:
                            recipient.setFirstName(text);
                            break;
                        case 2:
                            if (text.contains("@")) {
                                recipient.setEmailAddress(text);
                            }
                            break;
                        default:
                            break;
                    }
                    columnNum++;
                    if (columnNum > 2) {
                        break;
                    }
                }
                if (StringUtils.isNotEmpty(recipient.getLastName()) &&
                        StringUtils.isNotEmpty(recipient.getFirstName()) &&
                        StringUtils.isNotEmpty(recipient.getEmailAddress())) {
                    recipientList.add(recipient);
                } else {
                    log.warn("Didn't add recipient " + recipient.getLastName() + " " + recipient.getFirstName());
                }
            }
            log.info("Read " + rowNumber + " rows from recipients file.");
        } catch (Exception e) {
            log.error("Error reading uploaded recipients file from " + fileInfo.getFilename());
        }
        return recipientList;
    }

    /**
     * Sends test email via SMTP host defined by config
     *
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

    /**
     * Sends email campaign or test email
     *
     * @param tournamentId
     * @param emailCampaign
     * @param campaignSendingStatus
     * @param currentUserName
     * @param sendTestEmail
     */
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
            log.info("Sending test email to " + recipient.getFirstName() + " " + recipient.getLastName() + " email: " + recipient.getEmailAddress());
        } else {
            // get recipients per filter configuration
            FilterConfiguration filterConfiguration = emailCampaign.getFilterConfiguration();
            recipients = getFilteredRecipients(tournamentId, filterConfiguration);
            log.info("Sending email campaign to " + recipients.size() + " recipients");
        }

        Tournament tournament = tournamentService.getByKey(tournamentId);
        String tournamentName = tournament.getName();

        String playerListUrl = String.format("%s/ui/tournaments/playerlist/%s", this.clientHostUrl, tournamentId);
        String registrationUrl = String.format("%s/ui/tournaments/view/%s", this.clientHostUrl, tournamentId);
        Map<String, String> variables = new HashMap<>();
        variables.put("${tournament_name}", tournamentName);
        variables.put("${tournament_director_name}", tournament.getContactName());
        variables.put("${player_list_url}", playerListUrl);
        variables.put("${tournament_registration_url}", registrationUrl);

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
        String[] searchList = new String[variables.size()];
        String[] replacementList = new String[variables.size()];
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
