import Link from "next/link";
import { formatCount, type Question } from "@/app/data/models";
import { VipBadge } from "@/app/components/VipBadge";

export function QuestionRow({
  question,
  showBank = false,
  bank,
}: {
  question: Question;
  showBank?: boolean;
  bank?: { id: string; title: string };
}) {
  const questionHref = bank
    ? `/questions/${question.id}?bankId=${encodeURIComponent(bank.id)}`
    : `/questions/${question.id}`;

  return (
    <article className="question-row" data-od-id={`question-row-${question.id}`}>
      <div className="question-row-main">
        <div className="tag-row">
          <span className={`difficulty ${question.difficulty}`}>{question.difficulty}</span>
          {question.vip && (
            <Link
              className="vip-badge-link"
              href="/membership"
              aria-label="开通梅问会员，解锁完整解析"
              data-od-id={`vip-upgrade-${question.id}`}
            >
              <VipBadge />
            </Link>
          )}
          {question.tags.slice(0, 3).map((tag) => (
            <span key={tag}>{tag}</span>
          ))}
        </div>
        <h3>
          <Link href={questionHref}>{question.title}</Link>
        </h3>
        {showBank && bank && (
          <p>
            来自 <Link href={`/banks/${bank.id}`}>{bank.title}</Link>
          </p>
        )}
        {question.vip && (
          <p className="vip-gating-note">
            完整解析由梅问会员解锁 · <Link href="/membership">查看会员方案</Link>
          </p>
        )}
      </div>
      <div className="question-stats" aria-label="题目数据">
        <span>浏览 {formatCount(question.views)}</span>
        <span>赞 {formatCount(question.likes)}</span>
        <span>收藏 {formatCount(question.favourites)}</span>
      </div>
    </article>
  );
}
