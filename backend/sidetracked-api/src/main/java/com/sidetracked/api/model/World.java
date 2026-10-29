package com.sidetracked.api.model;

/*
 * A broad area of curiosity, e.g. "Atomic Age".
 *
 * slug is the stable id stored in the database and used in URLs
 * ("atomic-age"); name is what people see.
 */
public record World(
        String slug,
        String name,
        String tagline) {
}
