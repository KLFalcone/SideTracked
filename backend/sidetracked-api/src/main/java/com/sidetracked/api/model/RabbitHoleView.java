package com.sidetracked.api.model;

import java.time.Instant;
import java.util.List;
import java.util.function.Function;

/*
 * What the frontend receives for a Rabbit Hole: the shared topic
 * content combined with the current user's own progress.
 *
 * id is the topic's id, so the number shown on a card is the
 * same for every user. World fields carry display names.
 */
public record RabbitHoleView(
        Long id,
        String sourceProvider,
        String world,
        List<String> relatedWorlds,
        String title,
        String hook,
        String researchPrompt,
        int xp,
        List<String> tags,
        List<TopicSource> sources,
        RabbitHoleStatus status,
        Instant discoveredAt,
        String reflection) {

    /*
     * worldName turns a World slug into its display name.
     */
    public static RabbitHoleView from(
            UserRabbitHole rabbitHole,
            Function<String, String> worldName) {

        Topic topic = rabbitHole.getTopic();

        return new RabbitHoleView(
                topic.getId(),
                topic.getSourceProvider(),
                worldName.apply(topic.getWorld()),
                topic.getRelatedWorlds()
                        .stream()
                        .map(worldName)
                        .toList(),
                topic.getTitle(),
                topic.getHook(),
                topic.getResearchPrompt(),
                topic.getXp(),
                List.copyOf(topic.getTags()),
                List.copyOf(topic.getSources()),
                rabbitHole.getStatus(),
                Times.toInstant(rabbitHole.getDiscoveredAt()),
                rabbitHole.getReflection()
        );
    }
}
