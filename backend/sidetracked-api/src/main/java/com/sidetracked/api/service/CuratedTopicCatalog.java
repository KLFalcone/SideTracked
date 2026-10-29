package com.sidetracked.api.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sidetracked.api.model.Topic;
import com.sidetracked.api.model.TopicSource;
import com.sidetracked.api.repository.TopicRepository;

/*
 * ============================================================
 * CURATED TOPICS
 * ============================================================
 *
 * Hand-written Rabbit Holes. They are the fallback content for
 * the Curiosity Engine when no live source is available.
 *
 * Every time the application starts, these are copied into the
 * shared topics table: new ones are added and existing ones are
 * updated in place, so fixing a link here fixes it for every
 * user who has already discovered that topic.
 */
@Service
public class CuratedTopicCatalog {

    private final TopicRepository topicRepository;
    private final WorldCatalog worldCatalog;

    public CuratedTopicCatalog(
            TopicRepository topicRepository,
            WorldCatalog worldCatalog) {

        this.topicRepository = topicRepository;
        this.worldCatalog = worldCatalog;
    }


    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void syncCuratedTopics() {

        for (Topic curated : buildCuriosityPool()) {

            checkWorldsExist(curated);

            Topic topic =
                    topicRepository
                            .findBySourceProviderAndExternalId(
                                    Topic.CURATED,
                                    curated.getExternalId()
                            )
                            .orElseGet(Topic::new);

            topic.copyContentFrom(curated);

            topicRepository.save(topic);
        }
    }


    /*
     * A misspelled World slug would silently hide a topic from
     * that World, so fail loudly at startup instead.
     */
    private void checkWorldsExist(Topic topic) {

        List<String> slugs = new ArrayList<>(topic.getRelatedWorlds());
        slugs.add(topic.getWorld());

        for (String slug : slugs) {

            if (worldCatalog.findBySlug(slug).isEmpty()) {

                throw new IllegalStateException(
                        "Curated topic \"" + topic.getTitle()
                                + "\" uses unknown World: " + slug
                );
            }
        }
    }


    /*
     * ============================================================
     * CURIOSITY POOL
     * ============================================================
     */

