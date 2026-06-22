package com.projectpilot.backend.repository;

import com.projectpilot.backend.entity.RecommendedPaper;
import com.projectpilot.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RecommendedPaperRepository extends JpaRepository<RecommendedPaper, Long> {
    List<RecommendedPaper> findByUser(User user);
    List<RecommendedPaper> findByUserId(Long userId);
    void deleteByUserId(Long userId);
}
