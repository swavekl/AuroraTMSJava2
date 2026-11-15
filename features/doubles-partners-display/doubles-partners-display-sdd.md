# Doubles Partners Display

## Status

Proposed.

## Problem

The tournament player list's Event view displays each event's entrants sorted by individual seed rating. For doubles events, it does not show which partner each player selected or whether that selection is confirmed. A player can enter multiple doubles events and have a different partner in each, so partner information must be scoped to a tournament event. Partner requests can overlap (for example, A selects B while B selects C), so the display must not assign players into potentially conflicting pseudo-teams.

## Goals

- Show every player in each doubles event exactly once, sorted by individual seed rating, highest first.
- Show the player's selected partner and whether a confirmed pair exists.
- Use a two-line layout per doubles player: name and ratings on the first line, partner status on a second line spanning all three table columns.
- Separate each player's two-line entry with a subtle 1 px light gray horizontal rule.
- Keep partner data associated with an event ID so partners in one event cannot affect another event.
- Show the requested partner and confirmed-pair status in each player's individual entry summary.

## Out of scope

- Changing how partner requests are made, accepted, or confirmed.
- Changing event entry, eligibility, rating, or draw behavior.
- Changing the Name, Rating, State, Club, or Team views of the player list.

## Current implementation

The public screen loads tournament entry infos from `GET /publicapi/tournamentplayers/{tournamentId}` and tournament events separately. Authenticated screens use `GET /api/tournamentplayers/{tournamentId}`. Both controllers call `TournamentEntryInfoService.getAllEntryInfosForTournament`, which creates one `TournamentEntryInfo` per tournament entry and fills its `eventIds` from tournament event entries. The response does not currently contain doubles partner or pair data. On the event-list screen, `TournamentPlayersListComponent.categorizeEntriesByEvents` copies each entrant into each event's `players` list, then sorts that list by individual seed rating. The Event view template renders one row per player with seed and eligibility ratings.

The big-format player list also loads through `TournamentEntryInfoService.getAll`, but only uses names, ratings, event IDs, and waiting-list event IDs. It displays players alphabetically and does not need partner data. To avoid expanding its response unnecessarily, partner information is opt-in.

The individual entry view loads event status information from `GET /api/tournamententry/{tournamentEntryId}/eventstatus/list`. `EventEntryStatusService.getEntriesWithStatus` already includes the requested partner profile ID and name on each `TournamentEventEntryInfo` when present. The entry summary currently displays the event name and event day/time; it does not display the partner name or whether a confirmed `DoublesPair` exists.

`TournamentEventEntry.doublesPartnerProfileId` records the partner selected by that player for that specific event. `DoublesPair` records a confirmed pair. Requests and confirmed pairs are event-specific. A one-sided or overlapping request must not be presented as a confirmed team. The display will keep players as individual entries rather than grouping partner requests into teams.

## API contract

Add an optional `includeDoublesPartners` Boolean query parameter to both tournament-player endpoints:

```text
GET /api/tournamentplayers/{tournamentId}?includeDoublesPartners=true
GET /publicapi/tournamentplayers/{tournamentId}?includeDoublesPartners=true
```

The default is `false`, including when the parameter is omitted. When false, leave `doublesPartnerStatuses` null or omit it and do not load partner or `DoublesPair` data. When true, populate `TournamentEntryInfo` with an event-scoped `doublesPartnerStatuses` list. Each item identifies the partner and status for the identified event, whether the pair was formed automatically from mutual selections or manually by a tournament director:

```json
{
  "eventId": 495,
  "partnerProfileId": "00u27f27g5jhQVJv40h8",
  "partnerName": "Pingle, Ashwini",
  "status": "CONFIRMED"
}
```

Example response addition:

```json
"doublesPartnerStatuses": [
  {
    "eventId": 495,
    "partnerProfileId": "00u27f27g5jhQVJv40h8",
    "partnerName": "Pingle, Ashwini",
    "status": "CONFIRMED"
  }
]
```

`status` is `CONFIRMED` when a persisted `DoublesPair` matches this player's event entry and the partner event entry. This includes pairs formed automatically when both players select one another and pairs made manually by tournament staff. `status` is `REQUESTED` when the player selected a partner but no confirmed pair exists. An entrant with neither a confirmed pair nor a request has an empty or null list. Partner profile ID is the identity key; partner name is display support. When `includeDoublesPartners=false`, omit `doublesPartnerStatuses` (or return it as null) and do not perform partner-specific queries.

## Server changes