    public List<Topic> buildCuriosityPool() {

        List<Topic> pool = new ArrayList<>();


        /*
         * THE GAME OF LIFE
         */

        pool.add(
                curated(
                        "reality-simulation",
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

                        worlds(
                                "technology-engineering",
                                "science-experiments"
                        ),

                        tags(
                                "MATHEMATICS",
                                "COMPUTING"
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
                curated(
                        "signals-communications",
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

                        worlds(
                                "space-unknown"
                        ),

                        tags(
                                "UNSOLVED & UNEXPLAINED"
                        ),

                        List.of(
                                source(
                                        "Big Ear Radio Observatory: The Wow! Signal",
                                        "https://www.bigear.org/wow.htm",
                                        "PRIMARY SOURCE"
                                ),
                                source(
                                        "Wikipedia: Wow! signal",
                                        "https://en.wikipedia.org/wiki/Wow!_signal",
                                        "REFERENCE"
                                )
                        )
                )
        );


        /*
         * THE ANTIKYTHERA MECHANISM
         */

        pool.add(
                curated(
                        "history-lost-knowledge",
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

                        worlds(
                                "technology-engineering",
                                "space-unknown"
                        ),

                        tags(
                                "ANCIENT ENGINEERING"
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
                curated(
                        "atomic-age",
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

                        worlds(
                                "energy-power",
                                "earth-materials",
                                "science-experiments"
                        ),

                        tags(
                                "DEEP TIME"
                        ),

                        List.of(
                                source(
                                        "Scientific American: The Workings of an Ancient Nuclear Reactor",
                                        "https://www.scientificamerican.com/article/ancient-nuclear-reactor/",
                                        "EXPLANATION"
                                ),
                                source(
                                        "Wikipedia: Natural nuclear fission reactor",
                                        "https://en.wikipedia.org/wiki/Natural_nuclear_fission_reactor",
                                        "REFERENCE"
                                )
                        )
                )
        );


        /*
         * THE LONG NOW
         */

        pool.add(
                curated(
                        "technology-engineering",
                        "A CLOCK BUILT FOR 10,000 YEARS",

                        "Inside a mountain, engineers are building a "
                                + "mechanical clock designed to keep "
                                + "time for ten thousand years with "
                                + "minimal human intervention.",

                        "How do you engineer something when the expected "
                                + "lifespan of the machine is longer than "
                                + "entire civilizations?",

                        25,

                        worlds(
                                "architecture-built-world",
                                "earth-materials"
                        ),

                        tags(
                                "DEEP TIME"
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
                curated(
                        "signals-communications",
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

                        worlds(
                                "history-lost-knowledge"
                        ),

                        tags(
                                "ESPIONAGE & SECRETS",
                                "UNSOLVED & UNEXPLAINED"
                        ),

                        List.of(
                                source(
                                        "The Conet Project recordings (Internet Archive)",
                                        "https://archive.org/details/ird059",
                                        "ARCHIVE"
                                ),
                                source(
                                        "Wikipedia: Numbers station",
                                        "https://en.wikipedia.org/wiki/Numbers_station",
                                        "REFERENCE"
                                )
                        )
                )
        );


        /*
         * THE VOYNICH MANUSCRIPT
         */

        pool.add(
                curated(
                        "history-lost-knowledge",
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

                        worlds(
                                "books-archives"
                        ),

                        tags(
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
                curated(
                        "transportation-infrastructure",
                        "THE INTERNET UNDER THE OCEAN",

                        "Most international internet traffic does not "
                                + "travel through satellites. It travels "
                                + "through fiber-optic cables lying across "
                                + "the ocean floor.",

                        "How does a global digital network depend on "
                                + "physical cables thousands of miles "
                                + "long sitting at the bottom of the sea?",

                        20,

                        worlds(
                                "signals-communications",
                                "technology-engineering"
                        ),

                        tags(
                                "HIDDEN INFRASTRUCTURE"
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
                curated(
                        "science-experiments",
                        "THE EXPERIMENT THAT TAKES YEARS TO DRIP",

                        "A funnel filled with pitch has been running "
                                + "as an experiment for generations. "
                                + "Pitch looks solid enough to shatter, "
                                + "but over very long periods it flows.",

                        "What does the pitch drop experiment reveal "
                                + "about materials that behave completely "
                                + "differently depending on the timescale?",

                        20,

                        worlds(
                                "earth-materials"
                        ),

                        tags(
                                "DEEP TIME"
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
                curated(
                        "wildcard",
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

                        worlds(
                                "technology-engineering",
                                "signals-communications"
                        ),

                        tags(
                                "DIGITAL CULTURE"
                        ),

                        List.of(
                                source(
                                        "Wikipedia: Dead Internet theory",
                                        "https://en.wikipedia.org/wiki/Dead_Internet_theory",
                                        "REFERENCE"
                                ),
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
                curated(
                        "earth-materials",
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

                        worlds(
                                "roads-less-traveled",
                                "exploration-expeditions"
                        ),

                        tags(
                                "LIVING SYSTEMS",
                                "STRANGE NATURE"
                        ),

                        List.of(
                                source(
                                        "Friends of Pando",
                                        "https://www.friendsofpando.org/",
                                        "PROJECT"
                                ),
                                source(
                                        "Wikipedia: Pando (tree)",
                                        "https://en.wikipedia.org/wiki/Pando_(tree)",
                                        "REFERENCE"
                                )
                        )
                )
        );


        /*
         * GPS AND RELATIVITY
         */

        pool.add(
                curated(
                        "science-experiments",
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

                        worlds(
                                "space-unknown",
                                "technology-engineering"
                        ),

                        tags(
                                "PHYSICS"
                        ),

                        List.of(
                                source(
                                        "Ohio State Astronomy: GPS and Relativity",
                                        "https://www.astronomy.ohio-state.edu/pogge.1/Ast162/Unit5/gps.html",
                                        "EXPLANATION"
                                ),
                                source(
                                        "Wikipedia: Error analysis for GPS",
                                        "https://en.wikipedia.org/wiki/Error_analysis_for_the_Global_Positioning_System",
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

    private Topic curated(
            String primaryWorld,
            String title,
            String hook,
            String researchPrompt,
            int xp,
            List<String> relatedWorlds,
            List<String> tags,
            List<TopicSource> sources) {

        Topic topic = new Topic();

        topic.setSourceProvider(Topic.CURATED);
        topic.setExternalId(Topic.slugify(title));

        topic.setWorld(primaryWorld);
        topic.setTitle(title);
        topic.setHook(hook);
        topic.setResearchPrompt(researchPrompt);
        topic.setXp(xp);

        topic.setRelatedWorlds(
                new ArrayList<>(relatedWorlds)
        );

        topic.setTags(
                new ArrayList<>(tags)
        );

        topic.setSources(
                new ArrayList<>(sources)
        );

        return topic;
    }


    private List<String> worlds(String... slugs) {
        return List.of(slugs);
    }


    private List<String> tags(String... tags) {
        return List.of(tags);
    }


    private TopicSource source(
            String label,
            String url,
            String sourceType) {

        return new TopicSource(
                label,
                url,
                sourceType
        );
    }
}
