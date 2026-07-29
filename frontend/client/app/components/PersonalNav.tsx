"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";

const personalItems = [
  { href: "/me/favourites", label: "我的收藏", glyph: "收" },
  { href: "/me/contributions", label: "题目贡献", glyph: "稿" },
  { href: "/me/sign-in", label: "每日签到", glyph: "签" },
  { href: "/me/profile", label: "个人资料", glyph: "我" },
];

export function PersonalNav() {
  const pathname = usePathname();

  return (
    <nav className="personal-nav" aria-label="个人中心导航" data-od-id="personal-navigation">
      {personalItems.map((item) => {
        const active = pathname === item.href;
        return (
          <Link
            href={item.href}
            className={active ? "active" : ""}
            aria-current={active ? "page" : undefined}
            key={item.href}
          >
            <span aria-hidden="true">{item.glyph}</span>
            <b>{item.label}</b>
          </Link>
        );
      })}
    </nav>
  );
}
