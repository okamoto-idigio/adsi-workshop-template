"use client";

import { usePathname } from "next/navigation";
import { useAuth } from "@/context/AuthContext";
import Header from "@/components/Header";

interface AppShellProps {
  children: React.ReactNode;
}

export default function AppShell({ children }: AppShellProps) {
  const pathname = usePathname();
  const { user } = useAuth();

  const isLoginPage = pathname?.endsWith("/login");

  return (
    <>
      {!isLoginPage && <Header user={user} />}
      {children}
    </>
  );
}
