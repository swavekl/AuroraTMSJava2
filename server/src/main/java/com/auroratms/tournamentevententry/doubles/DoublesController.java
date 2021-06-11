package com.auroratms.tournamentevententry.doubles;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api")
@PreAuthorize("isAuthenticated()")
public class DoublesController {

    @Autowired
    private DoublesService doublesService;

    @GetMapping("/doublespairs")
    @ResponseBody
    @PreAuthorize("hasAuthority('TournamentDirectors') or hasAuthority('Admins')")
    public ResponseEntity<List<DoublesPair>> getAllDoublesPairs(@RequestParam Long eventId) {
        List<DoublesPair> doublesPairList = doublesService.findDoublesPairsForEvent(eventId);
        return new ResponseEntity<>(doublesPairList, HttpStatus.OK);
    }

    @GetMapping("/doublespairinfos")
    @ResponseBody
    @PreAuthorize("hasAuthority('TournamentDirectors') or hasAuthority('Admins')")
    public ResponseEntity<List<DoublesPairInfo>> getAllDoublesPairInfos(@RequestParam Long eventId) {
        List<DoublesPairInfo> doublesPairInfos = doublesService.getInfosForDoublesEvent(eventId);
        return new ResponseEntity<>(doublesPairInfos, HttpStatus.OK);
    }

    @PostMapping("/doublespair")
    @ResponseBody
    @PreAuthorize("hasAuthority('TournamentDirectors') or hasAuthority('Admins')")
    public ResponseEntity<DoublesPair> create(@RequestBody DoublesPair doublesPair) {
        DoublesPair savedDoublesPair = doublesService.save(doublesPair);
        return new ResponseEntity<>(savedDoublesPair, HttpStatus.OK);
    }

    @DeleteMapping("/doublespair/{doublesPairId}")
    @PreAuthorize("hasAuthority('TournamentDirectors') or hasAuthority('Admins')")
    public void delete(@PathVariable Long doublesPairId) {
        doublesService.deletePair(doublesPairId);
    }
}
