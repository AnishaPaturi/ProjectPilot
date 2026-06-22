package com.projectpilot.backend.repository;

import com.projectpilot.backend.entity.StudentPreferences;
import com.projectpilot.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StudentPreferencesRepository extends JpaRepository<StudentPreferences, Long> {
    Optional<StudentPreferences> findByUser(User user);
    Optional<StudentPreferences> findByUserId(Long userId);
}
