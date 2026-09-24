package com.vectordb.dto;

import java.util.List;

public class InsertVectorRequestDto {
    private String metadata;
    private String category;
    private List<Float> embedding;

    public InsertVectorRequestDto() {}

    public String getMetadata() { return metadata; }
    public void setMetadata(String metadata) { this.metadata = metadata; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public List<Float> getEmbedding() { return embedding; }
    public void setEmbedding(List<Float> embedding) { this.embedding = embedding; }
}
