package com.sidetracked.api.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sidetracked.api.model.Player;
import com.sidetracked.api.model.Quest;
import com.sidetracked.api.repository.PlayerRepository;
import com.sidetracked.api.repository.QuestRepository;

@Service
public class PlayerService {

    private final PlayerRepository playerRepository;
    private final QuestRepository questRepository;

    public PlayerService(
            PlayerRepository playerRepository,
            QuestRepository questRepository) {

        this.playerRepository = playerRepository;
        this.questRepository = questRepository;
    }

    public Player getPlayer() {

        List<Player> players = playerRepository.findAll();

        if (!players.isEmpty()) {
            return players.get(0);
        }

        return createPlayerFromExistingQuests();
    }

    private Player createPlayerFromExistingQuests() {

        List<Quest> quests = questRepository.findAll();

        int existingXp = quests.stream()
                .filter(Quest::isCompleted)
                .mapToInt(Quest::getXp)
                .sum();

        int completedQuestCount = (int) quests.stream()
                .filter(Quest::isCompleted)
                .count();

        Player player = new Player();

        player.setXp(existingXp);
        player.setQuestsCompleted(completedQuestCount);
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