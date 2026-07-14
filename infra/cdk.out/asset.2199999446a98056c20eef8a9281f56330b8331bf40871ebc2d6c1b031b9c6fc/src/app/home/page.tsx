"use client";

import { useEffect, useState } from "react";
import { apiClient } from "@/lib/api-client";
import { useAuth } from "@/context/AuthContext";
import type { AttendanceResponse, ErrorResponse } from "@/types/api";

export default function HomePage() {
  const { user, isReady } = useAuth();
  const [status, setStatus] = useState<AttendanceResponse | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [currentTime, setCurrentTime] = useState("");

  useEffect(() => {
    const updateTime = () => {
      setCurrentTime(
        new Date().toLocaleTimeString("ja-JP", { timeZone: "Asia/Tokyo" })
      );
    };
    updateTime();
    const timer = setInterval(updateTime, 1000);
    return () => clearInterval(timer);
  }, []);

  useEffect(() => {
    if (!isReady) return;
    if (user) {
      fetchStatus();
    } else {
      setLoading(false);
    }
  }, [user, isReady]);

  const fetchStatus = async () => {
    try {
      const response = await apiClient<AttendanceResponse>(
        "/attendance/status"
      );
      setStatus(response);
    } catch {
      setStatus(null);
    } finally {
      setLoading(false);
    }
  };

  const handleClockIn = async () => {
    setError("");
    try {
      const response = await apiClient<AttendanceResponse>(
        "/attendance/clock-in",
        { method: "POST" }
      );
      setStatus(response);
    } catch (err) {
      const errorResponse = err as ErrorResponse;
      setError(errorResponse.message || "打刻に失敗しました");
    }
  };

  const handleClockOut = async () => {
    setError("");
    try {
      const response = await apiClient<AttendanceResponse>(
        "/attendance/clock-out",
        { method: "POST" }
      );
      setStatus(response);
    } catch (err) {
      const errorResponse = err as ErrorResponse;
      setError(errorResponse.message || "打刻に失敗しました");
    }
  };

  if (!isReady) return null;
  if (!user) return null;

  const today = new Date();
  const dateStr = today.toLocaleDateString("ja-JP", {
    timeZone: "Asia/Tokyo",
    year: "numeric",
    month: "long",
    day: "numeric",
    weekday: "short",
  });

  const isClockedIn = status?.clockInTime != null;
  const isClockedOut = status?.clockOutTime != null;

  const formatTime = (time: string | null | undefined) => {
    if (!time) return "";
    return time.substring(0, 5);
  };

  const getStatusBadge = () => {
    if (isClockedOut)
      return { text: "退勤済み", color: "bg-gray-100 text-gray-700 border-gray-300" };
    if (isClockedIn)
      return { text: "勤務中", color: "bg-emerald-50 text-emerald-700 border-emerald-300" };
    return { text: "未出勤", color: "bg-amber-50 text-amber-700 border-amber-300" };
  };

  const badge = getStatusBadge();

  return (
    <div className="flex-1 flex flex-col bg-gradient-to-br from-slate-50 to-cyan-50">
      <main className="flex-1 flex items-center justify-center p-4">
        {loading ? (
          <div className="flex items-center gap-3 text-slate-500">
            <div className="w-5 h-5 border-2 border-slate-300 border-t-cyan-600 rounded-full animate-spin" />
            <span>読み込み中...</span>
          </div>
        ) : (
          <div className="w-full max-w-md">
            <div className="bg-white rounded-2xl shadow-lg shadow-slate-200/50 border border-slate-100 overflow-hidden">
              {/* 時刻ヘッダー */}
              <div className="bg-gradient-to-r from-cyan-600 to-blue-600 px-6 py-8 text-center text-white">
                <p className="text-5xl font-mono font-bold tracking-wider">
                  {currentTime}
                </p>
                <p className="mt-2 text-cyan-100 text-sm">{dateStr}</p>
              </div>

              {/* ステータス */}
              <div className="px-6 py-4 border-b border-slate-100">
                <div className="flex items-center justify-between">
                  <span className={`inline-flex items-center px-3 py-1 rounded-full text-sm font-medium border ${badge.color}`}>
                    {badge.text}
                  </span>
                  {isClockedIn && (
                    <span className="text-sm text-slate-500">
                      {formatTime(status?.clockInTime)}
                      {isClockedOut && ` 〜 ${formatTime(status?.clockOutTime)}`}
                    </span>
                  )}
                </div>
              </div>

              {/* 打刻ボタン */}
              <div className="px-6 py-8">
                <div className="flex gap-4">
                  <button
                    onClick={handleClockIn}
                    disabled={isClockedIn}
                    className="flex-1 py-4 rounded-xl text-lg font-bold transition-all duration-200 disabled:opacity-40 disabled:cursor-not-allowed disabled:transform-none bg-emerald-500 text-white hover:bg-emerald-600 hover:shadow-lg hover:shadow-emerald-200 hover:-translate-y-0.5 active:translate-y-0"
                  >
                    出勤
                  </button>
                  <button
                    onClick={handleClockOut}
                    disabled={!isClockedIn || isClockedOut}
                    className="flex-1 py-4 rounded-xl text-lg font-bold transition-all duration-200 disabled:opacity-40 disabled:cursor-not-allowed disabled:transform-none bg-rose-500 text-white hover:bg-rose-600 hover:shadow-lg hover:shadow-rose-200 hover:-translate-y-0.5 active:translate-y-0"
                  >
                    退勤
                  </button>
                </div>

                {error && (
                  <div className="mt-4 px-4 py-3 bg-red-50 border border-red-200 rounded-lg">
                    <p className="text-sm text-red-700" role="alert">{error}</p>
                  </div>
                )}
              </div>
            </div>
          </div>
        )}
      </main>
    </div>
  );
}
