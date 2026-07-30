"use client";

import Link from "next/link";
import { useParams } from "next/navigation";
import { useEffect, useState } from "react";
import { BankVisual } from "@/app/components/BankVisual";
import { Pagination } from "@/app/components/Pagination";
import { QuestionRow } from "@/app/components/QuestionRow";
import { errorMessage } from "@/app/lib/api";
import { bankService } from "@/app/lib/services";
import type { Question, QuestionBank } from "@/app/data/models";

const PAGE_SIZE = 10;

export default function BankDetailPage() {
  const params = useParams<{ bankId: string }>();
  const [bank, setBank] = useState<QuestionBank | null>(null);
  const [questions, setQuestions] = useState<Question[]>([]);
  const [page, setPage] = useState(1);
  const [total, setTotal] = useState(0);
  const [status, setStatus] = useState<"loading" | "ready" | "error">("loading");
  const [message, setMessage] = useState("");

  useEffect(() => {
    const controller = new AbortController();

    Promise.all([
      bankService.detail(params.bankId, controller.signal),
      bankService.questions(params.bankId, page, PAGE_SIZE, controller.signal),
    ]).then(([nextBank, questionPage]) => {
      setBank(nextBank);
      setQuestions(questionPage.records);
      setTotal(questionPage.total);
      setStatus("ready");
    }).catch((error) => {
      if (controller.signal.aborted) return;
      setMessage(errorMessage(error));
      setStatus("error");
    });

    return () => controller.abort();
  }, [page, params.bankId]);

  if (status === "loading") {
    return <main className="page-shell app-shell"><section className="state-panel" role="status"><h1>正在加载题库…</h1></section></main>;
  }

  if (status === "error" || !bank) {
    return (
      <main className="page-shell app-shell">
        <section className="state-panel error" role="alert">
          <span aria-hidden="true">？</span>
          <h1>暂时无法打开这个题库</h1>
          <p>{message}</p>
          <Link href="/banks">返回题库</Link>
        </section>
      </main>
    );
  }

  const totalPages = Math.ceil(total / PAGE_SIZE);
  const firstQuestion = questions[0];

  return (
    <main className="page-shell app-shell" data-od-id="bank-detail-page">
      <nav className="breadcrumbs" aria-label="面包屑">
        <Link href="/banks">题库</Link><span>/</span><span>{bank.title}</span>
      </nav>

      <section className="bank-detail-hero">
        <div className={`bank-visual large ${bank.tone}`} aria-hidden="true">
          <BankVisual label={bank.eyebrow} />
        </div>
        <div className="bank-detail-copy">
          <div className="tag-row"><span>{bank.category}</span></div>
          <h1>{bank.title}</h1>
          <p>{bank.description}</p>
          <div className="creator-line">
            <span className="mini-avatar">{bank.creator.slice(0, 1)}</span>
            <span>由 {bank.creatorId
              ? <Link href={`/users/${bank.creatorId}`}><b>{bank.creator}</b></Link>
              : <b>{bank.creator}</b>} 创建</span>
            <i />
            <span>{total} 道题</span>
          </div>
        </div>
        <aside className="bank-progress-card">
          <div><span>题目总数</span><b>{total}</b></div>
          <div className="progress-track"><span style={{ width: total ? "100%" : "0%" }} /></div>
          <Link href={firstQuestion ? `/questions/${firstQuestion.id}?bankId=${encodeURIComponent(bank.id)}` : "/questions"}>
            {firstQuestion ? "开始练习" : "浏览题目"} <span aria-hidden="true">→</span>
          </Link>
        </aside>
      </section>

      <section className="detail-section">
        <div className="results-heading">
          <div><p className="section-kicker">题目目录</p><h2>共 {total} 道精选题</h2></div>
          <span>第 {page} / {totalPages || 1} 页</span>
        </div>
        {questions.length > 0 ? (
          <>
            <div className="question-list">
              {questions.map((question) => (
                <QuestionRow key={question.id} question={question} bank={bank} />
              ))}
            </div>
            <Pagination current={page} total={totalPages} onChange={setPage} />
          </>
        ) : (
          <div className="state-panel empty">
            <span aria-hidden="true">题</span>
            <h2>题目正在整理中</h2>
            <p>先去全部题目看看其他值得练习的内容。</p>
            <Link href="/questions">浏览全部题目</Link>
          </div>
        )}
      </section>
    </main>
  );
}
