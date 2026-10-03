package com.sidetracked.api.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sidetracked.api.model.Quest;

public interface QuestRepository extends JpaRepository<Quest, Long> {
}