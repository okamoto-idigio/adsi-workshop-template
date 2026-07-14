"use client";

import type { AttendanceResponse } from "@/types/api";

interface AttendanceTableProps {
  records: AttendanceResponse[];
}

function formatTime(time: string | null): string {
  if (!time) return "-";
  return time.substring(0, 5);
}

function formatDuration(minutes: number | null): string {
  if (minutes === null) return "-";
  const hours = Math.floor(minutes / 60);
  const mins = minutes % 60;
  if (mins === 0) return `${hours}:00`;
  return `${hours}:${mins.toString().padStart(2, "0")}`;
}

export function AttendanceTable({ records }: AttendanceTableProps) {
  if (records.length === 0) {
    return <p className="text-gray-500 py-4">データがありません</p>;
  }

  return (
    <table className="w-full border-collapse border border-gray-300">
      <thead>
        <tr className="bg-gray-100">
          <th className="border border-gray-300 px-4 py-2 text-left">日付</th>
          <th className="border border-gray-300 px-4 py-2 text-left">出勤</th>
          <th className="border border-gray-300 px-4 py-2 text-left">退勤</th>
          <th className="border border-gray-300 px-4 py-2 text-left">勤務時間</th>
        </tr>
      </thead>
      <tbody>
        {records.map((record) => (
          <tr key={record.date} className="hover:bg-gray-50">
            <td className="border border-gray-300 px-4 py-2">{record.date}</td>
            <td className="border border-gray-300 px-4 py-2">
              {formatTime(record.clockInTime)}
            </td>
            <td className="border border-gray-300 px-4 py-2">
              {formatTime(record.clockOutTime)}
            </td>
            <td className="border border-gray-300 px-4 py-2">
              {formatDuration(record.workDurationMinutes)}
            </td>
          </tr>
        ))}
      </tbody>
    </table>
  );
}
