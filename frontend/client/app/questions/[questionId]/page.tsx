"use client";

import Link from "next/link";
import { useParams, useSearchParams } from "next/navigation";
import { useEffect, useState } from "react";
import { VipBadge } from "@/app/components/VipBadge";
import { useAuth } from "@/app/components/AuthProvider";
import { formatCount, type Question, type QuestionBank } from "@/app/data/models";
import { errorMessage } from "@/app/lib/api";
import {
  bankService,
  favouriteService,
  isActiveVip,
  questionService,
  thumbService,
} from "@/app/lib/services";

export default function QuestionDetailPage() {
  const params = useParams<{ questionId: string }>();
  const searchParams = useSearchParams();
  const bankId = searchParams.get("bankId");
  const { user, status: authStatus } = useAuth();
  const [question, setQuestion] = useState<Question | null>(null);
  const [bank, setBank] = useState<QuestionBank | null>(null);
  const [bankQuestions, setBankQuestions] = useState<Question[]>([]);
  const [answerVisible, setAnswerVisible] = useState(false);
  const [liked, setLiked] = useState(false);
  const [favourited, setFavourited] = useState(false);
  const [busyAction, setBusyAction] = useState<"like" | "favourite" | null>(null);
  const [status, setStatus] = useState<"loading" | "ready" | "error">("loading");
  const [message, setMessage] = useState("");
  const activeVip = isActiveVip(user);

  useEffect(() => {
    if (authStatus === "loading") return;
    const controller = new AbortController();

    const contextRequest = bankId
      ? Promise.all([
          bankService.detail(bankId, controller.signal),
          bankService.questions(bankId, 1, 20, controller.signal),
        ])
      : Promise.resolve(null);

    Promise.all([
      questionService.detail(params.questionId, controller.signal),
      favouriteService.count(params.questionId, controller.signal),
      thumbService.count(params.questionId, controller.signal),
      contextRequest,
    ]).then(async ([loadedQuestion, favouriteCount, likeCount, context]) => {
      const safeQuestion = {
        ...loadedQuestion,
        favourites: favouriteCount,
        likes: likeCount,
      };
      if (safeQuestion.vip && !activeVip) delete safeQuestion.answer;
      setQuestion(safeQuestion);
      setAnswerVisible(false);

      if (context) {
        setBank(context[0]);
        setBankQuestions(context[1].records);
      } else {
        setBank(null);
        setBankQuestions([]);
      }

      if (authStatus === "authenticated") {
        const [favouriteStatus, likeStatus] = await Promise.all([
          favouriteService.status(params.questionId, controller.signal),
          thumbService.status(params.questionId, controller.signal),
        ]);
        setFavourited(favouriteStatus);
        setLiked(likeStatus);
      } else {
        setFavourited(false);
        setLiked(false);
      }
      setStatus("ready");
    }).catch((error) => {
      if (controller.signal.aborted) return;
      setMessage(errorMessage(error));
      setStatus("error");
    });

    return () => controller.abort();
  }, [activeVip, authStatus, bankId, params.questionId]);

  async function toggleLike() {
    if (!question || busyAction) return;
    if (authStatus !== "authenticated") {
      setMessage("登录后即可点赞题目。");
      return;
    }

    const previous = liked;
    setBusyAction("like");
    setLiked(!previous);
    setQuestion((current) => current
      ? { ...current, likes: Math.max(0, current.likes + (previous ? -1 : 1)) }
      : current);
    try {
      await (previous ? thumbService.remove(question.id) : thumbService.add(question.id));
      const count = await thumbService.count(question.id);
      setQuestion((current) => current ? { ...current, likes: count } : current);
      setMessage("");
    } catch (error) {
      setLiked(previous);
      setQuestion((current) => current
        ? { ...current, likes: Math.max(0, current.likes + (previous ? 1 : -1)) }
        : current);
      setMessage(errorMessage(error));
    } finally {
      setBusyAction(null);
    }
  }

  async function toggleFavourite() {
    if (!question || busyAction) return;
    if (authStatus !== "authenticated") {
      setMessage("登录后即可收藏题目。");
      return;
    }

    const previous = favourited;
    setBusyAction("favourite");
    setFavourited(!previous);
    setQuestion((current) => current
      ? { ...current, favourites: Math.max(0, current.favourites + (previous ? -1 : 1)) }
      : current);
    try {
      await (previous ? favouriteService.remove(question.id) : favouriteService.add(question.id));
      const count = await favouriteService.count(question.id);
      setQuestion((current) => current ? { ...current, favourites: count } : current);
      setMessage("");
    } catch (error) {
      setFavourited(previous);
      setQuestion((current) => current
        ? { ...current, favourites: Math.max(0, current.favourites + (previous ? 1 : -1)) }
        : current);
      setMessage(errorMessage(error));
    } finally {
      setBusyAction(null);
    }
  }

  if (status === "loading") {
    return <main className="page-shell app-shell"><section className="state-panel" role="status"><h1>正在加载题目…</h1></section></main>;
  }

  if (status === "error" || !question) {
    return (
      <main className="page-shell app-shell">
        <section className="state-panel error" role="alert">
          <span aria-hidden="true">？</span>
          <h1>暂时无法打开这道题</h1>
          <p>{message}</p>
          <Link href="/questions">浏览全部题目</Link>
        </section>
      </main>
    );
  }

  const currentIndex = bankQuestions.findIndex((item) => item.id === question.id);
  const previous = currentIndex > 0 ? bankQuestions[currentIndex - 1] : undefined;
  const next = currentIndex >= 0 ? bankQuestions[currentIndex + 1] : undefined;
  const gated = question.vip && !activeVip;

  return (
    <main className="practice-shell app-shell" data-od-id="question-practice-page">
      <nav className="breadcrumbs" aria-label="面包屑">
        <Link href="/banks">题库</Link><span>/</span>
        {bank && <Link href={`/banks/${bank.id}`}>{bank.title}</Link>}
        {bank && <span>/</span>}<span>{currentIndex >= 0 ? `第 ${currentIndex + 1} 题` : "题目详情"}</span>
      </nav>

      <div className="practice-layout">
        <article className="question-card">
          <div className="question-number">
            <span>QUESTION {currentIndex >= 0 ? String(currentIndex + 1).padStart(2, "0") : ""}</span>
            <span>{question.vip ? "会员题" : "公开题"}</span>
          </div>
          <div className="tag-row">
            <span className={`difficulty ${question.difficulty}`}>{question.difficulty}</span>
            {question.vip && (
              <Link
                className="vip-badge-link"
                href="/membership"
                aria-label="开通梅问会员，解锁完整解析"
                data-od-id="question-vip-upgrade"
              >
                <VipBadge />
              </Link>
            )}
            {question.tags.map((tag) => <span key={tag}>{tag}</span>)}
          </div>
          <h1>{question.title}</h1>
          <p className="question-content">{question.content}</p>

          {message && <div className="feedback error" role="alert">{message}</div>}
          <div className="question-actions">
            <button
              className={liked ? "active" : ""}
              type="button"
              onClick={toggleLike}
              disabled={busyAction !== null}
              aria-pressed={liked}
              data-od-id="like-question-button"
            >
              <span aria-hidden="true">{liked ? "♥" : "♡"}</span>
              {liked ? "已点赞" : "点赞"} {formatCount(question.likes)}
            </button>
            <button
              className={favourited ? "active" : ""}
              type="button"
              onClick={toggleFavourite}
              disabled={busyAction !== null}
              aria-pressed={favourited}
              data-od-id="favourite-question-button"
            >
              <span aria-hidden="true">{favourited ? "★" : "☆"}</span>
              {favourited ? "已收藏" : "收藏"} {formatCount(question.favourites)}
            </button>
            <span>{formatCount(question.views)} 次浏览</span>
          </div>

          <section className="answer-section" aria-live="polite">
            <div className="answer-heading">
              <div>
                <p className="section-kicker">参考答案</p>
                <h2>{gated ? "梅问会员专享解析" : "想好以后，再看答案。"}</h2>
              </div>
              {!gated && (
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

            {gated ? (
              <div className="vip-upgrade" data-od-id="vip-answer-gate">
                <VipBadge />
                <div>
                  <h3>完整解析由梅问会员解锁</h3>
                  <p>开通后可查看会员题完整解析与全部会员题库。</p>
                </div>
                <Link href="/membership">查看会员方案 <span aria-hidden="true">→</span></Link>
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
            <h2>{bank?.title ?? "梅问精选题目"}</h2>
            <p>{bank?.description ?? "继续浏览全部题目，找到下一道值得思考的问题。"}</p>
            <Link href={bank ? `/banks/${bank.id}` : "/questions"}>
              {bank ? "返回题库" : "浏览全部题目"} <span aria-hidden="true">→</span>
            </Link>
          </div>
          {currentIndex >= 0 && (
            <div className="practice-progress">
              <span>本页进度</span>
              <b>{currentIndex + 1} / {bankQuestions.length}</b>
              <div className="progress-track">
                <span style={{ width: `${((currentIndex + 1) / bankQuestions.length) * 100}%` }} />
              </div>
            </div>
          )}
        </aside>
      </div>

      {bank && (
        <nav className="question-navigation" aria-label="题目导航">
          {previous ? (
            <Link href={`/questions/${previous.id}?bankId=${encodeURIComponent(bank.id)}`}><span>← 上一题</span><b>{previous.title}</b></Link>
          ) : <span className="disabled">已经是本页第一题</span>}
          <Link className="return-link" href={`/banks/${bank.id}`}>返回题库</Link>
          {next ? (
            <Link className="next" href={`/questions/${next.id}?bankId=${encodeURIComponent(bank.id)}`}><span>下一题 →</span><b>{next.title}</b></Link>
          ) : <span className="disabled">已经是本页最后一题</span>}
        </nav>
      )}
    </main>
  );
}
