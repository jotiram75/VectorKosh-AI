package com.vectordb.algorithm;

import com.vectordb.model.VectorItem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class KDTreeIndexTest {

    private KDTreeIndex kdtree;

    @BeforeEach
    public void setUp() {
        kdtree = new KDTreeIndex(2);
    }

    @Test
    public void testInsertAndKnnEuclidean() {
        VectorItem item1 = new VectorItem(1, "item1", "cs", List.of(2.0f, 3.0f));
        VectorItem item2 = new VectorItem(2, "item2", "math", List.of(5.0f, 4.0f));
        VectorItem item3 = new VectorItem(3, "item3", "cs", List.of(9.0f, 6.0f));
        VectorItem item4 = new VectorItem(4, "item4", "cs", List.of(4.0f, 7.0f));

        kdtree.insert(item1);
        kdtree.insert(item2);
        kdtree.insert(item3);
        kdtree.insert(item4);

        List<BruteForceIndex.DistancePair> results = kdtree.knn(List.of(2.1f, 3.1f), 2, DistanceMetrics::euclidean);

        assertEquals(2, results.size());
        assertEquals(1, results.get(0).id);
    }

    @Test
    public void testRebuildIndex() {
        VectorItem item1 = new VectorItem(1, "item1", "cs", List.of(1.0f, 1.0f));
        VectorItem item2 = new VectorItem(2, "item2", "cs", List.of(10.0f, 10.0f));

        kdtree.insert(item1);
        kdtree.rebuild(List.of(item2));

        List<BruteForceIndex.DistancePair> results = kdtree.knn(List.of(9.9f, 9.9f), 1, DistanceMetrics::euclidean);

        assertEquals(1, results.size());
        assertEquals(2, results.get(0).id);
    }
}
