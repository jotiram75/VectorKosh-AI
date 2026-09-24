package com.vectordb.dto;

public class InsertDocRequestDto {
    private String title;
    private String text;

    public InsertDocRequestDto() {}

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }
}
