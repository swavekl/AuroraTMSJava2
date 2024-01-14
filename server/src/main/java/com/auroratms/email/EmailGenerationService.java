package com.auroratms.email;

import com.auroratms.email.config.EmailServerConfigurationEntity;
import com.auroratms.email.config.EmailServerConfigurationService;
import com.auroratms.profile.UserProfile;
import com.auroratms.profile.UserProfileService;
import com.auroratms.tournamententry.TournamentEntry;
import com.auroratms.tournamententry.TournamentEntryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.mail.Address;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Properties;

@Service
@Transactional
public class EmailGenerationService {

    @Autowired
    private TournamentEntryService entryService;

    @Autowired
    private UserProfileService userProfileService;

    public List<String> listEmailsForTournament(long tournamentId) {
        List<TournamentEntry> tournamentEntries = entryService.listForTournament(tournamentId);
        List<String> playerProfileIds = new ArrayList<>(tournamentEntries.size());
        for (TournamentEntry tournamentEntry : tournamentEntries) {
            String profileId = tournamentEntry.getProfileId();
            playerProfileIds.add(profileId);
        }

        List<String> emailAddresses = new ArrayList<>(playerProfileIds.size());
        Collection<UserProfile> userProfiles = this.userProfileService.listByProfileIds(playerProfileIds);
        for (UserProfile userProfile : userProfiles) {
            emailAddresses.add(userProfile.getEmail());
        }

        return emailAddresses;
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
        props.put("mail.debug", "true");
        props.put("mail.smtp.connectiontimeout", 5000);
        props.put("mail.smtp.timeout", 5000);

        return mailSender;
    }
}
