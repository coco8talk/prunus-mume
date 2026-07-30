"use client";

import Link from "next/link";
import Image from "next/image";
import { usePathname, useRouter } from "next/navigation";
import { useAuth } from "@/app/components/AuthProvider";
import { errorMessage } from "@/app/lib/api";

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
  const router = useRouter();
  const { user, status, logout } = useAuth();
  const displayName = user?.userName ?? user?.userAccount ?? "梅问用户";

  async function handleLogout() {
    try {
      await logout();
    } catch (error) {
      window.alert(errorMessage(error));
    }
    router.push(`/login?next=${encodeURIComponent(pathname)}`);
  }

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
          {status === "authenticated" && user ? (
            <>
              <Link
                className={`text-button ${isActive(pathname, "/me/favourites") ? "active" : ""}`}
                href="/me/favourites"
              >
                我的收藏
              </Link>
              <details className="avatar-menu" data-od-id="profile-avatar-menu">
                <summary className="avatar-button" aria-label="打开个人中心菜单">
                  {user.userAvatar
                    ? <Image src={user.userAvatar} alt="" width={40} height={40} unoptimized />
                    : <span>{displayName.slice(0, 1)}</span>}
                </summary>
                <div>
                  <p><b>{displayName}</b><small>{user.userAccount}</small></p>
                  <Link href="/me/favourites">我的收藏</Link>
                  <Link href="/me/contributions">题目贡献</Link>
                  <Link href="/me/sign-in">每日签到</Link>
                  <Link href="/me/profile">个人资料</Link>
                  <button type="button" onClick={handleLogout}>退出登录</button>
                </div>
              </details>
            </>
          ) : status === "anonymous" ? (
            <Link className="text-button" href={`/login?next=${encodeURIComponent(pathname)}`}>
              登录
            </Link>
          ) : null}
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
