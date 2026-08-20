import type React from "react";
import { cookies } from "next/headers";
import { AppSidebar } from "@/components/app-sidebar";
import { apiUrl } from "@/lib/api";
import type { Account } from "@/lib/types";
import { logoutAction } from "./actions";

async function getAccount(): Promise<Account | null> {
  const cookieStore = await cookies();
  const apiKey = cookieStore.get("apiKey")?.value;
  if (!apiKey) return null;

  const response = await fetch(apiUrl("/accounts"), {
    headers: { "X-API-Key": apiKey },
    cache: "no-store",
  });

  if (!response.ok) return null;
  return response.json();
}

export default async function AppLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  const account = await getAccount();

  return (
    <div className="flex min-h-screen">
      <AppSidebar account={account} onLogout={logoutAction} />
      <main className="flex-1 overflow-x-hidden px-6 py-8 sm:px-10">
        <div className="mx-auto max-w-6xl">{children}</div>
      </main>
    </div>
  );
}
