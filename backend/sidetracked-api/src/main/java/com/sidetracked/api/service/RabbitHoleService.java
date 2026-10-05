package com.sidetracked.api.service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.springframework.stereotype.Service;

import com.sidetracked.api.model.RabbitHole;
import com.sidetracked.api.model.RabbitHole.RabbitHoleSource;
import com.sidetracked.api.model.RabbitHoleStatus;
import com.sidetracked.api.repository.RabbitHoleRepository;

@Service
public class RabbitHoleService {

    private final RabbitHoleRepository rabbitHoleRepository;
    private final Random random = new Random();

    public RabbitHoleService(
            RabbitHoleRepository rabbitHoleRepository) {

        this.rabbitHoleRepository = rabbitHoleRepository;
    }

    /*
     * ============================================================
     * DISCOVERY
     * ============================================================
     *
     * Curiosity Engine V1
     *
     * Rabbit Holes currently come from a local curated pool.
     * The service selects a Rabbit Hole that has not already been
     * discovered whenever possible.
     *
     * Later, the local pool can be supplemented or replaced by
     * external discovery providers without changing the frontend.
     */

    public RabbitHole discoverRabbitHole() {

        List<RabbitHole> candidates = buildCuriosityPool();

        List<RabbitHole> library =
                rabbitHoleRepository.findAllByOrderByDiscoveredAtDesc();

        Set<String> discoveredTitles = new HashSet<>();

        for (RabbitHole rabbitHole : library) {
            discoveredTitles.add(
                    rabbitHole.getTitle().toLowerCase()
            );
        }

        List<RabbitHole> undiscovered =
                candidates.stream()
                        .filter(candidate ->
                                !discoveredTitles.contains(
                                        candidate
                                                .getTitle()
                                                .toLowerCase()
                                )
                        )
                        .toList();

        List<RabbitHole> selectionPool =
                undiscovered.isEmpty()
                        ? candidates
                        : undiscovered;

        RabbitHole rabbitHole =
                selectionPool.get(
                        random.nextInt(selectionPool.size())
                );

        rabbitHole.setStatus(
                RabbitHoleStatus.DISCOVERED
        );

        return rabbitHoleRepository.save(rabbitHole);
    }


    /*
     * ============================================================
     * LIBRARY
     * ============================================================
     */

    public List<RabbitHole> getLibrary() {

        return rabbitHoleRepository
                .findAllByOrderByDiscoveredAtDesc();
    }


    /*
     * ============================================================
     * STATUS CHANGES
     * ============================================================
     */

    public RabbitHole saveForLater(Long id) {

        RabbitHole rabbitHole = getRabbitHole(id);

        rabbitHole.setStatus(
                RabbitHoleStatus.SAVED
        );

        return rabbitHoleRepository.save(rabbitHole);
    }


    public RabbitHole startExploring(Long id) {

        RabbitHole rabbitHole = getRabbitHole(id);

        rabbitHole.setStatus(
                RabbitHoleStatus.EXPLORING
        );

        return rabbitHoleRepository.save(rabbitHole);
    }


    public RabbitHole completeRabbitHole(Long id) {

        RabbitHole rabbitHole = getRabbitHole(id);

        if (rabbitHole.getStatus()
                == RabbitHoleStatus.COMPLETED) {

            return rabbitHole;
        }

        rabbitHole.setStatus(
                RabbitHoleStatus.COMPLETED
        );

        return rabbitHoleRepository.save(rabbitHole);
    }


    /*
     * ============================================================
     * DELETE
     * ============================================================
     */

    public void deleteRabbitHole(Long id) {

        if (!rabbitHoleRepository.existsById(id)) {
            return;
        }

        rabbitHoleRepository.deleteById(id);
    }


    /*
     * ============================================================
     * LOOKUP
     * ============================================================
     */

    private RabbitHole getRabbitHole(Long id) {

        return rabbitHoleRepository
                .findById(id)
                .orElseThrow(
                        () -> new IllegalArgumentException(
                                "Rabbit Hole not found: " + id
                        )
                );
    }


    /*
     * ============================================================
     * CURIOSITY POOL
     * ============================================================
     */

