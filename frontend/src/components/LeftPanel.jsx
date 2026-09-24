import React, { useState } from 'react';

export default function LeftPanel({
  queryText,
  setQueryText,
  onSearch,
  selAlgo,
  setSelAlgo,
  metric,
  setMetric,
  topK,
  setTopK,
  onInsertVector,
  onRunBenchmark,
  isOpen,
  isMobileActive
}) {
  const [addMeta, setAddMeta] = useState('');
  const [addCat, setAddCat] = useState('cs');

  const sampleQueries = ['binary tree', 'sushi & ramen', 'linear algebra', 'basketball game'];

  const handleInsert = () => {
    if (!addMeta.trim()) return;
    onInsertVector(addMeta.trim(), addCat);
    setAddMeta('');
  };

  const applyPreset = (text) => {
    setQueryText(text);
  };

  return (
    <div className={`left-panel ${!isOpen ? 'collapsed' : ''} ${isMobileActive ? 'mobile-active' : ''}`}>
      <div className="panel-card">
        <div className="sec">
          <span>Query Vector Space</span>
          <span className="sec-badge">16D</span>
        </div>
        <div style={{ display: 'flex', flexDirection: 'column', gap: '8px' }}>
          <input
            type="text"
            id="qInput"
            placeholder="Search concepts (e.g. binary tree)…"
            value={queryText}
            onChange={(e) => setQueryText(e.target.value)}
            onKeyDown={(e) => { if (e.key === 'Enter') onSearch(); }}
          />
          <div className="presets-grid">
            {sampleQueries.map((q) => (
              <button key={q} className="chip-btn" onClick={() => applyPreset(q)}>
                {q}
              </button>
            ))}
          </div>
          <button className="btn-p" onClick={onSearch}>
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5">
              <polygon points="13 2 3 14 12 14 11 22 21 10 12 10 13 2" />
            </svg>
            SEARCH VECTORS
          </button>
        </div>
      </div>

      <div className="panel-card">
        <div className="sec">Indexing Algorithm</div>
        <div className="algo-row">
          <div
            className={`algo-btn ${selAlgo === 'hnsw' ? 'on' : ''}`}
            onClick={() => setSelAlgo('hnsw')}
          >
            HNSW
          </div>
          <div
            className={`algo-btn ${selAlgo === 'kdtree' ? 'on' : ''}`}
            onClick={() => setSelAlgo('kdtree')}
          >
            KD-TREE
          </div>
          <div
            className={`algo-btn ${selAlgo === 'bruteforce' ? 'on' : ''}`}
            onClick={() => setSelAlgo('bruteforce')}
          >
            BRUTE
          </div>
        </div>
      </div>

      <div className="panel-card">
        <div className="sec">Distance Metric</div>
        <select id="metric" value={metric} onChange={(e) => setMetric(e.target.value)}>
          <option value="cosine">Cosine Similarity</option>
          <option value="euclidean">Euclidean Distance</option>
          <option value="manhattan">Manhattan Distance</option>
        </select>
      </div>

      <div className="panel-card">
        <div className="sec">
          <span>Top-K Nearest</span>
          <span className="sec-badge">{topK} hits</span>
        </div>
        <input
          type="range"
          id="kSlider"
          min="1"
          max="10"
          value={topK}
          onChange={(e) => setTopK(parseInt(e.target.value, 10))}
        />
      </div>

      <div className="panel-card">
        <div className="sec">Category Legend</div>
        <div className="legend">
          <div className="leg-row">
            <div className="dot" style={{ background: 'var(--cs)', boxShadow: '0 0 8px var(--cs)' }}></div>
            Computer Science / Algos
          </div>
          <div className="leg-row">
            <div className="dot" style={{ background: 'var(--math)', boxShadow: '0 0 8px var(--math)' }}></div>
            Mathematics &amp; Logic
          </div>
          <div className="leg-row">
            <div className="dot" style={{ background: 'var(--food)', boxShadow: '0 0 8px var(--food)' }}></div>
            Food &amp; Cuisine
          </div>
          <div className="leg-row">
            <div className="dot" style={{ background: 'var(--sports)', boxShadow: '0 0 8px var(--sports)' }}></div>
            Sports &amp; Fitness
          </div>
          <div className="leg-row">
            <div className="dot" style={{ background: 'var(--doc)', boxShadow: '0 0 8px var(--doc)' }}></div>
            RAG Documents
          </div>
        </div>
      </div>

      <div className="panel-card">
        <div className="sec">Insert Demo Vector</div>
        <div style={{ display: 'flex', flexDirection: 'column', gap: '8px' }}>
          <input
            type="text"
            id="addMeta"
            placeholder="Vector description…"
            value={addMeta}
            onChange={(e) => setAddMeta(e.target.value)}
          />
          <select id="addCat" value={addCat} onChange={(e) => setAddCat(e.target.value)}>
            <option value="cs">Computer Science</option>
            <option value="math">Mathematics</option>
            <option value="food">Food &amp; Cuisine</option>
            <option value="sports">Sports</option>
          </select>
          <button className="btn-s" onClick={handleInsert}>
            + INSERT TO DB
          </button>
        </div>
      </div>

      <div className="panel-card">
        <div className="sec">Performance Test</div>
        <button className="btn-s" onClick={onRunBenchmark}>
          ▶ COMPARE ALGOS
        </button>
      </div>
    </div>
  );
}
