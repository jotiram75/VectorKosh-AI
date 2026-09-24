package com.vectordb.algorithm;

import com.vectordb.model.DistFn;
import com.vectordb.model.VectorItem;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

public class BruteForceIndex {
    private final List<VectorItem> items = new CopyOnWriteArrayList<>();

    public void insert(VectorItem v) {
        items.add(v);
    }

    public static class DistancePair implements Comparable<DistancePair> {
        public float dist;
        public int id;

        public DistancePair(float dist, int id) {
            this.dist = dist;
            this.id = id;
        }

        @Override
        public int compareTo(DistancePair o) {
            return Float.compare(this.dist, o.dist);
        }
    }

    public List<DistancePair> knn(List<Float> q, int k, DistFn dist) {
        List<DistancePair> r = new ArrayList<>(items.size());
        for (VectorItem v : items) {
            r.add(new DistancePair(dist.apply(q, v.getEmbedding()), v.getId()));
        }
        Collections.sort(r);
        if (r.size() > k) {
            return new ArrayList<>(r.subList(0, k));
        }
        return r;
    }

    public void remove(int id) {
        items.removeIf(v -> v.getId() == id);
    }

    public List<VectorItem> getItems() {
        return items;
    }
}
