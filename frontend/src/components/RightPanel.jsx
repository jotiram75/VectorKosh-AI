import React, { useState } from 'react';
import SearchTab from './tabs/SearchTab';
import DocsTab from './tabs/DocsTab';
import RagTab from './tabs/RagTab';

export default function RightPanel({
  latencyUs,
  selAlgo,
  metric,
  topK,
  searchResults,
  onDeleteVector,
  queryEmbedding,
  benchData,
  hnswInfo,
  ollamaStatus,
  docList,
  onInsertDoc,
  onDeleteDoc,
  insertLoading,
  insertStatusMsg,
  onAskAI,
  isThinking,
  chatData,
  onTabChange,
  isOpen,
  isMobileActive
}) {
  const [activeTab, setActiveTab] = useState('search');

  const switchTab = (tabName) => {
    setActiveTab(tabName);
    if (onTabChange) onTabChange(tabName);
  };

  return (
    <div className={`right-panel ${!isOpen ? 'collapsed' : ''} ${isMobileActive ? 'mobile-active' : ''}`}>
      <div className="tabs">
        <div
          className={`tab ${activeTab === 'search' ? 'on' : ''}`}
          onClick={() => switchTab('search')}
        >
          <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
            <circle cx="11" cy="11" r="8" />
            <line x1="21" y1="21" x2="16.65" y2="16.65" />
          </svg>
          SEARCH
        </div>
        <div
          className={`tab ${activeTab === 'docs' ? 'on' : ''}`}
          onClick={() => switchTab('docs')}
        >
          <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
            <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z" />
            <polyline points="14 2 14 8 20 8" />
          </svg>
          DOCUMENTS
        </div>
        <div
          className={`tab ${activeTab === 'rag' ? 'on' : ''}`}
          onClick={() => switchTab('rag')}
        >
          <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
            <rect x="3" y="11" width="18" height="10" rx="2" />
            <circle cx="12" cy="5" r="2" />
            <path d="M12 7v4" />
            <line x1="8" y1="16" x2="8" y2="16" />
            <line x1="16" y1="16" x2="16" y2="16" />
          </svg>
          ASK AI
        </div>
      </div>

      {activeTab === 'search' && (
        <SearchTab
          latencyUs={latencyUs}
          selAlgo={selAlgo}
          metric={metric}
          topK={topK}
          searchResults={searchResults}
          onDeleteVector={onDeleteVector}
          queryEmbedding={queryEmbedding}
          benchData={benchData}
          hnswInfo={hnswInfo}
        />
      )}

      {activeTab === 'docs' && (
        <DocsTab
          ollamaStatus={ollamaStatus}
          docList={docList}
          onInsertDoc={onInsertDoc}
          onDeleteDoc={onDeleteDoc}
          insertLoading={insertLoading}
          insertStatusMsg={insertStatusMsg}
        />
      )}

      {activeTab === 'rag' && (
        <RagTab
          onAskAI={onAskAI}
          isThinking={isThinking}
          chatData={chatData}
        />
      )}
    </div>
  );
}
