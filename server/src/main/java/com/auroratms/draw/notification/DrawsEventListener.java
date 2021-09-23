package com.auroratms.draw.notification;

import com.auroratms.draw.notification.event.DrawsEvent;
import com.auroratms.match.MatchCardService;
import com.auroratms.notification.SystemPrincipalExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class DrawsEventListener {

    private static final Logger logger = LoggerFactory.getLogger(DrawsEventListener.class);

    @Autowired
    private MatchCardService matchCardService;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleEvent(DrawsEvent drawsEvent) {
        // run this task as system principal so we have access to various services
        SystemPrincipalExecutor task = new SystemPrincipalExecutor() {
            @Override
            @Transactional
            protected void taskBody() {
                processEvent(drawsEvent);
            }
        };
        task.execute();
    }

    private void processEvent(DrawsEvent drawsEvent) {
        switch (drawsEvent.getAction()) {
            case GENERATED:
                this.matchCardService.deleteAllForEventAndDrawType(drawsEvent.getEventId(), drawsEvent.getDrawType());
                this.matchCardService.generateMatchCardsForEvent(drawsEvent.getEventId(), drawsEvent.getDrawType());
                break;

            case UPDATED:
                this.matchCardService.updateMatchCardsForEvent(drawsEvent.getEventId(), drawsEvent.getDrawType(), drawsEvent.getUpdatedItems());
                break;

            case DELETED:
                this.matchCardService.deleteAllForEventAndDrawType(drawsEvent.getEventId(), drawsEvent.getDrawType());
                break;
        }
    }
}
