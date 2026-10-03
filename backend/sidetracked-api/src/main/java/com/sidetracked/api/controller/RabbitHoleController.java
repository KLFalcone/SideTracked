package com.sidetracked.api.controller;

import java.util.List;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sidetracked.api.model.RabbitHole;
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

    @PostMapping("/discover")
    public RabbitHole discoverRabbitHole() {
        return rabbitHoleService.discoverRabbitHole();
    }

    @GetMapping
    public List<RabbitHole> getLibrary() {
        return rabbitHoleService.getLibrary();
    }

    @PutMapping("/{id}/save")
    public RabbitHole saveForLater(
            @PathVariable Long id) {

        return rabbitHoleService.saveForLater(id);
    }

    @PutMapping("/{id}/explore")
    public RabbitHole startExploring(
            @PathVariable Long id) {

        return rabbitHoleService.startExploring(id);
    }

    @PutMapping("/{id}/complete")
    public RabbitHole completeRabbitHole(
            @PathVariable Long id) {

        return rabbitHoleService.completeRabbitHole(id);
    }

    @DeleteMapping("/{id}")
    public void deleteRabbitHole(
            @PathVariable Long id) {

        rabbitHoleService.deleteRabbitHole(id);
    }
}