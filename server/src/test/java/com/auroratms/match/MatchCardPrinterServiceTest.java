package com.auroratms.match;

import com.auroratms.AbstractServiceTest;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertTrue;

@Transactional
public class MatchCardPrinterServiceTest extends AbstractServiceTest {

    @Autowired
    private MatchCardPrinterService matchCardPrinterService;

    @Test
    public void testSingleEventPagePDF () {
        long matchId = 463;
        String matchCardAsPDF = matchCardPrinterService.getMatchCardAsPDF(matchId);
        System.out.println("matchCardAsPDF = " + matchCardAsPDF);
        File checkFile = new File(matchCardAsPDF);
        assertTrue("PDF doesn't exist at " + matchCardAsPDF, checkFile.exists());
    }

    @Test
    public void testSingleEliminationRoundPagePDF () {
        long matchId = 405;
        String matchCardAsPDF = matchCardPrinterService.getMatchCardAsPDF(matchId);
        System.out.println("matchCardAsPDF = " + matchCardAsPDF);
        File checkFile = new File(matchCardAsPDF);
        assertTrue("PDF doesn't exist at " + matchCardAsPDF, checkFile.exists());
    }

    @Test
    public void testDoublesEventPagePDF () {
        long matchId = 422;
        String matchCardAsPDF = matchCardPrinterService.getMatchCardAsPDF(matchId);
        System.out.println("matchCardAsPDF = " + matchCardAsPDF);
        File checkFile = new File(matchCardAsPDF);
        assertTrue("PDF doesn't exist at " + matchCardAsPDF, checkFile.exists());
    }

    @Test
    public void testMergingDocuments() {
        List<Long> matchCardIdsList = new ArrayList<>();
        matchCardIdsList.add(2925L);
        matchCardIdsList.add(2926L);
        matchCardIdsList.add(2927L);
        String matchCardsAsPDF = matchCardPrinterService.getMultipleMatchCardsAsPDF(matchCardIdsList);
        System.out.println("matchCardsAsPDF = " + matchCardsAsPDF);
        File checkFile = new File(matchCardsAsPDF);
        assertTrue("PDF doesn't exist at " + matchCardsAsPDF, checkFile.exists());
    }
}

