package com.vectordb.controller;

import com.vectordb.algorithm.DistanceMetrics;
import com.vectordb.algorithm.HNSWIndex;
import com.vectordb.dto.*;
import com.vectordb.model.VectorItem;
import com.vectordb.service.DocumentDBService;
import com.vectordb.service.OllamaClient;
import com.vectordb.service.TextChunker;
import com.vectordb.service.VectorDBService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@CrossOrigin(origins = "*")
public class VectorDBController {

    private final VectorDBService vectorDBService;
    private final DocumentDBService documentDBService;
    private final OllamaClient ollamaClient;
    private final TextChunker textChunker;

    public VectorDBController(VectorDBService vectorDBService,
                              DocumentDBService documentDBService,
                              OllamaClient ollamaClient,
                              TextChunker textChunker) {
        this.vectorDBService = vectorDBService;
        this.documentDBService = documentDBService;
        this.ollamaClient = ollamaClient;
        this.textChunker = textChunker;
    }

    private List<Float> parseVec(String s) {
        if (s == null || s.trim().isEmpty()) return new ArrayList<>();
        List<Float> v = new ArrayList<>();
        for (String t : s.split(",")) {
            try {
                v.add(Float.parseFloat(t.trim()));
            } catch (Exception ignored) {}
        }
        return v;
    }

    // ── DEMO VECTOR ENDPOINTS ─────────────────────────────────────────

