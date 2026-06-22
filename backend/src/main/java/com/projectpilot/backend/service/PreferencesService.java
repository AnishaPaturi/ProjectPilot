package com.projectpilot.backend.service;

import com.projectpilot.backend.dto.PreferencesRequest;
import com.projectpilot.backend.dto.PreferencesResponse;
import com.projectpilot.backend.entity.AvoidProject;
import com.projectpilot.backend.entity.StudentPreferences;
import com.projectpilot.backend.entity.User;
import com.projectpilot.backend.repository.AvoidProjectRepository;
import com.projectpilot.backend.repository.StudentPreferencesRepository;
import com.projectpilot.backend.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PreferencesService {

    private final UserRepository userRepository;
    private final StudentPreferencesRepository preferencesRepository;
    private final AvoidProjectRepository avoidProjectRepository;

    public PreferencesService(UserRepository userRepository,
                              StudentPreferencesRepository preferencesRepository,
                              AvoidProjectRepository avoidProjectRepository) {
        this.userRepository = userRepository;
        this.preferencesRepository = preferencesRepository;
        this.avoidProjectRepository = avoidProjectRepository;
    }

    @Transactional
    public PreferencesResponse savePreferences(Long userId, PreferencesRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found!"));

        // 1. Save or Update StudentPreferences
        StudentPreferences preferences = preferencesRepository.findByUser(user)
                .orElse(new StudentPreferences());
        
        preferences.setUser(user);
        preferences.setDomain(request.getDomain());
        preferences.setSkills(request.getSkills());
        preferences.setTeamSize(request.getTeamSize());
        preferences.setDuration(request.getDuration());
        preferencesRepository.save(preferences);

        // 2. Update Avoid List (delete old, save new)
        avoidProjectRepository.deleteByUserId(userId);
        if (request.getAvoidList() != null) {
            List<AvoidProject> avoidProjects = request.getAvoidList().stream()
                    .map(name -> AvoidProject.builder()
                            .user(user)
                            .projectName(name)
                            .build())
                    .collect(Collectors.toList());
            avoidProjectRepository.saveAll(avoidProjects);
        }

        return PreferencesResponse.builder()
                .domain(preferences.getDomain())
                .skills(preferences.getSkills())
                .teamSize(preferences.getTeamSize())
                .duration(preferences.getDuration())
                .avoidList(request.getAvoidList())
                .build();
    }

    @Transactional(readOnly = true)
    public PreferencesResponse getPreferences(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found!"));

        StudentPreferences preferences = preferencesRepository.findByUser(user).orElse(null);
        List<AvoidProject> avoidProjects = avoidProjectRepository.findByUser(user);
        List<String> avoidListNames = avoidProjects.stream()
                .map(AvoidProject::getProjectName)
                .collect(Collectors.toList());

        if (preferences == null) {
            return PreferencesResponse.builder()
                    .domain("")
                    .skills("")
                    .teamSize(0)
                    .duration(0)
                    .avoidList(Collections.emptyList())
                    .build();
        }

        return PreferencesResponse.builder()
                .domain(preferences.getDomain())
                .skills(preferences.getSkills())
                .teamSize(preferences.getTeamSize())
                .duration(preferences.getDuration())
                .avoidList(avoidListNames)
                .build();
    }
}
