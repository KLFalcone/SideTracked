package com.sidetracked.api.service;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sidetracked.api.model.Quest;
import com.sidetracked.api.repository.QuestRepository;

@Service
public class QuestService {

    /*
     * ============================================================
     * XP RULES
     * ============================================================
     *
     * A quest's type alone decides its XP:
     * base XP x the type's multiplier.
     *
     * Example:
     * Boss Fight = 20 x 3 = 60 XP
     */

    private static final int BASE_XP = 20;

    private static final Map<String, Double> TYPE_MULTIPLIERS =
            Map.of(
                    "QUICK WIN", 0.5,
                    "SELF-CARE", 1.0,
                    "CARETAKER", 1.25,
                    "CHORE", 1.5,
                    "MAIN QUEST", 2.0,
                    "BOSS FIGHT", 3.0
            );

    private final QuestRepository questRepository;
    private final PlayerService playerService;

    public QuestService(
            QuestRepository questRepository,
            PlayerService playerService) {

        this.questRepository = questRepository;
        this.playerService = playerService;
    }

    public List<Quest> getAllQuests() {
        return questRepository.findAll();
    }

    public Quest createQuest(Quest quest) {

        quest.setCompleted(false);

        /*
         * XP is always calculated here rather than
         * trusted from the frontend.
         */
        quest.setXp(
                calculateXp(quest.getType())
        );

        return questRepository.save(quest);
    }

    private int calculateXp(String type) {

        double multiplier =
                type == null
                        ? 1.0
                        : TYPE_MULTIPLIERS.getOrDefault(type, 1.0);

        return (int) Math.round(BASE_XP * multiplier);
    }

    public Quest updateQuest(Long id, Quest updatedQuest) {

        Quest quest = getQuestById(id);

        quest.setTitle(updatedQuest.getTitle());
        quest.setDescription(updatedQuest.getDescription());
        quest.setType(updatedQuest.getType());
        quest.setPriority(updatedQuest.getPriority());

        /*
         * XP and completion are intentionally NOT changed here.
         *
         * Completing a quest is a separate operation because
         * completion awards player XP.
         */

        return questRepository.save(quest);
    }

    @Transactional
    public Quest completeQuest(Long id) {

        Quest quest = getQuestById(id);

        if (quest.isCompleted()) {
            return quest;
        }

        quest.setCompleted(true);

        Quest savedQuest = questRepository.save(quest);

        playerService.awardQuestXp(
                savedQuest.getXp()
        );

        return savedQuest;
    }

    @Transactional
    public void deleteQuest(Long id) {

        Quest quest = getQuestById(id);

        if (quest.isCompleted()) {
            playerService.removeQuestXp(
                    quest.getXp()
            );
        }

        questRepository.delete(quest);
    }

    private Quest getQuestById(Long id) {

        return questRepository
                .findById(id)
                .orElseThrow(
                        () -> new IllegalArgumentException(
                                "Quest not found: " + id
                        )
                );
    }
}