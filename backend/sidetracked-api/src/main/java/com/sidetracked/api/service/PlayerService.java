package com.sidetracked.api.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sidetracked.api.model.Player;
import com.sidetracked.api.model.Quest;
import com.sidetracked.api.model.QuestCompletion;
import com.sidetracked.api.model.UserRabbitHole;
import com.sidetracked.api.model.XpEvent;
import com.sidetracked.api.model.XpSourceType;
import com.sidetracked.api.repository.PlayerRepository;
import com.sidetracked.api.repository.XpEventRepository;

/*
 * The current user's progression.
 *
 * Every XP change goes through record(), which writes an XpEvent
 * and updates the player's totals in the same transaction. If the
 * same completion is ever recorded twice, the database rejects
 * the second event and the whole change is rolled back.
 */
@Service
public class PlayerService {

    private final PlayerRepository playerRepository;
    private final XpEventRepository xpEventRepository;
    private final CurrentUserService currentUserService;

    public PlayerService(
            PlayerRepository playerRepository,
            XpEventRepository xpEventRepository,
            CurrentUserService currentUserService) {

        this.playerRepository = playerRepository;
        this.xpEventRepository = xpEventRepository;
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
        player.setRabbitHolesCompleted(0);

        return playerRepository.save(player);
    }

    /*
     * The current user's XP history, newest first.
     */
    public List<XpEvent> getXpHistory() {

        return xpEventRepository
                .findByUserIdOrderByCreatedAtDescIdDesc(
                        currentUserService.getCurrentUserId()
                );
    }


    /*
     * ============================================================
     * XP CHANGES
     * ============================================================
     */

    @Transactional
    public Player recordQuestCompleted(
            Quest quest,
            QuestCompletion completion) {

        Player player = record(
                XpSourceType.QUEST_COMPLETED,
                completion.getId(),
                quest.getXp(),
                quest.getTitle()
        );

        player.setQuestsCompleted(
                player.getQuestsCompleted() + 1
        );

        return player;
    }

    @Transactional
    public Player recordQuestDeleted(
            Quest quest,
            QuestCompletion completion) {

        Player player = record(
                XpSourceType.QUEST_DELETED,
                completion.getId(),
                -quest.getXp(),
                quest.getTitle()
        );

        player.setQuestsCompleted(
                Math.max(0, player.getQuestsCompleted() - 1)
        );

        return player;
    }

    @Transactional
    public Player recordRabbitHoleCompleted(
            UserRabbitHole rabbitHole) {

        Player player = record(
                XpSourceType.RABBIT_HOLE_COMPLETED,
                rabbitHole.getId(),
                rabbitHole.getTopic().getXp(),
                rabbitHole.getTopic().getTitle()
        );

        player.setRabbitHolesCompleted(
                player.getRabbitHolesCompleted() + 1
        );

        return player;
    }

    private Player record(
            XpSourceType sourceType,
            Long sourceId,
            int amount,
            String label) {

        Player player = getPlayer();

        String readableLabel =
                label == null || label.isBlank()
                        ? "Untitled"
                        : label.trim();

        xpEventRepository.saveAndFlush(
                new XpEvent(
                        player.getUserId(),
                        sourceType,
                        sourceId,
                        amount,
                        readableLabel.length() > 255
                                ? readableLabel.substring(0, 255)
                                : readableLabel
                )
        );

        player.setXp(player.getXp() + amount);

        return playerRepository.save(player);
    }
}
