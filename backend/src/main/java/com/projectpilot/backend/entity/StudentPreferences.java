package com.projectpilot.backend.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "student_preferences")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudentPreferences {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String domain;

    @Column(nullable = false)
    private String skills;

    @Column(name = "team_size", nullable = false)
    private Integer teamSize;

    @Column(nullable = false)
    private Integer duration;
}
