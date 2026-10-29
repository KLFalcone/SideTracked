package com.sidetracked.api.controller;

import java.util.List;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sidetracked.api.model.Topic;
import com.sidetracked.api.repository.TopicRepository;
import com.sidetracked.api.service.WorldCatalog;

@RestController
@RequestMapping("/api/worlds")
@CrossOrigin(origins = "http://localhost:4200")
public class WorldController {

    private final WorldCatalog worldCatalog;
    private final TopicRepository topicRepository;

    public WorldController(
            WorldCatalog worldCatalog,
            TopicRepository topicRepository) {

        this.worldCatalog = worldCatalog;
        this.topicRepository = topicRepository;
    }

    /*
     * Every World, with how many topics can currently be
     * discovered from it (so empty Worlds can show as
     * "coming soon").
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
                                .count()
                ))
                .toList();
    }

    public record WorldSummary(
            String slug,
            String name,
            String tagline,
            long topicCount) {
    }
}
