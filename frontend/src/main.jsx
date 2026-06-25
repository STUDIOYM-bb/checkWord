import React, { useCallback, useEffect, useMemo, useState } from 'react';
import { createRoot } from 'react-dom/client';
import { Eraser, FileText, RefreshCw, Search, Sparkles } from 'lucide-react';
import './styles.css';

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL ?? 'http://localhost:8080';
const DEFAULT_TEXT = '사과와 바나나를 샀다. 사과는 달고, banana는 노랗다. Banana 123 123 테스트 테스트!';

/**
 * 백엔드 분석 API를 호출하고 반복 단어 결과를 반환합니다.
 */
async function analyzeText(text) {
  const response = await fetch(`${API_BASE_URL}/api/words/analyze`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify({ text }),
  });

  if (!response.ok) {
    throw new Error('분석 요청에 실패했습니다.');
  }

  return response.json();
}

/**
 * 반복 단어의 등장 위치를 원문 순서대로 정렬해 하이라이트 조각 생성에 사용합니다.
 */
function flattenOccurrences(repeatedWords) {
  return repeatedWords
    .flatMap((word) =>
      word.occurrences.map((occurrence) => ({
        ...occurrence,
        normalizedWord: word.normalizedWord,
        color: word.color,
        label: word.displayWord,
      })),
    )
    .sort((a, b) => a.startIndex - b.startIndex);
}

/**
 * 원문과 반복 단어 위치를 받아 일반 텍스트 조각과 하이라이트 조각으로 분리합니다.
 */
function buildHighlightedSegments(text, repeatedWords, selectedWord) {
  const occurrences = flattenOccurrences(repeatedWords);
  const segments = [];
  let cursor = 0;

  occurrences.forEach((occurrence, index) => {
    if (occurrence.startIndex > cursor) {
      segments.push({
        key: `plain-${cursor}-${index}`,
        text: text.slice(cursor, occurrence.startIndex),
        highlighted: false,
      });
    }

    const segmentText = text.slice(occurrence.startIndex, occurrence.endIndex);
    const isSelected = selectedWord === null || selectedWord === occurrence.normalizedWord;

    if (isSelected) {
      segments.push({
        key: `highlight-${occurrence.startIndex}-${occurrence.endIndex}-${index}`,
        text: segmentText,
        highlighted: true,
        normalizedWord: occurrence.normalizedWord,
        color: occurrence.color,
        label: occurrence.label,
      });
    } else {
      segments.push({
        key: `plain-highlight-${occurrence.startIndex}-${occurrence.endIndex}-${index}`,
        text: segmentText,
        highlighted: false,
      });
    }

    cursor = occurrence.endIndex;
  });

  if (cursor < text.length) {
    segments.push({
      key: `plain-${cursor}-end`,
      text: text.slice(cursor),
      highlighted: false,
    });
  }

  return segments.length > 0 ? segments : [{ key: 'empty', text, highlighted: false }];
}

/**
 * 단어 색상을 연한 배경색으로 변환해 긴 원문에서도 가독성을 유지합니다.
 */
function toSoftBackground(hexColor) {
  return `${hexColor}26`;
}

/**
 * CheckWord의 단일 화면 애플리케이션 컴포넌트입니다.
 */
