package com.vectordb.service;

import com.vectordb.dto.BenchmarkResponseDto;
import com.vectordb.dto.SearchResponseDto;
import com.vectordb.algorithm.DistanceMetrics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class VectorDBServiceTest {

    private VectorDBService vectorService;

    @BeforeEach
    public void setUp() {
        vectorService = new VectorDBService();
        vectorService.initDemoData();
    }

    @Test
    public void testInitDemoDataLoaded() {
        assertTrue(vectorService.size() > 0);
    }

    @Test
    public void testSearchAlgorithms() {
        List<Float> q = Collections.nCopies(16, 0.5f);

        SearchResponseDto hnswRes = vectorService.search(q, 3, "cosine", "hnsw");
        assertNotNull(hnswRes);
        assertEquals(3, hnswRes.getResults().size());

        SearchResponseDto kdRes = vectorService.search(q, 3, "euclidean", "kdtree");
        assertNotNull(kdRes);
        assertEquals(3, kdRes.getResults().size());

        SearchResponseDto bfRes = vectorService.search(q, 3, "manhattan", "bruteforce");
        assertNotNull(bfRes);
        assertEquals(3, bfRes.getResults().size());
    }

    @Test
    public void testBenchmarkExecution() {
        List<Float> q = Collections.nCopies(16, 0.2f);
        BenchmarkResponseDto bench = vectorService.benchmark(q, 5, "cosine");

        assertNotNull(bench);
        assertTrue(bench.getItemCount() > 0);
    }

    @Test
    public void testInsertAndRemoveVector() {
        int initialSize = vectorService.size();
        List<Float> emb = Collections.nCopies(16, 0.1f);

        int newId = vectorService.insert("Custom Node", "cs", emb, DistanceMetrics.getDistFn("cosine"));
        assertEquals(initialSize + 1, vectorService.size());

        boolean deleted = vectorService.remove(newId);
        assertTrue(deleted);
        assertEquals(initialSize, vectorService.size());
    }
}
