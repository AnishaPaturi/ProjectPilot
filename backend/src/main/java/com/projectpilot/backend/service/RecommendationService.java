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
            "1. Paper Search Agent: Retrieve 3 highly realistic, high-fidelity IEEE journal or transaction papers published in 2025 or 2026 specifically about the subdomain: '%s'. " +
            "Directly cross-reference this subdomain with high-demand, cutting-edge computer science subfields (such as Agentic AI, Multi-Agent Systems, Cybersecurity, Digital Forensics, Smart Energy, Industry 4.0, Digital Twins, Healthcare AI, Explainable AI (XAI), Edge AI, Federated Learning, and Reinforcement Learning). The recommended papers must show integration of these advanced CS subfields to be suitable for high-scoring CSE final-year projects.\n" +
            "2. Paper Verification Agent: Verify each paper. Ensure it is an IEEE Journal or Transaction paper (e.g. IEEE Transactions on Cloud Computing, IEEE Access, IEEE Journal of IoT). Ensure it is NOT a conference paper, survey paper, or review paper. It must have a valid-looking DOI and IEEE Xplore link. Conference papers, workshop papers, or symposium proceedings are STRICTLY PROHIBITED and must not be returned.\n" +
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
            "      \"journal\": \"IEEE Journal or Transactions Name (e.g. IEEE Transactions on Smart Grid. MUST NOT be a conference or proceedings)\",\n" +
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
                    String journal = (String) paperMap.get("journal");
                    if (journal != null) {
                        String lowerJournal = journal.toLowerCase();
                        if (lowerJournal.contains("conference") || lowerJournal.contains("proceedings") ||
                            lowerJournal.contains("workshop") || lowerJournal.contains("symposium") ||
                            lowerJournal.contains("conf.") || lowerJournal.contains("conf ")) {
                            System.out.println("Skipping conference paper: " + paperMap.get("title") + " from " + journal);
                            continue; // Skip conference papers
                        }
                    }

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
                            .journal(journal)
                            .doi((String) paperMap.get("doi"))
                            .link(getDirectDownloadUrl((String) paperMap.get("link"), (String) paperMap.get("doi")))
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
    public String suggestBroadDomains() {
        return """
            {
              "domains": [
                {
                  "name": "Healthcare",
                  "description": "Improving medical services, diagnosis, patient care, and hospital management.",
                  "projectAreas": [
                    "Disease Prediction",
                    "Medical Image Analysis",
                    "AI Diagnostic Systems",
                    "Electronic Health Records (EHR)",
                    "Telemedicine",
                    "Drug Recommendation Systems",
                    "Mental Health Monitoring",
                    "Wearable Health Devices",
                    "Hospital Resource Management",
                    "Personalized Medicine"
                  ]
                },
                {
                  "name": "Education",
                  "description": "Enhancing learning experiences and academic management.",
                  "projectAreas": [
                    "Adaptive Learning Systems",
                    "AI Tutors",
                    "Student Performance Prediction",
                    "Online Examination Systems",
                    "Learning Analytics",
                    "Attendance Automation",
                    "Personalized Learning Paths",
                    "Educational Chatbots",
                    "Virtual Labs",
                    "Academic Recommendation Systems"
                  ]
                },
                {
                  "name": "Career & Recruitment",
                  "description": "Helping people find jobs and helping companies hire talent.",
                  "projectAreas": [
                    "Resume Screening",
                    "Job Recommendation Engines",
                    "Skill Gap Analysis",
                    "Career Guidance Agents",
                    "Interview Preparation Systems",
                    "AI Recruiters",
                    "Talent Matching Platforms",
                    "Employee Assessment Tools",
                    "Professional Networking Platforms",
                    "Career Roadmap Generation"
                  ]
                },
                {
                  "name": "Finance",
                  "description": "Banking, investments, loans, and financial decision-making.",
                  "projectAreas": [
                    "Loan Approval Prediction",
                    "Credit Scoring",
                    "Fraud Detection",
                    "Expense Tracking",
                    "Financial Planning Agents",
                    "Budget Optimization",
                    "Digital Banking",
                    "FinTech Solutions",
                    "Risk Analysis",
                    "Cryptocurrency Analytics"
                  ]
                },
                {
                  "name": "Stock Market",
                  "description": "Trading, investment analysis, and market prediction.",
                  "projectAreas": [
                    "Stock Price Prediction",
                    "Portfolio Management",
                    "Sentiment Analysis",
                    "Trading Bots",
                    "Investment Advisors",
                    "Risk Assessment",
                    "Market Trend Analysis",
                    "News Impact Prediction",
                    "Algorithmic Trading",
                    "Financial Forecasting"
                  ]
                },
                {
                  "name": "Real Estate & Interior Design",
                  "description": "Property management, architecture, and smart living. Fits Habitat AI concepts.",
                  "projectAreas": [
                    "Property Price Prediction",
                    "Smart Home Systems",
                    "Interior Design Recommendation",
                    "Virtual Property Tours",
                    "GIS-based Land Analysis",
                    "Digital Twins",
                    "Construction Planning",
                    "Rental Recommendation Systems",
                    "Property Fraud Detection",
                    "Sustainable Architecture"
                  ]
                },
                {
                  "name": "Agriculture",
                  "description": "Improving farming and food production.",
                  "projectAreas": [
                    "Crop Yield Prediction",
                    "Smart Irrigation",
                    "Pest Detection",
                    "Soil Analysis",
                    "Agricultural Drones",
                    "Precision Farming",
                    "Weather Prediction",
                    "Farm Management Systems",
                    "Livestock Monitoring",
                    "Crop Disease Detection"
                  ]
                },
                {
                  "name": "Cybersecurity",
                  "description": "Protecting systems, networks, and data.",
                  "projectAreas": [
                    "Intrusion Detection Systems",
                    "Malware Analysis",
                    "Phishing Detection",
                    "Threat Intelligence",
                    "Blockchain Security",
                    "Zero Trust Security",
                    "Vulnerability Assessment",
                    "Digital Forensics",
                    "Identity Management",
                    "Security Monitoring Agents"
                  ]
                },
                {
                  "name": "Logistics",
                  "description": "Transportation, delivery, and supply chain optimization.",
                  "projectAreas": [
                    "Route Optimization",
                    "Supply Chain Analytics",
                    "Warehouse Automation",
                    "Fleet Management",
                    "Delivery Prediction",
                    "Smart Inventory Systems",
                    "Last-Mile Delivery",
                    "Shipment Tracking",
                    "Demand Forecasting",
                    "Autonomous Logistics"
                  ]
                },
                {
                  "name": "Environment & Sustainability",
                  "description": "Protecting the environment and sustainable development.",
                  "projectAreas": [
                    "Carbon Footprint Analysis",
                    "Waste Management",
                    "Pollution Monitoring",
                    "Renewable Energy Optimization",
                    "Smart Water Management",
                    "Climate Prediction",
                    "Green Building Solutions",
                    "Environmental Risk Assessment",
                    "Biodiversity Monitoring",
                    "Sustainability Analytics"
                  ]
                },
                {
                  "name": "HR & Recruitment",
                  "description": "Employee management and organizational growth.",
                  "projectAreas": [
                    "Employee Attrition Prediction",
                    "Workforce Analytics",
                    "HR Chatbots",
                    "Performance Evaluation",
                    "Talent Acquisition",
                    "Employee Engagement",
                    "Payroll Automation",
                    "Skill Development Tracking",
                    "Organizational Analytics",
                    "Workforce Planning"
                  ]
                },
                {
                  "name": "Travel & Tourism",
                  "description": "Enhancing travel experiences and tourism services.",
                  "projectAreas": [
                    "Trip Planning Agents",
                    "Hotel Recommendation Systems",
                    "Tourism Analytics",
                    "Smart Travel Assistants",
                    "Route Optimization",
                    "Virtual Tourism",
                    "Personalized Itinerary Generation",
                    "Travel Risk Assessment",
                    "Language Assistance Systems",
                    "Tourist Behavior Analysis"
                  ]
                },
                {
                  "name": "Government Services",
                  "description": "Public administration and citizen services.",
                  "projectAreas": [
                    "Smart Governance",
                    "Digital Identity Systems",
                    "Public Service Automation",
                    "Citizen Grievance Management",
                    "Document Verification",
                    "E-Governance Platforms",
                    "Welfare Distribution Systems",
                    "Traffic Management",
                    "Smart City Solutions",
                    "Public Data Analytics"
                  ]
                },
                {
                  "name": "Sports Analytics",
                  "description": "Performance improvement and sports intelligence.",
                  "projectAreas": [
                    "Player Performance Analysis",
                    "Injury Prediction",
                    "Match Outcome Prediction",
                    "Team Strategy Optimization",
                    "Wearable Sports Analytics",
                    "Athlete Monitoring",
                    "Sports Video Analysis",
                    "Talent Scouting",
                    "Fan Engagement Platforms",
                    "Sports Recommendation Systems"
                  ]
                },
                {
                  "name": "Insurance",
                  "description": "Risk management and insurance automation.",
                  "projectAreas": [
                    "Claim Fraud Detection",
                    "Insurance Recommendation Systems",
                    "Risk Assessment Models",
                    "Automated Claims Processing",
                    "Premium Prediction",
                    "Customer Retention Analytics",
                    "Health Insurance Analytics",
                    "Vehicle Insurance Systems",
                    "Policy Management Platforms",
                    "AI Insurance Agents"
                  ]
                },
                {
                  "name": "LegalTech",
                  "description": "Legal document generation, court scheduling, and compliance.",
                  "projectAreas": [
                    "Legal Document Generator",
                    "Court Scheduling Systems",
                    "Legal Compliance Monitoring"
                  ]
                },
                {
                  "name": "Media & Journalism",
                  "description": "Technology for news, content, and information verification.",
                  "projectAreas": [
                    "Fake News Detection",
                    "Deepfake Detection",
                    "AI News Summarization",
                    "Personalized News Platforms",
                    "Content Recommendation Systems",
                    "Fact-Checking Agents"
                  ]
                },
                {
                  "name": "Entertainment Technology",
                  "description": "Technology for movies, music, OTT, and content creation.",
                  "projectAreas": [
                    "AI Video Generation",
                    "Movie Recommendation Systems",
                    "Music Recommendation Systems",
                    "Content Moderation",
                    "Audience Analytics",
                    "Virtual Influencers"
                  ]
                },
                {
                  "name": "Smart Cities",
                  "description": "Urban infrastructure powered by AI and IoT.",
                  "projectAreas": [
                    "Smart Traffic Management",
                    "Intelligent Parking Systems",
                    "Smart Waste Collection",
                    "Urban Planning Analytics",
                    "Public Safety Systems",
                    "Smart Street Lighting"
                  ]
                },
                {
                  "name": "Emergency & Disaster Response",
                  "description": "Disaster prediction and emergency management.",
                  "projectAreas": [
                    "Flood Prediction Systems",
                    "Wildfire Detection",
                    "Earthquake Damage Assessment",
                    "Emergency Route Planning",
                    "Disaster Resource Allocation",
                    "Rescue Coordination Systems"
                  ]
                },
                {
                  "name": "Aerospace & Aviation",
                  "description": "A rapidly growing aviation domain with AI integration.",
                  "projectAreas": [
                    "Flight Delay Prediction",
                    "Drone Intelligence",
                    "Air Traffic Optimization",
                    "Predictive Aircraft Maintenance",
                    "Autonomous UAVs",
                    "Satellite Analytics"
                  ]
                },
                {
                  "name": "Marine & Ocean Technology",
                  "description": "Ocean pollution, fisheries, and underwater systems.",
                  "projectAreas": [
                    "Ocean Pollution Monitoring",
                    "Smart Fisheries",
                    "Coastal Risk Prediction",
                    "Marine Ecosystem Analytics",
                    "Underwater Drone Systems"
                  ]
                },
                {
                  "name": "Computational Biology",
                  "description": "Protein structure, gene analysis, and drug discovery.",
                  "projectAreas": [
                    "Protein Structure Prediction",
                    "Gene Analysis",
                    "Drug Discovery",
                    "Disease Risk Prediction",
                    "Personalized Treatment Models"
                  ]
                },
                {
                  "name": "Human Behavior Analytics",
                  "description": "AI for understanding human actions and emotions.",
                  "projectAreas": [
                    "Emotion Recognition",
                    "Human Activity Recognition",
                    "Crowd Behavior Analysis",
                    "Consumer Behavior Prediction",
                    "Social Media Intelligence"
                  ]
                },
                {
                  "name": "Enterprise Automation",
                  "description": "Intelligent document processing and workflow optimization.",
                  "projectAreas": [
                    "Intelligent Document Processing",
                    "Business Process Automation",
                    "AI Meeting Assistants",
                    "Workflow Optimization",
                    "Enterprise Knowledge Agents"
                  ]
                },
                {
                  "name": "Industrial Automation",
                  "description": "Industry 4.0 and manufacturing optimization.",
                  "projectAreas": [
                    "Predictive Maintenance",
                    "Defect Detection",
                    "Digital Factory Twins",
                    "Production Optimization",
                    "Smart Manufacturing Systems"
                  ]
                },
                {
                  "name": "Battery & EV Technology",
                  "description": "Electric vehicles, charging, and battery health.",
                  "projectAreas": [
                    "EV Charging Optimization",
                    "Battery Health Prediction",
                    "Smart Charging Networks",
                    "Vehicle-to-Grid Systems",
                    "EV Route Planning"
                  ]
                },
                {
                  "name": "Knowledge Management",
                  "description": "Research assistants, knowledge graphs, and search.",
                  "projectAreas": [
                    "Research Paper Assistants",
                    "Knowledge Graph Systems",
                    "Enterprise Search Engines",
                    "AI Research Agents",
                    "Academic Recommendation Systems"
                  ]
                },
                {
                  "name": "Accessibility Technology",
                  "description": "Sign translation, navigation, and reading assistants.",
                  "projectAreas": [
                    "Sign Language Translation",
                    "Voice-Based Navigation",
                    "Smart Wheelchair Systems",
                    "AI Reading Assistants",
                    "Accessibility Auditing Tools"
                  ]
                },
                {
                  "name": "Cognitive Computing",
                  "description": "Advanced AI reasoning and autonomous decision systems.",
                  "projectAreas": [
                    "Multi-Agent Systems",
                    "Autonomous Decision Systems",
                    "Explainable AI",
                    "Human-AI Collaboration",
                    "Cognitive Assistants"
                  ]
                },
                {
                  "name": "Space Technology",
                  "description": "Satellite intelligence, planetary mapping, and debris tracking.",
                  "projectAreas": [
                    "Satellite Image Intelligence",
                    "Space Debris Tracking",
                    "Orbital Prediction Systems",
                    "Planetary Mapping",
                    "Remote Sensing Analytics"
                  ]
                },
                {
                  "name": "Design Technology",
                  "description": "AI for interior, logo, fashion, and architecture generation.",
                  "projectAreas": [
                    "AI Interior Designers",
                    "Logo Generation Systems",
                    "Fashion Recommendation",
                    "Architecture Generation",
                    "Creative Content Agents"
                  ]
                },
                {
                  "name": "Developer Tools",
                  "description": "Code review, bug detection, and code generation.",
                  "projectAreas": [
                    "AI Code Review Systems",
                    "Automated Bug Detection",
                    "Code Generation Agents",
                    "DevOps Intelligence Platforms",
                    "Software Architecture Assistants"
                  ]
                },
                {
                  "name": "Digital Humanities",
                  "description": "Heritage preservation, ancient documents, and cultural graphs.",
                  "projectAreas": [
                    "Heritage Preservation",
                    "Ancient Document Analysis",
                    "Museum Intelligence Systems",
                    "Historical Knowledge Graphs",
                    "Cultural Recommendation Systems"
                  ]
                },
                {
                  "name": "Research & Scientific Discovery",
                  "description": "Scientific literature, gap analysis, and experiment design.",
                  "projectAreas": [
                    "Scientific Literature Agents",
                    "Research Gap Identification",
                    "Automated Experiment Design",
                    "Citation Intelligence Systems",
                    "Scientific Knowledge Mining"
                  ]
                }
              ]
            }
            """;
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

    private String getDirectDownloadUrl(String link, String doi) {
        // 1. Try converting abstract IEEE Xplore link to stamp PDF viewer link first (never blocked)
        if (link != null && link.contains("ieeexplore.ieee.org/document/")) {
            String[] parts = link.split("/document/");
            if (parts.length > 1) {
                String arnumber = parts[1].replaceAll("[^0-9]", "");
                if (!arnumber.isEmpty()) {
                    return "https://ieeexplore.ieee.org/stampPDF/getPDF.jsp?tp=&arnumber=" + arnumber;
                }
            }
        }

        // 2. Try official DOI link (never blocked)
        if (doi != null && !doi.trim().isEmpty() && !doi.toLowerCase().contains("N/A")) {
            return "https://doi.org/" + doi.trim();
        }

        // 3. Fallback to standard link
        return (link != null && !link.trim().isEmpty()) ? link : "#";
    }
}
