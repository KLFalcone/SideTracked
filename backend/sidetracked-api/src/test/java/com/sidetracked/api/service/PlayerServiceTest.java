package com.sidetracked.api.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.util.ReflectionTestUtils;

import com.sidetracked.api.model.Player;
import com.sidetracked.api.model.Quest;
import com.sidetracked.api.model.QuestCompletion;
import com.sidetracked.api.model.XpEvent;
import com.sidetracked.api.model.XpSourceType;
import com.sidetracked.api.repository.PlayerRepository;
import com.sidetracked.api.repository.XpEventRepository;

/*
 * XP history rules, tested without a database. The fake event
 * store rejects a duplicate (user, sourceType, sourceId) the same
 * way the real unique constraint does.
 */
class PlayerServiceTest {

    private static final Long USER_ID = 1L;

    private final List<XpEvent> events = new ArrayList<>();
    private Player player;
    private PlayerService service;

    @BeforeEach
    void setUp() {

        player = new Player();
        player.setUserId(USER_ID);

        PlayerRepository playerRepository =
                mock(PlayerRepository.class);

        when(playerRepository.findByUserId(USER_ID))
                .thenReturn(Optional.of(player));

        when(playerRepository.save(any(Player.class)))
                .thenAnswer(call -> call.getArgument(0));

        XpEventRepository xpEventRepository =
                mock(XpEventRepository.class);

        when(xpEventRepository.saveAndFlush(any(XpEvent.class)))
                .thenAnswer(call -> {

                    XpEvent event = call.getArgument(0);

                    boolean duplicate = events.stream().anyMatch(e ->
                            e.getUserId().equals(event.getUserId())
                                    && e.getSourceType() == event.getSourceType()
                                    && e.getSourceId().equals(event.getSourceId())
                    );

                    if (duplicate) {
                        throw new DataIntegrityViolationException(
                                "uk_xp_events_source"
                        );
                    }

                    events.add(event);

                    return event;
                });

        when(xpEventRepository.existsByUserIdAndSourceTypeAndSourceId(
                any(), any(), any()))
                .thenAnswer(call -> events.stream().anyMatch(e ->
                        e.getUserId().equals(call.getArgument(0))
                                && e.getSourceType() == call.getArgument(1)
                                && e.getSourceId().equals(call.getArgument(2))
                ));

        CurrentUserService currentUserService =
                mock(CurrentUserService.class);

        when(currentUserService.getCurrentUserId())
                .thenReturn(USER_ID);

        service = new PlayerService(
                playerRepository,
                xpEventRepository,
                currentUserService
        );
    }

    @Test
    void completingAQuestRecordsTimestampedXp() {

        service.recordQuestCompleted(
                quest("medicate nux", 20),
                completion(7L)
        );

        assertThat(player.getXp()).isEqualTo(20);
        assertThat(player.getQuestsCompleted()).isEqualTo(1);

        assertThat(events).singleElement().satisfies(event -> {
            assertThat(event.getSourceType())
                    .isEqualTo(XpSourceType.QUEST_COMPLETED);
            assertThat(event.getAmount()).isEqualTo(20);
            assertThat(event.getLabel()).isEqualTo("medicate nux");
            assertThat(event.getCreatedAt()).isNotNull();
        });
    }

    @Test
    void theSameCompletionCannotPayTwice() {

        Quest quest = quest("clean sink", 30);
        QuestCompletion completion = completion(8L);

        service.recordQuestCompleted(quest, completion);

        assertThatThrownBy(() ->
                service.recordQuestCompleted(quest, completion)
        ).isInstanceOf(DataIntegrityViolationException.class);

        assertThat(events).hasSize(1);
    }

    @Test
    void eachRepeatOfAQuestEarnsItsOwnXp() {

        Quest medication = quest("medicate nux", 20);

        service.recordQuestCompleted(medication, completion(1L));
        service.recordQuestCompleted(medication, completion(2L));
        service.recordQuestCompleted(medication, completion(3L));

        assertThat(player.getXp()).isEqualTo(60);
        assertThat(events).hasSize(3);
    }

    @Test
    void deletingACompletedQuestIsRecordedAsANegativeLine() {

        Quest quest = quest("kiss gbert's little head", 20);
        QuestCompletion completion = completion(9L);

        service.recordQuestCompleted(quest, completion);
        service.recordQuestDeleted(quest, completion);

        assertThat(player.getXp()).isZero();
        assertThat(player.getQuestsCompleted()).isZero();

        assertThat(events)
                .extracting(XpEvent::getAmount)
                .containsExactly(20, -20);
    }


    @Test
    void deletingACompletionThatEarnedNothingTakesNothingBack() {

        Quest medication = quest("medicate nux", 25);

        service.recordQuestCompleted(medication, completion(1L));

        /* Completion 2 was an early dose: logged, never rewarded. */
        service.recordQuestDeleted(medication, completion(1L));
        service.recordQuestDeleted(medication, completion(2L));

        assertThat(player.getXp()).isZero();

        assertThat(events)
                .extracting(XpEvent::getAmount)
                .containsExactly(25, -25);
    }


    private Quest quest(String title, int xp) {

        Quest quest = new Quest();

        quest.setTitle(title);
        quest.setXp(xp);

        return quest;
    }

    private QuestCompletion completion(Long id) {

        QuestCompletion completion = new QuestCompletion(1L);

        ReflectionTestUtils.setField(completion, "id", id);

        return completion;
    }
}
