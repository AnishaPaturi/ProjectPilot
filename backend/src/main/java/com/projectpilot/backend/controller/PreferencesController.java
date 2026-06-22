package com.projectpilot.backend.controller;

import com.projectpilot.backend.dto.PreferencesRequest;
import com.projectpilot.backend.dto.PreferencesResponse;
import com.projectpilot.backend.service.PreferencesService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/preferences")
@CrossOrigin(origins = "*")
public class PreferencesController {

    private final PreferencesService preferencesService;

    public PreferencesController(PreferencesService preferencesService) {
        this.preferencesService = preferencesService;
    }

    @GetMapping("/{userId}")
    public ResponseEntity<?> getPreferences(@PathVariable Long userId) {
        try {
            PreferencesResponse response = preferencesService.getPreferences(userId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/{userId}")
    public ResponseEntity<?> savePreferences(@PathVariable Long userId, @RequestBody PreferencesRequest request) {
        try {
            PreferencesResponse response = preferencesService.savePreferences(userId, request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
