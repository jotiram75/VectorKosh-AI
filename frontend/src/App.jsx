import React, { useState, useEffect } from 'react';
import Header from './components/Header';
import LeftPanel from './components/LeftPanel';
import CenterScatterPlot from './components/CenterScatterPlot';
import RightPanel from './components/RightPanel';
import { textToEmbedding } from './utils/embedding';
import { pca2D } from './utils/pca';

const API = import.meta.env.VITE_API_BASE_URL || '/api';

export default function App() {
  const [allItems, setAllItems] = useState([]);
  const [pcaPoints, setPcaPoints] = useState([]);
  const [hitIds, setHitIds] = useState(new Set());
  const [queryPt, setQueryPt] = useState(null);
  const [bounds, setBounds] = useState({ minX: -1, maxX: 1, minY: -1, maxY: 1 });

  const [queryText, setQueryText] = useState('');
  const [selAlgo, setSelAlgo] = useState('hnsw');
  const [metric, setMetric] = useState('cosine');
  const [topK, setTopK] = useState(5);

  const [searchResults, setSearchResults] = useState([]);
  const [latencyUs, setLatencyUs] = useState(null);
  const [queryEmbedding, setQueryEmbedding] = useState(null);
  const [benchData, setBenchData] = useState(null);
  const [hnswInfo, setHnswInfo] = useState(null);

  const [ollamaStatus, setOllamaStatus] = useState(null);
  const [docList, setDocList] = useState([]);
  const [insertLoading, setInsertLoading] = useState(false);
  const [insertStatusMsg, setInsertStatusMsg] = useState('');

  const [isThinking, setIsThinking] = useState(false);
  const [chatData, setChatData] = useState(null);

  // Responsive state
  const [leftOpen, setLeftOpen] = useState(true);
  const [rightOpen, setRightOpen] = useState(true);
  const [mobileTab, setMobileTab] = useState('map'); // 'controls' | 'map' | 'insights'

  // Load demo items & compute 2D PCA projection
  const loadItems = async () => {
    try {
      const res = await fetch(`${API}/items`);
      const items = await res.json();
      setAllItems(items);

      if (items.length >= 1) {
        const coords = pca2D(items.map(v => v.embedding));
        const pts = items.map((item, i) => ({ x: coords[i][0], y: coords[i][1], item }));
        setPcaPoints(pts);

        if (items.length === 1) {
          setBounds({ minX: -1, maxX: 1, minY: -1, maxY: 1 });
        } else {
          let x0 = Infinity, x1 = -Infinity, y0 = Infinity, y1 = -Infinity;
          for (const p of pts) {
            x0 = Math.min(x0, p.x);
            x1 = Math.max(x1, p.x);
            y0 = Math.min(y0, p.y);
            y1 = Math.max(y1, p.y);
          }
          const px = (x1 - x0) * 0.18 || 0.1;
          const py = (y1 - y0) * 0.18 || 0.1;
          setBounds({ minX: x0 - px, maxX: x1 + px, minY: y0 - py, maxY: y1 + py });
        }
      } else {
        setPcaPoints([]);
      }
    } catch (err) {
      console.error('Failed to load items', err);
    }
  };

  const loadHnswInfo = async () => {
    try {
      const res = await fetch(`${API}/hnsw-info`);
      const data = await res.json();
      setHnswInfo(data);
    } catch (err) {
      console.error('Failed to load HNSW info', err);
    }
  };

  const checkOllamaStatus = async () => {
    try {
      const res = await fetch(`${API}/status`);
      const data = await res.json();
      setOllamaStatus(data);
    } catch (err) {
      console.error('Failed to check status', err);
    }
  };

  const loadDocList = async () => {
    try {
      const res = await fetch(`${API}/doc/list`);
      const docs = await res.json();
      setDocList(docs);
    } catch (err) {
      console.error('Failed to load doc list', err);
    }
  };

  useEffect(() => {
    loadItems().then(loadHnswInfo);
    checkOllamaStatus();
  }, []);

  // Run 16D demo vector search
  const handleSearch = async () => {
    const text = queryText.trim();
    if (!text) return;
    const emb = textToEmbedding(text);
    setQueryEmbedding(emb);

    // On mobile, auto-switch to insights tab on search
    if (window.innerWidth <= 768) {
      setMobileTab('insights');
    }

    const url = `${API}/search?v=${emb.join(',')}&k=${topK}&metric=${metric}&algo=${selAlgo}`;
    try {
      const res = await fetch(url);
      const data = await res.json();
      const results = data.results || [];
      setSearchResults(results);
      setLatencyUs(data.latencyUs || 0);

      const hits = new Set(results.map(r => r.id));
      setHitIds(hits);

      if (results.length > 0) {
        let sx = 0, sy = 0, sw = 0;
        for (let i = 0; i < Math.min(3, results.length); i++) {
          const pt = pcaPoints.find(p => p.item.id === results[i].id);
          if (pt) {
            const w = 1 / (i + 1);
            sx += pt.x * w;
            sy += pt.y * w;
            sw += w;
          }
        }
        if (sw > 0) {
          setQueryPt({
            x: sx / sw + (Math.random() - 0.5) * 0.015,
            y: sy / sw + (Math.random() - 0.5) * 0.015
          });
        }
      }
    } catch (err) {
      alert('Cannot reach server — is Spring Boot running on :8080?');
    }
  };

  const handleInsertVector = async (metadata, category) => {
    const emb = textToEmbedding(`${metadata} ${category}`);
    try {
      await fetch(`${API}/insert`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ metadata, category, embedding: emb })
      });
      await loadItems();
      loadHnswInfo();
    } catch (err) {
      console.error('Failed to insert vector', err);
    }
  };

  const handleDeleteVector = async (id) => {
    try {
      await fetch(`${API}/delete/${id}`, { method: 'DELETE' });
      setSearchResults(prev => prev.filter(r => r.id !== id));
      setHitIds(prev => {
        const next = new Set(prev);
        next.delete(id);
        return next;
      });
      await loadItems();
      loadHnswInfo();
    } catch (err) {
      console.error('Failed to delete vector', err);
    }
  };

  const handleRunBenchmark = async () => {
    const text = queryText.trim() || 'binary tree algorithm';
    const emb = textToEmbedding(text);
    if (window.innerWidth <= 768) {
      setMobileTab('insights');
    }
    try {
      const res = await fetch(`${API}/benchmark?v=${emb.join(',')}&k=5&metric=${metric}`);
      const data = await res.json();
      setBenchData(data);
    } catch (err) {
      console.error('Failed to run benchmark', err);
    }
  };

  const handleInsertDoc = async (title, text) => {
    setInsertLoading(true);
    setInsertStatusMsg('Calling Ollama nomic-embed-text…');
    try {
      const res = await fetch(`${API}/doc/insert`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ title, text })
      });
      const data = await res.json();
      if (data.error) {
        setInsertStatusMsg(`✗ ${data.error}`);
        setInsertLoading(false);
        return false;
      } else {
        setInsertStatusMsg(`✓ Inserted ${data.chunks} chunk(s) · ${data.dims}D embeddings`);

        const emb16 = textToEmbedding(`${title} ${text}`);
        await fetch(`${API}/insert`, {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({ metadata: title, category: 'doc', embedding: emb16 })
        });
        await loadItems();
        loadHnswInfo();
        loadDocList();
        checkOllamaStatus();
        setInsertLoading(false);
        return true;
      }
    } catch (err) {
      setInsertStatusMsg('✗ Server error');
      setInsertLoading(false);
      return false;
    }
  };

  const handleDeleteDoc = async (id) => {
    try {
      await fetch(`${API}/doc/delete/${id}`, { method: 'DELETE' });
      loadDocList();
      checkOllamaStatus();
    } catch (err) {
      console.error('Failed to delete doc', err);
    }
  };

  const handleAskAI = async (question, k) => {
    setIsThinking(true);
    setChatData({ question, thinking: true });

    const qEmb16 = textToEmbedding(question);
    setQueryEmbedding(qEmb16);

    const vectorSearchPromise = fetch(`${API}/search?v=${qEmb16.join(',')}&k=${k}&metric=cosine&algo=hnsw`)
      .then(res => res.json())
      .catch(() => null);

    const docSearchPromise = fetch(`${API}/doc/search`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ question, k })
    })
      .then(res => res.json())
      .catch(() => null);

    Promise.all([vectorSearchPromise, docSearchPromise]).then(([vData, dData]) => {
      const hits = new Set();

      if (vData && vData.results) {
        vData.results.forEach(r => hits.add(r.id));
      }

      if (dData && dData.contexts) {
        dData.contexts.forEach(ctx => {
          const cleanTitle = ctx.title.split(' [')[0].trim().toLowerCase();
          const pt = pcaPoints.find(p => {
            if (p.item.category === 'doc') {
              const meta = p.item.metadata.trim().toLowerCase();
              return cleanTitle.includes(meta) || meta.includes(cleanTitle);
            }
            return false;
          });
          if (pt) hits.add(pt.item.id);
        });
      }

      setHitIds(hits);

      let sx = 0, sy = 0, sw = 0;
      let rank = 1;
      for (const id of hits) {
        const pt = pcaPoints.find(p => p.item.id === id);
        if (pt) {
          const w = 1 / rank;
          sx += pt.x * w;
          sy += pt.y * w;
          sw += w;
          rank++;
        }
      }

      if (sw > 0) {
        setQueryPt({
          x: sx / sw + (Math.random() - 0.5) * 0.015,
          y: sy / sw + (Math.random() - 0.5) * 0.015
        });
      } else if (pcaPoints.length > 0) {
        setQueryPt({
          x: (bounds.minX + bounds.maxX) / 2 + (Math.random() - 0.5) * 0.05,
          y: (bounds.minY + bounds.maxY) / 2 + (Math.random() - 0.5) * 0.05
        });
      }
    });

    try {
      const res = await fetch(`${API}/doc/ask`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ question, k })
      });
      const data = await res.json();
      setIsThinking(false);

      if (data.error) {
        setChatData({ question, error: data.error });
      } else {
        setChatData({
          question,
          answer: data.answer,
          model: data.model,
          contexts: data.contexts || []
        });
      }
    } catch (err) {
      setIsThinking(false);
      setChatData({
        question,
        error: 'Server error — is the backend running?'
      });
    }
  };

  const handleTabChange = (tabName) => {
    if (tabName === 'docs') loadDocList();
  };

  const statsLabel = `${allItems.length} vectors · 16 dims`;

  return (
    <>
      <Header
        ollamaStatus={ollamaStatus}
        statsLabel={statsLabel}
        leftOpen={leftOpen}
        setLeftOpen={setLeftOpen}
        rightOpen={rightOpen}
        setRightOpen={setRightOpen}
        mobileTab={mobileTab}
        setMobileTab={setMobileTab}
      />

      <div className="layout">
        <LeftPanel
          queryText={queryText}
          setQueryText={setQueryText}
          onSearch={handleSearch}
          selAlgo={selAlgo}
          setSelAlgo={setSelAlgo}
          metric={metric}
          setMetric={setMetric}
          topK={topK}
          setTopK={setTopK}
          onInsertVector={handleInsertVector}
          onRunBenchmark={handleRunBenchmark}
          isOpen={leftOpen}
          isMobileActive={mobileTab === 'controls'}
        />

        <CenterScatterPlot
          pcaPoints={pcaPoints}
          hitIds={hitIds}
          queryPt={queryPt}
          bounds={bounds}
          isMobileHidden={mobileTab !== 'map'}
        />

        <RightPanel
          latencyUs={latencyUs}
          selAlgo={selAlgo}
          metric={metric}
          topK={topK}
          searchResults={searchResults}
          onDeleteVector={handleDeleteVector}
          queryEmbedding={queryEmbedding}
          benchData={benchData}
          hnswInfo={hnswInfo}
          ollamaStatus={ollamaStatus}
          docList={docList}
          onInsertDoc={handleInsertDoc}
          onDeleteDoc={handleDeleteDoc}
          insertLoading={insertLoading}
          insertStatusMsg={insertStatusMsg}
          onAskAI={handleAskAI}
          isThinking={isThinking}
          chatData={chatData}
          onTabChange={handleTabChange}
          isOpen={rightOpen}
          isMobileActive={mobileTab === 'insights'}
        />
      </div>
    </>
  );
}
