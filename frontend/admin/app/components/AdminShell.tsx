"use client";

import Link from "next/link";
import { usePathname, useRouter } from "next/navigation";
import { useEffect, useState } from "react";
import { useAuth } from "./AuthProvider";
import { BrandMark } from "./BrandMark";

const navigation = [
  { label: "Users", icon: "◎", href: "/users" },
  { label: "Question banks", icon: "▤", href: "/question-banks" },
  { label: "Questions", icon: "?", href: "/questions" },
  { label: "Review queue", icon: "✓", planned: true },
  { label: "Review history", icon: "↻", planned: true },
];

function pageContext(pathname: string) {
  if (pathname.startsWith("/question-banks/")) {
    return { title: "Bank contents", description: "Question assignment and curation" };
  }
  if (pathname.startsWith("/question-banks")) {
    return { title: "Question banks", description: "Collections, covers and contents" };
  }
  if (pathname.startsWith("/questions")) {
    return { title: "Question management", description: "Question library and review state" };
  }
  return { title: "User management", description: "Accounts, roles and access" };
}

function initials(value: string) {
  return value.slice(0, 2).toUpperCase();
}

export function AdminLoading() {
  return (
    <main className="loading-screen">
      <div className="loading-mark">
        <BrandMark compact />
      </div>
      <div className="loading-bar" />
      <p>Preparing your operations workspace…</p>
    </main>
  );
}

export function AdminShell({ children }: { children: React.ReactNode }) {
  const router = useRouter();
  const pathname = usePathname();
  const { user, ready, logout } = useAuth();
  const [menuOpen, setMenuOpen] = useState(false);
  const [loggingOut, setLoggingOut] = useState(false);

  useEffect(() => {
    if (ready && !user) {
      router.replace("/login");
    }
  }, [ready, router, user]);

  async function handleLogout() {
    setLoggingOut(true);
    try {
      await logout();
    } finally {
      router.replace("/login");
    }
  }

  if (!ready || !user) {
    return <AdminLoading />;
  }

  const context = pageContext(pathname);

  return (
    <div className="admin-shell">
      <aside className={`sidebar${menuOpen ? " sidebar--open" : ""}`}>
        <div className="sidebar__brand">
          <BrandMark />
          <button
            className="sidebar-close"
            type="button"
            aria-label="Close navigation"
            onClick={() => setMenuOpen(false)}
          >
            ×
          </button>
        </div>

        <nav className="sidebar__nav" aria-label="Admin navigation">
          <p className="nav-label">Manage</p>
          {navigation.map((item) =>
            item.href ? (
              <Link
                className={`nav-item${
                  pathname === item.href ||
                  (item.href !== "/users" && pathname.startsWith(`${item.href}/`))
                    ? " nav-item--active"
                    : ""
                }`}
                href={item.href}
                key={item.label}
                onClick={() => setMenuOpen(false)}
              >
                <span className="nav-icon" aria-hidden="true">
                  {item.icon}
                </span>
                <span>{item.label}</span>
              </Link>
            ) : (
              <button
                className="nav-item"
                type="button"
                key={item.label}
                disabled
                title="Available when this module's API contract is added"
              >
                <span className="nav-icon" aria-hidden="true">
                  {item.icon}
                </span>
                <span>{item.label}</span>
                {item.planned && <small>Soon</small>}
              </button>
            ),
          )}
        </nav>

        <div className="sidebar__status">
          <div className="system-status">
            <span className="system-status__icon" aria-hidden="true">
              ◌
            </span>
            <div>
              <strong>Admin session</strong>
              <small>Role gate verified</small>
            </div>
            <span className="status-dot" />
          </div>
          <p>Admin workspace · v1.0</p>
        </div>
      </aside>

      {menuOpen && (
        <button
          className="sidebar-backdrop"
          type="button"
          aria-label="Close navigation"
          onClick={() => setMenuOpen(false)}
        />
      )}

      <div className="workspace">
        <header className="topbar">
          <div className="topbar__left">
            <button
              className="menu-button"
              type="button"
              aria-label="Open navigation"
              onClick={() => setMenuOpen(true)}
            >
              ☰
            </button>
            <div>
              <p>{context.title}</p>
              <small>{context.description}</small>
            </div>
          </div>

          <div className="topbar__right">
            <span className="secure-session">
              <span aria-hidden="true">◇</span>
              Secure admin session
            </span>
            <div className="account-menu">
              {user.userAvatar ? (
                // eslint-disable-next-line @next/next/no-img-element
                <img src={user.userAvatar} alt="" className="avatar" />
              ) : (
                <span className="avatar avatar--initials" aria-hidden="true">
                  {initials(user.userName || user.userAccount)}
                </span>
              )}
              <div className="account-copy">
                <strong>{user.userName || user.userAccount}</strong>
                <small>Administrator</small>
              </div>
              <button
                type="button"
                className="logout-button"
                onClick={handleLogout}
                disabled={loggingOut}
              >
                {loggingOut ? "Leaving…" : "Sign out"}
              </button>
            </div>
          </div>
        </header>

        {children}
      </div>
    </div>
  );
}
