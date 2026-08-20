"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";
import { FileText, LogOut, PlusCircle } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Logo } from "@/components/logo";
import { cn } from "@/lib/utils";
import { formatCurrency } from "@/lib/format";
import type { Account } from "@/lib/types";

const navItems = [
  { href: "/invoices", label: "Faturas", icon: FileText },
  { href: "/invoices/create", label: "Nova Fatura", icon: PlusCircle },
];

export function AppSidebar({
  account,
  onLogout,
}: {
  account: Account | null;
  onLogout: () => void;
}) {
  const pathname = usePathname();

  return (
    <aside className="flex h-full w-64 shrink-0 flex-col border-r border-sidebar-border bg-sidebar px-4 py-6">
      <div className="px-2">
        <Logo />
      </div>

      <nav className="mt-8 flex flex-col gap-1">
        {navItems.map((item) => {
          const active =
            item.href === "/invoices"
              ? pathname === "/invoices"
              : pathname.startsWith(item.href);
          const Icon = item.icon;
          return (
            <Link
              key={item.href}
              href={item.href}
              className={cn(
                "flex items-center gap-2.5 rounded-lg px-3 py-2 text-sm font-medium transition-colors",
                active
                  ? "bg-sidebar-accent text-sidebar-accent-foreground"
                  : "text-muted-foreground hover:bg-sidebar-accent/60 hover:text-sidebar-accent-foreground"
              )}
            >
              <Icon className="size-4" />
              {item.label}
            </Link>
          );
        })}
      </nav>

      <div className="mt-auto space-y-3">
        {account && (
          <div className="rounded-xl border border-sidebar-border bg-card/60 p-3.5">
            <p className="truncate text-sm font-medium text-foreground">
              {account.name}
            </p>
            <p className="truncate text-xs text-muted-foreground">
              {account.email}
            </p>
            <div className="mt-2.5 flex items-baseline justify-between border-t border-sidebar-border pt-2.5">
              <span className="text-xs text-muted-foreground">Saldo</span>
              <span className="font-mono text-sm font-semibold text-success">
                {formatCurrency(account.balance)}
              </span>
            </div>
          </div>
        )}

        <form action={onLogout}>
          <Button
            variant="ghost"
            size="sm"
            type="submit"
            className="w-full justify-start gap-2.5 text-muted-foreground hover:text-destructive"
          >
            <LogOut className="size-4" />
            Sair
          </Button>
        </form>
      </div>
    </aside>
  );
}
