"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";
import type { LoginResponse } from "@/types/api";

interface HeaderProps {
  user: LoginResponse | null;
}

export default function Header({ user }: HeaderProps) {
  const pathname = usePathname();

  if (!user) return null;

  const navItems = [
    { href: "/", label: "打刻" },
    { href: "/attendance", label: "勤怠一覧" },
  ];

  if (user.role === "ADMIN") {
    navItems.push({ href: "/admin/attendance", label: "管理者一覧" });
  }

  return (
    <header className="bg-white border-b border-gray-200 px-6 py-3">
      <div className="flex items-center justify-between">
        <nav className="flex gap-4">
          {navItems.map((item) => (
            <Link
              key={item.href}
              href={item.href}
              className={`px-3 py-2 rounded-md text-sm font-medium ${
                pathname === item.href
                  ? "bg-blue-100 text-blue-700"
                  : "text-gray-600 hover:text-gray-900 hover:bg-gray-100"
              }`}
            >
              {item.label}
            </Link>
          ))}
        </nav>
        <span className="text-sm text-gray-600">{user.name}</span>
      </div>
    </header>
  );
}
