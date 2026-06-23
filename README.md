# ProjectPilot AI 🚀
### AI Agent for IEEE Research Paper Recommendation and Capstone Major Project Planning

ProjectPilot AI is a multi-agent recommendation and planning platform designed to help final-year computer science students identify, evaluate, and plan their major projects. The system automates the process of matching student skills and domain preferences with verified, recent **IEEE Journal or Transactions** papers (published in **2025–2026**), cross-referencing them with cutting-edge CS subfields, filtering out redundant selections, and compiling a structured, week-by-week implementation roadmap.

---

## 📋 Problem Statement & Motivation
Finding a viable final-year capstone project is a major hurdle for engineering students:
- Students spend weeks searching through thousands of papers without finding high-quality, practical matches.
- Many accidentally pick conference papers, surveys, or review articles that are frequently rejected by university review boards.
- Determining whether a research paper is feasible for a 3-4 month implementation timeline with a standard tech stack is difficult.
- Students run the risk of choosing redundant project topics already selected by classmates.

**ProjectPilot AI** resolves these issues by serving as an intelligent research planner that searches, validates, scores, and designs concrete implementation plans for engineering majors.

---

## ✨ Core Features

1. **Intelligent Domain & Project Area Wizard**
   - Interactive multi-step wizard supporting **35 exhaustive vertical application domains** (e.g., Healthcare, Smart Cities, Computational Biology, Cybersecurity, Real Estate & Interior Design, Space Technology, Battery & EV Technology, and more).
   - Predefined project areas mapped to each domain to seed highly specific search queries.
   - Real-time keyword search and filtering of the entire domain database.

2. **Custom Avoid List Filtering**
   - Students can save a profile list of topics or project titles to avoid.
   - The AI recommendation agent checks paper metadata and abstracts against the avoid list using semantic comparison, filtering out redundant topics.

3. **Multi-Agent Search & Validation Pipeline**
   - Powered by **Gemini 2.5 Flash** with strict JSON schema enforcement and zero mock fallback.
   - **Paper Search Agent**: Fetches 3 realistic, high-fidelity IEEE journal/transaction papers (2025–2026) matching the target subdomain.
   - **Hot CS Subfields Alignment**: Integrates cutting-edge subfields (e.g., Agentic AI, Multi-Agent Systems, Explainable AI (XAI), Digital Twins, Edge AI, Federated Learning) to boost project innovation scores.
   - **Verification Agent**: Validates that papers are peer-reviewed Journals or Transactions (e.g., IEEE Transactions on Smart Grid, IEEE Access, etc.), strictly filtering out conference proceedings, symposiums, workshops, and reviews.

4. **Dynamic Project Expansion & Roadmapping**
   - For each recommended paper, the system generates:
     - Feasibility Score (feasibility, difficulty, innovation, publication potential).
     - Proposed System Modules with detailed description.
     - Recommended Technology Stack.
     - System Architecture outline.
     - Custom Novelty Additions (innovative features to secure higher grades).
     - Week-by-week implementation roadmap.

5. **Hyperlinked PDF Report Generator**
   - Instantly downloads a clean, publication-grade project roadmap report using **OpenPDF**.
   - Fully interactive PDF featuring **hyperlinked Paper Titles** and **clickable DOI links** directing students to direct PDF download streams or IEEE Xplore.
   - Excludes evaluative metrics like "Pilot Score" from the PDF metadata for academic submissions.

---

## ⚙️ Tech Stack

### Frontend
- **Framework**: React.js (built on Vite)
- **Styling**: Vanilla CSS (glassmorphism theme) with Tailwind CSS support
- **Icons**: Lucide React
- **API Client**: Axios

### Backend
- **Framework**: Spring Boot 3.x
- **ORM & JPA**: Hibernate, Spring Data JPA
- **Database**: MySQL 8.x
- **PDF Engine**: OpenPDF (for document compilation)

### AI Integration
- **LLM**: Gemini 2.5 Flash API (`responseMimeType: "application/json"`)

---

## 📐 Architecture & Multi-Agent Workflow

The system employs a multi-step user interaction flow mapped across a React single page application, a Spring Boot backend orchestrator, a relational MySQL database, and the Google Gemini LLM API.

```mermaid
graph TD
    User([Student / User]) -->|1. Select Domain & Project Area| Frontend[React Wizard Dashboard]
    Frontend -->|2. Get Subdomains| Backend[Spring Boot API Controller]
    Backend -->|3. Call Gemini| LLM[Gemini 2.5 Flash]
    LLM -->|4. Return Subdomains JSON| Backend
    Backend -->|5. Show Subdomains| Frontend
    User -->|6. Select Subdomain & Trigger Generate| Frontend
    Frontend -->|7. Generate Recommendations| Backend
    Backend -->|8. Retrieve Avoid List| DB[(MySQL Database)]
    Backend -->|9. Run Multi-Agent Prompts| LLM
    LLM -->|10. Validated Recommendations JSON| Backend
    Backend -->|11. Exclude Conferences & Save| DB
    Backend -->|12. Return Recommendations| Frontend
    User -->|13. Click Download PDF| Frontend
    Frontend -->|14. Export Styled PDF| PDF[OpenPDF Report Writer]
    PDF -->|15. Clickable IEEE/DOI links| User
```

---

## 🕸️ Codebase & Entity Knowledge Graph

This graph models the internal code modules, database persistence channels, repository mappings, and external library interfaces of ProjectPilot AI.

