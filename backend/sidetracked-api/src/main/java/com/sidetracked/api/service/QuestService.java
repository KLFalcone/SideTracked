package com.sidetracked.api.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.sidetracked.api.model.Quest;
import com.sidetracked.api.model.QuestCompletion;
import com.sidetracked.api.repository.QuestCompletionRepository;
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
    private final QuestCompletionRepository questCompletionRepository;
    private final PlayerService playerService;
    private final CurrentUserService currentUserService;

    public QuestService(
            QuestRepository questRepository,
            QuestCompletionRepository questCompletionRepository,
            PlayerService playerService,
            CurrentUserService currentUserService) {

        this.questRepository = questRepository;
        this.questCompletionRepository = questCompletionRepository;
        this.playerService = playerService;
        this.currentUserService = currentUserService;
    }

    /*
     * Only the current user's quests are ever returned.
     * Repeating quests come with when they were last done.
     */
    public List<Quest> getAllQuests() {

        List<Quest> quests =
                questRepository.findByUserId(
                        currentUserService.getCurrentUserId()
                );

        List<Long> repeatingIds =
                quests.stream()
                        .filter(Quest::isRepeating)
                        .map(Quest::getId)
                        .toList();

        if (!repeatingIds.isEmpty()) {

            Map<Long, LocalDateTime> lastDone =
                    questCompletionRepository
                            .findByQuestIdIn(repeatingIds)
                            .stream()
                            .collect(Collectors.toMap(
                                    QuestCompletion::getQuestId,
                                    QuestCompletion::getCompletedAt,
                                    (a, b) -> a.isAfter(b) ? a : b
                            ));

            quests.forEach(quest ->
                    quest.setLastCompletedAt(lastDone.get(quest.getId()))
            );
        }

        return quests;
    }

    public Quest createQuest(Quest quest) {

        checkSchedule(quest);

        quest.setUserId(
                currentUserService.getCurrentUserId()
        );

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

    /*
     * A quest either has no schedule, or a complete one where
     * 1 <= window start <= repeat every <= window end <= 168 hours
     * (one week).
     */
    private void checkSchedule(Quest quest) {

        Integer every = quest.getRepeatEveryHours();
        Integer start = quest.getWindowStartHours();
        Integer end = quest.getWindowEndHours();

        if (every == null && start == null && end == null) {
            return;
        }

        boolean valid =
                every != null && start != null && end != null
                        && start >= 1
                        && start <= every
                        && every <= end
                        && end <= 168;

        if (!valid) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "A repeating quest needs: 1 <= window start <= "
                            + "repeat every <= window end <= 168 hours."
            );
        }
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

        if (quest.isRepeating()) {
            return completeRepeatingQuest(quest);
        }

        if (quest.isCompleted()) {
            return quest;
        }

        quest.setCompleted(true);

        Quest savedQuest = questRepository.save(quest);

        QuestCompletion completion =
                questCompletionRepository.save(
                        new QuestCompletion(savedQuest.getId())
                );

        playerService.recordQuestCompleted(
                savedQuest,
                completion
        );

        return savedQuest;
    }

    /*
     * A repeating quest never becomes "completed": each time it's
     * done, a timestamped completion is logged.
     *
     * XP is earned when it's done on time or late. Done early
     * (before the window opens) it is still logged, because for
     * things like medication the log matters most, but earns no XP,
     * so XP can't be farmed by repeated clicks.
     */
    private Quest completeRepeatingQuest(Quest quest) {

        LocalDateTime now = LocalDateTime.now();

        boolean early =
                questCompletionRepository
                        .findTopByQuestIdOrderByCompletedAtDesc(quest.getId())
                        .map(last ->
                                now.isBefore(
                                        last.getCompletedAt().plusHours(
                                                quest.getWindowStartHours()
                                        )
                                )
                        )
                        .orElse(false);

        QuestCompletion completion =
                questCompletionRepository.save(
                        new QuestCompletion(quest.getId())
                );

        if (!early) {

            playerService.recordQuestCompleted(
                    quest,
                    completion
            );
        }

        quest.setLastCompletedAt(completion.getCompletedAt());

        return quest;
    }

    /*
     * Deleting a completed quest takes its XP back. Each reversal
     * is recorded in the XP history rather than erasing the
     * original reward. (Early completions of a repeating quest
     * earned nothing, so there is nothing to take back for them.)
     */
    @Transactional
    public void deleteQuest(Long id) {

        Quest quest = getQuestById(id);

        List<QuestCompletion> completions =
                questCompletionRepository.findByQuestId(
                        quest.getId()
                );

        for (QuestCompletion completion : completions) {

            playerService.recordQuestDeleted(
                    quest,
                    completion
            );
        }

        questCompletionRepository.deleteAll(completions);

        questRepository.delete(quest);
    }

    /*
     * Another user's quest is treated exactly like a quest
     * that doesn't exist, so its existence is never revealed.
     */
    private Quest getQuestById(Long id) {

        return questRepository
                .findByIdAndUserId(
                        id,
                        currentUserService.getCurrentUserId()
                )
                .orElseThrow(
                        () -> new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Quest not found: " + id
                        )
                );
    }
}