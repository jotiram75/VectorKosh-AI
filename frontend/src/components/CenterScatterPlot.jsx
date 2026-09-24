import React, { useEffect, useRef, useState } from 'react';

const COL = {
  cs: '#00f0ff',
  math: '#a855f7',
  food: '#f59e0b',
  sports: '#10b981',
  doc: '#34d399',
  default: '#94a3b8'
};

export default function CenterScatterPlot({ pcaPoints, hitIds, queryPt, bounds, isMobileHidden }) {
  const canvasRef = useRef(null);
  const containerRef = useRef(null);

  const [hoverItem, setHoverItem] = useState(null);
  const [tooltipPos, setTooltipPos] = useState({ x: 0, y: 0, show: false });
  const [zoomLevel, setZoomLevel] = useState(1);
  const [activeCategoryFilter, setActiveCategoryFilter] = useState('all');

  const pulseRef = useRef(0);

  // Convert world coordinates (PCA 2D) to Canvas pixel coordinates
  const w2c = (wx, wy, width, height) => {
    const P = 70;
    const baseRx = (bounds.maxX - bounds.minX) || 1;
    const baseRy = (bounds.maxY - bounds.minY) || 1;
    const rx = baseRx / zoomLevel;
    const ry = baseRy / zoomLevel;

    const midX = (bounds.minX + bounds.maxX) / 2;
    const midY = (bounds.minY + bounds.maxY) / 2;

    const minX = midX - rx / 2;
    const minY = midY - ry / 2;

    return [
      P + ((wx - minX) / rx) * (width - 2 * P),
      height - P - ((wy - minY) / ry) * (height - 2 * P)
    ];
  };

  useEffect(() => {
    const canvas = canvasRef.current;
    const container = containerRef.current;
    if (!canvas || !container) return;

    const ctx = canvas.getContext('2d');
    let animId;

    const resize = () => {
      const rect = container.getBoundingClientRect();
      const dpr = window.devicePixelRatio || 1;
      canvas.width = rect.width * dpr;
      canvas.height = rect.height * dpr;
      canvas.style.width = `${rect.width}px`;
      canvas.style.height = `${rect.height}px`;
    };

    const resizeObserver = new ResizeObserver(() => {
      resize();
    });
    resizeObserver.observe(container);
    resize();

    const drawFrame = () => {
      const dpr = window.devicePixelRatio || 1;
      const W = canvas.width / dpr;
      const H = canvas.height / dpr;

      ctx.save();
      ctx.scale(dpr, dpr);
      ctx.clearRect(0, 0, W, H);

      // Grid background pattern
      ctx.strokeStyle = 'rgba(255, 255, 255, 0.04)';
      ctx.lineWidth = 1;
      const stepX = (W - 140) / 8;
      const stepY = (H - 140) / 8;
      for (let i = 0; i <= 8; i++) {
        const tx = 70 + i * stepX;
        const ty = 70 + i * stepY;
        ctx.beginPath(); ctx.moveTo(tx, 70); ctx.lineTo(tx, H - 70); ctx.stroke();
        ctx.beginPath(); ctx.moveTo(70, ty); ctx.lineTo(W - 70, ty); ctx.stroke();
      }

      // Axis labels
      ctx.fillStyle = '#64748b';
      ctx.font = '500 11px "Fira Code", monospace';
      ctx.fillText('PC₁ (Principal Component 1) →', W / 2 - 80, H - 24);

      ctx.save();
      ctx.translate(24, H / 2 + 70);
      ctx.rotate(-Math.PI / 2);
      ctx.fillText('PC₂ (Principal Component 2) →', 0, 0);
      ctx.restore();

      ctx.fillStyle = '#94a3b8';
      ctx.font = '600 13px "Plus Jakarta Sans", sans-serif';
      ctx.fillText('2D PCA Projection · Semantic Vector Space', 80, 32);

      // Filtered points
      const visiblePoints = pcaPoints.filter(p => {
        if (activeCategoryFilter === 'all') return true;
        return p.item.category === activeCategoryFilter;
      });

      // Connecting lines from Query Star to Top-K hits
      if (queryPt && hitIds.size > 0) {
        const [qx, qy] = w2c(queryPt.x, queryPt.y, W, H);
        for (const pt of visiblePoints) {
          if (!hitIds.has(pt.item.id)) continue;
          const [px, py] = w2c(pt.x, pt.y, W, H);
          ctx.strokeStyle = 'rgba(99, 102, 241, 0.4)';
          ctx.lineWidth = 1.5;
          ctx.setLineDash([4, 4]);
          ctx.beginPath();
          ctx.moveTo(qx, qy);
          ctx.lineTo(px, py);
          ctx.stroke();
          ctx.setLineDash([]);
        }
      }

      // Render vector nodes
      for (const pt of visiblePoints) {
        const [cx, cy] = w2c(pt.x, pt.y, W, H);
        const col = COL[pt.item.category] || COL.default;
        const isHit = hitIds.has(pt.item.id);
        const r = isHit ? 10 : 7;

        // Pulse ring around hits
        if (isHit) {
          const pr = r + 8 + Math.sin(pulseRef.current) * 4;
          ctx.beginPath();
          ctx.arc(cx, cy, pr, 0, 2 * Math.PI);
          ctx.strokeStyle = col + '77';
          ctx.lineWidth = 2;
          ctx.stroke();
        }

        // Radial outer glow
        const grd = ctx.createRadialGradient(cx, cy, 0, cx, cy, r * 3);
        grd.addColorStop(0, col + (isHit ? 'dd' : '77'));
        grd.addColorStop(1, 'transparent');

        ctx.beginPath();
        ctx.arc(cx, cy, r * 3, 0, 2 * Math.PI);
        ctx.fillStyle = grd;
        ctx.fill();

        // Core dot
        ctx.beginPath();
        ctx.arc(cx, cy, r, 0, 2 * Math.PI);
        ctx.fillStyle = col;
        ctx.fill();

        // Hover highlight ring
        if (hoverItem && hoverItem.id === pt.item.id) {
          ctx.beginPath();
          ctx.arc(cx, cy, r + 5, 0, 2 * Math.PI);
          ctx.strokeStyle = '#ffffff';
          ctx.lineWidth = 2;
          ctx.stroke();
        }
      }

      // Render query star
      if (queryPt) {
        const [qx, qy] = w2c(queryPt.x, queryPt.y, W, H);
        ctx.save();
        ctx.translate(qx, qy);
        ctx.shadowColor = '#00f0ff';
        ctx.shadowBlur = 20;
        ctx.beginPath();
        for (let i = 0; i < 10; i++) {
          const a = (i * Math.PI / 5) - Math.PI / 2;
          const rr = i % 2 === 0 ? 14 : 6;
          if (i === 0) ctx.moveTo(Math.cos(a) * rr, Math.sin(a) * rr);
          else ctx.lineTo(Math.cos(a) * rr, Math.sin(a) * rr);
        }
        ctx.closePath();
        ctx.fillStyle = '#ffffff';
        ctx.fill();
        ctx.shadowBlur = 0;
        ctx.restore();

        ctx.fillStyle = '#00f0ff';
        ctx.font = '600 11px "Fira Code", monospace';
        ctx.fillText('QUERY', qx + 18, qy + 4);
      }

      if (!pcaPoints.length) {
        ctx.fillStyle = '#64748b';
        ctx.font = '600 14px "Plus Jakarta Sans", sans-serif';
        ctx.textAlign = 'center';
        ctx.fillText('Connecting to VectorDB Engine…', W / 2, H / 2);
        ctx.textAlign = 'left';
      }

      pulseRef.current += 0.05;
      ctx.restore();
      animId = requestAnimationFrame(drawFrame);
    };

    drawFrame();

    return () => {
      resizeObserver.disconnect();
      cancelAnimationFrame(animId);
    };
  }, [pcaPoints, hitIds, queryPt, bounds, hoverItem, zoomLevel, activeCategoryFilter]);

  const handleMouseMove = (e) => {
    const canvas = canvasRef.current;
    if (!canvas) return;
    const rect = canvas.getBoundingClientRect();
    const mx = e.clientX - rect.left;
    const my = e.clientY - rect.top;

    let best = 20;
    let found = null;
    for (const pt of pcaPoints) {
      if (activeCategoryFilter !== 'all' && pt.item.category !== activeCategoryFilter) continue;
      const [cx, cy] = w2c(pt.x, pt.y, rect.width, rect.height);
      const d = Math.hypot(mx - cx, my - cy);
      if (d < best) {
        best = d;
        found = pt.item;
      }
    }

    setHoverItem(found);
    if (found) {
      setTooltipPos({ x: e.clientX + 16, y: e.clientY - 10, show: true });
    } else {
      setTooltipPos((prev) => ({ ...prev, show: false }));
    }
  };

  const handleMouseLeave = () => {
    setHoverItem(null);
    setTooltipPos((prev) => ({ ...prev, show: false }));
  };

  return (
    <div
      ref={containerRef}
      className={`center-panel ${isMobileHidden ? 'mobile-hidden' : ''}`}
    >
      <div className="canvas-toolbar">
        <button
          className="canvas-tool-btn"
          onClick={() => setZoomLevel((z) => Math.min(z * 1.3, 4))}
          title="Zoom In"
        >
          +
        </button>
        <button
          className="canvas-tool-btn"
          onClick={() => setZoomLevel((z) => Math.max(z / 1.3, 0.5))}
          title="Zoom Out"
        >
          −
        </button>
        <button
          className="canvas-tool-btn"
          onClick={() => setZoomLevel(1)}
          title="Reset Zoom"
        >
          ⟲
        </button>
      </div>

      <div className="canvas-legend-bar">
        {[
          { id: 'all', label: 'All', color: '#f1f5f9' },
          { id: 'cs', label: 'CS', color: COL.cs },
          { id: 'math', label: 'Math', color: COL.math },
          { id: 'food', label: 'Food', color: COL.food },
          { id: 'sports', label: 'Sports', color: COL.sports },
          { id: 'doc', label: 'Docs', color: COL.doc }
        ].map((cat) => (
          <div
            key={cat.id}
            className={`legend-pill ${activeCategoryFilter === cat.id ? 'active' : ''}`}
            onClick={() => setActiveCategoryFilter(cat.id)}
          >
            <span className="dot" style={{ background: cat.color }}></span>
            {cat.label}
          </div>
        ))}
      </div>

      <canvas
        id="scatter"
        ref={canvasRef}
        onMouseMove={handleMouseMove}
        onMouseLeave={handleMouseLeave}
      />

      {tooltipPos.show && hoverItem && (
        <div
          id="tip"
          style={{
            display: 'block',
            left: tooltipPos.x,
            top: tooltipPos.y
          }}
        >
          <div style={{ color: COL[hoverItem.category] || COL.default, fontWeight: 700, fontSize: '11px' }}>
            [{hoverItem.category.toUpperCase()}]
          </div>
          <div style={{ marginTop: '2px', fontWeight: 500 }}>
            {hoverItem.metadata}
          </div>
        </div>
      )}
    </div>
  );
}
