"use client";

import Link from "next/link";
import { useEffect, useMemo, useState } from "react";
import { Pagination } from "@/app/components/Pagination";
import { PersonalShell } from "@/app/components/PersonalShell";
import { ProtectedContent, useAuth } from "@/app/components/AuthProvider";
import { errorMessage } from "@/app/lib/api";
import { favouriteService } from "@/app/lib/services";
import type { Question } from "@/app/data/models";

const PAGE_SIZE = 10;

export default function MyFavouritesPage() {
  const { user, status: authStatus } = useAuth();
  const [questions, setQuestions] = useState<Question[]>([]);
  const [serverTotal, setServerTotal] = useState(0);
  const [query, setQuery] = useState("");
  const [tag, setTag] = useState("全部");
  const [difficulty, setDifficulty] = useState("全部");
  const [page, setPage] = useState(1);
  const [status, setStatus] = useState<"loading" | "ready" | "error">("loading");
  const [notice, setNotice] = useState("");
  const [noticeType, setNoticeType] = useState<"success" | "error">("success");
  const [retry, setRetry] = useState(0);
  const [removingId, setRemovingId] = useState<string | null>(null);
  const [loadedUserId, setLoadedUserId] = useState<string | null>(null);

  useEffect(() => {
    if (authStatus !== "authenticated") return;
    const controller = new AbortController();
    favouriteService.mine(page, PAGE_SIZE, controller.signal).then((result) => {
      setQuestions(result.records);
      setServerTotal(result.total);
      setNotice("");
      setLoadedUserId(user?.id ?? null);
      setStatus("ready");
    }).catch((error) => {
      if (controller.signal.aborted) return;
      setNotice(errorMessage(error));
      setNoticeType("error");
      setLoadedUserId(user?.id ?? null);
      setStatus("error");
    });
    return () => controller.abort();
  }, [authStatus, page, retry, user?.id]);

  const availableTags = useMemo(
    () => Array.from(new Set(questions.flatMap((question) => question.tags))),
    [questions],
  );
  const filtered = questions.filter((question) => {
    const term = query.trim().toLowerCase();
    return (
      (!term || [question.title, ...question.tags].some((value) => value.toLowerCase().includes(term))) &&
      (tag === "全部" || question.tags.includes(tag)) &&
      (difficulty === "全部" || question.difficulty === difficulty)
    );
  });
  const totalPages = Math.ceil(serverTotal / PAGE_SIZE);

  async function removeFavourite(question: Question) {
    if (removingId) return;
    setRemovingId(question.id);
    setNotice("");
    const previous = questions;
    setQuestions((current) => current.filter((item) => item.id !== question.id));
    try {
      await favouriteService.remove(question.id);
      setServerTotal((current) => Math.max(0, current - 1));
      setNotice(`已从收藏中移除「${question.title}」`);
      setNoticeType("success");
      if (questions.length === 1 && page > 1) setPage((current) => current - 1);
    } catch (error) {
      setQuestions(previous);
      setNotice(errorMessage(error));
      setNoticeType("error");
    } finally {
      setRemovingId(null);
    }
  }

  function resetFilters() {
    setQuery("");
    setTag("全部");
    setDifficulty("全部");
  }

  return (
    <ProtectedContent>
      <PersonalShell
        eyebrow="我的收藏"
        title="把重要的问题，留给下一次思考。"
        description="搜索、筛选并整理收藏过的题目，让复习更有方向。"
      >
        <section data-od-id="my-favourites-page">
          {notice && (
            <div className={`feedback ${noticeType}`} role={noticeType === "error" ? "alert" : "status"}>
              {notice}
            </div>
          )}
          <div className="personal-toolbar" aria-label="收藏筛选">
            <label className="filter-search">
              <span className="search-glyph" aria-hidden="true" />
              <span className="sr-only">搜索当前页收藏</span>
              <input
                value={query}
                onChange={(event) => setQuery(event.target.value)}
                placeholder="搜索当前页题目或标签"
              />
            </label>
            <label>
              <span>标签</span>
              <select value={tag} onChange={(event) => setTag(event.target.value)}>
                <option>全部</option>
                {availableTags.map((item) => <option key={item}>{item}</option>)}
              </select>
            </label>
            <label>
              <span>难度</span>
              <select value={difficulty} onChange={(event) => setDifficulty(event.target.value)}>
                {["全部", "入门", "中级", "进阶"].map((item) => <option key={item}>{item}</option>)}
              </select>
            </label>
          </div>

          <div className="personal-section-heading">
            <div><p className="section-kicker">收藏题目</p><h2>{filtered.length} 道题</h2></div>
            <span>共收藏 {serverTotal} 道</span>
          </div>

          {status === "loading" || loadedUserId !== user?.id ? (
            <div className="loading-list" role="status" aria-label="正在加载收藏">
              {[1, 2].map((item) => <div key={item}><span /><span /><span /></div>)}
            </div>
          ) : status === "error" ? (
            <div className="state-panel error" role="alert">
              <h2>暂时无法获取收藏</h2>
              <button type="button" onClick={() => setRetry((value) => value + 1)}>重新加载</button>
            </div>
          ) : filtered.length > 0 ? (
            <>
              <div className="favourite-list">
                {filtered.map((question) => (
                  <article className="favourite-card" key={question.id} data-od-id={`favourite-${question.id}`}>
                    <div>
                      <div className="tag-row">
                        <span className={`difficulty ${question.difficulty}`}>{question.difficulty}</span>
                        {question.tags.map((item) => <span key={item}>{item}</span>)}
                      </div>
                      <h3><Link href={`/questions/${question.id}`}>{question.title}</Link></h3>
                      <p>收藏 {question.favourites}</p>
                    </div>
                    <button
                      className="secondary-button danger-text"
                      type="button"
                      disabled={removingId === question.id}
                      onClick={() => removeFavourite(question)}
                      aria-label={`移除收藏：${question.title}`}
                    >
                      {removingId === question.id ? "正在移除…" : "移除收藏"}
                    </button>
                  </article>
                ))}
              </div>
              <Pagination current={page} total={totalPages} onChange={setPage} />
            </>
          ) : (
            <div className="state-panel empty" role="status" data-od-id="favourites-empty-state">
              <span aria-hidden="true">收</span>
              <h2>{serverTotal ? "当前页没有匹配的收藏" : "收藏夹还是空的"}</h2>
              <p>{serverTotal ? "换个关键词或清除筛选条件再试试。" : "在练习题目时收藏值得回看的问题。"}</p>
              {serverTotal ? <button type="button" onClick={resetFilters}>清除筛选</button> : <Link href="/questions">去发现题目</Link>}
            </div>
          )}
        </section>
      </PersonalShell>
    </ProtectedContent>
  );
}