function App() {
  const [text, setText] = useState(DEFAULT_TEXT);
  const [analysis, setAnalysis] = useState(null);
  const [isLoading, setIsLoading] = useState(false);
  const [errorMessage, setErrorMessage] = useState('');
  const [selectedWord, setSelectedWord] = useState(null);

  const segments = useMemo(
    () => buildHighlightedSegments(analysis?.text ?? text, analysis?.repeatedWords ?? [], selectedWord),
    [analysis, selectedWord, text],
  );

  const textStats = useMemo(() => {
    const chars = text.length;
    const lines = text.length === 0 ? 0 : text.split(/\r\n|\r|\n/).length;
    return { chars, lines };
  }, [text]);

  const requestAnalysis = useCallback(async (targetText) => {
    setIsLoading(true);
    setErrorMessage('');

    try {
      const result = await analyzeText(targetText);
      setAnalysis(result);
      setSelectedWord((currentWord) =>
        result.repeatedWords.some((word) => word.normalizedWord === currentWord) ? currentWord : null,
      );
    } catch (error) {
      setErrorMessage(error.message);
    } finally {
      setIsLoading(false);
    }
  }, []);

  const toggleSelectedWord = useCallback((normalizedWord) => {
    setSelectedWord((currentWord) => (currentWord === normalizedWord ? null : normalizedWord));
  }, []);

  useEffect(() => {
    const timer = window.setTimeout(() => {
      requestAnalysis(text);
    }, 450);

    return () => window.clearTimeout(timer);
  }, [text, requestAnalysis]);

  return (
    <main className="app-shell">
      <header className="app-header">
        <div>
          <p className="eyebrow">CheckWord</p>
          <h1>반복 단어 검사기</h1>
        </div>
        <button className="primary-button" type="button" onClick={() => requestAnalysis(text)} disabled={isLoading}>
          <Search size={18} aria-hidden="true" />
          <span>{isLoading ? '분석 중' : '분석'}</span>
        </button>
      </header>

      <section className="workspace" aria-label="반복 단어 분석 작업 영역">
        <div className="editor-panel">
          <div className="panel-toolbar">
            <div>
              <p className="toolbar-kicker">입력</p>
              <h2>원문</h2>
            </div>
            <button className="icon-button" type="button" onClick={() => setText('')} aria-label="입력 내용 지우기">
              <Eraser size={18} aria-hidden="true" />
            </button>
          </div>
          <textarea
            value={text}
            onChange={(event) => setText(event.target.value)}
            maxLength={100000}
            spellCheck="false"
            aria-label="분석할 글 입력"
            placeholder="반복 단어를 찾을 글을 입력하세요."
          />
          <div className="editor-meta">
            <span>{textStats.chars.toLocaleString()}자</span>
            <span>{textStats.lines.toLocaleString()}줄</span>
          </div>
        </div>

        <div className="result-panel">
          <div className="panel-toolbar">
            <div>
              <p className="toolbar-kicker">결과</p>
              <h2>하이라이트</h2>
            </div>
            <div className="status-pill" aria-live="polite">
              {isLoading ? <RefreshCw className="spin" size={16} aria-hidden="true" /> : <Sparkles size={16} aria-hidden="true" />}
              <span>{analysis?.repeatedWordTypeCount ?? 0}개</span>
            </div>
          </div>

          {errorMessage ? <p className="error-message">{errorMessage}</p> : null}

          <div className="highlight-view" aria-label="반복 단어 하이라이트 결과">
            {segments.map((segment) =>
              segment.highlighted ? (
                <button
                  key={segment.key}
                  className="highlight-token"
                  type="button"
                  title={segment.label}
                  onClick={() => toggleSelectedWord(segment.normalizedWord)}
                  style={{
                    borderColor: segment.color,
                    backgroundColor: toSoftBackground(segment.color),
                  }}
                >
                  {segment.text}
                </button>
              ) : (
                <React.Fragment key={segment.key}>{segment.text}</React.Fragment>
              ),
            )}
          </div>
        </div>
      </section>

      <section className="summary-band" aria-label="반복 단어 통계">
        <div className="summary-head">
          <FileText size={20} aria-hidden="true" />
          <div>
            <p className="toolbar-kicker">통계</p>
            <h2>반복 단어 목록</h2>
          </div>
        </div>

        <div className="metrics">
          <div>
            <span>분석 단어</span>
            <strong>{analysis?.totalWordCount ?? 0}</strong>
          </div>
          <div>
            <span>반복 단어</span>
            <strong>{analysis?.repeatedWordTypeCount ?? 0}</strong>
          </div>
        </div>

        <div className="word-list">
          {(analysis?.repeatedWords ?? []).length > 0 ? (
            analysis.repeatedWords.map((word) => (
              <button
                className={`word-item${selectedWord === word.normalizedWord ? ' is-selected' : ''}`}
                key={word.normalizedWord}
                type="button"
                onClick={() => toggleSelectedWord(word.normalizedWord)}
                aria-pressed={selectedWord === word.normalizedWord}
              >
                <span className="color-swatch" style={{ backgroundColor: word.color }} aria-hidden="true" />
                <div>
                  <h3>{word.displayWord}</h3>
                  <p>{word.normalizedWord}</p>
                </div>
                <strong>{word.count}회</strong>
              </button>
            ))
          ) : (
            <p className="empty-state">2회 이상 반복된 단어가 없습니다.</p>
          )}
        </div>
      </section>
    </main>
  );
}

createRoot(document.getElementById('root')).render(<App />);
