"use server";

import { updateTag } from "next/cache";
import { cookies } from "next/headers";
import { redirect } from "next/navigation";
import { apiUrl } from "@/lib/api";

export interface CreateInvoiceState {
  error?: string;
}

export async function createInvoiceAction(
  _prevState: CreateInvoiceState,
  formData: FormData
): Promise<CreateInvoiceState> {
  const cookieStore = await cookies();
  const apiKey = cookieStore.get("apiKey")?.value;

  const amount = formData.get("amount")?.toString().replace(",", ".");
  const description = formData.get("description")?.toString() ?? "";
  const cardNumber = formData.get("cardNumber")?.toString().replace(/\s/g, "") ?? "";
  const [expiryMonth, expiryYear] = (formData.get("expiryDate")?.toString() ?? "")
    .split("/")
    .map((part) => part.trim());
  const cvv = formData.get("cvv")?.toString() ?? "";
  const cardholderName = formData.get("cardholderName")?.toString() ?? "";

  if (!amount || !description || !cardNumber || !expiryMonth || !expiryYear || !cvv || !cardholderName) {
    return { error: "Preencha todos os campos para processar o pagamento." };
  }

  const response = await fetch(apiUrl("/invoice"), {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
      "X-API-Key": apiKey ?? "",
    },
    body: JSON.stringify({
      amount: parseFloat(amount),
      description,
      card_number: cardNumber,
      expiry_month: parseInt(expiryMonth, 10),
      expiry_year: parseInt(expiryYear, 10),
      cvv,
      cardholder_name: cardholderName,
      payment_type: "credit_card",
    }),
  });

  if (!response.ok) {
    return { error: "Não foi possível processar o pagamento. Verifique os dados e tente novamente." };
  }

  const invoice = await response.json();

  updateTag(`accounts/${apiKey}/invoices`);
  updateTag(`accounts/${apiKey}/invoices/${invoice.id}`);

  redirect(`/invoices/${invoice.id}`);
}
