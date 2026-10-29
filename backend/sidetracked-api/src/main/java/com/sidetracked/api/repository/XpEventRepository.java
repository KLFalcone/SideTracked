package com.sidetracked.api.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sidetracked.api.model.XpEvent;

public interface XpEventRepository extends JpaRepository<XpEvent, Long> {

    List<XpEvent> findByUserIdOrderByCreatedAtDescIdDesc(Long userId);
}
