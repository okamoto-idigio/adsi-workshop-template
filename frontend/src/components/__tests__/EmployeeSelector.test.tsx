import { render, screen, fireEvent } from "@testing-library/react";
import { describe, it, expect, vi } from "vitest";
import { EmployeeSelector } from "../EmployeeSelector";
import type { EmployeeSummary } from "@/types/api";

describe("EmployeeSelector", () => {
  const employees: EmployeeSummary[] = [
    { id: 1, name: "田中太郎" },
    { id: 2, name: "鈴木花子" },
  ];

  it("社員一覧がドロップダウンに表示される", () => {
    render(<EmployeeSelector employees={employees} selectedId={null} onChange={vi.fn()} />);
    expect(screen.getByRole("option", { name: "全社員" })).toBeInTheDocument();
    expect(screen.getByRole("option", { name: "田中太郎" })).toBeInTheDocument();
    expect(screen.getByRole("option", { name: "鈴木花子" })).toBeInTheDocument();
  });

  it("選択時にコールバックが発火する", () => {
    const onChange = vi.fn();
    render(<EmployeeSelector employees={employees} selectedId={null} onChange={onChange} />);
    fireEvent.change(screen.getByRole("combobox"), { target: { value: "1" } });
    expect(onChange).toHaveBeenCalledWith(1);
  });

  it("全社員を選択するとnullが返る", () => {
    const onChange = vi.fn();
    render(<EmployeeSelector employees={employees} selectedId={1} onChange={onChange} />);
    fireEvent.change(screen.getByRole("combobox"), { target: { value: "" } });
    expect(onChange).toHaveBeenCalledWith(null);
  });
});
