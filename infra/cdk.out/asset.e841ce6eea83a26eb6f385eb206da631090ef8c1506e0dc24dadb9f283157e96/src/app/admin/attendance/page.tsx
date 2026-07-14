"use client";

import { useEffect, useRef, useState, useTransition } from "react";
import { MonthSelector } from "@/components/MonthSelector";
import { AttendanceTable } from "@/components/AttendanceTable";
import { EmployeeSelector } from "@/components/EmployeeSelector";
import { apiClient } from "@/lib/api-client";
import type { EmployeeMonthlyAttendance, EmployeeSummary } from "@/types/api";

function fetchEmployees(): Promise<EmployeeSummary[]> {
  return apiClient<EmployeeSummary[]>("/employees");
}

function fetchAttendance(
  year: number,
  month: number,
  employeeId: number | null
): Promise<EmployeeMonthlyAttendance[]> {
  const params = new URLSearchParams({
    year: String(year),
    month: String(month),
  });
  if (employeeId !== null) {
    params.set("employeeId", String(employeeId));
  }
  return apiClient<EmployeeMonthlyAttendance[]>(`/attendance/all?${params}`);
}

export default function AdminAttendancePage() {
  const now = new Date();
  const [year, setYear] = useState(now.getFullYear());
  const [month, setMonth] = useState(now.getMonth() + 1);
  const [selectedEmployeeId, setSelectedEmployeeId] = useState<number | null>(
    null
  );
  const [employees, setEmployees] = useState<EmployeeSummary[]>([]);
  const [data, setData] = useState<EmployeeMonthlyAttendance[]>([]);
  const [error, setError] = useState<string | null>(null);
  const [isPending, startTransition] = useTransition();
  const initialized = useRef(false);

  useEffect(() => {
    if (initialized.current) return;
    initialized.current = true;
    startTransition(async () => {
      try {
        const [emps, attendance] = await Promise.all([
          fetchEmployees(),
          fetchAttendance(year, month, null),
        ]);
        setEmployees(emps);
        setData(attendance);
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
        const result = await fetchAttendance(newYear, newMonth, selectedEmployeeId);
        setData(result);
        setError(null);
      } catch {
        setError("データの取得に失敗しました");
      }
    });
  };

  const handleEmployeeChange = (empId: number | null) => {
    setSelectedEmployeeId(empId);
    startTransition(async () => {
      try {
        const result = await fetchAttendance(year, month, empId);
        setData(result);
        setError(null);
      } catch {
        setError("データの取得に失敗しました");
      }
    });
  };

  return (
    <div className="max-w-6xl mx-auto p-6">
      <h1 className="text-2xl font-bold mb-6">勤怠管理（管理者）</h1>
      <div className="flex items-center gap-6 mb-6">
        <MonthSelector year={year} month={month} onChange={handleMonthChange} />
        <EmployeeSelector
          employees={employees}
          selectedId={selectedEmployeeId}
          onChange={handleEmployeeChange}
        />
      </div>
      {isPending && <p className="text-gray-500">読み込み中...</p>}
      {error && <p className="text-red-500">{error}</p>}
      {data.map((emp) => (
        <div key={emp.employeeId} className="mb-8">
          <h2 className="text-lg font-semibold mb-2">{emp.employeeName}</h2>
          <AttendanceTable records={emp.records} />
        </div>
      ))}
    </div>
  );
}
