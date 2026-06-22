package com.projectpilot.backend.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.projectpilot.backend.entity.RecommendedPaper;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
public class PdfService {

    private final ObjectMapper objectMapper;

    public PdfService() {
        this.objectMapper = new ObjectMapper();
    }

    public byte[] generatePdfReport(RecommendedPaper paper) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4, 36, 36, 54, 36);

        try {
            PdfWriter.getInstance(document, out);
            document.open();

            // Fonts
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 22, Font.NORMAL, new Color(26, 54, 93)); // Navy Blue
            Font h2Font = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, Font.NORMAL, new Color(44, 82, 130));   // Dark Blue
            Font bodyFont = FontFactory.getFont(FontFactory.HELVETICA, 10, Font.NORMAL, Color.DARK_GRAY);
            Font bodyBoldFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Font.NORMAL, Color.DARK_GRAY);
            Font italicFont = FontFactory.getFont(FontFactory.HELVETICA, 10, Font.ITALIC, Color.GRAY);


            // Document Header
            Paragraph mainTitle = new Paragraph("ProjectPilot AI - Project Roadmap Report", titleFont);
            mainTitle.setAlignment(Element.ALIGN_CENTER);
            mainTitle.setSpacingAfter(20);
            document.add(mainTitle);

            // Paper Metadata Card (Table)
            PdfPTable metaTable = new PdfPTable(2);
            metaTable.setWidthPercentage(100);
            metaTable.setWidths(new float[]{1.5f, 5.5f});
            metaTable.setSpacingAfter(15);

            addMetaRow(metaTable, "Paper Title:", paper.getTitle(), bodyBoldFont, bodyFont);
            addMetaRow(metaTable, "Authors:", paper.getAuthors(), bodyBoldFont, bodyFont);
            addMetaRow(metaTable, "Journal:", paper.getJournal() + " (" + paper.getYear() + ")", bodyBoldFont, bodyFont);
            addMetaRow(metaTable, "DOI / Link:", paper.getDoi(), bodyBoldFont, bodyFont);
            addMetaRow(metaTable, "Pilot Score:", String.format("%.1f / 100", paper.getScore()), bodyBoldFont, bodyFont);

            document.add(metaTable);

            // Abstract Section
            document.add(new Paragraph("Research Abstract", h2Font));
            Paragraph absPara = new Paragraph(paper.getAbstractText(), italicFont);
            absPara.setSpacingBefore(5);
            absPara.setSpacingAfter(15);
            document.add(absPara);

            // Parse Implementation Plan
            Map<String, Object> plan = Collections.emptyMap();
            if (paper.getImplementationPlan() != null && !paper.getImplementationPlan().trim().isEmpty()) {
                try {
                    plan = objectMapper.readValue(paper.getImplementationPlan(), new TypeReference<Map<String, Object>>() {});
                } catch (Exception parseEx) {
                    System.err.println("Error parsing implementation plan JSON in PDF: " + parseEx.getMessage());
                }
            }

            // Tech Stack & Novelty Additions
            document.add(new Paragraph("Project Architecture & Innovation", h2Font));
            document.add(new Paragraph(""));
            
            String techStack = (String) plan.getOrDefault("techStack", "Spring Boot, React, MySQL");
            String architecture = (String) plan.getOrDefault("architecture", "Client-Server System");
            String novelty = (String) plan.getOrDefault("noveltyAdditions", "AI recommendation verification dashboard");

            Paragraph techPara = new Paragraph();
            techPara.add(new Chunk("Technology Stack: ", bodyBoldFont));
            techPara.add(new Chunk(techStack + "\n", bodyFont));
            techPara.add(new Chunk("System Architecture: ", bodyBoldFont));
            techPara.add(new Chunk(architecture + "\n", bodyFont));
            techPara.add(new Chunk("Proposed Novelty Additions: ", bodyBoldFont));
            techPara.add(new Chunk(novelty + "\n", bodyFont));
            techPara.setSpacingBefore(5);
            techPara.setSpacingAfter(15);
            document.add(techPara);

            // Modules
            List<Map<String, String>> modules = (List<Map<String, String>>) plan.get("modules");
            if (modules != null && !modules.isEmpty()) {
                document.add(new Paragraph("Proposed System Modules", h2Font));
                PdfPTable moduleTable = new PdfPTable(2);
                moduleTable.setWidthPercentage(100);
                moduleTable.setWidths(new float[]{2.5f, 4.5f});
                moduleTable.setSpacingBefore(5);
                moduleTable.setSpacingAfter(15);

                // Table Header
                PdfPCell h1 = new PdfPCell(new Phrase("Module Name", bodyBoldFont));
                h1.setBackgroundColor(new Color(237, 242, 247));
                h1.setPadding(6);
                PdfPCell h2 = new PdfPCell(new Phrase("Description & Core Features", bodyBoldFont));
                h2.setBackgroundColor(new Color(237, 242, 247));
                h2.setPadding(6);
                moduleTable.addCell(h1);
                moduleTable.addCell(h2);

                for (Map<String, String> mod : modules) {
                    PdfPCell nameCell = new PdfPCell(new Phrase(mod.get("name"), bodyBoldFont));
                    nameCell.setPadding(6);
                    PdfPCell descCell = new PdfPCell(new Phrase(mod.get("description"), bodyFont));
                    descCell.setPadding(6);
                    moduleTable.addCell(nameCell);
                    moduleTable.addCell(descCell);
                }
                document.add(moduleTable);
            }

            // Week-by-Week Roadmap
            List<Map<String, String>> roadmap = (List<Map<String, String>>) plan.get("roadmap");
            if (roadmap != null && !roadmap.isEmpty()) {
                document.add(new Paragraph("Week-by-Week Implementation Roadmap", h2Font));
                PdfPTable roadmapTable = new PdfPTable(2);
                roadmapTable.setWidthPercentage(100);
                roadmapTable.setWidths(new float[]{2.0f, 5.0f});
                roadmapTable.setSpacingBefore(5);
                roadmapTable.setSpacingAfter(15);

                // Table Header
                PdfPCell h1 = new PdfPCell(new Phrase("Timeline", bodyBoldFont));
                h1.setBackgroundColor(new Color(237, 242, 247));
                h1.setPadding(6);
                PdfPCell h2 = new PdfPCell(new Phrase("Target Development Tasks", bodyBoldFont));
                h2.setBackgroundColor(new Color(237, 242, 247));
                h2.setPadding(6);
                roadmapTable.addCell(h1);
                roadmapTable.addCell(h2);

                for (Map<String, String> step : roadmap) {
                    PdfPCell weekCell = new PdfPCell(new Phrase(step.get("week"), bodyBoldFont));
                    weekCell.setPadding(6);
                    PdfPCell taskCell = new PdfPCell(new Phrase(step.get("tasks"), bodyFont));
                    taskCell.setPadding(6);
                    roadmapTable.addCell(weekCell);
                    roadmapTable.addCell(taskCell);
                }
                document.add(roadmapTable);
            }

            document.close();
        } catch (Exception e) {
            System.err.println("Error generating PDF: " + e.getMessage());
            e.printStackTrace();
        }

        return out.toByteArray();
    }

    private void addMetaRow(PdfPTable table, String field, String value, Font labelFont, Font valFont) {
        PdfPCell labelCell = new PdfPCell(new Phrase(field, labelFont));
        labelCell.setBorder(Rectangle.NO_BORDER);
        labelCell.setPadding(4);

        PdfPCell valueCell = new PdfPCell(new Phrase(value != null ? value : "N/A", valFont));
        valueCell.setBorder(Rectangle.NO_BORDER);
        valueCell.setPadding(4);

        table.addCell(labelCell);
        table.addCell(valueCell);
    }
}
