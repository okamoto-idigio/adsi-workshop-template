"use client";

interface MonthSelectorProps {
  year: number;
  month: number;
  onChange: (year: number, month: number) => void;
}

export function MonthSelector({ year, month, onChange }: MonthSelectorProps) {
  const handlePrev = () => {
    if (month === 1) {
      onChange(year - 1, 12);
    } else {
      onChange(year, month - 1);
    }
  };

  const handleNext = () => {
    if (month === 12) {
      onChange(year + 1, 1);
    } else {
      onChange(year, month + 1);
    }
  };

  return (
    <div className="flex items-center gap-4">
      <button
        onClick={handlePrev}
        aria-label="前月"
        className="px-3 py-1 rounded border border-gray-300 hover:bg-gray-100"
      >
        ←
      </button>
      <span className="text-lg font-medium">
        {year}年{month}月
      </span>
      <button
        onClick={handleNext}
        aria-label="翌月"
        className="px-3 py-1 rounded border border-gray-300 hover:bg-gray-100"
      >
        →
      </button>
    </div>
  );
}
