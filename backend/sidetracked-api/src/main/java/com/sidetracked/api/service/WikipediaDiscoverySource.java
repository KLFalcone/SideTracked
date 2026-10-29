package com.sidetracked.api.service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.function.Predicate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.sidetracked.api.model.Topic;
import com.sidetracked.api.model.TopicSource;

/*
 * ============================================================
 * WIKIPEDIA
 * ============================================================
 *
 * Finds a live topic by picking a hand-picked category for the
 * World (WikipediaCategories), choosing an article in it that isn't
 * in the catalog yet, and turning its summary into a topic.
 *
 * Free, no API key. Wikimedia asks clients to identify themselves
 * with a User-Agent that includes contact details, and to keep
 * request rates modest; each discovery makes only a few requests.
 *
 * Wikipedia text is licensed CC BY-SA 4.0: the frontend credits
 * Wikipedia and links to the article on every such topic.
 */
@Service
public class WikipediaDiscoverySource implements DiscoverySource {

    public static final String PROVIDER = "WIKIPEDIA";

    private static final Logger log =
            LoggerFactory.getLogger(WikipediaDiscoverySource.class);

    private static final String USER_AGENT =
            "SideTracked/0.1 (https://github.com/KLFalcone/SideTracked)";

    private static final int MAX_HOOK_LENGTH = 600;
    private static final int CATEGORY_ATTEMPTS = 3;
    private static final int ARTICLES_PER_CATEGORY = 4;

    /*
     * Questions that make sense for any kind of article (a place,
     * a person, a phenomenon, an object); {title} is replaced.
     */
    private static final List<String> RESEARCH_PROMPTS = List.of(
            "What is the strangest detail you can find about {title}, "
                    + "and what question does it leave unanswered?",
            "Where do the sources about {title} disagree, "
                    + "and who do you find more convincing?",
            "How would you explain {title} to a friend in one minute, "
                    + "and what would you still want to know?",
            "What surprising connection can you find between {title} "
                    + "and something you already know?"
    );

    private final RestClient restClient;
    private final Random random = new Random();

    public WikipediaDiscoverySource() {

        SimpleClientHttpRequestFactory requestFactory =
                new SimpleClientHttpRequestFactory();

        requestFactory.setConnectTimeout(Duration.ofSeconds(4));
        requestFactory.setReadTimeout(Duration.ofSeconds(6));

        this.restClient = RestClient.builder()
                .baseUrl("https://en.wikipedia.org")
                .defaultHeader("User-Agent", USER_AGENT)
                .requestFactory(requestFactory)
                .build();
    }

    @Override
    public String provider() {
        return PROVIDER;
    }

    @Override
    public boolean supports(String worldSlug) {
        return !WikipediaCategories.forWorld(worldSlug).isEmpty();
    }

    @Override
    public Optional<Topic> discover(
            String worldSlug,
            Predicate<String> alreadyInCatalog) {

        List<String> categories =
                new ArrayList<>(WikipediaCategories.forWorld(worldSlug));

        Collections.shuffle(categories, random);

        try {

            for (String category : categories.subList(
                    0, Math.min(CATEGORY_ATTEMPTS, categories.size()))) {

                Optional<Topic> topic =
                        discoverFromCategory(
                                worldSlug,
                                category,
                                alreadyInCatalog
                        );

                if (topic.isPresent()) {
                    return topic;
                }
            }

        } catch (RestClientException e) {

            log.warn(
                    "Wikipedia discovery failed for World {}: {}",
                    worldSlug,
                    e.getMessage()
            );
        }

        return Optional.empty();
    }


