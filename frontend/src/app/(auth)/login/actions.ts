"use server";

import { cookies } from "next/headers";
import { redirect } from "next/navigation";
import { apiUrl } from "@/lib/api";

export interface LoginState {
  error?: string;
}

export async function loginAction(
  _prevState: LoginState,
  formData: FormData
): Promise<LoginState> {
  const apiKey = formData.get("apiKey")?.toString().trim();

  if (!apiKey) {
    return { error: "Informe sua API Key." };
  }

  const response = await fetch(apiUrl("/accounts"), {
    headers: {
      "X-API-Key": apiKey,
    },
  });

  if (!response.ok) {
    return { error: "API Key inválida. Verifique e tente novamente." };
  }

  const cookieStore = await cookies();
  cookieStore.set("apiKey", apiKey, {
    httpOnly: true,
    sameSite: "lax",
    path: "/",
  });

  redirect("/invoices");
}
