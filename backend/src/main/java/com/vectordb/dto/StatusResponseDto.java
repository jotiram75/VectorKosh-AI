package com.vectordb.dto;

public class StatusResponseDto {
    private boolean ollamaAvailable;
    private String embedModel;
    private String genModel;
    private int docCount;
    private int docDims;
    private int demoDims;
    private int demoCount;

    public StatusResponseDto() {}

    public StatusResponseDto(boolean ollamaAvailable, String embedModel, String genModel, int docCount, int docDims, int demoDims, int demoCount) {
        this.ollamaAvailable = ollamaAvailable;
        this.embedModel = embedModel;
        this.genModel = genModel;
        this.docCount = docCount;
        this.docDims = docDims;
        this.demoDims = demoDims;
        this.demoCount = demoCount;
    }

    public boolean isOllamaAvailable() { return ollamaAvailable; }
    public void setOllamaAvailable(boolean ollamaAvailable) { this.ollamaAvailable = ollamaAvailable; }

    public String getEmbedModel() { return embedModel; }
    public void setEmbedModel(String embedModel) { this.embedModel = embedModel; }

    public String getGenModel() { return genModel; }
    public void setGenModel(String genModel) { this.genModel = genModel; }

    public int getDocCount() { return docCount; }
    public void setDocCount(int docCount) { this.docCount = docCount; }

    public int getDocDims() { return docDims; }
    public void setDocDims(int docDims) { this.docDims = docDims; }

    public int getDemoDims() { return demoDims; }
    public void setDemoDims(int demoDims) { this.demoDims = demoDims; }

    public int getDemoCount() { return demoCount; }
    public void setDemoCount(int demoCount) { this.demoCount = demoCount; }
}
