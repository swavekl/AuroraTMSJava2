package com.auroratms.tournament;

class TournamentNotFoundException extends RuntimeException {

    TournamentNotFoundException(Long id) {
        super("Could not find tournament " + id);
    }
}