1. Add the optional `includeDoublesPartners` query parameter, defaulting to false, to both `TournamentEventEntryInfoController` and `PublicPlayerListController`. Pass it to `getAllEntryInfosForTournament`.
2. Add a serializable partner-status summary type and a `doublesPartnerStatuses` property to `TournamentEntryInfo`. Include the event ID, partner profile ID/name, and `status` (`CONFIRMED` or `REQUESTED`).
3. In `getAllEntryInfosForTournament`, when the flag is true, identify the tournament's doubles event IDs. Use event definitions already loaded if available; otherwise query only the doubles event definitions, since confirmed manually made pairs have no partner request IDs. For each event entry, return a confirmed partner status from the persisted `DoublesPair` when present; otherwise, return a requested status from a non-null `doublesPartnerProfileId`. When false, skip partner-specific processing and queries.
4. Resolve partner display names in a batch, reusing profiles already loaded for tournament entrants where practical. Do not issue a query per partner.
5. For each known doubles event, call `DoublesService.findDoublesPairsForEvent(eventId)` once. Search that event's returned pair list in memory and use matched pairs to populate `CONFIRMED` partner statuses for both players, including manually created pairs. If there are no doubles events, skip pair retrieval.
6. If pair state is inconsistent or missing, retain the player's entry and show the selected partner as unconfirmed. Do not group players or infer a pair from reciprocal requests alone.
7. Leave `eventIds` and existing waiting-list/pending-list semantics unchanged. Do not include partner request information for singles or team events.

The private event-entry endpoint is not the source for this public screen and does not need to change for this feature. Reuse `DoublesService.findDoublesPairsForEvent(eventId)` once per known doubles event and match returned pairs in memory. Do not add a lookup by event-entry IDs.

## Individual entry view changes

1. Extend `TournamentEventEntryInfo` with partner status and the effective partner profile ID/name. In `EventEntryStatusService.getEntriesWithStatus`, populate confirmed partner data from persisted pairs first, including manually created pairs; if no pair exists, use the event entry's selected partner request and mark it unconfirmed.
2. For each entered doubles event, call `DoublesService.findDoublesPairsForEvent(eventId)` once and search returned pairs in memory for the current player's event-entry ID. Do not call `findPlayerDoublesEntry` separately for every event entry or add a lookup by event-entry IDs.
3. Reuse the existing `doublesPartnerName` returned by `getEntriesWithStatus`. Do not add a separate partner lookup in the entry-view client.
4. In the individual entry summary, add a third line under every entered doubles event's name and day/time using the consistent format `Partner: ...`. Show `Partner: {partner name} ✓` for confirmed automatic or manual pairs; show `Partner: {partner name} ?` when a partner was requested but no confirmed pair exists; show `Partner: None` when there is neither a pair nor a request. Do not show this line for non-doubles events.
5. The entry-view and Event-list markers have the same meanings: a checkmark means the pair is confirmed, and `?` means the player selected this partner but the pair is not confirmed.

The entry-summary report item currently supports a main line and one sub-line. Extend its display representation to support the additional partner line without changing event names, event times, prices, fee calculations, or other summary rows.

### Entry view text mockup

```text
Events
Open Doubles                         $15.00
Sunday 3:30 PM
Partner: Pingle, Ashwini ✓
U-1800                               $26.00
Sunday 11:30 AM
Partner: Huang, Chi ?
U-2000                               $28.00
Saturday 9:00 AM
```

In this example, Pingle, Ashwini is a confirmed partner and Huang, Chi was requested as a partner but the pair is not confirmed. The singles event has no partner-status line. The entry view's checkmark means confirmed pair; its question mark means a requested but unconfirmed partner.

An entered doubles event with no selected partner shows an explicit status line:

```text
U-1800 Doubles                      $26.00
Sunday 11:30 AM
Partner: None
```

## Client changes