    private List<RabbitHole> buildCuriosityPool() {

        List<RabbitHole> pool = new ArrayList<>();


        /*
         * THE GAME OF LIFE
         */

        pool.add(
                createRabbitHole(
                        "REALITY & SIMULATION",
                        "THE GAME OF LIFE",

                        "In 1970, mathematician John Conway created "
                                + "an extremely simple set of rules. "
                                + "Those rules can produce systems that "
                                + "appear to move, reproduce, and even "
                                + "perform computation.",

                        "How can complex behavior emerge from rules "
                                + "that contain almost no complexity "
                                + "themselves?",

                        25,

                        List.of(
                                "TECHNOLOGY & ENGINEERING",
                                "SCIENCE & EXPERIMENTS",
                                "HISTORY & LOST KNOWLEDGE"
                        ),

                        List.of(
                                source(
                                        "Interactive Game of Life",
                                        "https://playgameoflife.com/",
                                        "INTERACTIVE"
                                ),
                                source(
                                        "LifeWiki",
                                        "https://conwaylife.com/wiki/Conway%27s_Game_of_Life",
                                        "REFERENCE"
                                ),
                                source(
                                        "Stanford Encyclopedia of Philosophy",
                                        "https://plato.stanford.edu/entries/cellular-automata/",
                                        "EXPLANATION"
                                )
                        )
                )
        );


        /*
         * THE WOW! SIGNAL
         */

        pool.add(
                createRabbitHole(
                        "SIGNALS & COMMUNICATIONS",
                        "THE WOW! SIGNAL",

                        "In 1977, a radio telescope detected an "
                                + "unusually strong narrowband signal "
                                + "that appeared to come from the sky. "
                                + "Astronomer Jerry Ehman circled the "
                                + "printout and wrote a single word "
                                + "beside it: Wow!",

                        "What could produce a powerful radio signal "
                                + "that appears once and is never "
                                + "detected again?",

                        30,

                        List.of(
                                "SPACE & COSMOS",
                                "SIGNALS & COMMUNICATIONS",
                                "UNSOLVED & UNEXPLAINED"
                        ),

                        List.of(
                                source(
                                        "SETI Institute",
                                        "https://www.seti.org/",
                                        "REFERENCE"
                                ),
                                source(
                                        "Big Ear Radio Observatory",
                                        "https://www.bigear.org/",
                                        "ARCHIVE"
                                )
                        )
                )
        );


        /*
         * THE ANTIKYTHERA MECHANISM
         */

        pool.add(
                createRabbitHole(
                        "HISTORY & LOST KNOWLEDGE",
                        "THE ANTIKYTHERA MECHANISM",

                        "Divers exploring an ancient shipwreck found "
                                + "a corroded object containing dozens "
                                + "of precision bronze gears. The device "
                                + "was built more than two thousand years "
                                + "ago and could model astronomical cycles.",

                        "How did ancient engineers build a mechanical "
                                + "computer whose complexity would not "
                                + "be matched for centuries?",

                        30,

                        List.of(
                                "HISTORY & LOST KNOWLEDGE",
                                "TECHNOLOGY & ENGINEERING",
                                "SPACE & COSMOS"
                        ),

                        List.of(
                                source(
                                        "Antikythera Mechanism Research Project",
                                        "http://www.antikythera-mechanism.gr/",
                                        "RESEARCH"
                                ),
                                source(
                                        "Encyclopaedia Britannica",
                                        "https://www.britannica.com/topic/Antikythera-mechanism",
                                        "REFERENCE"
                                )
                        )
                )
        );


        /*
         * NATURAL NUCLEAR REACTORS
         */

        pool.add(
                createRabbitHole(
                        "ATOMIC AGE",
                        "THE REACTOR THAT BUILT ITSELF",

                        "Nearly two billion years before humans built "
                                + "nuclear reactors, uranium deposits "
                                + "in what is now Gabon underwent "
                                + "self-sustaining nuclear fission "
                                + "underground.",

                        "How can the conditions for a nuclear reactor "
                                + "occur naturally without machines, "
                                + "engineers, or human intervention?",

                        35,

                        List.of(
                                "ATOMIC AGE",
                                "SCIENCE & EXPERIMENTS",
                                "EARTH & DEEP TIME"
                        ),

                        List.of(
                                source(
                                        "IAEA",
                                        "https://www.iaea.org/",
                                        "REFERENCE"
                                ),
                                source(
                                        "U.S. Department of Energy",
                                        "https://www.energy.gov/",
                                        "REFERENCE"
                                )
                        )
                )
        );


        /*
         * THE LONG NOW
         */

        pool.add(
                createRabbitHole(
                        "DEEP TIME",
                        "A CLOCK BUILT FOR 10,000 YEARS",

                        "Inside a mountain, engineers are building a "
                                + "mechanical clock designed to keep "
                                + "time for ten thousand years with "
                                + "minimal human intervention.",

                        "How do you engineer something when the expected "
                                + "lifespan of the machine is longer than "
                                + "entire civilizations?",

                        25,

                        List.of(
                                "TECHNOLOGY & ENGINEERING",
                                "EARTH & DEEP TIME",
                                "HUMAN SYSTEMS"
                        ),

                        List.of(
                                source(
                                        "The Clock of the Long Now",
                                        "https://longnow.org/clock/",
                                        "PROJECT"
                                )
                        )
                )
        );


        /*
         * NUMBERS STATIONS
         */

        pool.add(
                createRabbitHole(
                        "SIGNALS & COMMUNICATIONS",
                        "THE VOICES IN THE STATIC",

                        "For decades, shortwave radios have picked up "
                                + "strange broadcasts consisting of "
                                + "numbers, tones, melodies, and coded "
                                + "messages read by anonymous voices.",

                        "Why would someone maintain powerful radio "
                                + "stations that broadcast apparently "
                                + "meaningless sequences to unknown "
                                + "listeners?",

                        25,

                        List.of(
                                "SIGNALS & COMMUNICATIONS",
                                "ESPIONAGE & SECRETS",
                                "UNSOLVED & UNEXPLAINED"
                        ),

                        List.of(
                                source(
                                        "The Conet Project",
                                        "https://www.irdial.com/conet.htm",
                                        "ARCHIVE"
                                )
                        )
                )
        );


        /*
         * THE VOYNICH MANUSCRIPT
         */

        pool.add(
                createRabbitHole(
                        "HISTORY & LOST KNOWLEDGE",
                        "THE VOYNICH MANUSCRIPT",

                        "A centuries-old illustrated manuscript is "
                                + "written in a script that no one has "
                                + "conclusively deciphered. Its pages "
                                + "contain unfamiliar plants, astronomical "
                                + "diagrams, and mysterious text.",

                        "Is the Voynich Manuscript an unknown language, "
                                + "a cipher, an elaborate hoax, or "
                                + "something else entirely?",

                        30,

                        List.of(
                                "HISTORY & LOST KNOWLEDGE",
                                "LANGUAGE & CODES",
                                "UNSOLVED & UNEXPLAINED"
                        ),

                        List.of(
                                source(
                                        "Yale Beinecke Library",
                                        "https://beinecke.library.yale.edu/collections/highlights/voynich-manuscript",
                                        "PRIMARY SOURCE"
                                )
                        )
                )
        );


        /*
         * THE INTERNET'S UNDERSEA CABLES
         */

        pool.add(
                createRabbitHole(
                        "HIDDEN INFRASTRUCTURE",
                        "THE INTERNET UNDER THE OCEAN",

                        "Most international internet traffic does not "
                                + "travel through satellites. It travels "
                                + "through fiber-optic cables lying across "
                                + "the ocean floor.",

                        "How does a global digital network depend on "
                                + "physical cables thousands of miles "
                                + "long sitting at the bottom of the sea?",

                        20,

                        List.of(
                                "TECHNOLOGY & ENGINEERING",
                                "HIDDEN INFRASTRUCTURE",
                                "SIGNALS & COMMUNICATIONS"
                        ),

                        List.of(
                                source(
                                        "TeleGeography Submarine Cable Map",
                                        "https://www.submarinecablemap.com/",
                                        "INTERACTIVE"
                                )
                        )
                )
        );


        /*
         * THE WORLD'S LONGEST EXPERIMENT
         */

        pool.add(
                createRabbitHole(
                        "SCIENCE & EXPERIMENTS",
                        "THE EXPERIMENT THAT TAKES YEARS TO DRIP",

                        "A funnel filled with pitch has been running "
                                + "as an experiment for generations. "
                                + "Pitch looks solid enough to shatter, "
                                + "but over very long periods it flows.",

                        "What does the pitch drop experiment reveal "
                                + "about materials that behave completely "
                                + "differently depending on the timescale?",

                        20,

                        List.of(
                                "SCIENCE & EXPERIMENTS",
                                "EARTH & DEEP TIME",
                                "MATERIALS & MATTER"
                        ),

                        List.of(
                                source(
                                        "University of Queensland Pitch Drop Experiment",
                                        "https://smp.uq.edu.au/pitch-drop-experiment",
                                        "LIVE EXPERIMENT"
                                )
                        )
                )
        );


        /*
         * THE DEAD INTERNET THEORY
         */

        pool.add(
                createRabbitHole(
                        "DIGITAL CULTURE",
                        "THE DEAD INTERNET THEORY",

                        "A modern internet conspiracy theory proposes "
                                + "that much of online activity is no "
                                + "longer produced by real people, but "
                                + "by automated systems generating and "
                                + "amplifying content.",

                        "As bots and generative systems become more "
                                + "capable, how could we actually measure "
                                + "what fraction of online activity "
                                + "comes from humans?",

                        25,

                        List.of(
                                "DIGITAL CULTURE",
                                "TECHNOLOGY & ENGINEERING",
                                "HUMAN SYSTEMS"
                        ),

                        List.of(
                                source(
                                        "Imperva Bad Bot Report",
                                        "https://www.imperva.com/resources/resource-library/reports/bad-bot-report/",
                                        "RESEARCH"
                                )
                        )
                )
        );


        /*
         * PANDO
         */

        pool.add(
                createRabbitHole(
                        "LIVING SYSTEMS",
                        "THE FOREST THAT IS ONE TREE",

                        "In Utah, tens of thousands of aspen trunks "
                                + "cover more than one hundred acres. "
                                + "Genetically, however, they are parts "
                                + "of a single organism connected by "
                                + "one enormous root system.",

                        "When thousands of apparently separate trees "
                                + "share one genetic identity and root "
                                + "system, what actually counts as "
                                + "an individual organism?",

                        25,

                        List.of(
                                "LIVING SYSTEMS",
                                "EARTH & DEEP TIME",
                                "STRANGE NATURE"
                        ),

                        List.of(
                                source(
                                        "U.S. Forest Service",
                                        "https://www.fs.usda.gov/",
                                        "REFERENCE"
                                )
                        )
                )
        );


        /*
         * GPS AND RELATIVITY
         */

        pool.add(
                createRabbitHole(
                        "REALITY & SIMULATION",
                        "YOUR GPS NEEDS EINSTEIN",

                        "Satellites experience time differently from "
                                + "receivers on Earth's surface. Without "
                                + "correcting for relativity, GPS position "
                                + "calculations would rapidly become "
                                + "inaccurate.",

                        "Why does finding your location on Earth require "
                                + "engineers to account for the fact that "
                                + "time itself passes at different rates?",

                        30,

                        List.of(
                                "SPACE & COSMOS",
                                "TECHNOLOGY & ENGINEERING",
                                "PHYSICS & REALITY"
                        ),

                        List.of(
                                source(
                                        "GPS.gov",
                                        "https://www.gps.gov/",
                                        "REFERENCE"
                                ),
                                source(
                                        "NIST",
                                        "https://www.nist.gov/",
                                        "REFERENCE"
                                )
                        )
                )
        );


        return pool;
    }


    /*
     * ============================================================
     * FACTORY HELPERS
     * ============================================================
     */

    private RabbitHole createRabbitHole(
            String world,
            String title,
            String hook,
            String researchPrompt,
            int xp,
            List<String> tags,
            List<RabbitHoleSource> sources) {

        RabbitHole rabbitHole = new RabbitHole();

        rabbitHole.setWorld(world);
        rabbitHole.setTitle(title);
        rabbitHole.setHook(hook);
        rabbitHole.setResearchPrompt(researchPrompt);
        rabbitHole.setXp(xp);

        rabbitHole.setTags(
                new ArrayList<>(tags)
        );

        rabbitHole.setSources(
                new ArrayList<>(sources)
        );

        return rabbitHole;
    }


    private RabbitHoleSource source(
            String label,
            String url,
            String sourceType) {

        return new RabbitHoleSource(
                label,
                url,
                sourceType
        );
    }
}