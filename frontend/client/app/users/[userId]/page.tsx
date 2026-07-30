"use client";

import Image from "next/image";
import Link from "next/link";
import { useParams } from "next/navigation";
import { useEffect, useState } from "react";
import { errorMessage } from "@/app/lib/api";
import { userService, type ApiUser } from "@/app/lib/services";

export default function PublicUserProfilePage() {
  const { userId } = useParams<{ userId: string }>();
  const [profile, setProfile] = useState<ApiUser | null>(null);
  const [status, setStatus] = useState<"loading" | "ready" | "error">("loading");
  const [message, setMessage] = useState("");
  const [loadedUserId, setLoadedUserId] = useState<string | null>(null);

  useEffect(() => {
    const controller = new AbortController();
    userService.publicProfile(userId, controller.signal).then((value) => {
      setProfile(value);
      setLoadedUserId(userId);
      setStatus("ready");
    }).catch((error) => {
      if (controller.signal.aborted) return;
      setMessage(errorMessage(error));
      setLoadedUserId(userId);
      setStatus("error");
    });
    return () => controller.abort();
  }, [userId]);

  if (status === "loading" || loadedUserId !== userId) {
    return <main className="page-shell app-shell"><section className="state-panel" role="status"><h1>正在加载学习者主页…</h1></section></main>;
  }

  if (status === "error" || !profile) {
    return (
      <main className="page-shell app-shell">
        <section className="state-panel error" role="alert">
          <h1>暂时无法打开这个主页</h1>
          <p>{message}</p>
          <Link href="/">返回首页</Link>
        </section>
      </main>
    );
  }

  const displayName = profile.userName ?? "梅问学习者";
  const joinDate = profile.createTime
    ? new Intl.DateTimeFormat("zh-CN", { year: "numeric", month: "long" }).format(new Date(profile.createTime))
    : "未知";

  return (
    <main className="public-profile-page app-shell" data-od-id="public-user-profile">
      <nav className="breadcrumbs" aria-label="面包屑">
        <Link href="/">首页</Link><span>/</span><span>学习者主页</span>
      </nav>
      <section className="public-profile-hero">
        {profile.userAvatar
          ? <Image className="public-avatar" src={profile.userAvatar} alt={`${displayName}的头像`} width={96} height={96} unoptimized />
          : <span className="public-avatar" aria-hidden="true">{displayName.slice(0, 1)}</span>}
        <div>
          <div className="tag-row">
            <span className="role-pill">{profile.userRole === 2 ? "梅问会员" : "学习者"}</span>
          </div>
          <h1>{displayName}</h1>
          <p>{profile.userProfile || "这位学习者还没有填写个人简介。"}</p>
          <div className="public-meta"><span>加入于 {joinDate}</span></div>
        </div>
      </section>
      <div className="public-profile-grid">
        <section>
          <div className="personal-section-heading">
            <div><p className="section-kicker">公开资料</p><h2>{displayName} 的学习主页</h2></div>
          </div>
          <div className="state-panel">
            <p>这里只展示昵称、头像、身份、简介与加入时间。</p>
          </div>
        </section>
        <aside className="expertise-card">
          <p className="section-kicker">隐私说明</p>
          <h2>公开信息保持克制</h2>
          <p>账号、电话、邮箱、学历与工作经历不会出现在公开主页。</p>
        </aside>
      </div>
    </main>
  );
}
