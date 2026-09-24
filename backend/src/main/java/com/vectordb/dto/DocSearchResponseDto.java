package com.vectordb.dto;

import java.util.List;

public class DocSearchResponseDto {
    private List<SearchHitSummaryDto> contexts;

    public DocSearchResponseDto() {}

    public DocSearchResponseDto(List<SearchHitSummaryDto> contexts) {
        this.contexts = contexts;
    }

    public List<SearchHitSummaryDto> getContexts() { return contexts; }
    public void setContexts(List<SearchHitSummaryDto> contexts) { this.contexts = contexts; }

    public static class SearchHitSummaryDto {
        private int id;
        private String title;
        private float distance;

        public SearchHitSummaryDto() {}

        public SearchHitSummaryDto(int id, String title, float distance) {
            this.id = id;
            this.title = title;
            this.distance = distance;
        }

        public int getId() { return id; }
        public void setId(int id) { this.id = id; }

        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }

        public float getDistance() { return distance; }
        public void setDistance(float distance) { this.distance = distance; }
    }
}
