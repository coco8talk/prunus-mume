import Link from "next/link";
import { formatCount, getBank, type Question } from "@/app/data/mock";

export function QuestionRow({
  question,
  showBank = false,
}: {
  question: Question;
  showBank?: boolean;
}) {
  const bank = getBank(question.bankId);

  return (
    <article className="question-row" data-od-id={`question-row-${question.id}`}>
      <div className="question-row-main">
        <div className="tag-row">
          <span className={`difficulty ${question.difficulty}`}>{question.difficulty}</span>
          {question.vip && <span className="vip-pill">VIP</span>}
          {question.tags.slice(0, 3).map((tag) => (
            <span key={tag}>{tag}</span>
          ))}
        </div>
        <h3>
          <Link href={`/questions/${question.id}`}>{question.title}</Link>
        </h3>
        {showBank && bank && (
          <p>
            来自 <Link href={`/banks/${bank.id}`}>{bank.title}</Link>
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
