import type { ReactNode } from "react";
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
  return (
    <main className="personal-shell app-shell">
      <header className="personal-heading">
        <div>
          <p className="section-kicker">{eyebrow}</p>
          <h1>{title}</h1>
          <p>{description}</p>
        </div>
        <span className="personal-avatar" aria-hidden="true">林</span>
      </header>
      <div className="personal-layout">
        <aside className="personal-sidebar">
          <div className="personal-mini-profile">
            <span>林</span>
            <div><b>林晚</b><small>持续学习的第 1,036 天</small></div>
          </div>
          <PersonalNav />
        </aside>
        <div className="personal-content">{children}</div>
      </div>
    </main>
  );
}
