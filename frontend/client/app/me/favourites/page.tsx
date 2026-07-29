"use client";

import Link from "next/link";
import { useMemo, useState } from "react";
import { Pagination } from "@/app/components/Pagination";
import { PersonalShell } from "@/app/components/PersonalShell";
import { favouriteQuestionIds, getBank, questions } from "@/app/data/mock";

const PAGE_SIZE = 2;

export default function MyFavouritesPage() {
  const [favouriteIds, setFavouriteIds] = useState(favouriteQuestionIds);
  const [query, setQuery] = useState("");
  const [tag, setTag] = useState("全部");
  const [difficulty, setDifficulty] = useState("全部");
  const [page, setPage] = useState(1);
  const [notice, setNotice] = useState("");

  const favouriteQuestions = useMemo(
    () => questions.filter((question) => favouriteIds.includes(question.id)),
    [favouriteIds],
  );
  const availableTags = Array.from(new Set(favouriteQuestions.flatMap((question) => question.tags)));
  const filtered = favouriteQuestions.filter((question) => {
    const term = query.trim().toLowerCase();
    return (
      (!term || [question.title, ...question.tags].some((value) => value.toLowerCase().includes(term))) &&
      (tag === "全部" || question.tags.includes(tag)) &&
      (difficulty === "全部" || question.difficulty === difficulty)
    );
  });
  const totalPages = Math.ceil(filtered.length / PAGE_SIZE);
  const visible = filtered.slice((page - 1) * PAGE_SIZE, page * PAGE_SIZE);

  function removeFavourite(id: string, title: string) {
    setFavouriteIds((current) => current.filter((item) => item !== id));
    setNotice(`已从收藏中移除「${title}」`);
    setPage((current) => Math.min(current, Math.max(1, Math.ceil((filtered.length - 1) / PAGE_SIZE))));
  }

  function resetFilters() {
    setQuery("");
    setTag("全部");
    setDifficulty("全部");
    setPage(1);
  }

  return (
    <PersonalShell
      eyebrow="我的收藏"
      title="把重要的问题，留给下一次思考。"
      description="搜索、筛选并整理收藏过的题目，让复习更有方向。"
    >
      <section data-od-id="my-favourites-page">
        {notice && <div className="feedback success" role="status">{notice}</div>}
        <div className="personal-toolbar" aria-label="收藏筛选">
          <label className="filter-search">
            <span className="search-glyph" aria-hidden="true" />
            <span className="sr-only">搜索收藏</span>
            <input
              value={query}
              onChange={(event) => { setQuery(event.target.value); setPage(1); }}
              placeholder="搜索题目或标签"
            />
          </label>
          <label>
            <span>标签</span>
            <select value={tag} onChange={(event) => { setTag(event.target.value); setPage(1); }}>
              <option>全部</option>
              {availableTags.map((item) => <option key={item}>{item}</option>)}
            </select>
          </label>
          <label>
            <span>难度</span>
            <select
              value={difficulty}
              onChange={(event) => { setDifficulty(event.target.value); setPage(1); }}
            >
              {["全部", "入门", "中级", "进阶"].map((item) => <option key={item}>{item}</option>)}
            </select>
          </label>
        </div>

        <div className="personal-section-heading">
          <div><p className="section-kicker">收藏题目</p><h2>{filtered.length} 道题</h2></div>
          <span>共收藏 {favouriteQuestions.length} 道</span>
        </div>

        {visible.length > 0 ? (
          <>
            <div className="favourite-list">
              {visible.map((question) => {
                const bank = getBank(question.bankId);
                return (
                  <article className="favourite-card" key={question.id} data-od-id={`favourite-${question.id}`}>
                    <div>
                      <div className="tag-row">
                        <span className={`difficulty ${question.difficulty}`}>{question.difficulty}</span>
                        {question.tags.map((item) => <span key={item}>{item}</span>)}
                      </div>
                      <h3><Link href={`/questions/${question.id}`}>{question.title}</Link></h3>
                      <p>{bank?.title} · 收藏 {question.favourites}</p>
                    </div>
                    <button
                      className="secondary-button danger-text"
                      type="button"
                      onClick={() => removeFavourite(question.id, question.title)}
                      aria-label={`移除收藏：${question.title}`}
                    >
                      移除收藏
                    </button>
                  </article>
                );
              })}
            </div>
            <Pagination current={page} total={totalPages} onChange={setPage} />
          </>
        ) : (
          <div className="state-panel empty" role="status" data-od-id="favourites-empty-state">
            <span aria-hidden="true">收</span>
            <h2>{favouriteQuestions.length ? "没有匹配的收藏" : "收藏夹还是空的"}</h2>
            <p>{favouriteQuestions.length ? "换个关键词或清除筛选条件再试试。" : "在练习题目时收藏值得回看的问题。"}
            </p>
            {favouriteQuestions.length ? (
              <button type="button" onClick={resetFilters}>清除筛选</button>
            ) : (
              <Link href="/questions">去发现题目</Link>
            )}
          </div>
        )}
      </section>
    </PersonalShell>
  );
}
