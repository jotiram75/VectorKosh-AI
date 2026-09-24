package com.vectordb.dto;

import java.util.List;

public class SearchResponseDto {
    private List<SearchHitDto> results;
    private long latencyUs;
    private String algo;
    private String metric;

    public SearchResponseDto() {}

    public SearchResponseDto(List<SearchHitDto> results, long latencyUs, String algo, String metric) {
        this.results = results;
        this.latencyUs = latencyUs;
        this.algo = algo;
        this.metric = metric;
    }

    public List<SearchHitDto> getResults() { return results; }
    public void setResults(List<SearchHitDto> results) { this.results = results; }

    public long getLatencyUs() { return latencyUs; }
    public void setLatencyUs(long latencyUs) { this.latencyUs = latencyUs; }

    public String getAlgo() { return algo; }
    public void setAlgo(String algo) { this.algo = algo; }

    public String getMetric() { return metric; }
    public void setMetric(String metric) { this.metric = metric; }
}
