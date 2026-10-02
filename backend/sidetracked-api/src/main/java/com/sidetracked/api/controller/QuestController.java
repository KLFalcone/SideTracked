package com.sidetracked.api.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class QuestController {
 
    @GetMapping("/api/quests")
    public String getQuests() {
        return "SideTracked API is alive!";
    }
}
