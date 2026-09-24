package com.vectordb.algorithm;

import com.vectordb.model.DistFn;
import com.vectordb.model.VectorItem;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class HNSWIndex {

    public static class Node {
        public VectorItem item;
        public int maxLyr;
        public List<List<Integer>> nbrs;

        public Node(VectorItem item, int maxLyr) {
            this.item = item;
            this.maxLyr = maxLyr;
            this.nbrs = new ArrayList<>(maxLyr + 1);
            for (int i = 0; i <= maxLyr; i++) {
                this.nbrs.add(new ArrayList<>());
            }
        }
    }

    private final Map<Integer, Node> G = new ConcurrentHashMap<>();
    private final int M;
    private final int M0;
    private final int efBuild;
    private final float mL;
    private int topLayer = -1;
    private int entryPt = -1;
    private final Random rng = new Random(42);

    public HNSWIndex(int m, int efBuild) {
        this.M = m;
        this.M0 = 2 * m;
        this.efBuild = efBuild;
        this.mL = 1.0f / (float) Math.log(m);
    }

    public HNSWIndex() {
        this(16, 200);
    }

    private int randLevel() {
        float u = rng.nextFloat();
        if (u < 1e-9f) u = 1e-9f;
        return (int) Math.floor(-Math.log(u) * mL);
    }

    private List<BruteForceIndex.DistancePair> searchLayer(
            List<Float> q, int ep, int ef, int lyr, DistFn dist, int skipId) {

        Set<Integer> vis = new HashSet<>();
        PriorityQueue<BruteForceIndex.DistancePair> cands = new PriorityQueue<>();
        PriorityQueue<BruteForceIndex.DistancePair> found =
                new PriorityQueue<>(Comparator.comparing(dp -> -dp.dist));

        Node epNode = G.get(ep);
        if (epNode == null) return new ArrayList<>();

        if (ep != skipId) {
            float d0 = dist.apply(q, epNode.item.getEmbedding());
            vis.add(ep);
            cands.add(new BruteForceIndex.DistancePair(d0, ep));
            found.add(new BruteForceIndex.DistancePair(d0, ep));
        } else {
            vis.add(ep);
            if (lyr < epNode.nbrs.size()) {
                for (int nid : epNode.nbrs.get(lyr)) {
                    if (nid != skipId && G.containsKey(nid)) {
                        Node nNode = G.get(nid);
                        if (nNode != null) {
                            float nd = dist.apply(q, nNode.item.getEmbedding());
                            vis.add(nid);
                            cands.add(new BruteForceIndex.DistancePair(nd, nid));
                            found.add(new BruteForceIndex.DistancePair(nd, nid));
                        }
                    }
                }
            }
        }

        while (!cands.isEmpty()) {
            BruteForceIndex.DistancePair curr = cands.poll();
            if (found.size() >= ef && curr.dist > found.peek().dist) break;

            Node currNode = G.get(curr.id);
            if (currNode == null || lyr >= currNode.nbrs.size()) continue;

            for (int nid : currNode.nbrs.get(lyr)) {
                if (nid == skipId || vis.contains(nid) || !G.containsKey(nid)) continue;
                vis.add(nid);

                Node nNode = G.get(nid);
                if (nNode == null) continue;
                float nd = dist.apply(q, nNode.item.getEmbedding());

                if (found.size() < ef || nd < found.peek().dist) {
                    cands.add(new BruteForceIndex.DistancePair(nd, nid));
                    found.add(new BruteForceIndex.DistancePair(nd, nid));
                    if (found.size() > ef) {
                        found.poll();
                    }
                }
            }
        }

        List<BruteForceIndex.DistancePair> res = new ArrayList<>(found);
        Collections.sort(res);
        return res;
    }

    private List<Integer> selectNbrs(List<BruteForceIndex.DistancePair> cands, int maxM, int skipId) {
        List<Integer> r = new ArrayList<>();
        for (BruteForceIndex.DistancePair dp : cands) {
            if (dp.id == skipId) continue;
            r.add(dp.id);
            if (r.size() >= maxM) break;
        }
        return r;
    }

    public synchronized void insert(VectorItem item, DistFn dist) {
        int id = item.getId();
        int lvl = randLevel();
        Node node = new Node(item, lvl);
        G.put(id, node);

        if (entryPt == -1) {
            entryPt = id;
            topLayer = lvl;
            return;
        }

        int ep = entryPt;
        for (int lc = topLayer; lc > lvl; lc--) {
            Node epNode = G.get(ep);
            if (epNode != null && lc < epNode.nbrs.size()) {
                List<BruteForceIndex.DistancePair> W = searchLayer(item.getEmbedding(), ep, 1, lc, dist, id);
                if (!W.isEmpty()) {
                    ep = W.get(0).id;
                }
            }
        }

        for (int lc = Math.min(topLayer, lvl); lc >= 0; lc--) {
            List<BruteForceIndex.DistancePair> W = searchLayer(item.getEmbedding(), ep, efBuild, lc, dist, id);
            int maxM = (lc == 0) ? M0 : M;
            List<Integer> sel = selectNbrs(W, maxM, id);
            node.nbrs.get(lc).addAll(sel);

            for (int nid : sel) {
                Node nNode = G.get(nid);
                if (nNode == null || nid == id) continue;

                while (nNode.nbrs.size() <= lc) {
                    nNode.nbrs.add(new ArrayList<>());
                }

                List<Integer> conn = nNode.nbrs.get(lc);
                if (!conn.contains(id)) {
                    conn.add(id);
                }

                if (conn.size() > maxM) {
                    List<BruteForceIndex.DistancePair> ds = new ArrayList<>();
                    for (int c : conn) {
                        Node cNode = G.get(c);
                        if (cNode != null) {
                            ds.add(new BruteForceIndex.DistancePair(
                                    dist.apply(nNode.item.getEmbedding(), cNode.item.getEmbedding()), c));
                        }
                    }
                    Collections.sort(ds);
                    conn.clear();
                    for (int i = 0; i < maxM && i < ds.size(); i++) {
                        conn.add(ds.get(i).id);
                    }
                }
            }

            if (!W.isEmpty()) {
                ep = W.get(0).id;
            }
        }

        if (lvl > topLayer) {
            topLayer = lvl;
            entryPt = id;
        }
    }

    public synchronized List<BruteForceIndex.DistancePair> knn(
            List<Float> q, int k, int ef, DistFn dist) {

        if (entryPt == -1 || G.isEmpty()) return new ArrayList<>();

        int ep = entryPt;
        for (int lc = topLayer; lc > 0; lc--) {
            Node epNode = G.get(ep);
            if (epNode != null && lc < epNode.nbrs.size()) {
                List<BruteForceIndex.DistancePair> W = searchLayer(q, ep, 1, lc, dist, -1);
                if (!W.isEmpty()) ep = W.get(0).id;
            }
        }

        List<BruteForceIndex.DistancePair> W = searchLayer(q, ep, Math.max(ef, k), 0, dist, -1);
        if (W.size() > k) {
            return new ArrayList<>(W.subList(0, k));
        }
        return W;
    }

    public synchronized void remove(int id) {
        if (!G.containsKey(id)) return;

        for (Node nd : G.values()) {
            for (List<Integer> layer : nd.nbrs) {
                layer.removeIf(nbrId -> nbrId == id);
            }
        }

        G.remove(id);

        if (G.isEmpty()) {
            entryPt = -1;
            topLayer = -1;
            return;
        }

        if (entryPt == id) {
            int newTop = -1;
            int newEp = -1;
            for (Map.Entry<Integer, Node> e : G.entrySet()) {
                if (e.getValue().maxLyr > newTop) {
                    newTop = e.getValue().maxLyr;
                    newEp = e.getKey();
                }
            }
            entryPt = newEp;
            topLayer = newTop;
        }
    }

    public static class GraphInfo {
        public int topLayer;
        public int nodeCount;
        public List<Integer> nodesPerLayer;
        public List<Integer> edgesPerLayer;

        public static class NodeView {
            public int id;
            public String metadata;
            public String category;
            public int maxLyr;

            public NodeView(int id, String metadata, String category, int maxLyr) {
                this.id = id;
                this.metadata = metadata;
                this.category = category;
                this.maxLyr = maxLyr;
            }
        }

        public static class EdgeView {
            public int src;
            public int dst;
            public int lyr;

            public EdgeView(int src, int dst, int lyr) {
                this.src = src;
                this.dst = dst;
                this.lyr = lyr;
            }
        }

        public List<NodeView> nodes = new ArrayList<>();
        public List<EdgeView> edges = new ArrayList<>();
    }

    public synchronized GraphInfo getInfo() {
        GraphInfo gi = new GraphInfo();
        gi.topLayer = topLayer;
        gi.nodeCount = G.size();
        int maxL = Math.max(topLayer + 1, 1);

        gi.nodesPerLayer = new ArrayList<>(Collections.nCopies(maxL, 0));
        gi.edgesPerLayer = new ArrayList<>(Collections.nCopies(maxL, 0));

        for (Map.Entry<Integer, Node> entry : G.entrySet()) {
            int id = entry.getKey();
            Node nd = entry.getValue();

            gi.nodes.add(new GraphInfo.NodeView(id, nd.item.getMetadata(), nd.item.getCategory(), nd.maxLyr));

            for (int lc = 0; lc <= nd.maxLyr && lc < maxL; lc++) {
                gi.nodesPerLayer.set(lc, gi.nodesPerLayer.get(lc) + 1);

                if (lc < nd.nbrs.size()) {
                    for (int nid : nd.nbrs.get(lc)) {
                        if (id < nid) {
                            gi.edgesPerLayer.set(lc, gi.edgesPerLayer.get(lc) + 1);
                            gi.edges.add(new GraphInfo.EdgeView(id, nid, lc));
                        }
                    }
                }
            }
        }

        return gi;
    }

    public int size() {
        return G.size();
    }
}

