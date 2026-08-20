import Link from "next/link";
import { notFound } from "next/navigation";
import { cookies } from "next/headers";
import { ArrowLeft, CreditCard } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { StatusBadge } from "@/components/status-badge";
import { apiUrl } from "@/lib/api";
import { formatCurrency, formatDateTime } from "@/lib/format";
import type { Invoice } from "@/lib/types";

async function getInvoice(id: string): Promise<Invoice | null> {
  const cookieStore = await cookies();
  const apiKey = cookieStore.get("apiKey")?.value;
  const response = await fetch(apiUrl(`/invoice/${id}`), {
    headers: { "X-API-Key": apiKey ?? "" },
    next: { tags: [`accounts/${apiKey}/invoices/${id}`] },
  });

  if (response.status === 404) return null;
  if (!response.ok) throw new Error("Falha ao carregar a fatura");
  return response.json();
}

export default async function InvoiceDetailsPage({
  params,
}: {
  params: Promise<{ id: string }>;
}) {
  const { id } = await params;
  const invoice = await getInvoice(id);

  if (!invoice) notFound();

  return (
    <div className="space-y-6">
      <div className="flex flex-wrap items-center gap-4">
        <Button variant="ghost" size="sm" nativeButton={false} render={<Link href="/invoices" />}>
          <ArrowLeft className="size-4" />
          Voltar
        </Button>

        <div className="min-w-0 flex-1">
          <div className="flex flex-wrap items-center gap-3">
            <h1 className="text-xl font-semibold tracking-tight">
              Fatura #{invoice.id.slice(0, 8)}
            </h1>
            <StatusBadge status={invoice.status} />
          </div>
          <p className="text-sm text-muted-foreground">
            Criada em {formatDateTime(invoice.created_at)}
          </p>
        </div>

        <p className="font-mono text-2xl font-semibold tabular-nums">
          {formatCurrency(invoice.amount)}
        </p>
      </div>

      <div className="grid grid-cols-1 gap-6 md:grid-cols-2">
        <Card>
          <CardHeader>
            <CardTitle>Informações da fatura</CardTitle>
          </CardHeader>
          <CardContent className="space-y-3">
            <Row label="ID da fatura" value={invoice.id} mono />
            <Row label="Valor" value={formatCurrency(invoice.amount)} />
            <Row label="Data de criação" value={formatDateTime(invoice.created_at)} />
            <Row label="Última atualização" value={formatDateTime(invoice.updated_at)} />
            <Row label="Descrição" value={invoice.description} last />
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle>Método de pagamento</CardTitle>
          </CardHeader>
          <CardContent className="space-y-3">
            <Row
              label="Tipo"
              value={
                invoice.payment_type === "credit_card"
                  ? "Cartão de crédito"
                  : invoice.payment_type
              }
            />
            <Row
              label="Cartão"
              value={`•••• •••• •••• ${invoice.card_last_digits}`}
              mono
              last
            />
            <div className="mt-4 flex items-center gap-2 rounded-lg border border-dashed p-3 text-xs text-muted-foreground">
              <CreditCard className="size-4" />
              Pagamento processado com segurança pelo GPayments Gateway
            </div>
          </CardContent>
        </Card>
      </div>
    </div>
  );
}

function Row({
  label,
  value,
  mono,
  last,
}: {
  label: string;
  value: string;
  mono?: boolean;
  last?: boolean;
}) {
  return (
    <div
      className={
        last
          ? "flex justify-between gap-4"
          : "flex justify-between gap-4 border-b pb-3"
      }
    >
      <span className="text-sm text-muted-foreground">{label}</span>
      <span
        className={
          mono
            ? "font-mono text-sm font-medium text-right"
            : "text-sm font-medium text-right"
        }
      >
        {value}
      </span>
    </div>
  );
}
