package com.sidetracked.api.controller;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sidetracked.api.model.Player;
import com.sidetracked.api.service.PlayerService;

@RestController
@RequestMapping("/api/player")
@CrossOrigin(origins = "http://localhost:4200")
public class PlayerController {

    private final PlayerService playerService;

    public PlayerController(
            PlayerService playerService) {

        this.playerService = playerService;
    }

    @GetMapping
    public Player getPlayer() {
        return playerService.getPlayer();
    }

    @PostMapping("/encounters/{xp}/complete")
    public Player completeEncounter(
            @PathVariable int xp) {

        return playerService.awardEncounterXp(xp);
    }
}