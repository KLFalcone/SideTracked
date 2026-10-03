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
import org.springframework.web.bind.annotation.RestController;

import com.sidetracked.api.model.Quest;
import com.sidetracked.api.service.QuestService;

@RestController
@RequestMapping("/api/quests")
@CrossOrigin(origins = "http://localhost:4200")
public class QuestController {

    private final QuestService questService;

    public QuestController(QuestService questService) {
        this.questService = questService;
    }

    @GetMapping
    public List<Quest> getAllQuests() {
        return questService.getAllQuests();
    }

    @PostMapping
    public Quest createQuest(
            @RequestBody Quest quest) {

        return questService.createQuest(quest);
    }

    @PutMapping("/{id}")
    public Quest updateQuest(
            @PathVariable Long id,
            @RequestBody Quest updatedQuest) {

        return questService.updateQuest(
                id,
                updatedQuest
        );
    }

    @PutMapping("/{id}/complete")
    public Quest completeQuest(
            @PathVariable Long id) {

        return questService.completeQuest(id);
    }

    @DeleteMapping("/{id}")
    public void deleteQuest(
            @PathVariable Long id) {

        questService.deleteQuest(id);
    }
}