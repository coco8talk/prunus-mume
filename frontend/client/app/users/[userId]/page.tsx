import Link from "next/link";
import { notFound } from "next/navigation";
import { publicProfiles } from "@/app/data/personal";

export default async function PublicUserProfilePage({
  params,
}: {
  params: Promise<{ userId: string }>;
}) {
  const { userId } = await params;
  const profile = publicProfiles[userId as keyof typeof publicProfiles];
  if (!profile) notFound();

  return (
    <main className="public-profile-page app-shell" data-od-id="public-user-profile">
      <nav className="breadcrumbs" aria-label="面包屑">
        <Link href="/">首页</Link><span>/</span><span>学习者主页</span>
      </nav>
      <section className="public-profile-hero">
        <span className="public-avatar" aria-hidden="true">{profile.displayName.slice(0, 1)}</span>
        <div>
          <div className="tag-row"><span className="role-pill">{profile.role}</span></div>
          <h1>{profile.displayName}</h1>
          <p>{profile.bio}</p>
          <div className="public-meta"><span>加入于 {profile.joinDate}</span><i /><span>{profile.approvedContributions} 道贡献已通过</span></div>
        </div>
      </section>
      <div className="public-profile-grid">
        <section>
          <div className="personal-section-heading">
            <div><p className="section-kicker">公开题库</p><h2>{profile.displayName} 的知识整理</h2></div>
          </div>
          <div className="public-bank-list">
            {profile.banks.map((bank) => (
              <Link href={`/banks/${bank.id}`} key={bank.id}>
                <span aria-hidden="true">册</span>
                <div><b>{bank.title}</b><small>{bank.questions} 道公开题目</small></div>
                <i aria-hidden="true">→</i>
              </Link>
            ))}
          </div>
        </section>
        <aside className="expertise-card">
          <p className="section-kicker">擅长方向</p>
          <h2>持续深耕的主题</h2>
          <div className="tag-row">{profile.expertise.map((item) => <span key={item}>{item}</span>)}</div>
          <p>公开主页仅展示学习与贡献相关信息。</p>
        </aside>
      </div>
    </main>
  );
}
