"use client";

import { useEffect, useRef, useState, useTransition } from "react";
import { MonthSelector } from "@/components/MonthSelector";
import { AttendanceTable } from "@/components/AttendanceTable";
import { apiClient } from "@/lib/api-client";
import type { AttendanceResponse, MonthlyAttendanceResponse } from "@/types/api";

function fetchAttendance(
  year: number,
  month: number
): Promise<MonthlyAttendanceResponse> {
  return apiClient<MonthlyAttendanceResponse>(
    `/attendance/me?year=${year}&month=${month}`
  );
}

export default function AttendancePage() {
  const now = new Date();
  const [year, setYear] = useState(now.getFullYear());
  const [month, setMonth] = useState(now.getMonth() + 1);
  const [records, setRecords] = useState<AttendanceResponse[] | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [isPending, startTransition] = useTransition();
  const initialized = useRef(false);

  useEffect(() => {
    if (initialized.current) return;
    initialized.current = true;
    startTransition(async () => {
      try {
        const result = await fetchAttendance(year, month);
        setRecords(result.records);
      } catch {
        setError("データの取得に失敗しました");
      }
    });
  }, []); // eslint-disable-line react-hooks/exhaustive-deps

  const handleMonthChange = (newYear: number, newMonth: number) => {
    setYear(newYear);
    setMonth(newMonth);
    startTransition(async () => {
      try {
        const result = await fetchAttendance(newYear, newMonth);
        setRecords(result.records);
        setError(null);
      } catch {
        setError("データの取得に失敗しました");
        setRecords(null);
      }
    });
  };

  return (
    <div className="max-w-4xl mx-auto p-6">
      <h1 className="text-2xl font-bold mb-6">勤怠一覧</h1>
      <div className="mb-6">
        <MonthSelector year={year} month={month} onChange={handleMonthChange} />
      </div>
      {isPending && <p className="text-gray-500">読み込み中...</p>}
      {error && <p className="text-red-500">{error}</p>}
      {records && <AttendanceTable records={records} />}
    </div>
  );
}
