package com.vectordb.algorithm;

import com.vectordb.model.DistFn;
import com.vectordb.model.VectorItem;

import java.util.*;

public class KDTreeIndex {

    private static class KDNode {
        VectorItem item;
        KDNode left;
        KDNode right;

        KDNode(VectorItem v) {
            this.item = v;
        }
    }

    private KDNode root = null;
    private final int dims;

    public KDTreeIndex(int dims) {
        this.dims = dims;
    }

    private KDNode ins(KDNode n, VectorItem v, int d) {
        if (n == null) return new KDNode(v);
        if (dims <= 0 || v.getEmbedding() == null || v.getEmbedding().isEmpty()) return n;
        int ax = d % dims;
        float vVal = ax < v.getEmbedding().size() ? v.getEmbedding().get(ax) : 0.0f;
        float nVal = ax < n.item.getEmbedding().size() ? n.item.getEmbedding().get(ax) : 0.0f;

        if (vVal < nVal) {
            n.left = ins(n.left, v, d + 1);
        } else {
            n.right = ins(n.right, v, d + 1);
        }
        return n;
    }

    public synchronized void insert(VectorItem v) {
        if (v == null || v.getEmbedding() == null) return;
        root = ins(root, v, 0);
    }

    private void knnSearch(KDNode n, List<Float> q, int k, int d, DistFn dist,
                           PriorityQueue<BruteForceIndex.DistancePair> maxHeap) {
        if (n == null || q == null || q.isEmpty() || dims <= 0) return;
        float dn = dist.apply(q, n.item.getEmbedding());

        if (maxHeap.size() < k || dn < maxHeap.peek().dist) {
            maxHeap.add(new BruteForceIndex.DistancePair(dn, n.item.getId()));
            if (maxHeap.size() > k) {
                maxHeap.poll();
            }
        }

        int ax = d % dims;
        float qVal = ax < q.size() ? q.get(ax) : 0.0f;
        float nVal = ax < n.item.getEmbedding().size() ? n.item.getEmbedding().get(ax) : 0.0f;
        float diff = qVal - nVal;

        KDNode closer = diff < 0 ? n.left : n.right;
        KDNode farther = diff < 0 ? n.right : n.left;

        knnSearch(closer, q, k, d + 1, dist, maxHeap);

        boolean shouldSearchFarther = (maxHeap.size() < k);
        if (!shouldSearchFarther && maxHeap.peek() != null) {
            float maxDist = maxHeap.peek().dist;
            shouldSearchFarther = (Math.abs(diff) <= maxDist);
        }

        if (shouldSearchFarther) {
            knnSearch(farther, q, k, d + 1, dist, maxHeap);
        }
    }

    public synchronized List<BruteForceIndex.DistancePair> knn(List<Float> q, int k, DistFn dist) {
        if (q == null || q.isEmpty() || k <= 0) return new ArrayList<>();

        PriorityQueue<BruteForceIndex.DistancePair> maxHeap =
                new PriorityQueue<>(Comparator.comparing(dp -> -dp.dist));

        knnSearch(root, q, k, 0, dist, maxHeap);

        List<BruteForceIndex.DistancePair> r = new ArrayList<>();
        while (!maxHeap.isEmpty()) {
            r.add(maxHeap.poll());
        }
        Collections.sort(r);
        return r;
    }

    public synchronized void rebuild(List<VectorItem> items) {
        root = null;
        if (items == null) return;
        for (VectorItem v : items) {
            insert(v);
        }
    }
}

