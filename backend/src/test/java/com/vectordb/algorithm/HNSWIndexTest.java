package com.vectordb.algorithm;

import com.vectordb.model.VectorItem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class HNSWIndexTest {

    private HNSWIndex hnsw;

    @BeforeEach
    public void setUp() {
        hnsw = new HNSWIndex(16, 200);
    }

    @Test
    public void testNoSelfLoopsOnInsert() {
        VectorItem item1 = new VectorItem(1, "item1", "cs", List.of(0.8f, 0.2f, 0.1f, 0.0f));
        VectorItem item2 = new VectorItem(2, "item2", "math", List.of(0.1f, 0.9f, 0.2f, 0.0f));
        VectorItem item3 = new VectorItem(3, "item3", "cs", List.of(0.75f, 0.25f, 0.05f, 0.0f));

        hnsw.insert(item1, DistanceMetrics::cosine);
        hnsw.insert(item2, DistanceMetrics::cosine);
        hnsw.insert(item3, DistanceMetrics::cosine);

        HNSWIndex.GraphInfo info = hnsw.getInfo();
        for (HNSWIndex.GraphInfo.EdgeView edge : info.edges) {
            assertNotEquals(edge.src, edge.dst, "HNSW graph must not contain self-loop edges!");
        }
    }

    @Test
    public void testKnnSearchRecall() {
        VectorItem item1 = new VectorItem(1, "point1", "cs", List.of(1.0f, 0.0f, 0.0f));
        VectorItem item2 = new VectorItem(2, "point2", "cs", List.of(0.9f, 0.1f, 0.0f));
        VectorItem item3 = new VectorItem(3, "point3", "math", List.of(0.0f, 1.0f, 0.0f));

        hnsw.insert(item1, DistanceMetrics::cosine);
        hnsw.insert(item2, DistanceMetrics::cosine);
        hnsw.insert(item3, DistanceMetrics::cosine);

        List<BruteForceIndex.DistancePair> hits = hnsw.knn(List.of(0.95f, 0.05f, 0.0f), 2, 50, DistanceMetrics::cosine);

        assertFalse(hits.isEmpty());
        assertTrue(hits.stream().anyMatch(h -> h.id == 1));
        assertTrue(hits.stream().anyMatch(h -> h.id == 2));
    }

    @Test
    public void testRemoveNodeAndEntryPtRecomputation() {
        VectorItem item1 = new VectorItem(1, "item1", "cs", List.of(1.0f, 0.0f));
        VectorItem item2 = new VectorItem(2, "item2", "cs", List.of(0.9f, 0.1f));

        hnsw.insert(item1, DistanceMetrics::cosine);
        hnsw.insert(item2, DistanceMetrics::cosine);

        assertEquals(2, hnsw.size());

        hnsw.remove(1);
        assertEquals(1, hnsw.size());

        hnsw.remove(2);
        assertEquals(0, hnsw.size());
    }
}
