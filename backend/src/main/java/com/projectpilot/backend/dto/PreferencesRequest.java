package com.projectpilot.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PreferencesRequest {
    private String domain;
    private String skills;
    private Integer teamSize;
    private Integer duration;
    private List<String> avoidList;
}
