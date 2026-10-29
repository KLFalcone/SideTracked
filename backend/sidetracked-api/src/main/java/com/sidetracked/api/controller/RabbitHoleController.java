package com.sidetracked.api.controller;

import java.util.List;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.sidetracked.api.model.RabbitHoleView;
import com.sidetracked.api.service.RabbitHoleService;

@RestController
@RequestMapping("/api/rabbit-holes")
@CrossOrigin(origins = "http://localhost:4200")
public class RabbitHoleController {

    private final RabbitHoleService rabbitHoleService;

    public RabbitHoleController(
            RabbitHoleService rabbitHoleService) {

        this.rabbitHoleService = rabbitHoleService;
    }

    /*
     * POST /api/rabbit-holes/discover              -> any World
     * POST /api/rabbit-holes/discover?world=slug   -> that World only
     */
    @PostMapping("/discover")
    public RabbitHoleView discoverRabbitHole(
            @RequestParam(name = "world", required = false)
            String worldSlug) {

        return rabbitHoleService.discoverRabbitHole(worldSlug);
    }

    @GetMapping
    public List<RabbitHoleView> getLibrary() {
        return rabbitHoleService.getLibrary();
    }

    @PutMapping("/{id}/save")
    public RabbitHoleView saveForLater(
            @PathVariable("id") Long topicId) {

        return rabbitHoleService.saveForLater(topicId);
    }

    @PutMapping("/{id}/explore")
    public RabbitHoleView startExploring(
            @PathVariable("id") Long topicId) {

        return rabbitHoleService.startExploring(topicId);
    }

    @PutMapping("/{id}/complete")
    public RabbitHoleView completeRabbitHole(
            @PathVariable("id") Long topicId,
            @RequestBody(required = false)
            CompleteRabbitHoleRequest request) {

        String reflection =
                request == null
                        ? null
                        : request.reflection();

        return rabbitHoleService.completeRabbitHole(
                topicId,
                reflection
        );
    }

    public record CompleteRabbitHoleRequest(
            String reflection) {
    }

    @DeleteMapping("/{id}")
    public void deleteRabbitHole(
            @PathVariable("id") Long topicId) {

        rabbitHoleService.deleteRabbitHole(topicId);
    }
}