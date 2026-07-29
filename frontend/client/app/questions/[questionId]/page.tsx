"use client";

import Link from "next/link";
import { useParams } from "next/navigation";
import { useState } from "react";
import { formatCount, getBank, getQuestion, getQuestionsForBank } from "@/app/data/mock";

export default function QuestionDetailPage() {
  const params = useParams<{ questionId: string }>();
  const question = getQuestion(params.questionId);
  const [answerVisible, setAnswerVisible] = useState(false);
  const [liked, setLiked] = useState(false);
  const [favourited, setFavourited] = useState(false);

  if (!question) {
    return (
      <main className="page-shell app-shell">
        <section className="state-panel empty">
          <span aria-hidden="true">？</span>
          <h1>这道题暂时不存在</h1>
          <p>返回全部题目，继续寻找下一道练习。</p>
          <Link href="/questions">浏览全部题目</Link>
        </section>
      </main>
    );
  }

  const bank = getBank(question.bankId);
  const bankQuestions = getQuestionsForBank(question.bankId);
  const currentIndex = bankQuestions.findIndex((item) => item.id === question.id);
  const previous = bankQuestions[currentIndex - 1];
  const next = bankQuestions[currentIndex + 1];

  return (
    <main className="practice-shell app-shell" data-od-id="question-practice-page">
      <nav className="breadcrumbs" aria-label="面包屑">
        <Link href="/banks">题库</Link><span>/</span>
        {bank && <Link href={`/banks/${bank.id}`}>{bank.title}</Link>}
        <span>/</span><span>第 {currentIndex + 1} 题</span>
      </nav>

      <div className="practice-layout">
        <article className="question-card">
          <div className="question-number">
            <span>QUESTION {String(currentIndex + 1).padStart(2, "0")}</span>
            <span>{question.vip ? "会员题" : "公开题"}</span>
          </div>
          <div className="tag-row">
            <span className={`difficulty ${question.difficulty}`}>{question.difficulty}</span>
            {question.vip && <span className="vip-pill">VIP</span>}
            {question.tags.map((tag) => <span key={tag}>{tag}</span>)}
          </div>
          <h1>{question.title}</h1>
          <p className="question-content">{question.content}</p>

          <div className="question-actions">
            <button
              className={liked ? "active" : ""}
              type="button"
              onClick={() => setLiked((current) => !current)}
              aria-pressed={liked}
              data-od-id="like-question-button"
            >
              <span aria-hidden="true">{liked ? "♥" : "♡"}</span>
              {liked ? "已点赞" : "点赞"} {formatCount(question.likes + (liked ? 1 : 0))}
            </button>
            <button
              className={favourited ? "active" : ""}
              type="button"
              onClick={() => setFavourited((current) => !current)}
              aria-pressed={favourited}
              data-od-id="favourite-question-button"
            >
              <span aria-hidden="true">{favourited ? "★" : "☆"}</span>
              {favourited ? "已收藏" : "收藏"} {formatCount(question.favourites + (favourited ? 1 : 0))}
            </button>
            <span>{formatCount(question.views)} 次浏览</span>
          </div>

          <section className="answer-section" aria-live="polite">
            <div className="answer-heading">
              <div>
                <p className="section-kicker">参考答案</p>
                <h2>{question.vip ? "会员专享解析" : "想好以后，再看答案。"}</h2>
              </div>
              {!question.vip && (
                <button
                  type="button"
                  onClick={() => setAnswerVisible((current) => !current)}
                  aria-expanded={answerVisible}
                  data-od-id="show-answer-button"
                >
                  {answerVisible ? "收起答案" : "显示答案"}
                </button>
              )}
            </div>

            {question.vip ? (
              <div className="vip-upgrade" data-od-id="vip-answer-gate">
                <span className="vip-label">PRO</span>
                <div>
                  <h3>这道题的完整解析属于梅问会员</h3>
                  <p>解锁进阶题解、学习路径与全部会员题库。</p>
                </div>
                <Link href="/membership">了解会员 <span aria-hidden="true">→</span></Link>
              </div>
            ) : answerVisible ? (
              <div className="answer-content" data-od-id="revealed-answer">
                <p>{question.answer}</p>
                <div className="answer-note">
                  <b>复盘提示</b>
                  <span>尝试用自己的话复述一次，再进入下一题。</span>
                </div>
              </div>
            ) : (
              <div className="answer-placeholder">
                <span aria-hidden="true">思</span>
                <p>答案当前已收起。先整理你的思路，再与参考答案对照。</p>
              </div>
            )}
          </section>
        </article>

        <aside className="practice-aside">
          <div className="source-card">
            <p className="section-kicker">来自题库</p>
            <h2>{bank?.title}</h2>
            <p>{bank?.description}</p>
            {bank && <Link href={`/banks/${bank.id}`}>返回题库 <span aria-hidden="true">→</span></Link>}
          </div>
          <div className="practice-progress">
            <span>本组进度</span>
            <b>{currentIndex + 1} / {bankQuestions.length}</b>
            <div className="progress-track">
              <span style={{ width: `${((currentIndex + 1) / bankQuestions.length) * 100}%` }} />
            </div>
          </div>
        </aside>
      </div>

      <nav className="question-navigation" aria-label="题目导航">
        {previous ? (
          <Link href={`/questions/${previous.id}`}><span>← 上一题</span><b>{previous.title}</b></Link>
        ) : <span className="disabled">已经是第一题</span>}
        {bank && <Link className="return-link" href={`/banks/${bank.id}`}>返回题库</Link>}
        {next ? (
          <Link className="next" href={`/questions/${next.id}`}><span>下一题 →</span><b>{next.title}</b></Link>
        ) : <span className="disabled">已经是最后一题</span>}
      </nav>
    </main>
  );
}
