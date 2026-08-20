import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import { AuthForm } from "./AuthForm";

export default function LoginPage() {
  return (
    <Card className="w-full max-w-md border-border/60 shadow-xl shadow-black/20 backdrop-blur">
      <CardHeader className="text-center">
        <CardTitle className="heading-accent heading-accent--center inline-block text-2xl">
          Autenticação
        </CardTitle>
        <CardDescription>
          Insira sua API Key para acessar o painel do gateway
        </CardDescription>
      </CardHeader>
      <CardContent>
        <AuthForm />
      </CardContent>
    </Card>
  );
}
