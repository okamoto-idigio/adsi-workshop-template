import { render, screen, fireEvent } from "@testing-library/react";
import { describe, it, expect, vi } from "vitest";
import { MonthSelector } from "../MonthSelector";

describe("MonthSelector", () => {
  it("現在の年月が表示される", () => {
    render(<MonthSelector year={2026} month={7} onChange={vi.fn()} />);
    expect(screen.getByText("2026年7月")).toBeInTheDocument();
  });

  it("前月ボタンで月が1つ戻る", () => {
    const onChange = vi.fn();
    render(<MonthSelector year={2026} month={7} onChange={onChange} />);
    fireEvent.click(screen.getByRole("button", { name: "前月" }));
    expect(onChange).toHaveBeenCalledWith(2026, 6);
  });

  it("翌月ボタンで月が1つ進む", () => {
    const onChange = vi.fn();
    render(<MonthSelector year={2026} month={7} onChange={onChange} />);
    fireEvent.click(screen.getByRole("button", { name: "翌月" }));
    expect(onChange).toHaveBeenCalledWith(2026, 8);
  });

  it("1月の前月は前年12月になる", () => {
    const onChange = vi.fn();
    render(<MonthSelector year={2026} month={1} onChange={onChange} />);
    fireEvent.click(screen.getByRole("button", { name: "前月" }));
    expect(onChange).toHaveBeenCalledWith(2025, 12);
  });

  it("12月の翌月は翌年1月になる", () => {
    const onChange = vi.fn();
    render(<MonthSelector year={2026} month={12} onChange={onChange} />);
    fireEvent.click(screen.getByRole("button", { name: "翌月" }));
    expect(onChange).toHaveBeenCalledWith(2027, 1);
  });
});
