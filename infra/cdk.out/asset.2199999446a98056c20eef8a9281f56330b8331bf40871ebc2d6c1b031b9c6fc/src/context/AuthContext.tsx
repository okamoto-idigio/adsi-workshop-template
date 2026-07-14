"use client";

import {
  createContext,
  useCallback,
  useContext,
  useEffect,
  useState,
} from "react";
import { apiClient } from "@/lib/api-client";
import type { LoginResponse } from "@/types/api";

interface AuthContextType {
  user: LoginResponse | null;
  setUser: (user: LoginResponse | null) => void;
  logout: () => void;
  isReady: boolean;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

const STORAGE_KEY = "attendance_user";

export function AuthProvider({ children }: { children: React.ReactNode }) {
  const [user, setUserState] = useState<LoginResponse | null>(null);
  const [isReady, setIsReady] = useState(false);

  useEffect(() => {
    const stored = localStorage.getItem(STORAGE_KEY);
    if (stored) {
      try {
        setUserState(JSON.parse(stored));
        setIsReady(true);
      } catch {
        localStorage.removeItem(STORAGE_KEY);
        autoLoginAsDefault();
      }
    } else {
      autoLoginAsDefault();
    }
  }, []);

  const autoLoginAsDefault = async () => {
    try {
      const response = await apiClient<LoginResponse>("/auth/login", {
        method: "POST",
        body: JSON.stringify({ username: "yamada" }),
      });
      setUserState(response);
      localStorage.setItem(STORAGE_KEY, JSON.stringify(response));
    } catch {
      // ログイン失敗時はユーザーなしのまま
    } finally {
      setIsReady(true);
    }
  };

  const setUser = useCallback((newUser: LoginResponse | null) => {
    setUserState(newUser);
    if (newUser) {
      localStorage.setItem(STORAGE_KEY, JSON.stringify(newUser));
    } else {
      localStorage.removeItem(STORAGE_KEY);
    }
  }, []);

  const logout = useCallback(() => {
    setUser(null);
  }, [setUser]);

  return (
    <AuthContext value={{ user, setUser, logout, isReady }}>
      {children}
    </AuthContext>
  );
}

export function useAuth(): AuthContextType {
  const context = useContext(AuthContext);
  if (context === undefined) {
    throw new Error("useAuth must be used within an AuthProvider");
  }
  return context;
}
