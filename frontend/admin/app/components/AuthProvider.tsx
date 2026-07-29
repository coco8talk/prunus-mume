"use client";

import {
  createContext,
  useCallback,
  useContext,
  useEffect,
  useMemo,
  useState,
} from "react";
import type { AdminUser } from "../lib/api";
import { apiRequest } from "../lib/api";

type LoginCredentials = {
  userAccount: string;
  userPassword: string;
};

type AuthContextValue = {
  user: AdminUser | null;
  token: string | null;
  ready: boolean;
  login: (credentials: LoginCredentials) => Promise<AdminUser>;
  logout: () => Promise<void>;
  clearSession: () => void;
};

const AuthContext = createContext<AuthContextValue | null>(null);
const USER_KEY = "prunus-admin-user";
const TOKEN_KEY = "satoken";

function clearStoredSession() {
  window.localStorage.removeItem(USER_KEY);
  window.localStorage.removeItem(TOKEN_KEY);
}

export function AuthProvider({ children }: { children: React.ReactNode }) {
  const [user, setUser] = useState<AdminUser | null>(null);
  const [token, setToken] = useState<string | null>(null);
  const [ready, setReady] = useState(false);

  useEffect(() => {
    const hydrationTimer = window.setTimeout(() => {
      const storedUser = window.localStorage.getItem(USER_KEY);
      const storedToken = window.localStorage.getItem(TOKEN_KEY);

      if (storedUser) {
        try {
          const parsedUser = JSON.parse(storedUser) as AdminUser;
          if (parsedUser.userRole === 0) {
            setUser(parsedUser);
            setToken(storedToken);
          } else {
            clearStoredSession();
          }
        } catch {
          clearStoredSession();
        }
      }

      setReady(true);
    }, 0);

    return () => window.clearTimeout(hydrationTimer);
  }, []);

  const clearSession = useCallback(() => {
    setUser(null);
    setToken(null);
    clearStoredSession();
  }, []);

  const login = useCallback(async (credentials: LoginCredentials) => {
    const response = await apiRequest<AdminUser>("/auth/login", {
      method: "POST",
      body: JSON.stringify(credentials),
    });

    if (response.data.userRole !== 0) {
      try {
        await apiRequest<void>(
          "/auth/logout",
          { method: "POST" },
          response.token,
        );
      } finally {
        clearStoredSession();
      }
      throw new Error(
        "Access denied. This console is restricted to administrator accounts.",
      );
    }

    setUser(response.data);
    setToken(response.token);
    window.localStorage.setItem(USER_KEY, JSON.stringify(response.data));

    if (response.token) {
      window.localStorage.setItem(TOKEN_KEY, response.token);
    } else {
      window.localStorage.removeItem(TOKEN_KEY);
    }

    return response.data;
  }, []);

  const logout = useCallback(async () => {
    try {
      await apiRequest<void>("/auth/logout", { method: "POST" }, token);
    } finally {
      clearSession();
    }
  }, [clearSession, token]);

  const value = useMemo(
    () => ({ user, token, ready, login, logout, clearSession }),
    [user, token, ready, login, logout, clearSession],
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  const context = useContext(AuthContext);

  if (!context) {
    throw new Error("useAuth must be used inside AuthProvider");
  }

  return context;
}