```mermaid
graph TD
    %% Style Definitions
    classDef frontend fill:#4f46e5,stroke:#312e81,color:#fff,stroke-width:2px;
    classDef backend fill:#0891b2,stroke:#083344,color:#fff,stroke-width:2px;
    classDef database fill:#15803d,stroke:#14532d,color:#fff,stroke-width:2px;
    classDef external fill:#d97706,stroke:#78350f,color:#fff,stroke-width:2px;

    %% Nodes
    subgraph Frontend ["React Client Application (Port 5174)"]
        DASH["Dashboard.jsx (Multi-Step Wizard UI)"]:::frontend
        API["api.js (Axios REST Services)"]:::frontend
    end

    subgraph Backend ["Spring Boot Application Server (Port 8080)"]
        RC["RecommendationController.java (REST Endpoints)"]:::backend
        RS["RecommendationService.java (Orchestrator)"]:::backend
        GS["GeminiService.java (LLM REST Client)"]:::backend
        PS["PdfService.java (OpenPDF Generator)"]:::backend
        UR["UserRepository.java (JPA Repository)"]:::backend
        PR["StudentPreferencesRepository.java (JPA)"]:::backend
        RR["RecommendedPaperRepository.java (JPA)"]:::backend
        AR["AvoidProjectRepository.java (JPA)"]:::backend
    end

    subgraph MySQL ["MySQL Database (projectpilot Schema)"]
        T_USERS["users Table"]:::database
        T_PREF["student_preferences Table"]:::database
        T_PAPERS["recommended_papers Table"]:::database
        T_AVOID["avoid_projects Table"]:::database
    end

    subgraph External ["External Services"]
        GEMINI["Gemini 2.5 Flash API (JSON Mode)"]:::external
        IEEE["IEEE Xplore / DOI Redirects"]:::external
    end

    %% Relations
    DASH -->|User Actions| API
    API -->|REST HTTP Requests| RC

    RC -->|Binds Endpoints| RS
    RC -->|Streams PDF Bytes| PS

    RS -->|Loads User Profile| UR
    RS -->|Loads Saved Preferences| PR
    RS -->|Queries Avoid Projects| AR
    RS -->|Saves Recommendations| RR
    RS -->|Invokes Prompt Workflow| GS

    GS -->|POST Request| GEMINI

    UR -->|Performs JPA Queries| T_USERS
    PR -->|Performs JPA Queries| T_PREF
    RR -->|Performs JPA Queries| T_PAPERS
    AR -->|Performs JPA Queries| T_AVOID

    PS -->|Parses Plan JSON| RR
    PS -->|Creates Title Hyperlink| IEEE
    RS -->|Resolves Download URLs| IEEE
```

---

## 🗄️ Database Schema

The system uses a relational MySQL database with the following table structure:

### 1. `users`
Represents the user registration and login state.
- `id` (BIGINT, Primary Key, Auto-Increment)
- `name` (VARCHAR)
- `email` (VARCHAR, Unique)
- `password` (VARCHAR)

### 2. `student_preferences`
Stores the student's profile wizard metrics.
- `id` (BIGINT, Primary Key)
- `user_id` (BIGINT, Foreign Key)
- `domain` (VARCHAR)
- `skills` (VARCHAR)
- `team_size` (INT)
- `duration` (INT)

### 3. `avoid_projects`
A profile-based list of titles or topics the agent must avoid recommending.
- `id` (BIGINT, Primary Key)
- `user_id` (BIGINT, Foreign Key)
- `project_name` (VARCHAR)

### 4. `recommended_papers`
Caches the recommended papers generated for each student.
- `id` (BIGINT, Primary Key)
- `user_id` (BIGINT, Foreign Key)
- `title` (VARCHAR)
- `authors` (VARCHAR)
- `publication_year` (INT)
- `journal` (VARCHAR)
- `doi` (VARCHAR)
- `link` (VARCHAR)
- `paper_abstract` (TEXT)
- `score` (DOUBLE)
- `implementation_plan` (JSON Text)

---

## 🚀 Setup & Installation

### 1. Prerequisites
- **Java**: JDK 21 or higher
- **Node.js**: Node 18+ and npm
- **Database**: MySQL Server
- **AI Key**: Google Gemini API Key

### 2. Database Configuration
1. Open your MySQL client and create the schema:
   ```sql
   CREATE DATABASE projectpilot;
   ```
2. Configure your MySQL credentials and Gemini API key in `backend/src/main/resources/application.properties`:
   ```properties
   spring.datasource.url=jdbc:mysql://localhost:3306/projectpilot?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
   spring.datasource.username=your_mysql_username
   spring.datasource.password=your_mysql_password
   
   gemini.api.key=your_gemini_api_key
   ```

### 3. Run the Backend (Spring Boot)
Open a terminal in the `backend` directory and execute:
```bash
# Windows
.\mvnw.cmd clean spring-boot:run

# macOS / Linux
chmod +x mvnw
./mvnw clean spring-boot:run
```
Tomcat will spin up on port **`8080`**.

### 4. Run the Frontend (React + Vite)
Open a separate terminal in the `frontend` directory and execute:
```bash
# Install package dependencies
npm install

# Start the local development server
npm run dev
```
The React frontend dev server will launch at **`http://localhost:5174/`** (or next available port).

---

## 📌 Development Milestones
- **Authentication**: JWT/Session-based student registration and login.
- **Dynamic Wizard**: Adaptive selectors fetching 35 distinct domains from backend REST APIs.
- **Gemini Pipeline**: Complete prompt design containing search, conference-exclusion rules, scoring heuristics, and JSON parser bindings.
- **PDF Exporter**: Flowable cell layout compiling title anchors, authors, abstract block, modules grid, and chronological timeline table.
