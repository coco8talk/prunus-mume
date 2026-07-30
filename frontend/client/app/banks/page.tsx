"use client";

import Link from "next/link";
import { useSearchParams } from "next/navigation";
import { useEffect, useState } from "react";
import { BankVisual } from "@/app/components/BankVisual";
import { Pagination } from "@/app/components/Pagination";
import { bankService } from "@/app/lib/services";
import { errorMessage } from "@/app/lib/api";
import type { QuestionBank } from "@/app/data/models";

const PAGE_SIZE = 6;

export default function BanksPage() {
  const searchParams = useSearchParams();
  const [input, setInput] = useState(searchParams.get("query") ?? "");
  const [query, setQuery] = useState(searchParams.get("query") ?? "");
  const [sort, setSort] = useState("推荐");
  const [page, setPage] = useState(1);
  const [banks, setBanks] = useState<QuestionBank[]>([]);
  const [total, setTotal] = useState(0);
  const [status, setStatus] = useState<"loading" | "ready" | "error">("loading");
  const [message, setMessage] = useState("");
  const [retry, setRetry] = useState(0);

  useEffect(() => {
    const controller = new AbortController();

    const sortRequest = sort === "最近更新"
      ? { sortField: "updateTime", sortOrder: "descend" as const }
      : sort === "最早创建"
        ? { sortField: "createTime", sortOrder: "ascend" as const }
        : {};

    bankService.search({
      current: page,
      pageSize: PAGE_SIZE,
      searchText: query.trim() || undefined,
      ...sortRequest,
    }, controller.signal).then((result) => {
      setBanks(result.records);
      setTotal(result.total);
      setStatus("ready");
    }).catch((error) => {
      if (controller.signal.aborted) return;
      setMessage(errorMessage(error));
      setStatus("error");
    });

    return () => controller.abort();
  }, [page, query, retry, sort]);

  const totalPages = Math.ceil(total / PAGE_SIZE);

  function submitSearch(event: React.FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setPage(1);
    setQuery(input);
  }

  function resetFilters() {
    setInput("");
    setQuery("");
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
        <span className="hero-count"><b>{total}</b> 个精选题库</span>
      </section>

      <section className="filter-bar" aria-label="题库筛选">
        <form className="filter-search" onSubmit={submitSearch}>
          <span className="sr-only">搜索题库</span>
          <span className="search-glyph" aria-hidden="true" />
          <input
            value={input}
            onChange={(event) => setInput(event.target.value)}
            placeholder="搜索题库、方向或知识点"
          />
        </form>
        <label>
          <span>排序</span>
          <select
            value={sort}
            onChange={(event) => {
              setSort(event.target.value);
              setPage(1);
            }}
          >
            {["推荐", "最近更新", "最早创建"].map((item) => <option key={item}>{item}</option>)}
          </select>
        </label>
      </section>

      <div className="results-heading">
        <div>
          <p className="section-kicker">全部题库</p>
          <h2>{total} 个结果</h2>
        </div>
        <span>第 {total ? page : 0} / {totalPages || 0} 页</span>
      </div>

      {status === "loading" ? (
        <div className="loading-list" role="status" aria-label="正在加载题库">
          {[1, 2, 3].map((item) => <div key={item}><span /><span /><span /></div>)}
        </div>
      ) : status === "error" ? (
        <section className="state-panel error" role="alert">
          <span aria-hidden="true">!</span>
          <h2>暂时无法获取题库</h2>
          <p>{message}</p>
          <button type="button" onClick={() => setRetry((value) => value + 1)}>重新加载</button>
        </section>
      ) : banks.length > 0 ? (
        <>
          <section className="bank-grid" data-od-id="bank-results-grid">
            {banks.map((bank) => (
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
