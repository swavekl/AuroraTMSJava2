package com.auroratms.utils;

import com.auroratms.users.UserRolesHelper;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestClientException;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Currency service for getting the exchange rates between different currencies
 * Created here so we don't have to deal with CORS configuration on the client
 */
@RestController
@RequestMapping("api/importtournament")
@PreAuthorize("isAuthenticated() and hasAuthority('TournamentDirectors')")
public class ImportTournamentController {

    private static final Logger log = LoggerFactory.getLogger(ImportTournamentController.class);
    @Autowired
    private ImportTournamentService importTournamentService;

    @GetMapping("/list")
    @ResponseBody
    public ResponseEntity<List<Map<String, String>>> listTournaments() {
        try {
            List<Map<String, String>> tournaments = this.importTournamentService.listTournaments();
            return ResponseEntity.ok(tournaments);
        } catch (RestClientException e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Imports entries extracted from html page in Omnipong locaated at fromUrl into tournament with toTournamentId
     *
     * @param importEntriesRequest request data
     * @return
     */
    @PostMapping("/checkaccounts")
    public ResponseEntity<ImportProgressInfo> checkAccounts(@RequestBody ImportEntriesRequest importEntriesRequest,
                                                            HttpSession session) {

        ImportProgressInfo importProgressInfo = new ImportProgressInfo();
        importProgressInfo.phaseName = "Starting player accounts check";
        importProgressInfo.jobId = UUID.randomUUID().toString();
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        Thread thread = new Thread(new Runnable() {
            @Override
            @Transactional
            public void run() {
                SecurityContextHolder.getContext().setAuthentication(authentication);

                session.setAttribute("importProgressInfo" + importProgressInfo.jobId, importProgressInfo);
                importTournamentService.checkAccounts(importEntriesRequest.tournamentId,
                        importEntriesRequest.playersUrl,
                        importProgressInfo);
            }
        });
        thread.setName("CheckUserProfiles" + importProgressInfo.jobId);
        thread.start();

        return ResponseEntity.ok(importProgressInfo);
    }

    /**
     * Imports entries extracted from html page in Omnipong locaated at fromUrl into tournament with toTournamentId
     *
     * @param importEntriesRequest request data
     * @return
     */
    @PostMapping("/entries")
    public ResponseEntity<ImportProgressInfo> importEntries(@RequestBody ImportEntriesRequest importEntriesRequest,
                                                               HttpSession session) {

        ImportProgressInfo importProgressInfo = new ImportProgressInfo();
        importProgressInfo.phaseName = "Starting player entries import";
        importProgressInfo.jobId = UUID.randomUUID().toString();
        session.setAttribute("importProgressInfo" + importProgressInfo.jobId, importProgressInfo);

        final String currentUserName = UserRolesHelper.getCurrentUsername();
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        Thread thread = new Thread(new Runnable() {
            @Override
            @Transactional
            public void run() {
                log.info("Starting player entries import for " + currentUserName);
                SecurityContextHolder.getContext().setAuthentication(authentication);
                importTournamentService.importEntries(
                        importEntriesRequest.tournamentId,
                        importEntriesRequest.playersUrl,
                        importEntriesRequest.emailsFileRepoPath,
                        importProgressInfo);
            }
        });
        thread.setName("ImportTournament_" + importProgressInfo.jobId);
        thread.start();

        return ResponseEntity.ok(importProgressInfo);
    }

    /**
     * Imports tournaemnt configuration and its events extracted from html page in Omnipong locaated at fromUrl into tournament with toTournamentId
     *
     * @param importTournamentRequest request data
     * @return
     */
    @PostMapping("/configuration")
    public ResponseEntity<ImportProgressInfo> importTournamentConfiguration(@RequestBody ImportTournamentRequest importTournamentRequest,
                                                               HttpSession session) {

        ImportProgressInfo importProgressInfo = new ImportProgressInfo();
        importProgressInfo.phaseName = "Starting tournament configuration import";
        importProgressInfo.jobId = UUID.randomUUID().toString();
        session.setAttribute("importProgressInfo" + importProgressInfo.jobId, importProgressInfo);

        final String currentUserName = UserRolesHelper.getCurrentUsername();
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        Thread thread = new Thread(new Runnable() {
            @Override
            @Transactional
            public void run() {
                log.info("Starting tournament import for " + currentUserName);
                SecurityContextHolder.getContext().setAuthentication(authentication);
                importTournamentService.importTournamentConfiguration(importTournamentRequest, importProgressInfo);
            }
        });
        thread.setName("ImportTournament_" + importProgressInfo.jobId);
        thread.start();

        return ResponseEntity.ok(importProgressInfo);
    }

    /**
     * Imports tournament configuration and its events extracted from blank entry form PDF uploaded to repository
     *
     * @return
     */
    @GetMapping("/configurationfrompdf")
    public ResponseEntity<ImportProgressInfo> importTournamentConfigurationFromPDF(
            @RequestParam String blankEntryFormPdfURI,
            HttpSession session) {

        ImportProgressInfo importProgressInfo = new ImportProgressInfo();
        importProgressInfo.phaseName = "Starting tournament blank entry form import";
        importProgressInfo.jobId = UUID.randomUUID().toString();
        session.setAttribute("importProgressInfo" + importProgressInfo.jobId, importProgressInfo);

        final String currentUserName = UserRolesHelper.getCurrentUsername();
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        Thread thread = new Thread(new Runnable() {
            @Override
            @Transactional
            public void run() {
                log.info("Starting tournament blank entry form for " + currentUserName);
                SecurityContextHolder.getContext().setAuthentication(authentication);
                importTournamentService.importTournamentConfigurationFromPDF(blankEntryFormPdfURI, importProgressInfo);
            }
        });
        thread.setName("ImportTournamentPDF_" + importProgressInfo.jobId);
        thread.start();

        return ResponseEntity.ok(importProgressInfo);
    }

    /**
     * Gets import job status
     *
     * @param jobId   import job id
     * @param session http session
     * @return
     */
    @GetMapping("/status/{jobId}")
    public ResponseEntity<ImportProgressInfo> importTournamentStatus(@PathVariable String jobId,
                                                                     HttpSession session) {

        ImportProgressInfo importProgressInfo = (ImportProgressInfo) session.getAttribute("importProgressInfo" + jobId);
        if (importProgressInfo != null) {
            return ResponseEntity.ok(importProgressInfo);
        } else {
            return ResponseEntity.badRequest().build();
        }
    }
}
