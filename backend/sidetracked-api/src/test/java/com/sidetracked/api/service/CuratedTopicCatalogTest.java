package com.sidetracked.api.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.sidetracked.api.model.Topic;
import com.sidetracked.api.repository.TopicRepository;

/*
 * Checks the hand-written Rabbit Holes, without a database.
 */
class CuratedTopicCatalogTest {

    private final WorldCatalog worldCatalog = new WorldCatalog();

    private final List<Topic> pool =
            new CuratedTopicCatalog(
                    mock(TopicRepository.class),
                    worldCatalog
            ).buildCuriosityPool();

    @Test
    void everyCuratedTopicUsesKnownWorlds() {

        for (Topic topic : pool) {

            assertThat(worldCatalog.findBySlug(topic.getWorld()))
                    .as("primary World of %s", topic.getTitle())
                    .isPresent();

            for (String related : topic.getRelatedWorlds()) {

                assertThat(worldCatalog.findBySlug(related))
                        .as("related World of %s", topic.getTitle())
                        .isPresent();
            }
        }
    }

    @Test
    void curatedTopicsAreUniqueAndHaveSources() {

        assertThat(pool)
                .extracting(Topic::getExternalId)
                .doesNotHaveDuplicates();

        assertThat(pool)
                .allSatisfy(topic ->
                        assertThat(topic.getSources()).isNotEmpty()
                );
    }
}
