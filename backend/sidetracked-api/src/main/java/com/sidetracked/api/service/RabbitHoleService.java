package com.sidetracked.api.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.sidetracked.api.model.RabbitHoleStatus;
import com.sidetracked.api.model.RabbitHoleView;
import com.sidetracked.api.model.Topic;
import com.sidetracked.api.model.UserRabbitHole;
import com.sidetracked.api.repository.TopicRepository;
import com.sidetracked.api.repository.UserRabbitHoleRepository;

/*
 * The current user's Rabbit Holes.
 *
 * Topics are shared; each user has at most one UserRabbitHole
 * per topic holding their own status and journal note.
 *
 * Rabbit Holes are addressed by topic id, and every lookup is
 * scoped to the current user.
 */
@Service
public class RabbitHoleService {

    private final TopicRepository topicRepository;
    private final UserRabbitHoleRepository userRabbitHoleRepository;
    private final PlayerService playerService;
    private final CurrentUserService currentUserService;
    private final Random random = new Random();

    public RabbitHoleService(
            TopicRepository topicRepository,
            UserRabbitHoleRepository userRabbitHoleRepository,
            PlayerService playerService,
            CurrentUserService currentUserService) {

        this.topicRepository = topicRepository;
        this.userRabbitHoleRepository = userRabbitHoleRepository;
        this.playerService = playerService;
        this.currentUserService = currentUserService;
    }

    /*
     * ============================================================
     * DISCOVERY
     * ============================================================
     *
     * 1. A topic the user has never seen, if any remain.
     * 2. Otherwise revisit one they skipped or saved,
     *    then one they are exploring, then anything.
     *
     * A revisit returns the user's existing Rabbit Hole;
     * it never creates a second copy.
     */

    @Transactional
    public RabbitHoleView discoverRabbitHole() {

        Long userId =
                currentUserService.getCurrentUserId();

        Map<Long, UserRabbitHole> seen =
                userRabbitHoleRepository
                        .findByUserIdOrderByDiscoveredAtDesc(userId)
                        .stream()
                        .collect(Collectors.toMap(
                                rabbitHole -> rabbitHole.getTopic().getId(),
                                Function.identity()
                        ));

        List<Topic> unseen =
                topicRepository.findAll()
                        .stream()
                        .filter(topic ->
                                !seen.containsKey(topic.getId())
                        )
                        .toList();

        if (!unseen.isEmpty()) {

            Topic topic = pickRandom(unseen);

            UserRabbitHole rabbitHole =
                    userRabbitHoleRepository.save(
                            new UserRabbitHole(userId, topic)
                    );

            return RabbitHoleView.from(rabbitHole);
        }

        if (seen.isEmpty()) {

            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "No Rabbit Holes are available yet."
            );
        }

        return RabbitHoleView.from(
                pickRevisit(List.copyOf(seen.values()))
        );
    }


    private UserRabbitHole pickRevisit(
            List<UserRabbitHole> seen) {

        List<Set<RabbitHoleStatus>> preference = List.of(
                Set.of(RabbitHoleStatus.DISCOVERED, RabbitHoleStatus.SAVED),
                Set.of(RabbitHoleStatus.EXPLORING)
        );

        for (Set<RabbitHoleStatus> statuses : preference) {

            List<UserRabbitHole> candidates =
                    seen.stream()
                            .filter(rabbitHole ->
                                    statuses.contains(rabbitHole.getStatus())
                            )
                            .toList();

            if (!candidates.isEmpty()) {
                return pickRandom(candidates);
            }
        }

        return pickRandom(seen);
    }


    private <T> T pickRandom(List<T> items) {
        return items.get(random.nextInt(items.size()));
    }


    /*
     * ============================================================
     * LIBRARY
     * ============================================================
     */

    public List<RabbitHoleView> getLibrary() {

        return userRabbitHoleRepository
                .findByUserIdOrderByDiscoveredAtDesc(
                        currentUserService.getCurrentUserId()
                )
                .stream()
                .map(RabbitHoleView::from)
                .toList();
    }


    /*
     * ============================================================
     * STATUS CHANGES
     * ============================================================
     *
     * Completed rabbit holes keep their status.
     * Otherwise a rabbit hole could be completed again
     * and award its XP more than once.
     */

    @Transactional
    public RabbitHoleView saveForLater(Long topicId) {

        UserRabbitHole rabbitHole = getRabbitHole(topicId);

        if (rabbitHole.getStatus()
                != RabbitHoleStatus.COMPLETED) {

            rabbitHole.setStatus(
                    RabbitHoleStatus.SAVED
            );
        }

        return RabbitHoleView.from(rabbitHole);
    }


    @Transactional
    public RabbitHoleView startExploring(Long topicId) {

        UserRabbitHole rabbitHole = getRabbitHole(topicId);

        if (rabbitHole.getStatus()
                != RabbitHoleStatus.COMPLETED) {

            rabbitHole.setStatus(
                    RabbitHoleStatus.EXPLORING
            );
        }

        return RabbitHoleView.from(rabbitHole);
    }


    @Transactional
    public RabbitHoleView completeRabbitHole(
            Long topicId,
            String reflection) {

        UserRabbitHole rabbitHole = getRabbitHole(topicId);

        if (rabbitHole.getStatus()
                == RabbitHoleStatus.COMPLETED) {

            return RabbitHoleView.from(rabbitHole);
        }

        rabbitHole.setStatus(
                RabbitHoleStatus.COMPLETED
        );

        rabbitHole.setCompletedAt(
                LocalDateTime.now()
        );

        /*
         * The reflection is a personal journal entry,
         * so whatever the player writes is accepted.
         */
        rabbitHole.setReflection(reflection);

        playerService.awardEncounterXp(
                rabbitHole.getTopic().getXp()
        );

        return RabbitHoleView.from(rabbitHole);
    }


    /*
     * ============================================================
     * DELETE
     * ============================================================
     *
     * Removes the user's own record of a topic.
     * The shared topic itself is never deleted here.
     */

    @Transactional
    public void deleteRabbitHole(Long topicId) {

        userRabbitHoleRepository
                .findByUserIdAndTopicId(
                        currentUserService.getCurrentUserId(),
                        topicId
                )
                .ifPresent(userRabbitHoleRepository::delete);
    }


    /*
     * ============================================================
     * LOOKUP
     * ============================================================
     *
     * A topic the user hasn't discovered, or another user's
     * Rabbit Hole, is reported as not found.
     */

    private UserRabbitHole getRabbitHole(Long topicId) {

        return userRabbitHoleRepository
                .findByUserIdAndTopicId(
                        currentUserService.getCurrentUserId(),
                        topicId
                )
                .orElseThrow(
                        () -> new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Rabbit Hole not found: " + topicId
                        )
                );
    }
}
