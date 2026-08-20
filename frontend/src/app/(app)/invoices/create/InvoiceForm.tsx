"use client";

import { useActionState, useState } from "react";
import { CreditCard, Loader2 } from "lucide-react";
import { Input } from "@/components/ui/input";
import { Textarea } from "@/components/ui/textarea";
import { Button } from "@/components/ui/button";
import { Label } from "@/components/ui/label";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { CardPreview } from "@/components/card-preview";
import { createInvoiceAction, type CreateInvoiceState } from "./create-invoice-action";

const initialState: CreateInvoiceState = {};

function formatCardNumber(value: string) {
  return value
    .replace(/\D/g, "")
    .slice(0, 16)
    .replace(/(.{4})/g, "$1 ")
    .trim();
}

function formatExpiry(value: string) {
  const digits = value.replace(/\D/g, "").slice(0, 6);
  if (digits.length <= 2) return digits;
  return `${digits.slice(0, 2)}/${digits.slice(2)}`;
}

export function InvoiceForm() {
  const [state, formAction, isPending] = useActionState(
    createInvoiceAction,
    initialState
  );

  const [cardNumber, setCardNumber] = useState("1111 1111 1111 1111");
  const [expiry, setExpiry] = useState("12/2025");
  const [cardholderName, setCardholderName] = useState("Nome Sobrenome");

  return (
    <form action={formAction} className="grid grid-cols-1 gap-6 lg:grid-cols-5">
      <Card className="lg:col-span-3">
        <CardHeader>
          <CardTitle>Detalhes do pagamento</CardTitle>
        </CardHeader>
        <CardContent className="space-y-5">
          <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
            <div className="space-y-2">
              <Label htmlFor="amount">Valor (R$)</Label>
              <Input
                id="amount"
                name="amount"
                type="number"
                step={0.01}
                min={0}
                defaultValue={0.01}
                placeholder="0,00"
              />
            </div>
          </div>

          <div className="space-y-2">
            <Label htmlFor="description">Descrição</Label>
            <Textarea
              id="description"
              name="description"
              placeholder="Descreva o motivo do pagamento"
              defaultValue="Pagamento de fatura"
              className="min-h-28"
            />
          </div>

          <div className="space-y-2">
            <Label htmlFor="cardNumber">Número do cartão</Label>
            <div className="relative">
              <Input
                id="cardNumber"
                name="cardNumber"
                placeholder="0000 0000 0000 0000"
                value={cardNumber}
                onChange={(e) => setCardNumber(formatCardNumber(e.target.value))}
                className="pl-9 font-mono"
                inputMode="numeric"
              />
              <CreditCard className="pointer-events-none absolute left-3 top-1/2 size-4 -translate-y-1/2 text-muted-foreground" />
            </div>
          </div>

          <div className="grid grid-cols-2 gap-4">
            <div className="space-y-2">
              <Label htmlFor="expiryDate">Validade</Label>
              <Input
                id="expiryDate"
                name="expiryDate"
                placeholder="MM/AAAA"
                value={expiry}
                onChange={(e) => setExpiry(formatExpiry(e.target.value))}
                className="font-mono"
                inputMode="numeric"
              />
            </div>

            <div className="space-y-2">
              <Label htmlFor="cvv">CVV</Label>
              <Input
                id="cvv"
                name="cvv"
                placeholder="123"
                defaultValue="123"
                maxLength={4}
                className="font-mono"
                inputMode="numeric"
              />
            </div>
          </div>

          <div className="space-y-2">
            <Label htmlFor="cardholderName">Nome no cartão</Label>
            <Input
              id="cardholderName"
              name="cardholderName"
              placeholder="Como aparece no cartão"
              value={cardholderName}
              onChange={(e) => setCardholderName(e.target.value)}
            />
          </div>

          {state.error && (
            <p className="text-sm text-destructive">{state.error}</p>
          )}
        </CardContent>
      </Card>

      <div className="flex flex-col gap-4 lg:col-span-2">
        <CardPreview number={cardNumber} name={cardholderName} expiry={expiry} />

        <Card>
          <CardContent className="space-y-3 text-xs text-muted-foreground">
            <p>
              Transações acima de <strong className="text-foreground">R$ 10.000,00</strong> são
              enviadas para análise e ficam com status pendente.
            </p>
            <p>Transações menores são processadas imediatamente.</p>
          </CardContent>
        </Card>

        <Button type="submit" size="lg" disabled={isPending} className="w-full">
          {isPending ? (
            <>
              <Loader2 className="size-4 animate-spin" />
              Processando...
            </>
          ) : (
            <>
              <CreditCard className="size-4" />
              Processar pagamento
            </>
          )}
        </Button>
      </div>
    </form>
  );
}
