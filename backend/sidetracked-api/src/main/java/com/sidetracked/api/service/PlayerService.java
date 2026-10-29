package com.sidetracked.api.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sidetracked.api.model.Player;
import com.sidetracked.api.repository.PlayerRepository;

@Service
public class PlayerService {

    private final PlayerRepository playerRepository;
    private final CurrentUserService currentUserService;

    public PlayerService(
            PlayerRepository playerRepository,
            CurrentUserService currentUserService) {

        this.playerRepository = playerRepository;
        this.currentUserService = currentUserService;
    }

    /*
     * The current user's game profile.
     * A user without one yet starts fresh at 0 XP.
     */
    @Transactional
    public Player getPlayer() {

        Long userId =
                currentUserService.getCurrentUserId();

        return playerRepository
                .findByUserId(userId)
                .orElseGet(() -> createPlayer(userId));
    }

    private Player createPlayer(Long userId) {

        Player player = new Player();

        player.setUserId(userId);
        player.setXp(0);
        player.setQuestsCompleted(0);
        player.setEncountersCompleted(0);

        return playerRepository.save(player);
    }

    @Transactional
    public Player awardQuestXp(int xp) {

        Player player = getPlayer();

        player.setXp(player.getXp() + xp);

        player.setQuestsCompleted(
                player.getQuestsCompleted() + 1
        );

        return playerRepository.save(player);
    }

    @Transactional
    public Player removeQuestXp(int xp) {

        Player player = getPlayer();

        player.setXp(
                Math.max(0, player.getXp() - xp)
        );

        player.setQuestsCompleted(
                Math.max(0, player.getQuestsCompleted() - 1)
        );

        return playerRepository.save(player);
    }

    @Transactional
    public Player awardEncounterXp(int xp) {

        Player player = getPlayer();

        player.setXp(player.getXp() + xp);

        player.setEncountersCompleted(
                player.getEncountersCompleted() + 1
        );

        return playerRepository.save(player);
    }
}