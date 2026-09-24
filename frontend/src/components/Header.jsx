import React from 'react';

export default function Header({
  ollamaStatus,
  statsLabel,
  leftOpen,
  setLeftOpen,
  rightOpen,
  setRightOpen,
  mobileTab,
  setMobileTab
}) {
  let badgeClass = 'badge';
  let badgeText = 'OLLAMA…';

  if (ollamaStatus) {
    if (ollamaStatus.ollamaAvailable) {
      badgeClass = 'badge ok';
      badgeText = 'OLLAMA';
    } else {
      badgeClass = 'badge err';
      badgeText = 'OLLAMA';
    }
  }

  return (
    <>
      <header>
        <div className="brand-section">
          <button
            className={`icon-btn ${leftOpen ? 'active' : ''}`}
            onClick={() => setLeftOpen(!leftOpen)}
            title="Toggle Controls Panel"
          >
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
              <rect x="3" y="3" width="18" height="18" rx="2" />
              <path d="M9 3v18" />
            </svg>
          </button>

          <div className="brand-logo">V</div>
          <h1>VectorKosh AI</h1>
        </div>

        <div className="header-badges">
          <span className="badge hl">HNSW</span>
          <span className="badge">KD-TREE</span>
          <span className="badge">BRUTE FORCE</span>
          <span className={badgeClass} id="ollamaBadge">
            <span className="status-dot"></span>
            {badgeText}
          </span>
        </div>

        <div className="header-right">
          <span id="statsLabel">{statsLabel}</span>
          <button
            className={`icon-btn ${rightOpen ? 'active' : ''}`}
            onClick={() => setRightOpen(!rightOpen)}
            title="Toggle Search & RAG Insights"
          >
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
              <rect x="3" y="3" width="18" height="18" rx="2" />
              <path d="M15 3v18" />
            </svg>
          </button>
        </div>
      </header>

      <div className="mobile-nav-tabs">
        <button
          className={`mobile-nav-btn ${mobileTab === 'controls' ? 'active' : ''}`}
          onClick={() => setMobileTab('controls')}
        >
          <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
            <path d="M12 20h9" />
            <path d="M16.5 3.5a2.121 2.121 0 0 1 3 3L7 19l-4 1 1-4L16.5 3.5z" />
          </svg>
          Controls
        </button>
        <button
          className={`mobile-nav-btn ${mobileTab === 'map' ? 'active' : ''}`}
          onClick={() => setMobileTab('map')}
        >
          <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
            <circle cx="12" cy="12" r="10" />
            <path d="M12 2a14.5 14.5 0 0 0 0 20M2 12h20" />
          </svg>
          2D Map
        </button>
        <button
          className={`mobile-nav-btn ${mobileTab === 'insights' ? 'active' : ''}`}
          onClick={() => setMobileTab('insights')}
        >
          <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
            <polygon points="12 2 2 7 12 12 22 7 12 2" />
            <polyline points="2 17 12 22 22 17" />
            <polyline points="2 12 12 17 22 12" />
          </svg>
          Search &amp; RAG
        </button>
      </div>
    </>
  );
}
