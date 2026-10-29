package com.sidetracked.api.controller;

import java.util.List;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sidetracked.api.model.Topic;
import com.sidetracked.api.repository.TopicRepository;
import com.sidetracked.api.service.DiscoverySource;
import com.sidetracked.api.service.WorldCatalog;

@RestController
@RequestMapping("/api/worlds")
@CrossOrigin(origins = "http://localhost:4200")
public class WorldController {

    private final WorldCatalog worldCatalog;
    private final TopicRepository topicRepository;
    private final List<DiscoverySource> discoverySources;

    public WorldController(
            WorldCatalog worldCatalog,
            TopicRepository topicRepository,
            List<DiscoverySource> discoverySources) {

        this.worldCatalog = worldCatalog;
        this.topicRepository = topicRepository;
        this.discoverySources = discoverySources;
    }

    /*
     * Every World, with how many topics the catalog already holds
     * for it and whether a live source can find more.
     */
    @GetMapping
    public List<WorldSummary> getWorlds() {

        List<Topic> topics = topicRepository.findAll();

        return worldCatalog.getWorlds()
                .stream()
                .map(world -> new WorldSummary(
                        world.slug(),
                        world.name(),
                        world.tagline(),
                        topics.stream()
                                .filter(topic ->
                                        topic.belongsTo(world.slug())
                                )
                                .count(),
                        discoverySources.stream()
                                .anyMatch(source ->
                                        source.supports(world.slug())
                                )
                ))
                .toList();
    }

    public record WorldSummary(
            String slug,
            String name,
            String tagline,
            long topicCount,
            boolean liveDiscovery) {
    }
}
