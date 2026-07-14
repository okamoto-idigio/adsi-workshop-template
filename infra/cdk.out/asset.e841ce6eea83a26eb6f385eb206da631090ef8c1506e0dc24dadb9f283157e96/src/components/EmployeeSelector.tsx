"use client";

import type { EmployeeSummary } from "@/types/api";

interface EmployeeSelectorProps {
  employees: EmployeeSummary[];
  selectedId: number | null;
  onChange: (employeeId: number | null) => void;
}

export function EmployeeSelector({
  employees,
  selectedId,
  onChange,
}: EmployeeSelectorProps) {
  const handleChange = (e: React.ChangeEvent<HTMLSelectElement>) => {
    const value = e.target.value;
    onChange(value === "" ? null : Number(value));
  };

  return (
    <select
      value={selectedId ?? ""}
      onChange={handleChange}
      className="border border-gray-300 rounded px-3 py-2"
    >
      <option value="">全社員</option>
      {employees.map((emp) => (
        <option key={emp.id} value={emp.id}>
          {emp.name}
        </option>
      ))}
    </select>
  );
}
