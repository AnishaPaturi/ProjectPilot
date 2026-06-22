package com.projectpilot.backend.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "avoid_projects")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AvoidProject {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "project_name", nullable = false)
    private String projectName;
}
