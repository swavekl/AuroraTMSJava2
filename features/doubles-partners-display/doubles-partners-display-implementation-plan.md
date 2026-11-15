# Doubles Partners Display: Implementation Plan

## Purpose

Turn the [Doubles Partners Display SDD](doubles-partners-display-sdd.md) into ordered implementation increments covering the tournament Event list and individual entry view. Keep the big-format player list free of unused partner data.

## Implementation boundaries

- `TournamentEntryInfoService.getAllEntryInfosForTournament` supplies partner-status data only when `includeDoublesPartners=true`.
- Both authenticated `/api/tournamentplayers/{tournamentId}` and public `/publicapi/tournamentplayers/{tournamentId}` endpoints accept the optional flag, defaulting to false.
- The event-list client opts in; the big-format list and other callers keep the default.
- `TournamentEventEntry.doublesPartnerProfileId` describes the player's event-specific request. A persisted `DoublesPair` is the source of truth for confirmed pairs, whether created automatically from reciprocal selections or manually by a tournament director.
- `EventEntryStatusService.getEntriesWithStatus` supplies confirmation status for the individual entry view by calling `DoublesService.findDoublesPairsForEvent(eventId)` once per relevant doubles event and searching each returned pair list in memory.
- In both the event list and entry view, a checkmark means a confirmed pair, `?` means the player selected that partner but no pair is confirmed, and `None` means neither a pair nor a request exists.

## Phase 1 — Server response contracts and batch data access

1. Add a partner-status DTO and an optional `doublesPartnerStatuses` property to server `TournamentEntryInfo`. Include `eventId`, partner profile ID/name, and a `CONFIRMED` or `REQUESTED` status.
2. Add `includeDoublesPartners=false` as a request parameter to `TournamentEventEntryInfoController` and `PublicPlayerListController`; pass the flag through to `TournamentEntryInfoService`.
3. In `getAllEntryInfosForTournament`, preserve existing behavior when the flag is false and skip partner-specific processing. When true, obtain doubles event IDs from already-loaded definitions or call the existing doubles-event listing once. This is needed to include manually formed pairs that have no partner request ID. If no doubles events exist, skip pair retrieval.
4. For each doubles event, call `DoublesService.findDoublesPairsForEvent(eventId)` once and index the results in memory by both event-entry IDs. Add `CONFIRMED` statuses from these pairs for both players, including manually paired players. For event entries without a confirmed pair, add a `REQUESTED` status from a non-null `doublesPartnerProfileId`. Do not query once per player or event entry.
5. Resolve partner names in a batch, reusing already-loaded tournament player profiles where possible. Avoid per-player, per-event, or per-partner queries.
6. Add partner status and effective partner profile ID/name to `TournamentEventEntryInfo`. In `EventEntryStatusService.getEntriesWithStatus`, use the already-loaded event definitions to find entered doubles events; if none are doubles, skip pair retrieval. Call `DoublesService.findDoublesPairsForEvent(eventId)` once for each relevant event and match returned pairs against the current player's event-entry ID. This includes manual pairs. Do not query once per event entry.
7. Keep existing `doublesPartnerName` population for entry status. Review its profile lookup behavior and batch it if the implementation would otherwise add or retain avoidable per-entry lookups in this path.

**Completion gate:** both API paths can provide event-specific request and confirmation data when requested, and entry status provides confirmed-pair state through batch reads. Omitting the list flag leaves the existing response behavior and cost unchanged.

## Phase 2 — Client models and event-list rendering

1. Add typed partner-status data to the tournament entry info client model and the event-status model.
2. Extend `TournamentEntryInfoService.getAll` with an optional `includeDoublesPartners` argument defaulting to false. Append the query parameter only when true.
3. Update the event-list container to call `getAll(tournamentId, true)`. Leave the big-format list call as `getAll(tournamentId)` and verify all other callers retain the default.
4. Keep one player entry per entrant and sort all entrants by individual seed rating descending. Do not build partner groups, since requests can overlap (A selects B, B selects C).
5. Render each doubles entrant's name and individual seed/eligibility ratings in the existing three columns on the first row. Render partner status in a second row with one cell spanning all three columns (`colspan="3"`): `Partner: [name] ✓` when confirmed, `Partner: [name] ?` when requested but unconfirmed, or `Partner: None` when no partner was selected.
6. Add a 1 px light gray divider after the partner-status row for each doubles entrant. Leave non-doubles events on their current single-row layout.
7. Keep the existing event header and Max / Total / Remaining Spots footer. If a legend is needed, explain that the checkmark means confirmed and `?` means not confirmed. Do not add partner status to non-doubles events.

