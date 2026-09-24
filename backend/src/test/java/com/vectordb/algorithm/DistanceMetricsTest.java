package com.vectordb.algorithm;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class DistanceMetricsTest {

    @Test
    public void testEuclideanDistance() {
        List<Float> a = List.of(0.0f, 0.0f, 0.0f);
        List<Float> b = List.of(3.0f, 4.0f, 0.0f);
        float dist = DistanceMetrics.euclidean(a, b);
        assertEquals(5.0f, dist, 1e-5f);
    }

    @Test
    public void testCosineDistanceIdenticalVectors() {
        List<Float> a = List.of(1.0f, 2.0f, 3.0f);
        List<Float> b = List.of(1.0f, 2.0f, 3.0f);
        float dist = DistanceMetrics.cosine(a, b);
        assertEquals(0.0f, dist, 1e-5f);
    }

    @Test
    public void testCosineDistanceOrthogonalVectors() {
        List<Float> a = List.of(1.0f, 0.0f);
        List<Float> b = List.of(0.0f, 1.0f);
        float dist = DistanceMetrics.cosine(a, b);
        assertEquals(1.0f, dist, 1e-5f);
    }

    @Test
    public void testCosineDistanceZeroVectorGuard() {
        List<Float> a = List.of(0.0f, 0.0f);
        List<Float> b = List.of(1.0f, 1.0f);
        float dist = DistanceMetrics.cosine(a, b);
        assertEquals(1.0f, dist, 1e-5f);
    }

    @Test
    public void testManhattanDistance() {
        List<Float> a = List.of(1.0f, 2.0f, 3.0f);
        List<Float> b = List.of(4.0f, 0.0f, 5.0f);
        float dist = DistanceMetrics.manhattan(a, b);
        assertEquals(3.0f + 2.0f + 2.0f, dist, 1e-5f);
    }

    @Test
    public void testNullAndEmptyGuards() {
        assertEquals(Float.MAX_VALUE, DistanceMetrics.euclidean(null, List.of(1.0f)));
        assertEquals(1.0f, DistanceMetrics.cosine(List.of(), List.of(1.0f)));
        assertEquals(Float.MAX_VALUE, DistanceMetrics.manhattan(List.of(1.0f), null));
    }
}
