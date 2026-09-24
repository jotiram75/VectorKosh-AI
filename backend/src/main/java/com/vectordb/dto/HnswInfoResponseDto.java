package com.vectordb.dto;

import java.util.List;

public class HnswInfoResponseDto {
    private int topLayer;
    private int nodeCount;
    private List<Integer> nodesPerLayer;
    private List<Integer> edgesPerLayer;
    private List<NodeInfoDto> nodes;
    private List<EdgeInfoDto> edges;

    public HnswInfoResponseDto() {}

    public HnswInfoResponseDto(int topLayer, int nodeCount, List<Integer> nodesPerLayer, List<Integer> edgesPerLayer, List<NodeInfoDto> nodes, List<EdgeInfoDto> edges) {
        this.topLayer = topLayer;
        this.nodeCount = nodeCount;
        this.nodesPerLayer = nodesPerLayer;
        this.edgesPerLayer = edgesPerLayer;
        this.nodes = nodes;
        this.edges = edges;
    }

    public int getTopLayer() { return topLayer; }
    public void setTopLayer(int topLayer) { this.topLayer = topLayer; }

    public int getNodeCount() { return nodeCount; }
    public void setNodeCount(int nodeCount) { this.nodeCount = nodeCount; }

    public List<Integer> getNodesPerLayer() { return nodesPerLayer; }
    public void setNodesPerLayer(List<Integer> nodesPerLayer) { this.nodesPerLayer = nodesPerLayer; }

    public List<Integer> getEdgesPerLayer() { return edgesPerLayer; }
    public void setEdgesPerLayer(List<Integer> edgesPerLayer) { this.edgesPerLayer = edgesPerLayer; }

    public List<NodeInfoDto> getNodes() { return nodes; }
    public void setNodes(List<NodeInfoDto> nodes) { this.nodes = nodes; }

    public List<EdgeInfoDto> getEdges() { return edges; }
    public void setEdges(List<EdgeInfoDto> edges) { this.edges = edges; }

    public static class NodeInfoDto {
        private int id;
        private String metadata;
        private String category;
        private int maxLyr;

        public NodeInfoDto() {}

        public NodeInfoDto(int id, String metadata, String category, int maxLyr) {
            this.id = id;
            this.metadata = metadata;
            this.category = category;
            this.maxLyr = maxLyr;
        }

        public int getId() { return id; }
        public void setId(int id) { this.id = id; }

        public String getMetadata() { return metadata; }
        public void setMetadata(String metadata) { this.metadata = metadata; }

        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }

        public int getMaxLyr() { return maxLyr; }
        public void setMaxLyr(int maxLyr) { this.maxLyr = maxLyr; }
    }

    public static class EdgeInfoDto {
        private int src;
        private int dst;
        private int lyr;

        public EdgeInfoDto() {}

        public EdgeInfoDto(int src, int dst, int lyr) {
            this.src = src;
            this.dst = dst;
            this.lyr = lyr;
        }

        public int getSrc() { return src; }
        public void setSrc(int src) { this.src = src; }

        public int getDst() { return dst; }
        public void setDst(int dst) { this.dst = dst; }

        public int getLyr() { return lyr; }
        public void setLyr(int lyr) { this.lyr = lyr; }
    }
}
