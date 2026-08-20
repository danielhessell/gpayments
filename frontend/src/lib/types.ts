export type InvoiceStatus = "approved" | "pending" | "rejected";

export interface Invoice {
  id: string;
  account_id: string;
  amount: number;
  status: InvoiceStatus;
  description: string;
  payment_type: string;
  card_last_digits: string;
  created_at: string;
  updated_at: string;
}

export interface Account {
  id: string;
  name: string;
  email: string;
  balance: number;
  api_key: string;
  created_at: string;
  updated_at: string;
}
