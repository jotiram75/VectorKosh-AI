package com.vectordb.dto;

import java.util.List;

public class SearchHitDto {
    private int id;
    private String metadata;
    private String category;
    private float distance;
    private List<Float> embedding;

    public SearchHitDto() {}

    public SearchHitDto(int id, String metadata, String category, float distance, List<Float> embedding) {
        this.id = id;
        this.metadata = metadata;
        this.category = category;
        this.distance = distance;
        this.embedding = embedding;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getMetadata() { return metadata; }
    public void setMetadata(String metadata) { this.metadata = metadata; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public float getDistance() { return distance; }
    public void setDistance(float distance) { this.distance = distance; }

    public List<Float> getEmbedding() { return embedding; }
    public void setEmbedding(List<Float> embedding) { this.embedding = embedding; }
}
