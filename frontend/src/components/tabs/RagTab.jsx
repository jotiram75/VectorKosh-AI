import React, { useState, useRef, useEffect } from 'react';

export default function RagTab({ onAskAI, isThinking, chatData }) {
  const [question, setQuestion] = useState('');
  const [ragK, setRagK] = useState(3);
  const [expandedCtx, setExpandedCtx] = useState({});
  const [typewriterText, setTypewriterText] = useState('');
  const [isTyping, setIsTyping] = useState(false);
  const [copied, setCopied] = useState(false);

  const chatHistoryRef = useRef(null);

  const presets = [
    'What is dynamic programming?',
    'Explain how HNSW vector search works.',
    'Difference between Cosine & Euclidean metric'
  ];

  useEffect(() => {
    if (chatData && chatData.answer) {
      setIsTyping(true);
      setTypewriterText('');
      const full = chatData.answer;
      let i = 0;

      const timer = setInterval(() => {
        if (i >= full.length) {
          clearInterval(timer);
          setIsTyping(false);
          setTypewriterText(full);
          return;
        }
        i += 3;
        setTypewriterText(full.slice(0, i));
        if (chatHistoryRef.current) {
          chatHistoryRef.current.scrollTop = chatHistoryRef.current.scrollHeight;
        }
      }, 16);

      return () => clearInterval(timer);
    }
  }, [chatData]);

  const handleAsk = () => {
    if (!question.trim()) return;
    onAskAI(question.trim(), ragK);
  };

  const toggleCtx = (index) => {
    setExpandedCtx((prev) => ({ ...prev, [index]: !prev[index] }));
  };

  const copyToClipboard = (text) => {
    navigator.clipboard.writeText(text);
    setCopied(true);
    setTimeout(() => setCopied(false), 2000);
  };

  return (
    <div className="tab-content on" id="tab-rag">
      <div>
        <div className="sec">Ask Local RAG AI</div>
        <div style={{ display: 'flex', flexDirection: 'column', gap: '10px' }}>
          <textarea
            id="ragQuestion"
            rows="3"
            placeholder="Ask a question about your indexed documents (Ctrl+Enter to send)…"
            value={question}
            onChange={(e) => setQuestion(e.target.value)}
            onKeyDown={(e) => {
              if (e.key === 'Enter' && e.ctrlKey) handleAsk();
            }}
          ></textarea>

          <div className="presets-grid">
            {presets.map((p) => (
              <button key={p} className="chip-btn" onClick={() => setQuestion(p)}>
                {p}
              </button>
            ))}
          </div>

          <div style={{ display: 'flex', gap: '8px' }}>
            <select
              id="ragK"
              style={{ width: 'auto', flexShrink: 0 }}
              value={ragK}
              onChange={(e) => setRagK(parseInt(e.target.value, 10))}
            >
              <option value="2">Top 2 Chunks</option>
              <option value="3">Top 3 Chunks</option>
              <option value="5">Top 5 Chunks</option>
            </select>

            <button
              className="btn-g"
              id="askBtn"
              disabled={isThinking}
              onClick={handleAsk}
              style={{ flex: 1 }}
            >
              {isThinking ? 'THINKING…' : '🤖 ASK LOCAL LLM'}
            </button>
          </div>
        </div>
      </div>

      <div>
        <div className="sec">Conversation</div>
        <div className="chat-history" id="chatHistory" ref={chatHistoryRef}>
          {!chatData && !isThinking ? (
            <div style={{ color: 'var(--text-sub)', fontSize: '12px', padding: '10px 0' }}>
              Ask questions to search your indexed documents and synthesize responses locally.
            </div>
          ) : (
            <>
              {chatData && chatData.question && (
                <div className="chat-q">{chatData.question}</div>
              )}

              {isThinking && (
                <div className="thinking">
                  <div className="spinner"></div>
                  <span>Retrieving vector contexts &amp; generating answer with Ollama…</span>
                </div>
              )}

              {chatData && !isThinking && (
                <div className="chat-a">
                  {chatData.error ? (
                    <>
                      <div className="chat-a-header">
                        <div className="chat-a-label" style={{ color: 'var(--red)' }}>ERROR</div>
                      </div>
                      <div className="chat-a-text" style={{ color: 'var(--red)' }}>
                        {chatData.error}
                      </div>
                    </>
                  ) : (
                    <>
                      <div className="chat-a-header">
                        <div className="chat-a-label">🤖 {chatData.model || 'llm'}</div>
                        <button
                          className="copy-btn"
                          onClick={() => copyToClipboard(chatData.answer)}
                        >
                          {copied ? '✓ Copied' : 'Copy'}
                        </button>
                      </div>

                      <div className={`chat-a-text ${isTyping ? 'typing' : ''}`} id="typeTarget">
                        {typewriterText}
                      </div>

                      {chatData.contexts && chatData.contexts.length > 0 && (
                        <div className="chat-ctx">
                          <div className="chat-ctx-label">
                            RETRIEVED KNOWLEDGE CONTEXT ({chatData.contexts.length} chunks)
                          </div>
                          {chatData.contexts.map((c, i) => (
                            <React.Fragment key={i}>
                              <span className="ctx-chip" onClick={() => toggleCtx(i)}>
                                #{i + 1} {c.title} · {c.distance.toFixed(3)}
                              </span>
                              <div
                                className={`ctx-expand ${expandedCtx[i] ? 'show' : ''}`}
                                id={`ctx-${i}`}
                              >
                                {c.text}
                              </div>
                            </React.Fragment>
                          ))}
                        </div>
                      )}
                    </>
                  )}
                </div>
              )}
            </>
          )}
        </div>
      </div>
    </div>
  );
}
