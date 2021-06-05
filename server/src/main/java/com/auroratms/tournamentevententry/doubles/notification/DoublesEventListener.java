package com.auroratms.tournamentevententry.doubles.notification;

import com.auroratms.notification.SystemPrincipalExecutor;
import com.auroratms.tournamentevententry.doubles.DoublesService;
import com.auroratms.tournamentevententry.doubles.notification.event.MakeBreakDoublesPairsEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.List;

/**
 * Listener for events used to create or break up doubles pairs
 */
@Component
public class DoublesEventListener {

    private static final Logger logger = LoggerFactory.getLogger(DoublesEventListener.class);

    @Autowired
    private DoublesService doublesService;


    @Async
    @TransactionalEventListener(phase= TransactionPhase.AFTER_COMMIT)
    public void handleEvent(MakeBreakDoublesPairsEvent event) {
        // run this task as system principal so we have access to various services
        SystemPrincipalExecutor task = new SystemPrincipalExecutor() {
            @Override
            @Transactional
            protected void taskBody() {
                makeBreakPairs(event);
            }
        };
        task.execute();
    }

    private void makeBreakPairs(MakeBreakDoublesPairsEvent event) {
        // if any events were withdrawn then break up pairs
        List<MakeBreakDoublesPairsEvent.DoublesEventEntryWithdrawal> entryWithdrawalList = event.getEntryWithdrawalList();
        for (MakeBreakDoublesPairsEvent.DoublesEventEntryWithdrawal doublesEventEntryWithdrawal: entryWithdrawalList) {
            if (doublesEventEntryWithdrawal.isWithdrawal()) {
                doublesService.breakUpPair(event.getTournamentEntryId(), doublesEventEntryWithdrawal.getEventFk(),
                        doublesEventEntryWithdrawal.getEventEntryFk());
            } else {
                doublesService.makeRequestedPair(event.getTournamentEntryId(), doublesEventEntryWithdrawal.getEventFk(),
                        doublesEventEntryWithdrawal.getEventEntryFk());
            }
        }
    }
}
