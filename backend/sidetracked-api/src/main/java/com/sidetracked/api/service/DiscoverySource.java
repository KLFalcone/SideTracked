package com.sidetracked.api.service;

import java.util.Optional;
import java.util.function.Predicate;

import com.sidetracked.api.model.Topic;

/*
 * A place the Curiosity Engine can find new topics live,
 * e.g. Wikipedia. More sources (Wikivoyage, fan wikis, archives)
 * can be added by implementing this interface.
 */
public interface DiscoverySource {

    /*
     * Find one topic from the given World that isn't already in
     * the catalog, or empty if nothing suitable was found (or the
     * source couldn't be reached).
     *
     * alreadyInCatalog receives an external id from this source.
     * The returned topic is not saved yet.
     */
    Optional<Topic> discover(
            String worldSlug,
            Predicate<String> alreadyInCatalog
    );

    /*
     * The sourceProvider value this source gives its topics.
     */
    String provider();

    /*
     * Whether this source knows where to look for the World.
     */
    boolean supports(String worldSlug);
}
