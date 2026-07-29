import Link from "next/link";

export default function ProfilePage() {
  return (
    <main className="page-shell app-shell" data-od-id="profile-page">
      <section className="profile-card">
        <span className="profile-avatar">林</span>
        <div>
          <p className="section-kicker">个人中心</p>
          <h1>林晚</h1>
          <p>连续学习 12 天 · 本周已完成 48 题</p>
        </div>
        <Link href="/login">切换账号</Link>
      </section>
      <section className="profile-stats">
        <article><span>累计练习</span><b>1,286</b><small>道题</small></article>
        <article><span>掌握题库</span><b>8</b><small>个主题</small></article>
        <article><span>我的收藏</span><b>36</b><small>道题</small></article>
      </section>
    </main>
  );
}
