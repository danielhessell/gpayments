import { cn } from "@/lib/utils";
import type { InvoiceStatus } from "@/lib/types";

const statusConfig: Record<
  InvoiceStatus,
  { text: string; dot: string; className: string }
> = {
  approved: {
    text: "Aprovado",
    dot: "bg-success",
    className: "bg-success/10 text-success ring-success/20",
  },
  pending: {
    text: "Pendente",
    dot: "bg-warning",
    className: "bg-warning/10 text-warning ring-warning/20",
  },
  rejected: {
    text: "Rejeitado",
    dot: "bg-danger",
    className: "bg-danger/10 text-danger ring-danger/20",
  },
};

export function StatusBadge({ status }: { status: InvoiceStatus }) {
  const config = statusConfig[status];

  return (
    <span
      className={cn(
        "inline-flex items-center gap-1.5 rounded-full px-2.5 py-1 text-xs font-medium ring-1 ring-inset",
        config.className
      )}
    >
      <span className={cn("size-1.5 rounded-full", config.dot)} />
      {config.text}
    </span>
  );
}
