"use client";

import Link from "next/link";
import { useSearchParams } from "next/navigation";
import { useMemo, useState } from "react";
import { BankVisual } from "@/app/components/BankVisual";
import { Pagination } from "@/app/components/Pagination";
import { banks } from "@/app/data/mock";

const PAGE_SIZE = 6;

export default function BanksPage() {
  const searchParams = useSearchParams();
  const [query, setQuery] = useState(searchParams.get("query") ?? "");
  const [category, setCategory] = useState("全部");
  const [difficulty, setDifficulty] = useState("全部");
  const [sort, setSort] = useState("推荐");
  const [page, setPage] = useState(1);

  const filtered = useMemo(() => {
    const term = query.trim().toLowerCase();
    const next = banks.filter((bank) => {
      const matchesQuery =
        !term ||
        [bank.title, bank.description, bank.eyebrow].some((value) =>
          value.toLowerCase().includes(term),
        );
      return (
        matchesQuery &&
        (category === "全部" || bank.category === category) &&
        (difficulty === "全部" || bank.level === difficulty)
      );
    });

    return [...next].sort((a, b) => {
      if (sort === "题目最多") return b.questions - a.questions;
      if (sort === "进度优先") return b.progress - a.progress;
      return banks.indexOf(a) - banks.indexOf(b);
    });
  }, [category, difficulty, query, sort]);

  const totalPages = Math.ceil(filtered.length / PAGE_SIZE);
  const visible = filtered.slice((page - 1) * PAGE_SIZE, page * PAGE_SIZE);

  function resetFilters() {
    setQuery("");
    setCategory("全部");
    setDifficulty("全部");
    setSort("推荐");
    setPage(1);
  }

  return (
    <main className="page-shell app-shell" data-od-id="bank-list-page">
      <section className="page-hero compact">
        <div>
          <p className="section-kicker">探索题库</p>
          <h1>找到下一组值得练习的题。</h1>
          <p>从基础到进阶，按主题建立完整知识结构。</p>
        </div>
        <span className="hero-count"><b>{banks.length}</b> 个精选题库</span>
      </section>

      <section className="filter-bar" aria-label="题库筛选">
        <label className="filter-search">
          <span className="sr-only">搜索题库</span>
          <span className="search-glyph" aria-hidden="true" />
          <input
            value={query}
            onChange={(event) => {
              setQuery(event.target.value);
              setPage(1);
            }}
            placeholder="搜索题库、方向或知识点"
          />
        </label>
        <label>
          <span>分类</span>
          <select
            value={category}
            onChange={(event) => {
              setCategory(event.target.value);
              setPage(1);
            }}
          >
            {["全部", "前端", "算法", "架构", "后端", "数据库", "基础", "产品", "AI"].map((item) => (
              <option key={item}>{item}</option>
            ))}
          </select>
        </label>
        <label>
          <span>难度</span>
          <select
            value={difficulty}
            onChange={(event) => {
              setDifficulty(event.target.value);
              setPage(1);
            }}
          >
            {["全部", "入门", "中级", "进阶"].map((item) => <option key={item}>{item}</option>)}
          </select>
        </label>
        <label>
          <span>排序</span>
          <select value={sort} onChange={(event) => setSort(event.target.value)}>
            {["推荐", "题目最多", "进度优先"].map((item) => <option key={item}>{item}</option>)}
          </select>
        </label>
      </section>

      <div className="results-heading">
        <div>
          <p className="section-kicker">全部题库</p>
          <h2>{filtered.length} 个结果</h2>
        </div>
        <span>第 {filtered.length ? page : 0} / {totalPages || 0} 页</span>
      </div>

      {visible.length > 0 ? (
        <>
          <section className="bank-grid" data-od-id="bank-results-grid">
            {visible.map((bank) => (
              <article className="bank-grid-card" key={bank.id}>
                <Link className={`bank-visual ${bank.tone}`} href={`/banks/${bank.id}`} aria-label={`打开${bank.title}`}>
                  <BankVisual label={bank.eyebrow} />
                </Link>
                <div className="bank-meta">
                  <span>{bank.eyebrow}</span>
                  <span>{bank.level}</span>
                </div>
                <h2><Link href={`/banks/${bank.id}`}>{bank.title}</Link></h2>
                <p>{bank.description}</p>
                <div className="bank-grid-footer">
                  <span>{bank.creator} 创建</span>
                  <b>{bank.questions} 题</b>
                </div>
                <div className="progress-track" aria-label={`已完成 ${bank.progress}%`}>
                  <span style={{ width: `${bank.progress}%` }} />
                </div>
              </article>
            ))}
          </section>
          <Pagination current={page} total={totalPages} onChange={setPage} />
        </>
      ) : (
        <section className="state-panel empty" role="status" data-od-id="bank-empty-state">
          <span aria-hidden="true">梅</span>
          <h2>没有找到匹配的题库</h2>
          <p>换一个关键词，或清除筛选条件再试一次。</p>
          <button type="button" onClick={resetFilters}>清除筛选</button>
        </section>
      )}
    </main>
  );
}
