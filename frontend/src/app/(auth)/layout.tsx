import type React from "react";
import { Logo } from "@/components/logo";

export default function AuthLayout({ children }: { children: React.ReactNode }) {
  return (
    <div className="relative flex min-h-screen flex-col overflow-hidden">
      <div
        className="pointer-events-none absolute inset-0 -z-10"
        style={{
          backgroundImage:
            "radial-gradient(60rem 30rem at 15% -10%, color-mix(in oklch, var(--primary) 22%, transparent), transparent), radial-gradient(50rem 25rem at 100% 10%, color-mix(in oklch, var(--gold) 12%, transparent), transparent)",
        }}
      />
      <header className="px-6 py-6 sm:px-10">
        <Logo />
      </header>
      <main className="flex flex-1 items-center justify-center px-6 pb-16">
        {children}
      </main>
      <footer className="px-6 pb-8 text-center text-xs text-muted-foreground">
        © {new Date().getFullYear()} GPayments. Todos os direitos reservados.
      </footer>
    </div>
  );
}
