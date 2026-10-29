package com.sidetracked.api.service;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import com.sidetracked.api.model.World;

/*
 * Offline checks only: these never call Wikipedia.
 */
class WikipediaDiscoverySourceTest {

    private final WikipediaDiscoverySource source =
            new WikipediaDiscoverySource();

    @Test
    void everyWorldHasCategoriesToDigIn() {

        for (World world : new WorldCatalog().getWorlds()) {

            assertThat(source.supports(world.slug()))
                    .as("Wikipedia categories for %s", world.name())
                    .isTrue();
        }
    }

    @Test
    void categoriesOnlyReferToRealWorlds() {

        WorldCatalog worlds = new WorldCatalog();

        assertThat(WikipediaCategories.BY_WORLD.keySet())
                .allSatisfy(slug ->
                        assertThat(worlds.findBySlug(slug)).isPresent()
                );
    }

    @Test
    void shortSummariesAreKeptWhole() {

        assertThat(WikipediaDiscoverySource.shorten("  A short one.  "))
                .isEqualTo("A short one.");
    }

    @Test
    void longSummariesAreCutAtTheLastFullSentence() {

        String sentence = "This sentence is exactly fifty characters long ok. ";
        String longText = sentence.repeat(20);

        String shortened = WikipediaDiscoverySource.shorten(longText);

        assertThat(shortened)
                .hasSizeLessThanOrEqualTo(600)
                .endsWith("ok.");
    }
}
