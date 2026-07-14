"use client";

interface ClockButtonProps {
  label: string;
  onClick: () => void;
  disabled: boolean;
  variant: "clockIn" | "clockOut";
}

export default function ClockButton({
  label,
  onClick,
  disabled,
  variant,
}: ClockButtonProps) {
  const baseClasses =
    "px-8 py-4 rounded-lg text-lg font-semibold transition-colors disabled:opacity-50 disabled:cursor-not-allowed";
  const variantClasses =
    variant === "clockIn"
      ? "bg-green-600 text-white hover:bg-green-700"
      : "bg-red-600 text-white hover:bg-red-700";

  return (
    <button
      onClick={onClick}
      disabled={disabled}
      className={`${baseClasses} ${variantClasses}`}
    >
      {label}
    </button>
  );
}
