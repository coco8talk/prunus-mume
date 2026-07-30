"use client";

import Image from "next/image";
import type { ReactNode } from "react";
import { useAuth } from "@/app/components/AuthProvider";
import { PersonalNav } from "@/app/components/PersonalNav";

export function PersonalShell({
  eyebrow,
  title,
  description,
  children,
}: {
  eyebrow: string;
  title: string;
  description: string;
  children: ReactNode;
}) {
  const { user } = useAuth();
  const displayName = user?.userName ?? user?.userAccount ?? "梅问用户";
  const joinDate = user?.createTime
    ? new Intl.DateTimeFormat("zh-CN", { year: "numeric", month: "long" }).format(new Date(user.createTime))
    : null;

  return (
    <main className="personal-shell app-shell">
      <header className="personal-heading">
        <div>
          <p className="section-kicker">{eyebrow}</p>
          <h1>{title}</h1>
          <p>{description}</p>
        </div>
        {user?.userAvatar
          ? <Image className="personal-avatar" src={user.userAvatar} alt="" width={82} height={82} unoptimized />
          : <span className="personal-avatar" aria-hidden="true">{displayName.slice(0, 1)}</span>}
      </header>
      <div className="personal-layout">
        <aside className="personal-sidebar">
          <div className="personal-mini-profile">
            <span>{displayName.slice(0, 1)}</span>
            <div>
              <b>{displayName}</b>
              <small>{joinDate ? `加入于 ${joinDate}` : "梅问学习者"}</small>
            </div>
          </div>
          <PersonalNav />
        </aside>
        <div className="personal-content">{children}</div>
      </div>
    </main>
  );
}
