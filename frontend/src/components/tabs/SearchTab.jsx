import React, { useEffect, useRef } from 'react';

const COL = {
  cs: '#00f0ff',
  math: '#a855f7',
  food: '#f59e0b',
  sports: '#10b981',
  doc: '#34d399',
  default: '#94a3b8'
};

const DIM_COL = [
  '#00f0ff','#00f0ff','#00f0ff','#00f0ff',
  '#a855f7','#a855f7','#a855f7','#a855f7',
  '#f59e0b','#f59e0b','#f59e0b','#f59e0b',
  '#10b981','#10b981','#10b981','#10b981'
];

export default function SearchTab({
  latencyUs,
  selAlgo,
  metric,
  topK,
  searchResults,
  onDeleteVector,
  queryEmbedding,
  benchData,
  hnswInfo
}) {
  const vecCvsRef = useRef(null);

  useEffect(() => {
    if (!queryEmbedding || !vecCvsRef.current) return;
    const vc = vecCvsRef.current;
    const W = vc.parentElement.clientWidth || 300;
    vc.width = W;
    const vx = vc.getContext('2d');

    vx.clearRect(0, 0, W, 76);
    vx.fillStyle = '#060713';
    vx.fillRect(0, 0, W, 76);

    const DIMS = 16;
    const bw = (W - 4) / DIMS;

    for (let i = 0; i < DIMS; i++) {
      const val = queryEmbedding[i] || 0;
      const h = val * 58;
      const x = 2 + i * bw;
      const col = DIM_COL[i];
      vx.shadowColor = col;
      vx.shadowBlur = 6;
      vx.fillStyle = col + 'cc';
      vx.fillRect(x + 1, 63 - h, bw - 2, h);
    }

    vx.shadowBlur = 0;
    vx.font = '500 9px "Fira Code", monospace';
    vx.textAlign = 'center';
    [['CS', 0], ['MATH', 4], ['FOOD', 8], ['SPORT', 12]].forEach(([lbl, gi], i) => {
      vx.fillStyle = Object.values(COL)[i] + 'aa';
      vx.fillText(lbl, 2 + (gi + 1.5) * bw, 73);
    });
    vx.textAlign = 'left';
  }, [queryEmbedding]);

  const latencyDisplay = latencyUs === null
    ? '—'
    : (latencyUs < 1000 ? `${latencyUs} μs` : `${(latencyUs / 1000).toFixed(2)} ms`);

  return (
    <div className="tab-content on" id="tab-search">
      <div className="latency-card">
        <div>
          <div className="sec">Search Latency</div>
          <div className="lat-big" id="latBig">{latencyDisplay}</div>
          <div className="lat-sub" id="latSub">
            {latencyUs === null ? 'No query executed yet' : `${selAlgo.toUpperCase()} · ${metric} · k=${topK}`}
          </div>
        </div>
      </div>

      <div>
        <div className="sec">
          <span>Top Matches</span>
          {searchResults && searchResults.length > 0 && (
            <span className="sec-badge">{searchResults.length} hits</span>
          )}
        </div>
        <div className="results" id="results">
          {(!searchResults || searchResults.length === 0) ? (
            <div style={{ color: 'var(--text-sub)', fontSize: '12px', padding: '10px 0' }}>
              Run a vector search from the controls panel…
            </div>
          ) : (
            searchResults.map((r, i) => {
              const col = COL[r.category] || COL.default;
              return (
                <div key={r.id} className="rcard">
                  <div className="rrank">#{i + 1} NEAREST MATCH</div>
                  <div className="rmeta">{r.metadata}</div>
                  <div className="rfoot">
                    <span
                      className="rcat"
                      style={{ background: `${col}18`, color: col, border: `1px solid ${col}44` }}
                    >
                      {r.category.toUpperCase()}
                    </span>
                    <span className="rdist">dist: {r.distance.toFixed(5)}</span>
                    <button className="del" onClick={() => onDeleteVector(r.id)} title="Delete vector">✕</button>
                  </div>
                </div>
              );
            })
          )}
        </div>
      </div>

      <div>
        <div className="sec">Query Embedding Spectrum (16D)</div>
        <canvas id="vecCvs" ref={vecCvsRef} height="76"></canvas>
      </div>

      {benchData && (
        <div id="benchSec">
          <div className="sec">Algorithm Comparison</div>
          <div className="bench" id="benchBars">
            {(() => {
              const mx = Math.max(benchData.bruteforceUs, benchData.kdtreeUs, benchData.hnswUs, 1);
              const items = [
                { lbl: 'Brute Force', us: benchData.bruteforceUs, col: '#f43f5e' },
                { lbl: 'KD-Tree', us: benchData.kdtreeUs, col: '#00f0ff' },
                { lbl: 'HNSW Graph', us: benchData.hnswUs, col: '#a855f7' }
              ];
              return items.map(({ lbl, us, col }) => {
                const pct = Math.max((us / mx) * 100, 3);
                const disp = us < 1000 ? `${us} μs` : `${(us / 1000).toFixed(2)} ms`;
                return (
                  <div key={lbl} className="brow">
                    <div className="blabel">
                      <span style={{ color: col }}>{lbl}</span>
                      <span style={{ color: 'var(--text-sub)' }}>{disp}</span>
                    </div>
                    <div className="btrack">
                      <div className="bfill" style={{ width: `${pct}%`, background: col }}></div>
                    </div>
                  </div>
                );
              });
            })()}
          </div>
        </div>
      )}

      <div>
        <div className="sec">HNSW Graph Hierarchy</div>
        <div className="layers" id="layers">
          {!hnswInfo || !hnswInfo.nodesPerLayer ? (
            <div style={{ color: 'var(--text-sub)', fontSize: '12px' }}>Loading HNSW stats…</div>
          ) : (
            hnswInfo.nodesPerLayer.map((cnt, lyr) => {
              const maxN = hnswInfo.nodesPerLayer[0] || 1;
              const pct = Math.max((cnt / maxN) * 100, 4);
              const edg = hnswInfo.edgesPerLayer[lyr] || 0;
              return (
                <div key={lyr} className="lrow">
                  <div className="lnum">L{lyr}</div>
                  <div className="ltrack">
                    <div className="lfill" style={{ width: `${pct}%` }}></div>
                  </div>
                  <div className="lcount">{cnt} nodes · {edg} edges</div>
                </div>
              );
            })
          )}
        </div>
      </div>
    </div>
  );
}
