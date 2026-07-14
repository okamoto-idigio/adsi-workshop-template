import { render, screen } from "@testing-library/react";
import { describe, it, expect } from "vitest";
import { AttendanceTable } from "../AttendanceTable";
import type { AttendanceResponse } from "@/types/api";

describe("AttendanceTable", () => {
  const records: AttendanceResponse[] = [
    { date: "2026-07-01", clockInTime: "09:00:00", clockOutTime: "18:00:00", workDurationMinutes: 480 },
    { date: "2026-07-02", clockInTime: "09:30:00", clockOutTime: null, workDurationMinutes: null },
  ];

  it("レコードがテーブルに表示される", () => {
    render(<AttendanceTable records={records} />);
    expect(screen.getByText("2026-07-01")).toBeInTheDocument();
    expect(screen.getByText("09:00")).toBeInTheDocument();
    expect(screen.getByText("18:00")).toBeInTheDocument();
    expect(screen.getByText("8:00")).toBeInTheDocument();
  });

  it("退勤前のレコードはハイフンが表示される", () => {
    render(<AttendanceTable records={records} />);
    const cells = screen.getAllByText("-");
    expect(cells.length).toBeGreaterThanOrEqual(1);
  });

  it("レコードが空の場合はメッセージが表示される", () => {
    render(<AttendanceTable records={[]} />);
    expect(screen.getByText("データがありません")).toBeInTheDocument();
  });
});
