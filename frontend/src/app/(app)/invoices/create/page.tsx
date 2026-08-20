import { InvoiceForm } from "./InvoiceForm";

export default function CreateInvoicePage() {
  return (
    <div className="space-y-6">
      <div>
        <h1 className="heading-accent text-2xl font-semibold tracking-tight">
          Nova Fatura
        </h1>
        <p className="text-sm text-muted-foreground">
          Preencha os dados abaixo para processar um novo pagamento
        </p>
      </div>

      <InvoiceForm />
    </div>
  );
}
