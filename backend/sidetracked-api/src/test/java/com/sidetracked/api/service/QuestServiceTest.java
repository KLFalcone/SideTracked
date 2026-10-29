package com.sidetracked.api.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

import com.sidetracked.api.model.Quest;
import com.sidetracked.api.model.QuestCompletion;
import com.sidetracked.api.model.Times;
import com.sidetracked.api.repository.QuestCompletionRepository;
import com.sidetracked.api.repository.QuestRepository;

/*
 * Repeating quest rules, tested without a database.
 */
class QuestServiceTest {

    private static final Long USER_ID = 1L;
    private static final Long QUEST_ID = 7L;

    private QuestRepository questRepository;
    private QuestCompletionRepository completionRepository;
    private PlayerService playerService;
    private QuestService service;

    @BeforeEach
    void setUp() {

        questRepository = mock(QuestRepository.class);
        completionRepository = mock(QuestCompletionRepository.class);
        playerService = mock(PlayerService.class);

        CurrentUserService currentUserService =
                mock(CurrentUserService.class);

        when(currentUserService.getCurrentUserId())
                .thenReturn(USER_ID);

        when(questRepository.save(any(Quest.class)))
                .thenAnswer(call -> call.getArgument(0));

        when(completionRepository.save(any(QuestCompletion.class)))
                .thenAnswer(call -> call.getArgument(0));

        service = new QuestService(
                questRepository,
                completionRepository,
                playerService,
                currentUserService
        );
    }

    @Test
    void theFirstDoseEarnsXp() {

        Quest medication = medicationQuest();

        noPreviousDose();

        service.completeQuest(QUEST_ID);

        verify(playerService).recordQuestCompleted(any(), any());
        assertThat(medication.isCompleted()).isFalse();
        assertThat(medication.getLastCompletedAt()).isNotNull();
    }

    @Test
    void aDoseInsideTheWindowEarnsXp() {

        medicationQuest();

        lastDoseHoursAgo(7);

        service.completeQuest(QUEST_ID);

        verify(playerService).recordQuestCompleted(any(), any());
    }

    @Test
    void aLateDoseStillEarnsXp() {

        medicationQuest();

        lastDoseHoursAgo(12);

        service.completeQuest(QUEST_ID);

        verify(playerService).recordQuestCompleted(any(), any());
    }

    @Test
    void anEarlyDoseIsLoggedButEarnsNoXp() {

        medicationQuest();

        lastDoseHoursAgo(2);

        service.completeQuest(QUEST_ID);

        verify(completionRepository).save(any(QuestCompletion.class));
        verify(playerService, never()).recordQuestCompleted(any(), any());
    }

    @Test
    void undoRemovesTheLastCompletionAndTakesBackItsXp() {

        Quest medication = medicationQuest();

        QuestCompletion previous = completionHoursAgo(9);
        QuestCompletion mistake = completionHoursAgo(0);

        when(completionRepository
                .findTopByQuestIdOrderByCompletedAtDesc(QUEST_ID))
                .thenReturn(Optional.of(mistake))
                .thenReturn(Optional.of(previous));

        service.undoLastCompletion(QUEST_ID);

        verify(playerService).recordCompletionUndone(medication, mistake);
        verify(completionRepository).delete(mistake);

        assertThat(medication.getLastCompletedAt())
                .isEqualTo(Times.toInstant(previous.getCompletedAt()));
    }

    @Test
    void undoWithNothingToUndoIsNotFound() {

        medicationQuest();
        noPreviousDose();

        assertThatThrownBy(() -> service.undoLastCompletion(QUEST_ID))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("404");
    }

    @Test
    void aScheduleMustBeCompleteAndInOrder() {

        Quest quest = new Quest();
        quest.setTitle("medicate nux");
        quest.setRepeatEveryHours(8);
        quest.setWindowStartHours(10);
        quest.setWindowEndHours(6);

        assertThatThrownBy(() -> service.createQuest(quest))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("400");

        Quest partial = new Quest();
        partial.setTitle("medicate nux");
        partial.setRepeatEveryHours(8);

        assertThatThrownBy(() -> service.createQuest(partial))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("400");
    }

    @Test
    void aValidScheduleIsAccepted() {

        Quest quest = new Quest();
        quest.setTitle("medicate nux");
        quest.setType("CARETAKER");
        quest.setRepeatEveryHours(8);
        quest.setWindowStartHours(6);
        quest.setWindowEndHours(10);

        Quest saved = service.createQuest(quest);

        assertThat(saved.isRepeating()).isTrue();
        assertThat(saved.getXp()).isEqualTo(25);
        assertThat(saved.getUserId()).isEqualTo(USER_ID);
    }


    private Quest medicationQuest() {

        Quest quest = new Quest();

        ReflectionTestUtils.setField(quest, "id", QUEST_ID);

        quest.setUserId(USER_ID);
        quest.setTitle("medicate nux");
        quest.setXp(25);
        quest.setRepeatEveryHours(8);
        quest.setWindowStartHours(6);
        quest.setWindowEndHours(10);

        when(questRepository.findByIdAndUserId(QUEST_ID, USER_ID))
                .thenReturn(Optional.of(quest));

        return quest;
    }

    private void noPreviousDose() {

        when(completionRepository
                .findTopByQuestIdOrderByCompletedAtDesc(QUEST_ID))
                .thenReturn(Optional.empty());
    }

    private QuestCompletion completionHoursAgo(int hours) {

        QuestCompletion completion = new QuestCompletion(QUEST_ID);

        ReflectionTestUtils.setField(
                completion,
                "completedAt",
                Times.nowUtc().minusHours(hours)
        );

        return completion;
    }

    private void lastDoseHoursAgo(int hours) {

        QuestCompletion last = completionHoursAgo(hours);

        when(completionRepository
                .findTopByQuestIdOrderByCompletedAtDesc(QUEST_ID))
                .thenReturn(Optional.of(last));
    }
}
