package com.sidetracked.api.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.sidetracked.api.model.RabbitHole;
import com.sidetracked.api.model.RabbitHoleStatus;
import com.sidetracked.api.repository.RabbitHoleRepository;

@Service
public class RabbitHoleService {

    private final RabbitHoleRepository rabbitHoleRepository;

    public RabbitHoleService(
            RabbitHoleRepository rabbitHoleRepository) {

        this.rabbitHoleRepository = rabbitHoleRepository;
    }

    public RabbitHole discoverRabbitHole() {

        /*
         * TEMPORARY SHOWCASE PROVIDER
         *
         * This is intentionally behind the service layer.
         *
         * The frontend does not care where Rabbit Holes come from.
         * Later this method will call the Curiosity Engine, which
         * will dynamically pull subjects from external providers,
         * avoid repeats, apply interest weighting, and attach sources.
         */

        RabbitHole rabbitHole = new RabbitHole();

        rabbitHole.setWorld(
                "REALITY & SIMULATION"
        );

        rabbitHole.setTitle(
                "THE GAME OF LIFE"
        );

        rabbitHole.setHook(
                "In 1970, mathematician John Conway created "
                + "an extremely simple set of rules. "
                + "Those rules can produce systems that appear "
                + "to move, reproduce, and even perform computation."
        );

        rabbitHole.setResearchPrompt(
                "How can complex behavior emerge from rules "
                + "that contain almost no complexity themselves?"
        );

        rabbitHole.setXp(25);

        rabbitHole.setStatus(
                RabbitHoleStatus.DISCOVERED
        );

        return rabbitHoleRepository.save(rabbitHole);
    }

    public List<RabbitHole> getLibrary() {
        return rabbitHoleRepository
                .findAllByOrderByDiscoveredAtDesc();
    }

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

    public void deleteRabbitHole(Long id) {

        if (!rabbitHoleRepository.existsById(id)) {
            return;
        }

        rabbitHoleRepository.deleteById(id);
    }

    private RabbitHole getRabbitHole(Long id) {

        return rabbitHoleRepository
                .findById(id)
                .orElseThrow(
                        () -> new IllegalArgumentException(
                                "Rabbit Hole not found: " + id
                        )
                );
    }
}