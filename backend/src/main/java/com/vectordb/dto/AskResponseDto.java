package com.vectordb.dto;

import java.util.List;

public class AskResponseDto {
    private String answer;
    private String model;
    private List<DocContextDto> contexts;
    private int docCount;

    public AskResponseDto() {}

    public AskResponseDto(String answer, String model, List<DocContextDto> contexts, int docCount) {
        this.answer = answer;
        this.model = model;
        this.contexts = contexts;
        this.docCount = docCount;
    }

    public String getAnswer() { return answer; }
    public void setAnswer(String answer) { this.answer = answer; }

    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }

    public List<DocContextDto> getContexts() { return contexts; }
    public void setContexts(List<DocContextDto> contexts) { this.contexts = contexts; }

    public int getDocCount() { return docCount; }
    public void setDocCount(int docCount) { this.docCount = docCount; }

    public static class DocContextDto {
        private int id;
        private String title;
        private String text;
        private float distance;

        public DocContextDto() {}

        public DocContextDto(int id, String title, String text, float distance) {
            this.id = id;
            this.title = title;
            this.text = text;
            this.distance = distance;
        }

        public int getId() { return id; }
        public void setId(int id) { this.id = id; }

        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }

        public String getText() { return text; }
        public void setText(String text) { this.text = text; }

        public float getDistance() { return distance; }
        public void setDistance(float distance) { this.distance = distance; }
    }
}
