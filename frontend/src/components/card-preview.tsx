import { CreditCard, Wifi } from "lucide-react";

export function CardPreview({
  number,
  name,
  expiry,
}: {
  number: string;
  name: string;
  expiry: string;
}) {
  const digits = number.replace(/\s/g, "").padEnd(16, "•");
  const groups = digits.match(/.{1,4}/g) ?? [];

  return (
    <div className="relative aspect-[1.586/1] w-full max-w-sm overflow-hidden rounded-2xl border border-white/8 bg-gradient-to-br from-[#141b23] to-[#0a0d0b] p-5 text-foreground shadow-lg">
      <div
        className="pointer-events-none absolute -right-16 -top-20 size-56 rounded-full bg-primary/25 blur-2xl"
        aria-hidden="true"
      />

      <div className="relative flex items-start justify-between">
        <CreditCard className="size-7 text-gold" />
        <Wifi className="size-5 rotate-90 opacity-60" />
      </div>

      <p className="relative mt-6 font-mono text-lg tracking-widest sm:text-xl">
        {groups.join(" ")}
      </p>

      <div className="relative mt-5 flex items-end justify-between gap-4">
        <div className="min-w-0">
          <p className="text-[0.65rem] uppercase tracking-wide text-muted-foreground">
            Titular
          </p>
          <p className="truncate text-sm font-medium uppercase">
            {name || "Nome Sobrenome"}
          </p>
        </div>
        <div className="shrink-0 text-right">
          <p className="text-[0.65rem] uppercase tracking-wide text-muted-foreground">
            Validade
          </p>
          <p className="font-mono text-sm font-medium">{expiry || "MM/AAAA"}</p>
        </div>
      </div>
    </div>
  );
}
