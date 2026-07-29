"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";

const primaryItems = [
  { href: "/", label: "首页", shortLabel: "首页", glyph: "⌂" },
  { href: "/banks", label: "题库", shortLabel: "题库", glyph: "册" },
  { href: "/questions", label: "全部题目", shortLabel: "题目", glyph: "题" },
];

function isActive(pathname: string, href: string) {
  if (href === "/") return pathname === "/";
  return pathname === href || pathname.startsWith(`${href}/`);
}

export function Header() {
  const pathname = usePathname();

  return (
    <>
      <header className="topbar" data-od-id="top-navigation">
        <Link className="brand" href="/" aria-label="梅问首页">
          <span className="brand-mark" aria-hidden="true">梅</span>
          <span>梅问</span>
        </Link>
        <nav className="primary-nav" aria-label="主要导航">
          {primaryItems.map((item) => (
            <Link
              className={`nav-link ${isActive(pathname, item.href) ? "active" : ""}`}
              href={item.href}
              key={item.href}
              aria-current={isActive(pathname, item.href) ? "page" : undefined}
            >
              {item.label}
            </Link>
          ))}
        </nav>
        <div className="topbar-actions">
          <Link
            className={`text-button ${isActive(pathname, "/me/favourites") ? "active" : ""}`}
            href="/me/favourites"
          >
            我的收藏
          </Link>
          <details className="avatar-menu" data-od-id="profile-avatar-menu">
            <summary className="avatar-button" aria-label="打开个人中心菜单">
              <span>林</span>
            </summary>
            <div>
              <p><b>林晚</b><small>linwan</small></p>
              <Link href="/me/favourites">我的收藏</Link>
              <Link href="/me/contributions">题目贡献</Link>
              <Link href="/me/sign-in">每日签到</Link>
              <Link href="/me/profile">个人资料</Link>
            </div>
          </details>
        </div>
      </header>
      <nav className="mobile-bottom-nav" aria-label="移动端主要导航">
        {primaryItems.map((item) => (
          <Link
            className={isActive(pathname, item.href) ? "active" : ""}
            href={item.href}
            key={item.href}
            aria-current={isActive(pathname, item.href) ? "page" : undefined}
          >
            <span aria-hidden="true">{item.glyph}</span>
            {item.shortLabel}
          </Link>
        ))}
        <Link
          className={isActive(pathname, "/me") ? "active" : ""}
          href="/me/profile"
          aria-current={isActive(pathname, "/me") ? "page" : undefined}
        >
          <span aria-hidden="true">我</span>
          我的
        </Link>
      </nav>
    </>
  );
}