    @GetMapping("/search")
    public ResponseEntity<?> search(
            @RequestParam("v") String vStr,
            @RequestParam(value = "k", defaultValue = "5") int k,
            @RequestParam(value = "metric", defaultValue = "cosine") String metric,
            @RequestParam(value = "algo", defaultValue = "hnsw") String algo) {

        List<Float> q = parseVec(vStr);
        if (q.size() != VectorDBService.DIMS) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "need " + VectorDBService.DIMS + "D vector"));
        }

        SearchResponseDto out = vectorDBService.search(q, k, metric, algo);
        return ResponseEntity.ok(out);
    }

    @PostMapping("/insert")
    public ResponseEntity<?> insert(@RequestBody InsertVectorRequestDto req) {
        if (req.getMetadata() == null || req.getEmbedding() == null || req.getEmbedding().size() != VectorDBService.DIMS) {
            return ResponseEntity.badRequest().body(Map.of("error", "invalid body"));
        }
        int id = vectorDBService.insert(req.getMetadata(), req.getCategory(), req.getEmbedding(), DistanceMetrics.getDistFn("cosine"));
        return ResponseEntity.ok(Map.of("id", id));
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> delete(@PathVariable("id") int id) {
        boolean ok = vectorDBService.remove(id);
        return ResponseEntity.ok(Map.of("ok", ok));
    }

    @GetMapping("/items")
    public ResponseEntity<List<VectorItem>> getItems() {
        return ResponseEntity.ok(vectorDBService.getAll());
    }

    @GetMapping("/benchmark")
    public ResponseEntity<?> benchmark(
            @RequestParam("v") String vStr,
            @RequestParam(value = "k", defaultValue = "5") int k,
            @RequestParam(value = "metric", defaultValue = "cosine") String metric) {

        List<Float> q = parseVec(vStr);
        if (q.size() != VectorDBService.DIMS) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "need " + VectorDBService.DIMS + "D vector"));
        }

        BenchmarkResponseDto b = vectorDBService.benchmark(q, k, metric);
        return ResponseEntity.ok(b);
    }

    @GetMapping("/hnsw-info")
    public ResponseEntity<HnswInfoResponseDto> getHnswInfo() {
        HNSWIndex.GraphInfo gi = vectorDBService.getHnswInfo();

        List<HnswInfoResponseDto.NodeInfoDto> nodes = gi.nodes.stream()
                .map(n -> new HnswInfoResponseDto.NodeInfoDto(n.id, n.metadata, n.category, n.maxLyr))
                .collect(Collectors.toList());

        List<HnswInfoResponseDto.EdgeInfoDto> edges = gi.edges.stream()
                .map(e -> new HnswInfoResponseDto.EdgeInfoDto(e.src, e.dst, e.lyr))
                .collect(Collectors.toList());

        HnswInfoResponseDto dto = new HnswInfoResponseDto(
                gi.topLayer, gi.nodeCount, gi.nodesPerLayer, gi.edgesPerLayer, nodes, edges);

        return ResponseEntity.ok(dto);
    }

    @GetMapping("/stats")
    public ResponseEntity<StatsResponseDto> getStats() {
        StatsResponseDto dto = new StatsResponseDto(
                vectorDBService.size(),
                VectorDBService.DIMS,
                List.of("bruteforce", "kdtree", "hnsw"),
                List.of("euclidean", "cosine", "manhattan")
        );
        return ResponseEntity.ok(dto);
    }

    // ── DOCUMENT + RAG ENDPOINTS ──────────────────────────────────────

    @PostMapping("/doc/insert")
    public ResponseEntity<?> insertDoc(@RequestBody InsertDocRequestDto req) {
        if (req.getTitle() == null || req.getTitle().trim().isEmpty() ||
            req.getText() == null || req.getText().trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "need title and text"));
        }

        List<String> chunks = textChunker.chunkText(req.getText(), 250, 30);
        List<Integer> ids = new ArrayList<>();

        for (int i = 0; i < chunks.size(); i++) {
            List<Float> emb = ollamaClient.embed(chunks.get(i));
            if (emb.isEmpty()) {
                return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                        .body(Map.of("error", "Ollama unavailable. Install from https://ollama.com then run: ollama pull nomic-embed-text && ollama pull llama3.2"));
            }

            String chunkTitle = (chunks.size() > 1)
                    ? req.getTitle() + " [" + (i + 1) + "/" + chunks.size() + "]"
                    : req.getTitle();

            ids.add(documentDBService.insert(chunkTitle, chunks.get(i), emb));
        }

        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("ids", ids);
        resp.put("chunks", chunks.size());
        resp.put("dims", documentDBService.getDims());
        return ResponseEntity.ok(resp);
    }

    @DeleteMapping("/doc/delete/{id}")
    public ResponseEntity<?> deleteDoc(@PathVariable("id") int id) {
        boolean ok = documentDBService.remove(id);
        return ResponseEntity.ok(Map.of("ok", ok));
    }

    @GetMapping("/doc/list")
    public ResponseEntity<List<DocItemSummaryDto>> getDocList() {
        List<DocItemSummaryDto> list = documentDBService.getAll().stream().map(doc -> {
            String preview = doc.getText().substring(0, Math.min(doc.getText().length(), 120));
            if (doc.getText().length() > 120) preview += "…";
            int words = doc.getText().trim().split("\\s+").length;
            return new DocItemSummaryDto(doc.getId(), doc.getTitle(), preview, words);
        }).collect(Collectors.toList());

        return ResponseEntity.ok(list);
    }

    @PostMapping("/doc/search")
    public ResponseEntity<?> searchDoc(@RequestBody AskRequestDto req) {
        if (req.getQuestion() == null || req.getQuestion().trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "need question"));
        }

        List<Float> qEmb = ollamaClient.embed(req.getQuestion());
        if (qEmb.isEmpty()) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(Map.of("error", "Ollama unavailable"));
        }

        List<DocumentDBService.DocHit> hits = documentDBService.search(qEmb, req.getK());
        List<DocSearchResponseDto.SearchHitSummaryDto> contexts = hits.stream()
                .map(h -> new DocSearchResponseDto.SearchHitSummaryDto(h.item.getId(), h.item.getTitle(), h.dist))
                .collect(Collectors.toList());

        return ResponseEntity.ok(new DocSearchResponseDto(contexts));
    }

    @PostMapping("/doc/ask")
    public ResponseEntity<?> askDoc(@RequestBody AskRequestDto req) {
        if (req.getQuestion() == null || req.getQuestion().trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "need question"));
        }

        // Step 1: embed question
        List<Float> qEmb = ollamaClient.embed(req.getQuestion());
        if (qEmb.isEmpty()) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(Map.of("error", "Ollama unavailable"));
        }

        // Step 2: retrieve top-k chunks
        List<DocumentDBService.DocHit> hits = documentDBService.search(qEmb, req.getK());

        // Step 3: build prompt
        StringBuilder ctxStr = new StringBuilder();
        for (int i = 0; i < hits.size(); i++) {
            ctxStr.append("[").append(i + 1).append("] ")
                  .append(hits.get(i).item.getTitle()).append(":\n")
                  .append(hits.get(i).item.getText()).append("\n\n");
        }

        String prompt =
                "You are a helpful assistant. Answer the user's question directly. " +
                "Use the provided context if it contains relevant information. " +
                "If it doesn't, just use your own general knowledge. " +
                "IMPORTANT: Do NOT mention the 'context', 'provided text', or say things like 'the context doesn't mention'. " +
                "Just answer the question naturally.\n\n" +
                "Context:\n" + ctxStr +
                "Question: " + req.getQuestion() + "\n\n" +
                "Answer:";

        // Step 4: generate answer
        String answer = ollamaClient.generate(prompt);

        // Step 5: format response
        List<AskResponseDto.DocContextDto> contextDtos = hits.stream()
                .map(h -> new AskResponseDto.DocContextDto(h.item.getId(), h.item.getTitle(), h.item.getText(), h.dist))
                .collect(Collectors.toList());

        AskResponseDto resp = new AskResponseDto(
                answer, ollamaClient.getGenModel(), contextDtos, documentDBService.size());

        return ResponseEntity.ok(resp);
    }

    @GetMapping("/status")
    public ResponseEntity<StatusResponseDto> getStatus() {
        boolean up = ollamaClient.isAvailable();
        StatusResponseDto dto = new StatusResponseDto(
                up,
                ollamaClient.getEmbedModel(),
                ollamaClient.getGenModel(),
                documentDBService.size(),
                documentDBService.getDims(),
                VectorDBService.DIMS,
                vectorDBService.size()
        );
        return ResponseEntity.ok(dto);
    }
}
