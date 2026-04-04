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
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.opencsv.CSVReader;
import jakarta.mail.Address;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.Multipart;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import software.amazon.awssdk.services.sesv2.SesV2Client;
import software.amazon.awssdk.services.sesv2.model.*;

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

    private boolean useSes = false;

    @Autowired
    private SesV2Client sesV2Client;

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Retrieves a list of filtered recipients based on the provided tournament ID and filter configuration.
     * The filtering logic can include multiple criteria such as inclusion of all recipients, exclusion of already
     * registered participants, filtering by events, and inclusion of uploaded recipients from a specified file.
     *
     * @param tournamentId        The ID of the tournament for which recipients are being filtered.
     * @param filterConfiguration An instance of {@link FilterConfiguration} containing various filtering options such as:
     *                            - `getAllRecipients` to determine if all recipients should be included.
     *                            - `excludeRegistered` to exclude already registered recipients.
     *                            - `recipientFilters` to filter recipients based on specific events.
     *                            - `stateFilters` to filter recipients based on their state.
     *                            - `removedRecipients` to exclude specific recipients.
     *                            - `includeUploadedRecipients` and `uploadedRecipientsFile` to include external recipients from a file.
     * @return A list of {@link FilterConfiguration.Recipient} containing filtered recipients who match the criteria
     * specified in the filter configuration. The list excludes duplicates, removed recipients, and participants
     * filtered out by the provided state or event constraints.
     */
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
            log.info("Getting players in all or selected events");
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
        log.info("Removed recipients count " + removedRecipients.size());
        List<FilterConfiguration.Recipient> recipients = new ArrayList<>();
        for (UserProfile userProfile : userProfileList) {
            FilterConfiguration.Recipient recipient = new FilterConfiguration.Recipient();
            recipient.setEmailAddress(userProfile.getEmail());
            recipient.setFirstName(userProfile.getFirstName());
            recipient.setLastName(userProfile.getLastName());
            recipient.setState(userProfile.getState());
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
                List<String> stateFilters = filterConfiguration.getStateFilters();
                boolean isAllStates = isAllStatesSelected(stateFilters);
                for (FilterConfiguration.Recipient recipient : recipientsFromFile) {
                    if (!removedRecipients.contains(recipient) && !recipients.contains(recipient)) {
                        if (stateFilters == null || isAllStates) {
                            recipients.add(recipient);
                        } else if (stateFilters.contains(recipient.getState())) {
                            recipients.add(recipient);
                        }
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
        boolean allStates = isAllStatesSelected(stateFilters);
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

        // remove unsubscribed players
        int before = filteredAllUserProfiles.size();
        filteredAllUserProfiles = filteredAllUserProfiles.stream().filter(UserProfile::isEmailSubscribed).toList();
        int after = filteredAllUserProfiles.size();
        log.info("Removed " + (before - after) + " unsubscribed users");

        log.info("Final filtered user profiles list " + filteredAllUserProfiles.size());
        return filteredAllUserProfiles;
    }

    private boolean isAllStatesSelected(List<String> stateFilters) {
        return stateFilters == null || stateFilters.stream().anyMatch(new Predicate<String>() {
            @Override
            public boolean test(String state) {
                return "ALL".equals(state);
            }
        });
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
                        case 3:
                            recipient.setState(text != null ? text.trim() : null);
                        default:
                            break;
                    }
                    columnNum++;
                    if (columnNum > 3) {
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
     * Sends an email campaign to a list of recipients. Depending on the configuration, the method
     * can send the campaign using AWS SES Bulk or JavaMail SMTP. It also supports sending
     * a test email to the campaign creator.
     *
     * @param tournamentId          The unique identifier of the tournament associated with the campaign.
     * @param emailCampaign         The email campaign object containing campaign details such as content and configuration.
     * @param campaignSendingStatus The object used to track the current phase and progress of the campaign sending process.
     * @param currentUserName       The username of the current user who initiated the campaign process.
     * @param sendTestEmail         A flag indicating whether the method should send a single test email instead of a full campaign.
     */
    public void sendCampaign(Long tournamentId, EmailCampaign emailCampaign, CampaignSendingStatus campaignSendingStatus, String currentUserName, Boolean sendTestEmail) {
        campaignSendingStatus.phase = "Initializing campaign";
        log.info("Starting campaign send. Mode: " + (useSes ? "AWS SES Bulk" : "JavaMail SMTP"));

        String profileByLoginId = userProfileService.getProfileByLoginId(currentUserName);
        EmailServerConfigurationEntity emailConfig = emailServerConfigurationService.findById(profileByLoginId);

        // 1. Get Recipients
        campaignSendingStatus.phase = "Getting recipients and variables information";
        log.info("Getting recipients and variables information for campaign " + emailCampaign.toString());
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
//recipients = getSesTestRecipients();
            log.info("Sending email campaign to " + recipients.size() + " recipients");
        }
        Tournament tournament = tournamentService.getByKey(tournamentId);
        String tournamentName = tournament.getName();

        // B. Prepare Global/Default Variables
        String playerListUrl = String.format("%s/ui/tournaments/playerlist/%s", this.clientHostUrl, tournamentId);
        String registrationUrl = String.format("%s/ui/tournaments/view/%s", this.clientHostUrl, tournamentId);
        Map<String, String> templateData = new HashMap<>();
        templateData.put("${tournament_name}", tournamentName);
        templateData.put("${tournament_director_name}", tournament.getContactName());
        templateData.put("${player_list_url}", playerListUrl);
        templateData.put("${tournament_registration_url}", registrationUrl);

        log.info(String.format("Got %d recipients. Sending emails", recipients.size()));
        campaignSendingStatus.phase = String.format("Got %d recipients. Sending emails", recipients.size());
        campaignSendingStatus.totalSent = 0;
        campaignSendingStatus.totalErrors = 0;

        emailCampaign.setDateSent(new Date());
        emailCampaign.setTournamentName(tournamentName);
        emailCampaign.setEmailsCount(0);
        this.emailCampaignService.save(emailCampaign);

        if (useSes) {
            // --- AWS SES BULK PATH ---
            sendViaSesBulk(tournamentId, emailCampaign, recipients, emailConfig, templateData, campaignSendingStatus);
        } else {
            // --- LEGACY JAVAMAIL PATH (or single Test Email) ---
            sendViaLegacySmtp(tournament.getName(), emailCampaign, recipients, templateData, campaignSendingStatus, currentUserName);
        }

        campaignSendingStatus.endTime = System.currentTimeMillis();
        log.info("Finished. Sent: {}, Errors: {}", campaignSendingStatus.totalSent, campaignSendingStatus.totalErrors);
    }

    private List<FilterConfiguration.Recipient> getSesTestRecipients() {
        List<FilterConfiguration.Recipient> recipients = new ArrayList<>();
        FilterConfiguration.Recipient recipient = new FilterConfiguration.Recipient();
        recipient.setEmailAddress("success@simulator.amazonses.com");
        recipient.setFirstName("Success");
        recipient.setLastName("Test");
        recipients.add(recipient);

        recipient = new FilterConfiguration.Recipient();
        recipient.setEmailAddress("bounce@simulator.amazonses.com");
        recipient.setFirstName("Hard");
        recipient.setLastName("Bounce");
        recipients.add(recipient);

        recipient = new FilterConfiguration.Recipient();
        recipient.setEmailAddress("complaint@simulator.amazonses.com");
        recipient.setFirstName("Spam");
        recipient.setLastName("Complaint");
        recipients.add(recipient);

        return recipients;
    }

    /**
     * Sends bulk emails via Amazon SES (Simple Email Service) using a templated approach.
     * The method prepares an SES email template based on the provided email campaign details
     * and sends emails in batches of 50 to the specified recipients.
     * adds mandatory Gmail/Yahoo unsubscribe headers.
     *
     * @param tournamentId             Unique identifier of the tournament, used for generating a distinct template name.
     * @param emailCampaign            Contains the email subject, body, and campaign-specific configurations.
     * @param recipients               A list of recipients to whom the email should be sent, including personalization details.
     * @param emailServerConfiguration Configuration details for the email server, including the reply-to address.
     * @param defaultData              Default key-value data to populate template placeholders for all recipients.
     * @param status                   A mutable object for tracking the sending progress and error statistics.
     */
    private void sendViaSesBulk(Long tournamentId,
                                EmailCampaign emailCampaign,
                                List<FilterConfiguration.Recipient> recipients,
                                EmailServerConfigurationEntity emailServerConfiguration,
                                Map<String, String> defaultData,
                                CampaignSendingStatus status) {

        // Generate a unique, timestamped template name to prevent race conditions
        String timestamp = new java.text.SimpleDateFormat("yyyyMMddHHmmss").format(new java.util.Date());
        String templateName = String.format("T_%d_C_%d_%s", tournamentId, emailCampaign.getId(), timestamp);

        try {
            // A. Prepare and Sync the Template
            String subject = replaceVariables(emailCampaign.getSubject(), defaultData);
            String body = replaceVariables(emailCampaign.getBody(), defaultData);

            // B. Convert tokens from ${ } to {{ }}
            String sesSubject = convertToSesTemplate(subject);
            String sesBody = convertToSesTemplate(body);

            // NOTE: Make sure your syncSesTemplate method is updated to use the SesV2Client!
            syncSesTemplate(templateName, sesSubject, sesBody, emailCampaign.isHtmlEmail());

            // C. Send in Batches of 50
            for (int i = 0; i < recipients.size(); i += 50) {
                List<FilterConfiguration.Recipient> batch = recipients.subList(i, Math.min(i + 50, recipients.size()));

                // Build the V2 Bulk Email Entries
                List<BulkEmailEntry> entries = batch.stream().map(r -> {
                    Map<String, String> personalData = new java.util.HashMap<>();
                    personalData.put("first_name", r.getFirstName());
                    personalData.put("last_name", r.getLastName());

                    String unsubscribeUrl = String.format("%s/publicapi/profiles/unsubscribe/%s", clientHostUrl, r.getEmailAddress());
                    personalData.put("unsubscribe_url", unsubscribeUrl);

                    // 1. Explicitly build the headers as a list to fix the stream compile error
                    List<MessageHeader> headers = java.util.Arrays.asList(
                            MessageHeader.builder()
                                    .name("List-Unsubscribe")
                                    .value("<" + unsubscribeUrl + ">")
                                    .build(),
                            MessageHeader.builder()
                                    .name("List-Unsubscribe-Post")
                                    .value("List-Unsubscribe=One-Click")
                                    .build()
                    );

                    return BulkEmailEntry.builder()
                            // Map the destination
                            .destination(d -> d.toAddresses(r.getEmailAddress()))

                            // Map the template data
                            .replacementEmailContent(content -> content
                                    .replacementTemplate(template -> template
                                            .replacementTemplateData(toJson(personalData))
                                    )
                            )

                            // 2. Pass the explicitly typed list here!
                            .replacementHeaders(headers)
                            .build();
                }).collect(java.util.stream.Collectors.toList());

                // Build the V2 Bulk Request
                SendBulkEmailRequest bulkRequest = SendBulkEmailRequest.builder()
                        .fromEmailAddress(emailServerConfiguration.getUserId()) // Must be verified
                        .replyToAddresses(emailServerConfiguration.getUserId())
                        .defaultContent(BulkEmailContent.builder()
                                .template(Template.builder()
                                        .templateName(templateName)
                                        // Merges your default data placeholders across the whole batch
                                        .templateData(toJson(defaultData))
                                        .build())
                                .build())
                        .bulkEmailEntries(entries)
                        .build();

                // Calling SES v2 instead of v1
                sesV2Client.sendBulkEmail(bulkRequest);

                status.totalSent += batch.size();
                log.info("Dispatched SES v2 batch. Total sent so far: " + status.totalSent);

                // Your custom 10-second sleep interval
                if (i + 50 < recipients.size()) {
                    try {
                        Thread.sleep(10000);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException("Campaign interrupted", e);
                    }
                }
            }
        } catch (Exception e) {
            log.error("Critical failure in SES Bulk send", e);
            status.totalErrors = recipients.size() - status.totalSent;
        } finally {
            // D. Cleanup: Always delete the temporary template
            // NOTE: Make sure deleteSesTemplate is updated to use SesV2Client!
            deleteSesTemplate(templateName);
        }
    }

    /**
     * Synchronizes an Amazon SES email template using SES v2 by creating a new template
     * or updating an existing one if it already exists.
     *
     * @param name    the name of the template to be created or updated
     * @param subject the subject line of the email template
     * @param body    the HTML or text content of the email template
     * @param isHtml  true if the email body is in HTML format, false otherwise
     */
    private void syncSesTemplate(String name, String subject, String body, boolean isHtml) {
        try {
            log.info("Preparing template data for: " + name);

            // We build the body and fallback strings ahead of time
            String htmlContent = isHtml ? body : "";
            String textContent = isHtml
                    ? "This message is sent in HTML format. Please use an HTML-compatible email client to view it."
                    : body;

            try {
                log.info("Attempting to create SES v2 template: " + name);

                // 1. Consumer builder handles object instantiation automatically!
                this.sesV2Client.createEmailTemplate(req -> req
                        .templateName(name)
                        .templateContent(content -> content
                                .subject(subject)
                                .html(htmlContent)
                                .text(textContent)
                        )
                );
                log.info("Successfully created template: " + name);

            } catch (AlreadyExistsException e) {
                log.info("SES v2 template already exists, updating instead: " + name);

                // 2. Exact same mapping structure for the update call
                this.sesV2Client.updateEmailTemplate(req -> req
                        .templateName(name)
                        .templateContent(content -> content
                                .subject(subject)
                                .html(htmlContent)
                                .text(textContent)
                        )
                );
                log.info("Successfully updated template: " + name);
            }

        } catch (Exception e) {
            log.error("Failed to sync SES v2 template " + name, e);
            throw e;
        }
    }

    /**
     * Deletes an Amazon SES email template using SES v2 by its name.
     *
     * @param name template name
     */
    private void deleteSesTemplate(String name) {
        try {
            // Consumer builder approach for deletion
            this.sesV2Client.deleteEmailTemplate(req -> req.templateName(name));
            log.info("Deleted temporary SES v2 template: " + name);
        } catch (Exception e) {
            log.warn("Failed to delete template: " + name + " (It may have already been removed)");
        }
    }

    /**
     * Converts a string to a JSON string by escaping special characters.
     *
     * @param input
     * @return
     */
    private String convertToSesTemplate(String input) {
        if (input == null) return "";
        // Converts ${var_name} to {{var_name}} for SES Handlebars
        return input.replace("${", "{{").replace("}", "}}");
    }

    private String toJson(Map<String, String> data) {
        try {
            return objectMapper.writeValueAsString(data);
        } catch (JsonProcessingException e) {
            return "{}";
        }
    }

    /**
     * Sends an email campaign to a list of recipients using a legacy SMTP configuration.
     *
     * @param tournamentName        the name of the tournament associated with the email campaign
     * @param emailCampaign         the email campaign to be processed and sent
     * @param recipients            the list of recipients to whom the emails will be sent
     * @param variables             a map of variables to be replaced in the email subject and body
     * @param campaignSendingStatus an object tracking the sending status including total sent and errors
     * @param currentUserName       the username of the current user, typically the tournament director
     */
    private void sendViaLegacySmtp(String tournamentName,
                                   EmailCampaign emailCampaign,
                                   List<FilterConfiguration.Recipient> recipients,
                                   Map<String, String> variables,
                                   CampaignSendingStatus campaignSendingStatus,
                                   String currentUserName) {
        // retrieve email server configuration for current user (i.e. Tournament director)
        String profileByLoginId = userProfileService.getProfileByLoginId(currentUserName);
        EmailServerConfigurationEntity emailServerConfiguration = emailServerConfigurationService.findById(profileByLoginId);

        JavaMailSenderImpl javaMailSender = getJavaMailSender(emailServerConfiguration);

        boolean htmlEmail = emailCampaign.isHtmlEmail();
        for (FilterConfiguration.Recipient recipient : recipients) {
            try {
                variables.put("${last_name}", recipient.getLastName());
                variables.put("${first_name}", recipient.getFirstName());
                String unsubscribeUrl = String.format("%s/publicapi/profiles/unsubscribe/%s", this.clientHostUrl, recipient.getEmailAddress());
                variables.put("${unsubscribe_url}", unsubscribeUrl);

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
                    Thread.sleep(5000);
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

    /**
     * Replaces placeholders in the provided text with corresponding values from the given map of variables.
     * Placeholders are expected to match the keys in the variables map.
     *
     * @param text      the input string containing placeholders to be replaced
     * @param variables a map where keys are placeholder strings and values are the replacements
     * @return a new string with placeholders replaced by their corresponding values from the variables map
     */
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