**Completion gate:** the Event view shows every doubles entrant once in individual-rating order, with partner status in a full-width second row and a divider after each player, while other sort modes and the big-format player list remain unchanged.

## Phase 3 — Individual entry view

1. Extend the entry summary report item and template to support the third event line while preserving event name, time, price, and existing summary rows.
2. For each entered doubles event, render `Partner: [name]` followed by a checkmark for confirmed automatic or manual pairs, `Partner: [name] ?` if a partner was requested but no pair is confirmed, or `Partner: None` if no pair or request exists.
3. Do not show partner status for singles or team events. Keep fee calculations and entry totals unchanged.
4. Ensure status comes from the entry-status API response and partner name uses its existing `doublesPartnerName`; do not add a separate client request.

**Completion gate:** the entry summary consistently shows confirmed, requested-but-unconfirmed, or no-partner status for doubles events.

## Phase 4 — Verification and compatibility

1. Verify both controller routes accept true, false, and omitted `includeDoublesPartners` values; only true returns partner data and triggers partner-specific retrieval.
2. Verify a player with different partners in two doubles events shows the event-specific partner on each individual entry.
3. Verify confirmed, requested-but-unconfirmed, and no-partner statuses display correctly; overlapping requests do not group or duplicate players.
4. Verify entry-view status differentiates persisted pair, requested-only partner, and no request, including multiple doubles events in one entry.
5. Verify event list behavior for singles, existing sort modes, event totals, and spot footer; verify big-format list behavior and payload remain unaffected by partner data.
6. Verify one pair-list retrieval per doubles event and batch partner-name resolution; avoid per-player or per-event-entry pair queries.

**Completion gate:** requested and confirmed states render correctly in both views, legacy callers remain compatible, and partner lookups are batched.

## Dependencies and sequencing

- Complete Phase 1 before client integration so both clients can rely on stable response fields.
- Phase 2 and Phase 3 can proceed independently after their respective response contracts are available.
- Complete Phase 4 after both UIs are integrated.
- No schema migration is expected: requests already live on `TournamentEventEntry` and confirmed teams already live in `DoublesPair`.

## Key implementation areas

- `server/src/main/java/com/auroratms/tournamententry/TournamentEntryInfo.java`
- `server/src/main/java/com/auroratms/tournamententry/TournamentEntryInfoService.java`
- `server/src/main/java/com/auroratms/tournamententry/TournamentEventEntryInfoController.java`
- `server/src/main/java/com/auroratms/tournamententry/PublicPlayerListController.java`
- `server/src/main/java/com/auroratms/tournamentevententry/EventEntryStatusService.java`
- `server/src/main/java/com/auroratms/tournamentevententry/TournamentEventEntryInfo.java`
- `server/src/main/java/com/auroratms/tournamentevententry/doubles/DoublesService.java`
- `server/src/main/java/com/auroratms/tournamentevententry/doubles/DoublesRepository.java`
- `client/src/app/tournament/service/tournament-entry-info.service.ts`
- `client/src/app/tournament/model/tournament-entry-info.model.ts`
- `client/src/app/tournament/tournament/tournament-players-list/`
- `client/src/app/tournament/tournament/tournament-players-list-big/`
- `client/src/app/tournament/tournament-entry/entry-view/`
- `client/src/app/tournament/tournament-entry/pricecalculator/`

## Open implementation checks

- Identify doubles events from already-loaded definitions or one doubles-event listing call when partner data is requested. Do not infer candidate events solely from partner requests, because manually formed pairs may have no request IDs. Call `findDoublesPairsForEvent` once per doubles event and match pairs in memory.
- Confirm the status semantics for pending, waiting-list, or pending-deletion event entries. Only entries represented as active doubles entries should receive a pair-confirmed state.
- Keep DTO naming and null serialization aligned between the two endpoint variants.
