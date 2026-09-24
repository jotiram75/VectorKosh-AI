package com.vectordb.dto;

public class DocItemSummaryDto {
    private int id;
    private String title;
    private String preview;
    private int words;

    public DocItemSummaryDto() {}

    public DocItemSummaryDto(int id, String title, String preview, int words) {
        this.id = id;
        this.title = title;
        this.preview = preview;
        this.words = words;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getPreview() { return preview; }
    public void setPreview(String preview) { this.preview = preview; }

    public int getWords() { return words; }
    public void setWords(int words) { this.words = words; }
}
