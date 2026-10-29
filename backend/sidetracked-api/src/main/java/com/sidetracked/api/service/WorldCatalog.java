package com.sidetracked.api.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.sidetracked.api.model.World;

/*
 * ============================================================
 * WORLDS
 * ============================================================
 *
 * The broad areas the Curiosity Engine explores. To add a World,
 * add one line here; topics refer to Worlds by slug.
 *
 * Topics and subtopics inside each World will be added when live
 * discovery sources arrive and there is content to fill them.
 */
@Service
public class WorldCatalog {

    private static final List<World> WORLDS = List.of(
            new World("science-experiments", "Science & Experiments",
                    "Strange results, long experiments, how we find out."),
            new World("atomic-age", "Atomic Age",
                    "Reactors, radiation, and the nuclear century."),
            new World("history-lost-knowledge", "History & Lost Knowledge",
                    "Forgotten inventions, mysteries, and lost techniques."),
            new World("technology-engineering", "Technology & Engineering",
                    "How things work, and how they fail."),
            new World("manufacturing-industry", "Manufacturing & Industry",
                    "Factories, machines, and how things get made at scale."),
            new World("transportation-infrastructure", "Transportation & Infrastructure",
                    "Roads, rails, cables, and the systems under everything."),
            new World("architecture-built-world", "Architecture & Built World",
                    "Buildings, structures, and the places people make."),
            new World("energy-power", "Energy & Power",
                    "Where power comes from and where it goes."),
            new World("agriculture-food-systems", "Agriculture & Food Systems",
                    "Farming, seeds, and feeding the world."),
            new World("medicine", "Medicine",
                    "Bodies, cures, and medical history."),
            new World("space-unknown", "Space & The Unknown",
                    "The cosmos and the things we can't yet explain."),
            new World("exploration-expeditions", "Exploration & Expeditions",
                    "Journeys to the edges of the map."),
            new World("roads-less-traveled", "Roads Less Traveled",
                    "Real places worth visiting, legally and safely."),
            new World("making-fabrication", "Making & Fabrication",
                    "3D printing, CNC, woodworking, and building things."),
            new World("trades-forgotten-skills", "Trades & Forgotten Skills",
                    "Crafts and know-how that are fading away."),
            new World("trading-card-games", "Trading Card Games",
                    "Cards, collections, and the games around them."),
            new World("food-cooking", "Food & Cooking",
                    "Recipes, techniques, and food history."),
            new World("music-sound", "Music & Sound",
                    "Instruments, recordings, and the science of sound."),
            new World("books-archives", "Books & Archives",
                    "Manuscripts, libraries, and what they preserve."),
            new World("earth-materials", "Earth & Materials",
                    "Rocks, minerals, deep time, and how matter behaves."),
            new World("reality-simulation", "Reality & Simulation",
                    "Emergence, computation, and the nature of reality."),
            new World("signals-communications", "Signals & Communications",
                    "Radio, codes, and messages across distance."),
            new World("wildcard", "Wildcard",
                    "Anything that refuses to fit anywhere else.")
    );

    public List<World> getWorlds() {
        return WORLDS;
    }

    public Optional<World> findBySlug(String slug) {

        return WORLDS.stream()
                .filter(world -> world.slug().equals(slug))
                .findFirst();
    }

    /*
     * The display name for a slug, or the slug itself if unknown.
     */
    public String nameOf(String slug) {

        return findBySlug(slug)
                .map(World::name)
                .orElse(slug);
    }
}
