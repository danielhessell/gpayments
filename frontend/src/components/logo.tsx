import { cn } from "@/lib/utils";

export function LogoMark({ className }: { className?: string }) {
  return (
    <div
      className={cn(
        "flex size-8 items-center justify-center rounded-lg bg-gradient-to-br from-primary to-[#0a0d0b] text-primary-foreground ring-1 ring-gold/40",
        className
      )}
    >
      <svg
        viewBox="0 0 24 24"
        fill="none"
        className="size-4.5"
        aria-hidden="true"
      >
        <rect x="2" y="5" width="20" height="14" rx="2.5" fill="currentColor" fillOpacity="0.25" />
        <rect x="2" y="8.5" width="20" height="2.5" fill="currentColor" />
        <rect x="4.5" y="14" width="6" height="2" rx="1" fill="currentColor" />
      </svg>
    </div>
  );
}

export function Logo({ className }: { className?: string }) {
  return (
    <div className={cn("flex items-center gap-2.5", className)}>
      <LogoMark />
      <span className="text-base font-semibold tracking-tight text-foreground">
        GPayments
      </span>
    </div>
  );
}
