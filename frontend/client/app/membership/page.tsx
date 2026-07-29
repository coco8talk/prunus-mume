import Link from "next/link";

export default function MembershipPage() {
  return (
    <main className="page-shell app-shell membership-page" data-od-id="membership-page">
      <section className="membership-hero">
        <span className="vip-label">MEIWEN PRO</span>
        <p className="section-kicker">梅问会员</p>
        <h1>把难题，也练成你的直觉。</h1>
        <p>解锁完整进阶题解、会员题库与结构化学习路径。</p>
        <Link href="/login">登录后升级 <span aria-hidden="true">→</span></Link>
      </section>
      <section className="membership-benefits">
        <article><span>01</span><h2>完整题解</h2><p>查看所有会员题的思路拆解与复盘提示。</p></article>
        <article><span>02</span><h2>进阶题库</h2><p>系统设计、大模型工程等高阶内容持续更新。</p></article>
        <article><span>03</span><h2>学习路径</h2><p>按照目标与掌握程度，获得更清晰的练习顺序。</p></article>
      </section>
    </main>
  );
}
