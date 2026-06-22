package com.projectpilot.backend.controller;

import com.projectpilot.backend.entity.RecommendedPaper;
import com.projectpilot.backend.repository.RecommendedPaperRepository;
import com.projectpilot.backend.service.PdfService;
import com.projectpilot.backend.service.RecommendationService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/recommendations")
@CrossOrigin(origins = "*")
public class RecommendationController {

    private final RecommendationService recommendationService;
    private final RecommendedPaperRepository recommendedPaperRepository;
    private final PdfService pdfService;

    public RecommendationController(RecommendationService recommendationService,
                                    RecommendedPaperRepository recommendedPaperRepository,
                                    PdfService pdfService) {
        this.recommendationService = recommendationService;
        this.recommendedPaperRepository = recommendedPaperRepository;
        this.pdfService = pdfService;
    }

    @PostMapping("/generate/{userId}")
    public ResponseEntity<?> generateRecommendations(@PathVariable Long userId) {
        try {
            List<RecommendedPaper> papers = recommendationService.generateRecommendations(userId);
            return ResponseEntity.ok(papers);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/saved/{userId}")
    public ResponseEntity<?> getSavedRecommendations(@PathVariable Long userId) {
        try {
            List<RecommendedPaper> papers = recommendationService.getSavedRecommendations(userId);
            return ResponseEntity.ok(papers);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/download/{paperId}")
    public ResponseEntity<byte[]> downloadReport(@PathVariable Long paperId) {
        try {
            RecommendedPaper paper = recommendedPaperRepository.findById(paperId)
                    .orElseThrow(() -> new RuntimeException("Paper not found!"));
            byte[] pdfBytes = pdfService.generatePdfReport(paper);
            
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"project_roadmap_" + paperId + ".pdf\"")
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(pdfBytes);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }
}

