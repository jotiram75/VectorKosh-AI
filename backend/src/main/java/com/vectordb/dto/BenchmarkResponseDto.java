package com.vectordb.dto;

public class BenchmarkResponseDto {
    private long bruteforceUs;
    private long kdtreeUs;
    private long hnswUs;
    private int itemCount;

    public BenchmarkResponseDto() {}

    public BenchmarkResponseDto(long bruteforceUs, long kdtreeUs, long hnswUs, int itemCount) {
        this.bruteforceUs = bruteforceUs;
        this.kdtreeUs = kdtreeUs;
        this.hnswUs = hnswUs;
        this.itemCount = itemCount;
    }

    public long getBruteforceUs() { return bruteforceUs; }
    public void setBruteforceUs(long bruteforceUs) { this.bruteforceUs = bruteforceUs; }

    public long getKdtreeUs() { return kdtreeUs; }
    public void setKdtreeUs(long kdtreeUs) { this.kdtreeUs = kdtreeUs; }

    public long getHnswUs() { return hnswUs; }
    public void setHnswUs(long hnswUs) { this.hnswUs = hnswUs; }

    public int getItemCount() { return itemCount; }
    public void setItemCount(int itemCount) { this.itemCount = itemCount; }
}
