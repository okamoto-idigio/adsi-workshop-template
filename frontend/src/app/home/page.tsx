"use client";

import { useEffect, useState } from "react";
import { apiClient, withBasePath } from "@/lib/api-client";
import { useAuth } from "@/context/AuthContext";
import Header from "@/components/Header";
import ClockButton from "@/components/ClockButton";
import type { AttendanceResponse, ErrorResponse, LoginResponse } from "@/types/api";

export default function HomePage() {
  const { user, setUser } = useAuth();
  const [status, setStatus] = useState<AttendanceResponse | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  useEffect(() => {
    if (!user) {
      autoLogin();
      return;
    }
    fetchStatus();
  }, [user]);

  const autoLogin = async () => {
    try {
      const response = await apiClient<LoginResponse>("/auth/login", {
        method: "POST",
        body: JSON.stringify({ username: "yamada" }),
      });
      setUser(response);
    } catch {
      window.location.href = withBasePath("/login");
    }
  };

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

  if (!user) return null;

  const today = new Date();
  const dateStr = today.toLocaleDateString("ja-JP", {
    year: "numeric",
    month: "long",
    day: "numeric",
    weekday: "short",
  });

  const isClockedIn = status?.clockInTime != null;
  const isClockedOut = status?.clockOutTime != null;

  const getStatusText = () => {
    if (isClockedOut) return `退勤済み（${status?.clockInTime} 〜 ${status?.clockOutTime}）`;
    if (isClockedIn) return `出勤済み（${status?.clockInTime}）`;
    return "未出勤";
  };

  return (
    <div className="min-h-screen flex flex-col">
      <Header user={user} />
      <main className="flex-1 flex items-center justify-center">
        {loading ? (
          <p className="text-gray-500">読み込み中...</p>
        ) : (
          <div className="text-center space-y-8">
            <p className="text-xl text-gray-700">{dateStr}</p>
            <div className="flex gap-6 justify-center">
              <ClockButton
                label="出勤"
                onClick={handleClockIn}
                disabled={isClockedIn}
                variant="clockIn"
              />
              <ClockButton
                label="退勤"
                onClick={handleClockOut}
                disabled={!isClockedIn || isClockedOut}
                variant="clockOut"
              />
            </div>
            <p className="text-gray-600">{getStatusText()}</p>
            {error && (
              <p className="text-sm text-red-600" role="alert">
                {error}
              </p>
            )}
          </div>
        )}
      </main>
    </div>
  );
}
