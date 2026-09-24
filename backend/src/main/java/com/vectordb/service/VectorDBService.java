package com.vectordb.service;

import com.vectordb.algorithm.*;
import com.vectordb.dto.BenchmarkResponseDto;
import com.vectordb.dto.SearchHitDto;
import com.vectordb.dto.SearchResponseDto;
import com.vectordb.model.DistFn;
import com.vectordb.model.VectorItem;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class VectorDBService {

    public static final int DIMS = 16;

    private final Map<Integer, VectorItem> store = new ConcurrentHashMap<>();
    private final BruteForceIndex bf = new BruteForceIndex();
    private final KDTreeIndex kdt = new KDTreeIndex(DIMS);
    private final HNSWIndex hnsw = new HNSWIndex(16, 200);
    private final AtomicInteger nextId = new AtomicInteger(1);

    @PostConstruct
    public void initDemoData() {
        loadDemoData();
    }

    public synchronized int insert(String metadata, String category, List<Float> emb, DistFn dist) {
        int id = nextId.getAndIncrement();
        VectorItem item = new VectorItem(id, metadata, category, emb);
        store.put(id, item);
        bf.insert(item);
        kdt.insert(item);
        hnsw.insert(item, dist);
        return id;
    }

    public synchronized boolean remove(int id) {
        if (!store.containsKey(id)) return false;
        store.remove(id);
        bf.remove(id);
        hnsw.remove(id);
        kdt.rebuild(new ArrayList<>(store.values()));
        return true;
    }

    public synchronized SearchResponseDto search(List<Float> q, int k, String metric, String algo) {
        DistFn dfn = DistanceMetrics.getDistFn(metric);
        long t0 = System.nanoTime();

        List<BruteForceIndex.DistancePair> raw;
        if ("bruteforce".equalsIgnoreCase(algo)) {
            raw = bf.knn(q, k, dfn);
        } else if ("kdtree".equalsIgnoreCase(algo)) {
            raw = kdt.knn(q, k, dfn);
        } else {
            raw = hnsw.knn(q, k, 50, dfn);
        }

        long latencyUs = (System.nanoTime() - t0) / 1000;

        List<SearchHitDto> hits = new ArrayList<>();
        for (BruteForceIndex.DistancePair pair : raw) {
            VectorItem v = store.get(pair.id);
            if (v != null) {
                hits.add(new SearchHitDto(v.getId(), v.getMetadata(), v.getCategory(), pair.dist, v.getEmbedding()));
            }
        }

        return new SearchResponseDto(hits, latencyUs, algo, metric);
    }

    public synchronized BenchmarkResponseDto benchmark(List<Float> q, int k, String metric) {
        DistFn dfn = DistanceMetrics.getDistFn(metric);

        long t0 = System.nanoTime();
        bf.knn(q, k, dfn);
        long bfUs = (System.nanoTime() - t0) / 1000;

        t0 = System.nanoTime();
        kdt.knn(q, k, dfn);
        long kdUs = (System.nanoTime() - t0) / 1000;

        t0 = System.nanoTime();
        hnsw.knn(q, k, 50, dfn);
        long hnswUs = (System.nanoTime() - t0) / 1000;

        return new BenchmarkResponseDto(bfUs, kdUs, hnswUs, store.size());
    }

    public synchronized List<VectorItem> getAll() {
        return new ArrayList<>(store.values());
    }

    public synchronized HNSWIndex.GraphInfo getHnswInfo() {
        return hnsw.getInfo();
    }

    public synchronized int size() {
        return store.size();
    }

    private void loadDemoData() {
        DistFn dist = DistanceMetrics.getDistFn("cosine");

        // Dims 0-3: CS | Dims 4-7: Math | Dims 8-11: Food | Dims 12-15: Sports
        insert("Linked List: nodes connected by pointers", "cs",
                List.of(0.90f,0.85f,0.72f,0.68f,0.12f,0.08f,0.15f,0.10f,0.05f,0.08f,0.06f,0.09f,0.07f,0.11f,0.08f,0.06f), dist);
        insert("Binary Search Tree: O(log n) search and insert", "cs",
                List.of(0.88f,0.82f,0.78f,0.74f,0.15f,0.10f,0.08f,0.12f,0.06f,0.07f,0.08f,0.05f,0.09f,0.06f,0.07f,0.10f), dist);
        insert("Dynamic Programming: memoization overlapping subproblems", "cs",
                List.of(0.82f,0.76f,0.88f,0.80f,0.20f,0.18f,0.12f,0.09f,0.07f,0.06f,0.08f,0.07f,0.08f,0.09f,0.06f,0.07f), dist);
        insert("Graph BFS and DFS: breadth and depth first traversal", "cs",
                List.of(0.85f,0.80f,0.75f,0.82f,0.18f,0.14f,0.10f,0.08f,0.06f,0.09f,0.07f,0.06f,0.10f,0.08f,0.09f,0.07f), dist);
        insert("Hash Table: O(1) lookup with collision chaining", "cs",
                List.of(0.87f,0.78f,0.70f,0.76f,0.13f,0.11f,0.09f,0.14f,0.08f,0.07f,0.06f,0.08f,0.07f,0.10f,0.08f,0.09f), dist);

        insert("Calculus: derivatives integrals and limits", "math",
                List.of(0.12f,0.15f,0.18f,0.10f,0.91f,0.86f,0.78f,0.72f,0.08f,0.06f,0.07f,0.09f,0.07f,0.08f,0.06f,0.10f), dist);
        insert("Linear Algebra: matrices eigenvalues eigenvectors", "math",
                List.of(0.20f,0.18f,0.15f,0.12f,0.88f,0.90f,0.82f,0.76f,0.09f,0.07f,0.08f,0.06f,0.10f,0.07f,0.08f,0.09f), dist);
        insert("Probability: distributions random variables Bayes theorem", "math",
                List.of(0.15f,0.12f,0.20f,0.18f,0.84f,0.80f,0.88f,0.82f,0.07f,0.08f,0.06f,0.10f,0.09f,0.06f,0.09f,0.08f), dist);
        insert("Number Theory: primes modular arithmetic RSA cryptography", "math",
                List.of(0.22f,0.16f,0.14f,0.20f,0.80f,0.85f,0.76f,0.90f,0.08f,0.09f,0.07f,0.06f,0.08f,0.10f,0.07f,0.06f), dist);
        insert("Combinatorics: permutations combinations generating functions", "math",
                List.of(0.18f,0.20f,0.16f,0.14f,0.86f,0.78f,0.84f,0.80f,0.06f,0.07f,0.09f,0.08f,0.06f,0.09f,0.10f,0.07f), dist);

        insert("Neapolitan Pizza: wood-fired dough San Marzano tomatoes", "food",
                List.of(0.08f,0.06f,0.09f,0.07f,0.07f,0.08f,0.06f,0.09f,0.90f,0.86f,0.78f,0.72f,0.08f,0.06f,0.09f,0.07f), dist);
        insert("Sushi: vinegared rice raw fish and nori rolls", "food",
                List.of(0.06f,0.08f,0.07f,0.09f,0.09f,0.06f,0.08f,0.07f,0.86f,0.90f,0.82f,0.76f,0.07f,0.09f,0.06f,0.08f), dist);
        insert("Ramen: noodle soup with chashu pork and soft-boiled eggs", "food",
                List.of(0.09f,0.07f,0.06f,0.08f,0.08f,0.09f,0.07f,0.06f,0.82f,0.78f,0.90f,0.84f,0.09f,0.07f,0.08f,0.06f), dist);
        insert("Tacos: corn tortillas with carnitas salsa and cilantro", "food",
                List.of(0.07f,0.09f,0.08f,0.06f,0.06f,0.07f,0.09f,0.08f,0.78f,0.82f,0.86f,0.90f,0.06f,0.08f,0.07f,0.09f), dist);
        insert("Croissant: laminated pastry with buttery flaky layers", "food",
                List.of(0.06f,0.07f,0.10f,0.09f,0.10f,0.06f,0.07f,0.10f,0.85f,0.80f,0.76f,0.82f,0.09f,0.07f,0.10f,0.06f), dist);

        insert("Basketball: fast-paced shooting dribbling slam dunks", "sports",
                List.of(0.09f,0.07f,0.08f,0.10f,0.08f,0.09f,0.07f,0.06f,0.08f,0.07f,0.09f,0.06f,0.91f,0.85f,0.78f,0.72f), dist);
        insert("Football: tackles touchdowns field goals and strategy", "sports",
                List.of(0.07f,0.09f,0.06f,0.08f,0.09f,0.07f,0.10f,0.08f,0.07f,0.09f,0.08f,0.07f,0.87f,0.89f,0.82f,0.76f), dist);
        insert("Tennis: racket volleys groundstrokes and Wimbledon serves", "sports",
                List.of(0.08f,0.06f,0.09f,0.07f,0.07f,0.08f,0.06f,0.09f,0.09f,0.06f,0.07f,0.08f,0.83f,0.80f,0.88f,0.82f), dist);
        insert("Chess: openings endgames tactics strategic board game", "sports",
                List.of(0.25f,0.20f,0.22f,0.18f,0.22f,0.18f,0.20f,0.15f,0.06f,0.08f,0.07f,0.09f,0.80f,0.84f,0.78f,0.90f), dist);
        insert("Swimming: butterfly freestyle backstroke Olympic competition", "sports",
                List.of(0.06f,0.08f,0.07f,0.09f,0.08f,0.06f,0.09f,0.07f,0.10f,0.08f,0.06f,0.07f,0.85f,0.82f,0.86f,0.80f), dist);
    }
}
