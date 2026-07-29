"use client";

import Link from "next/link";
import { useParams } from "next/navigation";
import { useState } from "react";
import { BankVisual } from "@/app/components/BankVisual";
import { Pagination } from "@/app/components/Pagination";
import { QuestionRow } from "@/app/components/QuestionRow";
import { getBank, getQuestionsForBank } from "@/app/data/mock";

const PAGE_SIZE = 2;

export default function BankDetailPage() {
  const params = useParams<{ bankId: string }>();
  const bank = getBank(params.bankId);
  const [page, setPage] = useState(1);

  if (!bank) {
    return (
      <main className="page-shell app-shell">
        <section className="state-panel empty">
          <span aria-hidden="true">？</span>
          <h1>这个题库暂时不存在</h1>
          <p>它可能已被移动，返回题库列表继续探索吧。</p>
          <Link href="/banks">返回题库</Link>
        </section>
      </main>
    );
  }

  const bankQuestions = getQuestionsForBank(bank.id);
  const totalPages = Math.max(1, Math.ceil(bankQuestions.length / PAGE_SIZE));
  const visible = bankQuestions.slice((page - 1) * PAGE_SIZE, page * PAGE_SIZE);

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
          <div className="tag-row">
            <span>{bank.category}</span>
            <span className={`difficulty ${bank.level}`}>{bank.level}</span>
          </div>
          <h1>{bank.title}</h1>
          <p>{bank.description}</p>
          <div className="creator-line">
            <span className="mini-avatar">{bank.creator.slice(0, 1)}</span>
            <span>由 <b>{bank.creator}</b> 创建</span>
            <i />
            <span>{bank.questions} 道题</span>
          </div>
        </div>
        <aside className="bank-progress-card">
          <div>
            <span>学习进度</span>
            <b>{bank.progress}%</b>
          </div>
          <div className="progress-track"><span style={{ width: `${bank.progress}%` }} /></div>
          <Link href={bankQuestions[0] ? `/questions/${bankQuestions[0].id}` : "/questions"}>
            {bank.progress > 0 ? "继续练习" : "开始练习"} <span aria-hidden="true">→</span>
          </Link>
        </aside>
      </section>

      <section className="detail-section">
        <div className="results-heading">
          <div>
            <p className="section-kicker">题目目录</p>
            <h2>共 {bankQuestions.length || bank.questions} 道精选题</h2>
          </div>
          <span>当前展示示例题目</span>
        </div>
        {visible.length > 0 ? (
          <>
            <div className="question-list">
              {visible.map((question) => <QuestionRow key={question.id} question={question} />)}
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
