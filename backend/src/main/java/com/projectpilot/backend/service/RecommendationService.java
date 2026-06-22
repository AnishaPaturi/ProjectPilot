package com.projectpilot.backend.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.projectpilot.backend.entity.AvoidProject;
import com.projectpilot.backend.entity.RecommendedPaper;
import com.projectpilot.backend.entity.StudentPreferences;
import com.projectpilot.backend.entity.User;
import com.projectpilot.backend.repository.AvoidProjectRepository;
import com.projectpilot.backend.repository.RecommendedPaperRepository;
import com.projectpilot.backend.repository.StudentPreferencesRepository;
import com.projectpilot.backend.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class RecommendationService {

    private final UserRepository userRepository;
    private final StudentPreferencesRepository preferencesRepository;
    private final RecommendedPaperRepository recommendedPaperRepository;
    private final AvoidProjectRepository avoidProjectRepository;
    private final GeminiService geminiService;
    private final ObjectMapper objectMapper;

    public RecommendationService(UserRepository userRepository,
                                 StudentPreferencesRepository preferencesRepository,
                                 RecommendedPaperRepository recommendedPaperRepository,
                                 AvoidProjectRepository avoidProjectRepository,
                                 GeminiService geminiService) {
        this.userRepository = userRepository;
        this.preferencesRepository = preferencesRepository;
        this.recommendedPaperRepository = recommendedPaperRepository;
        this.avoidProjectRepository = avoidProjectRepository;
        this.geminiService = geminiService;
        this.objectMapper = new ObjectMapper();
    }

    @Transactional
    public List<RecommendedPaper> generateRecommendations(Long userId, String subdomain) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found!"));

        List<AvoidProject> avoidProjects = avoidProjectRepository.findByUserId(userId);
        List<String> avoidList = avoidProjects.stream().map(AvoidProject::getProjectName).collect(Collectors.toList());
        String avoidListStr = String.join(", ", avoidList);

        // Construct the multi-agent pipeline prompt
        String prompt = String.format(
            "You are a Multi-Agent IEEE Paper Recommender and Project Planner System. Run the following workflow:\n" +
            "1. Paper Search Agent: Retrieve 3 highly realistic, high-fidelity IEEE journal or transaction papers published in 2025 or 2026 specifically about the subdomain: '%s'.\n" +
            "2. Paper Verification Agent: Verify each paper. Ensure it is an IEEE Journal or Transaction paper (e.g. IEEE Transactions on Cloud Computing, IEEE Access, IEEE Journal of IoT). Ensure it is NOT a conference paper, survey paper, or review paper. It must have a valid-looking DOI and IEEE Xplore link.\n" +
            "3. Similarity Agent: Filter out any papers that are similar or related to the following user avoid list: [%s]. If a paper is similar, replace it with a completely different topic.\n" +
            "4. Ranking Agent: Rate each paper out of 100 based on feasibility for a standard CSE final-year major project, implementation difficulty, innovation, and publication scope.\n" +
            "5. Project Planning Agent: For each paper, detail a complete implementation plan including sub-modules, architecture description, novelty additions, tech stack, and a week-by-week implementation roadmap.\n\n" +
            "Return the output in a single, valid JSON object with the following schema:\n" +
            "{\n" +
            "  \"papers\": [\n" +
            "    {\n" +
            "      \"title\": \"Paper Title\",\n" +
            "      \"authors\": \"Author Names\",\n" +
            "      \"year\": 2025 or 2026,\n" +
            "      \"journal\": \"IEEE Journal Name\",\n" +
            "      \"doi\": \"10.1109/...\",\n" +
            "      \"link\": \"IEEE Xplore URL\",\n" +
            "      \"abstract\": \"Abstract describing the research problem, methodology, and results\",\n" +
            "      \"score\": 85.5,\n" +
            "      \"implementationPlan\": {\n" +
            "        \"modules\": [\n" +
            "          { \"name\": \"Module Name\", \"description\": \"Description of what this module does\" }\n" +
            "        ],\n" +
            "        \"architecture\": \"High-level system architecture description\",\n" +
            "        \"noveltyAdditions\": \"Specific innovation additions to score higher marks\",\n" +
            "        \"techStack\": \"Recommended technology stack\",\n" +
            "        \"roadmap\": [\n" +
            "          { \"week\": \"Week X\", \"tasks\": \"Tasks to accomplish\" }\n" +
            "        ]\n" +
            "      }\n" +
            "    }\n" +
            "  ]\n" +
            "}\n\n" +
            "Make sure the response contains ONLY valid JSON and nothing else.",
            subdomain,
            avoidListStr
        );

        String jsonResponse = geminiService.generateContent(prompt, true);
        List<RecommendedPaper> recommendedPapersList = new ArrayList<>();

        try {
            Map<String, Object> responseMap = objectMapper.readValue(jsonResponse, new TypeReference<Map<String, Object>>() {});
            List<Map<String, Object>> papersJson = (List<Map<String, Object>>) responseMap.get("papers");

            if (papersJson != null) {
                // Delete previous recommendations for user
                recommendedPaperRepository.deleteByUserId(userId);

                for (Map<String, Object> paperMap : papersJson) {
                    Object planObj = paperMap.get("implementationPlan");
                    String planJsonString = planObj != null ? objectMapper.writeValueAsString(planObj) : "";

                    Number yearNum = (Number) paperMap.get("year");
                    Integer yearVal = yearNum != null ? yearNum.intValue() : 2025;

                    Number scoreNum = (Number) paperMap.get("score");
                    Double scoreVal = scoreNum != null ? scoreNum.doubleValue() : 80.0;

                    RecommendedPaper paper = RecommendedPaper.builder()
                            .user(user)
                            .title((String) paperMap.get("title"))
                            .authors((String) paperMap.get("authors"))
                            .year(yearVal)
                            .journal((String) paperMap.get("journal"))
                            .doi((String) paperMap.get("doi"))
                            .link((String) paperMap.get("link"))
                            .abstractText((String) paperMap.get("abstract"))
                            .score(scoreVal)
                            .implementationPlan(planJsonString)
                            .build();

                    recommendedPapersList.add(recommendedPaperRepository.save(paper));
                }
            }
        } catch (Exception e) {
            System.err.println("Failed to parse Gemini response for recommendations: " + e.getMessage());
            e.printStackTrace();
        }

        return recommendedPapersList;
    }

    @Transactional
    public String suggestSubdomains(String domain) {
        String prompt = String.format(
            "You are a Research Domain Analyzer. Given a broad domain of interest: '%s', suggest 4 specific, relevant, and cutting-edge final-year CSE project subdomains.\n" +
            "Return the output in a strict JSON format matching the following schema:\n" +
            "{\n" +
            "  \"subdomains\": [\n" +
            "    {\n" +
            "      \"name\": \"Subdomain Name\",\n" +
            "      \"description\": \"Brief explanation of what this research subdomain entails and why it makes a strong major project\"\n" +
            "    }\n" +
            "  ]\n" +
            "}\n\n" +
            "Make sure the response contains ONLY valid JSON and nothing else.",
            domain
        );

        return geminiService.generateContent(prompt, true);
    }

    @Transactional(readOnly = true)
    public List<RecommendedPaper> getSavedRecommendations(Long userId) {
        return recommendedPaperRepository.findByUserId(userId);
    }
}
