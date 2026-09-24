import React, { useState } from 'react';

export default function DocsTab({
  ollamaStatus,
  docList,
  onInsertDoc,
  onDeleteDoc,
  insertLoading,
  insertStatusMsg
}) {
  const [docTitle, setDocTitle] = useState('');
  const [docText, setDocText] = useState('');

  const handleInsert = async () => {
    if (!docTitle.trim() || !docText.trim()) return;
    const ok = await onInsertDoc(docTitle.trim(), docText.trim());
    if (ok) {
      setDocTitle('');
      setDocText('');
    }
  };

  return (
    <div className="tab-content on" id="tab-docs">
      <div>
        <div className="sec">Ollama Engine Status</div>
        {ollamaStatus && ollamaStatus.ollamaAvailable ? (
          <div className="ollama-status ok" id="ollamaStatus">
            <div style={{ color: 'var(--green)', fontWeight: 700, display: 'flex', alignItems: 'center', gap: '6px' }}>
              <span className="status-dot" style={{ background: 'var(--green)', boxShadow: '0 0 8px var(--green)' }}></span>
              Online &amp; Connected
            </div>
            <div style={{ marginTop: '6px', color: 'var(--text-sub)' }}>
              Embed: <span style={{ color: 'var(--cs)', fontFamily: 'Fira Code' }}>{ollamaStatus.embedModel}</span><br />
              Generate: <span style={{ color: 'var(--accent-light)', fontFamily: 'Fira Code' }}>{ollamaStatus.genModel}</span><br />
              Dimensions: <span style={{ color: 'var(--text)', fontFamily: 'Fira Code' }}>{ollamaStatus.docDims || '768D'}</span><br />
              Docs Index: <span style={{ color: 'var(--text)', fontWeight: 600 }}>{ollamaStatus.docCount} docs</span>
            </div>
          </div>
        ) : (
          <div className="ollama-status err" id="ollamaStatus">
            <div style={{ color: 'var(--red)', fontWeight: 700, display: 'flex', alignItems: 'center', gap: '6px' }}>
              <span className="status-dot" style={{ background: 'var(--red)' }}></span>
              Ollama Offline
            </div>
            <div style={{ marginTop: '6px', color: 'var(--text-sub)', fontSize: '11px', lineHeight: '1.6' }}>
              To activate Local RAG features:<br />
              1. Download from <span style={{ color: 'var(--cs)' }}>ollama.com</span><br />
              2. Run: <code style={{ color: 'var(--accent-light)' }}>ollama pull nomic-embed-text</code><br />
              3. Run: <code style={{ color: 'var(--accent-light)' }}>ollama pull llama3.2</code>
            </div>
          </div>
        )}
      </div>

      <div>
        <div className="sec">Embed &amp; Index Document</div>
        <div style={{ display: 'flex', flexDirection: 'column', gap: '10px' }}>
          <input
            type="text"
            id="docTitle"
            placeholder="Document Title (e.g. Data Structures Notes)"
            value={docTitle}
            onChange={(e) => setDocTitle(e.target.value)}
          />
          <textarea
            id="docText"
            placeholder="Paste your notes, article, textbook chapter, or code docs…&#10;&#10;Text is automatically chunked and converted into real embeddings via nomic-embed-text."
            value={docText}
            onChange={(e) => setDocText(e.target.value)}
          ></textarea>
          <button
            className="btn-g"
            id="insertDocBtn"
            disabled={insertLoading}
            onClick={handleInsert}
          >
            {insertLoading ? 'CHUNKING & EMBEDDING…' : '⚡ EMBED & INSERT'}
          </button>
          {insertStatusMsg && (
            <div id="insertStatus" style={{ fontSize: '11px', fontFamily: 'Fira Code', color: 'var(--text-sub)' }}>
              {insertStatusMsg}
            </div>
          )}
        </div>
      </div>

      <div>
        <div className="sec">
          <span>Stored Knowledge Base</span>
          <span className="sec-badge">{docList ? docList.length : 0} docs</span>
        </div>
        <div className="doc-list" id="docList">
          {(!docList || docList.length === 0) ? (
            <div style={{ color: 'var(--text-sub)', fontSize: '12px', padding: '10px 0' }}>
              No documents indexed yet. Paste text above to get started.
            </div>
          ) : (
            docList.map((d) => (
              <div key={d.id} className="dcard">
                <div className="dcard-title">{d.title}</div>
                <div className="dcard-preview">{d.preview}</div>
                <div className="dcard-foot">
                  <span className="dcard-words">{d.words} words</span>
                  <button className="del" onClick={() => onDeleteDoc(d.id)} title="Delete document">✕</button>
                </div>
              </div>
            ))
          )}
        </div>
      </div>
    </div>
  );
}
