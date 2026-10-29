package com.sidetracked.api.model;

import java.time.LocalDateTime;
import java.util.List;

/*
 * What the frontend receives for a Rabbit Hole: the shared topic
 * content combined with the current user's own progress.
 *
 * id is the topic's id, so the number shown on a card is the
 * same for every user.
 */
public record RabbitHoleView(
        Long id,
        String world,
        String title,
        String hook,
        String researchPrompt,
        int xp,
        List<String> tags,
        List<TopicSource> sources,
        RabbitHoleStatus status,
        LocalDateTime discoveredAt,
        String reflection) {

    public static RabbitHoleView from(UserRabbitHole rabbitHole) {

        Topic topic = rabbitHole.getTopic();

        return new RabbitHoleView(
                topic.getId(),
                topic.getWorld(),
                topic.getTitle(),
                topic.getHook(),
                topic.getResearchPrompt(),
                topic.getXp(),
                List.copyOf(topic.getTags()),
                List.copyOf(topic.getSources()),
                rabbitHole.getStatus(),
                rabbitHole.getDiscoveredAt(),
                rabbitHole.getReflection()
        );
    }
}
