package com.sidetracked.api.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sidetracked.api.model.XpEvent;
import com.sidetracked.api.model.XpSourceType;

public interface XpEventRepository extends JpaRepository<XpEvent, Long> {

    List<XpEvent> findByUserIdOrderByCreatedAtDescIdDesc(Long userId);

    boolean existsByUserIdAndSourceTypeAndSourceId(
            Long userId,
            XpSourceType sourceType,
            Long sourceId
    );
}
