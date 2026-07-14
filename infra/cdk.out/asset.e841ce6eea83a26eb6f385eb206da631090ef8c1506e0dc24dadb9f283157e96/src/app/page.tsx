"use client";

import { useEffect } from "react";
import { withBasePath } from "@/lib/api-client";

export default function RootPage() {
  useEffect(() => {
    window.location.href = withBasePath("/home");
  }, []);

  return null;
}
