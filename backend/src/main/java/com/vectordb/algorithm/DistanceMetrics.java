package com.vectordb.algorithm;

import com.vectordb.model.DistFn;
import java.util.List;

public class DistanceMetrics {

    public static float euclidean(List<Float> a, List<Float> b) {
        if (a == null || b == null || a.isEmpty() || b.isEmpty()) return Float.MAX_VALUE;
        float s = 0.0f;
        int size = Math.min(a.size(), b.size());
        for (int i = 0; i < size; i++) {
            float d = a.get(i) - b.get(i);
            s += d * d;
        }
        return (float) Math.sqrt(s);
    }

    public static float cosine(List<Float> a, List<Float> b) {
        if (a == null || b == null || a.isEmpty() || b.isEmpty()) return 1.0f;
        float dot = 0.0f, na = 0.0f, nb = 0.0f;
        int size = Math.min(a.size(), b.size());
        for (int i = 0; i < size; i++) {
            float va = a.get(i);
            float vb = b.get(i);
            dot += va * vb;
            na += va * va;
            nb += vb * vb;
        }
        if (na < 1e-9f || nb < 1e-9f) return 1.0f;
        float denom = (float) (Math.sqrt(na) * Math.sqrt(nb));
        if (denom < 1e-9f) return 1.0f;
        float cosSim = dot / denom;
        cosSim = Math.max(-1.0f, Math.min(1.0f, cosSim));
        return 1.0f - cosSim;
    }

    public static float manhattan(List<Float> a, List<Float> b) {
        if (a == null || b == null || a.isEmpty() || b.isEmpty()) return Float.MAX_VALUE;
        float s = 0.0f;
        int size = Math.min(a.size(), b.size());
        for (int i = 0; i < size; i++) {
            s += Math.abs(a.get(i) - b.get(i));
        }
        return s;
    }

    public static DistFn getDistFn(String metric) {
        if ("cosine".equalsIgnoreCase(metric)) {
            return DistanceMetrics::cosine;
        } else if ("manhattan".equalsIgnoreCase(metric)) {
            return DistanceMetrics::manhattan;
        }
        return DistanceMetrics::euclidean;
    }
}

