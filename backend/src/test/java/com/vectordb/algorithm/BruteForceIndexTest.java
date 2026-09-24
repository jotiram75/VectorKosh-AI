package com.vectordb.algorithm;

import com.vectordb.model.VectorItem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class BruteForceIndexTest {

    private BruteForceIndex index;

    @BeforeEach
    public void setUp() {
        index = new BruteForceIndex();
    }

    @Test
    public void testInsertAndKnnSearch() {
        VectorItem item1 = new VectorItem(1, "item1", "cs", List.of(1.0f, 0.0f));
        VectorItem item2 = new VectorItem(2, "item2", "math", List.of(0.0f, 1.0f));
        VectorItem item3 = new VectorItem(3, "item3", "cs", List.of(0.9f, 0.1f));

        index.insert(item1);
        index.insert(item2);
        index.insert(item3);

        List<BruteForceIndex.DistancePair> results = index.knn(List.of(1.0f, 0.0f), 2, DistanceMetrics::euclidean);

        assertEquals(2, results.size());
        assertEquals(1, results.get(0).id);
        assertEquals(0.0f, results.get(0).dist, 1e-5f);
        assertEquals(3, results.get(1).id);
    }

    @Test
    public void testRemoveItem() {
        VectorItem item1 = new VectorItem(1, "item1", "cs", List.of(1.0f, 0.0f));
        VectorItem item2 = new VectorItem(2, "item2", "cs", List.of(0.5f, 0.5f));

        index.insert(item1);
        index.insert(item2);
        assertEquals(2, index.getItems().size());

        index.remove(1);
        assertEquals(1, index.getItems().size());
        assertEquals(2, index.getItems().get(0).getId());
    }
}
