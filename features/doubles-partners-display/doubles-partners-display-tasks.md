# Doubles Partners Display: Task List

Track implementation against the [SDD](doubles-partners-display-sdd.md) and [implementation plan](doubles-partners-display-implementation-plan.md).

## Server response and data access

- [ ] Add partner-status DTO and optional partner status data to server `TournamentEntryInfo`.
- [ ] Add optional `includeDoublesPartners=false` parameter to authenticated and public tournament-player endpoints.
- [ ] Populate event-specific partner status only when `includeDoublesPartners=true`.
- [ ] Load confirmed `DoublesPair` rows once per doubles event and include manually formed pairs in partner status.
- [ ] Resolve partner names without per-partner profile queries.
- [ ] Add partner status and effective partner identity/name to `TournamentEventEntryInfo`.
- [ ] Identify doubles event IDs, then call `DoublesService.findDoublesPairsForEvent(eventId)` once per event and search results in memory. Do not add a lookup by event-entry IDs.
- [ ] Populate confirmed pair status first, then pending request status, in `EventEntryStatusService.getEntriesWithStatus`.
- [ ] Confirm entry-status partner-name lookups do not introduce avoidable N+1 queries.

## Event-list client

- [ ] Add partner request types to the client tournament-entry info model.
- [ ] Add optional `includeDoublesPartners` argument to `TournamentEntryInfoService.getAll`, default false.
- [ ] Make the event-list container opt in to partner data.
- [ ] Keep the big-format player list and other callers on the default false behavior.
- [ ] Keep one row entry per player and sort all doubles entrants by individual seed rating.
- [ ] Render player name and seed/eligibility ratings on the first row.
- [ ] Render a second status row with `colspan="3"`: `Partner: [name] ✓`, `Partner: [name] ?`, or `Partner: None`.
- [ ] Add a thin light gray divider after each player's two-line entry.
- [ ] Preserve singles rendering, non-event sort views, and visibility of players with no partner selection.

## Individual entry view

- [ ] Add the confirmed-pair field to the client event-entry info model.
- [ ] Extend the summary item/template to render a third line under event name and time.
- [ ] Render `Partner: [name]` plus a checkmark for confirmed pairs.
- [ ] Render `Partner: [name] ?` for requested but unconfirmed partners.
- [ ] Render `Partner: None` for doubles entries without a partner request.
- [ ] Keep the partner status in a third line below the event name and time.
- [ ] Suppress partner status on singles/team events and preserve fees and totals.

## Verification

- [ ] Verify authenticated and public endpoint behavior with the flag true, false, and omitted.
- [ ] Verify the big-format list does not request or receive populated partner data.
- [ ] Verify different partners in different events remain independent.
- [ ] Verify confirmed, requested-but-unconfirmed, and no-request statuses in both screens.
- [ ] Verify manually created `DoublesPair` records display confirmed partner names even when neither event entry has a partner request ID.
- [ ] Verify overlapping requests (A selects B, B selects C, C selects none) show each player once with the correct status and no pseudo-teams.
- [ ] Verify batch data access and absence of N+1 pair/profile reads.
- [ ] Verify existing event counts, waiting-list information, sort views, and non-doubles behavior.