    private Optional<Topic> discoverFromCategory(
            String worldSlug,
            String category,
            Predicate<String> alreadyInCatalog) {

        CategoryMembersResponse response =
                restClient.get()
                        .uri(uri -> uri
                                .path("/w/api.php")
                                .queryParam("action", "query")
                                .queryParam("list", "categorymembers")
                                .queryParam("cmtitle", "Category:{category}")
                                .queryParam("cmtype", "page")
                                .queryParam("cmnamespace", "0")
                                .queryParam("cmlimit", "200")
                                .queryParam("format", "json")
                                .queryParam("formatversion", "2")
                                .build(category))
                        .retrieve()
                        .body(CategoryMembersResponse.class);

        if (response == null || response.query() == null) {
            return Optional.empty();
        }

        List<Member> members =
                new ArrayList<>(
                        response.query().categorymembers()
                                .stream()
                                .filter(member ->
                                        isInteresting(member.title())
                                )
                                .filter(member ->
                                        !alreadyInCatalog.test(
                                                String.valueOf(member.pageid())
                                        )
                                )
                                .toList()
                );

        Collections.shuffle(members, random);

        for (Member member : members.subList(
                0, Math.min(ARTICLES_PER_CATEGORY, members.size()))) {

            Optional<Topic> topic =
                    toTopic(worldSlug, category, member.title());

            if (topic.isPresent()) {
                return topic;
            }
        }

        return Optional.empty();
    }


    /*
     * Skips list pages, and titles with "/" (which can't be used in
     * the summary URL).
     */
    private boolean isInteresting(String title) {

        return !title.startsWith("List of")
                && !title.startsWith("Lists of")
                && !title.startsWith("Outline of")
                && !title.startsWith("Index of")
                && !title.contains("/");
    }


    private Optional<Topic> toTopic(
            String worldSlug,
            String category,
            String articleTitle) {

        Summary summary =
                restClient.get()
                        .uri(
                                "/api/rest_v1/page/summary/{title}",
                                articleTitle.replace(' ', '_')
                        )
                        .retrieve()
                        .body(Summary.class);

        if (summary == null
                || !"standard".equals(summary.type())
                || summary.pageid() == null
                || summary.extract() == null
                || summary.extract().isBlank()
                || summary.content_urls() == null
                || summary.content_urls().desktop() == null) {

            return Optional.empty();
        }

        String title = summary.title();
        String articleUrl = summary.content_urls().desktop().page();

        Topic topic = new Topic();

        topic.setSourceProvider(PROVIDER);
        topic.setExternalId(String.valueOf(summary.pageid()));

        topic.setWorld(worldSlug);
        topic.setTitle(title);
        topic.setHook(shorten(summary.extract()));
        topic.setResearchPrompt(
                RESEARCH_PROMPTS
                        .get(random.nextInt(RESEARCH_PROMPTS.size()))
                        .replace("{title}", title)
        );
        topic.setXp(25);

        topic.setTags(new ArrayList<>(List.of(
                category.toUpperCase()
        )));

        topic.setSources(new ArrayList<>(List.of(
                new TopicSource(
                        "Wikipedia: " + title,
                        articleUrl,
                        "REFERENCE"
                ),
                new TopicSource(
                        "Search the Internet Archive",
                        "https://archive.org/search?query="
                                + URLEncoder.encode(
                                        "\"" + title + "\"",
                                        StandardCharsets.UTF_8
                                ),
                        "ARCHIVE"
                )
        )));

        return Optional.of(topic);
    }


    /*
     * Keeps whole sentences up to MAX_HOOK_LENGTH characters.
     */
    static String shorten(String text) {

        String trimmed = text.trim();

        if (trimmed.length() <= MAX_HOOK_LENGTH) {
            return trimmed;
        }

        String cut = trimmed.substring(0, MAX_HOOK_LENGTH);
        int lastSentenceEnd = cut.lastIndexOf(". ");

        return lastSentenceEnd > 0
                ? cut.substring(0, lastSentenceEnd + 1)
                : cut.substring(0, cut.lastIndexOf(' ')) + "...";
    }


    /*
     * Only the fields SideTracked uses from Wikipedia's responses.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    record CategoryMembersResponse(Query query) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record Query(List<Member> categorymembers) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record Member(long pageid, String title) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record Summary(
            String type,
            String title,
            Long pageid,
            String extract,
            ContentUrls content_urls) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record ContentUrls(Desktop desktop) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record Desktop(String page) {
    }
}
