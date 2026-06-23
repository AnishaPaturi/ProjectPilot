package com.projectpilot.backend.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class GeminiService {

    @Value("${gemini.api.key:}")
    private String apiKey;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public GeminiService() {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    @PostConstruct
    public void init() {
        if (apiKey == null || apiKey.trim().isEmpty()) {
            System.out.println("GeminiService: API Key is MISSING in environment/application.properties!");
        } else {
            System.out.println("GeminiService: API Key loaded successfully (length: " + apiKey.length() + ")");
        }
    }

    public String generateContent(String prompt, boolean jsonMode) {
        if (apiKey == null || apiKey.trim().isEmpty()) {
            System.out.println("Warning: GEMINI_API_KEY is not set. Using Mock Response.");
            return getMockResponse(prompt);
        }

        try {
            String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=" + apiKey;

            // Prepare Request Body
            Map<String, Object> textPart = new HashMap<>();
            textPart.put("text", prompt);

            Map<String, Object> partContainer = new HashMap<>();
            partContainer.put("parts", List.of(textPart));

            Map<String, Object> contentContainer = new HashMap<>();
            contentContainer.put("contents", List.of(partContainer));

            if (jsonMode) {
                Map<String, Object> generationConfig = new HashMap<>();
                generationConfig.put("responseMimeType", "application/json");
                contentContainer.put("generationConfig", generationConfig);
            }

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(contentContainer, headers);
            ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                // Extract response text: response.body.candidates[0].content.parts[0].text
                List candidates = (List) response.getBody().get("candidates");
                if (candidates != null && !candidates.isEmpty()) {
                    Map candidate = (Map) candidates.get(0);
                    Map content = (Map) candidate.get("content");
                    if (content != null) {
                        List parts = (List) content.get("parts");
                        if (parts != null && !parts.isEmpty()) {
                            Map part = (Map) parts.get(0);
                            return cleanJsonResponse((String) part.get("text"));
                        }
                    }
                }
            }
            throw new RuntimeException("Empty response or failed response from Gemini API");
        } catch (Exception e) {
            System.err.println("Gemini API call failed: " + e.getMessage() + ". Falling back to Mock.");
            return getMockResponse(prompt);
        }
    }

    private String getMockResponse(String prompt) {
        String lowerPrompt = prompt.toLowerCase();

        // 1. Mock response for Domain Analyzer
        if (lowerPrompt.contains("subdomain") || lowerPrompt.contains("analyze domain")) {
            return """
            {
              "domain": "Cybersecurity + AI",
              "subdomains": [
                {
                  "name": "Intelligent Malware Detection",
                  "description": "Using deep learning and neural networks to detect polymorphic malware in real-time.",
                  "keywords": ["malware detection", "deep learning", "polymorphic", "neural networks"]
                },
                {
                  "name": "Threat Intelligence Extraction",
                  "description": "NLP-based models for automated extraction of indicators of compromise (IOCs) from unstructured security blogs.",
                  "keywords": ["NLP", "threat intelligence", "IOC extraction", "unstructured data"]
                },
                {
                  "name": "Phishing Detection via GNNs",
                  "description": "Graph Neural Networks applied to web-link structure and domain relationship maps to detect sophisticated phishing websites.",
                  "keywords": ["phishing detection", "GNN", "graph neural networks", "phishing links"]
                }
              ]
            }
            """;
        }

        // 2. Mock response for Recommendations and Filtering
        if (lowerPrompt.contains("recommend") || lowerPrompt.contains("papers")) {
            return """
            {
              "papers": [
                {
                  "title": "Deep Learning-Based Intrusion Detection System for Internet of Things: A Review",
                  "authors": "J. Doe, K. Smith",
                  "year": 2024,
                  "journal": "IEEE Access",
                  "doi": "10.1109/ACCESS.2023.3339824",
                  "link": "https://ieeexplore.ieee.org/document/10360814",
                  "abstract": "Traditional signature-based malware detection systems fail to recognize advanced polymorphic malware. This paper proposes DeepMalAnal, a multimodal neural network that integrates static API call sequences and dynamic control-flow-graph features. Our approach achieves 99.4% accuracy on the 2025 malware dataset and provides robustness against adversarial evasion techniques.",
                  "score": 94.5,
                  "difficulty": "Medium",
                  "novelty": "High",
                  "feasibility": "High"
                },
                {
                  "title": "Security and Privacy in Cloud Computing: A Survey",
                  "authors": "A. Gupta, M. Patel",
                  "year": 2023,
                  "journal": "IEEE Access",
                  "doi": "10.1109/ACCESS.2023.3273187",
                  "link": "https://ieeexplore.ieee.org/document/10121303",
                  "abstract": "Cyber threat intelligence enables proactive security measures, but extracting IOCs manually is time-consuming. This paper introduces SecNLP, an ensemble transformer model designed to read unstructured security reports and output machine-readable STIX JSON objects with 97.8% F1-score. We discuss deployment scopes and dataset challenges for enterprise servers.",
                  "score": 91.0,
                  "difficulty": "Easy",
                  "novelty": "Medium",
                  "feasibility": "High"
                }
              ]
            }
            """;
        }

        // 3. Mock response for Planning Agent
        if (lowerPrompt.contains("plan") || lowerPrompt.contains("roadmap")) {
            return """
            {
              "modules": [
                {
                  "name": "Data Preprocessing and Feature Extraction Module",
                  "description": "Parses raw PE files, extracts API call sequences, and builds static control-flow graphs."
                },
                {
                  "name": "Multimodal Deep Learning Classifier",
                  "description": "Implements the dual-stream CNN-BiLSTM network in PyTorch to classify files as benign or malicious."
                },
                {
                  "name": "Explainable AI (XAI) dashboard",
                  "description": "Utilizes SHAP/LIME to explain why a particular binary was flagged as malware, indicating critical API triggers."
                }
              ],
              "architecture": "Client-Server model. Python Flask/FastAPI acts as the AI inference microservice, while a Spring Boot application serves as the main business logic API with a React dashboard frontend.",
              "noveltyAdditions": "Adding a Local Explainable AI module using SHAP to highlight exactly which API calls triggered the malware classification, resolving the 'black-box' issue for security analysts.",
              "techStack": "React, TailwindCSS, Spring Boot, FastAPI, PyTorch, MySQL, SHAP library.",
              "roadmap": [
                { "week": "Week 1-2", "tasks": "Establish backend Spring Boot schema, database setup, and mock API endpoints. Initialize PyTorch model configuration." },
                { "week": "Week 3-4", "tasks": "Build static feature extraction script. Implement CNN-LSTM architecture and begin model training on benchmark dataset." },
                { "week": "Week 5-6", "tasks": "Create React dashboard frontend. Connect frontend forms and design visualization graphs for dataset analysis." },
                { "week": "Week 7-8", "tasks": "Integrate SHAP explainability model with frontend. Conduct system integration testing, performance evaluation, and compile final documentation." }
              ]
            }
            """;
        }

        return "{}";
    }

    private String cleanJsonResponse(String responseText) {
        if (responseText == null) return null;
        String clean = responseText.trim();
        if (clean.startsWith("```")) {
            int firstNewline = clean.indexOf('\n');
            if (firstNewline != -1) {
                clean = clean.substring(firstNewline).trim();
            }
            if (clean.endsWith("```")) {
                clean = clean.substring(0, clean.length() - 3).trim();
            }
        }
        return clean;
    }
}
