import { QuestionRow } from "@/app/components/QuestionRow";
import { favouriteQuestionIds, questions } from "@/app/data/mock";

export default function FavoritesPage() {
  const favourites = questions.filter((question) => favouriteQuestionIds.includes(question.id));

  return (
    <main className="page-shell app-shell" data-od-id="favorites-page">
      <section className="page-hero compact">
        <div>
          <p className="section-kicker">我的收藏</p>
          <h1>值得再想一次的题。</h1>
          <p>把关键问题留在手边，定期回看，直到答案真正属于你。</p>
        </div>
        <span className="hero-count"><b>{favourites.length}</b> 道已收藏</span>
      </section>
      <section className="detail-section">
        <div className="question-list">
          {favourites.map((question) => <QuestionRow question={question} key={question.id} showBank />)}
        </div>
      </section>
    </main>
  );
}
