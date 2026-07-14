"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";
import { useAuth } from "@/context/AuthContext";
import { withBasePath } from "@/lib/api-client";
import type { LoginResponse } from "@/types/api";

interface HeaderProps {
  user: LoginResponse | null;
}

export default function Header({ user }: HeaderProps) {
  const pathname = usePathname();
  const { logout } = useAuth();

  if (!user) return null;

  const navItems = [
    { href: "/home", label: "打刻" },
    { href: "/attendance", label: "勤怠一覧" },
  ];

  if (user.role === "ADMIN") {
    navItems.push({ href: "/admin/attendance", label: "管理者一覧" });
  }

  const handleLogout = () => {
    logout();
    window.location.href = withBasePath("/login");
  };

  return (
    <header className="bg-white/80 backdrop-blur-md border-b border-slate-200 px-6 py-3 sticky top-0 z-50">
      <div className="flex items-center justify-between">
        <div className="flex items-center gap-6">
          <span className="text-lg font-bold text-cyan-700 tracking-tight">
            勤怠管理
          </span>
          <nav className="flex gap-1">
            {navItems.map((item) => (
              <Link
                key={item.href}
                href={item.href}
                className={`px-3 py-1.5 rounded-lg text-sm font-medium transition-colors ${
                  pathname === item.href
                    ? "bg-cyan-50 text-cyan-700"
                    : "text-slate-600 hover:text-slate-900 hover:bg-slate-50"
                }`}
              >
                {item.label}
              </Link>
            ))}
          </nav>
        </div>
        <div className="flex items-center gap-4">
          <span className="text-sm text-slate-600 font-medium">{user.name}</span>
          <button
            onClick={handleLogout}
            className="text-xs text-slate-400 hover:text-slate-600 transition-colors"
          >
            ログアウト
          </button>
        </div>
      </div>
    </header>
  );
}
