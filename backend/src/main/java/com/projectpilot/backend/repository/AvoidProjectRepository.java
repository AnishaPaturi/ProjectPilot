package com.projectpilot.backend.repository;

import com.projectpilot.backend.entity.AvoidProject;
import com.projectpilot.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AvoidProjectRepository extends JpaRepository<AvoidProject, Long> {
    List<AvoidProject> findByUser(User user);
    List<AvoidProject> findByUserId(Long userId);
    void deleteByUserId(Long userId);
}
