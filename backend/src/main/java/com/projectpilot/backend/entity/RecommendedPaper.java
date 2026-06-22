package com.projectpilot.backend.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "recommended_papers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecommendedPaper {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 500)
    private String title;

    @Column(length = 500)
    private String authors;

    @Column(name = "publication_year")
    private Integer year;

    private String journal;

    private String doi;

    private String link;

    @Column(name = "paper_abstract", columnDefinition = "TEXT")
    private String abstractText;

    private Double score;

    @Column(name = "implementation_plan", columnDefinition = "TEXT")
    private String implementationPlan;
}
