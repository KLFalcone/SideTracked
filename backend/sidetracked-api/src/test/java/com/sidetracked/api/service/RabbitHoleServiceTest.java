package com.sidetracked.api.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

import com.sidetracked.api.model.RabbitHoleStatus;
import com.sidetracked.api.model.RabbitHoleView;
import com.sidetracked.api.model.Topic;
import com.sidetracked.api.model.UserRabbitHole;
import com.sidetracked.api.repository.TopicRepository;
import com.sidetracked.api.repository.UserRabbitHoleRepository;

/*
 * Discovery and completion rules, tested without a database.
 */
class RabbitHoleServiceTest {

    private static final Long USER_ID = 1L;

    private TopicRepository topicRepository;
    private UserRabbitHoleRepository userRabbitHoleRepository;
    private PlayerService playerService;
    private RabbitHoleService service;

    private final List<Topic> topics = new ArrayList<>();
    private final List<UserRabbitHole> seen = new ArrayList<>();

    @BeforeEach
    void setUp() {

        topicRepository = mock(TopicRepository.class);
        userRabbitHoleRepository = mock(UserRabbitHoleRepository.class);
        playerService = mock(PlayerService.class);

        CurrentUserService currentUserService =
                mock(CurrentUserService.class);

        when(currentUserService.getCurrentUserId())
                .thenReturn(USER_ID);

        when(topicRepository.findAll())
                .thenReturn(topics);

        when(userRabbitHoleRepository
                .findByUserIdOrderByDiscoveredAtDesc(USER_ID))
                .thenReturn(seen);

        when(userRabbitHoleRepository.save(any(UserRabbitHole.class)))
                .thenAnswer(call -> call.getArgument(0));

        service = new RabbitHoleService(
                topicRepository,
                userRabbitHoleRepository,
                playerService,
                currentUserService
        );
    }

    @Test
    void discoversOnlyTopicsTheUserHasNotSeen() {

        Topic seenTopic = topic(1L, "THE GAME OF LIFE");
        topic(2L, "THE WOW! SIGNAL");

        seen.add(card(seenTopic, RabbitHoleStatus.SAVED));

        for (int i = 0; i < 20; i++) {

            RabbitHoleView view = service.discoverRabbitHole();

            assertThat(view.id()).isEqualTo(2L);
        }
    }

    @Test
    void revisitingNeverCreatesADuplicate() {

        Topic onlyTopic = topic(1L, "THE GAME OF LIFE");

        seen.add(card(onlyTopic, RabbitHoleStatus.SAVED));

        RabbitHoleView view = service.discoverRabbitHole();

        assertThat(view.id()).isEqualTo(1L);
        assertThat(view.status()).isEqualTo(RabbitHoleStatus.SAVED);

        verify(userRabbitHoleRepository, never())
                .save(any(UserRabbitHole.class));
    }

    @Test
    void revisitsSkippedOrSavedBeforeCompleted() {

        Topic completed = topic(1L, "THE VOYNICH MANUSCRIPT");
        Topic skipped = topic(2L, "THE WOW! SIGNAL");

        seen.add(card(completed, RabbitHoleStatus.COMPLETED));
        seen.add(card(skipped, RabbitHoleStatus.DISCOVERED));

        for (int i = 0; i < 20; i++) {

            assertThat(service.discoverRabbitHole().id())
                    .isEqualTo(2L);
        }
    }

    @Test
    void completingAwardsTopicXpOnlyOnce() {

        Topic topic = topic(1L, "THE WOW! SIGNAL");
        topic.setXp(30);

        UserRabbitHole card =
                card(topic, RabbitHoleStatus.EXPLORING);

        when(userRabbitHoleRepository
                .findByUserIdAndTopicId(USER_ID, 1L))
                .thenReturn(Optional.of(card));

        RabbitHoleView first =
                service.completeRabbitHole(1L, "Wow indeed");

        service.completeRabbitHole(1L, "again");

        assertThat(first.status()).isEqualTo(RabbitHoleStatus.COMPLETED);
        assertThat(card.getReflection()).isEqualTo("Wow indeed");
        assertThat(card.getCompletedAt()).isNotNull();

        verify(playerService, times(1)).recordRabbitHoleCompleted(card);
    }

    @Test
    void savingOrExploringNeverReopensACompletedRabbitHole() {

        Topic topic = topic(1L, "THE WOW! SIGNAL");

        UserRabbitHole card =
                card(topic, RabbitHoleStatus.COMPLETED);

        when(userRabbitHoleRepository
                .findByUserIdAndTopicId(USER_ID, 1L))
                .thenReturn(Optional.of(card));

        service.saveForLater(1L);
        service.startExploring(1L);

        assertThat(card.getStatus())
                .isEqualTo(RabbitHoleStatus.COMPLETED);

        verify(playerService, never()).recordRabbitHoleCompleted(any());
    }

    @Test
    void anotherUsersRabbitHoleIsNotFound() {

        when(userRabbitHoleRepository
                .findByUserIdAndTopicId(USER_ID, 99L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.completeRabbitHole(99L, null))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("404");
    }

    @Test
    void slugMatchesTheMigrationRule() {

        assertThat(Topic.slugify("THE WOW! SIGNAL"))
                .isEqualTo("the-wow-signal");

        assertThat(Topic.slugify("A CLOCK BUILT FOR 10,000 YEARS"))
                .isEqualTo("a-clock-built-for-10-000-years");
    }


    private Topic topic(Long id, String title) {

        Topic topic = new Topic();

        ReflectionTestUtils.setField(topic, "id", id);

        topic.setTitle(title);
        topic.setSourceProvider(Topic.CURATED);
        topic.setExternalId(Topic.slugify(title));

        topics.add(topic);

        return topic;
    }

    private UserRabbitHole card(
            Topic topic,
            RabbitHoleStatus status) {

        UserRabbitHole card = new UserRabbitHole(USER_ID, topic);

        card.setStatus(status);

        return card;
    }
}
