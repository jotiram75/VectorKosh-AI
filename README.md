# VectorKosh AI — High-Performance Vector Database & Local RAG Workbench

[![Live Demo](https://img.shields.io/badge/Live%20Demo-vectorkoshai.vercel.app-000000?style=for-the-badge&logo=vercel&logoColor=white)](https://vectorkoshai.vercel.app/)

A fully working **Vector Database & RAG Pipeline** built from scratch in Java with **Spring Boot** and a **React JS** web UI.  
Implements **HNSW**, **KD-Tree**, and **Brute Force** search algorithms side-by-side, plus a **RAG pipeline** powered by a local LLM via Ollama.

🌐 **Live Demo App:** [https://vectorkoshai.vercel.app/](https://vectorkoshai.vercel.app/)

> Built as an educational project to show how production vector databases like Pinecone, Weaviate, and Chroma actually work under the hood.

---

## What This Project Does

| Feature | Description |
|---|---|
| **3 Search Algorithms** | HNSW (production-grade), KD-Tree, Brute Force — run all three and compare speed |
| **3 Distance Metrics** | Cosine similarity, Euclidean distance, Manhattan distance |
| **16D Demo Vectors** | 20 pre-loaded semantic vectors across 4 categories (CS, Math, Food, Sports) |
| **2D PCA Scatter Plot** | Live visualization of semantic space — watch clusters form |
| **Real Document Embedding** | Paste any text → Ollama embeds it with `nomic-embed-text` (768D) |
| **RAG Pipeline** | Ask questions about your documents → HNSW retrieves context → local LLM answers |
| **Full REST API** | CRUD endpoints: insert, delete, search, benchmark, hnsw-info |

---

## How It Works

```
Your Text
    │
    ▼
Ollama (nomic-embed-text)          ← converts text to a 768-dimensional vector
    │
    ▼
HNSW Index (Spring Boot)           ← indexes the vector in a multilayer graph
    │
    ▼
Semantic Search                    ← finds nearest neighbors in vector space
    │
    ▼
Ollama (llama3.2)                  ← reads retrieved chunks, generates an answer
    │
    ▼
Answer
```

**HNSW (Hierarchical Navigable Small World)** is the same algorithm used by Pinecone, Weaviate, Chroma, and Milvus. It builds a multilayer graph where each layer is progressively sparser — searches start at the top layer and zoom in, achieving O(log N) complexity instead of O(N) for brute force.

---

## Prerequisites

You need **4 things** installed on your laptop:

1. **Java JDK 17+** (runs the Spring Boot backend)
2. **Node.js & npm** (runs the React JS frontend)
3. **Git**
4. **Ollama** (runs the local AI models)

---

## Step-by-Step Setup (Windows)

### Step 1 — Verify / Install Java 17+ (JDK)

1. Open PowerShell and check Java version:
```powershell
java -version
```
2. If Java is not installed or version is below 17, download OpenJDK 17+ from **https://adoptium.net** and run the installer.

---

### Step 2 — Verify / Install Node.js & npm

1. Open PowerShell and check Node.js version:
```powershell
node -v
npm -v
```
2. If Node.js is not installed, download the LTS version from **https://nodejs.org** and install it.

---

### Step 3 — Install Git

1. Go to **https://git-scm.com/download/win** and download Git for Windows
2. Run the installer with default settings
3. Verify in PowerShell:
```powershell
git --version
```

---

### Step 4 — Install Ollama (Local AI Models)

1. Go to **https://ollama.com** and click **Download for Windows**
2. Run the installer
3. Ollama starts automatically in the system tray
4. Open **PowerShell** and pull the two required models:

```powershell
ollama pull nomic-embed-text
```
*(~274 MB — this is the embedding model)*

```powershell
ollama pull llama3.2
```
*(~2 GB — this is the language model)*

5. Verify Ollama is running:
```powershell
ollama list
```
You should see both models listed.

> **Minimum specs for Ollama:** 8GB RAM recommended. The models will use ~3GB total.

---

### Step 5 — Clone the Repository

Open **PowerShell** and run:

```powershell
git clone https://github.com/YOUR_USERNAME/VectorDB.git
cd VectorDB
```

*(Replace `YOUR_USERNAME` with the actual GitHub username)*

---

### Step 6 — Run Spring Boot Backend & React Frontend

**Terminal 1** — Start Ollama (if not already running):
```powershell
ollama serve
```
*(If Ollama is already in the system tray, skip this)*

**Terminal 2** — Start the Spring Boot Backend:
```powershell
cd backend
mvn spring-boot:run
```

You should see Spring Boot startup logs:
```
Started VectorDbApplication on port 8080
```

**Terminal 3** — Start the React JS Frontend:
```powershell
cd frontend
npm install
npm run dev
```

**Open your browser** and go to:
```
http://localhost:3000
```

---

## Using the Application

### Tab 1: Search (Demo Vectors)

- Type any concept in the search box: `binary tree`, `sushi`, `basketball`, `calculus`
- Choose your algorithm: **HNSW**, **KD-Tree**, or **Brute Force**
- Choose distance metric: **Cosine**, **Euclidean**, or **Manhattan**
- Click **⚡ SEARCH** — results appear with distances, the matching point glows on the scatter plot
- Click **▶ COMPARE ALL ALGOS** to run all 3 algorithms and compare their speed

**The scatter plot** shows all 20 vectors projected to 2D using PCA. Notice how the 4 semantic categories (CS, Math, Food, Sports) form distinct clusters — this is what "semantic similarity" looks like visually.

### Tab 2: Documents (Real Embeddings)

This uses Ollama to generate **real 768-dimensional embeddings** from any text.

1. Type a title (e.g., `Operating Systems Notes`)
2. Paste any text — lecture notes, textbook paragraphs, Wikipedia articles
3. Click **⚡ EMBED & INSERT**
4. Long documents are automatically split into overlapping 250-word chunks
5. Each chunk gets its own embedding and is stored in a separate HNSW index

### Tab 3: Ask AI (RAG Pipeline)

1. Make sure you have inserted some documents in Tab 2 first
2. Type a question about your documents
3. Click **🤖 ASK AI**

What happens behind the scenes:
```
1. Your question → embedded with nomic-embed-text (768D vector)
2. HNSW search → finds 3 most semantically similar chunks
3. Retrieved chunks → sent as context to llama3.2
4. llama3.2 → generates an answer based only on your documents
```

The answer streams in with a typewriter effect. Click the **context chips** to see exactly which chunks the AI used.

---

## REST API Reference

The server exposes a full REST API at `http://localhost:8080`.

### Demo Vector Endpoints

| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/search?v=f1,f2,...&k=5&metric=cosine&algo=hnsw` | K-NN search |
| `POST` | `/insert` | Insert a demo vector |
| `DELETE` | `/delete/:id` | Delete by ID |
| `GET` | `/items` | List all demo vectors |
| `GET` | `/benchmark?v=...&k=5&metric=cosine` | Compare all 3 algorithms |
| `GET` | `/hnsw-info` | HNSW graph structure and layer stats |
| `GET` | `/stats` | Database statistics |

### Document & RAG Endpoints

| Method | Endpoint | Body | Description |
|---|---|---|---|
| `POST` | `/doc/insert` | `{"title":"...","text":"..."}` | Embed and store document |
| `GET` | `/doc/list` | — | List all stored documents |
| `DELETE` | `/doc/delete/:id` | — | Delete document chunk |
| `POST` | `/doc/ask` | `{"question":"...","k":3}` | RAG: retrieve + generate |
| `GET` | `/status` | — | Ollama status and model info |

### Example: Search via curl

```powershell
curl "http://localhost:8080/search?v=0.9,0.8,0.7,0.6,0.1,0.1,0.1,0.1,0.1,0.1,0.1,0.1,0.1,0.1,0.1,0.1&k=3&metric=cosine&algo=hnsw"
```

### Example: Ask a question via curl

```powershell
curl -X POST http://localhost:8080/doc/ask `
  -H "Content-Type: application/json" `
  -d '{"question":"What is dynamic programming?","k":3}'
```

---

## Project Structure

```
VectorDB/
├── backend/                  ← Spring Boot Backend (Java 17)
│   ├── pom.xml
│   └── src/main/java/com/vectordb/
│       ├── algorithm/       ← HNSW, KD-Tree, BruteForce, DistanceMetrics
│       ├── controller/      ← REST Controllers
│       ├── dto/             ← Request/Response DTOs
│       ├── model/           ← VectorItem, DocItem
│       └── service/         ← VectorDBService, DocumentDBService, OllamaClient, TextChunker
├── frontend/                 ← React JS Frontend (Vite)
│   ├── package.json
│   ├── vite.config.js
│   └── src/
│       ├── components/      ← Header, LeftPanel, CenterScatterPlot, RightPanel, Tabs
│       ├── utils/           ← embedding.js, pca.js
│       └── App.jsx
└── README.md                 ← This file
```

### Architecture (Spring Boot Backend)

```
BruteForce          O(N·d)      Exact, baseline
KDTree              O(log N)    Exact, axis-aligned partitioning
HNSW                O(log N)    Approximate, multilayer small-world graph

VectorDBService     Unified manager over all 3 (16D demo vectors)
DocumentDBService   HNSW-only index for real Ollama embeddings (768D)
OllamaClient        HTTP client → /api/embeddings + /api/generate
```

---

## Algorithm Deep Dive

### HNSW (Hierarchical Navigable Small World)

Nodes are inserted into a multilayer graph. Each node randomly gets assigned a maximum layer. Layer 0 has all nodes with many connections; higher layers have fewer nodes (exponentially fewer) with longer-range connections.

**Insert:** Start at the top layer, greedily find the nearest node, drop a layer, repeat. At each layer from your assigned max down to 0, run a beam search (ef_construction=200) and connect to the M nearest neighbors bidirectionally.

**Search:** Same greedy descent from top layer. At layer 0, expand to ef nearest candidates using a priority queue.

**Why it's fast:** The upper layers act like a highway — you quickly get to the right neighborhood, then zoom in at layer 0.

### KD-Tree (K-Dimensional Tree)

Binary space partitioning. Each node splits space along one dimension (cycling through all dimensions). Search prunes entire subtrees when the closest possible point in that subtree can't beat the current best — the "ball within hyperslab" check.

**Weakness:** Degrades with high dimensions (curse of dimensionality). Works well for ≤20D, becomes close to brute force at 768D.

### Why HNSW Wins at High Dimensions

KD-Tree pruning relies on axis-aligned distance bounds. In high dimensions, almost all the space is near the boundary of the hypersphere — no subtrees get pruned. HNSW's graph-based approach doesn't have this problem.

---

## Common Issues

| Problem | Fix |
|---|---|
| `Ollama: OFFLINE` in header | Run `ollama serve` in a terminal |
| Embedding takes forever | Ollama is downloading the model on first use, wait 2 min |
| Port 8080 already in use | Kill the process: `netstat -ano \| findstr 8080` then `taskkill /PID <pid> /F` |
| LLM answer is slow | Normal — llama3.2 takes 10–30s on a laptop CPU. Use llama3.2:1b for faster answers |

### Use a Smaller/Faster LLM

If llama3.2 is too slow on your laptop, switch to the 1B model:

```powershell
ollama pull llama3.2:1b
```

Then edit [OllamaClient.java](backend/src/main/java/com/vectordb/service/OllamaClient.java) line where `genModel` is set:
```java
private final String genModel = "llama3.2:1b";   // change this
```

---

## License

MIT — use this however you want.
