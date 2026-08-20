import Link from "next/link";
import { cookies } from "next/headers";
import { ArrowUpRight, Eye, PlusIcon, Receipt } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Card, CardContent } from "@/components/ui/card";
import { StatusBadge } from "@/components/status-badge";
import { apiUrl } from "@/lib/api";
import { formatCurrency, formatDate } from "@/lib/format";
import type { Invoice } from "@/lib/types";

async function getInvoices(): Promise<Invoice[]> {
  const cookieStore = await cookies();
  const apiKey = cookieStore.get("apiKey")?.value;
  const response = await fetch(apiUrl("/invoice"), {
    headers: { "X-API-Key": apiKey ?? "" },
    next: { tags: [`accounts/${apiKey}/invoices`] },
  });

  if (!response.ok) return [];
  return response.json();
}

export async function InvoiceList() {
  const invoices = await getInvoices();

  const approvedTotal = invoices
    .filter((i) => i.status === "approved")
    .reduce((sum, i) => sum + i.amount, 0);
  const pendingCount = invoices.filter((i) => i.status === "pending").length;

  return (
    <div className="space-y-6">
      <div className="flex flex-wrap items-center justify-between gap-4">
        <div>
          <h1 className="heading-accent text-2xl font-semibold tracking-tight">
            Faturas
          </h1>
          <p className="text-sm text-muted-foreground">
            Gerencie suas faturas e acompanhe os pagamentos
          </p>
        </div>
        <Button nativeButton={false} render={<Link href="/invoices/create" />}>
          <PlusIcon className="size-4" />
          Nova Fatura
        </Button>
      </div>

      <div className="grid grid-cols-1 gap-4 sm:grid-cols-3">
        <Card>
          <CardContent className="flex items-center justify-between">
            <div>
              <p className="text-xs text-muted-foreground">
                Total aprovado
              </p>
              <p className="mt-1 font-mono text-xl font-semibold tabular-nums text-success">
                {formatCurrency(approvedTotal)}
              </p>
            </div>
            <div className="flex size-9 items-center justify-center rounded-full bg-success/10 text-success">
              <ArrowUpRight className="size-4.5" />
            </div>
          </CardContent>
        </Card>
        <Card>
          <CardContent className="flex items-center justify-between">
            <div>
              <p className="text-xs text-muted-foreground">Pendentes</p>
              <p className="mt-1 font-mono text-xl font-semibold tabular-nums text-warning">
                {pendingCount}
              </p>
            </div>
            <div className="flex size-9 items-center justify-center rounded-full bg-warning/10 text-warning">
              <Receipt className="size-4.5" />
            </div>
          </CardContent>
        </Card>
        <Card>
          <CardContent className="flex items-center justify-between">
            <div>
              <p className="text-xs text-muted-foreground">Total de faturas</p>
              <p className="mt-1 font-mono text-xl font-semibold tabular-nums">
                {invoices.length}
              </p>
            </div>
            <div className="flex size-9 items-center justify-center rounded-full bg-primary/10 text-primary">
              <Receipt className="size-4.5" />
            </div>
          </CardContent>
        </Card>
      </div>

      <Card className="py-0">
        {invoices.length === 0 ? (
          <div className="flex flex-col items-center gap-3 py-16 text-center">
            <Receipt className="size-8 text-muted-foreground" />
            <p className="text-sm text-muted-foreground">
              Nenhuma fatura encontrada
            </p>
            <Button size="sm" nativeButton={false} render={<Link href="/invoices/create" />}>
              <PlusIcon className="size-4" />
              Criar primeira fatura
            </Button>
          </div>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-sm">
              <thead>
                <tr className="border-b">
                  <th className="px-5 py-3 text-left font-medium text-muted-foreground">
                    ID
                  </th>
                  <th className="px-5 py-3 text-left font-medium text-muted-foreground">
                    Data
                  </th>
                  <th className="px-5 py-3 text-left font-medium text-muted-foreground">
                    Descrição
                  </th>
                  <th className="px-5 py-3 text-left font-medium text-muted-foreground">
                    Valor
                  </th>
                  <th className="px-5 py-3 text-left font-medium text-muted-foreground">
                    Status
                  </th>
                  <th className="px-5 py-3 text-right font-medium text-muted-foreground">
                    Ações
                  </th>
                </tr>
              </thead>
              <tbody>
                {invoices.map((invoice) => (
                  <tr
                    key={invoice.id}
                    className="border-b last:border-0 hover:bg-muted/40"
                  >
                    <td className="px-5 py-3.5 font-mono text-xs text-muted-foreground">
                      {invoice.id.slice(0, 8)}
                    </td>
                    <td className="px-5 py-3.5 whitespace-nowrap">
                      {formatDate(invoice.created_at)}
                    </td>
                    <td className="px-5 py-3.5 max-w-60 truncate">
                      {invoice.description}
                    </td>
                    <td className="px-5 py-3.5 font-mono font-medium tabular-nums whitespace-nowrap">
                      {formatCurrency(invoice.amount)}
                    </td>
                    <td className="px-5 py-3.5">
                      <StatusBadge status={invoice.status} />
                    </td>
                    <td className="px-5 py-3.5 text-right">
                      <Button
                        variant="ghost"
                        size="icon"
                        nativeButton={false}
                        render={<Link href={`/invoices/${invoice.id}`} />}
                      >
                        <Eye className="size-4" />
                      </Button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </Card>
    </div>
  );
}
