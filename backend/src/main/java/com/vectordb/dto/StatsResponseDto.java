package com.vectordb.dto;

import java.util.List;

public class StatsResponseDto {
    private int count;
    private int dims;
    private List<String> algorithms;
    private List<String> metrics;

    public StatsResponseDto() {}

    public StatsResponseDto(int count, int dims, List<String> algorithms, List<String> metrics) {
        this.count = count;
        this.dims = dims;
        this.algorithms = algorithms;
        this.metrics = metrics;
    }

    public int getCount() { return count; }
    public void setCount(int count) { this.count = count; }

    public int getDims() { return dims; }
    public void setDims(int dims) { this.dims = dims; }

    public List<String> getAlgorithms() { return algorithms; }
    public void setAlgorithms(List<String> algorithms) { this.algorithms = algorithms; }

    public List<String> getMetrics() { return metrics; }
    public void setMetrics(List<String> metrics) { this.metrics = metrics; }
}
