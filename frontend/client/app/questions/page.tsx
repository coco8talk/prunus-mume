"use client";

import { FormEvent, useMemo, useState } from "react";
import { Pagination } from "@/app/components/Pagination";
import { QuestionRow } from "@/app/components/QuestionRow";
import { banks, questions } from "@/app/data/mock";

const PAGE_SIZE = 5;

type FilterControlsProps = {
  difficulty: string;
  bankId: string;
  tag: string;
  onDifficulty: (value: string) => void;
  onBank: (value: string) => void;
  onTag: (value: string) => void;
  onReset: () => void;
};

function FilterControls({
  difficulty,
  bankId,
  tag,
  onDifficulty,
  onBank,
  onTag,
  onReset,
}: FilterControlsProps) {
  return (
    <div className="filter-controls">
      <label>
        <span>难度</span>
        <select value={difficulty} onChange={(event) => onDifficulty(event.target.value)}>
          {["全部", "入门", "中级", "进阶"].map((item) => <option key={item}>{item}</option>)}
        </select>
      </label>
      <label>
        <span>题库</span>
        <select value={bankId} onChange={(event) => onBank(event.target.value)}>
          <option value="全部">全部题库</option>
          {banks.map((bank) => <option value={bank.id} key={bank.id}>{bank.title}</option>)}
        </select>
      </label>
      <div>
        <span className="filter-label">热门标签</span>
        <div className="filter-chips">
          {["全部", "JavaScript", "React", "动态规划", "系统设计", "Java", "MySQL", "RAG"].map((item) => (
            <button
              className={tag === item ? "active" : ""}
              type="button"
              key={item}
              onClick={() => onTag(item)}
            >
              {item}
            </button>
          ))}
        </div>
      </div>
      <button className="reset-filters" type="button" onClick={onReset}>清除全部筛选</button>
    </div>
  );
}

export default function QuestionsPage() {
  const [input, setInput] = useState("");
  const [query, setQuery] = useState("");
  const [difficulty, setDifficulty] = useState("全部");
  const [bankId, setBankId] = useState("全部");
  const [tag, setTag] = useState("全部");
  const [page, setPage] = useState(1);
  const [status, setStatus] = useState<"ready" | "loading" | "error">("ready");
  const [drawerOpen, setDrawerOpen] = useState(false);

  const filtered = useMemo(() => {
    const term = query.trim().toLowerCase();
    return questions.filter((question) => {
      const matchesQuery =
        !term ||
        [question.title, question.content, ...question.tags].some((value) =>
          value.toLowerCase().includes(term),
        );
      return (
        matchesQuery &&
        (difficulty === "全部" || question.difficulty === difficulty) &&
        (bankId === "全部" || question.bankId === bankId) &&
        (tag === "全部" || question.tags.includes(tag))
      );
    });
  }, [bankId, difficulty, query, tag]);

  const totalPages = Math.ceil(filtered.length / PAGE_SIZE);
  const visible = filtered.slice((page - 1) * PAGE_SIZE, page * PAGE_SIZE);

  function updateFilter(setter: (value: string) => void, value: string) {
    setter(value);
    setPage(1);
  }

  function handleSearch(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setStatus("loading");
    setPage(1);
    window.setTimeout(() => {
      if (input.trim().includes("错误")) {
        setStatus("error");
      } else {
        setQuery(input);
        setStatus("ready");
      }
    }, 550);
  }

  function resetFilters() {
    setInput("");
    setQuery("");
    setDifficulty("全部");
    setBankId("全部");
    setTag("全部");
    setPage(1);
    setStatus("ready");
  }

  const controls = {
    difficulty,
    bankId,
    tag,
    onDifficulty: (value: string) => updateFilter(setDifficulty, value),
    onBank: (value: string) => updateFilter(setBankId, value),
    onTag: (value: string) => updateFilter(setTag, value),
    onReset: resetFilters,
  };

  return (
    <main className="page-shell app-shell" data-od-id="question-search-page">
      <section className="page-hero question-search-hero">
        <div>
          <p className="section-kicker">全部题目</p>
          <h1>把模糊的理解，变成清晰的答案。</h1>
          <p>从一道好问题开始，检验真正掌握的边界。</p>
        </div>
        <form className="wide-search" onSubmit={handleSearch} role="search">
          <span className="search-glyph" aria-hidden="true" />
          <label className="sr-only" htmlFor="question-keyword">搜索题目</label>
          <input
            id="question-keyword"
            value={input}
            onChange={(event) => setInput(event.target.value)}
            placeholder="搜索题目、标签或知识点"
          />
          <button type="submit">搜索</button>
        </form>
      </section>

      <div className="question-browser">
        <aside className="desktop-filters" aria-label="题目筛选">
          <div className="filter-title">
            <b>筛选题目</b>
            <span>{filtered.length} 个结果</span>
          </div>
          <FilterControls {...controls} />
        </aside>

        <section className="question-results">
          <div className="results-heading">
            <div>
              <p className="section-kicker">搜索结果</p>
              <h2>{status === "ready" ? `${filtered.length} 道题目` : "正在更新"}</h2>
            </div>
            <button className="mobile-filter-button" type="button" onClick={() => setDrawerOpen(true)}>
              筛选 <span aria-hidden="true">☰</span>
            </button>
          </div>

          {status === "loading" ? (
            <div className="loading-list" role="status" aria-label="正在加载题目">
              {[1, 2, 3].map((item) => <div key={item}><span /><span /><span /></div>)}
            </div>
          ) : status === "error" ? (
            <div className="state-panel error" role="alert" data-od-id="question-error-state">
              <span aria-hidden="true">!</span>
              <h2>暂时无法获取题目</h2>
              <p>网络好像走神了，请稍后再试。</p>
              <button type="button" onClick={() => setStatus("ready")}>重新加载</button>
            </div>
          ) : visible.length === 0 ? (
            <div className="state-panel empty" role="status" data-od-id="question-empty-state">
              <span aria-hidden="true">题</span>
              <h2>没有找到匹配的题目</h2>
              <p>试试更短的关键词，或清除部分筛选条件。</p>
              <button type="button" onClick={resetFilters}>清除筛选</button>
            </div>
          ) : (
            <>
              <div className="question-list">
                {visible.map((question) => (
                  <QuestionRow question={question} key={question.id} showBank />
                ))}
              </div>
              <Pagination current={page} total={totalPages} onChange={setPage} />
            </>
          )}
        </section>
      </div>

      {drawerOpen && (
        <div className="filter-drawer-backdrop" role="presentation" onClick={() => setDrawerOpen(false)}>
          <aside
            className="filter-drawer"
            role="dialog"
            aria-modal="true"
            aria-label="筛选题目"
            onClick={(event) => event.stopPropagation()}
          >
            <div className="drawer-heading">
              <h2>筛选题目</h2>
              <button type="button" onClick={() => setDrawerOpen(false)} aria-label="关闭筛选">×</button>
            </div>
            <FilterControls {...controls} />
            <button className="form-primary" type="button" onClick={() => setDrawerOpen(false)}>
              查看 {filtered.length} 个结果
            </button>
          </aside>
        </div>
      )}
    </main>
  );
}
