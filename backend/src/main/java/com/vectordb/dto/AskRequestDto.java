package com.vectordb.dto;

public class AskRequestDto {
    private String question;
    private Integer k = 3;

    public AskRequestDto() {}

    public String getQuestion() { return question; }
    public void setQuestion(String question) { this.question = question; }

    public Integer getK() { return k != null ? k : 3; }
    public void setK(Integer k) { this.k = k; }
}
