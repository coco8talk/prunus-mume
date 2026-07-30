"use client";

import { createContext, useCallback, useContext, useEffect, useMemo, useState } from "react";
import { clearToken, errorMessage } from "@/app/lib/api";
import { authService, userService, type ApiUser } from "@/app/lib/services";

type AuthContextValue = {
  user: ApiUser | null;
  status: "loading" | "authenticated" | "anonymous";
  error: string;
  setUser: (user: ApiUser | null) => void;
  refresh: () => Promise<void>;
  logout: () => Promise<void>;
};

const AuthContext = createContext<AuthContextValue | null>(null);

export function AuthProvider({ children }: { children: React.ReactNode }) {
  const [user, setCurrentUser] = useState<ApiUser | null>(null);
  const [status, setStatus] = useState<AuthContextValue["status"]>("loading");
  const [error, setError] = useState("");

  const setUser = useCallback((nextUser: ApiUser | null) => {
    setCurrentUser(nextUser);
    setStatus(nextUser ? "authenticated" : "anonymous");
    setError("");
  }, []);

  const refresh = useCallback(async () => {
    try {
      const nextUser = await userService.me();
      setCurrentUser(nextUser);
      setStatus("authenticated");
      setError("");
    } catch (loadError) {
      setCurrentUser(null);
      setStatus("anonymous");
      setError(errorMessage(loadError));
    }
  }, []);

  useEffect(() => {
    void Promise.resolve().then(refresh);
  }, [refresh]);

  const logout = useCallback(async () => {
    try {
      await authService.logout();
    } finally {
      clearToken();
      setCurrentUser(null);
      setStatus("anonymous");
      setError("");
    }
  }, []);

  const value = useMemo(
    () => ({ user, status, error, setUser, refresh, logout }),
    [error, logout, refresh, setUser, status, user],
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  const value = useContext(AuthContext);
  if (!value) throw new Error("useAuth must be used inside AuthProvider");
  return value;
}

export function ProtectedContent({ children }: { children: React.ReactNode }) {
  const { error, status } = useAuth();

  if (status === "loading") {
    return (
      <main className="page-shell app-shell">
        <section className="state-panel" role="status"><h1>正在恢复登录状态…</h1></section>
      </main>
    );
  }

  if (status === "anonymous") {
    const next = typeof window === "undefined"
      ? "/"
      : `${window.location.pathname}${window.location.search}`;
    return (
      <main className="page-shell app-shell">
        <section className="state-panel empty">
          <span aria-hidden="true">梅</span>
          <h1>登录后继续</h1>
          <p>{error ? `无法恢复登录状态：${error}` : "登录后即可访问你的收藏、贡献、签到与个人资料。"}</p>
          <a href={`/login?next=${encodeURIComponent(next)}`}>前往登录</a>
        </section>
      </main>
    );
  }

  return children;
}
