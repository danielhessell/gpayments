"use client";

import { useActionState } from "react";
import { InfoIcon, KeyRound, Loader2 } from "lucide-react";
import { Alert, AlertDescription, AlertTitle } from "@/components/ui/alert";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { loginAction, type LoginState } from "./actions";

const initialState: LoginState = {};

export function AuthForm() {
  const [state, formAction, isPending] = useActionState(
    loginAction,
    initialState
  );

  return (
    <form className="space-y-5" action={formAction}>
      <div className="space-y-2">
        <Label htmlFor="apiKey">API Key</Label>
        <div className="relative">
          <KeyRound className="pointer-events-none absolute left-3 top-1/2 size-4 -translate-y-1/2 text-muted-foreground" />
          <Input
            id="apiKey"
            name="apiKey"
            placeholder="Digite sua API Key"
            className="pl-9 font-mono"
            autoComplete="off"
            autoFocus
          />
        </div>
        {state.error && (
          <p className="text-sm text-destructive">{state.error}</p>
        )}
      </div>

      <Button type="submit" className="w-full" disabled={isPending}>
        {isPending ? (
          <>
            <Loader2 className="size-4 animate-spin" />
            Entrando...
          </>
        ) : (
          "Entrar no painel"
        )}
      </Button>

      <Alert>
        <InfoIcon className="size-4" />
        <AlertTitle>Como obter uma API Key?</AlertTitle>
        <AlertDescription>
          Para obter sua API Key, você precisa criar uma conta de comerciante.
          Entre em contato com nosso suporte para mais informações.
        </AlertDescription>
      </Alert>
    </form>
  );
}
