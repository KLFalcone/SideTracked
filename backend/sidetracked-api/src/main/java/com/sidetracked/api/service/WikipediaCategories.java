package com.sidetracked.api.service;

import java.util.List;
import java.util.Map;

/*
 * Where on Wikipedia to dig for each World.
 *
 * Hand-picked categories keep discoveries interesting rather than
 * random. Every category here was checked (2026-10-29) to exist and
 * to contain at least 8 articles directly. When adding one, check it
 * at https://en.wikipedia.org/wiki/Category:<name>.
 */
final class WikipediaCategories {

    static final Map<String, List<String>> BY_WORLD = Map.ofEntries(
            Map.entry("science-experiments", List.of(
                    "Physics experiments",
                    "Psychology experiments",
                    "Thought experiments",
                    "Unsolved problems in physics")),
            Map.entry("atomic-age", List.of(
                    "Nuclear accidents and incidents",
                    "Nuclear history",
                    "Nuclear-powered aircraft",
                    "Peaceful nuclear explosions")),
            Map.entry("history-lost-knowledge", List.of(
                    "Undeciphered writing systems",
                    "Ancient inventions",
                    "Lost works")),
            Map.entry("technology-engineering", List.of(
                    "Engineering failures",
                    "Prototypes",
                    "Hypothetical technology")),
            Map.entry("manufacturing-industry", List.of(
                    "Industrial history",
                    "Company towns")),
            Map.entry("transportation-infrastructure", List.of(
                    "Submarine communications cables",
                    "Canals")),
            Map.entry("architecture-built-world", List.of(
                    "Underground cities",
                    "Brutalist architecture",
                    "Abandoned buildings and structures")),
            Map.entry("energy-power", List.of(
                    "Nuclear reactors",
                    "Energy storage",
                    "Perpetual motion",
                    "Power station technology")),
            Map.entry("agriculture-food-systems", List.of(
                    "History of agriculture",
                    "Agricultural machinery")),
            Map.entry("medicine", List.of(
                    "History of medicine",
                    "Medical controversies",
                    "Mass psychogenic illness")),
            Map.entry("space-unknown", List.of(
                    "Unsolved problems in astronomy",
                    "Hypothetical astronomical objects")),
            Map.entry("exploration-expeditions", List.of(
                    "Lost explorers",
                    "Arctic expeditions",
                    "Antarctic expeditions")),
            Map.entry("roads-less-traveled", List.of(
                    "Abandoned buildings and structures",
                    "Natural arches",
                    "Roadside attractions",
                    "Caves",
                    "Volcanoes")),
            Map.entry("making-fabrication", List.of(
                    "3D printing",
                    "Woodworking",
                    "Metalworking")),
            Map.entry("trades-forgotten-skills", List.of(
                    "Obsolete occupations",
                    "Crafts")),
            Map.entry("trading-card-games", List.of(
                    "Collectible card games",
                    "Magic: The Gathering")),
            Map.entry("food-cooking", List.of(
                    "Fermented foods",
                    "Cooking techniques",
                    "Historical foods",
                    "Food preservation")),
            Map.entry("music-sound", List.of(
                    "Experimental musical instruments",
                    "Electronic musical instruments",
                    "Lost musical works")),
            Map.entry("books-archives", List.of(
                    "Literary forgeries",
                    "Lost books",
                    "Illuminated manuscripts")),
            Map.entry("earth-materials", List.of(
                    "Minerals",
                    "Rock formations",
                    "Meteorites",
                    "Geological hazards")),
            Map.entry("reality-simulation", List.of(
                    "Cellular automata",
                    "Paradoxes",
                    "Thought experiments")),
            Map.entry("signals-communications", List.of(
                    "Numbers stations",
                    "Classical ciphers",
                    "Telegraphy",
                    "Unidentified sounds")),
            Map.entry("wildcard", List.of(
                    "Hoaxes in science",
                    "Internet hoaxes",
                    "Urban legends",
                    "Unexplained disappearances"))
    );

    private WikipediaCategories() {
    }

    static List<String> forWorld(String worldSlug) {
        return BY_WORLD.getOrDefault(worldSlug, List.of());
    }
}
