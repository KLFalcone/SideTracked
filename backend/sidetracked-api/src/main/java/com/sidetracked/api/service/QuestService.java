package com.sidetracked.api.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sidetracked.api.model.Quest;
import com.sidetracked.api.repository.QuestRepository;

@Service
public class QuestService {

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

        return questRepository.save(quest);
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