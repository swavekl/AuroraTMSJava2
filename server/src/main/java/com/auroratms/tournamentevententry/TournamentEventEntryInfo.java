package com.auroratms.tournamentevententry;

import com.auroratms.event.TournamentEventEntity;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class TournamentEventEntryInfo {
    private Long id; // generated id
    private TournamentEventEntry eventEntry;
    private TournamentEventEntity event;
}
