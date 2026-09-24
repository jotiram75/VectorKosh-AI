package com.vectordb.service;

import com.vectordb.algorithm.BruteForceIndex;
import com.vectordb.algorithm.DistanceMetrics;
import com.vectordb.algorithm.HNSWIndex;
import com.vectordb.model.DocItem;
import com.vectordb.model.VectorItem;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class DocumentDBService {

    private final Map<Integer, DocItem> store = new ConcurrentHashMap<>();
    private final HNSWIndex hnsw = new HNSWIndex(16, 200);
    private final BruteForceIndex bf = new BruteForceIndex();
    private final AtomicInteger nextId = new AtomicInteger(1);
    private int dims = 0;

    public synchronized int insert(String title, String text, List<Float> emb) {
        if (dims == 0 && emb != null) {
            dims = emb.size();
        }
        int id = nextId.getAndIncrement();
        DocItem item = new DocItem(id, title, text, emb);
        store.put(id, item);

        VectorItem vi = new VectorItem(id, title, "doc", emb);
        hnsw.insert(vi, DistanceMetrics::cosine);
        bf.insert(vi);
        return id;
    }

    public static class DocHit {
        public float dist;
        public DocItem item;

        public DocHit(float dist, DocItem item) {
            this.dist = dist;
            this.item = item;
        }
    }

    public synchronized List<DocHit> search(List<Float> q, int k, float maxDist) {
        if (store.isEmpty() || q == null || q.isEmpty()) return new ArrayList<>();

        List<BruteForceIndex.DistancePair> raw = (store.size() < 10)
                ? bf.knn(q, k, DistanceMetrics::cosine)
                : hnsw.knn(q, k, 50, DistanceMetrics::cosine);

        List<DocHit> out = new ArrayList<>();
        for (BruteForceIndex.DistancePair pair : raw) {
            DocItem doc = store.get(pair.id);
            if (doc != null && pair.dist <= maxDist) {
                out.add(new DocHit(pair.dist, doc));
            }
        }
        return out;
    }

    public synchronized List<DocHit> search(List<Float> q, int k) {
        return search(q, k, Float.MAX_VALUE);
    }

    public synchronized boolean remove(int id) {
        if (!store.containsKey(id)) return false;
        store.remove(id);
        hnsw.remove(id);
        bf.remove(id);
        if (store.isEmpty()) {
            dims = 0;
        }
        return true;
    }

    public synchronized List<DocItem> getAll() {
        return new ArrayList<>(store.values());
    }

    public synchronized int size() {
        return store.size();
    }

    public synchronized int getDims() {
        return dims;
    }
}
