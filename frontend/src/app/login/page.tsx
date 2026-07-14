"use client";

import { useState } from "react";
import { apiClient, withBasePath } from "@/lib/api-client";
import { useAuth } from "@/context/AuthContext";
import type { LoginResponse, ErrorResponse } from "@/types/api";

export default function LoginPage() {
  const [username, setUsername] = useState("");
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);
  const { setUser } = useAuth();

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!username.trim() || loading) return;
    setError("");
    setLoading(true);

    try {
      const response = await apiClient<LoginResponse>("/auth/login", {
        method: "POST",
        body: JSON.stringify({ username }),
      });
      setUser(response);
      window.location.href = withBasePath("/home");
    } catch (err) {
      const errorResponse = err as ErrorResponse;
      setError(errorResponse.message || "ログインに失敗しました");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="flex items-center justify-center min-h-screen bg-gradient-to-br from-slate-50 to-cyan-50">
      <div className="w-full max-w-sm">
        <div className="bg-white rounded-2xl shadow-lg shadow-slate-200/50 border border-slate-100 overflow-hidden">
          {/* ヘッダー */}
          <div className="bg-gradient-to-r from-cyan-600 to-blue-600 px-8 py-10 text-center">
            <h1 className="text-2xl font-bold text-white tracking-tight">
              勤怠管理システム
            </h1>
            <p className="mt-2 text-cyan-100 text-sm">ユーザー名を入力してログイン</p>
          </div>

          {/* フォーム */}
          <form onSubmit={handleSubmit} className="px-8 py-8 space-y-6">
            <div>
              <label
                htmlFor="username"
                className="block text-sm font-medium text-slate-700 mb-2"
              >
                ユーザー名
              </label>
              <input
                id="username"
                type="text"
                value={username}
                onChange={(e) => setUsername(e.target.value)}
                className="w-full px-4 py-3 border border-slate-200 rounded-xl bg-slate-50 focus:outline-none focus:ring-2 focus:ring-cyan-500 focus:border-transparent focus:bg-white transition-all"
                placeholder="例: yamada"
                required
              />
            </div>
            {error && (
              <div className="px-4 py-3 bg-red-50 border border-red-200 rounded-lg">
                <p className="text-sm text-red-700" role="alert">{error}</p>
              </div>
            )}
            <button
              type="submit"
              disabled={loading}
              className="w-full py-3 px-4 bg-cyan-600 text-white rounded-xl font-bold hover:bg-cyan-700 hover:shadow-lg hover:shadow-cyan-200 disabled:opacity-50 disabled:cursor-not-allowed transition-all duration-200"
            >
              {loading ? (
                <span className="flex items-center justify-center gap-2">
                  <span className="w-4 h-4 border-2 border-white/30 border-t-white rounded-full animate-spin" />
                  ログイン中...
                </span>
              ) : (
                "ログイン"
              )}
            </button>
          </form>
        </div>
      </div>
    </div>
  );
}