1. Extend the client `TournamentEntryInfo` model and add the partner-status summary type with event ID, partner identity/name, and status (`CONFIRMED` or `REQUESTED`).
2. Extend `TournamentEntryInfoService.getAll` with an optional `includeDoublesPartners` argument defaulting to false, and append the query parameter only when true. The tournament event-list container must opt in. The big-format list container must call `getAll(tournamentId)` without the flag. Other callers retain existing behavior by default.
3. Keep each doubles event's players as individual entries sorted by individual seed rating, descending. Do not group players into teams or duplicate an entry when requests overlap.
4. Render each doubles player's name, seed rating, and eligibility rating on the first row. Add a second row immediately below with one cell spanning all three columns (`colspan="3"`). Display `Partner: {partnerName} ✓` for a confirmed automatic or manual pair, `Partner: {partnerName} ?` for a selected but unconfirmed partner, and `Partner: None` when no pair or request exists.
5. Add a 1 px light gray divider after each player's partner-status row. Use this two-line layout only for doubles events; leave non-doubles event rows unchanged.
6. In this event list, a checkmark means a confirmed pair exists and `?` means that player selected the displayed partner but no confirmed pair exists. Do not infer a team assignment from incoming or overlapping requests.
7. Keep existing event headers and Max / Total / Remaining Spots footer. Add a compact legend to doubles events if needed to explain the symbols; do not show it for non-doubles events. Keep other sort views unchanged.

## Example rendering

```text
Open Doubles
Sunday 3:30 PM
Lam, Spenser          2036   2036
Partner: Pingle, Ashwini [x]
---------------------------------  1 px light gray
Pingle, Ashwini       1743   1743
Partner: Lam, Spenser [x]
---------------------------------  1 px light gray
Iosup, Eric           1705   1705
Partner: Huang, Chi [?]
---------------------------------  1 px light gray
Huang, Chi            1677   1677
Partner: None
---------------------------------  1 px light gray
Arfeen, Shahabul      1072   1072
Partner: None
---------------------------------  1 px light gray
Yaqub, Tahir          1054   1054
Partner: None
---------------------------------  1 px light gray
Max / Total / Remaining Spots: 32 / 7 / 25
```

`[x]` represents a checkmark and `[?]` a question mark. The second line is one table cell spanning the player, seed, and eligibility columns. Each player appears once and is sorted by individual seed rating. Names and ratings are illustrative.

## Edge cases and rules

- A player may select one partner in event 495 and a different partner in event 496; these are independent event-specific statuses.
- A confirmed pair shows the checkmark and partner name on each player's individual entry. A selected but unconfirmed partner shows `?` and the selected partner's name.
- Overlapping selections such as A selects B, B selects C, and C selects no one are rendered as three individual entries. Do not form or sort pseudo-teams from one-sided requests.
- A player with no selected partner shows `Partner: None` and remains in individual seed-rating order.
- A doubles entry without a partner request shows `Partner: None` in the individual entry view.
- If partner data is missing or stale, do not hide event entrants; display players without a valid group as individual rows.
- Each player is rendered exactly once. Confirmed partner names may appear in both players' status rows.
- A doubles event with no partner requests retains individual-rating order; each player has a `Partner: None` status row.
- Non-doubles events retain current individual-player ordering and rendering.
- If partner profile/name resolution fails, use the corresponding event entrant's own record; do not create a duplicate or external player row.

## Verification scenarios

- Every doubles entrant appears once, sorted by individual seed rating, with a second full-width status row and divider.
- Confirmed pairs show `Partner: [name] ✓`; requested but unconfirmed partners show `Partner: [name] ?`; no request shows `Partner: None`.
- Overlapping requests such as A selects B, B selects C, C selects no one do not duplicate players or create pseudo-teams.
- The same player may show different partner statuses in different doubles events.
- Singles events and other player-list views remain unchanged.
- The public response adds the new property without disrupting clients that ignore unknown JSON fields.
- The big-format list calls the endpoint without the flag and receives no partner data; the event-list screen opts in and receives partner data.
- The event-status response includes the confirmed-pair indicator for the individual entry view, populated using a batch lookup.
- The individual entry view shows a third line for each doubles event using `Partner: ...`: a confirmed partner name followed by a checkmark, an unconfirmed requested partner name followed by `?`, or `Partner: None`.
- Server-side data retrieval uses batch reads rather than per-player or per-pair queries.

## Acceptance criteria

- The Event view shows each doubles player once, with partner status in a second row spanning all three columns.
- Both authenticated and public endpoint variants include partner data only when `includeDoublesPartners=true`; omitting the parameter preserves the existing response behavior.
- A compact legend, if included, explains that a checkmark means confirmed and `?` means not confirmed.
- The individual entry view distinguishes confirmed pairs, unconfirmed requests, and no partner selection using one pair-list retrieval per entered doubles event, not per event entry.
- Confirmed status is event-specific and comes from persisted pair data.
- Doubles players remain visible in individual seed-rating order, including players with no partner selection.
- A 1 px light gray divider follows each two-line player entry and is visually subordinate to event headers.
- Existing event counts and non-doubles list behavior are preserved.

